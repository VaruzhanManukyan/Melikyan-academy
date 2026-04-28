package com.melikyan.academy.service;

import org.mockito.ArgumentCaptor;
import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.Course;
import com.melikyan.academy.entity.Lesson;
import com.melikyan.academy.entity.Homework;
import com.melikyan.academy.entity.ContentItem;
import com.melikyan.academy.entity.HomeworkTask;
import org.mockito.junit.jupiter.MockitoExtension;
import com.melikyan.academy.entity.enums.TaskType;
import com.melikyan.academy.mapper.HomeworkTaskMapper;
import com.melikyan.academy.repository.UserRepository;
import org.springframework.test.util.ReflectionTestUtils;
import com.melikyan.academy.repository.HomeworkRepository;
import com.melikyan.academy.repository.ContentItemRepository;
import org.springframework.web.server.ResponseStatusException;
import com.melikyan.academy.repository.HomeworkTaskRepository;
import com.melikyan.academy.dto.response.homeworkTask.HomeworkTaskResponse;
import com.melikyan.academy.dto.request.homeworkTask.CreateHomeworkTaskRequest;
import com.melikyan.academy.dto.request.homeworkTask.UpdateHomeworkTaskRequest;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;
import java.util.UUID;
import java.util.Optional;

import static org.mockito.Mockito.*;
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
    private ContentItemRepository contentItemRepository;

    @Mock
    private HomeworkTaskRepository homeworkTaskRepository;

    @InjectMocks
    private HomeworkTaskService homeworkTaskService;

    private UUID userId;
    private UUID homeworkId;
    private UUID secondHomeworkId;
    private UUID contentItemId;
    private UUID secondContentItemId;
    private UUID homeworkTaskId;

    private User user;
    private Homework homework;
    private Homework secondHomework;
    private HomeworkTask homeworkTask;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        homeworkId = UUID.randomUUID();
        secondHomeworkId = UUID.randomUUID();
        contentItemId = UUID.randomUUID();
        secondContentItemId = UUID.randomUUID();
        homeworkTaskId = UUID.randomUUID();

        user = new User();
        ReflectionTestUtils.setField(user, "id", userId);

        homework = createHomework(homeworkId, contentItemId);
        secondHomework = createHomework(secondHomeworkId, secondContentItemId);

        homeworkTask = new HomeworkTask();
        ReflectionTestUtils.setField(homeworkTask, "id", homeworkTaskId);
        homeworkTask.setOrderIndex(1);
        homeworkTask.setPoint(10);
        homeworkTask.setType(TaskType.QUIZ);
        homeworkTask.setContentPayload(Map.of("question", "Old"));
        homeworkTask.setHomework(homework);
        homeworkTask.setCreatedBy(user);
    }

    @Test
    @DisplayName("create -> saves homework task, increments steps and returns response")
    void create_ShouldSaveHomeworkTaskIncrementStepsAndReturnResponse() {
        CreateHomeworkTaskRequest request = mock(CreateHomeworkTaskRequest.class);
        HomeworkTaskResponse response = mock(HomeworkTaskResponse.class);

        when(request.orderIndex()).thenReturn(1);
        when(request.point()).thenReturn(10);
        when(request.type()).thenReturn(TaskType.QUIZ);
        when(request.contentPayload()).thenReturn(Map.of("question", "What is Spring Boot?"));
        when(request.homeworkId()).thenReturn(homeworkId);
        when(request.createdById()).thenReturn(userId);

        when(homeworkRepository.findDetailedById(homeworkId)).thenReturn(Optional.of(homework));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(homeworkTaskRepository.existsByHomeworkIdAndOrderIndex(homeworkId, 1)).thenReturn(false);
        when(homeworkTaskRepository.saveAndFlush(any(HomeworkTask.class))).thenAnswer(invocation -> {
            HomeworkTask saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", homeworkTaskId);
            return saved;
        });
        when(contentItemRepository.changeTotalSteps(contentItemId, 1)).thenReturn(1);
        when(homeworkTaskMapper.toResponse(any(HomeworkTask.class))).thenReturn(response);

        HomeworkTaskResponse result = homeworkTaskService.create(request);

        assertEquals(response, result);

        ArgumentCaptor<HomeworkTask> captor = ArgumentCaptor.forClass(HomeworkTask.class);
        verify(homeworkTaskRepository).saveAndFlush(captor.capture());

        HomeworkTask savedTask = captor.getValue();
        assertEquals(1, savedTask.getOrderIndex());
        assertEquals(10, savedTask.getPoint());
        assertEquals(TaskType.QUIZ, savedTask.getType());
        assertEquals(user, savedTask.getCreatedBy());
        assertEquals(homework, savedTask.getHomework());

        verify(contentItemRepository).changeTotalSteps(contentItemId, 1);
    }

    @Test
    @DisplayName("create -> throws conflict when order index already exists")
    void create_ShouldThrowConflict_WhenOrderIndexAlreadyExists() {
        CreateHomeworkTaskRequest request = mock(CreateHomeworkTaskRequest.class);

        when(request.orderIndex()).thenReturn(1);
        when(request.homeworkId()).thenReturn(homeworkId);
        when(request.createdById()).thenReturn(userId);

        when(homeworkRepository.findDetailedById(homeworkId)).thenReturn(Optional.of(homework));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(homeworkTaskRepository.existsByHomeworkIdAndOrderIndex(homeworkId, 1)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> homeworkTaskService.create(request)
        );

        assertEquals(409, exception.getStatusCode().value());
        assertEquals(
                "Homework task with order index 1 already exists in homework " + homeworkId,
                exception.getReason()
        );

        verify(homeworkTaskRepository, never()).saveAndFlush(any(HomeworkTask.class));
        verify(contentItemRepository, never()).changeTotalSteps(any(), anyInt());
    }

    @Test
    @DisplayName("getById -> returns mapped response")
    void getById_ShouldReturnMappedResponse() {
        HomeworkTaskResponse response = mock(HomeworkTaskResponse.class);

        when(homeworkTaskRepository.findById(homeworkTaskId)).thenReturn(Optional.of(homeworkTask));
        when(homeworkTaskMapper.toResponse(homeworkTask)).thenReturn(response);

        HomeworkTaskResponse result = homeworkTaskService.getById(homeworkTaskId);

        assertEquals(response, result);
        verify(homeworkTaskRepository).findById(homeworkTaskId);
        verify(homeworkTaskMapper).toResponse(homeworkTask);
    }

    @Test
    @DisplayName("update -> moves task to another homework, updates fields and syncs total steps")
    void update_ShouldMoveTaskAndSyncContentItemSteps() {
        UpdateHomeworkTaskRequest request = mock(UpdateHomeworkTaskRequest.class);
        HomeworkTaskResponse response = mock(HomeworkTaskResponse.class);

        when(request.homeworkId()).thenReturn(secondHomeworkId);
        when(request.orderIndex()).thenReturn(2);
        when(request.point()).thenReturn(15);
        when(request.type()).thenReturn(TaskType.ESSAY);
        when(request.contentPayload()).thenReturn(Map.of("topic", "New topic"));

        when(homeworkTaskRepository.findById(homeworkTaskId)).thenReturn(Optional.of(homeworkTask));
        when(homeworkRepository.findDetailedById(homeworkId)).thenReturn(Optional.of(homework));
        when(homeworkRepository.findDetailedById(secondHomeworkId)).thenReturn(Optional.of(secondHomework));
        when(homeworkTaskRepository.existsByHomeworkIdAndOrderIndexAndIdNot(secondHomeworkId, 2, homeworkTaskId))
                .thenReturn(false);
        when(contentItemRepository.changeTotalSteps(contentItemId, -1)).thenReturn(1);
        when(contentItemRepository.changeTotalSteps(secondContentItemId, 1)).thenReturn(1);
        when(homeworkTaskRepository.saveAndFlush(any(HomeworkTask.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(homeworkTaskMapper.toResponse(any(HomeworkTask.class))).thenReturn(response);

        HomeworkTaskResponse result = homeworkTaskService.update(homeworkTaskId, request);

        assertEquals(response, result);
        assertEquals(secondHomework, homeworkTask.getHomework());
        assertEquals(2, homeworkTask.getOrderIndex());
        assertEquals(15, homeworkTask.getPoint());
        assertEquals(TaskType.ESSAY, homeworkTask.getType());
        assertEquals(Map.of("topic", "New topic"), homeworkTask.getContentPayload());

        verify(contentItemRepository).changeTotalSteps(contentItemId, -1);
        verify(contentItemRepository).changeTotalSteps(secondContentItemId, 1);
        verify(homeworkTaskRepository).saveAndFlush(homeworkTask);
    }

    @Test
    @DisplayName("delete -> decrements total steps and deletes task")
    void delete_ShouldDecrementTotalStepsAndDeleteTask() {
        when(homeworkTaskRepository.findById(homeworkTaskId)).thenReturn(Optional.of(homeworkTask));
        when(homeworkRepository.findDetailedById(homeworkId)).thenReturn(Optional.of(homework));
        when(contentItemRepository.changeTotalSteps(contentItemId, -1)).thenReturn(1);

        homeworkTaskService.delete(homeworkTaskId);

        verify(contentItemRepository).changeTotalSteps(contentItemId, -1);
        verify(homeworkTaskRepository).delete(homeworkTask);
    }

    private Homework createHomework(UUID homeworkId, UUID contentItemId) {
        ContentItem contentItem = new ContentItem();
        ReflectionTestUtils.setField(contentItem, "id", contentItemId);

        Course course = new Course();
        course.setContentItem(contentItem);

        Lesson lesson = new Lesson();
        lesson.setCourse(course);

        Homework homework = new Homework();
        ReflectionTestUtils.setField(homework, "id", homeworkId);
        homework.setLesson(lesson);

        return homework;
    }
}