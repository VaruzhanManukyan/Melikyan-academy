package com.melikyan.academy.service;

import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.Course;
import com.melikyan.academy.entity.Lesson;
import org.springframework.http.HttpStatus;
import com.melikyan.academy.entity.LessonAttendance;
import com.melikyan.academy.entity.enums.LessonState;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.repository.LessonRepository;
import org.springframework.security.core.Authentication;
import com.melikyan.academy.entity.enums.AttendanceStatus;
import com.melikyan.academy.mapper.LessonAttendanceMapper;
import com.melikyan.academy.entity.enums.RegistrationStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import com.melikyan.academy.repository.LessonAttendanceRepository;
import com.melikyan.academy.repository.ProductRegistrationRepository;
import com.melikyan.academy.dto.response.lessonAttendance.LessonAttendanceResponse;
import com.melikyan.academy.dto.request.lessonAttendance.CreateLessonAttendanceRequest;
import com.melikyan.academy.dto.request.lessonAttendance.UpdateLessonAttendanceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class LessonAttendanceService {
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final LessonAttendanceMapper lessonAttendanceMapper;
    private final LessonAttendanceRepository lessonAttendanceRepository;
    private final ProductRegistrationRepository productRegistrationRepository;

    private String normalizeNote(String note) {
        if (note == null) {
            return null;
        }

        String normalized = note.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Authenticated user not found"
                ));
    }

    private User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + id
                ));
    }

    private Lesson getLessonById(UUID id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Lesson not found with id: " + id
                ));
    }

    private LessonAttendance getLessonAttendanceEntityById(UUID id) {
        return lessonAttendanceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Lesson attendance not found with id: " + id
                ));
    }

    private LessonAttendance getLessonAttendanceEntityByIdAndUserId(UUID id, UUID userId) {
        return lessonAttendanceRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Lesson attendance not found with id: " + id
                ));
    }

    private UUID getContentItemIdFromLesson(Lesson lesson) {
        if (lesson.getCourse() == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Lesson is not linked to course"
            );
        }

        Course course = lesson.getCourse();

        if (course.getContentItem() == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Course is not linked to content item"
            );
        }

        return course.getContentItem().getId();
    }

    private void validateUserHasActiveAccessToLesson(User user, Lesson lesson) {
        UUID contentItemId = getContentItemIdFromLesson(lesson);

        List<UUID> userIds = productRegistrationRepository.findUserIdsByContentItemIdAndStatus(
                contentItemId,
                RegistrationStatus.ACTIVE
        );

        if (!userIds.contains(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User does not have active access to this lesson"
            );
        }
    }

    public LessonAttendanceResponse create(CreateLessonAttendanceRequest request) {
        User user = getUserById(request.userId());
        Lesson lesson = getLessonById(request.lessonId());

        validateUserHasActiveAccessToLesson(user, lesson);

        if (lessonAttendanceRepository.existsByUserIdAndLessonId(user.getId(), lesson.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Lesson attendance already exists for this user and lesson"
            );
        }

        LessonAttendance lessonAttendance = new LessonAttendance();
        lessonAttendance.setNote(normalizeNote(request.note()));
        lessonAttendance.setStatus(request.status());
        lessonAttendance.setUser(user);
        lessonAttendance.setLesson(lesson);

        try {
            LessonAttendance savedLessonAttendance = lessonAttendanceRepository.saveAndFlush(lessonAttendance);
            return lessonAttendanceMapper.toResponse(savedLessonAttendance);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Lesson attendance already exists for this user and lesson",
                    exception
            );
        }
    }

    public LessonAttendanceResponse checkIn(UUID lessonId, Authentication authentication) {
        User user = getCurrentUser(authentication);
        Lesson lesson = getLessonById(lessonId);

        LessonAttendance lessonAttendance = lessonAttendanceRepository
                .findByUserIdAndLessonId(user.getId(), lesson.getId())
                .orElseGet(() -> {
                    LessonAttendance created = new LessonAttendance();
                    created.setUser(user);
                    created.setLesson(lesson);
                    return created;
                });

        lessonAttendance.setStatus(AttendanceStatus.ATTENDED);
        lessonAttendance.setNote("Checked in by student");

        try {
            LessonAttendance savedLessonAttendance = lessonAttendanceRepository.saveAndFlush(lessonAttendance);

            return lessonAttendanceMapper.toResponse(savedLessonAttendance);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Lesson attendance already exists for this user and lesson",
                    exception
            );
        }
    }

    public void generateMissedForLesson(UUID lessonId) {
        Lesson lesson = getLessonById(lessonId);

        if (lesson.getState() != LessonState.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Missed attendance can be generated only for completed lesson"
            );
        }

        UUID contentItemId = getContentItemIdFromLesson(lesson);

        List<UUID> userIds = productRegistrationRepository.findUserIdsByContentItemIdAndStatus(
                contentItemId,
                RegistrationStatus.ACTIVE
        );

        for (UUID userId : userIds) {
            User user = getUserById(userId);

            LessonAttendance lessonAttendance = lessonAttendanceRepository
                    .findByUserIdAndLessonId(user.getId(), lesson.getId())
                    .orElseGet(() -> {
                        LessonAttendance created = new LessonAttendance();
                        created.setUser(user);
                        created.setLesson(lesson);
                        return created;
                    });

            if (lessonAttendance.getStatus() == AttendanceStatus.ATTENDED) {
                continue;
            }

            lessonAttendance.setStatus(AttendanceStatus.MISSED);
            lessonAttendance.setNote("Automatically marked as missed");

            lessonAttendanceRepository.save(lessonAttendance);
        }

        lessonAttendanceRepository.flush();
    }

    public void generateEnrolledForLesson(UUID lessonId) {
        Lesson lesson = getLessonById(lessonId);

        if (lesson.getState() == LessonState.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Enrolled attendance cannot be generated for completed lesson"
            );
        }

        UUID contentItemId = getContentItemIdFromLesson(lesson);

        List<UUID> userIds = productRegistrationRepository.findUserIdsByContentItemIdAndStatus(
                contentItemId,
                RegistrationStatus.ACTIVE
        );

        for (UUID userId : userIds) {
            if (lessonAttendanceRepository.existsByUserIdAndLessonId(userId, lesson.getId())) {
                continue;
            }

            User user = getUserById(userId);

            LessonAttendance lessonAttendance = new LessonAttendance();
            lessonAttendance.setUser(user);
            lessonAttendance.setLesson(lesson);
            lessonAttendance.setStatus(AttendanceStatus.ENROLLED);
            lessonAttendance.setNote("Automatically marked as enrolled");

            lessonAttendanceRepository.save(lessonAttendance);
        }

        lessonAttendanceRepository.flush();
    }

    @Transactional(readOnly = true)
    public LessonAttendanceResponse getById(UUID id) {
        LessonAttendance lessonAttendance = getLessonAttendanceEntityById(id);
        return lessonAttendanceMapper.toResponse(lessonAttendance);
    }

    @Transactional(readOnly = true)
    public LessonAttendanceResponse getMyById(UUID id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        LessonAttendance lessonAttendance =
                getLessonAttendanceEntityByIdAndUserId(id, currentUser.getId());

        return lessonAttendanceMapper.toResponse(lessonAttendance);
    }

    @Transactional(readOnly = true)
    public List<LessonAttendanceResponse> getMyAll(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        return lessonAttendanceMapper.toResponseList(
                lessonAttendanceRepository.findAllByUserId(currentUser.getId())
        );
    }

    @Transactional(readOnly = true)
    public LessonAttendanceResponse getMyByLesson(UUID lessonId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        LessonAttendance lessonAttendance = lessonAttendanceRepository
                .findByUserIdAndLessonId(currentUser.getId(), lessonId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Lesson attendance not found for lesson id: " + lessonId
                ));

        return lessonAttendanceMapper.toResponse(lessonAttendance);
    }

    @Transactional(readOnly = true)
    public List<LessonAttendanceResponse> getAllByLesson(UUID lessonId) {
        getLessonById(lessonId);

        return lessonAttendanceMapper.toResponseList(
                lessonAttendanceRepository.findAllByLessonId(lessonId)
        );
    }

    public LessonAttendanceResponse update(UUID id, UpdateLessonAttendanceRequest request) {
        LessonAttendance lessonAttendance = getLessonAttendanceEntityById(id);

        lessonAttendance.setNote(normalizeNote(request.note()));
        lessonAttendance.setStatus(request.status());

        try {
            LessonAttendance savedLessonAttendance =
                    lessonAttendanceRepository.saveAndFlush(lessonAttendance);

            return lessonAttendanceMapper.toResponse(savedLessonAttendance);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Unable to update lesson attendance",
                    exception
            );
        }
    }

    public void delete(UUID id) {
        LessonAttendance lessonAttendance = getLessonAttendanceEntityById(id);
        lessonAttendanceRepository.delete(lessonAttendance);
    }
}