package com.melikyan.academy.service;

import org.mockito.ArgumentCaptor;
import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.Course;
import com.melikyan.academy.entity.Lesson;
import com.melikyan.academy.mapper.LessonMapper;
import org.mockito.junit.jupiter.MockitoExtension;
import com.melikyan.academy.entity.enums.LessonType;
import com.melikyan.academy.entity.enums.LessonState;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.repository.CourseRepository;
import com.melikyan.academy.repository.LessonRepository;
import org.springframework.web.server.ResponseStatusException;
import com.melikyan.academy.dto.response.lesson.LessonResponse;
import com.melikyan.academy.dto.request.lesson.CreateLessonRequest;
import com.melikyan.academy.dto.request.lesson.UpdateLessonRequest;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.UUID;
import java.time.Duration;
import java.util.Optional;
import java.time.OffsetDateTime;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {
    @Mock
    private LessonMapper lessonMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private LessonService lessonService;

    private UUID lessonId;
    private UUID courseId;
    private UUID userId;

    private User user;
    private Course course;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        lessonId = UUID.randomUUID();
        courseId = UUID.randomUUID();
        userId = UUID.randomUUID();

        user = new User();
        user.setId(userId);

        course = new Course();
        course.setId(courseId);

        lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setOrderIndex(1);
        lesson.setTitle("Lesson title");
        lesson.setDescription("Lesson description");
        lesson.setLessonType(LessonType.MEET_LINK);
        lesson.setValueUrl("https://meet.google.com/test");
        lesson.setState(LessonState.SCHEDULED);
        lesson.setStartsAt(OffsetDateTime.parse("2026-04-20T14:00:00+04:00"));
        lesson.setDuration(Duration.ofMinutes(90));
        lesson.setCourse(course);
        lesson.setCreatedBy(user);
    }

    @Test
    @DisplayName("create -> saves lesson and returns response")
    void create_ShouldSaveLessonAndReturnResponse() {
        CreateLessonRequest request = new CreateLessonRequest(
                1,
                "  Introduction to Spring  ",
                "  First lesson  ",
                LessonType.MEET_LINK,
                "  https://meet.google.com/test  ",
                LessonState.SCHEDULED,
                OffsetDateTime.parse("2026-04-20T14:00:00+04:00"),
                Duration.ofMinutes(90),
                courseId,
                userId
        );

        LessonResponse response = mock(LessonResponse.class);

        when(lessonRepository.existsByCourseIdAndTitleIgnoreCase(courseId, "Introduction to Spring"))
                .thenReturn(false);
        when(lessonRepository.existsByCourseIdAndOrderIndex(courseId, 1)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.findDetailedById(courseId)).thenReturn(Optional.of(course));
        when(lessonRepository.saveAndFlush(any(Lesson.class))).thenAnswer(invocation -> {
            Lesson saved = invocation.getArgument(0);
            saved.setId(lessonId);
            return saved;
        });
        when(lessonMapper.toResponse(any(Lesson.class))).thenReturn(response);

        LessonResponse result = lessonService.create(request);

        assertNotNull(result);
        assertEquals(response, result);

        ArgumentCaptor<Lesson> lessonCaptor = ArgumentCaptor.forClass(Lesson.class);
        verify(lessonRepository).saveAndFlush(lessonCaptor.capture());

        Lesson savedLesson = lessonCaptor.getValue();
        assertEquals(1, savedLesson.getOrderIndex());
        assertEquals("Introduction to Spring", savedLesson.getTitle());
        assertEquals("First lesson", savedLesson.getDescription());
        assertEquals("https://meet.google.com/test", savedLesson.getValueUrl());
        assertEquals(LessonType.MEET_LINK, savedLesson.getLessonType());
        assertEquals(LessonState.SCHEDULED, savedLesson.getState());
        assertEquals(course, savedLesson.getCourse());
        assertEquals(user, savedLesson.getCreatedBy());
    }

    @Test
    @DisplayName("create -> throws conflict when orderIndex already exists in course")
    void create_ShouldThrowConflict_WhenOrderIndexAlreadyExists() {
        CreateLessonRequest request = new CreateLessonRequest(
                1,
                "Lesson",
                "Desc",
                LessonType.MEET_LINK,
                "https://meet.google.com/test",
                LessonState.SCHEDULED,
                OffsetDateTime.parse("2026-04-20T14:00:00+04:00"),
                Duration.ofMinutes(90),
                courseId,
                userId
        );

        when(lessonRepository.existsByCourseIdAndTitleIgnoreCase(courseId, "Lesson")).thenReturn(false);
        when(lessonRepository.existsByCourseIdAndOrderIndex(courseId, 1)).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> lessonService.create(request)
        );

        assertEquals(409, ex.getStatusCode().value());
        verify(lessonRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("create -> throws bad request when title is blank")
    void create_ShouldThrowBadRequest_WhenTitleIsBlank() {
        CreateLessonRequest request = new CreateLessonRequest(
                1,
                "   ",
                "Desc",
                LessonType.MEET_LINK,
                "https://meet.google.com/test",
                LessonState.SCHEDULED,
                OffsetDateTime.parse("2026-04-20T14:00:00+04:00"),
                Duration.ofMinutes(90),
                courseId,
                userId
        );

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> lessonService.create(request)
        );

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("getById -> returns mapped response")
    void getById_ShouldReturnMappedResponse() {
        LessonResponse response = mock(LessonResponse.class);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonMapper.toResponse(lesson)).thenReturn(response);

        LessonResponse result = lessonService.getById(lessonId);

        assertEquals(response, result);
        verify(lessonRepository).findById(lessonId);
        verify(lessonMapper).toResponse(lesson);
    }

    @Test
    @DisplayName("getAll -> returns mapped list")
    void getAll_ShouldReturnMappedList() {
        Lesson first = new Lesson();
        Lesson second = new Lesson();

        LessonResponse firstResponse = mock(LessonResponse.class);
        LessonResponse secondResponse = mock(LessonResponse.class);

        when(lessonRepository.findAll()).thenReturn(List.of(first, second));
        when(lessonMapper.toResponseList(List.of(first, second)))
                .thenReturn(List.of(firstResponse, secondResponse));

        List<LessonResponse> result = lessonService.getAll();

        assertEquals(2, result.size());
        verify(lessonRepository).findAll();
        verify(lessonMapper).toResponseList(List.of(first, second));
    }

    @Test
    @DisplayName("update -> updates provided fields and returns response")
    void update_ShouldUpdateLessonAndReturnResponse() {
        UpdateLessonRequest request = new UpdateLessonRequest(
                2,
                "  Updated title  ",
                "  Updated description  ",
                LessonType.VIDEO_LINK,
                "  https://youtube.com/test  ",
                LessonState.ONGOING,
                OffsetDateTime.parse("2026-04-22T16:00:00+04:00"),
                Duration.ofMinutes(60)
        );

        LessonResponse response = mock(LessonResponse.class);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonRepository.existsByCourseIdAndOrderIndexAndIdNot(courseId, 2, lessonId)).thenReturn(false);
        when(lessonRepository.existsByCourseIdAndTitleIgnoreCaseAndIdNot(courseId, "Updated title", lessonId))
                .thenReturn(false);
        when(lessonRepository.saveAndFlush(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(lessonMapper.toResponse(any(Lesson.class))).thenReturn(response);

        LessonResponse result = lessonService.update(lessonId, request);

        assertEquals(response, result);
        assertEquals(2, lesson.getOrderIndex());
        assertEquals("Updated title", lesson.getTitle());
        assertEquals("Updated description", lesson.getDescription());
        assertEquals(LessonType.VIDEO_LINK, lesson.getLessonType());
        assertEquals("https://youtube.com/test", lesson.getValueUrl());
        assertEquals(LessonState.ONGOING, lesson.getState());
        assertEquals(OffsetDateTime.parse("2026-04-22T16:00:00+04:00"), lesson.getStartsAt());
        assertEquals(Duration.ofMinutes(60), lesson.getDuration());
    }

    @Test
    @DisplayName("update -> throws conflict when new orderIndex already exists")
    void update_ShouldThrowConflict_WhenOrderIndexAlreadyExists() {
        UpdateLessonRequest request = new UpdateLessonRequest(
                2,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonRepository.existsByCourseIdAndOrderIndexAndIdNot(courseId, 2, lessonId)).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> lessonService.update(lessonId, request)
        );

        assertEquals(409, ex.getStatusCode().value());
        verify(lessonRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("delete -> deletes existing lesson")
    void delete_ShouldDeleteLesson() {
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        lessonService.delete(lessonId);

        verify(lessonRepository).delete(lesson);
    }
}