package com.melikyan.academy.service;

import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.Course;
import com.melikyan.academy.entity.Lesson;
import org.springframework.http.HttpStatus;
import com.melikyan.academy.mapper.LessonMapper;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.repository.CourseRepository;
import com.melikyan.academy.repository.LessonRepository;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import com.melikyan.academy.dto.response.lesson.LessonResponse;
import com.melikyan.academy.dto.request.lesson.UpdateLessonRequest;
import com.melikyan.academy.dto.request.lesson.CreateLessonRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class LessonService {
    private final LessonMapper lessonMapper;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;

    private String normalizeTitle(String title) {
        String normalizedTitle = title.trim();

        if (normalizedTitle.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Lesson title must not be blank"
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

    private String normalizeValueUrl(String valueUrl) {
        String normalizedValueUrl = valueUrl.trim();

        if (normalizedValueUrl.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Lesson valueUrl must not be blank"
            );
        }

        return normalizedValueUrl;
    }

    private User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + id
                ));
    }

    private Course getCourseById(UUID id) {
        return courseRepository.findDetailedById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Course not found with id: " + id
                ));
    }

    private Lesson getLessonEntityById(UUID id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Lesson not found with id: " + id
                ));
    }

    private void validateOrderIndexUnique(UUID courseId, Integer orderIndex) {
        if (lessonRepository.existsByCourseIdAndOrderIndex(courseId, orderIndex)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Lesson with order index " + orderIndex +
                            " already exists in course " + courseId
            );
        }
    }

    private void validateOrderIndexUnique(UUID courseId, Integer orderIndex, UUID lessonId) {
        if (lessonRepository.existsByCourseIdAndOrderIndexAndIdNot(
                courseId,
                orderIndex,
                lessonId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Lesson with order index " + orderIndex +
                            " already exists in course " + courseId
            );
        }
    }

    private void validateTitleUnique(UUID courseId, String title) {
        if (lessonRepository.existsByCourseIdAndTitleIgnoreCase(courseId, title)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Lesson with title '" + title +
                            "' already exists in course " + courseId
            );
        }
    }

    private void validateTitleUnique(UUID courseId, String title, UUID lessonId) {
        if (lessonRepository.existsByCourseIdAndTitleIgnoreCaseAndIdNot(courseId, title, lessonId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Lesson with title '" + title +
                            "' already exists in course " + courseId
            );
        }
    }

    public LessonResponse create(CreateLessonRequest request) {
        String normalizedTitle = normalizeTitle(request.title());
        String normalizedDescription = normalizeDescription(request.description());
        String normalizedValueUrl = normalizeValueUrl(request.valueUrl());

        validateTitleUnique(request.courseId(), normalizedTitle);
        validateOrderIndexUnique(request.courseId(), request.orderIndex());

        User createdBy = getUserById(request.createdById());
        Course course = getCourseById(request.courseId());

        Lesson lesson = new Lesson();
        lesson.setOrderIndex(request.orderIndex());
        lesson.setTitle(normalizedTitle);
        lesson.setDescription(normalizedDescription);
        lesson.setLessonType(request.lessonType());
        lesson.setValueUrl(normalizedValueUrl);
        lesson.setState(request.state());
        lesson.setStartsAt(request.startsAt());
        lesson.setDuration(request.duration());
        lesson.setCourse(course);
        lesson.setCreatedBy(createdBy);

        try {
            Lesson savedLesson = lessonRepository.saveAndFlush(lesson);
            return lessonMapper.toResponse(savedLesson);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Lesson with such title or order index already exists in this course",
                    exception
            );
        }
    }

    @Transactional(readOnly = true)
    public LessonResponse getById(UUID id) {
        Lesson lesson = getLessonEntityById(id);
        return lessonMapper.toResponse(lesson);
    }

    @Transactional(readOnly = true)
    public List<LessonResponse> getAll() {
        List<Lesson> lessons = lessonRepository.findAll();
        return lessonMapper.toResponseList(lessons);
    }

    @Transactional(readOnly = true)
    public List<LessonResponse> getByCourseId(UUID courseId) {
        getCourseById(courseId);
        List<Lesson> lessons = lessonRepository
                .findByCourseIdOrderByOrderIndexAsc(courseId);
        return lessonMapper.toResponseList(lessons);
    }

    public LessonResponse update(UUID id, UpdateLessonRequest request) {
        Lesson lesson = getLessonEntityById(id);

        Integer targetOrderIndex = request.orderIndex() != null
                ? request.orderIndex()
                : lesson.getOrderIndex();

        if (!targetOrderIndex.equals(lesson.getOrderIndex())) {
            validateOrderIndexUnique(lesson.getCourse().getId(), targetOrderIndex, lesson.getId());
        }

        if (request.orderIndex() != null) {
            lesson.setOrderIndex(request.orderIndex());
        }

        if (request.title() != null) {
            String normalizedTitle = normalizeTitle(request.title());
            validateTitleUnique(lesson.getCourse().getId(), normalizedTitle, lesson.getId());
            lesson.setTitle(normalizedTitle);
        }

        if (request.description() != null) {
            lesson.setDescription(normalizeDescription(request.description()));
        }

        if (request.lessonType() != null) {
            lesson.setLessonType(request.lessonType());
        }

        if (request.valueUrl() != null) {
            lesson.setValueUrl(normalizeValueUrl(request.valueUrl()));
        }

        if (request.state() != null) {
            lesson.setState(request.state());
        }

        if (request.startsAt() != null) {
            lesson.setStartsAt(request.startsAt());
        }

        if (request.duration() != null) {
            lesson.setDuration(request.duration());
        }

        try {
            Lesson savedLesson = lessonRepository.saveAndFlush(lesson);
            return lessonMapper.toResponse(savedLesson);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Lesson with such title or order index already exists in this course",
                    exception
            );
        }
    }

    public void delete(UUID id) {
        Lesson lesson = getLessonEntityById(id);
        lessonRepository.delete(lesson);
    }
}
