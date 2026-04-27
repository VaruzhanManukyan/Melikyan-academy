package com.melikyan.academy.service;

import com.melikyan.academy.entity.User;
import org.springframework.http.HttpStatus;
import com.melikyan.academy.entity.Homework;
import com.melikyan.academy.entity.HomeworkTask;
import com.melikyan.academy.mapper.HomeworkTaskMapper;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.repository.HomeworkRepository;
import com.melikyan.academy.repository.ContentItemRepository;
import com.melikyan.academy.repository.HomeworkTaskRepository;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import com.melikyan.academy.dto.response.homeworkTask.HomeworkTaskResponse;
import com.melikyan.academy.dto.request.homeworkTask.UpdateHomeworkTaskRequest;
import com.melikyan.academy.dto.request.homeworkTask.CreateHomeworkTaskRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class HomeworkTaskService {
    private final UserRepository userRepository;
    private final HomeworkRepository homeworkRepository;
    private final HomeworkTaskMapper homeworkTaskMapper;
    private final ContentItemRepository contentItemRepository;
    private final HomeworkTaskRepository homeworkTaskRepository;

    private User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + id
                ));
    }

    private Homework getHomeworkById(UUID id) {
        return homeworkRepository.findDetailedById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Homework not found with id: " + id
                ));
    }

    private HomeworkTask getHomeworkTaskEntityById(UUID id) {
        return homeworkTaskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Homework task not found with id: " + id
                ));
    }

    private void validateOrderIndexUnique(UUID homeworkId, Integer orderIndex) {
        if (homeworkTaskRepository.existsByHomeworkIdAndOrderIndex(homeworkId, orderIndex)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Homework task with order index " + orderIndex +
                            " already exists in homework " + homeworkId
            );
        }
    }

    private void validateOrderIndexUnique(UUID homeworkId, Integer orderIndex, UUID homeworkTaskId) {
        if (homeworkTaskRepository.existsByHomeworkIdAndOrderIndexAndIdNot(
                homeworkId,
                orderIndex,
                homeworkTaskId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Homework task with order index " + orderIndex +
                            " already exists in homework " + homeworkId
            );
        }
    }

    private UUID getContentItemId(Homework homework) {
        return homework.getLesson()
                .getCourse()
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

    public HomeworkTaskResponse create(CreateHomeworkTaskRequest request) {
        Homework homework = getHomeworkById(request.homeworkId());
        User createdBy = getUserById(request.createdById());

        validateOrderIndexUnique(homework.getId(), request.orderIndex());

        HomeworkTask homeworkTask = new HomeworkTask();
        homeworkTask.setOrderIndex(request.orderIndex());
        homeworkTask.setPoint(request.point());
        homeworkTask.setType(request.type());
        homeworkTask.setPayloadContent(request.payloadContent());
        homeworkTask.setCreatedBy(createdBy);
        homeworkTask.setHomework(homework);

        try {
            HomeworkTask savedHomeworkTask = homeworkTaskRepository.saveAndFlush(homeworkTask);
            changeContentItemTotalSteps(getContentItemId(homework), 1);
            return homeworkTaskMapper.toResponse(savedHomeworkTask);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Homework task with such order index already exists in this homework",
                    exception
            );
        }
    }

    @Transactional(readOnly = true)
    public HomeworkTaskResponse getById(UUID id) {
        HomeworkTask homeworkTask = getHomeworkTaskEntityById(id);
        return homeworkTaskMapper.toResponse(homeworkTask);
    }

    @Transactional(readOnly = true)
    public List<HomeworkTaskResponse> getAll() {
        List<HomeworkTask> homeworkTasks = homeworkTaskRepository.findAll();
        return homeworkTaskMapper.toResponseList(homeworkTasks);
    }

    @Transactional(readOnly = true)
    public List<HomeworkTaskResponse> getAllByHomeworkId(UUID homeworkId) {
        getHomeworkById(homeworkId);
        List<HomeworkTask> homeworkTasks = homeworkTaskRepository
                .findAllByHomeworkIdOrderByOrderIndexAsc(homeworkId);
        return homeworkTaskMapper.toResponseList(homeworkTasks);
    }

    public HomeworkTaskResponse update(UUID id, UpdateHomeworkTaskRequest request) {
        HomeworkTask homeworkTask = getHomeworkTaskEntityById(id);

        Homework currentHomework = getHomeworkById(homeworkTask.getHomework().getId());
        Homework targetHomework = request.homeworkId() != null
                ? getHomeworkById(request.homeworkId())
                : currentHomework;

        Integer targetOrderIndex = request.orderIndex() != null
                ? request.orderIndex()
                : homeworkTask.getOrderIndex();

        if (!targetHomework.getId().equals(currentHomework.getId())
                || !targetOrderIndex.equals(homeworkTask.getOrderIndex())) {
            validateOrderIndexUnique(targetHomework.getId(), targetOrderIndex, homeworkTask.getId());
        }

        if (!targetHomework.getId().equals(currentHomework.getId())) {
            UUID currentContentItemId = getContentItemId(currentHomework);
            UUID targetContentItemId = getContentItemId(targetHomework);

            if (!currentContentItemId.equals(targetContentItemId)) {
                changeContentItemTotalSteps(currentContentItemId, -1);
                changeContentItemTotalSteps(targetContentItemId, 1);
            }

            homeworkTask.setHomework(targetHomework);
        }

        if (request.orderIndex() != null) {
            homeworkTask.setOrderIndex(request.orderIndex());
        }

        if (request.point() != null) {
            homeworkTask.setPoint(request.point());
        }

        if (request.type() != null) {
            homeworkTask.setType(request.type());
        }

        if (request.payloadContent() != null) {
            homeworkTask.setPayloadContent(request.payloadContent());
        }

        try {
            HomeworkTask updatedHomeworkTask = homeworkTaskRepository.saveAndFlush(homeworkTask);
            return homeworkTaskMapper.toResponse(updatedHomeworkTask);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Homework task with such order index already exists in this homework",
                    exception
            );
        }
    }

    public void delete(UUID id) {
        HomeworkTask homeworkTask = getHomeworkTaskEntityById(id);
        Homework homework = getHomeworkById(homeworkTask.getHomework().getId());

        changeContentItemTotalSteps(getContentItemId(homework), -1);

        homeworkTaskRepository.delete(homeworkTask);
    }
}