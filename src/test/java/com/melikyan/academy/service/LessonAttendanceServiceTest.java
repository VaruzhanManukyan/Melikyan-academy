package com.melikyan.academy.service;

import org.mockito.ArgumentCaptor;
import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.Course;
import com.melikyan.academy.entity.Lesson;
import com.melikyan.academy.entity.ContentItem;
import com.melikyan.academy.entity.LessonAttendance;
import org.mockito.junit.jupiter.MockitoExtension;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.repository.LessonRepository;
import org.springframework.security.core.Authentication;
import com.melikyan.academy.entity.enums.LessonState;
import com.melikyan.academy.entity.enums.AttendanceStatus;
import com.melikyan.academy.mapper.LessonAttendanceMapper;
import com.melikyan.academy.entity.enums.RegistrationStatus;
import org.springframework.web.server.ResponseStatusException;
import com.melikyan.academy.repository.LessonAttendanceRepository;
import com.melikyan.academy.repository.ProductRegistrationRepository;
import com.melikyan.academy.dto.response.lessonAttendance.LessonAttendanceResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.melikyan.academy.dto.request.lessonAttendance.CreateLessonAttendanceRequest;
import com.melikyan.academy.dto.request.lessonAttendance.UpdateLessonAttendanceRequest;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
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
class LessonAttendanceServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private LessonAttendanceMapper lessonAttendanceMapper;

    @Mock
    private LessonAttendanceRepository lessonAttendanceRepository;

    @Mock
    private ProductRegistrationRepository productRegistrationRepository;

    @InjectMocks
    private LessonAttendanceService lessonAttendanceService;

    private UUID userId;
    private UUID attendanceId;
    private UUID lessonId;
    private UUID contentItemId;
    private String email;

    private User user;
    private ContentItem contentItem;
    private Course course;
    private Lesson lesson;
    private LessonAttendance attendance;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        attendanceId = UUID.randomUUID();
        lessonId = UUID.randomUUID();
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
        lesson.setId(lessonId);
        lesson.setCourse(course);
        lesson.setState(LessonState.SCHEDULED);

        attendance = new LessonAttendance();
        attendance.setId(attendanceId);
        attendance.setNote("Initial note");
        attendance.setStatus(AttendanceStatus.ENROLLED);
        attendance.setUser(user);
        attendance.setLesson(lesson);
    }

    @Test
    @DisplayName("create -> saves lesson attendance when user has active access")
    void create_ShouldSaveLessonAttendance_WhenUserHasActiveAccess() {
        CreateLessonAttendanceRequest request = new CreateLessonAttendanceRequest(
                "  Manual attendance  ",
                AttendanceStatus.ENROLLED,
                userId,
                lessonId
        );

        LessonAttendanceResponse response = mock(LessonAttendanceResponse.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(productRegistrationRepository.findUserIdsByContentItemIdAndStatus(
                contentItemId,
                RegistrationStatus.ACTIVE
        )).thenReturn(List.of(userId));
        when(lessonAttendanceRepository.existsByUserIdAndLessonId(userId, lessonId)).thenReturn(false);

        when(lessonAttendanceRepository.saveAndFlush(any(LessonAttendance.class))).thenAnswer(invocation -> {
            LessonAttendance saved = invocation.getArgument(0);
            saved.setId(attendanceId);
            return saved;
        });

        when(lessonAttendanceMapper.toResponse(any(LessonAttendance.class))).thenReturn(response);

        LessonAttendanceResponse result = lessonAttendanceService.create(request);

        assertNotNull(result);
        assertEquals(response, result);

        ArgumentCaptor<LessonAttendance> captor = ArgumentCaptor.forClass(LessonAttendance.class);
        verify(lessonAttendanceRepository).saveAndFlush(captor.capture());

        LessonAttendance savedAttendance = captor.getValue();
        assertEquals("Manual attendance", savedAttendance.getNote());
        assertEquals(AttendanceStatus.ENROLLED, savedAttendance.getStatus());
        assertEquals(user, savedAttendance.getUser());
        assertEquals(lesson, savedAttendance.getLesson());
    }

    @Test
    @DisplayName("create -> throws forbidden when user has no active access")
    void create_ShouldThrowForbidden_WhenUserHasNoActiveAccess() {
        CreateLessonAttendanceRequest request = new CreateLessonAttendanceRequest(
                "Manual attendance",
                AttendanceStatus.ENROLLED,
                userId,
                lessonId
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(productRegistrationRepository.findUserIdsByContentItemIdAndStatus(
                contentItemId,
                RegistrationStatus.ACTIVE
        )).thenReturn(List.of());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> lessonAttendanceService.create(request)
        );

        assertEquals(403, ex.getStatusCode().value());
        assertEquals("User does not have active access to this lesson", ex.getReason());

        verify(lessonAttendanceRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("create -> throws conflict when attendance already exists")
    void create_ShouldThrowConflict_WhenAttendanceAlreadyExists() {
        CreateLessonAttendanceRequest request = new CreateLessonAttendanceRequest(
                "Manual attendance",
                AttendanceStatus.ENROLLED,
                userId,
                lessonId
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(productRegistrationRepository.findUserIdsByContentItemIdAndStatus(
                contentItemId,
                RegistrationStatus.ACTIVE
        )).thenReturn(List.of(userId));
        when(lessonAttendanceRepository.existsByUserIdAndLessonId(userId, lessonId)).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> lessonAttendanceService.create(request)
        );

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("Lesson attendance already exists for this user and lesson", ex.getReason());

        verify(lessonAttendanceRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("checkIn -> creates attended attendance when it does not exist")
    void checkIn_ShouldCreateAttendedAttendance_WhenAttendanceDoesNotExist() {
        LessonAttendanceResponse response = mock(LessonAttendanceResponse.class);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonAttendanceRepository.findByUserIdAndLessonId(userId, lessonId))
                .thenReturn(Optional.empty());

        when(lessonAttendanceRepository.saveAndFlush(any(LessonAttendance.class))).thenAnswer(invocation -> {
            LessonAttendance saved = invocation.getArgument(0);
            saved.setId(attendanceId);
            return saved;
        });

        when(lessonAttendanceMapper.toResponse(any(LessonAttendance.class))).thenReturn(response);

        LessonAttendanceResponse result = lessonAttendanceService.checkIn(lessonId, authentication);

        assertNotNull(result);
        assertEquals(response, result);

        ArgumentCaptor<LessonAttendance> captor = ArgumentCaptor.forClass(LessonAttendance.class);
        verify(lessonAttendanceRepository).saveAndFlush(captor.capture());

        LessonAttendance savedAttendance = captor.getValue();
        assertEquals(AttendanceStatus.ATTENDED, savedAttendance.getStatus());
        assertEquals("Checked in by student", savedAttendance.getNote());
        assertEquals(user, savedAttendance.getUser());
        assertEquals(lesson, savedAttendance.getLesson());
    }

    @Test
    @DisplayName("checkIn -> updates existing attendance to attended")
    void checkIn_ShouldUpdateExistingAttendanceToAttended() {
        LessonAttendanceResponse response = mock(LessonAttendanceResponse.class);

        attendance.setStatus(AttendanceStatus.ENROLLED);
        attendance.setNote("Old note");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonAttendanceRepository.findByUserIdAndLessonId(userId, lessonId))
                .thenReturn(Optional.of(attendance));
        when(lessonAttendanceRepository.saveAndFlush(attendance)).thenReturn(attendance);
        when(lessonAttendanceMapper.toResponse(attendance)).thenReturn(response);

        LessonAttendanceResponse result = lessonAttendanceService.checkIn(lessonId, authentication);

        assertNotNull(result);
        assertEquals(response, result);
        assertEquals(AttendanceStatus.ATTENDED, attendance.getStatus());
        assertEquals("Checked in by student", attendance.getNote());
    }

    @Test
    @DisplayName("generateMissedForLesson -> marks non-attended active users as missed")
    void generateMissedForLesson_ShouldMarkNonAttendedUsersAsMissed() {
        lesson.setState(LessonState.COMPLETED);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(productRegistrationRepository.findUserIdsByContentItemIdAndStatus(
                contentItemId,
                RegistrationStatus.ACTIVE
        )).thenReturn(List.of(userId));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(lessonAttendanceRepository.findByUserIdAndLessonId(userId, lessonId))
                .thenReturn(Optional.of(attendance));

        lessonAttendanceService.generateMissedForLesson(lessonId);

        assertEquals(AttendanceStatus.MISSED, attendance.getStatus());
        assertEquals("Automatically marked as missed", attendance.getNote());

        verify(lessonAttendanceRepository).save(attendance);
        verify(lessonAttendanceRepository).flush();
    }

    @Test
    @DisplayName("generateMissedForLesson -> creates missed attendance when attendance does not exist")
    void generateMissedForLesson_ShouldCreateMissedAttendance_WhenAttendanceDoesNotExist() {
        lesson.setState(LessonState.COMPLETED);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(productRegistrationRepository.findUserIdsByContentItemIdAndStatus(
                contentItemId,
                RegistrationStatus.ACTIVE
        )).thenReturn(List.of(userId));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(lessonAttendanceRepository.findByUserIdAndLessonId(userId, lessonId))
                .thenReturn(Optional.empty());

        lessonAttendanceService.generateMissedForLesson(lessonId);

        ArgumentCaptor<LessonAttendance> captor = ArgumentCaptor.forClass(LessonAttendance.class);
        verify(lessonAttendanceRepository).save(captor.capture());

        LessonAttendance savedAttendance = captor.getValue();
        assertEquals(AttendanceStatus.MISSED, savedAttendance.getStatus());
        assertEquals("Automatically marked as missed", savedAttendance.getNote());
        assertEquals(user, savedAttendance.getUser());
        assertEquals(lesson, savedAttendance.getLesson());

        verify(lessonAttendanceRepository).flush();
    }

    @Test
    @DisplayName("generateMissedForLesson -> does not override attended attendance")
    void generateMissedForLesson_ShouldNotOverrideAttendedAttendance() {
        lesson.setState(LessonState.COMPLETED);
        attendance.setStatus(AttendanceStatus.ATTENDED);
        attendance.setNote("Checked in by student");

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(productRegistrationRepository.findUserIdsByContentItemIdAndStatus(
                contentItemId,
                RegistrationStatus.ACTIVE
        )).thenReturn(List.of(userId));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(lessonAttendanceRepository.findByUserIdAndLessonId(userId, lessonId))
                .thenReturn(Optional.of(attendance));

        lessonAttendanceService.generateMissedForLesson(lessonId);

        assertEquals(AttendanceStatus.ATTENDED, attendance.getStatus());
        assertEquals("Checked in by student", attendance.getNote());

        verify(lessonAttendanceRepository, never()).save(any());
        verify(lessonAttendanceRepository).flush();
    }

    @Test
    @DisplayName("generateMissedForLesson -> throws bad request when lesson is not completed")
    void generateMissedForLesson_ShouldThrowBadRequest_WhenLessonIsNotCompleted() {
        lesson.setState(LessonState.SCHEDULED);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> lessonAttendanceService.generateMissedForLesson(lessonId)
        );

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Missed attendance can be generated only for completed lesson", ex.getReason());

        verify(lessonAttendanceRepository, never()).save(any());
        verify(lessonAttendanceRepository, never()).flush();
    }

    @Test
    @DisplayName("generateEnrolledForLesson -> creates enrolled attendance for active users")
    void generateEnrolledForLesson_ShouldCreateEnrolledAttendanceForActiveUsers() {
        lesson.setState(LessonState.SCHEDULED);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(productRegistrationRepository.findUserIdsByContentItemIdAndStatus(
                contentItemId,
                RegistrationStatus.ACTIVE
        )).thenReturn(List.of(userId));
        when(lessonAttendanceRepository.existsByUserIdAndLessonId(userId, lessonId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        lessonAttendanceService.generateEnrolledForLesson(lessonId);

        ArgumentCaptor<LessonAttendance> captor = ArgumentCaptor.forClass(LessonAttendance.class);
        verify(lessonAttendanceRepository).save(captor.capture());

        LessonAttendance savedAttendance = captor.getValue();
        assertEquals(AttendanceStatus.ENROLLED, savedAttendance.getStatus());
        assertEquals("Automatically marked as enrolled", savedAttendance.getNote());
        assertEquals(user, savedAttendance.getUser());
        assertEquals(lesson, savedAttendance.getLesson());

        verify(lessonAttendanceRepository).flush();
    }

    @Test
    @DisplayName("generateEnrolledForLesson -> skips existing attendance")
    void generateEnrolledForLesson_ShouldSkipExistingAttendance() {
        lesson.setState(LessonState.SCHEDULED);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(productRegistrationRepository.findUserIdsByContentItemIdAndStatus(
                contentItemId,
                RegistrationStatus.ACTIVE
        )).thenReturn(List.of(userId));
        when(lessonAttendanceRepository.existsByUserIdAndLessonId(userId, lessonId)).thenReturn(true);

        lessonAttendanceService.generateEnrolledForLesson(lessonId);

        verify(userRepository, never()).findById(userId);
        verify(lessonAttendanceRepository, never()).save(any());
        verify(lessonAttendanceRepository).flush();
    }

    @Test
    @DisplayName("generateEnrolledForLesson -> throws bad request when lesson is completed")
    void generateEnrolledForLesson_ShouldThrowBadRequest_WhenLessonIsCompleted() {
        lesson.setState(LessonState.COMPLETED);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> lessonAttendanceService.generateEnrolledForLesson(lessonId)
        );

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Enrolled attendance cannot be generated for completed lesson", ex.getReason());

        verify(lessonAttendanceRepository, never()).save(any());
        verify(lessonAttendanceRepository, never()).flush();
    }

    @Test
    @DisplayName("getById -> returns mapped response")
    void getById_ShouldReturnMappedResponse() {
        LessonAttendanceResponse response = mock(LessonAttendanceResponse.class);

        when(lessonAttendanceRepository.findById(attendanceId)).thenReturn(Optional.of(attendance));
        when(lessonAttendanceMapper.toResponse(attendance)).thenReturn(response);

        LessonAttendanceResponse result = lessonAttendanceService.getById(attendanceId);

        assertNotNull(result);
        assertEquals(response, result);
    }

    @Test
    @DisplayName("getMyById -> returns mapped response")
    void getMyById_ShouldReturnMappedResponse() {
        LessonAttendanceResponse response = mock(LessonAttendanceResponse.class);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(lessonAttendanceRepository.findByIdAndUserId(attendanceId, userId))
                .thenReturn(Optional.of(attendance));
        when(lessonAttendanceMapper.toResponse(attendance)).thenReturn(response);

        LessonAttendanceResponse result = lessonAttendanceService.getMyById(attendanceId, authentication);

        assertNotNull(result);
        assertEquals(response, result);
    }

    @Test
    @DisplayName("getMyAll -> returns mapped responses")
    void getMyAll_ShouldReturnMappedResponses() {
        List<LessonAttendance> attendances = List.of(attendance);
        List<LessonAttendanceResponse> responses = List.of(mock(LessonAttendanceResponse.class));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(lessonAttendanceRepository.findAllByUserId(userId)).thenReturn(attendances);
        when(lessonAttendanceMapper.toResponseList(attendances)).thenReturn(responses);

        List<LessonAttendanceResponse> result = lessonAttendanceService.getMyAll(authentication);

        assertNotNull(result);
        assertEquals(responses, result);
    }

    @Test
    @DisplayName("getMyByLesson -> returns mapped response")
    void getMyByLesson_ShouldReturnMappedResponse() {
        LessonAttendanceResponse response = mock(LessonAttendanceResponse.class);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(lessonAttendanceRepository.findByUserIdAndLessonId(userId, lessonId))
                .thenReturn(Optional.of(attendance));
        when(lessonAttendanceMapper.toResponse(attendance)).thenReturn(response);

        LessonAttendanceResponse result = lessonAttendanceService.getMyByLesson(lessonId, authentication);

        assertNotNull(result);
        assertEquals(response, result);
    }

    @Test
    @DisplayName("getAllByLesson -> returns mapped responses")
    void getAllByLesson_ShouldReturnMappedResponses() {
        List<LessonAttendance> attendances = List.of(attendance);
        List<LessonAttendanceResponse> responses = List.of(mock(LessonAttendanceResponse.class));

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonAttendanceRepository.findAllByLessonId(lessonId)).thenReturn(attendances);
        when(lessonAttendanceMapper.toResponseList(attendances)).thenReturn(responses);

        List<LessonAttendanceResponse> result = lessonAttendanceService.getAllByLesson(lessonId);

        assertNotNull(result);
        assertEquals(responses, result);
    }

    @Test
    @DisplayName("update -> updates status and note")
    void update_ShouldUpdateStatusAndNote() {
        UpdateLessonAttendanceRequest request = new UpdateLessonAttendanceRequest(
                "  Student attended successfully  ",
                AttendanceStatus.ATTENDED
        );

        LessonAttendanceResponse response = mock(LessonAttendanceResponse.class);

        when(lessonAttendanceRepository.findById(attendanceId)).thenReturn(Optional.of(attendance));
        when(lessonAttendanceRepository.saveAndFlush(attendance)).thenReturn(attendance);
        when(lessonAttendanceMapper.toResponse(attendance)).thenReturn(response);

        LessonAttendanceResponse result = lessonAttendanceService.update(attendanceId, request);

        assertNotNull(result);
        assertEquals(response, result);
        assertEquals(AttendanceStatus.ATTENDED, attendance.getStatus());
        assertEquals("Student attended successfully", attendance.getNote());
    }

    @Test
    @DisplayName("update -> converts blank note to null")
    void update_ShouldConvertBlankNoteToNull() {
        UpdateLessonAttendanceRequest request = new UpdateLessonAttendanceRequest(
                "   ",
                AttendanceStatus.MISSED
        );

        LessonAttendanceResponse response = mock(LessonAttendanceResponse.class);

        when(lessonAttendanceRepository.findById(attendanceId)).thenReturn(Optional.of(attendance));
        when(lessonAttendanceRepository.saveAndFlush(attendance)).thenReturn(attendance);
        when(lessonAttendanceMapper.toResponse(attendance)).thenReturn(response);

        LessonAttendanceResponse result = lessonAttendanceService.update(attendanceId, request);

        assertNotNull(result);
        assertEquals(response, result);
        assertEquals(AttendanceStatus.MISSED, attendance.getStatus());
        assertEquals(null, attendance.getNote());
    }

    @Test
    @DisplayName("delete -> deletes attendance")
    void delete_ShouldDeleteAttendance() {
        when(lessonAttendanceRepository.findById(attendanceId)).thenReturn(Optional.of(attendance));

        lessonAttendanceService.delete(attendanceId);

        verify(lessonAttendanceRepository).delete(attendance);
    }
}