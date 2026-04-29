package com.melikyan.academy.service;

import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.Lesson;
import org.springframework.http.HttpStatus;
import com.melikyan.academy.entity.LessonTranslation;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.repository.LessonRepository;
import com.melikyan.academy.repository.LanguageRepository;
import com.melikyan.academy.mapper.LessonTranslationMapper;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import com.melikyan.academy.repository.LessonTranslationRepository;
import com.melikyan.academy.dto.response.lessonTranslation.LessonTranslationResponse;
import com.melikyan.academy.dto.request.lessonTranslation.CreateLessonTranslationRequest;
import com.melikyan.academy.dto.request.lessonTranslation.UpdateLessonTranslationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.Locale;

@Service
@Transactional
@RequiredArgsConstructor
public class LessonTranslationService {
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final LanguageRepository languageRepository;
    private final LessonTranslationMapper lessonTranslationMapper;
    private final LessonTranslationRepository lessonTranslationRepository;

    private String normalizeCode(String code) {
        String normalizedCode = code.trim().toLowerCase(Locale.ROOT);

        if (normalizedCode.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Lesson translation code must not be blank"
            );
        }

        if (normalizedCode.length() != 2) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Lesson translation code must contain exactly 2 characters"
            );
        }

        return normalizedCode;
    }

    private String normalizeTitle(String title) {
        String normalizedTitle = title.trim();

        if (normalizedTitle.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Lesson translation title must not be blank"
            );
        }

        return normalizedTitle;
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }

        String normalizedDescription = description.trim();
        return normalizedDescription.isBlank() ? null : normalizedDescription;
    }

    private String normalizeValueUrl(String valueUrl) {
        String normalizedValueUrl = valueUrl.trim();

        if (normalizedValueUrl.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Lesson translation valueUrl must not be blank"
            );
        }

        return normalizedValueUrl;
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

    private LessonTranslation getLessonTranslationById(UUID id) {
        return lessonTranslationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Lesson translation not found with id: " + id
                ));
    }

    private void validateLanguageExists(String code) {
        if (!languageRepository.existsByCodeIgnoreCase(code)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Language not found with code: " + code
            );
        }
    }

    public LessonTranslationResponse create(CreateLessonTranslationRequest request) {
        String normalizedCode = normalizeCode(request.code());
        String normalizedTitle = normalizeTitle(request.title());
        String normalizedDescription = normalizeDescription(request.description());
        String normalizedValueUrl = normalizeValueUrl(request.valueUrl());

        Lesson lesson = getLessonById(request.lessonId());
        User createdBy = getUserById(request.createdById());

        validateLanguageExists(normalizedCode);

        if (lessonTranslationRepository.existsByLessonIdAndCodeIgnoreCase(
                request.lessonId(),
                normalizedCode
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Lesson translation with this code already exists for this lesson"
            );
        }

        LessonTranslation lessonTranslation = new LessonTranslation();
        lessonTranslation.setCode(normalizedCode);
        lessonTranslation.setTitle(normalizedTitle);
        lessonTranslation.setDescription(normalizedDescription);
        lessonTranslation.setValueUrl(normalizedValueUrl);
        lessonTranslation.setLesson(lesson);
        lessonTranslation.setCreatedBy(createdBy);

        try {
            LessonTranslation savedLessonTranslation =
                    lessonTranslationRepository.saveAndFlush(lessonTranslation);

            return lessonTranslationMapper.toResponse(savedLessonTranslation);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Lesson translation with this code already exists for this lesson",
                    exception
            );
        }
    }

    @Transactional(readOnly = true)
    public LessonTranslationResponse getById(UUID id) {
        LessonTranslation lessonTranslation = getLessonTranslationById(id);
        return lessonTranslationMapper.toResponse(lessonTranslation);
    }

    @Transactional(readOnly = true)
    public List<LessonTranslationResponse> getAll() {
        List<LessonTranslation> lessonTranslations = lessonTranslationRepository.findAll();
        return lessonTranslationMapper.toResponseList(lessonTranslations);
    }

    @Transactional(readOnly = true)
    public List<LessonTranslationResponse> getByCode(String code) {
        String normalizedCode = normalizeCode(code);
        validateLanguageExists(normalizedCode);

        List<LessonTranslation> lessonTranslations =
                lessonTranslationRepository.findByCodeIgnoreCase(normalizedCode);

        return lessonTranslationMapper.toResponseList(lessonTranslations);
    }

    @Transactional(readOnly = true)
    public List<LessonTranslationResponse> getByLessonId(UUID lessonId) {
        getLessonById(lessonId);

        List<LessonTranslation> lessonTranslations =
                lessonTranslationRepository.findByLessonId(lessonId);

        return lessonTranslationMapper.toResponseList(lessonTranslations);
    }

    @Transactional(readOnly = true)
    public LessonTranslationResponse getByLessonIdAndCode(UUID lessonId, String code) {
        String normalizedCode = normalizeCode(code);

        getLessonById(lessonId);
        validateLanguageExists(normalizedCode);

        LessonTranslation lessonTranslation = lessonTranslationRepository
                .findByLessonIdAndCodeIgnoreCase(lessonId, normalizedCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Lesson translation not found with lesson id: " +
                                lessonId + " and code: " + normalizedCode
                ));

        return lessonTranslationMapper.toResponse(lessonTranslation);
    }

    public LessonTranslationResponse update(UUID id, UpdateLessonTranslationRequest request) {
        LessonTranslation lessonTranslation = getLessonTranslationById(id);

        if (
                request.code() == null &&
                        request.title() == null &&
                        request.description() == null &&
                        request.valueUrl() == null
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one field must be provided"
            );
        }

        if (request.code() != null) {
            String normalizedCode = normalizeCode(request.code());
            validateLanguageExists(normalizedCode);

            if (lessonTranslationRepository.existsByLessonIdAndCodeIgnoreCaseAndIdNot(
                    lessonTranslation.getLesson().getId(),
                    normalizedCode,
                    id
            )) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Lesson translation with this code already exists for this lesson"
                );
            }

            lessonTranslation.setCode(normalizedCode);
        }

        if (request.title() != null) {
            lessonTranslation.setTitle(normalizeTitle(request.title()));
        }

        if (request.description() != null) {
            lessonTranslation.setDescription(normalizeDescription(request.description()));
        }

        if (request.valueUrl() != null) {
            lessonTranslation.setValueUrl(normalizeValueUrl(request.valueUrl()));
        }

        try {
            LessonTranslation savedLessonTranslation = lessonTranslationRepository.saveAndFlush(lessonTranslation);

            return lessonTranslationMapper.toResponse(savedLessonTranslation);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Lesson translation with this code already exists for this lesson",
                    exception
            );
        }
    }

    public void delete(UUID id) {
        LessonTranslation lessonTranslation = getLessonTranslationById(id);
        lessonTranslationRepository.delete(lessonTranslation);
    }
}