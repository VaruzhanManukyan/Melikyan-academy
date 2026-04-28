package com.melikyan.academy.service;

import org.mockito.ArgumentCaptor;
import com.melikyan.academy.entity.*;
import com.melikyan.academy.repository.*;
import org.springframework.http.HttpStatus;
import org.mockito.junit.jupiter.MockitoExtension;
import com.melikyan.academy.entity.enums.ExamStatus;
import com.melikyan.academy.mapper.ExamSubmissionMapper;
import org.springframework.security.core.Authentication;
import com.melikyan.academy.entity.enums.RegistrationStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import com.melikyan.academy.dto.response.examSubmission.ExamSubmissionResponse;
import com.melikyan.academy.dto.request.examSubmission.CreateExamSubmissionRequest;
import com.melikyan.academy.dto.request.examSubmission.UpdateExamSubmissionRequest;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ExamSubmissionServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ExamTaskRepository examTaskRepository;

    @Mock
    private ExamSubmissionMapper examSubmissionMapper;

    @Mock
    private UserProcessRepository userProcessRepository;

    @Mock
    private ExamSubmissionRepository examSubmissionRepository;

    @Mock
    private ProductRegistrationRepository productRegistrationRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ExamSubmissionService examSubmissionService;

    private void mockAuthentication(User user) {
        when(authentication.getName()).thenReturn("student@example.com");
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(user));
    }

    private void mockExamTaskContentItemPath(
            ExamTask task,
            ExamSection section,
            Exam exam,
            ContentItem contentItem
    ) {
        when(task.getSection()).thenReturn(section);
        when(section.getExam()).thenReturn(exam);
        when(exam.getContentItem()).thenReturn(contentItem);
    }

    @Test
    @DisplayName("create -> creates exam submission when user has active access")
    void create_ShouldCreateExamSubmission_WhenUserHasActiveAccess() {
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID contentItemId = UUID.randomUUID();
        UUID submissionId = UUID.randomUUID();

        User user = mock(User.class);
        ExamTask task = mock(ExamTask.class);
        ExamSection section = mock(ExamSection.class);
        Exam exam = mock(Exam.class);
        ContentItem contentItem = mock(ContentItem.class);

        CreateExamSubmissionRequest request = new CreateExamSubmissionRequest(
                Map.of("answer", "My answer"),
                taskId
        );

        ExamSubmissionResponse response = new ExamSubmissionResponse(
                submissionId,
                null,
                ExamStatus.PENDING_REVIEW,
                Map.of("answer", "My answer"),
                userId,
                taskId,
                OffsetDateTime.parse("2026-04-20T14:00:00+04:00"),
                OffsetDateTime.parse("2026-04-20T14:00:00+04:00")
        );

        when(user.getId()).thenReturn(userId);
        when(task.getId()).thenReturn(taskId);
        when(contentItem.getId()).thenReturn(contentItemId);

        mockAuthentication(user);
        when(examTaskRepository.findById(taskId)).thenReturn(Optional.of(task));
        mockExamTaskContentItemPath(task, section, exam, contentItem);

        when(productRegistrationRepository.findUserIdsByContentItemIdAndStatus(
                contentItemId,
                RegistrationStatus.ACTIVE
        )).thenReturn(List.of(userId));

        when(examSubmissionRepository.existsByUserIdAndTaskId(userId, taskId)).thenReturn(false);
        when(userProcessRepository.findByUserIdAndContentItemId(userId, contentItemId)).thenReturn(Optional.empty());
        when(examSubmissionRepository.saveAndFlush(any(ExamSubmission.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(examSubmissionMapper.toResponse(any(ExamSubmission.class))).thenReturn(response);

        ExamSubmissionResponse result = examSubmissionService.create(request, authentication);

        assertEquals(response, result);

        ArgumentCaptor<ExamSubmission> submissionCaptor = ArgumentCaptor.forClass(ExamSubmission.class);
        verify(examSubmissionRepository).saveAndFlush(submissionCaptor.capture());

        ExamSubmission savedSubmission = submissionCaptor.getValue();

        assertEquals(ExamStatus.PENDING_REVIEW, savedSubmission.getStatus());
        assertEquals(Map.of("answer", "My answer"), savedSubmission.getAnswerPayload());
        assertEquals(user, savedSubmission.getUser());
        assertEquals(task, savedSubmission.getTask());

        ArgumentCaptor<UserProcess> userProcessCaptor = ArgumentCaptor.forClass(UserProcess.class);
        verify(userProcessRepository).save(userProcessCaptor.capture());

        UserProcess savedUserProcess = userProcessCaptor.getValue();

        assertEquals(1, savedUserProcess.getCurrentStep());
        assertEquals(BigDecimal.ZERO, savedUserProcess.getScoreAccumulated());
        assertEquals(user, savedUserProcess.getUser());
        assertEquals(contentItem, savedUserProcess.getContentItem());
        assertNotNull(savedUserProcess.getLastAccessedAt());
    }

    @Test
    @DisplayName("create -> throws forbidden when user does not have active access")
    void create_ShouldThrowForbidden_WhenUserDoesNotHaveActiveAccess() {
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID contentItemId = UUID.randomUUID();

        User user = mock(User.class);
        ExamTask task = mock(ExamTask.class);
        ExamSection section = mock(ExamSection.class);
        Exam exam = mock(Exam.class);
        ContentItem contentItem = mock(ContentItem.class);

        CreateExamSubmissionRequest request = new CreateExamSubmissionRequest(
                Map.of("answer", "My answer"),
                taskId
        );

        when(user.getId()).thenReturn(userId);
        when(contentItem.getId()).thenReturn(contentItemId);

        mockAuthentication(user);
        when(examTaskRepository.findById(taskId)).thenReturn(Optional.of(task));
        mockExamTaskContentItemPath(task, section, exam, contentItem);

        when(productRegistrationRepository.findUserIdsByContentItemIdAndStatus(
                contentItemId,
                RegistrationStatus.ACTIVE
        )).thenReturn(List.of());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> examSubmissionService.create(request, authentication)
        );

        assertEquals(HttpStatus.FORBIDDEN.value(), exception.getStatusCode().value());
        assertEquals("User does not have active access to this exam", exception.getReason());

        verify(examSubmissionRepository, never()).existsByUserIdAndTaskId(any(), any());
        verify(examSubmissionRepository, never()).saveAndFlush(any());
        verify(userProcessRepository, never()).save(any());
    }

    @Test
    @DisplayName("create -> throws conflict when submission already exists")
    void create_ShouldThrowConflict_WhenSubmissionAlreadyExists() {
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID contentItemId = UUID.randomUUID();

        User user = mock(User.class);
        ExamTask task = mock(ExamTask.class);
        ExamSection section = mock(ExamSection.class);
        Exam exam = mock(Exam.class);
        ContentItem contentItem = mock(ContentItem.class);

        CreateExamSubmissionRequest request = new CreateExamSubmissionRequest(
                Map.of("answer", "My answer"),
                taskId
        );

        when(user.getId()).thenReturn(userId);
        when(task.getId()).thenReturn(taskId);
        when(contentItem.getId()).thenReturn(contentItemId);

        mockAuthentication(user);
        when(examTaskRepository.findById(taskId)).thenReturn(Optional.of(task));
        mockExamTaskContentItemPath(task, section, exam, contentItem);

        when(productRegistrationRepository.findUserIdsByContentItemIdAndStatus(
                contentItemId,
                RegistrationStatus.ACTIVE
        )).thenReturn(List.of(userId));

        when(examSubmissionRepository.existsByUserIdAndTaskId(userId, taskId)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> examSubmissionService.create(request, authentication)
        );

        assertEquals(HttpStatus.CONFLICT.value(), exception.getStatusCode().value());
        assertEquals("Exam submission already exists for this task", exception.getReason());

        verify(examSubmissionRepository, never()).saveAndFlush(any());
        verify(userProcessRepository, never()).save(any());
    }

    @Test
    @DisplayName("create -> throws conflict when save violates unique constraint")
    void create_ShouldThrowConflict_WhenSaveThrowsDataIntegrityViolationException() {
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID contentItemId = UUID.randomUUID();

        User user = mock(User.class);
        ExamTask task = mock(ExamTask.class);
        ExamSection section = mock(ExamSection.class);
        Exam exam = mock(Exam.class);
        ContentItem contentItem = mock(ContentItem.class);

        CreateExamSubmissionRequest request = new CreateExamSubmissionRequest(
                Map.of("answer", "My answer"),
                taskId
        );

        when(user.getId()).thenReturn(userId);
        when(task.getId()).thenReturn(taskId);
        when(contentItem.getId()).thenReturn(contentItemId);

        mockAuthentication(user);
        when(examTaskRepository.findById(taskId)).thenReturn(Optional.of(task));
        mockExamTaskContentItemPath(task, section, exam, contentItem);

        when(productRegistrationRepository.findUserIdsByContentItemIdAndStatus(
                contentItemId,
                RegistrationStatus.ACTIVE
        )).thenReturn(List.of(userId));

        when(examSubmissionRepository.existsByUserIdAndTaskId(userId, taskId)).thenReturn(false);
        when(examSubmissionRepository.saveAndFlush(any(ExamSubmission.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> examSubmissionService.create(request, authentication)
        );

        assertEquals(HttpStatus.CONFLICT.value(), exception.getStatusCode().value());
        assertEquals("Exam submission already exists for this task", exception.getReason());

        verify(userProcessRepository, never()).save(any());
    }

    @Test
    @DisplayName("getById -> returns exam submission by id")
    void getById_ShouldReturnExamSubmission() {
        UUID submissionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        ExamSubmission examSubmission = new ExamSubmission();

        ExamSubmissionResponse response = new ExamSubmissionResponse(
                submissionId,
                null,
                ExamStatus.PENDING_REVIEW,
                Map.of("answer", "My answer"),
                userId,
                taskId,
                OffsetDateTime.parse("2026-04-20T14:00:00+04:00"),
                OffsetDateTime.parse("2026-04-20T14:00:00+04:00")
        );

        when(examSubmissionRepository.findById(submissionId)).thenReturn(Optional.of(examSubmission));
        when(examSubmissionMapper.toResponse(examSubmission)).thenReturn(response);

        ExamSubmissionResponse result = examSubmissionService.getById(submissionId);

        assertEquals(response, result);
    }

    @Test
    @DisplayName("getMyById -> returns current user's exam submission by id")
    void getMyById_ShouldReturnMyExamSubmission() {
        UUID userId = UUID.randomUUID();
        UUID submissionId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        User user = mock(User.class);
        ExamSubmission examSubmission = new ExamSubmission();

        ExamSubmissionResponse response = new ExamSubmissionResponse(
                submissionId,
                null,
                ExamStatus.PENDING_REVIEW,
                Map.of("answer", "My answer"),
                userId,
                taskId,
                OffsetDateTime.parse("2026-04-20T14:00:00+04:00"),
                OffsetDateTime.parse("2026-04-20T14:00:00+04:00")
        );

        when(user.getId()).thenReturn(userId);
        mockAuthentication(user);

        when(examSubmissionRepository.findByIdAndUserId(submissionId, userId))
                .thenReturn(Optional.of(examSubmission));
        when(examSubmissionMapper.toResponse(examSubmission)).thenReturn(response);

        ExamSubmissionResponse result = examSubmissionService.getMyById(submissionId, authentication);

        assertEquals(response, result);
    }

    @Test
    @DisplayName("getMyAll -> returns current user's exam submissions")
    void getMyAll_ShouldReturnMyExamSubmissions() {
        UUID userId = UUID.randomUUID();

        User user = mock(User.class);
        ExamSubmission first = new ExamSubmission();
        ExamSubmission second = new ExamSubmission();

        List<ExamSubmissionResponse> responses = List.of(
                new ExamSubmissionResponse(
                        UUID.randomUUID(),
                        null,
                        ExamStatus.PENDING_REVIEW,
                        Map.of("answer", "First answer"),
                        userId,
                        UUID.randomUUID(),
                        OffsetDateTime.parse("2026-04-20T14:00:00+04:00"),
                        OffsetDateTime.parse("2026-04-20T14:00:00+04:00")
                ),
                new ExamSubmissionResponse(
                        UUID.randomUUID(),
                        "Good work",
                        ExamStatus.PASSED,
                        Map.of("answer", "Second answer"),
                        userId,
                        UUID.randomUUID(),
                        OffsetDateTime.parse("2026-04-21T14:00:00+04:00"),
                        OffsetDateTime.parse("2026-04-21T15:00:00+04:00")
                )
        );

        when(user.getId()).thenReturn(userId);
        mockAuthentication(user);

        when(examSubmissionRepository.findAllByUserId(userId)).thenReturn(List.of(first, second));
        when(examSubmissionMapper.toResponseList(List.of(first, second))).thenReturn(responses);

        List<ExamSubmissionResponse> result = examSubmissionService.getMyAll(authentication);

        assertEquals(responses, result);
    }

    @Test
    @DisplayName("getMyByTask -> returns current user's exam submission by task id")
    void getMyByTask_ShouldReturnMyExamSubmissionByTask() {
        UUID userId = UUID.randomUUID();
        UUID submissionId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        User user = mock(User.class);
        ExamSubmission examSubmission = new ExamSubmission();

        ExamSubmissionResponse response = new ExamSubmissionResponse(
                submissionId,
                "Need review",
                ExamStatus.PENDING_REVIEW,
                Map.of("answer", "Task answer"),
                userId,
                taskId,
                OffsetDateTime.parse("2026-04-20T14:00:00+04:00"),
                OffsetDateTime.parse("2026-04-20T14:00:00+04:00")
        );

        when(user.getId()).thenReturn(userId);
        mockAuthentication(user);

        when(examSubmissionRepository.findByUserIdAndTaskId(userId, taskId))
                .thenReturn(Optional.of(examSubmission));
        when(examSubmissionMapper.toResponse(examSubmission)).thenReturn(response);

        ExamSubmissionResponse result = examSubmissionService.getMyByTask(taskId, authentication);

        assertEquals(response, result);
    }

    @Test
    @DisplayName("getAllByTask -> returns all exam submissions by task id")
    void getAllByTask_ShouldReturnAllExamSubmissionsByTask() {
        UUID taskId = UUID.randomUUID();

        ExamTask task = mock(ExamTask.class);
        ExamSubmission first = new ExamSubmission();
        ExamSubmission second = new ExamSubmission();

        List<ExamSubmissionResponse> responses = List.of(
                new ExamSubmissionResponse(
                        UUID.randomUUID(),
                        null,
                        ExamStatus.PENDING_REVIEW,
                        Map.of("answer", "First answer"),
                        UUID.randomUUID(),
                        taskId,
                        OffsetDateTime.parse("2026-04-20T14:00:00+04:00"),
                        OffsetDateTime.parse("2026-04-20T14:00:00+04:00")
                ),
                new ExamSubmissionResponse(
                        UUID.randomUUID(),
                        "Failed",
                        ExamStatus.FAILED,
                        Map.of("answer", "Second answer"),
                        UUID.randomUUID(),
                        taskId,
                        OffsetDateTime.parse("2026-04-21T14:00:00+04:00"),
                        OffsetDateTime.parse("2026-04-21T15:00:00+04:00")
                )
        );

        when(examTaskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(examSubmissionRepository.findAllByTaskId(taskId)).thenReturn(List.of(first, second));
        when(examSubmissionMapper.toResponseList(List.of(first, second))).thenReturn(responses);

        List<ExamSubmissionResponse> result = examSubmissionService.getAllByTask(taskId);

        assertEquals(responses, result);
    }

    @Test
    @DisplayName("update -> updates exam submission status and trims note")
    void update_ShouldUpdateExamSubmissionStatusAndTrimNote() {
        UUID submissionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        ExamSubmission examSubmission = new ExamSubmission();

        UpdateExamSubmissionRequest request = new UpdateExamSubmissionRequest(
                ExamStatus.PASSED,
                "  Good work  "
        );

        ExamSubmissionResponse response = new ExamSubmissionResponse(
                submissionId,
                "Good work",
                ExamStatus.PASSED,
                Map.of("answer", "Student answer"),
                userId,
                taskId,
                OffsetDateTime.parse("2026-04-20T14:00:00+04:00"),
                OffsetDateTime.parse("2026-04-20T15:00:00+04:00")
        );

        when(examSubmissionRepository.findById(submissionId)).thenReturn(Optional.of(examSubmission));
        when(examSubmissionRepository.saveAndFlush(any(ExamSubmission.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(examSubmissionMapper.toResponse(any(ExamSubmission.class))).thenReturn(response);

        ExamSubmissionResponse result = examSubmissionService.update(submissionId, request);

        assertEquals(response, result);

        ArgumentCaptor<ExamSubmission> captor = ArgumentCaptor.forClass(ExamSubmission.class);
        verify(examSubmissionRepository).saveAndFlush(captor.capture());

        ExamSubmission savedSubmission = captor.getValue();

        assertEquals(ExamStatus.PASSED, savedSubmission.getStatus());
        assertEquals("Good work", savedSubmission.getNote());
    }

    @Test
    @DisplayName("update -> converts blank note to null")
    void update_ShouldConvertBlankNoteToNull() {
        UUID submissionId = UUID.randomUUID();

        ExamSubmission examSubmission = new ExamSubmission();

        UpdateExamSubmissionRequest request = new UpdateExamSubmissionRequest(
                ExamStatus.FAILED,
                "   "
        );

        ExamSubmissionResponse response = new ExamSubmissionResponse(
                submissionId,
                null,
                ExamStatus.FAILED,
                Map.of("answer", "Student answer"),
                UUID.randomUUID(),
                UUID.randomUUID(),
                OffsetDateTime.parse("2026-04-20T14:00:00+04:00"),
                OffsetDateTime.parse("2026-04-20T15:00:00+04:00")
        );

        when(examSubmissionRepository.findById(submissionId)).thenReturn(Optional.of(examSubmission));
        when(examSubmissionRepository.saveAndFlush(any(ExamSubmission.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(examSubmissionMapper.toResponse(any(ExamSubmission.class))).thenReturn(response);

        ExamSubmissionResponse result = examSubmissionService.update(submissionId, request);

        assertEquals(response, result);

        ArgumentCaptor<ExamSubmission> captor = ArgumentCaptor.forClass(ExamSubmission.class);
        verify(examSubmissionRepository).saveAndFlush(captor.capture());

        assertNull(captor.getValue().getNote());
        assertEquals(ExamStatus.FAILED, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("update -> throws bad request when status is PENDING_REVIEW")
    void update_ShouldThrowBadRequest_WhenStatusIsPendingReview() {
        UUID submissionId = UUID.randomUUID();

        UpdateExamSubmissionRequest request = new UpdateExamSubmissionRequest(
                ExamStatus.PENDING_REVIEW,
                "Invalid review status"
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> examSubmissionService.update(submissionId, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode().value());
        assertEquals("Review status must be PASSED or FAILED", exception.getReason());

        verify(examSubmissionRepository, never()).findById(any());
        verify(examSubmissionRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("deleteMy -> deletes current user's submission and rolls back user process")
    void deleteMy_ShouldDeleteMySubmissionAndRollbackUserProcess() {
        UUID userId = UUID.randomUUID();
        UUID submissionId = UUID.randomUUID();
        UUID contentItemId = UUID.randomUUID();

        User user = mock(User.class);
        ExamTask task = mock(ExamTask.class);
        ExamSection section = mock(ExamSection.class);
        Exam exam = mock(Exam.class);
        ContentItem contentItem = mock(ContentItem.class);

        ExamSubmission examSubmission = new ExamSubmission();
        examSubmission.setUser(user);
        examSubmission.setTask(task);

        UserProcess userProcess = UserProcess.builder()
                .user(user)
                .contentItem(contentItem)
                .currentStep(3)
                .scoreAccumulated(BigDecimal.ZERO)
                .lastAccessedAt(null)
                .build();

        when(user.getId()).thenReturn(userId);
        when(contentItem.getId()).thenReturn(contentItemId);

        mockAuthentication(user);
        when(examSubmissionRepository.findByIdAndUserId(submissionId, userId))
                .thenReturn(Optional.of(examSubmission));
        mockExamTaskContentItemPath(task, section, exam, contentItem);

        when(userProcessRepository.findByUserIdAndContentItemId(userId, contentItemId))
                .thenReturn(Optional.of(userProcess));

        examSubmissionService.deleteMy(submissionId, authentication);

        verify(examSubmissionRepository).delete(examSubmission);

        ArgumentCaptor<UserProcess> captor = ArgumentCaptor.forClass(UserProcess.class);
        verify(userProcessRepository).save(captor.capture());

        UserProcess savedUserProcess = captor.getValue();

        assertEquals(2, savedUserProcess.getCurrentStep());
        assertNotNull(savedUserProcess.getLastAccessedAt());
    }

    @Test
    @DisplayName("delete -> deletes submission and rolls back user's process")
    void delete_ShouldDeleteSubmissionAndRollbackUserProcess() {
        UUID userId = UUID.randomUUID();
        UUID submissionId = UUID.randomUUID();
        UUID contentItemId = UUID.randomUUID();

        User user = mock(User.class);
        ExamTask task = mock(ExamTask.class);
        ExamSection section = mock(ExamSection.class);
        Exam exam = mock(Exam.class);
        ContentItem contentItem = mock(ContentItem.class);

        ExamSubmission examSubmission = new ExamSubmission();
        examSubmission.setUser(user);
        examSubmission.setTask(task);

        UserProcess userProcess = UserProcess.builder()
                .user(user)
                .contentItem(contentItem)
                .currentStep(1)
                .scoreAccumulated(BigDecimal.ZERO)
                .lastAccessedAt(null)
                .build();

        when(user.getId()).thenReturn(userId);
        when(contentItem.getId()).thenReturn(contentItemId);

        when(examSubmissionRepository.findById(submissionId)).thenReturn(Optional.of(examSubmission));
        mockExamTaskContentItemPath(task, section, exam, contentItem);

        when(userProcessRepository.findByUserIdAndContentItemId(userId, contentItemId))
                .thenReturn(Optional.of(userProcess));

        examSubmissionService.delete(submissionId);

        verify(examSubmissionRepository).delete(examSubmission);

        ArgumentCaptor<UserProcess> captor = ArgumentCaptor.forClass(UserProcess.class);
        verify(userProcessRepository).save(captor.capture());

        UserProcess savedUserProcess = captor.getValue();

        assertEquals(0, savedUserProcess.getCurrentStep());
        assertNotNull(savedUserProcess.getLastAccessedAt());
    }

    @Test
    @DisplayName("delete -> does not make current step negative")
    void delete_ShouldNotMakeCurrentStepNegative() {
        UUID userId = UUID.randomUUID();
        UUID submissionId = UUID.randomUUID();
        UUID contentItemId = UUID.randomUUID();

        User user = mock(User.class);
        ExamTask task = mock(ExamTask.class);
        ExamSection section = mock(ExamSection.class);
        Exam exam = mock(Exam.class);
        ContentItem contentItem = mock(ContentItem.class);

        ExamSubmission examSubmission = new ExamSubmission();
        examSubmission.setUser(user);
        examSubmission.setTask(task);

        UserProcess userProcess = UserProcess.builder()
                .user(user)
                .contentItem(contentItem)
                .currentStep(0)
                .scoreAccumulated(BigDecimal.ZERO)
                .lastAccessedAt(null)
                .build();

        when(user.getId()).thenReturn(userId);
        when(contentItem.getId()).thenReturn(contentItemId);

        when(examSubmissionRepository.findById(submissionId)).thenReturn(Optional.of(examSubmission));
        mockExamTaskContentItemPath(task, section, exam, contentItem);

        when(userProcessRepository.findByUserIdAndContentItemId(userId, contentItemId))
                .thenReturn(Optional.of(userProcess));

        examSubmissionService.delete(submissionId);

        ArgumentCaptor<UserProcess> captor = ArgumentCaptor.forClass(UserProcess.class);
        verify(userProcessRepository).save(captor.capture());

        assertEquals(0, captor.getValue().getCurrentStep());
    }
}