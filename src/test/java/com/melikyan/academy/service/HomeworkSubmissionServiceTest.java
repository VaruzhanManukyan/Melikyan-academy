package com.melikyan.academy.service;

import org.mockito.ArgumentCaptor;
import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.Course;
import com.melikyan.academy.entity.Lesson;
import com.melikyan.academy.entity.Homework;
import com.melikyan.academy.entity.ContentItem;
import com.melikyan.academy.entity.UserProcess;
import com.melikyan.academy.entity.HomeworkTask;
import org.mockito.junit.jupiter.MockitoExtension;
import com.melikyan.academy.entity.HomeworkSubmission;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.entity.enums.HomeworkStatus;
import org.springframework.security.core.Authentication;
import com.melikyan.academy.mapper.HomeworkSubmissionMapper;
import com.melikyan.academy.repository.UserProcessRepository;
import org.springframework.web.server.ResponseStatusException;
import com.melikyan.academy.repository.HomeworkTaskRepository;
import com.melikyan.academy.repository.HomeworkSubmissionRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.melikyan.academy.dto.response.homeworkSubmission.HomeworkSubmissionResponse;
import com.melikyan.academy.dto.request.homeworkSubmission.CreateHomeworkSubmissionRequest;
import com.melikyan.academy.dto.request.homeworkSubmission.UpdateHomeworkSubmissionRequest;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class HomeworkSubmissionServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProcessRepository userProcessRepository;

    @Mock
    private HomeworkTaskRepository homeworkTaskRepository;

    @Mock
    private HomeworkSubmissionRepository homeworkSubmissionRepository;

    @Mock
    private HomeworkSubmissionMapper homeworkSubmissionMapper;

    @InjectMocks
    private HomeworkSubmissionService homeworkSubmissionService;

    private UUID userId;
    private UUID submissionId;
    private UUID taskId;
    private UUID contentItemId;
    private String email;

    private User user;
    private ContentItem contentItem;
    private Course course;
    private Lesson lesson;
    private Homework homework;
    private HomeworkTask task;
    private HomeworkSubmission submission;
    private UserProcess userProcess;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        submissionId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        contentItemId = UUID.randomUUID();
        email = "student@test.com";

        authentication = new UsernamePasswordAuthenticationToken(email, null);

        user = new User();
        user.setId(userId);
        user.setEmail(email);

        contentItem = new ContentItem();
        contentItem.setId(contentItemId);

        course = new Course();
        course.setId(UUID.randomUUID());
        course.setContentItem(contentItem);

        lesson = new Lesson();
        lesson.setId(UUID.randomUUID());
        lesson.setCourse(course);

        homework = new Homework();
        homework.setId(UUID.randomUUID());
        homework.setLesson(lesson);

        task = new HomeworkTask();
        task.setId(taskId);
        task.setHomework(homework);

        submission = new HomeworkSubmission();
        submission.setId(submissionId);
        submission.setStatus(HomeworkStatus.PENDING_REVIEW);
        submission.setAnswerPayload(Map.of("answer", "My answer"));
        submission.setUser(user);
        submission.setTask(task);

        userProcess = new UserProcess();
        userProcess.setId(UUID.randomUUID());
        userProcess.setUser(user);
        userProcess.setContentItem(contentItem);
        userProcess.setCurrentStep(1);
    }

    @Test
    @DisplayName("create -> saves pending review submission and advances user process")
    void create_ShouldSavePendingReviewSubmissionAndAdvanceUserProcess() {
        CreateHomeworkSubmissionRequest request = mock(CreateHomeworkSubmissionRequest.class);
        HomeworkSubmissionResponse response = mock(HomeworkSubmissionResponse.class);

        when(request.taskId()).thenReturn(taskId);
        when(request.answerPayload()).thenReturn(Map.of("answer", "My answer"));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(homeworkTaskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(homeworkSubmissionRepository.existsByUserIdAndTaskId(userId, taskId)).thenReturn(false);
        when(userProcessRepository.findByUserIdAndContentItemId(userId, contentItemId))
                .thenReturn(Optional.of(userProcess));

        when(homeworkSubmissionRepository.saveAndFlush(any(HomeworkSubmission.class))).thenAnswer(invocation -> {
            HomeworkSubmission saved = invocation.getArgument(0);
            saved.setId(submissionId);
            return saved;
        });

        when(homeworkSubmissionMapper.toResponse(any(HomeworkSubmission.class))).thenReturn(response);

        HomeworkSubmissionResponse result = homeworkSubmissionService.create(request, authentication);

        assertNotNull(result);
        assertEquals(response, result);
        assertEquals(2, userProcess.getCurrentStep());

        ArgumentCaptor<HomeworkSubmission> captor = ArgumentCaptor.forClass(HomeworkSubmission.class);
        verify(homeworkSubmissionRepository).saveAndFlush(captor.capture());

        HomeworkSubmission savedSubmission = captor.getValue();
        assertEquals(HomeworkStatus.PENDING_REVIEW, savedSubmission.getStatus());
        assertEquals(user, savedSubmission.getUser());
        assertEquals(task, savedSubmission.getTask());
        assertEquals(Map.of("answer", "My answer"), savedSubmission.getAnswerPayload());

        verify(userProcessRepository).save(userProcess);
    }

    @Test
    @DisplayName("create -> creates user process when it does not exist")
    void create_ShouldCreateUserProcess_WhenUserProcessDoesNotExist() {
        CreateHomeworkSubmissionRequest request = mock(CreateHomeworkSubmissionRequest.class);
        HomeworkSubmissionResponse response = mock(HomeworkSubmissionResponse.class);

        when(request.taskId()).thenReturn(taskId);
        when(request.answerPayload()).thenReturn(Map.of("answer", "My answer"));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(homeworkTaskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(homeworkSubmissionRepository.existsByUserIdAndTaskId(userId, taskId)).thenReturn(false);
        when(userProcessRepository.findByUserIdAndContentItemId(userId, contentItemId))
                .thenReturn(Optional.empty());

        when(homeworkSubmissionRepository.saveAndFlush(any(HomeworkSubmission.class))).thenAnswer(invocation -> {
            HomeworkSubmission saved = invocation.getArgument(0);
            saved.setId(submissionId);
            return saved;
        });

        when(homeworkSubmissionMapper.toResponse(any(HomeworkSubmission.class))).thenReturn(response);

        HomeworkSubmissionResponse result = homeworkSubmissionService.create(request, authentication);

        assertNotNull(result);
        assertEquals(response, result);

        ArgumentCaptor<UserProcess> captor = ArgumentCaptor.forClass(UserProcess.class);
        verify(userProcessRepository).save(captor.capture());

        UserProcess savedUserProcess = captor.getValue();
        assertEquals(user, savedUserProcess.getUser());
        assertEquals(contentItem, savedUserProcess.getContentItem());
        assertEquals(1, savedUserProcess.getCurrentStep());
    }

    @Test
    @DisplayName("create -> throws conflict when submission already exists")
    void create_ShouldThrowConflict_WhenSubmissionAlreadyExists() {
        CreateHomeworkSubmissionRequest request = mock(CreateHomeworkSubmissionRequest.class);

        when(request.taskId()).thenReturn(taskId);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(homeworkTaskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(homeworkSubmissionRepository.existsByUserIdAndTaskId(userId, taskId)).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> homeworkSubmissionService.create(request, authentication)
        );

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("Homework submission already exists for this task", ex.getReason());

        verify(homeworkSubmissionRepository, never()).saveAndFlush(any());
        verify(userProcessRepository, never()).save(any());
    }

    @Test
    @DisplayName("getById -> returns mapped response")
    void getById_ShouldReturnMappedResponse() {
        HomeworkSubmissionResponse response = mock(HomeworkSubmissionResponse.class);

        when(homeworkSubmissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(homeworkSubmissionMapper.toResponse(submission)).thenReturn(response);

        HomeworkSubmissionResponse result = homeworkSubmissionService.getById(submissionId);

        assertNotNull(result);
        assertEquals(response, result);
    }

    @Test
    @DisplayName("getMyById -> returns mapped response")
    void getMyById_ShouldReturnMappedResponse() {
        HomeworkSubmissionResponse response = mock(HomeworkSubmissionResponse.class);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(homeworkSubmissionRepository.findByIdAndUserId(submissionId, userId))
                .thenReturn(Optional.of(submission));
        when(homeworkSubmissionMapper.toResponse(submission)).thenReturn(response);

        HomeworkSubmissionResponse result = homeworkSubmissionService.getMyById(submissionId, authentication);

        assertNotNull(result);
        assertEquals(response, result);
    }

    @Test
    @DisplayName("getMyAll -> returns mapped responses")
    void getMyAll_ShouldReturnMappedResponses() {
        List<HomeworkSubmission> submissions = List.of(submission);
        List<HomeworkSubmissionResponse> responses = List.of(mock(HomeworkSubmissionResponse.class));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(homeworkSubmissionRepository.findAllByUserId(userId)).thenReturn(submissions);
        when(homeworkSubmissionMapper.toResponseList(submissions)).thenReturn(responses);

        List<HomeworkSubmissionResponse> result = homeworkSubmissionService.getMyAll(authentication);

        assertNotNull(result);
        assertEquals(responses, result);
    }

    @Test
    @DisplayName("getMyByTask -> returns mapped response")
    void getMyByTask_ShouldReturnMappedResponse() {
        HomeworkSubmissionResponse response = mock(HomeworkSubmissionResponse.class);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(homeworkSubmissionRepository.findByUserIdAndTaskId(userId, taskId))
                .thenReturn(Optional.of(submission));
        when(homeworkSubmissionMapper.toResponse(submission)).thenReturn(response);

        HomeworkSubmissionResponse result = homeworkSubmissionService.getMyByTask(taskId, authentication);

        assertNotNull(result);
        assertEquals(response, result);
    }

    @Test
    @DisplayName("getAllByTask -> returns mapped responses")
    void getAllByTask_ShouldReturnMappedResponses() {
        List<HomeworkSubmission> submissions = List.of(submission);
        List<HomeworkSubmissionResponse> responses = List.of(mock(HomeworkSubmissionResponse.class));

        when(homeworkTaskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(homeworkSubmissionRepository.findAllByTaskId(taskId)).thenReturn(submissions);
        when(homeworkSubmissionMapper.toResponseList(submissions)).thenReturn(responses);

        List<HomeworkSubmissionResponse> result = homeworkSubmissionService.getAllByTask(taskId);

        assertNotNull(result);
        assertEquals(responses, result);
    }

    @Test
    @DisplayName("update -> sets passed status and note")
    void update_ShouldSetPassedStatusAndNote() {
        UpdateHomeworkSubmissionRequest request = mock(UpdateHomeworkSubmissionRequest.class);
        HomeworkSubmissionResponse response = mock(HomeworkSubmissionResponse.class);

        when(request.status()).thenReturn(HomeworkStatus.PASSED);
        when(request.note()).thenReturn("Good work");

        when(homeworkSubmissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(homeworkSubmissionRepository.saveAndFlush(submission)).thenReturn(submission);
        when(homeworkSubmissionMapper.toResponse(submission)).thenReturn(response);

        HomeworkSubmissionResponse result = homeworkSubmissionService.update(submissionId, request);

        assertNotNull(result);
        assertEquals(response, result);
        assertEquals(HomeworkStatus.PASSED, submission.getStatus());
        assertEquals("Good work", submission.getNote());
    }

    @Test
    @DisplayName("update -> throws bad request when status is pending review")
    void update_ShouldThrowBadRequest_WhenStatusIsPendingReview() {
        UpdateHomeworkSubmissionRequest request = mock(UpdateHomeworkSubmissionRequest.class);

        when(request.status()).thenReturn(HomeworkStatus.PENDING_REVIEW);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> homeworkSubmissionService.update(submissionId, request)
        );

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Review status must be PASSED or FAILED", ex.getReason());

        verify(homeworkSubmissionRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("deleteMy -> deletes submission and rolls back user process")
    void deleteMy_ShouldDeleteSubmissionAndRollbackUserProcess() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(homeworkSubmissionRepository.findByIdAndUserId(submissionId, userId))
                .thenReturn(Optional.of(submission));
        when(userProcessRepository.findByUserIdAndContentItemId(userId, contentItemId))
                .thenReturn(Optional.of(userProcess));

        homeworkSubmissionService.deleteMy(submissionId, authentication);

        verify(homeworkSubmissionRepository).delete(submission);
        verify(userProcessRepository).save(userProcess);
        assertEquals(0, userProcess.getCurrentStep());
    }

    @Test
    @DisplayName("delete -> deletes submission and rolls back user process")
    void delete_ShouldDeleteSubmissionAndRollbackUserProcess() {
        when(homeworkSubmissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(userProcessRepository.findByUserIdAndContentItemId(userId, contentItemId))
                .thenReturn(Optional.of(userProcess));

        homeworkSubmissionService.delete(submissionId);

        verify(homeworkSubmissionRepository).delete(submission);
        verify(userProcessRepository).save(userProcess);
        assertEquals(0, userProcess.getCurrentStep());
    }

    @Test
    @DisplayName("delete -> does not make current step negative")
    void delete_ShouldNotMakeCurrentStepNegative() {
        userProcess.setCurrentStep(0);

        when(homeworkSubmissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(userProcessRepository.findByUserIdAndContentItemId(userId, contentItemId))
                .thenReturn(Optional.of(userProcess));

        homeworkSubmissionService.delete(submissionId);

        verify(homeworkSubmissionRepository).delete(submission);
        verify(userProcessRepository).save(userProcess);
        assertEquals(0, userProcess.getCurrentStep());
    }
}