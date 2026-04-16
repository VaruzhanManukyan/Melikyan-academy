package com.melikyan.academy.service;

import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.Lesson;
import org.springframework.http.HttpStatus;
import com.melikyan.academy.entity.Homework;
import com.melikyan.academy.mapper.HomeworkMapper;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.repository.LessonRepository;
import com.melikyan.academy.repository.HomeworkRepository;
import org.springframework.web.server.ResponseStatusException;
import com.melikyan.academy.dto.response.homework.HomeworkResponse;
import com.melikyan.academy.dto.request.homework.UpdateHomeworkRequest;
import com.melikyan.academy.dto.request.homework.CreateHomeworkRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class HomeworkService {
    private final UserRepository userRepository;
    private final HomeworkMapper homeworkMapper;
    private final LessonRepository lessonRepository;
    private final HomeworkRepository homeworkRepository;

    private String normalizeTitle(String title) {
        String normalizedTitle = title.trim();

        if (normalizedTitle.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Course title must not be blank"
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

    private Lesson getLessonById(UUID id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Lesson not found with id: " + id
                ));
    }

    private Homework getHomeworkEntityById(UUID id) {
        return homeworkRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Homework not found with id: " + id
                ));
    }

    private void validateOrderIndexUnique(UUID lessonId, Integer orderIndex) {
        if (homeworkRepository.existsByLessonIdAndOrderIndex(lessonId, orderIndex)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Homework with order index " + orderIndex +
                            " already exists in lesson " + lessonId
            );
        }
    }

    private void validateOrderIndexUnique(UUID lessonId, Integer orderIndex, UUID homeworkId) {
        if (homeworkRepository.existsByLessonIdAndOrderIndexAndIdNot(
                lessonId,
                orderIndex,
                homeworkId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Homework with order index " + orderIndex +
                            " already exists in lesson " + lessonId
            );
        }
    }

    private void validateTitleUnique(UUID lessonId, String title) {
        if (homeworkRepository.existsByLessonIdAndTitleIgnoreCase(lessonId, title)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Homework with title '" + title +
                            "' already exists in lesson " + lessonId
            );
        }
    }

    private void validateTitleUnique(UUID lessonId, String title, UUID homeworkId) {
        if (homeworkRepository.existsByLessonIdAndTitleIgnoreCaseAndIdNot(lessonId, title, homeworkId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Homework with title '" + title +
                            "' already exists in lesson " + lessonId
            );
        }
    }

    public HomeworkResponse create(CreateHomeworkRequest request) {
        String normalizedTitle = normalizeTitle(request.title());
        String normalizedDescription = normalizeDescription(request.description());

        validateTitleUnique(request.lessonId(), normalizedTitle);
        validateOrderIndexUnique(request.lessonId(), request.orderIndex());

        User createdBy = getUserById(request.createdById());
        Lesson lesson = getLessonById(request.lessonId());

        Homework homework = new Homework();
        homework.setTitle(normalizedTitle);
        homework.setDescription(normalizedDescription);
        homework.setOrderIndex(request.orderIndex());
        homework.setDueDate(request.dueDate());
        homework.setIsPublished(request.isPublished());
        homework.setCreatedBy(createdBy);
        homework.setLesson(lesson);

        Homework savedHomework = homeworkRepository.saveAndFlush(homework);
        return homeworkMapper.toResponse(savedHomework);
    }

    @Transactional(readOnly = true)
    public HomeworkResponse getById(UUID id) {
        Homework homework = getHomeworkEntityById(id);
        return homeworkMapper.toResponse(homework);
    }

    @Transactional(readOnly = true)
    public List<HomeworkResponse> getAll() {
        List<Homework> homeworks = homeworkRepository.findAll();
        return homeworkMapper.toResponseList(homeworks);
    }

    @Transactional(readOnly = true)
    public List<HomeworkResponse> getAllByLessonId(UUID lessonId) {
        getLessonById(lessonId);
        List<Homework> homeworks = homeworkRepository
                .findByLessonIdOrderByOrderIndexAsc(lessonId);
        return homeworkMapper.toResponseList(homeworks);
    }

    public HomeworkResponse update(UUID id, UpdateHomeworkRequest request) {
        Homework homework = getHomeworkEntityById(id);

        UUID targetLessonId = request.lessonId() != null
                ? request.lessonId()
                : homework.getLesson().getId();

        Integer targetOrderIndex = request.orderIndex() != null
                ? request.orderIndex()
                : homework.getOrderIndex();

        if (!targetLessonId.equals(homework.getLesson().getId())
                || !targetOrderIndex.equals(homework.getOrderIndex())) {
            validateOrderIndexUnique(targetLessonId, targetOrderIndex, homework.getId());
        }

        if (request.lessonId() != null) {
            Lesson lesson = getLessonById(request.lessonId());
            homework.setLesson(lesson);
        }

        if (request.title() != null) {
            String normalizedTitle = normalizeTitle(request.title());
            validateTitleUnique(targetLessonId, normalizedTitle, homework.getId());
            homework.setTitle(normalizedTitle);
        }

        if (request.description() != null) {
            homework.setDescription(normalizeDescription(request.description()));
        }

        if (request.orderIndex() != null) {
            homework.setOrderIndex(request.orderIndex());
        }

        if (request.dueDate() != null) {
            homework.setDueDate(request.dueDate());
        }

        if (request.isPublished() != null) {
            homework.setIsPublished(request.isPublished());
        }

        Homework savedHomework = homeworkRepository.save(homework);
        return homeworkMapper.toResponse(savedHomework);
    }

    public void delete(UUID id) {
        Homework homework = getHomeworkEntityById(id);
        homeworkRepository.delete(homework);
    }
}
