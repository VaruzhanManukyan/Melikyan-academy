package com.melikyan.academy.service;

import org.mockito.ArgumentCaptor;
import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.Lesson;
import org.springframework.http.HttpStatus;
import com.melikyan.academy.entity.Homework;
import com.melikyan.academy.mapper.HomeworkMapper;
import org.mockito.junit.jupiter.MockitoExtension;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.repository.LessonRepository;
import org.springframework.test.util.ReflectionTestUtils;
import com.melikyan.academy.repository.HomeworkRepository;
import org.springframework.web.server.ResponseStatusException;
import com.melikyan.academy.dto.response.homework.HomeworkResponse;
import com.melikyan.academy.dto.request.homework.CreateHomeworkRequest;
import com.melikyan.academy.dto.request.homework.UpdateHomeworkRequest;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.time.OffsetDateTime;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class HomeworkServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HomeworkMapper homeworkMapper;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private HomeworkRepository homeworkRepository;

    @InjectMocks
    private HomeworkService homeworkService;

    @Test
    void create_shouldCreateAndReturnResponse() {
        UUID userId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();
        UUID homeworkId = UUID.randomUUID();
        OffsetDateTime dueDate = OffsetDateTime.parse("2026-04-25T18:00:00+04:00");

        CreateHomeworkRequest request = new CreateHomeworkRequest(
                1,
                "  Homework 1  ",
                "  Homework description  ",
                dueDate,
                true,
                lessonId,
                userId
        );

        User user = createUser(userId);
        Lesson lesson = createLesson(lessonId);

        HomeworkResponse response = new HomeworkResponse(
                homeworkId,
                1,
                "Homework 1",
                "Homework description",
                dueDate,
                true,
                lessonId,
                userId,
                OffsetDateTime.parse("2026-04-16T10:00:00+04:00"),
                OffsetDateTime.parse("2026-04-16T10:00:00+04:00")
        );

        when(homeworkRepository.existsByLessonIdAndOrderIndex(lessonId, 1)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(homeworkRepository.saveAndFlush(any(Homework.class))).thenAnswer(invocation -> {
            Homework homework = invocation.getArgument(0);
            ReflectionTestUtils.setField(homework, "id", homeworkId);
            return homework;
        });
        when(homeworkMapper.toResponse(any(Homework.class))).thenReturn(response);

        HomeworkResponse result = homeworkService.create(request);

        assertThat(result).isEqualTo(response);

        ArgumentCaptor<Homework> captor = ArgumentCaptor.forClass(Homework.class);
        verify(homeworkRepository).saveAndFlush(captor.capture());

        Homework savedHomework = captor.getValue();
        assertThat(savedHomework.getTitle()).isEqualTo("Homework 1");
        assertThat(savedHomework.getDescription()).isEqualTo("Homework description");
        assertThat(savedHomework.getOrderIndex()).isEqualTo(1);
        assertThat(savedHomework.getDueDate()).isEqualTo(dueDate);
        assertThat(savedHomework.getIsPublished()).isTrue();
        assertThat(savedHomework.getCreatedBy()).isEqualTo(user);
        assertThat(savedHomework.getLesson()).isEqualTo(lesson);

        verify(homeworkMapper).toResponse(any(Homework.class));
    }

    @Test
    void create_shouldThrowBadRequest_whenTitleIsBlank() {
        CreateHomeworkRequest request = new CreateHomeworkRequest(
                1,
                "   ",
                "Description",
                OffsetDateTime.parse("2026-04-25T18:00:00+04:00"),
                true,
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> homeworkService.create(request)
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getReason()).isEqualTo("Course title must not be blank");

        verifyNoInteractions(userRepository, lessonRepository, homeworkRepository, homeworkMapper);
    }

    @Test
    void create_shouldThrowConflict_whenOrderIndexAlreadyExists() {
        UUID lessonId = UUID.randomUUID();

        CreateHomeworkRequest request = new CreateHomeworkRequest(
                1,
                "Homework 1",
                "Description",
                OffsetDateTime.parse("2026-04-25T18:00:00+04:00"),
                true,
                lessonId,
                UUID.randomUUID()
        );

        when(homeworkRepository.existsByLessonIdAndOrderIndex(lessonId, 1)).thenReturn(true);
        when(homeworkRepository.existsByLessonIdAndTitleIgnoreCase(lessonId, "Homework 1")).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> homeworkService.create(request)
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getReason()).isEqualTo(
                "Homework with order index 1 already exists in lesson " + lessonId
        );

        verify(homeworkRepository).existsByLessonIdAndOrderIndex(lessonId, 1);
        verify(homeworkRepository).existsByLessonIdAndTitleIgnoreCase(lessonId, "Homework 1");
        verify(homeworkRepository, never()).saveAndFlush(any(Homework.class));

        verifyNoInteractions(userRepository, lessonRepository, homeworkMapper);
    }

    @Test
    void create_shouldThrowNotFound_whenUserNotFound() {
        UUID userId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();

        CreateHomeworkRequest request = new CreateHomeworkRequest(
                1,
                "Homework 1",
                "Description",
                OffsetDateTime.parse("2026-04-25T18:00:00+04:00"),
                true,
                lessonId,
                userId
        );

        when(homeworkRepository.existsByLessonIdAndOrderIndex(lessonId, 1)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> homeworkService.create(request)
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("User not found with id: " + userId);

        verify(userRepository).findById(userId);
        verifyNoInteractions(lessonRepository, homeworkMapper);
    }

    @Test
    void getById_shouldReturnResponse() {
        UUID homeworkId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Homework homework = createHomework(
                homeworkId,
                1,
                "Homework 1",
                "Description",
                OffsetDateTime.parse("2026-04-25T18:00:00+04:00"),
                true,
                createLesson(lessonId),
                createUser(userId)
        );

        HomeworkResponse response = new HomeworkResponse(
                homeworkId,
                1,
                "Homework 1",
                "Description",
                OffsetDateTime.parse("2026-04-25T18:00:00+04:00"),
                true,
                lessonId,
                userId,
                OffsetDateTime.parse("2026-04-16T10:00:00+04:00"),
                OffsetDateTime.parse("2026-04-16T10:00:00+04:00")
        );

        when(homeworkRepository.findById(homeworkId)).thenReturn(Optional.of(homework));
        when(homeworkMapper.toResponse(homework)).thenReturn(response);

        HomeworkResponse result = homeworkService.getById(homeworkId);

        assertThat(result).isEqualTo(response);
        verify(homeworkRepository).findById(homeworkId);
        verify(homeworkMapper).toResponse(homework);
    }

    @Test
    void getById_shouldThrowNotFound_whenHomeworkDoesNotExist() {
        UUID homeworkId = UUID.randomUUID();

        when(homeworkRepository.findById(homeworkId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> homeworkService.getById(homeworkId)
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Homework not found with id: " + homeworkId);

        verify(homeworkRepository).findById(homeworkId);
        verifyNoInteractions(homeworkMapper);
    }

    @Test
    void getAll_shouldReturnResponseList() {
        Homework homework1 = createHomework(
                UUID.randomUUID(),
                1,
                "Homework 1",
                "Description 1",
                OffsetDateTime.parse("2026-04-25T18:00:00+04:00"),
                true,
                createLesson(UUID.randomUUID()),
                createUser(UUID.randomUUID())
        );

        Homework homework2 = createHomework(
                UUID.randomUUID(),
                2,
                "Homework 2",
                "Description 2",
                OffsetDateTime.parse("2026-04-26T18:00:00+04:00"),
                false,
                createLesson(UUID.randomUUID()),
                createUser(UUID.randomUUID())
        );

        List<Homework> homeworks = List.of(homework1, homework2);
        List<HomeworkResponse> responses = List.of(mock(HomeworkResponse.class), mock(HomeworkResponse.class));

        when(homeworkRepository.findAll()).thenReturn(homeworks);
        when(homeworkMapper.toResponseList(homeworks)).thenReturn(responses);

        List<HomeworkResponse> result = homeworkService.getAll();

        assertThat(result).isEqualTo(responses);
        verify(homeworkRepository).findAll();
        verify(homeworkMapper).toResponseList(homeworks);
    }

    @Test
    void update_shouldUpdateAndReturnResponse() {
        UUID homeworkId = UUID.randomUUID();
        UUID oldLessonId = UUID.randomUUID();
        UUID newLessonId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Lesson oldLesson = createLesson(oldLessonId);
        Lesson newLesson = createLesson(newLessonId);
        User user = createUser(userId);

        Homework homework = createHomework(
                homeworkId,
                1,
                "Old title",
                "Old description",
                OffsetDateTime.parse("2026-04-25T18:00:00+04:00"),
                true,
                oldLesson,
                user
        );

        UpdateHomeworkRequest request = new UpdateHomeworkRequest(
                2,
                "  Updated title  ",
                "   ",
                OffsetDateTime.parse("2026-04-30T20:00:00+04:00"),
                false,
                newLessonId
        );

        HomeworkResponse response = new HomeworkResponse(
                homeworkId,
                2,
                "Updated title",
                null,
                OffsetDateTime.parse("2026-04-30T20:00:00+04:00"),
                false,
                newLessonId,
                userId,
                OffsetDateTime.parse("2026-04-16T10:00:00+04:00"),
                OffsetDateTime.parse("2026-04-17T10:00:00+04:00")
        );

        when(homeworkRepository.findById(homeworkId)).thenReturn(Optional.of(homework));
        when(homeworkRepository.existsByLessonIdAndOrderIndexAndIdNot(newLessonId, 2, homeworkId))
                .thenReturn(false);
        when(lessonRepository.findById(newLessonId)).thenReturn(Optional.of(newLesson));
        when(homeworkRepository.save(any(Homework.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(homeworkMapper.toResponse(any(Homework.class))).thenReturn(response);

        HomeworkResponse result = homeworkService.update(homeworkId, request);

        assertThat(result).isEqualTo(response);

        ArgumentCaptor<Homework> captor = ArgumentCaptor.forClass(Homework.class);
        verify(homeworkRepository).save(captor.capture());

        Homework savedHomework = captor.getValue();
        assertThat(savedHomework.getTitle()).isEqualTo("Updated title");
        assertThat(savedHomework.getDescription()).isNull();
        assertThat(savedHomework.getOrderIndex()).isEqualTo(2);
        assertThat(savedHomework.getDueDate()).isEqualTo(OffsetDateTime.parse("2026-04-30T20:00:00+04:00"));
        assertThat(savedHomework.getIsPublished()).isFalse();
        assertThat(savedHomework.getLesson()).isEqualTo(newLesson);
    }

    @Test
    void update_shouldThrowConflict_whenTargetOrderIndexAlreadyExists() {
        UUID homeworkId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Homework homework = createHomework(
                homeworkId,
                1,
                "Old title",
                "Old description",
                OffsetDateTime.parse("2026-04-25T18:00:00+04:00"),
                true,
                createLesson(lessonId),
                createUser(userId)
        );

        UpdateHomeworkRequest request = new UpdateHomeworkRequest(
                2,
                "Updated title",
                "Updated description",
                OffsetDateTime.parse("2026-04-30T20:00:00+04:00"),
                false,
                lessonId
        );

        when(homeworkRepository.findById(homeworkId)).thenReturn(Optional.of(homework));
        when(homeworkRepository.existsByLessonIdAndOrderIndexAndIdNot(lessonId, 2, homeworkId))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> homeworkService.update(homeworkId, request)
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getReason()).isEqualTo(
                "Homework with order index 2 already exists in lesson " + lessonId
        );

        verify(homeworkRepository, never()).save(any(Homework.class));
        verifyNoInteractions(homeworkMapper);
    }

    @Test
    void delete_shouldDeleteHomework() {
        UUID homeworkId = UUID.randomUUID();

        Homework homework = createHomework(
                homeworkId,
                1,
                "Homework 1",
                "Description",
                OffsetDateTime.parse("2026-04-25T18:00:00+04:00"),
                true,
                createLesson(UUID.randomUUID()),
                createUser(UUID.randomUUID())
        );

        when(homeworkRepository.findById(homeworkId)).thenReturn(Optional.of(homework));
        doNothing().when(homeworkRepository).delete(homework);

        homeworkService.delete(homeworkId);

        verify(homeworkRepository).findById(homeworkId);
        verify(homeworkRepository).delete(homework);
    }

    private User createUser(UUID id) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Lesson createLesson(UUID id) {
        Lesson lesson = new Lesson();
        ReflectionTestUtils.setField(lesson, "id", id);
        return lesson;
    }

    private Homework createHomework(
            UUID id,
            Integer orderIndex,
            String title,
            String description,
            OffsetDateTime dueDate,
            Boolean isPublished,
            Lesson lesson,
            User createdBy
    ) {
        Homework homework = new Homework();
        ReflectionTestUtils.setField(homework, "id", id);
        homework.setOrderIndex(orderIndex);
        homework.setTitle(title);
        homework.setDescription(description);
        homework.setDueDate(dueDate);
        homework.setIsPublished(isPublished);
        homework.setLesson(lesson);
        homework.setCreatedBy(createdBy);
        return homework;
    }
}
