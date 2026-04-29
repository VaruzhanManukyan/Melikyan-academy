package com.melikyan.academy.service;

import com.melikyan.academy.dto.request.lessonTranslation.CreateLessonTranslationRequest;
import com.melikyan.academy.dto.request.lessonTranslation.UpdateLessonTranslationRequest;
import com.melikyan.academy.dto.response.lessonTranslation.LessonTranslationResponse;
import com.melikyan.academy.entity.Lesson;
import com.melikyan.academy.entity.LessonTranslation;
import com.melikyan.academy.entity.User;
import com.melikyan.academy.mapper.LessonTranslationMapper;
import com.melikyan.academy.repository.LanguageRepository;
import com.melikyan.academy.repository.LessonRepository;
import com.melikyan.academy.repository.LessonTranslationRepository;
import com.melikyan.academy.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.RecordComponent;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonTranslationServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private LessonTranslationMapper lessonTranslationMapper;

    @Mock
    private LessonTranslationRepository lessonTranslationRepository;

    @InjectMocks
    private LessonTranslationService lessonTranslationService;

    private CreateLessonTranslationRequest createRequest(
            String code,
            String title,
            String description,
            String valueUrl,
            UUID lessonId,
            UUID createdById
    ) {
        Map<String, Object> values = new HashMap<>();
        values.put("code", code);
        values.put("title", title);
        values.put("description", description);
        values.put("valueUrl", valueUrl);
        values.put("lessonId", lessonId);
        values.put("createdById", createdById);

        return createRecord(CreateLessonTranslationRequest.class, values);
    }

    private UpdateLessonTranslationRequest updateRequest(
            String code,
            String title,
            String description,
            String valueUrl
    ) {
        Map<String, Object> values = new HashMap<>();
        values.put("code", code);
        values.put("title", title);
        values.put("description", description);
        values.put("valueUrl", valueUrl);

        return createRecord(UpdateLessonTranslationRequest.class, values);
    }

    private <T> T createRecord(Class<T> type, Map<String, Object> values) {
        try {
            RecordComponent[] components = type.getRecordComponents();

            Class<?>[] parameterTypes = Arrays.stream(components)
                    .map(RecordComponent::getType)
                    .toArray(Class<?>[]::new);

            Object[] args = Arrays.stream(components)
                    .map(component -> values.get(component.getName()))
                    .toArray();

            return type.getDeclaredConstructor(parameterTypes).newInstance(args);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Test
    void createLessonTranslation_shouldCreateAndReturnResponse() {
        UUID lessonId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        CreateLessonTranslationRequest request = createRequest(
                " EN ",
                "  Introduction  ",
                "  Introduction lesson  ",
                "  https://example.com/video-en  ",
                lessonId,
                userId
        );

        Lesson lesson = new Lesson();
        lesson.setId(lessonId);

        User user = new User();
        user.setId(userId);

        LessonTranslation savedLessonTranslation = new LessonTranslation();
        savedLessonTranslation.setId(translationId);
        savedLessonTranslation.setCode("en");
        savedLessonTranslation.setTitle("Introduction");
        savedLessonTranslation.setDescription("Introduction lesson");
        savedLessonTranslation.setValueUrl("https://example.com/video-en");
        savedLessonTranslation.setLesson(lesson);
        savedLessonTranslation.setCreatedBy(user);

        LessonTranslationResponse response = new LessonTranslationResponse(
                translationId,
                "en",
                "Introduction",
                "Introduction lesson",
                "https://example.com/video-en",
                lessonId,
                userId,
                now,
                now
        );

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(lessonTranslationRepository.existsByLessonIdAndCodeIgnoreCase(lessonId, "en"))
                .thenReturn(false);
        when(lessonTranslationRepository.saveAndFlush(any(LessonTranslation.class)))
                .thenReturn(savedLessonTranslation);
        when(lessonTranslationMapper.toResponse(savedLessonTranslation))
                .thenReturn(response);

        LessonTranslationResponse result = lessonTranslationService.create(request);

        assertNotNull(result);
        assertEquals(response, result);

        ArgumentCaptor<LessonTranslation> captor = ArgumentCaptor.forClass(LessonTranslation.class);
        verify(lessonTranslationRepository).saveAndFlush(captor.capture());

        LessonTranslation lessonTranslation = captor.getValue();

        assertEquals("en", lessonTranslation.getCode());
        assertEquals("Introduction", lessonTranslation.getTitle());
        assertEquals("Introduction lesson", lessonTranslation.getDescription());
        assertEquals("https://example.com/video-en", lessonTranslation.getValueUrl());
        assertEquals(lesson, lessonTranslation.getLesson());
        assertEquals(user, lessonTranslation.getCreatedBy());

        verify(lessonRepository).findById(lessonId);
        verify(userRepository).findById(userId);
        verify(languageRepository).existsByCodeIgnoreCase("en");
        verify(lessonTranslationRepository).existsByLessonIdAndCodeIgnoreCase(lessonId, "en");
        verify(lessonTranslationMapper).toResponse(savedLessonTranslation);
    }

    @Test
    void create_shouldThrowNotFound_whenLessonDoesNotExist() {
        UUID lessonId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateLessonTranslationRequest request = createRequest(
                "en",
                "Introduction",
                "Description",
                "https://example.com/video-en",
                lessonId,
                userId
        );

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> lessonTranslationService.create(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Lesson not found with id"));

        verify(lessonRepository).findById(lessonId);
        verify(userRepository, never()).findById(any());
        verify(languageRepository, never()).existsByCodeIgnoreCase(any());
        verify(lessonTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowNotFound_whenUserDoesNotExist() {
        UUID lessonId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateLessonTranslationRequest request = createRequest(
                "en",
                "Introduction",
                "Description",
                "https://example.com/video-en",
                lessonId,
                userId
        );

        Lesson lesson = new Lesson();
        lesson.setId(lessonId);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> lessonTranslationService.create(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("User not found with id"));

        verify(lessonRepository).findById(lessonId);
        verify(userRepository).findById(userId);
        verify(languageRepository, never()).existsByCodeIgnoreCase(any());
        verify(lessonTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowNotFound_whenLanguageDoesNotExist() {
        UUID lessonId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateLessonTranslationRequest request = createRequest(
                "en",
                "Introduction",
                "Description",
                "https://example.com/video-en",
                lessonId,
                userId
        );

        Lesson lesson = new Lesson();
        lesson.setId(lessonId);

        User user = new User();
        user.setId(userId);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> lessonTranslationService.create(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("404 NOT_FOUND \"Language not found with code: en\"", exception.getMessage());

        verify(languageRepository).existsByCodeIgnoreCase("en");
        verify(lessonTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowConflict_whenTranslationAlreadyExistsForLesson() {
        UUID lessonId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateLessonTranslationRequest request = createRequest(
                "en",
                "Introduction",
                "Description",
                "https://example.com/video-en",
                lessonId,
                userId
        );

        Lesson lesson = new Lesson();
        lesson.setId(lessonId);

        User user = new User();
        user.setId(userId);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(lessonTranslationRepository.existsByLessonIdAndCodeIgnoreCase(lessonId, "en"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> lessonTranslationService.create(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(
                "409 CONFLICT \"Lesson translation with this code already exists for this lesson\"",
                exception.getMessage()
        );

        verify(lessonTranslationRepository).existsByLessonIdAndCodeIgnoreCase(lessonId, "en");
        verify(lessonTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowBadRequest_whenCodeIsBlank() {
        UUID lessonId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateLessonTranslationRequest request = createRequest(
                "   ",
                "Introduction",
                "Description",
                "https://example.com/video-en",
                lessonId,
                userId
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> lessonTranslationService.create(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "400 BAD_REQUEST \"Lesson translation code must not be blank\"",
                exception.getMessage()
        );

        verify(lessonRepository, never()).findById(any());
        verify(lessonTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowBadRequest_whenTitleIsBlank() {
        UUID lessonId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateLessonTranslationRequest request = createRequest(
                "en",
                "   ",
                "Description",
                "https://example.com/video-en",
                lessonId,
                userId
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> lessonTranslationService.create(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "400 BAD_REQUEST \"Lesson translation title must not be blank\"",
                exception.getMessage()
        );

        verify(lessonRepository, never()).findById(any());
        verify(lessonTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowConflict_whenSaveFailsByUniqueConstraint() {
        UUID lessonId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateLessonTranslationRequest request = createRequest(
                "en",
                "Introduction",
                "Description",
                "https://example.com/video-en",
                lessonId,
                userId
        );

        Lesson lesson = new Lesson();
        lesson.setId(lessonId);

        User user = new User();
        user.setId(userId);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(lessonTranslationRepository.existsByLessonIdAndCodeIgnoreCase(lessonId, "en"))
                .thenReturn(false);
        when(lessonTranslationRepository.saveAndFlush(any(LessonTranslation.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> lessonTranslationService.create(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(
                "409 CONFLICT \"Lesson translation with this code already exists for this lesson\"",
                exception.getMessage()
        );

        verify(lessonTranslationRepository).saveAndFlush(any(LessonTranslation.class));
        verify(lessonTranslationMapper, never()).toResponse(any());
    }

    @Test
    void getById_shouldReturnResponse() {
        UUID lessonId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        Lesson lesson = new Lesson();
        lesson.setId(lessonId);

        User user = new User();
        user.setId(userId);

        LessonTranslation lessonTranslation = new LessonTranslation();
        lessonTranslation.setId(translationId);
        lessonTranslation.setCode("en");
        lessonTranslation.setTitle("Introduction");
        lessonTranslation.setValueUrl("https://example.com/video-en");
        lessonTranslation.setLesson(lesson);
        lessonTranslation.setCreatedBy(user);

        LessonTranslationResponse response = new LessonTranslationResponse(
                translationId,
                "en",
                "Introduction",
                null,
                "https://example.com/video-en",
                lessonId,
                userId,
                now,
                now
        );

        when(lessonTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(lessonTranslation));
        when(lessonTranslationMapper.toResponse(lessonTranslation))
                .thenReturn(response);

        LessonTranslationResponse result = lessonTranslationService.getById(translationId);

        assertNotNull(result);
        assertEquals(response, result);

        verify(lessonTranslationRepository).findById(translationId);
        verify(lessonTranslationMapper).toResponse(lessonTranslation);
    }

    @Test
    void getById_shouldThrowNotFound_whenDoesNotExist() {
        UUID translationId = UUID.randomUUID();

        when(lessonTranslationRepository.findById(translationId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> lessonTranslationService.getById(translationId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Lesson translation not found with id"));

        verify(lessonTranslationRepository).findById(translationId);
        verify(lessonTranslationMapper, never()).toResponse(any());
    }

    @Test
    void getAll_shouldReturnResponseList() {
        LessonTranslation translation1 = new LessonTranslation();
        translation1.setId(UUID.randomUUID());
        translation1.setCode("en");

        LessonTranslation translation2 = new LessonTranslation();
        translation2.setId(UUID.randomUUID());
        translation2.setCode("hy");

        List<LessonTranslation> translations = List.of(translation1, translation2);

        List<LessonTranslationResponse> responses = List.of(
                mock(LessonTranslationResponse.class),
                mock(LessonTranslationResponse.class)
        );

        when(lessonTranslationRepository.findAll()).thenReturn(translations);
        when(lessonTranslationMapper.toResponseList(translations)).thenReturn(responses);

        List<LessonTranslationResponse> result = lessonTranslationService.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(lessonTranslationRepository).findAll();
        verify(lessonTranslationMapper).toResponseList(translations);
    }

    @Test
    void getByCode_shouldReturnResponseList() {
        LessonTranslation translation = new LessonTranslation();
        translation.setId(UUID.randomUUID());
        translation.setCode("en");

        List<LessonTranslation> translations = List.of(translation);
        List<LessonTranslationResponse> responses = List.of(mock(LessonTranslationResponse.class));

        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(lessonTranslationRepository.findByCodeIgnoreCase("en")).thenReturn(translations);
        when(lessonTranslationMapper.toResponseList(translations)).thenReturn(responses);

        List<LessonTranslationResponse> result = lessonTranslationService.getByCode(" EN ");

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(languageRepository).existsByCodeIgnoreCase("en");
        verify(lessonTranslationRepository).findByCodeIgnoreCase("en");
        verify(lessonTranslationMapper).toResponseList(translations);
    }

    @Test
    void getByLessonId_shouldReturnResponseList() {
        UUID lessonId = UUID.randomUUID();

        Lesson lesson = new Lesson();
        lesson.setId(lessonId);

        LessonTranslation translation = new LessonTranslation();
        translation.setId(UUID.randomUUID());
        translation.setLesson(lesson);

        List<LessonTranslation> translations = List.of(translation);
        List<LessonTranslationResponse> responses = List.of(mock(LessonTranslationResponse.class));

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonTranslationRepository.findByLessonId(lessonId)).thenReturn(translations);
        when(lessonTranslationMapper.toResponseList(translations)).thenReturn(responses);

        List<LessonTranslationResponse> result = lessonTranslationService.getByLessonId(lessonId);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(lessonRepository).findById(lessonId);
        verify(lessonTranslationRepository).findByLessonId(lessonId);
        verify(lessonTranslationMapper).toResponseList(translations);
    }

    @Test
    void getByLessonIdAndCode_shouldReturnResponse() {
        UUID lessonId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        Lesson lesson = new Lesson();
        lesson.setId(lessonId);

        User user = new User();
        user.setId(userId);

        LessonTranslation translation = new LessonTranslation();
        translation.setId(translationId);
        translation.setCode("en");
        translation.setLesson(lesson);
        translation.setCreatedBy(user);

        LessonTranslationResponse response = new LessonTranslationResponse(
                translationId,
                "en",
                "Introduction",
                "Description",
                "https://example.com/video-en",
                lessonId,
                userId,
                now,
                now
        );

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(lessonTranslationRepository.findByLessonIdAndCodeIgnoreCase(lessonId, "en"))
                .thenReturn(Optional.of(translation));
        when(lessonTranslationMapper.toResponse(translation)).thenReturn(response);

        LessonTranslationResponse result =
                lessonTranslationService.getByLessonIdAndCode(lessonId, " EN ");

        assertNotNull(result);
        assertEquals(response, result);

        verify(lessonRepository).findById(lessonId);
        verify(languageRepository).existsByCodeIgnoreCase("en");
        verify(lessonTranslationRepository).findByLessonIdAndCodeIgnoreCase(lessonId, "en");
        verify(lessonTranslationMapper).toResponse(translation);
    }

    @Test
    void updateLessonTranslation_shouldUpdateAndReturnResponse() {
        UUID lessonId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        UpdateLessonTranslationRequest request = updateRequest(
                " HY ",
                "  Ներածություն  ",
                "  Հայերեն նկարագրություն  ",
                "  https://example.com/video-hy  "
        );

        Lesson lesson = new Lesson();
        lesson.setId(lessonId);

        User user = new User();
        user.setId(userId);

        LessonTranslation translation = new LessonTranslation();
        translation.setId(translationId);
        translation.setCode("en");
        translation.setTitle("Introduction");
        translation.setDescription("Description");
        translation.setValueUrl("https://example.com/video-en");
        translation.setLesson(lesson);
        translation.setCreatedBy(user);

        LessonTranslationResponse response = new LessonTranslationResponse(
                translationId,
                "hy",
                "Ներածություն",
                "Հայերեն նկարագրություն",
                "https://example.com/video-hy",
                lessonId,
                userId,
                now,
                now
        );

        when(lessonTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(translation));
        when(languageRepository.existsByCodeIgnoreCase("hy"))
                .thenReturn(true);
        when(lessonTranslationRepository.existsByLessonIdAndCodeIgnoreCaseAndIdNot(
                lessonId,
                "hy",
                translationId
        )).thenReturn(false);
        when(lessonTranslationRepository.saveAndFlush(translation))
                .thenReturn(translation);
        when(lessonTranslationMapper.toResponse(translation))
                .thenReturn(response);

        LessonTranslationResponse result = lessonTranslationService.update(translationId, request);

        assertNotNull(result);
        assertEquals(response, result);
        assertEquals("hy", translation.getCode());
        assertEquals("Ներածություն", translation.getTitle());
        assertEquals("Հայերեն նկարագրություն", translation.getDescription());
        assertEquals("https://example.com/video-hy", translation.getValueUrl());

        verify(lessonTranslationRepository).findById(translationId);
        verify(languageRepository).existsByCodeIgnoreCase("hy");
        verify(lessonTranslationRepository).existsByLessonIdAndCodeIgnoreCaseAndIdNot(
                lessonId,
                "hy",
                translationId
        );
        verify(lessonTranslationRepository).saveAndFlush(translation);
        verify(lessonTranslationMapper).toResponse(translation);
    }

    @Test
    void update_shouldThrowBadRequest_whenNoFieldsProvided() {
        UUID translationId = UUID.randomUUID();

        UpdateLessonTranslationRequest request = updateRequest(
                null,
                null,
                null,
                null
        );

        LessonTranslation translation = new LessonTranslation();
        translation.setId(translationId);

        when(lessonTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(translation));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> lessonTranslationService.update(translationId, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("400 BAD_REQUEST \"At least one field must be provided\"", exception.getMessage());

        verify(lessonTranslationRepository).findById(translationId);
        verify(lessonTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void update_shouldThrowConflict_whenCodeAlreadyExistsForLesson() {
        UUID lessonId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();

        UpdateLessonTranslationRequest request = updateRequest(
                "hy",
                null,
                null,
                null
        );

        Lesson lesson = new Lesson();
        lesson.setId(lessonId);

        LessonTranslation translation = new LessonTranslation();
        translation.setId(translationId);
        translation.setLesson(lesson);

        when(lessonTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(translation));
        when(languageRepository.existsByCodeIgnoreCase("hy"))
                .thenReturn(true);
        when(lessonTranslationRepository.existsByLessonIdAndCodeIgnoreCaseAndIdNot(
                lessonId,
                "hy",
                translationId
        )).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> lessonTranslationService.update(translationId, request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(
                "409 CONFLICT \"Lesson translation with this code already exists for this lesson\"",
                exception.getMessage()
        );

        verify(lessonTranslationRepository).existsByLessonIdAndCodeIgnoreCaseAndIdNot(
                lessonId,
                "hy",
                translationId
        );
        verify(lessonTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void update_shouldThrowConflict_whenSaveFailsByUniqueConstraint() {
        UUID lessonId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();

        UpdateLessonTranslationRequest request = updateRequest(
                "hy",
                "Armenian title",
                null,
                null
        );

        Lesson lesson = new Lesson();
        lesson.setId(lessonId);

        LessonTranslation translation = new LessonTranslation();
        translation.setId(translationId);
        translation.setLesson(lesson);

        when(lessonTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(translation));
        when(languageRepository.existsByCodeIgnoreCase("hy"))
                .thenReturn(true);
        when(lessonTranslationRepository.existsByLessonIdAndCodeIgnoreCaseAndIdNot(
                lessonId,
                "hy",
                translationId
        )).thenReturn(false);
        when(lessonTranslationRepository.saveAndFlush(translation))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> lessonTranslationService.update(translationId, request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(
                "409 CONFLICT \"Lesson translation with this code already exists for this lesson\"",
                exception.getMessage()
        );

        verify(lessonTranslationRepository).saveAndFlush(translation);
        verify(lessonTranslationMapper, never()).toResponse(any());
    }

    @Test
    void deleteLessonTranslation_shouldDelete() {
        UUID translationId = UUID.randomUUID();

        LessonTranslation translation = new LessonTranslation();
        translation.setId(translationId);

        when(lessonTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(translation));

        lessonTranslationService.delete(translationId);

        verify(lessonTranslationRepository).findById(translationId);
        verify(lessonTranslationRepository).delete(translation);
    }

    @Test
    void deleteLessonTranslation_shouldThrowNotFound_whenDoesNotExist() {
        UUID translationId = UUID.randomUUID();

        when(lessonTranslationRepository.findById(translationId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> lessonTranslationService.delete(translationId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Lesson translation not found with id"));

        verify(lessonTranslationRepository).findById(translationId);
        verify(lessonTranslationRepository, never()).delete(any());
    }
}