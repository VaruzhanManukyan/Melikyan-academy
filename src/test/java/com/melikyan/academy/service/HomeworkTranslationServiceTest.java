package com.melikyan.academy.service;

import org.mockito.ArgumentCaptor;
import com.melikyan.academy.entity.User;
import org.springframework.http.HttpStatus;
import org.mockito.junit.jupiter.MockitoExtension;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.repository.LanguageRepository;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import com.melikyan.academy.dto.response.homeworkTranslation.HomeworkTranslationResponse;
import com.melikyan.academy.dto.request.homeworkTranslation.CreateHomeworkTranslationRequest;
import com.melikyan.academy.dto.request.homeworkTranslation.UpdateHomeworkTranslationRequest;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.time.OffsetDateTime;
import java.lang.reflect.RecordComponent;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HomeworkTranslationServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private HomeworkRepository homeworkRepository;

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private HomeworkTranslationMapper homeworkTranslationMapper;

    @Mock
    private HomeworkTranslationRepository homeworkTranslationRepository;

    @InjectMocks
    private HomeworkTranslationService homeworkTranslationService;

    private CreateHomeworkTranslationRequest createRequest(
            String code,
            String title,
            String description,
            UUID homeworkId,
            UUID createdById
    ) {
        Map<String, Object> values = new HashMap<>();
        values.put("code", code);
        values.put("title", title);
        values.put("description", description);
        values.put("homeworkId", homeworkId);
        values.put("createdById", createdById);

        return createRecord(CreateHomeworkTranslationRequest.class, values);
    }

    private UpdateHomeworkTranslationRequest updateRequest(
            String code,
            String title,
            String description
    ) {
        Map<String, Object> values = new HashMap<>();
        values.put("code", code);
        values.put("title", title);
        values.put("description", description);

        return createRecord(UpdateHomeworkTranslationRequest.class, values);
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
    void createHomeworkTranslation_shouldCreateAndReturnResponse() {
        UUID homeworkId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        CreateHomeworkTranslationRequest request = createRequest(
                " EN ",
                "  Homework title  ",
                "  Homework description  ",
                homeworkId,
                userId
        );

        Homework homework = new Homework();
        homework.setId(homeworkId);

        User user = new User();
        user.setId(userId);

        HomeworkTranslation savedHomeworkTranslation = new HomeworkTranslation();
        savedHomeworkTranslation.setId(translationId);
        savedHomeworkTranslation.setCode("en");
        savedHomeworkTranslation.setTitle("Homework title");
        savedHomeworkTranslation.setDescription("Homework description");
        savedHomeworkTranslation.setHomework(homework);
        savedHomeworkTranslation.setCreatedBy(user);

        HomeworkTranslationResponse response = new HomeworkTranslationResponse(
                translationId,
                "en",
                "Homework title",
                "Homework description",
                homeworkId,
                userId,
                now,
                now
        );

        when(homeworkRepository.findById(homeworkId)).thenReturn(Optional.of(homework));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(homeworkTranslationRepository.existsByHomeworkIdAndCodeIgnoreCase(homeworkId, "en"))
                .thenReturn(false);
        when(homeworkTranslationRepository.saveAndFlush(any(HomeworkTranslation.class)))
                .thenReturn(savedHomeworkTranslation);
        when(homeworkTranslationMapper.toResponse(savedHomeworkTranslation))
                .thenReturn(response);

        HomeworkTranslationResponse result = homeworkTranslationService.create(request);

        assertNotNull(result);
        assertEquals(response, result);

        ArgumentCaptor<HomeworkTranslation> captor = ArgumentCaptor.forClass(HomeworkTranslation.class);
        verify(homeworkTranslationRepository).saveAndFlush(captor.capture());

        HomeworkTranslation homeworkTranslation = captor.getValue();

        assertEquals("en", homeworkTranslation.getCode());
        assertEquals("Homework title", homeworkTranslation.getTitle());
        assertEquals("Homework description", homeworkTranslation.getDescription());
        assertEquals(homework, homeworkTranslation.getHomework());
        assertEquals(user, homeworkTranslation.getCreatedBy());

        verify(homeworkRepository).findById(homeworkId);
        verify(userRepository).findById(userId);
        verify(languageRepository).existsByCodeIgnoreCase("en");
        verify(homeworkTranslationRepository).existsByHomeworkIdAndCodeIgnoreCase(homeworkId, "en");
        verify(homeworkTranslationMapper).toResponse(savedHomeworkTranslation);
    }

    @Test
    void create_shouldThrowNotFound_whenHomeworkDoesNotExist() {
        UUID homeworkId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateHomeworkTranslationRequest request = createRequest(
                "en",
                "Homework title",
                "Description",
                homeworkId,
                userId
        );

        when(homeworkRepository.findById(homeworkId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> homeworkTranslationService.create(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Homework not found with id"));

        verify(homeworkRepository).findById(homeworkId);
        verify(userRepository, never()).findById(any());
        verify(languageRepository, never()).existsByCodeIgnoreCase(any());
        verify(homeworkTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowNotFound_whenUserDoesNotExist() {
        UUID homeworkId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateHomeworkTranslationRequest request = createRequest(
                "en",
                "Homework title",
                "Description",
                homeworkId,
                userId
        );

        Homework homework = new Homework();
        homework.setId(homeworkId);

        when(homeworkRepository.findById(homeworkId)).thenReturn(Optional.of(homework));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> homeworkTranslationService.create(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("User not found with id"));

        verify(homeworkRepository).findById(homeworkId);
        verify(userRepository).findById(userId);
        verify(languageRepository, never()).existsByCodeIgnoreCase(any());
        verify(homeworkTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowNotFound_whenLanguageDoesNotExist() {
        UUID homeworkId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateHomeworkTranslationRequest request = createRequest(
                "en",
                "Homework title",
                "Description",
                homeworkId,
                userId
        );

        Homework homework = new Homework();
        homework.setId(homeworkId);

        User user = new User();
        user.setId(userId);

        when(homeworkRepository.findById(homeworkId)).thenReturn(Optional.of(homework));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> homeworkTranslationService.create(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("404 NOT_FOUND \"Language not found with code: en\"", exception.getMessage());

        verify(languageRepository).existsByCodeIgnoreCase("en");
        verify(homeworkTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowConflict_whenTranslationAlreadyExistsForHomework() {
        UUID homeworkId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateHomeworkTranslationRequest request = createRequest(
                "en",
                "Homework title",
                "Description",
                homeworkId,
                userId
        );

        Homework homework = new Homework();
        homework.setId(homeworkId);

        User user = new User();
        user.setId(userId);

        when(homeworkRepository.findById(homeworkId)).thenReturn(Optional.of(homework));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(homeworkTranslationRepository.existsByHomeworkIdAndCodeIgnoreCase(homeworkId, "en"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> homeworkTranslationService.create(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(
                "409 CONFLICT \"Homework translation with this code already exists for this homework\"",
                exception.getMessage()
        );

        verify(homeworkTranslationRepository).existsByHomeworkIdAndCodeIgnoreCase(homeworkId, "en");
        verify(homeworkTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowBadRequest_whenCodeIsBlank() {
        UUID homeworkId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateHomeworkTranslationRequest request = createRequest(
                "   ",
                "Homework title",
                "Description",
                homeworkId,
                userId
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> homeworkTranslationService.create(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "400 BAD_REQUEST \"Homework translation code must not be blank\"",
                exception.getMessage()
        );

        verify(homeworkRepository, never()).findById(any());
        verify(homeworkTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowBadRequest_whenTitleIsBlank() {
        UUID homeworkId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateHomeworkTranslationRequest request = createRequest(
                "en",
                "   ",
                "Description",
                homeworkId,
                userId
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> homeworkTranslationService.create(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "400 BAD_REQUEST \"Homework translation title must not be blank\"",
                exception.getMessage()
        );

        verify(homeworkRepository, never()).findById(any());
        verify(homeworkTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowConflict_whenSaveFailsByUniqueConstraint() {
        UUID homeworkId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateHomeworkTranslationRequest request = createRequest(
                "en",
                "Homework title",
                "Description",
                homeworkId,
                userId
        );

        Homework homework = new Homework();
        homework.setId(homeworkId);

        User user = new User();
        user.setId(userId);

        when(homeworkRepository.findById(homeworkId)).thenReturn(Optional.of(homework));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(homeworkTranslationRepository.existsByHomeworkIdAndCodeIgnoreCase(homeworkId, "en"))
                .thenReturn(false);
        when(homeworkTranslationRepository.saveAndFlush(any(HomeworkTranslation.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> homeworkTranslationService.create(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(
                "409 CONFLICT \"Homework translation with this code already exists for this homework\"",
                exception.getMessage()
        );

        verify(homeworkTranslationRepository).saveAndFlush(any(HomeworkTranslation.class));
        verify(homeworkTranslationMapper, never()).toResponse(any());
    }

    @Test
    void getById_shouldReturnResponse() {
        UUID homeworkId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        Homework homework = new Homework();
        homework.setId(homeworkId);

        User user = new User();
        user.setId(userId);

        HomeworkTranslation homeworkTranslation = new HomeworkTranslation();
        homeworkTranslation.setId(translationId);
        homeworkTranslation.setCode("en");
        homeworkTranslation.setTitle("Homework title");
        homeworkTranslation.setDescription("Homework description");
        homeworkTranslation.setHomework(homework);
        homeworkTranslation.setCreatedBy(user);

        HomeworkTranslationResponse response = new HomeworkTranslationResponse(
                translationId,
                "en",
                "Homework title",
                "Homework description",
                homeworkId,
                userId,
                now,
                now
        );

        when(homeworkTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(homeworkTranslation));
        when(homeworkTranslationMapper.toResponse(homeworkTranslation))
                .thenReturn(response);

        HomeworkTranslationResponse result = homeworkTranslationService.getById(translationId);

        assertNotNull(result);
        assertEquals(response, result);

        verify(homeworkTranslationRepository).findById(translationId);
        verify(homeworkTranslationMapper).toResponse(homeworkTranslation);
    }

    @Test
    void getById_shouldThrowNotFound_whenDoesNotExist() {
        UUID translationId = UUID.randomUUID();

        when(homeworkTranslationRepository.findById(translationId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> homeworkTranslationService.getById(translationId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Homework translation not found with id"));

        verify(homeworkTranslationRepository).findById(translationId);
        verify(homeworkTranslationMapper, never()).toResponse(any());
    }

    @Test
    void getAll_shouldReturnResponseList() {
        HomeworkTranslation translation1 = new HomeworkTranslation();
        translation1.setId(UUID.randomUUID());
        translation1.setCode("en");

        HomeworkTranslation translation2 = new HomeworkTranslation();
        translation2.setId(UUID.randomUUID());
        translation2.setCode("hy");

        List<HomeworkTranslation> translations = List.of(translation1, translation2);

        List<HomeworkTranslationResponse> responses = List.of(
                mock(HomeworkTranslationResponse.class),
                mock(HomeworkTranslationResponse.class)
        );

        when(homeworkTranslationRepository.findAll()).thenReturn(translations);
        when(homeworkTranslationMapper.toResponseList(translations)).thenReturn(responses);

        List<HomeworkTranslationResponse> result = homeworkTranslationService.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(homeworkTranslationRepository).findAll();
        verify(homeworkTranslationMapper).toResponseList(translations);
    }

    @Test
    void getByCode_shouldReturnResponseList() {
        HomeworkTranslation translation = new HomeworkTranslation();
        translation.setId(UUID.randomUUID());
        translation.setCode("en");

        List<HomeworkTranslation> translations = List.of(translation);
        List<HomeworkTranslationResponse> responses = List.of(mock(HomeworkTranslationResponse.class));

        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(homeworkTranslationRepository.findByCodeIgnoreCase("en")).thenReturn(translations);
        when(homeworkTranslationMapper.toResponseList(translations)).thenReturn(responses);

        List<HomeworkTranslationResponse> result = homeworkTranslationService.getByCode(" EN ");

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(languageRepository).existsByCodeIgnoreCase("en");
        verify(homeworkTranslationRepository).findByCodeIgnoreCase("en");
        verify(homeworkTranslationMapper).toResponseList(translations);
    }

    @Test
    void getByHomeworkId_shouldReturnResponseList() {
        UUID homeworkId = UUID.randomUUID();

        Homework homework = new Homework();
        homework.setId(homeworkId);

        HomeworkTranslation translation = new HomeworkTranslation();
        translation.setId(UUID.randomUUID());
        translation.setHomework(homework);

        List<HomeworkTranslation> translations = List.of(translation);
        List<HomeworkTranslationResponse> responses = List.of(mock(HomeworkTranslationResponse.class));

        when(homeworkRepository.findById(homeworkId)).thenReturn(Optional.of(homework));
        when(homeworkTranslationRepository.findByHomeworkId(homeworkId)).thenReturn(translations);
        when(homeworkTranslationMapper.toResponseList(translations)).thenReturn(responses);

        List<HomeworkTranslationResponse> result = homeworkTranslationService.getByHomeworkId(homeworkId);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(homeworkRepository).findById(homeworkId);
        verify(homeworkTranslationRepository).findByHomeworkId(homeworkId);
        verify(homeworkTranslationMapper).toResponseList(translations);
    }

    @Test
    void getByHomeworkIdAndCode_shouldReturnResponse() {
        UUID homeworkId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        Homework homework = new Homework();
        homework.setId(homeworkId);

        User user = new User();
        user.setId(userId);

        HomeworkTranslation translation = new HomeworkTranslation();
        translation.setId(translationId);
        translation.setCode("en");
        translation.setHomework(homework);
        translation.setCreatedBy(user);

        HomeworkTranslationResponse response = new HomeworkTranslationResponse(
                translationId,
                "en",
                "Homework title",
                "Description",
                homeworkId,
                userId,
                now,
                now
        );

        when(homeworkRepository.findById(homeworkId)).thenReturn(Optional.of(homework));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(homeworkTranslationRepository.findByHomeworkIdAndCodeIgnoreCase(homeworkId, "en"))
                .thenReturn(Optional.of(translation));
        when(homeworkTranslationMapper.toResponse(translation)).thenReturn(response);

        HomeworkTranslationResponse result =
                homeworkTranslationService.getByHomeworkIdAndCode(homeworkId, " EN ");

        assertNotNull(result);
        assertEquals(response, result);

        verify(homeworkRepository).findById(homeworkId);
        verify(languageRepository).existsByCodeIgnoreCase("en");
        verify(homeworkTranslationRepository).findByHomeworkIdAndCodeIgnoreCase(homeworkId, "en");
        verify(homeworkTranslationMapper).toResponse(translation);
    }

    @Test
    void updateHomeworkTranslation_shouldUpdateAndReturnResponse() {
        UUID homeworkId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        UpdateHomeworkTranslationRequest request = updateRequest(
                " HY ",
                "  Տնային աշխատանք  ",
                "  Հայերեն նկարագրություն  "
        );

        Homework homework = new Homework();
        homework.setId(homeworkId);

        User user = new User();
        user.setId(userId);

        HomeworkTranslation translation = new HomeworkTranslation();
        translation.setId(translationId);
        translation.setCode("en");
        translation.setTitle("Homework title");
        translation.setDescription("Description");
        translation.setHomework(homework);
        translation.setCreatedBy(user);

        HomeworkTranslationResponse response = new HomeworkTranslationResponse(
                translationId,
                "hy",
                "Տնային աշխատանք",
                "Հայերեն նկարագրություն",
                homeworkId,
                userId,
                now,
                now
        );

        when(homeworkTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(translation));
        when(languageRepository.existsByCodeIgnoreCase("hy"))
                .thenReturn(true);
        when(homeworkTranslationRepository.existsByHomeworkIdAndCodeIgnoreCaseAndIdNot(
                homeworkId,
                "hy",
                translationId
        )).thenReturn(false);
        when(homeworkTranslationRepository.saveAndFlush(translation))
                .thenReturn(translation);
        when(homeworkTranslationMapper.toResponse(translation))
                .thenReturn(response);

        HomeworkTranslationResponse result = homeworkTranslationService.update(translationId, request);

        assertNotNull(result);
        assertEquals(response, result);
        assertEquals("hy", translation.getCode());
        assertEquals("Տնային աշխատանք", translation.getTitle());
        assertEquals("Հայերեն նկարագրություն", translation.getDescription());

        verify(homeworkTranslationRepository).findById(translationId);
        verify(languageRepository).existsByCodeIgnoreCase("hy");
        verify(homeworkTranslationRepository).existsByHomeworkIdAndCodeIgnoreCaseAndIdNot(
                homeworkId,
                "hy",
                translationId
        );
        verify(homeworkTranslationRepository).saveAndFlush(translation);
        verify(homeworkTranslationMapper).toResponse(translation);
    }

    @Test
    void update_shouldThrowBadRequest_whenNoFieldsProvided() {
        UUID translationId = UUID.randomUUID();

        UpdateHomeworkTranslationRequest request = updateRequest(
                null,
                null,
                null
        );

        HomeworkTranslation translation = new HomeworkTranslation();
        translation.setId(translationId);

        when(homeworkTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(translation));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> homeworkTranslationService.update(translationId, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("400 BAD_REQUEST \"At least one field must be provided\"", exception.getMessage());

        verify(homeworkTranslationRepository).findById(translationId);
        verify(homeworkTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void update_shouldThrowConflict_whenCodeAlreadyExistsForHomework() {
        UUID homeworkId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();

        UpdateHomeworkTranslationRequest request = updateRequest(
                "hy",
                null,
                null
        );

        Homework homework = new Homework();
        homework.setId(homeworkId);

        HomeworkTranslation translation = new HomeworkTranslation();
        translation.setId(translationId);
        translation.setHomework(homework);

        when(homeworkTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(translation));
        when(languageRepository.existsByCodeIgnoreCase("hy"))
                .thenReturn(true);
        when(homeworkTranslationRepository.existsByHomeworkIdAndCodeIgnoreCaseAndIdNot(
                homeworkId,
                "hy",
                translationId
        )).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> homeworkTranslationService.update(translationId, request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(
                "409 CONFLICT \"Homework translation with this code already exists for this homework\"",
                exception.getMessage()
        );

        verify(homeworkTranslationRepository).existsByHomeworkIdAndCodeIgnoreCaseAndIdNot(
                homeworkId,
                "hy",
                translationId
        );
        verify(homeworkTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void update_shouldThrowConflict_whenSaveFailsByUniqueConstraint() {
        UUID homeworkId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();

        UpdateHomeworkTranslationRequest request = updateRequest(
                "hy",
                "Armenian title",
                null
        );

        Homework homework = new Homework();
        homework.setId(homeworkId);

        HomeworkTranslation translation = new HomeworkTranslation();
        translation.setId(translationId);
        translation.setHomework(homework);

        when(homeworkTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(translation));
        when(languageRepository.existsByCodeIgnoreCase("hy"))
                .thenReturn(true);
        when(homeworkTranslationRepository.existsByHomeworkIdAndCodeIgnoreCaseAndIdNot(
                homeworkId,
                "hy",
                translationId
        )).thenReturn(false);
        when(homeworkTranslationRepository.saveAndFlush(translation))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> homeworkTranslationService.update(translationId, request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(
                "409 CONFLICT \"Homework translation with this code already exists for this homework\"",
                exception.getMessage()
        );

        verify(homeworkTranslationRepository).saveAndFlush(translation);
        verify(homeworkTranslationMapper, never()).toResponse(any());
    }

    @Test
    void deleteHomeworkTranslation_shouldDelete() {
        UUID translationId = UUID.randomUUID();

        HomeworkTranslation translation = new HomeworkTranslation();
        translation.setId(translationId);

        when(homeworkTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(translation));

        homeworkTranslationService.delete(translationId);

        verify(homeworkTranslationRepository).findById(translationId);
        verify(homeworkTranslationRepository).delete(translation);
    }

    @Test
    void deleteHomeworkTranslation_shouldThrowNotFound_whenDoesNotExist() {
        UUID translationId = UUID.randomUUID();

        when(homeworkTranslationRepository.findById(translationId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> homeworkTranslationService.delete(translationId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Homework translation not found with id"));

        verify(homeworkTranslationRepository).findById(translationId);
        verify(homeworkTranslationRepository, never()).delete(any());
    }
}