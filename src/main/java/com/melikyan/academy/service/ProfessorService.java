package com.melikyan.academy.service;

import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.Course;
import org.springframework.http.HttpStatus;
import com.melikyan.academy.entity.Professor;
import com.melikyan.academy.entity.enums.Role;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.Locale;

@Service
@Transactional
@RequiredArgsConstructor
public class ProfessorService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfessorMapper professorMapper;
    private final CourseRepository courseRepository;
    private final ProfessorRepository professorRepository;

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeText(String text) {
        return text == null ? null : text.trim();
    }

    private User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + id
                ));
    }

    private Course getCourseById(UUID id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Course not found with id: " + id
                ));
    }

    private Professor getProfessorEntityById(UUID id) {
        return professorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Professor assignment not found with id: " + id
                ));
    }

    public ProfessorUserResponse create(CreateProfessorUserRequest request) {
        String email = normalizeEmail(request.email());

        if (!request.password().equals(request.confirmPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Passwords do not match"
            );
        }

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already in use"
            );
        }

        User professor = new User();
        professor.setFirstName(normalizeText(request.firstName()));
        professor.setLastName(normalizeText(request.lastName()));
        professor.setEmail(email);
        professor.setPassword(passwordEncoder.encode(request.password()));
        professor.setRole(Role.PROFESSOR);

        try {
            User savedProfessor = userRepository.saveAndFlush(professor);
            return professorMapper.toRegisterResponse(savedProfessor);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already in use",
                    exception
            );
        }
    }

    public ProfessorResponse assign(AssignProfessorRequest request) {
        User user = getUserById(request.userId());
        Course course = getCourseById(request.courseId());

        if (user.getRole() == Role.STUDENT) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only ADMIN or PROFESSOR user can be assigned to course"
            );
        }

        if (professorRepository.existsByUserIdAndCourseId(user.getId(), course.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Professor already assigned to this course"
            );
        }

        Professor professor = new Professor();
        professor.setUser(user);
        professor.setCourse(course);

        try {
            Professor savedProfessor = professorRepository.saveAndFlush(professor);
            return professorMapper.toResponse(savedProfessor);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Professor already assigned to this course",
                    exception
            );
        }
    }

    @Transactional(readOnly = true) public ProfessorResponse getById(UUID id) {
        Professor professor = getProfessorEntityById(id);
        return professorMapper.toResponse(professor);
    }

    @Transactional(readOnly = true)
    public List<ProfessorResponse> getAllByCourse(UUID courseId) {
        getCourseById(courseId);

        return professorMapper.toResponseList(
                professorRepository.findAllByCourseId(courseId)
        );
    }

    @Transactional(readOnly = true)
    public List<ProfessorResponse> getAllByUser(UUID userId) {
        getUserById(userId);

        return professorMapper.toResponseList(
                professorRepository.findAllByUserId(userId)
        );
    }

    @Transactional(readOnly = true)
    public List<ProfessorUserData> getAllProfessorUsers() {
        List<User> professors = userRepository.findAllByRoleIn(
                List.of(Role.PROFESSOR, Role.ADMIN)
        );

        return professorMapper.toUserDataList(professors);
    }

    public void delete(UUID userId, UUID courseId) {
        Professor professor = professorRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Professor assignment not found"
                ));

        professorRepository.delete(professor);
        professorRepository.flush();
    }
}
