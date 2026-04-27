package com.melikyan.academy.service;

import com.melikyan.academy.entity.*;
import org.springframework.http.HttpStatus;
import com.melikyan.academy.mapper.ExamSectionMapper;
import com.melikyan.academy.repository.ExamRepository;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.repository.ExamSectionRepository;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import com.melikyan.academy.dto.response.examSection.ExamSectionResponse;
import com.melikyan.academy.dto.request.examSection.CreateExamSectionRequest;
import com.melikyan.academy.dto.request.examSection.UpdateExamSectionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ExamSectionService {
    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final ExamSectionMapper examSectionMapper;
    private final ExamSectionRepository examSectionRepository;

    private String normalizeTitle(String title) {
        String normalizedTitle = title.trim();
        if (normalizedTitle.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Exam section title must not be blank"
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

    private User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + id
                ));
    }

    private Exam getExamById(UUID id) {
        return examRepository.findDetailedById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Exam not found with id: " + id
                ));
    }

    private ExamSection getExamSectionEntityById(UUID id) {
        return examSectionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Exam section not found with id: " + id
                ));
    }

    private void validateOrderIndexUnique(UUID examId, Integer orderIndex) {
        if (examSectionRepository.existsByExamIdAndOrderIndex(examId, orderIndex)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Exam section with order index " + orderIndex +
                            " already exists in exam " + examId
            );
        }
    }

    private void validateOrderIndexUnique(UUID examId, Integer orderIndex, UUID examSectionId) {
        if (examSectionRepository.existsByExamIdAndOrderIndexAndIdNot(
                examId,
                orderIndex,
                examSectionId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Exam section with order index " + orderIndex +
                            " already exists in exam " + examId
            );
        }
    }

    private void validateTitleUnique(UUID examId, String title) {
        if (examSectionRepository.existsByExamIdAndTitleIgnoreCase(examId, title)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Exam section with title '" + title +
                            "' already exists in exam " + examId
            );
        }
    }

    private void validateTitleUnique(UUID examId, String title, UUID examSectionId) {
        if (examSectionRepository.existsByExamIdAndTitleIgnoreCaseAndIdNot(examId, title, examSectionId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Exam section with title '" + title +
                            "' already exists in exam " + examId
            );
        }
    }

    public ExamSectionResponse create(CreateExamSectionRequest request) {
        String normalizedTitle = normalizeTitle(request.title());
        String normalizedDescription = normalizeDescription(request.description());

        validateTitleUnique(request.examId(), normalizedTitle);
        validateOrderIndexUnique(request.examId(), request.orderIndex());

        User createdBy = getUserById(request.createdById());
        Exam exam = getExamById(request.examId());

        ExamSection examSection = new ExamSection();
        examSection.setOrderIndex(request.orderIndex());
        examSection.setTitle(normalizedTitle);
        examSection.setDescription(normalizedDescription);
        examSection.setDuration(request.duration());
        examSection.setExam(exam);
        examSection.setCreatedBy(createdBy);

        try {
            ExamSection savedExamSection = examSectionRepository.saveAndFlush(examSection);
            return examSectionMapper.toResponse(savedExamSection);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Exam section with such title or order index already exists in this exam",
                    exception
            );
        }
    }

    @Transactional(readOnly = true)
    public ExamSectionResponse getById(UUID id) {
        ExamSection examSection = getExamSectionEntityById(id);
        return examSectionMapper.toResponse(examSection);
    }

    @Transactional(readOnly = true)
    public List<ExamSectionResponse> getAll() {
        List<ExamSection> examSection = examSectionRepository.findAll();
        return examSectionMapper.toResponseList(examSection);
    }


    @Transactional(readOnly = true)
    public List<ExamSectionResponse> getByExamId(UUID examId) {
        getExamById(examId);
        List<ExamSection> examSection = examSectionRepository.findByExamIdOrderByOrderIndexAsc(examId);
        return examSectionMapper.toResponseList(examSection);
    }

    public ExamSectionResponse update(UUID id, UpdateExamSectionRequest request) {
        ExamSection examSection = getExamSectionEntityById(id);

        Integer targetOrderIndex = request.orderIndex() != null
                ? request.orderIndex()
                : examSection.getOrderIndex();

        if (!targetOrderIndex.equals(examSection.getOrderIndex())) {
            validateOrderIndexUnique(examSection.getExam().getId(), targetOrderIndex, examSection.getId());
        }

        if (request.orderIndex() != null) {
            examSection.setOrderIndex(request.orderIndex());
        }

        if (request.title() != null) {
            String normalizedTitle = normalizeTitle(request.title());
            validateTitleUnique(examSection.getExam().getId(), normalizedTitle, examSection.getId());
            examSection.setTitle(normalizedTitle);
        }

        if (request.description() != null) {
            examSection.setDescription(normalizeDescription(request.description()));
        }

        if (request.duration() != null) {
            examSection.setDuration(request.duration());
        }

        try {
            ExamSection savedExamSection = examSectionRepository.saveAndFlush(examSection);
            return examSectionMapper.toResponse(savedExamSection);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Exam section with such title or order index already exists in this exam",
                    exception
            );
        }
    }

    public void delete(UUID id) {
        ExamSection examSection = getExamSectionEntityById(id);
        examSectionRepository.delete(examSection);
    }
}
