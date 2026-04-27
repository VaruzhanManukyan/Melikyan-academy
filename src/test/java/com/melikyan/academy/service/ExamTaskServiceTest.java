package com.melikyan.academy.service;

import org.mockito.ArgumentCaptor;
import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.Exam;
import com.melikyan.academy.entity.ExamTask;
import com.melikyan.academy.entity.ContentItem;
import com.melikyan.academy.entity.ExamSection;
import com.melikyan.academy.mapper.ExamTaskMapper;
import org.mockito.junit.jupiter.MockitoExtension;
import com.melikyan.academy.entity.enums.TaskType;
import com.melikyan.academy.repository.UserRepository;
import org.springframework.test.util.ReflectionTestUtils;
import com.melikyan.academy.repository.ExamTaskRepository;
import com.melikyan.academy.repository.ContentItemRepository;
import com.melikyan.academy.repository.ExamSectionRepository;
import org.springframework.web.server.ResponseStatusException;
import com.melikyan.academy.dto.response.examTask.ExamTaskResponse;
import com.melikyan.academy.dto.request.examTask.CreateExamTaskRequest;
import com.melikyan.academy.dto.request.examTask.UpdateExamTaskRequest;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ExamTaskServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ExamTaskMapper examTaskMapper;

    @Mock
    private ExamTaskRepository examTaskRepository;

    @Mock
    private ContentItemRepository contentItemRepository;

    @Mock
    private ExamSectionRepository examSectionRepository;

    @InjectMocks
    private ExamTaskService examTaskService;

    private UUID userId;
    private UUID sectionId;
    private UUID secondSectionId;
    private UUID contentItemId;
    private UUID secondContentItemId;
    private UUID examTaskId;

    private User user;
    private ExamSection section;
    private ExamSection secondSection;
    private ExamTask examTask;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        sectionId = UUID.randomUUID();
        secondSectionId = UUID.randomUUID();
        contentItemId = UUID.randomUUID();
        secondContentItemId = UUID.randomUUID();
        examTaskId = UUID.randomUUID();

        user = new User();
        ReflectionTestUtils.setField(user, "id", userId);

        section = createExamSection(sectionId, contentItemId);
        secondSection = createExamSection(secondSectionId, secondContentItemId);

        examTask = new ExamTask();
        ReflectionTestUtils.setField(examTask, "id", examTaskId);
        examTask.setOrderIndex(1);
        examTask.setPoint(10);
        examTask.setType(TaskType.QUIZ);
        examTask.setContentPayload(Map.of("question", "Old question"));
        examTask.setSection(section);
        examTask.setCreatedBy(user);
    }

    @Test
    @DisplayName("create -> saves exam task, increments steps and returns response")
    void create_ShouldSaveExamTaskIncrementStepsAndReturnResponse() {
        CreateExamTaskRequest request = mock(CreateExamTaskRequest.class);
        ExamTaskResponse response = mock(ExamTaskResponse.class);

        when(request.orderIndex()).thenReturn(1);
        when(request.point()).thenReturn(10);
        when(request.type()).thenReturn(TaskType.QUIZ);
        when(request.contentPayload()).thenReturn(Map.of("question", "What is Spring Boot?"));
        when(request.sectionId()).thenReturn(sectionId);
        when(request.createdById()).thenReturn(userId);

        when(examSectionRepository.findDetailedById(sectionId)).thenReturn(Optional.of(section));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(examTaskRepository.existsBySectionIdAndOrderIndex(sectionId, 1)).thenReturn(false);
        when(examTaskRepository.saveAndFlush(any(ExamTask.class))).thenAnswer(invocation -> {
            ExamTask saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", examTaskId);
            return saved;
        });
        when(contentItemRepository.changeTotalSteps(contentItemId, 1)).thenReturn(1);
        when(examTaskMapper.toResponse(any(ExamTask.class))).thenReturn(response);

        ExamTaskResponse result = examTaskService.create(request);

        assertEquals(response, result);

        ArgumentCaptor<ExamTask> captor = ArgumentCaptor.forClass(ExamTask.class);
        verify(examTaskRepository).saveAndFlush(captor.capture());

        ExamTask savedTask = captor.getValue();
        assertEquals(1, savedTask.getOrderIndex());
        assertEquals(10, savedTask.getPoint());
        assertEquals(TaskType.QUIZ, savedTask.getType());
        assertEquals(Map.of("question", "What is Spring Boot?"), savedTask.getContentPayload());
        assertEquals(user, savedTask.getCreatedBy());
        assertEquals(section, savedTask.getSection());

        verify(contentItemRepository).changeTotalSteps(contentItemId, 1);
        verify(examTaskMapper).toResponse(any(ExamTask.class));
    }

    @Test
    @DisplayName("create -> throws conflict when order index already exists")
    void create_ShouldThrowConflict_WhenOrderIndexAlreadyExists() {
        CreateExamTaskRequest request = mock(CreateExamTaskRequest.class);

        when(request.orderIndex()).thenReturn(1);
        when(request.sectionId()).thenReturn(sectionId);
        when(request.createdById()).thenReturn(userId);

        when(examSectionRepository.findDetailedById(sectionId)).thenReturn(Optional.of(section));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(examTaskRepository.existsBySectionIdAndOrderIndex(sectionId, 1)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> examTaskService.create(request)
        );

        assertEquals(409, exception.getStatusCode().value());
        assertEquals(
                "Exam task with order index 1 already exists in exam section " + sectionId,
                exception.getReason()
        );

        verify(examTaskRepository, never()).saveAndFlush(any(ExamTask.class));
        verify(contentItemRepository, never()).changeTotalSteps(any(), anyInt());
    }

    @Test
    @DisplayName("create -> throws not found when section does not exist")
    void create_ShouldThrowNotFound_WhenSectionDoesNotExist() {
        CreateExamTaskRequest request = mock(CreateExamTaskRequest.class);

        when(request.sectionId()).thenReturn(sectionId);

        when(examSectionRepository.findDetailedById(sectionId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> examTaskService.create(request)
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Exam section not found with id: " + sectionId, exception.getReason());

        verify(userRepository, never()).findById(any(UUID.class));
        verify(examTaskRepository, never()).saveAndFlush(any(ExamTask.class));
    }

    @Test
    @DisplayName("create -> throws not found when user does not exist")
    void create_ShouldThrowNotFound_WhenUserDoesNotExist() {
        CreateExamTaskRequest request = mock(CreateExamTaskRequest.class);

        when(request.sectionId()).thenReturn(sectionId);
        when(request.createdById()).thenReturn(userId);

        when(examSectionRepository.findDetailedById(sectionId)).thenReturn(Optional.of(section));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> examTaskService.create(request)
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("User not found with id: " + userId, exception.getReason());

        verify(examTaskRepository, never()).saveAndFlush(any(ExamTask.class));
    }

    @Test
    @DisplayName("getById -> returns mapped response")
    void getById_ShouldReturnMappedResponse() {
        ExamTaskResponse response = mock(ExamTaskResponse.class);

        when(examTaskRepository.findById(examTaskId)).thenReturn(Optional.of(examTask));
        when(examTaskMapper.toResponse(examTask)).thenReturn(response);

        ExamTaskResponse result = examTaskService.getById(examTaskId);

        assertEquals(response, result);
        verify(examTaskRepository).findById(examTaskId);
        verify(examTaskMapper).toResponse(examTask);
    }

    @Test
    @DisplayName("getById -> throws not found when exam task does not exist")
    void getById_ShouldThrowNotFound_WhenExamTaskDoesNotExist() {
        when(examTaskRepository.findById(examTaskId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> examTaskService.getById(examTaskId)
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Exam task not found with id: " + examTaskId, exception.getReason());

        verify(examTaskMapper, never()).toResponse(any(ExamTask.class));
    }

    @Test
    @DisplayName("getAll -> returns mapped list")
    void getAll_ShouldReturnMappedList() {
        ExamTask first = new ExamTask();
        ExamTask second = new ExamTask();

        ExamTaskResponse firstResponse = mock(ExamTaskResponse.class);
        ExamTaskResponse secondResponse = mock(ExamTaskResponse.class);

        List<ExamTask> examTasks = List.of(first, second);
        List<ExamTaskResponse> responses = List.of(firstResponse, secondResponse);

        when(examTaskRepository.findAll()).thenReturn(examTasks);
        when(examTaskMapper.toResponseList(examTasks)).thenReturn(responses);

        List<ExamTaskResponse> result = examTaskService.getAll();

        assertEquals(2, result.size());
        assertEquals(responses, result);
        verify(examTaskRepository).findAll();
        verify(examTaskMapper).toResponseList(examTasks);
    }

    @Test
    @DisplayName("getAllByExamSectionId -> returns mapped list")
    void getAllByExamSectionId_ShouldReturnMappedList() {
        ExamTask first = new ExamTask();
        ExamTask second = new ExamTask();

        ExamTaskResponse firstResponse = mock(ExamTaskResponse.class);
        ExamTaskResponse secondResponse = mock(ExamTaskResponse.class);

        List<ExamTask> examTasks = List.of(first, second);
        List<ExamTaskResponse> responses = List.of(firstResponse, secondResponse);

        when(examSectionRepository.findDetailedById(sectionId)).thenReturn(Optional.of(section));
        when(examTaskRepository.findAllBySectionIdOrderByOrderIndexAsc(sectionId)).thenReturn(examTasks);
        when(examTaskMapper.toResponseList(examTasks)).thenReturn(responses);

        List<ExamTaskResponse> result = examTaskService.getAllByExamSectionId(sectionId);

        assertEquals(2, result.size());
        assertEquals(responses, result);
        verify(examSectionRepository).findDetailedById(sectionId);
        verify(examTaskRepository).findAllBySectionIdOrderByOrderIndexAsc(sectionId);
        verify(examTaskMapper).toResponseList(examTasks);
    }

    @Test
    @DisplayName("update -> moves task to another section, updates fields and syncs total steps")
    void update_ShouldMoveTaskAndSyncContentItemSteps() {
        UpdateExamTaskRequest request = mock(UpdateExamTaskRequest.class);
        ExamTaskResponse response = mock(ExamTaskResponse.class);

        when(request.sectionId()).thenReturn(secondSectionId);
        when(request.orderIndex()).thenReturn(2);
        when(request.point()).thenReturn(15);
        when(request.type()).thenReturn(TaskType.ESSAY);
        when(request.contentPayload()).thenReturn(Map.of("topic", "New topic"));

        when(examTaskRepository.findById(examTaskId)).thenReturn(Optional.of(examTask));
        when(examSectionRepository.findDetailedById(sectionId)).thenReturn(Optional.of(section));
        when(examSectionRepository.findDetailedById(secondSectionId)).thenReturn(Optional.of(secondSection));
        when(examTaskRepository.existsBySectionIdAndOrderIndexAndIdNot(secondSectionId, 2, examTaskId))
                .thenReturn(false);
        when(contentItemRepository.changeTotalSteps(contentItemId, -1)).thenReturn(1);
        when(contentItemRepository.changeTotalSteps(secondContentItemId, 1)).thenReturn(1);
        when(examTaskRepository.saveAndFlush(any(ExamTask.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(examTaskMapper.toResponse(any(ExamTask.class))).thenReturn(response);

        ExamTaskResponse result = examTaskService.update(examTaskId, request);

        assertEquals(response, result);
        assertEquals(secondSection, examTask.getSection());
        assertEquals(2, examTask.getOrderIndex());
        assertEquals(15, examTask.getPoint());
        assertEquals(TaskType.ESSAY, examTask.getType());
        assertEquals(Map.of("topic", "New topic"), examTask.getContentPayload());

        verify(contentItemRepository).changeTotalSteps(contentItemId, -1);
        verify(contentItemRepository).changeTotalSteps(secondContentItemId, 1);
        verify(examTaskRepository).saveAndFlush(examTask);
    }

    @Test
    @DisplayName("update -> updates fields in same section without syncing total steps")
    void update_ShouldUpdateFieldsInSameSectionWithoutSyncingSteps() {
        UpdateExamTaskRequest request = mock(UpdateExamTaskRequest.class);
        ExamTaskResponse response = mock(ExamTaskResponse.class);

        when(request.sectionId()).thenReturn(null);
        when(request.orderIndex()).thenReturn(2);
        when(request.point()).thenReturn(20);
        when(request.type()).thenReturn(TaskType.CODE);
        when(request.contentPayload()).thenReturn(Map.of("task", "Write code"));

        when(examTaskRepository.findById(examTaskId)).thenReturn(Optional.of(examTask));
        when(examSectionRepository.findDetailedById(sectionId)).thenReturn(Optional.of(section));
        when(examTaskRepository.existsBySectionIdAndOrderIndexAndIdNot(sectionId, 2, examTaskId))
                .thenReturn(false);
        when(examTaskRepository.saveAndFlush(any(ExamTask.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(examTaskMapper.toResponse(any(ExamTask.class))).thenReturn(response);

        ExamTaskResponse result = examTaskService.update(examTaskId, request);

        assertEquals(response, result);
        assertEquals(section, examTask.getSection());
        assertEquals(2, examTask.getOrderIndex());
        assertEquals(20, examTask.getPoint());
        assertEquals(TaskType.CODE, examTask.getType());
        assertEquals(Map.of("task", "Write code"), examTask.getContentPayload());

        verify(contentItemRepository, never()).changeTotalSteps(any(), anyInt());
        verify(examTaskRepository).saveAndFlush(examTask);
    }

    @Test
    @DisplayName("update -> throws conflict when order index already exists")
    void update_ShouldThrowConflict_WhenOrderIndexAlreadyExists() {
        UpdateExamTaskRequest request = mock(UpdateExamTaskRequest.class);

        when(request.sectionId()).thenReturn(null);
        when(request.orderIndex()).thenReturn(2);

        when(examTaskRepository.findById(examTaskId)).thenReturn(Optional.of(examTask));
        when(examSectionRepository.findDetailedById(sectionId)).thenReturn(Optional.of(section));
        when(examTaskRepository.existsBySectionIdAndOrderIndexAndIdNot(sectionId, 2, examTaskId))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> examTaskService.update(examTaskId, request)
        );

        assertEquals(409, exception.getStatusCode().value());
        assertEquals(
                "Exam task with order index 2 already exists in exam section " + sectionId,
                exception.getReason()
        );

        verify(examTaskRepository, never()).saveAndFlush(any(ExamTask.class));
        verify(contentItemRepository, never()).changeTotalSteps(any(), anyInt());
    }

    @Test
    @DisplayName("delete -> decrements total steps and deletes task")
    void delete_ShouldDecrementTotalStepsAndDeleteTask() {
        when(examTaskRepository.findById(examTaskId)).thenReturn(Optional.of(examTask));
        when(examSectionRepository.findDetailedById(sectionId)).thenReturn(Optional.of(section));
        when(contentItemRepository.changeTotalSteps(contentItemId, -1)).thenReturn(1);

        examTaskService.delete(examTaskId);

        verify(contentItemRepository).changeTotalSteps(contentItemId, -1);
        verify(examTaskRepository).delete(examTask);
    }

    @Test
    @DisplayName("delete -> throws not found when exam task does not exist")
    void delete_ShouldThrowNotFound_WhenExamTaskDoesNotExist() {
        when(examTaskRepository.findById(examTaskId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> examTaskService.delete(examTaskId)
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Exam task not found with id: " + examTaskId, exception.getReason());

        verify(contentItemRepository, never()).changeTotalSteps(any(), anyInt());
        verify(examTaskRepository, never()).delete(any(ExamTask.class));
    }

    private ExamSection createExamSection(UUID sectionId, UUID contentItemId) {
        ContentItem contentItem = new ContentItem();
        ReflectionTestUtils.setField(contentItem, "id", contentItemId);

        Exam exam = new Exam();
        exam.setContentItem(contentItem);

        ExamSection section = new ExamSection();
        ReflectionTestUtils.setField(section, "id", sectionId);
        section.setExam(exam);

        return section;
    }
}