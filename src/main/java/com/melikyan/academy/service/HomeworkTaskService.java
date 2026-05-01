package com.melikyan.academy.service;

import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.Lesson;
import org.springframework.http.HttpStatus;
import com.melikyan.academy.entity.HomeworkTask;
import com.melikyan.academy.mapper.HomeworkTaskMapper;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.repository.LessonRepository;
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
    private final LessonRepository lessonRepository;
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

    private Lesson getLessonById(UUID id) {
        return lessonRepository.findDetailedById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Lesson not found with id: " + id
                ));
    }

    private HomeworkTask getHomeworkTaskEntityById(UUID id) {
        return homeworkTaskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Homework task not found with id: " + id
                ));
    }

    private void validateOrderIndexUnique(UUID lessonId, Integer orderIndex) {
        if (homeworkTaskRepository.existsByLessonIdAndOrderIndex(lessonId, orderIndex)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Homework task with order index " + orderIndex +
                            " already exists in lesson " + lessonId
            );
        }
    }

    private void validateOrderIndexUnique(UUID lessonId, Integer orderIndex, UUID homeworkTaskId) {
        if (homeworkTaskRepository.existsByLessonIdAndOrderIndexAndIdNot(
                lessonId,
                orderIndex,
                homeworkTaskId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Homework task with order index " + orderIndex +
                            " already exists in lesson " + lessonId
            );
        }
    }

    private UUID getContentItemId(Lesson lesson) {
        return lesson.getCourse()
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
        Lesson lesson = getLessonById(request.lessonId());
        User createdBy = getUserById(request.createdById());

        validateOrderIndexUnique(lesson.getId(), request.orderIndex());

        HomeworkTask homeworkTask = new HomeworkTask();
        homeworkTask.setOrderIndex(request.orderIndex());
        homeworkTask.setPoint(request.point());
        homeworkTask.setType(request.type());
        homeworkTask.setContentPayload(request.contentPayload());
        homeworkTask.setCreatedBy(createdBy);
        homeworkTask.setLesson(lesson);

        try {
            HomeworkTask savedHomeworkTask = homeworkTaskRepository.saveAndFlush(homeworkTask);
            changeContentItemTotalSteps(getContentItemId(lesson), 1);
            return homeworkTaskMapper.toResponse(savedHomeworkTask);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Homework task with such order index already exists in this lesson",
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
    public List<HomeworkTaskResponse> getAllByLessonId(UUID lessonId) {
        getLessonById(lessonId);
        List<HomeworkTask> homeworkTasks = homeworkTaskRepository
                .findAllByLessonIdOrderByOrderIndexAsc(lessonId);
        return homeworkTaskMapper.toResponseList(homeworkTasks);
    }

    public HomeworkTaskResponse update(UUID id, UpdateHomeworkTaskRequest request) {
        HomeworkTask homeworkTask = getHomeworkTaskEntityById(id);

        Lesson lesson = homeworkTask.getLesson();

        Integer targetOrderIndex = request.orderIndex() != null
                ? request.orderIndex()
                : homeworkTask.getOrderIndex();

        if (!targetOrderIndex.equals(homeworkTask.getOrderIndex())) {
            validateOrderIndexUnique(lesson.getId(), targetOrderIndex, homeworkTask.getId());
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

        if (request.contentPayload() != null) {
            homeworkTask.setContentPayload(request.contentPayload());
        }

        try {
            HomeworkTask updatedHomeworkTask = homeworkTaskRepository.saveAndFlush(homeworkTask);
            return homeworkTaskMapper.toResponse(updatedHomeworkTask);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Homework task with such order index already exists in this Lesson",
                    exception
            );
        }
    }

    public void delete(UUID id) {
        HomeworkTask homeworkTask = getHomeworkTaskEntityById(id);
        Lesson lesson = getLessonById(homeworkTask.getLesson().getId());

        changeContentItemTotalSteps(getContentItemId(lesson), -1);

        homeworkTaskRepository.delete(homeworkTask);
    }
}