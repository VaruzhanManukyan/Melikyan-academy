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

    private Course getCourseEntityById(UUID id) {
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

    public LessonResponse create(CreateLessonRequest request) {
        String normalizedTitle = normalizeTitle(request.title());
        String normalizedDescription = normalizeDescription(request.description());
        String normalizedValueUrl = normalizeValueUrl(request.valueUrl());

        if (lessonRepository.existsByCourseIdAndOrderIndex(request.courseId(), request.orderIndex())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Lesson with order index " + request.orderIndex() +
                            " already exists in course " + request.courseId()
            );
        }

        User createdBy = getUserById(request.createdById());
        Course course = getCourseEntityById(request.courseId());

        Lesson lesson = new Lesson();
        lesson.setOrderIndex(request.orderIndex());
        lesson.setTitle(normalizedTitle);
        lesson.setDescription(normalizedDescription);
        lesson.setSessionType(request.sessionType());
        lesson.setValueUrl(normalizedValueUrl);
        lesson.setState(request.state());
        lesson.setStartsAt(request.startsAt());
        lesson.setDuration(request.duration());
        lesson.setCourse(course);
        lesson.setCreatedBy(createdBy);

        Lesson savedLesson = lessonRepository.saveAndFlush(lesson);
        return lessonMapper.toResponse(savedLesson);
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

    public LessonResponse update(UUID id, UpdateLessonRequest request) {
        Lesson lesson = getLessonEntityById(id);

        UUID targetCourseId = request.courseId() != null
                ? request.courseId()
                : lesson.getCourse().getId();

        int targetOrderIndex = request.orderIndex() != null
                ? request.orderIndex()
                : lesson.getOrderIndex();

        boolean orderIndexConflict =
                lessonRepository.existsByCourseIdAndOrderIndexAndIdNot(
                        targetCourseId,
                        targetOrderIndex,
                        id
                );

        if (orderIndexConflict) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Lesson with order index " + targetOrderIndex
                            + " already exists in course " + targetCourseId
            );
        }

        if (request.orderIndex() != null) {
            lesson.setOrderIndex(request.orderIndex());
        }

        if (request.title() != null) {
            lesson.setTitle(normalizeTitle(request.title()));
        }

        if (request.description() != null) {
            lesson.setDescription(normalizeDescription(request.description()));
        }

        if (request.sessionType() != null) {
            lesson.setSessionType(request.sessionType());
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

        if (request.courseId() != null) {
            Course course = getCourseEntityById(request.courseId());
            lesson.setCourse(course);
        }

        Lesson updatedLesson = lessonRepository.save(lesson);
        return lessonMapper.toResponse(updatedLesson);
    }

    public void delete(UUID id) {
        Lesson lesson = getLessonEntityById(id);
        lessonRepository.delete(lesson);
    }
}
