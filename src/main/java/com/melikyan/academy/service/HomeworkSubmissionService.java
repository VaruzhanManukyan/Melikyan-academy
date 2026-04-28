package com.melikyan.academy.service;

import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.Course;
import com.melikyan.academy.repository.*;
import org.springframework.http.HttpStatus;
import com.melikyan.academy.entity.ContentItem;
import com.melikyan.academy.entity.UserProcess;
import com.melikyan.academy.entity.HomeworkTask;
import com.melikyan.academy.entity.HomeworkSubmission;
import com.melikyan.academy.entity.enums.HomeworkStatus;
import org.springframework.security.core.Authentication;
import com.melikyan.academy.entity.enums.RegistrationStatus;
import com.melikyan.academy.mapper.HomeworkSubmissionMapper;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import com.melikyan.academy.dto.response.homeworkSubmission.HomeworkSubmissionResponse;
import com.melikyan.academy.dto.request.homeworkSubmission.UpdateHomeworkSubmissionRequest;
import com.melikyan.academy.dto.request.homeworkSubmission.CreateHomeworkSubmissionRequest;
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
public class HomeworkSubmissionService {
    private final UserRepository userRepository;
    private final UserProcessRepository userProcessRepository;
    private final HomeworkTaskRepository homeworkTaskRepository;
    private final HomeworkSubmissionMapper homeworkSubmissionMapper;
    private final HomeworkSubmissionRepository homeworkSubmissionRepository;
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

    private HomeworkTask getHomeworkTaskById(UUID id) {
        return homeworkTaskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Homework task not found with id: " + id
                ));
    }

    private HomeworkSubmission getHomeworkSubmissionEntityById(UUID id) {
        return homeworkSubmissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Homework submission not found with id: " + id
                ));
    }

    private HomeworkSubmission getHomeworkSubmissionEntityByIdAndUserId(UUID id, UUID userId) {
        return homeworkSubmissionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Homework submission not found with id: " + id
                ));
    }

    private ContentItem getContentItemFromTask(HomeworkTask task) {
        if (task.getHomework() == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Homework task is not linked to homework"
            );
        }

        if (task.getHomework().getLesson() == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Homework is not linked to lesson"
            );
        }

        if (task.getHomework().getLesson().getCourse() == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Lesson is not linked to course"
            );
        }

        Course course = task.getHomework().getLesson().getCourse();

        if (course.getContentItem() == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Course is not linked to content item"
            );
        }

        return course.getContentItem();
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

    private void validateReviewStatus(HomeworkStatus status) {
        if (status == HomeworkStatus.PENDING_REVIEW) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Review status must be PASSED or FAILED"
            );
        }
    }

    private void validateUserHasActiveAccessToCourse(User user, ContentItem contentItem) {
        List<UUID> userIds = productRegistrationRepository.findUserIdsByContentItemIdAndStatus(
                contentItem.getId(),
                RegistrationStatus.ACTIVE
        );

        if (!userIds.contains(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User does not have active access to this course"
            );
        }
    }

    private HomeworkSubmissionResponse updateStatusInternal(
            UUID id,
            HomeworkStatus status,
            String note
    ) {
        validateReviewStatus(status);

        HomeworkSubmission homeworkSubmission = getHomeworkSubmissionEntityById(id);
        homeworkSubmission.setStatus(status);
        homeworkSubmission.setNote(normalizeNote(note));

        try {
            HomeworkSubmission savedHomeworkSubmission = homeworkSubmissionRepository.saveAndFlush(homeworkSubmission);

            return homeworkSubmissionMapper.toResponse(savedHomeworkSubmission);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Unable to update homework submission status",
                    exception
            );
        }
    }

    public HomeworkSubmissionResponse create(
            CreateHomeworkSubmissionRequest request,
            Authentication authentication
    ) {
        User currentUser = getCurrentUser(authentication);
        HomeworkTask task = getHomeworkTaskById(request.taskId());
        ContentItem contentItem = getContentItemFromTask(task);

        validateUserHasActiveAccessToCourse(currentUser, contentItem);

        if (homeworkSubmissionRepository.existsByUserIdAndTaskId(currentUser.getId(), task.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Homework submission already exists for this task"
            );
        }

        HomeworkSubmission homeworkSubmission = new HomeworkSubmission();
        homeworkSubmission.setStatus(HomeworkStatus.PENDING_REVIEW);
        homeworkSubmission.setAnswerPayload(request.answerPayload());
        homeworkSubmission.setUser(currentUser);
        homeworkSubmission.setTask(task);

        try {
            HomeworkSubmission savedHomeworkSubmission = homeworkSubmissionRepository.saveAndFlush(homeworkSubmission);

            advanceUserProcess(currentUser, contentItem);

            return homeworkSubmissionMapper.toResponse(savedHomeworkSubmission);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Homework submission already exists for this task",
                    exception
            );
        }
    }

    @Transactional(readOnly = true)
    public HomeworkSubmissionResponse getById(UUID id) {
        HomeworkSubmission homeworkSubmission = getHomeworkSubmissionEntityById(id);
        return homeworkSubmissionMapper.toResponse(homeworkSubmission);
    }

    @Transactional(readOnly = true)
    public HomeworkSubmissionResponse getMyById(UUID id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        HomeworkSubmission homeworkSubmission =
                getHomeworkSubmissionEntityByIdAndUserId(id, currentUser.getId());

        return homeworkSubmissionMapper.toResponse(homeworkSubmission);
    }

    @Transactional(readOnly = true)
    public List<HomeworkSubmissionResponse> getMyAll(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        return homeworkSubmissionMapper.toResponseList(
                homeworkSubmissionRepository.findAllByUserId(currentUser.getId())
        );
    }

    @Transactional(readOnly = true)
    public HomeworkSubmissionResponse getMyByTask(UUID taskId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        HomeworkSubmission homeworkSubmission = homeworkSubmissionRepository
                .findByUserIdAndTaskId(currentUser.getId(), taskId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Homework submission not found for task id: " + taskId
                ));

        return homeworkSubmissionMapper.toResponse(homeworkSubmission);
    }

    @Transactional(readOnly = true)
    public List<HomeworkSubmissionResponse> getAllByTask(UUID taskId) {
        getHomeworkTaskById(taskId);

        return homeworkSubmissionMapper.toResponseList(
                homeworkSubmissionRepository.findAllByTaskId(taskId)
        );
    }

    public HomeworkSubmissionResponse update(
            UUID id,
            UpdateHomeworkSubmissionRequest request
    ) {
        return updateStatusInternal(id, request.status(), request.note());
    }

    public void deleteMy(UUID id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        HomeworkSubmission homeworkSubmission = getHomeworkSubmissionEntityByIdAndUserId(id, currentUser.getId());

        ContentItem contentItem = getContentItemFromTask(homeworkSubmission.getTask());

        homeworkSubmissionRepository.delete(homeworkSubmission);

        rollbackUserProcess(currentUser, contentItem);
    }

    public void delete(UUID id) {
        HomeworkSubmission homeworkSubmission = getHomeworkSubmissionEntityById(id);

        User user = homeworkSubmission.getUser();
        ContentItem contentItem = getContentItemFromTask(homeworkSubmission.getTask());

        homeworkSubmissionRepository.delete(homeworkSubmission);

        rollbackUserProcess(user, contentItem);
    }
}