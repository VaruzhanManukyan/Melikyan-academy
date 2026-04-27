package com.melikyan.academy.service;

import com.melikyan.academy.entity.*;
import org.springframework.http.HttpStatus;
import com.melikyan.academy.mapper.ExamTaskMapper;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.repository.ExamTaskRepository;
import com.melikyan.academy.repository.ContentItemRepository;
import com.melikyan.academy.repository.ExamSectionRepository;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import com.melikyan.academy.dto.response.examTask.ExamTaskResponse;
import com.melikyan.academy.dto.request.examTask.CreateExamTaskRequest;
import com.melikyan.academy.dto.request.examTask.UpdateExamTaskRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional
@RequiredArgsConstructor
public class ExamTaskService {
    private final UserRepository userRepository;
    private final ExamTaskMapper examTaskMapper;
    private final ExamTaskRepository examTaskRepository;
    private final ContentItemRepository contentItemRepository;
    private final ExamSectionRepository examSectionRepository;

    private User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + id
                ));
    }

    private ExamSection getExamSectionById(UUID id) {
        return examSectionRepository.findDetailedById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Exam section not found with id: " + id
                ));
    }

    private ExamTask getExamTaskEntityById(UUID id) {
        return examTaskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Exam task not found with id: " + id
                ));
    }

    private void validateOrderIndexUnique(UUID examSectionId, Integer orderIndex) {
        if (examTaskRepository.existsBySectionIdAndOrderIndex(examSectionId, orderIndex)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Exam task with order index " + orderIndex +
                            " already exists in exam section " + examSectionId
            );
        }
    }

    private void validateOrderIndexUnique(UUID examSectionId, Integer orderIndex, UUID examTaskId) {
        if (examTaskRepository.existsBySectionIdAndOrderIndexAndIdNot(
                examSectionId,
                orderIndex,
                examTaskId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Exam task with order index " + orderIndex +
                            " already exists in exam section " + examSectionId
            );
        }
    }

    private UUID getContentItemId(ExamSection examSection) {
        return examSection.getExam()
                .getContentItem()
                .getId();
    }

    private void changeContentItemTotalSteps(UUID contentItemId, int delta) {
        int updatedRows = contentItemRepository.changeTotalSteps(contentItemId, delta);

        if (updatedRows == 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    delta < 0
                            ? "Content item total steps cannot go below zero"
                            : "Failed to update content item total steps"
            );
        }
    }

    public ExamTaskResponse create(CreateExamTaskRequest request) {
        ExamSection examSection = getExamSectionById(request.sectionId());
        User createdBy = getUserById(request.createdById());

        validateOrderIndexUnique(examSection.getId(), request.orderIndex());

        ExamTask examTask = new ExamTask();
        examTask.setOrderIndex(request.orderIndex());
        examTask.setPoint(request.point());
        examTask.setType(request.type());
        examTask.setContentPayload(request.contentPayload());
        examTask.setCreatedBy(createdBy);
        examTask.setSection(examSection);

        try {
            ExamTask savedExamTask = examTaskRepository.saveAndFlush(examTask);
            changeContentItemTotalSteps(getContentItemId(examSection), 1);
            return examTaskMapper.toResponse(savedExamTask);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Exam task with such order index already exists in this exam section",
                    exception
            );
        }
    }

    @Transactional(readOnly = true)
    public ExamTaskResponse getById(UUID id) {
        ExamTask examTask = getExamTaskEntityById(id);
        return examTaskMapper.toResponse(examTask);
    }

    @Transactional(readOnly = true)
    public List<ExamTaskResponse> getAll() {
        List<ExamTask> examTasks = examTaskRepository.findAll();
        return examTaskMapper.toResponseList(examTasks);
    }

    @Transactional(readOnly = true)
    public List<ExamTaskResponse> getAllByExamSectionId(UUID examSectionId) {
        getExamSectionById(examSectionId);
        List<ExamTask> examTasks = examTaskRepository
                .findAllBySectionIdOrderByOrderIndexAsc(examSectionId);
        return examTaskMapper.toResponseList(examTasks);
    }

    public ExamTaskResponse update(UUID id, UpdateExamTaskRequest request) {
        ExamTask examTask = getExamTaskEntityById(id);

        ExamSection currentExamSection = getExamSectionById(examTask.getSection().getId());
        ExamSection targetExamSection = request.sectionId() != null
                ? getExamSectionById(request.sectionId())
                : currentExamSection;

        Integer targetOrderIndex = request.orderIndex() != null
                ? request.orderIndex()
                : examTask.getOrderIndex();

        if (!targetExamSection.getId().equals(currentExamSection.getId())
                || !targetOrderIndex.equals(examTask.getOrderIndex())) {
            validateOrderIndexUnique(targetExamSection.getId(), targetOrderIndex, examTask.getId());
        }

        if (!targetExamSection.getId().equals(currentExamSection.getId())) {
            UUID currentContentItemId = getContentItemId(currentExamSection);
            UUID targetContentItemId = getContentItemId(targetExamSection);

            if (!currentContentItemId.equals(targetContentItemId)) {
                changeContentItemTotalSteps(currentContentItemId, -1);
                changeContentItemTotalSteps(targetContentItemId, 1);
            }

            examTask.setSection(targetExamSection);
        }

        if (request.orderIndex() != null) {
            examTask.setOrderIndex(request.orderIndex());
        }

        if (request.point() != null) {
            examTask.setPoint(request.point());
        }

        if (request.type() != null) {
            examTask.setType(request.type());
        }

        if (request.contentPayload() != null) {
            examTask.setContentPayload(request.contentPayload());
        }

        try {
            ExamTask updatedExamTask = examTaskRepository.saveAndFlush(examTask);
            return examTaskMapper.toResponse(updatedExamTask);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Exam task with such order index already exists in this exam section",
                    exception
            );
        }
    }

    public void delete(UUID id) {
        ExamTask examTask = getExamTaskEntityById(id);
        ExamSection examSection = getExamSectionById(examTask.getSection().getId());

        changeContentItemTotalSteps(getContentItemId(examSection), -1);

        examTaskRepository.delete(examTask);
    }
}
