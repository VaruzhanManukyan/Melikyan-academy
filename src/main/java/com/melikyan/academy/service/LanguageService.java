package com.melikyan.academy.service;

import com.melikyan.academy.dto.request.language.CreateLanguageRequest;
import com.melikyan.academy.dto.request.language.UpdateLanguageRequest;
import com.melikyan.academy.dto.response.language.LanguageResponse;
import com.melikyan.academy.entity.Language;
import com.melikyan.academy.entity.User;
import com.melikyan.academy.mapper.LanguageMapper;
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
public class LanguageService {
    private final UserRepository userRepository;
    private final LanguageMapper languageMapper;
    private final LanguageRepository languageRepository;

    private String normalizeName(String name) {
        String normalizedName = name.trim();

        if (normalizedName.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Language name must not be blank"
            );
        }

        return normalizedName;
    }

    private String normalizeCode(String code) {
        String normalizedCode = code.trim().toLowerCase(Locale.ROOT);

        if (normalizedCode.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Language code must not be blank"
            );
        }

        if (normalizedCode.length() != 2) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Language code must contain exactly 2 characters"
            );
        }

        return normalizedCode;
    }

    private User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + id
                ));
    }

    private Language getLanguageEntityById(UUID id) {
        return languageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Language not found with id: " + id
                ));
    }

    public LanguageResponse create(CreateLanguageRequest request) {
        String normalizedCode = normalizeCode(request.code());
        String normalizedName = normalizeName(request.name());

        if (languageRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Language with this code already exists"
            );
        }

        if (languageRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Language with this name already exists"
            );
        }

        User createdBy = getUserById(request.createdById());

        Language language = new Language();
        language.setCode(normalizedCode);
        language.setName(normalizedName);
        language.setCreatedBy(createdBy);

        try {
            Language savedLanguage = languageRepository.saveAndFlush(language);
            return languageMapper.toResponse(savedLanguage);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Language with this code or name already exists",
                    exception
            );
        }
    }

    @Transactional(readOnly = true)
    public LanguageResponse getById(UUID id) {
        Language language = getLanguageEntityById(id);
        return languageMapper.toResponse(language);
    }

    @Transactional(readOnly = true)
    public LanguageResponse getByCode(String code) {
        String normalizedCode = normalizeCode(code);

        Language language = languageRepository.findByCodeIgnoreCase(normalizedCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Language not found with code: " + normalizedCode
                ));

        return languageMapper.toResponse(language);
    }

    @Transactional(readOnly = true)
    public List<LanguageResponse> getAll() {
        List<Language> languages = languageRepository.findAll();
        return languageMapper.toResponseList(languages);
    }

    public LanguageResponse update(UUID id, UpdateLanguageRequest request) {
        Language language = getLanguageEntityById(id);

        if (request.code() == null && request.name() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one field must be provided"
            );
        }

        if (request.code() != null) {
            String normalizedCode = normalizeCode(request.code());

            if (languageRepository.existsByCodeIgnoreCaseAndIdNot(normalizedCode, id)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Language with this code already exists"
                );
            }

            language.setCode(normalizedCode);
        }

        if (request.name() != null) {
            String normalizedName = normalizeName(request.name());

            if (languageRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Language with this name already exists"
                );
            }

            language.setName(normalizedName);
        }

        try {
            Language savedLanguage = languageRepository.saveAndFlush(language);
            return languageMapper.toResponse(savedLanguage);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Language with this code or name already exists",
                    exception
            );
        }
    }

    public void delete(UUID id) {
        Language language = getLanguageEntityById(id);
        languageRepository.delete(language);
    }
}