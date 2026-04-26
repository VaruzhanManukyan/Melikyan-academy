package com.melikyan.academy.service;

import org.mockito.ArgumentCaptor;
import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.Course;
import com.melikyan.academy.entity.Professor;
import com.melikyan.academy.entity.enums.Role;
import org.mockito.junit.jupiter.MockitoExtension;
import com.melikyan.academy.mapper.ProfessorMapper;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.repository.CourseRepository;
import com.melikyan.academy.repository.ProfessorRepository;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.melikyan.academy.dto.response.professor.ProfessorUserData;
import com.melikyan.academy.dto.response.professor.ProfessorResponse;
import com.melikyan.academy.dto.request.professor.AssignProfessorRequest;
import com.melikyan.academy.dto.response.professor.ProfessorUserResponse;
import com.melikyan.academy.dto.request.professor.CreateProfessorUserRequest;
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
class ProfessorServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ProfessorMapper professorMapper;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ProfessorRepository professorRepository;

    @InjectMocks
    private ProfessorService professorService;

    private UUID userId;
    private UUID adminId;
    private UUID studentId;
    private UUID courseId;
    private UUID professorId;

    private User professorUser;
    private User adminUser;
    private User studentUser;
    private Course course;
    private Professor professorAssignment;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        adminId = UUID.randomUUID();
        studentId = UUID.randomUUID();
        courseId = UUID.randomUUID();
        professorId = UUID.randomUUID();

        professorUser = new User();
        professorUser.setId(userId);
        professorUser.setFirstName("Aram");
        professorUser.setLastName("Petrosyan");
        professorUser.setEmail("aram.professor@test.com");
        professorUser.setRole(Role.PROFESSOR);

        adminUser = new User();
        adminUser.setId(adminId);
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmail("admin@test.com");
        adminUser.setRole(Role.ADMIN);

        studentUser = new User();
        studentUser.setId(studentId);
        studentUser.setFirstName("Student");
        studentUser.setLastName("User");
        studentUser.setEmail("student@test.com");
        studentUser.setRole(Role.STUDENT);

        course = new Course();
        course.setId(courseId);

        professorAssignment = new Professor();
        professorAssignment.setId(professorId);
        professorAssignment.setUser(professorUser);
        professorAssignment.setCourse(course);
    }

    @Test
    @DisplayName("create -> creates professor user")
    void create_ShouldCreateProfessorUser() {
        CreateProfessorUserRequest request = new CreateProfessorUserRequest(
                "  Aram  ",
                "  Petrosyan  ",
                "  ARAM.PROFESSOR@TEST.COM  ",
                "Password123!",
                "Password123!"
        );

        ProfessorUserResponse response = mock(ProfessorUserResponse.class);

        when(userRepository.existsByEmail("aram.professor@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded-password");

        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(userId);
            return saved;
        });

        when(professorMapper.toRegisterResponse(any(User.class))).thenReturn(response);

        ProfessorUserResponse result = professorService.create(request);

        assertNotNull(result);
        assertEquals(response, result);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(captor.capture());

        User savedUser = captor.getValue();
        assertEquals("Aram", savedUser.getFirstName());
        assertEquals("Petrosyan", savedUser.getLastName());
        assertEquals("aram.professor@test.com", savedUser.getEmail());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals(Role.PROFESSOR, savedUser.getRole());
    }

    @Test
    @DisplayName("create -> throws bad request when passwords do not match")
    void create_ShouldThrowBadRequest_WhenPasswordsDoNotMatch() {
        CreateProfessorUserRequest request = new CreateProfessorUserRequest(
                "Aram",
                "Petrosyan",
                "aram.professor@test.com",
                "Password123!",
                "DifferentPassword123!"
        );

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> professorService.create(request)
        );

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Passwords do not match", ex.getReason());

        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("create -> throws conflict when email already exists")
    void create_ShouldThrowConflict_WhenEmailAlreadyExists() {
        CreateProfessorUserRequest request = new CreateProfessorUserRequest(
                "Aram",
                "Petrosyan",
                "ARAM.PROFESSOR@TEST.COM",
                "Password123!",
                "Password123!"
        );

        when(userRepository.existsByEmail("aram.professor@test.com")).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> professorService.create(request)
        );

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("Email is already in use", ex.getReason());

        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("create -> throws conflict when database unique constraint fails")
    void create_ShouldThrowConflict_WhenDatabaseUniqueConstraintFails() {
        CreateProfessorUserRequest request = new CreateProfessorUserRequest(
                "Aram",
                "Petrosyan",
                "aram.professor@test.com",
                "Password123!",
                "Password123!"
        );

        when(userRepository.existsByEmail("aram.professor@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded-password");
        when(userRepository.saveAndFlush(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate email"));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> professorService.create(request)
        );

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("Email is already in use", ex.getReason());
    }

    @Test
    @DisplayName("assign -> assigns professor user to course")
    void assign_ShouldAssignProfessorUserToCourse() {
        AssignProfessorRequest request = new AssignProfessorRequest(userId, courseId);
        ProfessorResponse response = mock(ProfessorResponse.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(professorUser));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(professorRepository.existsByUserIdAndCourseId(userId, courseId)).thenReturn(false);

        when(professorRepository.saveAndFlush(any(Professor.class))).thenAnswer(invocation -> {
            Professor saved = invocation.getArgument(0);
            saved.setId(professorId);
            return saved;
        });

        when(professorMapper.toResponse(any(Professor.class))).thenReturn(response);

        ProfessorResponse result = professorService.assign(request);

        assertNotNull(result);
        assertEquals(response, result);

        ArgumentCaptor<Professor> captor = ArgumentCaptor.forClass(Professor.class);
        verify(professorRepository).saveAndFlush(captor.capture());

        Professor savedProfessor = captor.getValue();
        assertEquals(professorUser, savedProfessor.getUser());
        assertEquals(course, savedProfessor.getCourse());
    }

    @Test
    @DisplayName("assign -> allows admin user to be assigned to course")
    void assign_ShouldAllowAdminUserToBeAssignedToCourse() {
        AssignProfessorRequest request = new AssignProfessorRequest(adminId, courseId);
        ProfessorResponse response = mock(ProfessorResponse.class);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(professorRepository.existsByUserIdAndCourseId(adminId, courseId)).thenReturn(false);

        when(professorRepository.saveAndFlush(any(Professor.class))).thenAnswer(invocation -> {
            Professor saved = invocation.getArgument(0);
            saved.setId(professorId);
            return saved;
        });

        when(professorMapper.toResponse(any(Professor.class))).thenReturn(response);

        ProfessorResponse result = professorService.assign(request);

        assertNotNull(result);
        assertEquals(response, result);

        ArgumentCaptor<Professor> captor = ArgumentCaptor.forClass(Professor.class);
        verify(professorRepository).saveAndFlush(captor.capture());

        Professor savedProfessor = captor.getValue();
        assertEquals(adminUser, savedProfessor.getUser());
        assertEquals(course, savedProfessor.getCourse());
    }

    @Test
    @DisplayName("assign -> throws bad request when user is student")
    void assign_ShouldThrowBadRequest_WhenUserIsStudent() {
        AssignProfessorRequest request = new AssignProfessorRequest(studentId, courseId);

        when(userRepository.findById(studentId)).thenReturn(Optional.of(studentUser));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> professorService.assign(request)
        );

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Only ADMIN or PROFESSOR user can be assigned to course", ex.getReason());

        verify(professorRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("assign -> throws conflict when professor already assigned to course")
    void assign_ShouldThrowConflict_WhenProfessorAlreadyAssignedToCourse() {
        AssignProfessorRequest request = new AssignProfessorRequest(userId, courseId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(professorUser));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(professorRepository.existsByUserIdAndCourseId(userId, courseId)).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> professorService.assign(request)
        );

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("Professor already assigned to this course", ex.getReason());

        verify(professorRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("assign -> throws conflict when database unique constraint fails")
    void assign_ShouldThrowConflict_WhenDatabaseUniqueConstraintFails() {
        AssignProfessorRequest request = new AssignProfessorRequest(userId, courseId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(professorUser));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(professorRepository.existsByUserIdAndCourseId(userId, courseId)).thenReturn(false);
        when(professorRepository.saveAndFlush(any(Professor.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate professor assignment"));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> professorService.assign(request)
        );

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("Professor already assigned to this course", ex.getReason());
    }

    @Test
    @DisplayName("getById -> returns mapped professor assignment")
    void getById_ShouldReturnMappedProfessorAssignment() {
        ProfessorResponse response = mock(ProfessorResponse.class);

        when(professorRepository.findById(professorId)).thenReturn(Optional.of(professorAssignment));
        when(professorMapper.toResponse(professorAssignment)).thenReturn(response);

        ProfessorResponse result = professorService.getById(professorId);

        assertNotNull(result);
        assertEquals(response, result);
    }

    @Test
    @DisplayName("getAllByCourse -> returns mapped professor assignments")
    void getAllByCourse_ShouldReturnMappedProfessorAssignments() {
        List<Professor> professors = List.of(professorAssignment);
        List<ProfessorResponse> responses = List.of(mock(ProfessorResponse.class));

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(professorRepository.findAllByCourseId(courseId)).thenReturn(professors);
        when(professorMapper.toResponseList(professors)).thenReturn(responses);

        List<ProfessorResponse> result = professorService.getAllByCourse(courseId);

        assertNotNull(result);
        assertEquals(responses, result);
    }

    @Test
    @DisplayName("getAllByUser -> returns mapped professor assignments")
    void getAllByUser_ShouldReturnMappedProfessorAssignments() {
        List<Professor> professors = List.of(professorAssignment);
        List<ProfessorResponse> responses = List.of(mock(ProfessorResponse.class));

        when(userRepository.findById(userId)).thenReturn(Optional.of(professorUser));
        when(professorRepository.findAllByUserId(userId)).thenReturn(professors);
        when(professorMapper.toResponseList(professors)).thenReturn(responses);

        List<ProfessorResponse> result = professorService.getAllByUser(userId);

        assertNotNull(result);
        assertEquals(responses, result);
    }

    @Test
    @DisplayName("getAllProfessorUsers -> returns professor and admin users")
    void getAllProfessorUsers_ShouldReturnProfessorAndAdminUsers() {
        List<User> users = List.of(professorUser, adminUser);
        List<ProfessorUserData> responses = List.of(
                mock(ProfessorUserData.class),
                mock(ProfessorUserData.class)
        );

        when(userRepository.findAllByRoleIn(List.of(Role.PROFESSOR, Role.ADMIN))).thenReturn(users);
        when(professorMapper.toUserDataList(users)).thenReturn(responses);

        List<ProfessorUserData> result = professorService.getAllProfessorUsers();

        assertNotNull(result);
        assertEquals(responses, result);
    }

    @Test
    @DisplayName("delete -> removes professor from course")
    void delete_ShouldRemoveProfessorFromCourse() {
        when(professorRepository.findByUserIdAndCourseId(userId, courseId))
                .thenReturn(Optional.of(professorAssignment));

        professorService.delete(userId, courseId);

        verify(professorRepository).delete(professorAssignment);
        verify(professorRepository).flush();
    }

    @Test
    @DisplayName("delete -> throws not found when assignment does not exist")
    void delete_ShouldThrowNotFound_WhenAssignmentDoesNotExist() {
        when(professorRepository.findByUserIdAndCourseId(userId, courseId))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> professorService.delete(userId, courseId)
        );

        assertEquals(404, ex.getStatusCode().value());
        assertEquals("Professor assignment not found", ex.getReason());

        verify(professorRepository, never()).delete(any());
        verify(professorRepository, never()).flush();
    }
}