package com.melikyan.academy.service;

import org.mockito.ArgumentCaptor;
import com.melikyan.academy.entity.User;
import org.springframework.http.HttpStatus;
import com.melikyan.academy.entity.Language;
import org.mockito.junit.jupiter.MockitoExtension;
import com.melikyan.academy.mapper.LanguageMapper;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.repository.LanguageRepository;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import com.melikyan.academy.dto.response.language.LanguageResponse;
import com.melikyan.academy.dto.request.language.CreateLanguageRequest;
import com.melikyan.academy.dto.request.language.UpdateLanguageRequest;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.time.OffsetDateTime;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LanguageServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private LanguageMapper languageMapper;

    @Mock
    private LanguageRepository languageRepository;

    @InjectMocks
    private LanguageService languageService;

    @Test
    void createLanguage_shouldCreateAndReturnResponse() {
        UUID userId = UUID.randomUUID();
        UUID languageId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        CreateLanguageRequest request = new CreateLanguageRequest(
                " EN ",
                "  English  ",
                userId
        );

        User user = new User();
        user.setId(userId);

        Language savedLanguage = new Language();
        savedLanguage.setId(languageId);
        savedLanguage.setCode("en");
        savedLanguage.setName("English");
        savedLanguage.setCreatedBy(user);

        LanguageResponse response = new LanguageResponse(
                languageId,
                "en",
                "English",
                userId,
                now,
                now
        );

        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(false);
        when(languageRepository.existsByNameIgnoreCase("English")).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.saveAndFlush(any(Language.class))).thenReturn(savedLanguage);
        when(languageMapper.toResponse(savedLanguage)).thenReturn(response);

        LanguageResponse result = languageService.create(request);

        assertNotNull(result);
        assertEquals(response, result);

        ArgumentCaptor<Language> languageCaptor = ArgumentCaptor.forClass(Language.class);
        verify(languageRepository).saveAndFlush(languageCaptor.capture());

        Language language = languageCaptor.getValue();

        assertEquals("en", language.getCode());
        assertEquals("English", language.getName());
        assertEquals(user, language.getCreatedBy());

        verify(languageRepository).existsByCodeIgnoreCase("en");
        verify(languageRepository).existsByNameIgnoreCase("English");
        verify(userRepository).findById(userId);
        verify(languageMapper).toResponse(savedLanguage);
    }

    @Test
    void create_shouldThrowConflict_whenCodeAlreadyExists() {
        UUID userId = UUID.randomUUID();

        CreateLanguageRequest request = new CreateLanguageRequest(
                " EN ",
                "English",
                userId
        );

        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> languageService.create(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("409 CONFLICT \"Language with this code already exists\"", exception.getMessage());

        verify(languageRepository).existsByCodeIgnoreCase("en");
        verify(languageRepository, never()).existsByNameIgnoreCase(any());
        verify(userRepository, never()).findById(any());
        verify(languageRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowConflict_whenNameAlreadyExists() {
        UUID userId = UUID.randomUUID();

        CreateLanguageRequest request = new CreateLanguageRequest(
                "en",
                "  English  ",
                userId
        );

        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(false);
        when(languageRepository.existsByNameIgnoreCase("English")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> languageService.create(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("409 CONFLICT \"Language with this name already exists\"", exception.getMessage());

        verify(languageRepository).existsByCodeIgnoreCase("en");
        verify(languageRepository).existsByNameIgnoreCase("English");
        verify(userRepository, never()).findById(any());
        verify(languageRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowNotFound_whenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();

        CreateLanguageRequest request = new CreateLanguageRequest(
                "en",
                "English",
                userId
        );

        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(false);
        when(languageRepository.existsByNameIgnoreCase("English")).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> languageService.create(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("User not found with id"));

        verify(languageRepository).existsByCodeIgnoreCase("en");
        verify(languageRepository).existsByNameIgnoreCase("English");
        verify(userRepository).findById(userId);
        verify(languageRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowBadRequest_whenCodeIsBlank() {
        UUID userId = UUID.randomUUID();

        CreateLanguageRequest request = new CreateLanguageRequest(
                "   ",
                "English",
                userId
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> languageService.create(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("400 BAD_REQUEST \"Language code must not be blank\"", exception.getMessage());

        verify(languageRepository, never()).existsByCodeIgnoreCase(any());
        verify(languageRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowBadRequest_whenCodeLengthIsInvalid() {
        UUID userId = UUID.randomUUID();

        CreateLanguageRequest request = new CreateLanguageRequest(
                "eng",
                "English",
                userId
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> languageService.create(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("400 BAD_REQUEST \"Language code must contain exactly 2 characters\"", exception.getMessage());

        verify(languageRepository, never()).existsByCodeIgnoreCase(any());
        verify(languageRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowBadRequest_whenNameIsBlank() {
        UUID userId = UUID.randomUUID();

        CreateLanguageRequest request = new CreateLanguageRequest(
                "en",
                "   ",
                userId
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> languageService.create(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("400 BAD_REQUEST \"Language name must not be blank\"", exception.getMessage());

        verify(languageRepository, never()).existsByCodeIgnoreCase(any());
        verify(languageRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowConflict_whenSaveFailsByUniqueConstraint() {
        UUID userId = UUID.randomUUID();

        CreateLanguageRequest request = new CreateLanguageRequest(
                "en",
                "English",
                userId
        );

        User user = new User();
        user.setId(userId);

        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(false);
        when(languageRepository.existsByNameIgnoreCase("English")).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.saveAndFlush(any(Language.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> languageService.create(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("409 CONFLICT \"Language with this code or name already exists\"", exception.getMessage());

        verify(languageRepository).saveAndFlush(any(Language.class));
        verify(languageMapper, never()).toResponse(any());
    }

    @Test
    void getById_shouldReturnResponse() {
        UUID languageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        User user = new User();
        user.setId(userId);

        Language language = new Language();
        language.setId(languageId);
        language.setCode("en");
        language.setName("English");
        language.setCreatedBy(user);

        LanguageResponse response = new LanguageResponse(
                languageId,
                "en",
                "English",
                userId,
                now,
                now
        );

        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));
        when(languageMapper.toResponse(language)).thenReturn(response);

        LanguageResponse result = languageService.getById(languageId);

        assertNotNull(result);
        assertEquals(response, result);

        verify(languageRepository).findById(languageId);
        verify(languageMapper).toResponse(language);
    }

    @Test
    void getById_shouldThrowNotFound_whenDoesNotExist() {
        UUID languageId = UUID.randomUUID();

        when(languageRepository.findById(languageId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> languageService.getById(languageId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Language not found with id"));

        verify(languageRepository).findById(languageId);
        verify(languageMapper, never()).toResponse(any());
    }

    @Test
    void getByCode_shouldReturnResponse() {
        UUID languageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        User user = new User();
        user.setId(userId);

        Language language = new Language();
        language.setId(languageId);
        language.setCode("en");
        language.setName("English");
        language.setCreatedBy(user);

        LanguageResponse response = new LanguageResponse(
                languageId,
                "en",
                "English",
                userId,
                now,
                now
        );

        when(languageRepository.findByCodeIgnoreCase("en")).thenReturn(Optional.of(language));
        when(languageMapper.toResponse(language)).thenReturn(response);

        LanguageResponse result = languageService.getByCode(" EN ");

        assertNotNull(result);
        assertEquals(response, result);

        verify(languageRepository).findByCodeIgnoreCase("en");
        verify(languageMapper).toResponse(language);
    }

    @Test
    void getByCode_shouldThrowNotFound_whenDoesNotExist() {
        when(languageRepository.findByCodeIgnoreCase("en")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> languageService.getByCode(" EN ")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("404 NOT_FOUND \"Language not found with code: en\"", exception.getMessage());

        verify(languageRepository).findByCodeIgnoreCase("en");
        verify(languageMapper, never()).toResponse(any());
    }

    @Test
    void getAll_shouldReturnResponseList() {
        Language language1 = new Language();
        language1.setId(UUID.randomUUID());
        language1.setCode("en");
        language1.setName("English");

        Language language2 = new Language();
        language2.setId(UUID.randomUUID());
        language2.setCode("hy");
        language2.setName("Armenian");

        List<Language> languages = List.of(language1, language2);

        List<LanguageResponse> responses = List.of(
                new LanguageResponse(
                        language1.getId(),
                        "en",
                        "English",
                        UUID.randomUUID(),
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                ),
                new LanguageResponse(
                        language2.getId(),
                        "hy",
                        "Armenian",
                        UUID.randomUUID(),
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                )
        );

        when(languageRepository.findAll()).thenReturn(languages);
        when(languageMapper.toResponseList(languages)).thenReturn(responses);

        List<LanguageResponse> result = languageService.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(languageRepository).findAll();
        verify(languageMapper).toResponseList(languages);
    }

    @Test
    void updateLanguage_shouldUpdateCodeAndName() {
        UUID languageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        UpdateLanguageRequest request = new UpdateLanguageRequest(
                "  Armenian  ",
                " HY "
        );

        User user = new User();
        user.setId(userId);

        Language language = new Language();
        language.setId(languageId);
        language.setCode("en");
        language.setName("English");
        language.setCreatedBy(user);

        Language savedLanguage = new Language();
        savedLanguage.setId(languageId);
        savedLanguage.setCode("hy");
        savedLanguage.setName("Armenian");
        savedLanguage.setCreatedBy(user);

        LanguageResponse response = new LanguageResponse(
                languageId,
                "hy",
                "Armenian",
                userId,
                now,
                now
        );

        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));
        when(languageRepository.existsByCodeIgnoreCaseAndIdNot("hy", languageId)).thenReturn(false);
        when(languageRepository.existsByNameIgnoreCaseAndIdNot("Armenian", languageId)).thenReturn(false);
        when(languageRepository.saveAndFlush(language)).thenReturn(savedLanguage);
        when(languageMapper.toResponse(savedLanguage)).thenReturn(response);

        LanguageResponse result = languageService.update(languageId, request);

        assertNotNull(result);
        assertEquals(response, result);
        assertEquals("hy", language.getCode());
        assertEquals("Armenian", language.getName());

        verify(languageRepository).findById(languageId);
        verify(languageRepository).existsByCodeIgnoreCaseAndIdNot("hy", languageId);
        verify(languageRepository).existsByNameIgnoreCaseAndIdNot("Armenian", languageId);
        verify(languageRepository).saveAndFlush(language);
        verify(languageMapper).toResponse(savedLanguage);
    }

    @Test
    void update_shouldUpdateOnlyName() {
        UUID languageId = UUID.randomUUID();

        UpdateLanguageRequest request = new UpdateLanguageRequest(
                "  English Updated  ",
                null
        );

        Language language = new Language();
        language.setId(languageId);
        language.setCode("en");
        language.setName("English");

        LanguageResponse response = new LanguageResponse(
                languageId,
                "en",
                "English Updated",
                UUID.randomUUID(),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));
        when(languageRepository.existsByNameIgnoreCaseAndIdNot("English Updated", languageId)).thenReturn(false);
        when(languageRepository.saveAndFlush(language)).thenReturn(language);
        when(languageMapper.toResponse(language)).thenReturn(response);

        LanguageResponse result = languageService.update(languageId, request);

        assertNotNull(result);
        assertEquals("en", language.getCode());
        assertEquals("English Updated", language.getName());

        verify(languageRepository).findById(languageId);
        verify(languageRepository, never()).existsByCodeIgnoreCaseAndIdNot(any(), any());
        verify(languageRepository).existsByNameIgnoreCaseAndIdNot("English Updated", languageId);
        verify(languageRepository).saveAndFlush(language);
        verify(languageMapper).toResponse(language);
    }

    @Test
    void update_shouldUpdateOnlyCode() {
        UUID languageId = UUID.randomUUID();

        UpdateLanguageRequest request = new UpdateLanguageRequest(
                null,
                " HY "
        );

        Language language = new Language();
        language.setId(languageId);
        language.setCode("en");
        language.setName("English");

        LanguageResponse response = new LanguageResponse(
                languageId,
                "hy",
                "English",
                UUID.randomUUID(),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));
        when(languageRepository.existsByCodeIgnoreCaseAndIdNot("hy", languageId)).thenReturn(false);
        when(languageRepository.saveAndFlush(language)).thenReturn(language);
        when(languageMapper.toResponse(language)).thenReturn(response);

        LanguageResponse result = languageService.update(languageId, request);

        assertNotNull(result);
        assertEquals("hy", language.getCode());
        assertEquals("English", language.getName());

        verify(languageRepository).findById(languageId);
        verify(languageRepository).existsByCodeIgnoreCaseAndIdNot("hy", languageId);
        verify(languageRepository, never()).existsByNameIgnoreCaseAndIdNot(any(), any());
        verify(languageRepository).saveAndFlush(language);
        verify(languageMapper).toResponse(language);
    }

    @Test
    void update_shouldThrowBadRequest_whenNoFieldsProvided() {
        UUID languageId = UUID.randomUUID();

        UpdateLanguageRequest request = new UpdateLanguageRequest(
                null,
                null
        );

        Language language = new Language();
        language.setId(languageId);

        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> languageService.update(languageId, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("400 BAD_REQUEST \"At least one field must be provided\"", exception.getMessage());

        verify(languageRepository).findById(languageId);
        verify(languageRepository, never()).saveAndFlush(any());
    }

    @Test
    void update_shouldThrowBadRequest_whenNameIsBlank() {
        UUID languageId = UUID.randomUUID();

        UpdateLanguageRequest request = new UpdateLanguageRequest(
                "   ",
                null
        );

        Language language = new Language();
        language.setId(languageId);

        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> languageService.update(languageId, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("400 BAD_REQUEST \"Language name must not be blank\"", exception.getMessage());

        verify(languageRepository).findById(languageId);
        verify(languageRepository, never()).saveAndFlush(any());
    }

    @Test
    void update_shouldThrowBadRequest_whenCodeLengthIsInvalid() {
        UUID languageId = UUID.randomUUID();

        UpdateLanguageRequest request = new UpdateLanguageRequest(
                null,
                "eng"
        );

        Language language = new Language();
        language.setId(languageId);

        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> languageService.update(languageId, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("400 BAD_REQUEST \"Language code must contain exactly 2 characters\"", exception.getMessage());

        verify(languageRepository).findById(languageId);
        verify(languageRepository, never()).saveAndFlush(any());
    }

    @Test
    void update_shouldThrowConflict_whenCodeAlreadyExists() {
        UUID languageId = UUID.randomUUID();

        UpdateLanguageRequest request = new UpdateLanguageRequest(
                null,
                "hy"
        );

        Language language = new Language();
        language.setId(languageId);
        language.setCode("en");
        language.setName("English");

        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));
        when(languageRepository.existsByCodeIgnoreCaseAndIdNot("hy", languageId)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> languageService.update(languageId, request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("409 CONFLICT \"Language with this code already exists\"", exception.getMessage());

        verify(languageRepository).findById(languageId);
        verify(languageRepository).existsByCodeIgnoreCaseAndIdNot("hy", languageId);
        verify(languageRepository, never()).saveAndFlush(any());
    }

    @Test
    void update_shouldThrowConflict_whenNameAlreadyExists() {
        UUID languageId = UUID.randomUUID();

        UpdateLanguageRequest request = new UpdateLanguageRequest(
                "Armenian",
                null
        );

        Language language = new Language();
        language.setId(languageId);
        language.setCode("en");
        language.setName("English");

        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));
        when(languageRepository.existsByNameIgnoreCaseAndIdNot("Armenian", languageId)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> languageService.update(languageId, request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("409 CONFLICT \"Language with this name already exists\"", exception.getMessage());

        verify(languageRepository).findById(languageId);
        verify(languageRepository).existsByNameIgnoreCaseAndIdNot("Armenian", languageId);
        verify(languageRepository, never()).saveAndFlush(any());
    }

    @Test
    void update_shouldThrowConflict_whenSaveFailsByUniqueConstraint() {
        UUID languageId = UUID.randomUUID();

        UpdateLanguageRequest request = new UpdateLanguageRequest(
                "Armenian",
                "hy"
        );

        Language language = new Language();
        language.setId(languageId);
        language.setCode("en");
        language.setName("English");

        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));
        when(languageRepository.existsByCodeIgnoreCaseAndIdNot("hy", languageId)).thenReturn(false);
        when(languageRepository.existsByNameIgnoreCaseAndIdNot("Armenian", languageId)).thenReturn(false);
        when(languageRepository.saveAndFlush(language))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> languageService.update(languageId, request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("409 CONFLICT \"Language with this code or name already exists\"", exception.getMessage());

        verify(languageRepository).saveAndFlush(language);
        verify(languageMapper, never()).toResponse(any());
    }

    @Test
    void deleteLanguage_shouldDelete() {
        UUID languageId = UUID.randomUUID();

        Language language = new Language();
        language.setId(languageId);

        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));

        languageService.delete(languageId);

        verify(languageRepository).findById(languageId);
        verify(languageRepository).delete(language);
    }

    @Test
    void deleteLanguage_shouldThrowNotFound_whenDoesNotExist() {
        UUID languageId = UUID.randomUUID();

        when(languageRepository.findById(languageId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> languageService.delete(languageId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Language not found with id"));

        verify(languageRepository).findById(languageId);
        verify(languageRepository, never()).delete(any());
    }
}