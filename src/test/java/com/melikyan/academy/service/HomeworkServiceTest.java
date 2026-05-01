package com.melikyan.academy.service;

import org.mockito.ArgumentCaptor;
import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.Lesson;
import org.mockito.junit.jupiter.MockitoExtension;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.repository.LessonRepository;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import com.melikyan.academy.dto.response.homework.HomeworkResponse;
import com.melikyan.academy.dto.request.homework.CreateHomeworkRequest;
import com.melikyan.academy.dto.request.homework.UpdateHomeworkRequest;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.time.OffsetDateTime;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

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

    private UUID userId;
    private UUID lessonId;
    private UUID homeworkId;

    private User user;
    private Lesson lesson;
    private Homework homework;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        lessonId = UUID.randomUUID();
        homeworkId = UUID.randomUUID();

        user = new User();
        ReflectionTestUtils.setField(user, "id", userId);

        lesson = new Lesson();
        ReflectionTestUtils.setField(lesson, "id", lessonId);

        homework = new Homework();
        ReflectionTestUtils.setField(homework, "id", homeworkId);
        homework.setOrderIndex(1);
        homework.setTitle("Old title");
        homework.setDescription("Old description");
        homework.setDueDate(OffsetDateTime.parse("2026-04-25T18:00:00+04:00"));
        homework.setIsPublished(true);
        homework.setLesson(lesson);
        homework.setCreatedBy(user);
    }

    @Test
    @DisplayName("create -> saves homework and returns response")
    void create_ShouldSaveHomeworkAndReturnResponse() {
        CreateHomeworkRequest request = mock(CreateHomeworkRequest.class);
        HomeworkResponse response = mock(HomeworkResponse.class);

        OffsetDateTime dueDate = OffsetDateTime.parse("2026-04-25T18:00:00+04:00");

        when(request.orderIndex()).thenReturn(1);
        when(request.title()).thenReturn("  Homework 1  ");
        when(request.description()).thenReturn("  Homework description  ");
        when(request.dueDate()).thenReturn(dueDate);
        when(request.isPublished()).thenReturn(true);
        when(request.lessonId()).thenReturn(lessonId);
        when(request.createdById()).thenReturn(userId);

        when(homeworkRepository.existsByLessonIdAndTitleIgnoreCase(lessonId, "Homework 1")).thenReturn(false);
        when(homeworkRepository.existsByLessonIdAndOrderIndex(lessonId, 1)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(homeworkRepository.saveAndFlush(any(Homework.class))).thenAnswer(invocation -> {
            Homework saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", homeworkId);
            return saved;
        });
        when(homeworkMapper.toResponse(any(Homework.class))).thenReturn(response);

        HomeworkResponse result = homeworkService.create(request);

        assertEquals(response, result);

        ArgumentCaptor<Homework> captor = ArgumentCaptor.forClass(Homework.class);
        verify(homeworkRepository).saveAndFlush(captor.capture());

        Homework savedHomework = captor.getValue();
        assertEquals(1, savedHomework.getOrderIndex());
        assertEquals("Homework 1", savedHomework.getTitle());
        assertEquals("Homework description", savedHomework.getDescription());
        assertEquals(dueDate, savedHomework.getDueDate());
        assertTrue(savedHomework.getIsPublished());
        assertEquals(user, savedHomework.getCreatedBy());
        assertEquals(lesson, savedHomework.getLesson());
    }

    @Test
    @DisplayName("create -> throws bad request when title is blank")
    void create_ShouldThrowBadRequest_WhenTitleIsBlank() {
        CreateHomeworkRequest request = mock(CreateHomeworkRequest.class);

        when(request.title()).thenReturn("   ");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> homeworkService.create(request)
        );

        assertEquals(400, exception.getStatusCode().value());
        assertEquals("Homework title must not be blank", exception.getReason());

        verifyNoInteractions(userRepository, lessonRepository, homeworkRepository, homeworkMapper);
    }

    @Test
    @DisplayName("create -> throws conflict when title already exists")
    void create_ShouldThrowConflict_WhenTitleAlreadyExists() {
        CreateHomeworkRequest request = mock(CreateHomeworkRequest.class);

        when(request.title()).thenReturn("Homework 1");
        when(request.lessonId()).thenReturn(lessonId);

        when(homeworkRepository.existsByLessonIdAndTitleIgnoreCase(lessonId, "Homework 1")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> homeworkService.create(request)
        );

        assertEquals(409, exception.getStatusCode().value());
        assertEquals(
                "Homework with title 'Homework 1' already exists in lesson " + lessonId,
                exception.getReason()
        );

        verify(homeworkRepository, never()).saveAndFlush(any(Homework.class));
        verifyNoInteractions(userRepository, lessonRepository, homeworkMapper);
    }

    @Test
    @DisplayName("getById -> returns mapped response")
    void getById_ShouldReturnMappedResponse() {
        HomeworkResponse response = mock(HomeworkResponse.class);

        when(homeworkRepository.findById(homeworkId)).thenReturn(Optional.of(homework));
        when(homeworkMapper.toResponse(homework)).thenReturn(response);

        HomeworkResponse result = homeworkService.getById(homeworkId);

        assertEquals(response, result);
        verify(homeworkRepository).findById(homeworkId);
        verify(homeworkMapper).toResponse(homework);
    }

    @Test
    @DisplayName("getAll -> returns mapped list")
    void getAll_ShouldReturnMappedList() {
        Homework secondHomework = new Homework();
        List<Homework> entities = List.of(homework, secondHomework);

        HomeworkResponse firstResponse = mock(HomeworkResponse.class);
        HomeworkResponse secondResponse = mock(HomeworkResponse.class);
        List<HomeworkResponse> responses = List.of(firstResponse, secondResponse);

        when(homeworkRepository.findAll()).thenReturn(entities);
        when(homeworkMapper.toResponseList(entities)).thenReturn(responses);

        List<HomeworkResponse> result = homeworkService.getAll();

        assertEquals(responses, result);
        verify(homeworkRepository).findAll();
        verify(homeworkMapper).toResponseList(entities);
    }

    @Test
    @DisplayName("update -> updates provided fields and returns response")
    void update_ShouldUpdateHomeworkAndReturnResponse() {
        UpdateHomeworkRequest request = mock(UpdateHomeworkRequest.class);
        HomeworkResponse response = mock(HomeworkResponse.class);

        OffsetDateTime updatedDueDate = OffsetDateTime.parse("2026-04-30T20:00:00+04:00");

        when(request.orderIndex()).thenReturn(2);
        when(request.title()).thenReturn("  Updated title  ");
        when(request.description()).thenReturn("   ");
        when(request.dueDate()).thenReturn(updatedDueDate);
        when(request.isPublished()).thenReturn(false);

        when(homeworkRepository.findById(homeworkId)).thenReturn(Optional.of(homework));
        when(homeworkRepository.existsByLessonIdAndOrderIndexAndIdNot(lessonId, 2, homeworkId)).thenReturn(false);
        when(homeworkRepository.existsByLessonIdAndTitleIgnoreCaseAndIdNot(lessonId, "Updated title", homeworkId))
                .thenReturn(false);
        when(homeworkRepository.save(any(Homework.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(homeworkMapper.toResponse(any(Homework.class))).thenReturn(response);

        HomeworkResponse result = homeworkService.update(homeworkId, request);

        assertEquals(response, result);
        assertEquals(2, homework.getOrderIndex());
        assertEquals("Updated title", homework.getTitle());
        assertNull(homework.getDescription());
        assertEquals(updatedDueDate, homework.getDueDate());
        assertFalse(homework.getIsPublished());

        verify(homeworkRepository).save(homework);
    }

    @Test
    @DisplayName("update -> throws conflict when order index already exists")
    void update_ShouldThrowConflict_WhenOrderIndexAlreadyExists() {
        UpdateHomeworkRequest request = mock(UpdateHomeworkRequest.class);

        when(request.orderIndex()).thenReturn(2);

        when(homeworkRepository.findById(homeworkId)).thenReturn(Optional.of(homework));
        when(homeworkRepository.existsByLessonIdAndOrderIndexAndIdNot(lessonId, 2, homeworkId)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> homeworkService.update(homeworkId, request)
        );

        assertEquals(409, exception.getStatusCode().value());
        assertEquals(
                "Homework with order index 2 already exists in lesson " + lessonId,
                exception.getReason()
        );

        verify(homeworkRepository, never()).save(any(Homework.class));
        verifyNoInteractions(homeworkMapper);
    }

    @Test
    @DisplayName("delete -> deletes homework")
    void delete_ShouldDeleteHomework() {
        when(homeworkRepository.findById(homeworkId)).thenReturn(Optional.of(homework));

        homeworkService.delete(homeworkId);

        verify(homeworkRepository).delete(homework);
    }
}