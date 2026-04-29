package com.melikyan.academy.service;

import com.melikyan.academy.dto.request.homeworkTranslation.CreateHomeworkTranslationRequest;
import com.melikyan.academy.dto.request.homeworkTranslation.UpdateHomeworkTranslationRequest;
import com.melikyan.academy.dto.response.homeworkTranslation.HomeworkTranslationResponse;
import com.melikyan.academy.entity.Homework;
import com.melikyan.academy.entity.HomeworkTranslation;
import com.melikyan.academy.entity.User;
import com.melikyan.academy.mapper.HomeworkTranslationMapper;
import com.melikyan.academy.repository.HomeworkRepository;
import com.melikyan.academy.repository.HomeworkTranslationRepository;
import com.melikyan.academy.repository.LanguageRepository;
import com.melikyan.academy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class HomeworkTranslationService {
    private final UserRepository userRepository;
    private final HomeworkRepository homeworkRepository;
    private final LanguageRepository languageRepository;
    private final HomeworkTranslationMapper homeworkTranslationMapper;
    private final HomeworkTranslationRepository homeworkTranslationRepository;

    private String normalizeCode(String code) {
        String normalizedCode = code.trim().toLowerCase(Locale.ROOT);

        if (normalizedCode.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Homework translation code must not be blank"
            );
        }

        if (normalizedCode.length() != 2) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Homework translation code must contain exactly 2 characters"
            );
        }

        return normalizedCode;
    }

    private String normalizeTitle(String title) {
        String normalizedTitle = title.trim();

        if (normalizedTitle.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Homework translation title must not be blank"
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

    private User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + id
                ));
    }

    private Homework getHomeworkById(UUID id) {
        return homeworkRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Homework not found with id: " + id
                ));
    }

    private HomeworkTranslation getHomeworkTranslationById(UUID id) {
        return homeworkTranslationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Homework translation not found with id: " + id
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

    public HomeworkTranslationResponse create(CreateHomeworkTranslationRequest request) {
        String normalizedCode = normalizeCode(request.code());
        String normalizedTitle = normalizeTitle(request.title());
        String normalizedDescription = normalizeDescription(request.description());

        Homework homework = getHomeworkById(request.homeworkId());
        User createdBy = getUserById(request.createdById());

        validateLanguageExists(normalizedCode);

        if (homeworkTranslationRepository.existsByHomeworkIdAndCodeIgnoreCase(
                request.homeworkId(),
                normalizedCode
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Homework translation with this code already exists for this homework"
            );
        }

        HomeworkTranslation homeworkTranslation = new HomeworkTranslation();
        homeworkTranslation.setCode(normalizedCode);
        homeworkTranslation.setTitle(normalizedTitle);
        homeworkTranslation.setDescription(normalizedDescription);
        homeworkTranslation.setHomework(homework);
        homeworkTranslation.setCreatedBy(createdBy);

        try {
            HomeworkTranslation savedHomeworkTranslation = homeworkTranslationRepository.saveAndFlush(homeworkTranslation);

            return homeworkTranslationMapper.toResponse(savedHomeworkTranslation);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Homework translation with this code already exists for this homework",
                    exception
            );
        }
    }

    @Transactional(readOnly = true)
    public HomeworkTranslationResponse getById(UUID id) {
        HomeworkTranslation homeworkTranslation = getHomeworkTranslationById(id);
        return homeworkTranslationMapper.toResponse(homeworkTranslation);
    }

    @Transactional(readOnly = true)
    public List<HomeworkTranslationResponse> getAll() {
        List<HomeworkTranslation> homeworkTranslations = homeworkTranslationRepository.findAll();
        return homeworkTranslationMapper.toResponseList(homeworkTranslations);
    }

    @Transactional(readOnly = true)
    public List<HomeworkTranslationResponse> getByCode(String code) {
        String normalizedCode = normalizeCode(code);
        validateLanguageExists(normalizedCode);

        List<HomeworkTranslation> homeworkTranslations = homeworkTranslationRepository.findByCodeIgnoreCase(normalizedCode);

        return homeworkTranslationMapper.toResponseList(homeworkTranslations);
    }

    @Transactional(readOnly = true)
    public List<HomeworkTranslationResponse> getByHomeworkId(UUID homeworkId) {
        getHomeworkById(homeworkId);

        List<HomeworkTranslation> homeworkTranslations = homeworkTranslationRepository.findByHomeworkId(homeworkId);

        return homeworkTranslationMapper.toResponseList(homeworkTranslations);
    }

    @Transactional(readOnly = true)
    public HomeworkTranslationResponse getByHomeworkIdAndCode(UUID homeworkId, String code) {
        String normalizedCode = normalizeCode(code);

        getHomeworkById(homeworkId);
        validateLanguageExists(normalizedCode);

        HomeworkTranslation homeworkTranslation = homeworkTranslationRepository
                .findByHomeworkIdAndCodeIgnoreCase(homeworkId, normalizedCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Homework translation not found with homework id: " +
                                homeworkId + " and code: " + normalizedCode
                ));

        return homeworkTranslationMapper.toResponse(homeworkTranslation);
    }

    public HomeworkTranslationResponse update(UUID id, UpdateHomeworkTranslationRequest request) {
        HomeworkTranslation homeworkTranslation = getHomeworkTranslationById(id);

        if (
                request.code() == null &&
                        request.title() == null &&
                        request.description() == null
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one field must be provided"
            );
        }

        if (request.code() != null) {
            String normalizedCode = normalizeCode(request.code());
            validateLanguageExists(normalizedCode);

            if (homeworkTranslationRepository.existsByHomeworkIdAndCodeIgnoreCaseAndIdNot(
                    homeworkTranslation.getHomework().getId(),
                    normalizedCode,
                    id
            )) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Homework translation with this code already exists for this homework"
                );
            }

            homeworkTranslation.setCode(normalizedCode);
        }

        if (request.title() != null) {
            homeworkTranslation.setTitle(normalizeTitle(request.title()));
        }

        if (request.description() != null) {
            homeworkTranslation.setDescription(normalizeDescription(request.description()));
        }

        try {
            HomeworkTranslation savedHomeworkTranslation = homeworkTranslationRepository.saveAndFlush(homeworkTranslation);

            return homeworkTranslationMapper.toResponse(savedHomeworkTranslation);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Homework translation with this code already exists for this homework",
                    exception
            );
        }
    }

    public void delete(UUID id) {
        HomeworkTranslation homeworkTranslation = getHomeworkTranslationById(id);
        homeworkTranslationRepository.delete(homeworkTranslation);
    }
}
