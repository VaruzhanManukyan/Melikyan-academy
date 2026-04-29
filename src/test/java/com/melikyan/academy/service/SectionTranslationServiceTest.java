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
class SectionTranslationServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ExamSectionRepository examSectionRepository;

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private SectionTranslationMapper sectionTranslationMapper;

    @Mock
    private SectionTranslationRepository sectionTranslationRepository;

    @InjectMocks
    private SectionTranslationService sectionTranslationService;

    private CreateSectionTranslationRequest createRequest(
            String code,
            String title,
            String description,
            UUID examSectionId,
            UUID createdById
    ) {
        Map<String, Object> values = new HashMap<>();
        values.put("code", code);
        values.put("title", title);
        values.put("description", description);
        values.put("examSectionId", examSectionId);
        values.put("createdById", createdById);

        return createRecord(CreateSectionTranslationRequest.class, values);
    }

    private UpdateSectionTranslationRequest updateRequest(
            String code,
            String title,
            String description
    ) {
        Map<String, Object> values = new HashMap<>();
        values.put("code", code);
        values.put("title", title);
        values.put("description", description);

        return createRecord(UpdateSectionTranslationRequest.class, values);
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
    void createSectionTranslation_shouldCreateAndReturnResponse() {
        UUID examSectionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        CreateSectionTranslationRequest request = createRequest(
                " EN ",
                "  Section title  ",
                "  Section description  ",
                examSectionId,
                userId
        );

        ExamSection examSection = new ExamSection();
        examSection.setId(examSectionId);

        User user = new User();
        user.setId(userId);

        SectionTranslation savedSectionTranslation = new SectionTranslation();
        savedSectionTranslation.setId(translationId);
        savedSectionTranslation.setCode("en");
        savedSectionTranslation.setTitle("Section title");
        savedSectionTranslation.setDescription("Section description");
        savedSectionTranslation.setExamSection(examSection);
        savedSectionTranslation.setCreatedBy(user);

        SectionTranslationResponse response = new SectionTranslationResponse(
                translationId,
                "Section title",
                "Section description",
                "en",
                examSectionId,
                userId,
                now,
                now
        );

        when(examSectionRepository.findById(examSectionId)).thenReturn(Optional.of(examSection));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(sectionTranslationRepository.existsByExamSectionIdAndCodeIgnoreCase(examSectionId, "en"))
                .thenReturn(false);
        when(sectionTranslationRepository.saveAndFlush(any(SectionTranslation.class)))
                .thenReturn(savedSectionTranslation);
        when(sectionTranslationMapper.toResponse(savedSectionTranslation))
                .thenReturn(response);

        SectionTranslationResponse result = sectionTranslationService.create(request);

        assertNotNull(result);
        assertEquals(response, result);

        ArgumentCaptor<SectionTranslation> captor = ArgumentCaptor.forClass(SectionTranslation.class);
        verify(sectionTranslationRepository).saveAndFlush(captor.capture());

        SectionTranslation sectionTranslation = captor.getValue();

        assertEquals("en", sectionTranslation.getCode());
        assertEquals("Section title", sectionTranslation.getTitle());
        assertEquals("Section description", sectionTranslation.getDescription());
        assertEquals(examSection, sectionTranslation.getExamSection());
        assertEquals(user, sectionTranslation.getCreatedBy());

        verify(examSectionRepository).findById(examSectionId);
        verify(userRepository).findById(userId);
        verify(languageRepository).existsByCodeIgnoreCase("en");
        verify(sectionTranslationRepository).existsByExamSectionIdAndCodeIgnoreCase(examSectionId, "en");
        verify(sectionTranslationMapper).toResponse(savedSectionTranslation);
    }

    @Test
    void create_shouldThrowNotFound_whenExamSectionDoesNotExist() {
        UUID examSectionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateSectionTranslationRequest request = createRequest(
                "en",
                "Section title",
                "Description",
                examSectionId,
                userId
        );

        when(examSectionRepository.findById(examSectionId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> sectionTranslationService.create(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Exam section not found with id"));

        verify(examSectionRepository).findById(examSectionId);
        verify(userRepository, never()).findById(any());
        verify(languageRepository, never()).existsByCodeIgnoreCase(any());
        verify(sectionTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowNotFound_whenUserDoesNotExist() {
        UUID examSectionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateSectionTranslationRequest request = createRequest(
                "en",
                "Section title",
                "Description",
                examSectionId,
                userId
        );

        ExamSection examSection = new ExamSection();
        examSection.setId(examSectionId);

        when(examSectionRepository.findById(examSectionId)).thenReturn(Optional.of(examSection));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> sectionTranslationService.create(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("User not found with id"));

        verify(examSectionRepository).findById(examSectionId);
        verify(userRepository).findById(userId);
        verify(languageRepository, never()).existsByCodeIgnoreCase(any());
        verify(sectionTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowNotFound_whenLanguageDoesNotExist() {
        UUID examSectionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateSectionTranslationRequest request = createRequest(
                "en",
                "Section title",
                "Description",
                examSectionId,
                userId
        );

        ExamSection examSection = new ExamSection();
        examSection.setId(examSectionId);

        User user = new User();
        user.setId(userId);

        when(examSectionRepository.findById(examSectionId)).thenReturn(Optional.of(examSection));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> sectionTranslationService.create(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("404 NOT_FOUND \"Language not found with code: en\"", exception.getMessage());

        verify(languageRepository).existsByCodeIgnoreCase("en");
        verify(sectionTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowConflict_whenTranslationAlreadyExistsForExamSection() {
        UUID examSectionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateSectionTranslationRequest request = createRequest(
                "en",
                "Section title",
                "Description",
                examSectionId,
                userId
        );

        ExamSection examSection = new ExamSection();
        examSection.setId(examSectionId);

        User user = new User();
        user.setId(userId);

        when(examSectionRepository.findById(examSectionId)).thenReturn(Optional.of(examSection));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(sectionTranslationRepository.existsByExamSectionIdAndCodeIgnoreCase(examSectionId, "en"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> sectionTranslationService.create(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(
                "409 CONFLICT \"Section translation with this code already exists for this exam section\"",
                exception.getMessage()
        );

        verify(sectionTranslationRepository).existsByExamSectionIdAndCodeIgnoreCase(examSectionId, "en");
        verify(sectionTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowBadRequest_whenCodeIsBlank() {
        UUID examSectionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateSectionTranslationRequest request = createRequest(
                "   ",
                "Section title",
                "Description",
                examSectionId,
                userId
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> sectionTranslationService.create(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "400 BAD_REQUEST \"Section translation code must not be blank\"",
                exception.getMessage()
        );

        verify(examSectionRepository, never()).findById(any());
        verify(sectionTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowBadRequest_whenTitleIsBlank() {
        UUID examSectionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateSectionTranslationRequest request = createRequest(
                "en",
                "   ",
                "Description",
                examSectionId,
                userId
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> sectionTranslationService.create(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "400 BAD_REQUEST \"Section translation title must not be blank\"",
                exception.getMessage()
        );

        verify(examSectionRepository, never()).findById(any());
        verify(sectionTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowConflict_whenSaveFailsByUniqueConstraint() {
        UUID examSectionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateSectionTranslationRequest request = createRequest(
                "en",
                "Section title",
                "Description",
                examSectionId,
                userId
        );

        ExamSection examSection = new ExamSection();
        examSection.setId(examSectionId);

        User user = new User();
        user.setId(userId);

        when(examSectionRepository.findById(examSectionId)).thenReturn(Optional.of(examSection));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(sectionTranslationRepository.existsByExamSectionIdAndCodeIgnoreCase(examSectionId, "en"))
                .thenReturn(false);
        when(sectionTranslationRepository.saveAndFlush(any(SectionTranslation.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> sectionTranslationService.create(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(
                "409 CONFLICT \"Section translation with this code already exists for this exam section\"",
                exception.getMessage()
        );

        verify(sectionTranslationRepository).saveAndFlush(any(SectionTranslation.class));
        verify(sectionTranslationMapper, never()).toResponse(any());
    }

    @Test
    void getById_shouldReturnResponse() {
        UUID examSectionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        ExamSection examSection = new ExamSection();
        examSection.setId(examSectionId);

        User user = new User();
        user.setId(userId);

        SectionTranslation sectionTranslation = new SectionTranslation();
        sectionTranslation.setId(translationId);
        sectionTranslation.setCode("en");
        sectionTranslation.setTitle("Section title");
        sectionTranslation.setDescription("Section description");
        sectionTranslation.setExamSection(examSection);
        sectionTranslation.setCreatedBy(user);

        SectionTranslationResponse response = new SectionTranslationResponse(
                translationId,
                "Section title",
                "Section description",
                "en",
                examSectionId,
                userId,
                now,
                now
        );

        when(sectionTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(sectionTranslation));
        when(sectionTranslationMapper.toResponse(sectionTranslation))
                .thenReturn(response);

        SectionTranslationResponse result = sectionTranslationService.getById(translationId);

        assertNotNull(result);
        assertEquals(response, result);

        verify(sectionTranslationRepository).findById(translationId);
        verify(sectionTranslationMapper).toResponse(sectionTranslation);
    }

    @Test
    void getById_shouldThrowNotFound_whenDoesNotExist() {
        UUID translationId = UUID.randomUUID();

        when(sectionTranslationRepository.findById(translationId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> sectionTranslationService.getById(translationId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Section translation not found with id"));

        verify(sectionTranslationRepository).findById(translationId);
        verify(sectionTranslationMapper, never()).toResponse(any());
    }

    @Test
    void getAll_shouldReturnResponseList() {
        SectionTranslation translation1 = new SectionTranslation();
        translation1.setId(UUID.randomUUID());
        translation1.setCode("en");

        SectionTranslation translation2 = new SectionTranslation();
        translation2.setId(UUID.randomUUID());
        translation2.setCode("hy");

        List<SectionTranslation> translations = List.of(translation1, translation2);

        List<SectionTranslationResponse> responses = List.of(
                mock(SectionTranslationResponse.class),
                mock(SectionTranslationResponse.class)
        );

        when(sectionTranslationRepository.findAll()).thenReturn(translations);
        when(sectionTranslationMapper.toResponseList(translations)).thenReturn(responses);

        List<SectionTranslationResponse> result = sectionTranslationService.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(sectionTranslationRepository).findAll();
        verify(sectionTranslationMapper).toResponseList(translations);
    }

    @Test
    void getByCode_shouldReturnResponseList() {
        SectionTranslation translation = new SectionTranslation();
        translation.setId(UUID.randomUUID());
        translation.setCode("en");

        List<SectionTranslation> translations = List.of(translation);
        List<SectionTranslationResponse> responses = List.of(mock(SectionTranslationResponse.class));

        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(sectionTranslationRepository.findByCodeIgnoreCase("en")).thenReturn(translations);
        when(sectionTranslationMapper.toResponseList(translations)).thenReturn(responses);

        List<SectionTranslationResponse> result = sectionTranslationService.getByCode(" EN ");

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(languageRepository).existsByCodeIgnoreCase("en");
        verify(sectionTranslationRepository).findByCodeIgnoreCase("en");
        verify(sectionTranslationMapper).toResponseList(translations);
    }

    @Test
    void getByExamSectionId_shouldReturnResponseList() {
        UUID examSectionId = UUID.randomUUID();

        ExamSection examSection = new ExamSection();
        examSection.setId(examSectionId);

        SectionTranslation translation = new SectionTranslation();
        translation.setId(UUID.randomUUID());
        translation.setExamSection(examSection);

        List<SectionTranslation> translations = List.of(translation);
        List<SectionTranslationResponse> responses = List.of(mock(SectionTranslationResponse.class));

        when(examSectionRepository.findById(examSectionId)).thenReturn(Optional.of(examSection));
        when(sectionTranslationRepository.findByExamSectionId(examSectionId)).thenReturn(translations);
        when(sectionTranslationMapper.toResponseList(translations)).thenReturn(responses);

        List<SectionTranslationResponse> result = sectionTranslationService.getByExamSectionId(examSectionId);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(examSectionRepository).findById(examSectionId);
        verify(sectionTranslationRepository).findByExamSectionId(examSectionId);
        verify(sectionTranslationMapper).toResponseList(translations);
    }

    @Test
    void getByExamSectionIdAndCode_shouldReturnResponse() {
        UUID examSectionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        ExamSection examSection = new ExamSection();
        examSection.setId(examSectionId);

        User user = new User();
        user.setId(userId);

        SectionTranslation translation = new SectionTranslation();
        translation.setId(translationId);
        translation.setCode("en");
        translation.setExamSection(examSection);
        translation.setCreatedBy(user);

        SectionTranslationResponse response = new SectionTranslationResponse(
                translationId,
                "Section title",
                "Description",
                "en",
                examSectionId,
                userId,
                now,
                now
        );

        when(examSectionRepository.findById(examSectionId)).thenReturn(Optional.of(examSection));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(sectionTranslationRepository.findByExamSectionIdAndCodeIgnoreCase(examSectionId, "en"))
                .thenReturn(Optional.of(translation));
        when(sectionTranslationMapper.toResponse(translation)).thenReturn(response);

        SectionTranslationResponse result =
                sectionTranslationService.getByExamSectionIdAndCode(examSectionId, " EN ");

        assertNotNull(result);
        assertEquals(response, result);

        verify(examSectionRepository).findById(examSectionId);
        verify(languageRepository).existsByCodeIgnoreCase("en");
        verify(sectionTranslationRepository).findByExamSectionIdAndCodeIgnoreCase(examSectionId, "en");
        verify(sectionTranslationMapper).toResponse(translation);
    }

    @Test
    void updateSectionTranslation_shouldUpdateAndReturnResponse() {
        UUID examSectionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        UpdateSectionTranslationRequest request = updateRequest(
                " HY ",
                "  Բաժին  ",
                "  Հայերեն նկարագրություն  "
        );

        ExamSection examSection = new ExamSection();
        examSection.setId(examSectionId);

        User user = new User();
        user.setId(userId);

        SectionTranslation translation = new SectionTranslation();
        translation.setId(translationId);
        translation.setCode("en");
        translation.setTitle("Section title");
        translation.setDescription("Description");
        translation.setExamSection(examSection);
        translation.setCreatedBy(user);

        SectionTranslationResponse response = new SectionTranslationResponse(
                translationId,
                "Բաժին",
                "Հայերեն նկարագրություն",
                "hy",
                examSectionId,
                userId,
                now,
                now
        );

        when(sectionTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(translation));
        when(languageRepository.existsByCodeIgnoreCase("hy"))
                .thenReturn(true);
        when(sectionTranslationRepository.existsByExamSectionIdAndCodeIgnoreCaseAndIdNot(
                examSectionId,
                "hy",
                translationId
        )).thenReturn(false);
        when(sectionTranslationRepository.saveAndFlush(translation))
                .thenReturn(translation);
        when(sectionTranslationMapper.toResponse(translation))
                .thenReturn(response);

        SectionTranslationResponse result = sectionTranslationService.update(translationId, request);

        assertNotNull(result);
        assertEquals(response, result);
        assertEquals("hy", translation.getCode());
        assertEquals("Բաժին", translation.getTitle());
        assertEquals("Հայերեն նկարագրություն", translation.getDescription());

        verify(sectionTranslationRepository).findById(translationId);
        verify(languageRepository).existsByCodeIgnoreCase("hy");
        verify(sectionTranslationRepository).existsByExamSectionIdAndCodeIgnoreCaseAndIdNot(
                examSectionId,
                "hy",
                translationId
        );
        verify(sectionTranslationRepository).saveAndFlush(translation);
        verify(sectionTranslationMapper).toResponse(translation);
    }

    @Test
    void update_shouldThrowBadRequest_whenNoFieldsProvided() {
        UUID translationId = UUID.randomUUID();

        UpdateSectionTranslationRequest request = updateRequest(
                null,
                null,
                null
        );

        SectionTranslation translation = new SectionTranslation();
        translation.setId(translationId);

        when(sectionTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(translation));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> sectionTranslationService.update(translationId, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("400 BAD_REQUEST \"At least one field must be provided\"", exception.getMessage());

        verify(sectionTranslationRepository).findById(translationId);
        verify(sectionTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void update_shouldThrowConflict_whenCodeAlreadyExistsForExamSection() {
        UUID examSectionId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();

        UpdateSectionTranslationRequest request = updateRequest(
                "hy",
                null,
                null
        );

        ExamSection examSection = new ExamSection();
        examSection.setId(examSectionId);

        SectionTranslation translation = new SectionTranslation();
        translation.setId(translationId);
        translation.setExamSection(examSection);

        when(sectionTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(translation));
        when(languageRepository.existsByCodeIgnoreCase("hy"))
                .thenReturn(true);
        when(sectionTranslationRepository.existsByExamSectionIdAndCodeIgnoreCaseAndIdNot(
                examSectionId,
                "hy",
                translationId
        )).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> sectionTranslationService.update(translationId, request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(
                "409 CONFLICT \"Section translation with this code already exists for this exam section\"",
                exception.getMessage()
        );

        verify(sectionTranslationRepository).existsByExamSectionIdAndCodeIgnoreCaseAndIdNot(
                examSectionId,
                "hy",
                translationId
        );
        verify(sectionTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void update_shouldThrowConflict_whenSaveFailsByUniqueConstraint() {
        UUID examSectionId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();

        UpdateSectionTranslationRequest request = updateRequest(
                "hy",
                "Armenian title",
                null
        );

        ExamSection examSection = new ExamSection();
        examSection.setId(examSectionId);

        SectionTranslation translation = new SectionTranslation();
        translation.setId(translationId);
        translation.setExamSection(examSection);

        when(sectionTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(translation));
        when(languageRepository.existsByCodeIgnoreCase("hy"))
                .thenReturn(true);
        when(sectionTranslationRepository.existsByExamSectionIdAndCodeIgnoreCaseAndIdNot(
                examSectionId,
                "hy",
                translationId
        )).thenReturn(false);
        when(sectionTranslationRepository.saveAndFlush(translation))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> sectionTranslationService.update(translationId, request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(
                "409 CONFLICT \"Section translation with this code already exists for this exam section\"",
                exception.getMessage()
        );

        verify(sectionTranslationRepository).saveAndFlush(translation);
        verify(sectionTranslationMapper, never()).toResponse(any());
    }

    @Test
    void deleteSectionTranslation_shouldDelete() {
        UUID translationId = UUID.randomUUID();

        SectionTranslation translation = new SectionTranslation();
        translation.setId(translationId);

        when(sectionTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(translation));

        sectionTranslationService.delete(translationId);

        verify(sectionTranslationRepository).findById(translationId);
        verify(sectionTranslationRepository).delete(translation);
    }

    @Test
    void deleteSectionTranslation_shouldThrowNotFound_whenDoesNotExist() {
        UUID translationId = UUID.randomUUID();

        when(sectionTranslationRepository.findById(translationId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> sectionTranslationService.delete(translationId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Section translation not found with id"));

        verify(sectionTranslationRepository).findById(translationId);
        verify(sectionTranslationRepository, never()).delete(any());
    }
}
