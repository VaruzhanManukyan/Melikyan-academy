package com.melikyan.academy.service;

import org.mockito.ArgumentCaptor;
import com.melikyan.academy.entity.User;
import org.springframework.http.HttpStatus;
import com.melikyan.academy.entity.Homework;
import com.melikyan.academy.entity.HomeworkTask;
import org.mockito.junit.jupiter.MockitoExtension;
import com.melikyan.academy.entity.enums.TaskType;
import com.melikyan.academy.mapper.HomeworkTaskMapper;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.repository.HomeworkRepository;
import org.springframework.web.server.ResponseStatusException;
import com.melikyan.academy.repository.HomeworkTaskRepository;
import com.melikyan.academy.dto.response.homeworkTask.HomeworkTaskResponse;
import com.melikyan.academy.dto.request.homeworkTask.CreateHomeworkTaskRequest;
import com.melikyan.academy.dto.request.homeworkTask.UpdateHomeworkTaskRequest;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;
import java.time.OffsetDateTime;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HomeworkTaskServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HomeworkRepository homeworkRepository;

    @Mock
    private HomeworkTaskMapper homeworkTaskMapper;

    @Mock
    private HomeworkTaskRepository homeworkTaskRepository;

    @InjectMocks
    private HomeworkTaskService homeworkTaskService;

    @Test
    void create_shouldCreateAndReturnResponse() {
        UUID homeworkId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();
        UUID homeworkTaskId = UUID.randomUUID();

        CreateHomeworkTaskRequest request = new CreateHomeworkTaskRequest(
                1,
                10,
                TaskType.QUIZ,
                Map.of("question", "What is Spring Boot?"),
                homeworkId,
                createdById
        );

        Homework homework = new Homework();
        homework.setId(homeworkId);

        User user = new User();
        user.setId(createdById);

        HomeworkTask savedHomeworkTask = new HomeworkTask();
        savedHomeworkTask.setId(homeworkTaskId);
        savedHomeworkTask.setOrderIndex(1);
        savedHomeworkTask.setPoint(10);
        savedHomeworkTask.setType(TaskType.QUIZ);
        savedHomeworkTask.setPayloadContent(Map.of("question", "What is Spring Boot?"));
        savedHomeworkTask.setHomework(homework);
        savedHomeworkTask.setCreatedBy(user);

        HomeworkTaskResponse response = new HomeworkTaskResponse(
                homeworkTaskId,
                1,
                10,
                TaskType.QUIZ,
                Map.of("question", "What is Spring Boot?"),
                homeworkId,
                createdById,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(homeworkRepository.findById(homeworkId)).thenReturn(Optional.of(homework));
        when(userRepository.findById(createdById)).thenReturn(Optional.of(user));
        when(homeworkTaskRepository.saveAndFlush(any(HomeworkTask.class))).thenReturn(savedHomeworkTask);
        when(homeworkTaskMapper.toResponse(savedHomeworkTask)).thenReturn(response);

        HomeworkTaskResponse result = homeworkTaskService.create(request);

        assertNotNull(result);
        assertEquals(homeworkTaskId, result.id());
        assertEquals(1, result.orderIndex());
        assertEquals(10, result.point());
        assertEquals(TaskType.QUIZ, result.type());

        ArgumentCaptor<HomeworkTask> captor = ArgumentCaptor.forClass(HomeworkTask.class);
        verify(homeworkTaskRepository).saveAndFlush(captor.capture());

        HomeworkTask captured = captor.getValue();
        assertEquals(1, captured.getOrderIndex());
        assertEquals(10, captured.getPoint());
        assertEquals(TaskType.QUIZ, captured.getType());
        assertEquals(homework, captured.getHomework());
        assertEquals(user, captured.getCreatedBy());
    }

    @Test
    void create_shouldThrowNotFound_whenHomeworkNotFound() {
        UUID homeworkId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        CreateHomeworkTaskRequest request = new CreateHomeworkTaskRequest(
                1,
                10,
                TaskType.QUIZ,
                Map.of("question", "Test"),
                homeworkId,
                createdById
        );

        when(homeworkRepository.findById(homeworkId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> homeworkTaskService.create(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(userRepository, never()).findById(any());
        verify(homeworkTaskRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowNotFound_whenUserNotFound() {
        UUID homeworkId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        CreateHomeworkTaskRequest request = new CreateHomeworkTaskRequest(
                1,
                10,
                TaskType.QUIZ,
                Map.of("question", "Test"),
                homeworkId,
                createdById
        );

        Homework homework = new Homework();
        homework.setId(homeworkId);

        when(homeworkRepository.findById(homeworkId)).thenReturn(Optional.of(homework));
        when(userRepository.findById(createdById)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> homeworkTaskService.create(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(homeworkTaskRepository, never()).saveAndFlush(any());
    }

    @Test
    void getById_shouldReturnResponse() {
        UUID id = UUID.randomUUID();
        UUID homeworkId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        Homework homework = new Homework();
        homework.setId(homeworkId);

        User user = new User();
        user.setId(createdById);

        HomeworkTask homeworkTask = new HomeworkTask();
        homeworkTask.setId(id);
        homeworkTask.setOrderIndex(1);
        homeworkTask.setPoint(10);
        homeworkTask.setType(TaskType.QUIZ);
        homeworkTask.setPayloadContent(Map.of("question", "Test"));
        homeworkTask.setHomework(homework);
        homeworkTask.setCreatedBy(user);

        HomeworkTaskResponse response = new HomeworkTaskResponse(
                id,
                1,
                10,
                TaskType.QUIZ,
                Map.of("question", "Test"),
                homeworkId,
                createdById,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(homeworkTaskRepository.findById(id)).thenReturn(Optional.of(homeworkTask));
        when(homeworkTaskMapper.toResponse(homeworkTask)).thenReturn(response);

        HomeworkTaskResponse result = homeworkTaskService.getById(id);

        assertNotNull(result);
        assertEquals(id, result.id());

        verify(homeworkTaskRepository).findById(id);
        verify(homeworkTaskMapper).toResponse(homeworkTask);
    }

    @Test
    void getById_shouldThrowNotFound_whenHomeworkTaskNotFound() {
        UUID id = UUID.randomUUID();

        when(homeworkTaskRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> homeworkTaskService.getById(id)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void getAll_shouldReturnResponseList() {
        UUID id = UUID.randomUUID();

        HomeworkTask homeworkTask = new HomeworkTask();
        homeworkTask.setId(id);

        List<HomeworkTask> entityList = List.of(homeworkTask);
        List<HomeworkTaskResponse> responseList = List.of(
                new HomeworkTaskResponse(
                        id,
                        1,
                        10,
                        TaskType.QUIZ,
                        Map.of("question", "Test"),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                )
        );

        when(homeworkTaskRepository.findAll()).thenReturn(entityList);
        when(homeworkTaskMapper.toResponseList(entityList)).thenReturn(responseList);

        List<HomeworkTaskResponse> result = homeworkTaskService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(homeworkTaskRepository).findAll();
        verify(homeworkTaskMapper).toResponseList(entityList);
    }

    @Test
    void update_shouldUpdateAndReturnResponse() {
        UUID id = UUID.randomUUID();
        UUID homeworkId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        Homework homework = new Homework();
        homework.setId(homeworkId);

        User user = new User();
        user.setId(createdById);

        HomeworkTask homeworkTask = new HomeworkTask();
        homeworkTask.setId(id);
        homeworkTask.setOrderIndex(1);
        homeworkTask.setPoint(10);
        homeworkTask.setType(TaskType.QUIZ);
        homeworkTask.setPayloadContent(Map.of("question", "Old"));
        homeworkTask.setHomework(homework);
        homeworkTask.setCreatedBy(user);

        UpdateHomeworkTaskRequest request = new UpdateHomeworkTaskRequest(
                2,
                15,
                TaskType.ESSAY,
                Map.of("topic", "New topic"),
                homeworkId
        );

        HomeworkTaskResponse response = new HomeworkTaskResponse(
                id,
                2,
                15,
                TaskType.ESSAY,
                Map.of("topic", "New topic"),
                homeworkId,
                createdById,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(homeworkTaskRepository.findById(id)).thenReturn(Optional.of(homeworkTask));
        when(homeworkTaskRepository.save(any(HomeworkTask.class))).thenReturn(homeworkTask);
        when(homeworkTaskMapper.toResponse(homeworkTask)).thenReturn(response);

        HomeworkTaskResponse result = homeworkTaskService.update(id, request);

        assertNotNull(result);
        assertEquals(2, homeworkTask.getOrderIndex());
        assertEquals(15, homeworkTask.getPoint());
        assertEquals(TaskType.ESSAY, homeworkTask.getType());
        assertEquals(Map.of("topic", "New topic"), homeworkTask.getPayloadContent());

        verify(homeworkTaskRepository).save(homeworkTask);
    }

    @Test
    void update_shouldThrowNotFound_whenHomeworkTaskNotFound() {
        UUID id = UUID.randomUUID();

        UpdateHomeworkTaskRequest request = new UpdateHomeworkTaskRequest(
                2,
                15,
                TaskType.ESSAY,
                Map.of("topic", "New topic"),
                UUID.randomUUID()
        );

        when(homeworkTaskRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> homeworkTaskService.update(id, request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void delete_shouldDeleteHomeworkTask() {
        UUID id = UUID.randomUUID();

        HomeworkTask homeworkTask = new HomeworkTask();
        homeworkTask.setId(id);

        when(homeworkTaskRepository.findById(id)).thenReturn(Optional.of(homeworkTask));

        homeworkTaskService.delete(id);

        verify(homeworkTaskRepository).delete(homeworkTask);
    }

    @Test
    void delete_shouldThrowNotFound_whenHomeworkTaskNotFound() {
        UUID id = UUID.randomUUID();

        when(homeworkTaskRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> homeworkTaskService.delete(id)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}