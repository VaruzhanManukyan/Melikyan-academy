package com.melikyan.academy.service;

import com.melikyan.academy.entity.Exam;
import com.melikyan.academy.entity.User;
import org.springframework.http.HttpStatus;
import com.melikyan.academy.mapper.ExamMapper;
import com.melikyan.academy.entity.ContentItem;
import com.melikyan.academy.repository.ExamRepository;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.entity.enums.ContentItemType;
import com.melikyan.academy.dto.response.exam.ExamResponse;
import com.melikyan.academy.repository.ContentItemRepository;
import org.springframework.web.server.ResponseStatusException;
import com.melikyan.academy.dto.request.exam.UpdateExamRequest;
import org.springframework.dao.DataIntegrityViolationException;
import com.melikyan.academy.dto.request.exam.CreateExamRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ExamService {
    private final ExamMapper examMapper;
    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final ContentItemRepository contentItemRepository;

    private String normalizeTitle(String title) {
        String normalizedTitle = title.trim();

        if (normalizedTitle.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Exam title must not be blank"
            );
        }

        return normalizedTitle;
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }

        String normalizedDescription = description.trim();
        return normalizedDescription.isBlank() ? null : normalizedDescription;
    }

    private Exam getExamEntityById(UUID id) {
        return examRepository.findDetailedById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Exam not found with id: " + id
                ));
    }

    private User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + id
                ));
    }

    public ExamResponse create(CreateExamRequest request) {
        String normalizedTitle = normalizeTitle(request.title());
        String normalizedDescription = normalizeDescription(request.description());

        if (contentItemRepository.existsByTypeAndTitleIgnoreCase(ContentItemType.EXAM, normalizedTitle)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Exam with this title already exists"
            );
        }

        User createdBy = getUserById(request.createdById());

        ContentItem contentItem = new ContentItem();
        contentItem.setTitle(normalizedTitle);
        contentItem.setDescription(normalizedDescription);
        contentItem.setType(ContentItemType.EXAM);
        contentItem.setTotalSteps(0);
        contentItem.setCreatedBy(createdBy);

        try {
            ContentItem savedContentItem = contentItemRepository.saveAndFlush(contentItem);

            Exam exam = new Exam();
            exam.setContentItem(savedContentItem);

            Exam savedExam = examRepository.saveAndFlush(exam);

            return examMapper.toResponse(savedExam);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Exam with this title already exists",
                    exception
            );
        }
    }

    @Transactional(readOnly = true)
    public ExamResponse getById(UUID id) {
        Exam exam = getExamEntityById(id);
        return examMapper.toResponse(exam);
    }

    @Transactional(readOnly = true)
    public List<ExamResponse> getAll() {
        List<Exam> exams = examRepository.findAllDetailed();
        return examMapper.toResponseList(exams);
    }

    public ExamResponse update(UUID id, UpdateExamRequest request) {
        Exam exam = getExamEntityById(id);
        ContentItem contentItem = exam.getContentItem();

        if (request.title() != null) {
            String normalizedTitle = normalizeTitle(request.title());

            if (contentItemRepository.existsByTypeAndTitleIgnoreCaseAndIdNot(
                    ContentItemType.EXAM,
                    normalizedTitle,
                    contentItem.getId()
            )) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Exam with this title already exists"
                );
            }

            contentItem.setTitle(normalizedTitle);
        }

        if (request.description() != null) {
            contentItem.setDescription(normalizeDescription(request.description()));
        }

        try {
            Exam updatedExam = examRepository.saveAndFlush(exam);
            return examMapper.toResponse(updatedExam);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Exam with this title already exists",
                    exception
            );
        }
    }

    public void delete(UUID id) {
        Exam exam = getExamEntityById(id);
        ContentItem contentItem = exam.getContentItem();

        examRepository.delete(exam);
        contentItemRepository.delete(contentItem);
    }
}