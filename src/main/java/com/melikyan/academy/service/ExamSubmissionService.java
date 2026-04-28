package com.melikyan.academy.service;

import com.melikyan.academy.entity.*;
import com.melikyan.academy.repository.*;
import org.springframework.http.HttpStatus;
import com.melikyan.academy.entity.enums.ExamStatus;
import com.melikyan.academy.mapper.ExamSubmissionMapper;
import org.springframework.security.core.Authentication;
import com.melikyan.academy.entity.enums.RegistrationStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import com.melikyan.academy.dto.response.examSubmission.ExamSubmissionResponse;
import com.melikyan.academy.dto.request.examSubmission.CreateExamSubmissionRequest;
import com.melikyan.academy.dto.request.examSubmission.UpdateExamSubmissionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class ExamSubmissionService {
    private final UserRepository userRepository;
    private final ExamTaskRepository examTaskRepository;
    private final ExamSubmissionMapper examSubmissionMapper;
    private final UserProcessRepository userProcessRepository;
    private final ExamSubmissionRepository examSubmissionRepository;
    private final ProductRegistrationRepository productRegistrationRepository;

    private String normalizeNote(String note) {
        if (note == null) {
            return null;
        }

        String normalized = note.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Authenticated user not found"
                ));
    }

    private ExamTask getExamTaskById(UUID id) {
        return examTaskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Exam task not found with id: " + id
                ));
    }

    private ExamSubmission getExamSubmissionEntityById(UUID id) {
        return examSubmissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Exam submission not found with id: " + id
                ));
    }

    private ExamSubmission getExamSubmissionEntityByIdAndUserId(UUID id, UUID userId) {
        return examSubmissionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Exam submission not found with id: " + id
                ));
    }

    private ContentItem getContentItemFromTask(ExamTask task) {
        if (task.getSection() == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Exam task is not linked to exam section"
            );
        }

        if (task.getSection().getExam() == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Exam section is not linked to exam"
            );
        }

        Exam exam = task.getSection().getExam();

        if (exam.getContentItem() == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Exam is not linked to content item"
            );
        }

        return exam.getContentItem();
    }

    private void advanceUserProcess(User user, ContentItem contentItem) {
        UserProcess userProcess = userProcessRepository
                .findByUserIdAndContentItemId(user.getId(), contentItem.getId())
                .orElseGet(() -> UserProcess.builder()
                        .user(user)
                        .contentItem(contentItem)
                        .currentStep(0)
                        .scoreAccumulated(BigDecimal.ZERO)
                        .lastAccessedAt(null)
                        .build());

        userProcess.setCurrentStep(userProcess.getCurrentStep() + 1);
        userProcess.setLastAccessedAt(OffsetDateTime.now());

        userProcessRepository.save(userProcess);
    }

    private void rollbackUserProcess(User user, ContentItem contentItem) {
        UserProcess userProcess = userProcessRepository
                .findByUserIdAndContentItemId(user.getId(), contentItem.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User process not found"
                ));

        userProcess.setCurrentStep(Math.max(0, userProcess.getCurrentStep() - 1));
        userProcess.setLastAccessedAt(OffsetDateTime.now());

        userProcessRepository.save(userProcess);
    }

    private void validateReviewStatus(ExamStatus status) {
        if (status == ExamStatus.PENDING_REVIEW) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Review status must be PASSED or FAILED"
            );
        }
    }

    private void validateUserHasActiveAccessToExam(User user, ContentItem contentItem) {
        List<UUID> userIds = productRegistrationRepository.findUserIdsByContentItemIdAndStatus(
                contentItem.getId(),
                RegistrationStatus.ACTIVE
        );

        if (!userIds.contains(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User does not have active access to this exam"
            );
        }
    }

    private ExamSubmissionResponse updateStatusInternal(
            UUID id,
            ExamStatus status,
            String note
    ) {
        validateReviewStatus(status);

        ExamSubmission examSubmission = getExamSubmissionEntityById(id);
        examSubmission.setStatus(status);
        examSubmission.setNote(normalizeNote(note));

        try {
            ExamSubmission savedExamSubmission = examSubmissionRepository.saveAndFlush(examSubmission);

            return examSubmissionMapper.toResponse(savedExamSubmission);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Unable to update exam submission status",
                    exception
            );
        }
    }

    public ExamSubmissionResponse create(
            CreateExamSubmissionRequest request,
            Authentication authentication
    ) {
        User currentUser = getCurrentUser(authentication);
        ExamTask task = getExamTaskById(request.taskId());
        ContentItem contentItem = getContentItemFromTask(task);

        validateUserHasActiveAccessToExam(currentUser, contentItem);

        if (examSubmissionRepository.existsByUserIdAndTaskId(currentUser.getId(), task.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Exam submission already exists for this task"
            );
        }

        ExamSubmission examSubmission = new ExamSubmission();
        examSubmission.setStatus(ExamStatus.PENDING_REVIEW);
        examSubmission.setAnswerPayload(request.answerPayload());
        examSubmission.setUser(currentUser);
        examSubmission.setTask(task);

        try {
            ExamSubmission savedExamSubmission = examSubmissionRepository.saveAndFlush(examSubmission);

            advanceUserProcess(currentUser, contentItem);

            return examSubmissionMapper.toResponse(savedExamSubmission);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Exam submission already exists for this task",
                    exception
            );
        }
    }

    @Transactional(readOnly = true)
    public ExamSubmissionResponse getById(UUID id) {
        ExamSubmission examSubmission = getExamSubmissionEntityById(id);
        return examSubmissionMapper.toResponse(examSubmission);
    }

    @Transactional(readOnly = true)
    public ExamSubmissionResponse getMyById(UUID id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        ExamSubmission examSubmission = getExamSubmissionEntityByIdAndUserId(id, currentUser.getId());

        return examSubmissionMapper.toResponse(examSubmission);
    }

    @Transactional(readOnly = true)
    public List<ExamSubmissionResponse> getMyAll(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        return examSubmissionMapper.toResponseList(
                examSubmissionRepository.findAllByUserId(currentUser.getId())
        );
    }

    @Transactional(readOnly = true)
    public ExamSubmissionResponse getMyByTask(UUID taskId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        ExamSubmission examSubmission = examSubmissionRepository
                .findByUserIdAndTaskId(currentUser.getId(), taskId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Exam submission not found for task id: " + taskId
                ));

        return examSubmissionMapper.toResponse(examSubmission);
    }

    @Transactional(readOnly = true)
    public List<ExamSubmissionResponse> getAllByTask(UUID taskId) {
        getExamTaskById(taskId);

        return examSubmissionMapper.toResponseList(
                examSubmissionRepository.findAllByTaskId(taskId)
        );
    }

    public ExamSubmissionResponse update(
            UUID id,
            UpdateExamSubmissionRequest request
    ) {
        return updateStatusInternal(id, request.status(), request.note());
    }

    public void deleteMy(UUID id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        ExamSubmission examSubmission  = getExamSubmissionEntityByIdAndUserId(id, currentUser.getId());

        ContentItem contentItem = getContentItemFromTask(examSubmission.getTask());

        examSubmissionRepository.delete(examSubmission);

        rollbackUserProcess(currentUser, contentItem);
    }

    public void delete(UUID id) {
        ExamSubmission examSubmission = getExamSubmissionEntityById(id);

        User user = examSubmission.getUser();
        ContentItem contentItem = getContentItemFromTask(examSubmission.getTask());

        examSubmissionRepository.delete(examSubmission);

        rollbackUserProcess(user, contentItem);
    }
}
