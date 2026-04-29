package com.melikyan.academy.service;

import com.melikyan.academy.dto.request.sectionTranslation.CreateSectionTranslationRequest;
import com.melikyan.academy.dto.request.sectionTranslation.UpdateSectionTranslationRequest;
import com.melikyan.academy.dto.response.sectionTranslation.SectionTranslationResponse;
import com.melikyan.academy.entity.ExamSection;
import com.melikyan.academy.entity.SectionTranslation;
import com.melikyan.academy.entity.User;
import com.melikyan.academy.mapper.SectionTranslationMapper;
import com.melikyan.academy.repository.ExamSectionRepository;
import com.melikyan.academy.repository.LanguageRepository;
import com.melikyan.academy.repository.SectionTranslationRepository;
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
public class SectionTranslationService {
    private final UserRepository userRepository;
    private final ExamSectionRepository examSectionRepository;
    private final LanguageRepository languageRepository;
    private final SectionTranslationMapper sectionTranslationMapper;
    private final SectionTranslationRepository sectionTranslationRepository;

    private String normalizeCode(String code) {
        String normalizedCode = code.trim().toLowerCase(Locale.ROOT);

        if (normalizedCode.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Section translation code must not be blank"
            );
        }

        if (normalizedCode.length() != 2) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Section translation code must contain exactly 2 characters"
            );
        }

        return normalizedCode;
    }

    private String normalizeTitle(String title) {
        String normalizedTitle = title.trim();

        if (normalizedTitle.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Section translation title must not be blank"
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

    private ExamSection getExamSectionById(UUID id) {
        return examSectionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Exam section not found with id: " + id
                ));
    }

    private SectionTranslation getSectionTranslationById(UUID id) {
        return sectionTranslationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Section translation not found with id: " + id
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

    public SectionTranslationResponse create(CreateSectionTranslationRequest request) {
        String normalizedCode = normalizeCode(request.code());
        String normalizedTitle = normalizeTitle(request.title());
        String normalizedDescription = normalizeDescription(request.description());

        ExamSection examSection = getExamSectionById(request.examSectionId());
        User createdBy = getUserById(request.createdById());

        validateLanguageExists(normalizedCode);

        if (sectionTranslationRepository.existsByExamSectionIdAndCodeIgnoreCase(
                request.examSectionId(),
                normalizedCode
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Section translation with this code already exists for this exam section"
            );
        }

        SectionTranslation sectionTranslation = new SectionTranslation();
        sectionTranslation.setCode(normalizedCode);
        sectionTranslation.setTitle(normalizedTitle);
        sectionTranslation.setDescription(normalizedDescription);
        sectionTranslation.setExamSection(examSection);
        sectionTranslation.setCreatedBy(createdBy);

        try {
            SectionTranslation savedSectionTranslation = sectionTranslationRepository.saveAndFlush(sectionTranslation);
            return sectionTranslationMapper.toResponse(savedSectionTranslation);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Section translation with this code already exists for this exam section",
                    exception
            );
        }
    }

    @Transactional(readOnly = true)
    public SectionTranslationResponse getById(UUID id) {
        SectionTranslation sectionTranslation = getSectionTranslationById(id);
        return sectionTranslationMapper.toResponse(sectionTranslation);
    }

    @Transactional(readOnly = true)
    public List<SectionTranslationResponse> getAll() {
        List<SectionTranslation> sectionTranslations = sectionTranslationRepository.findAll();
        return sectionTranslationMapper.toResponseList(sectionTranslations);
    }

    @Transactional(readOnly = true)
    public List<SectionTranslationResponse> getByCode(String code) {
        String normalizedCode = normalizeCode(code);
        validateLanguageExists(normalizedCode);

        List<SectionTranslation> sectionTranslations = sectionTranslationRepository.findByCodeIgnoreCase(normalizedCode);
        return sectionTranslationMapper.toResponseList(sectionTranslations);
    }

    @Transactional(readOnly = true)
    public List<SectionTranslationResponse> getByExamSectionId(UUID examSectionId) {
        getExamSectionById(examSectionId);

        List<SectionTranslation> sectionTranslations = sectionTranslationRepository.findByExamSectionId(examSectionId);
        return sectionTranslationMapper.toResponseList(sectionTranslations);
    }

    @Transactional(readOnly = true)
    public SectionTranslationResponse getByExamSectionIdAndCode(UUID examSectionId, String code) {
        String normalizedCode = normalizeCode(code);

        getExamSectionById(examSectionId);
        validateLanguageExists(normalizedCode);

        SectionTranslation sectionTranslation = sectionTranslationRepository
                .findByExamSectionIdAndCodeIgnoreCase(examSectionId, normalizedCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Section translation not found with exam section id: " +
                                examSectionId + " and code: " + normalizedCode
                ));

        return sectionTranslationMapper.toResponse(sectionTranslation);
    }

    public SectionTranslationResponse update(UUID id, UpdateSectionTranslationRequest request) {
        SectionTranslation sectionTranslation = getSectionTranslationById(id);

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

            if (sectionTranslationRepository.existsByExamSectionIdAndCodeIgnoreCaseAndIdNot(
                    sectionTranslation.getExamSection().getId(),
                    normalizedCode,
                    id
            )) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Section translation with this code already exists for this exam section"
                );
            }

            sectionTranslation.setCode(normalizedCode);
        }

        if (request.title() != null) {
            sectionTranslation.setTitle(normalizeTitle(request.title()));
        }

        if (request.description() != null) {
            sectionTranslation.setDescription(normalizeDescription(request.description()));
        }

        try {
            SectionTranslation savedSectionTranslation = sectionTranslationRepository.saveAndFlush(sectionTranslation);
            return sectionTranslationMapper.toResponse(savedSectionTranslation);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Section translation with this code already exists for this exam section",
                    exception
            );
        }
    }

    public void delete(UUID id) {
        SectionTranslation sectionTranslation = getSectionTranslationById(id);
        sectionTranslationRepository.delete(sectionTranslation);
    }
}
