package com.melikyan.academy.service;

import org.mockito.ArgumentCaptor;
import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.Exam;
import com.melikyan.academy.entity.Course;
import com.melikyan.academy.entity.ContentItem;
import org.mockito.junit.jupiter.MockitoExtension;
import com.melikyan.academy.entity.enums.ContentItemType;
import com.melikyan.academy.entity.ContentItemTranslation;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.repository.ExamRepository;
import com.melikyan.academy.repository.CourseRepository;
import com.melikyan.academy.repository.LanguageRepository;
import com.melikyan.academy.mapper.ContentItemTranslationMapper;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import com.melikyan.academy.repository.ContentItemTranslationRepository;
import com.melikyan.academy.dto.response.contentItemTranslation.ContentItemTranslationResponse;
import com.melikyan.academy.dto.request.contentItemTranslation.CreateContentItemTranslationRequest;
import com.melikyan.academy.dto.request.contentItemTranslation.UpdateContentItemTranslationRequest;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.time.OffsetDateTime;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class ContentItemTranslationServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ExamRepository examRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private ContentItemTranslationMapper contentItemTranslationMapper;

    @Mock
    private ContentItemTranslationRepository contentItemTranslationRepository;

    @InjectMocks
    private ContentItemTranslationService contentItemTranslationService;

    private UUID userId;
    private UUID courseId;
    private UUID contentItemId;
    private UUID translationId;

    private User user;
    private Course course;
    private ContentItem contentItem;
    private ContentItemTranslation contentItemTranslation;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        courseId = UUID.randomUUID();
        contentItemId = UUID.randomUUID();
        translationId = UUID.randomUUID();

        user = new User();
        user.setId(userId);

        contentItem = new ContentItem();
        contentItem.setId(contentItemId);
        contentItem.setType(ContentItemType.COURSE);

        course = new Course();
        course.setId(courseId);
        course.setContentItem(contentItem);

        contentItemTranslation = new ContentItemTranslation();
        contentItemTranslation.setId(translationId);
        contentItemTranslation.setCode("en");
        contentItemTranslation.setTitle("Java Backend Fundamentals");
        contentItemTranslation.setDescription("Spring Boot course");
        contentItemTranslation.setContentItem(contentItem);
        contentItemTranslation.setCreatedBy(user);
    }

    @Test
    @DisplayName("createCourseTranslation -> creates course translation")
    void createCourseTranslation_ShouldCreateCourseTranslation() {
        CreateContentItemTranslationRequest request = new CreateContentItemTranslationRequest(
                "  Java Backend Fundamentals  ",
                "  Spring Boot course  ",
                " EN ",
                courseId,
                userId
        );

        ContentItemTranslationResponse response = new ContentItemTranslationResponse(
                translationId,
                "en",
                "Java Backend Fundamentals",
                "Spring Boot course",
                contentItemId,
                userId,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(contentItemTranslationRepository.existsByContentItemIdAndCodeIgnoreCase(
                contentItemId,
                "en"
        )).thenReturn(false);

        when(contentItemTranslationRepository.saveAndFlush(any(ContentItemTranslation.class)))
                .thenAnswer(invocation -> {
                    ContentItemTranslation saved = invocation.getArgument(0);
                    saved.setId(translationId);
                    return saved;
                });

        when(contentItemTranslationMapper.toResponse(any(ContentItemTranslation.class)))
                .thenReturn(response);

        ContentItemTranslationResponse result = contentItemTranslationService.createCourseTranslation(request);

        assertNotNull(result);
        assertEquals(response, result);

        ArgumentCaptor<ContentItemTranslation> captor =
                ArgumentCaptor.forClass(ContentItemTranslation.class);

        verify(contentItemTranslationRepository).saveAndFlush(captor.capture());

        ContentItemTranslation savedContentItemTranslation = captor.getValue();

        assertEquals("en", savedContentItemTranslation.getCode());
        assertEquals("Java Backend Fundamentals", savedContentItemTranslation.getTitle());
        assertEquals("Spring Boot course", savedContentItemTranslation.getDescription());
        assertEquals(contentItem, savedContentItemTranslation.getContentItem());
        assertEquals(user, savedContentItemTranslation.getCreatedBy());
    }

    @Test
    @DisplayName("createCourseTranslation -> saves null description when description is blank")
    void createCourseTranslation_ShouldSetDescriptionNull_WhenDescriptionIsBlank() {
        CreateContentItemTranslationRequest request = new CreateContentItemTranslationRequest(
                "Java Backend Fundamentals",
                "   ",
                "en",
                courseId,
                userId
        );

        ContentItemTranslationResponse response = new ContentItemTranslationResponse(
                translationId,
                "en",
                "Java Backend Fundamentals",
                null,
                contentItemId,
                userId,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(contentItemTranslationRepository.existsByContentItemIdAndCodeIgnoreCase(
                contentItemId,
                "en"
        )).thenReturn(false);

        when(contentItemTranslationRepository.saveAndFlush(any(ContentItemTranslation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(contentItemTranslationMapper.toResponse(any(ContentItemTranslation.class)))
                .thenReturn(response);

        ContentItemTranslationResponse result = contentItemTranslationService.createCourseTranslation(request);

        assertNotNull(result);
        assertEquals(response, result);

        ArgumentCaptor<ContentItemTranslation> captor =
                ArgumentCaptor.forClass(ContentItemTranslation.class);

        verify(contentItemTranslationRepository).saveAndFlush(captor.capture());

        assertNull(captor.getValue().getDescription());
    }

    @Test
    @DisplayName("createCourseTranslation -> throws not found when course does not exist")
    void createCourseTranslation_ShouldThrowNotFound_WhenCourseDoesNotExist() {
        CreateContentItemTranslationRequest request = new CreateContentItemTranslationRequest(
                "Java Backend Fundamentals",
                "Spring Boot course",
                "en",
                courseId,
                userId
        );

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> contentItemTranslationService.createCourseTranslation(request)
        );

        assertEquals(404, ex.getStatusCode().value());
        assertEquals("Course not found with id: " + courseId, ex.getReason());

        verify(contentItemTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("createCourseTranslation -> throws not found when language does not exist")
    void createCourseTranslation_ShouldThrowNotFound_WhenLanguageDoesNotExist() {
        CreateContentItemTranslationRequest request = new CreateContentItemTranslationRequest(
                "Java Backend Fundamentals",
                "Spring Boot course",
                "en",
                courseId,
                userId
        );

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> contentItemTranslationService.createCourseTranslation(request)
        );

        assertEquals(404, ex.getStatusCode().value());
        assertEquals("Language not found with code: en", ex.getReason());

        verify(contentItemTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("createCourseTranslation -> throws conflict when translation already exists")
    void createCourseTranslation_ShouldThrowConflict_WhenTranslationAlreadyExists() {
        CreateContentItemTranslationRequest request = new CreateContentItemTranslationRequest(
                "Java Backend Fundamentals",
                "Spring Boot course",
                "en",
                courseId,
                userId
        );

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(contentItemTranslationRepository.existsByContentItemIdAndCodeIgnoreCase(
                contentItemId,
                "en"
        )).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> contentItemTranslationService.createCourseTranslation(request)
        );

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("Course translation with this code already exists for this course", ex.getReason());

        verify(contentItemTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("createCourseTranslation -> throws conflict when database save fails")
    void createCourseTranslation_ShouldThrowConflict_WhenDatabaseSaveFails() {
        CreateContentItemTranslationRequest request = new CreateContentItemTranslationRequest(
                "Java Backend Fundamentals",
                "Spring Boot course",
                "en",
                courseId,
                userId
        );

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(contentItemTranslationRepository.existsByContentItemIdAndCodeIgnoreCase(
                contentItemId,
                "en"
        )).thenReturn(false);

        when(contentItemTranslationRepository.saveAndFlush(any(ContentItemTranslation.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate translation"));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> contentItemTranslationService.createCourseTranslation(request)
        );

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("Course translation with this code already exists for this course", ex.getReason());
    }

    @Test
    @DisplayName("getCourseTranslationById -> returns mapped course translation")
    void getCourseTranslationById_ShouldReturnMappedCourseTranslation() {
        ContentItemTranslationResponse response = new ContentItemTranslationResponse(
                translationId,
                "en",
                "Java Backend Fundamentals",
                "Spring Boot course",
                contentItemId,
                userId,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(contentItemTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(contentItemTranslation));
        when(contentItemTranslationMapper.toResponse(contentItemTranslation))
                .thenReturn(response);

        ContentItemTranslationResponse result =
                contentItemTranslationService.getCourseTranslationById(translationId);

        assertNotNull(result);
        assertEquals(response, result);
    }

    @Test
    @DisplayName("getCourseTranslationById -> throws not found when translation does not exist")
    void getCourseTranslationById_ShouldThrowNotFound_WhenTranslationDoesNotExist() {
        when(contentItemTranslationRepository.findById(translationId))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> contentItemTranslationService.getCourseTranslationById(translationId)
        );

        assertEquals(404, ex.getStatusCode().value());
        assertEquals("Content item translation not found with id: " + translationId, ex.getReason());
    }

    @Test
    @DisplayName("getCourseTranslationById -> throws not found when translation is not course")
    void getCourseTranslationById_ShouldThrowNotFound_WhenTranslationIsNotCourse() {
        contentItem.setType(ContentItemType.EXAM);

        when(contentItemTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(contentItemTranslation));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> contentItemTranslationService.getCourseTranslationById(translationId)
        );

        assertEquals(404, ex.getStatusCode().value());
        assertEquals("Content item translation not found with id: " + translationId, ex.getReason());
    }

    @Test
    @DisplayName("getAllCourseTranslations -> returns mapped course translations")
    void getAllCourseTranslations_ShouldReturnMappedCourseTranslations() {
        List<ContentItemTranslation> translations = List.of(contentItemTranslation);
        List<ContentItemTranslationResponse> responses = List.of(
                new ContentItemTranslationResponse(
                        translationId,
                        "en",
                        "Java Backend Fundamentals",
                        "Spring Boot course",
                        contentItemId,
                        userId,
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                )
        );

        when(contentItemTranslationRepository.findByContentItemType(ContentItemType.COURSE))
                .thenReturn(translations);
        when(contentItemTranslationMapper.toResponseList(translations))
                .thenReturn(responses);

        List<ContentItemTranslationResponse> result =
                contentItemTranslationService.getAllCourseTranslations();

        assertNotNull(result);
        assertEquals(responses, result);
    }

    @Test
    @DisplayName("getCourseTranslationsByCode -> normalizes code and returns mapped list")
    void getCourseTranslationsByCode_ShouldNormalizeCodeAndReturnMappedList() {
        List<ContentItemTranslation> translations = List.of(contentItemTranslation);
        List<ContentItemTranslationResponse> responses = List.of(
                new ContentItemTranslationResponse(
                        translationId,
                        "en",
                        "Java Backend Fundamentals",
                        "Spring Boot course",
                        contentItemId,
                        userId,
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                )
        );

        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(contentItemTranslationRepository.findByContentItemTypeAndCodeIgnoreCase(
                ContentItemType.COURSE,
                "en"
        )).thenReturn(translations);
        when(contentItemTranslationMapper.toResponseList(translations))
                .thenReturn(responses);

        List<ContentItemTranslationResponse> result =
                contentItemTranslationService.getCourseTranslationsByCode(" EN ");

        assertNotNull(result);
        assertEquals(responses, result);
    }

    @Test
    @DisplayName("getByCourseId -> returns course translations")
    void getByCourseId_ShouldReturnCourseTranslations() {
        List<ContentItemTranslation> translations = List.of(contentItemTranslation);
        List<ContentItemTranslationResponse> responses = List.of(
                new ContentItemTranslationResponse(
                        translationId,
                        "en",
                        "Java Backend Fundamentals",
                        "Spring Boot course",
                        contentItemId,
                        userId,
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                )
        );

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(contentItemTranslationRepository.findByContentItemId(contentItemId))
                .thenReturn(translations);
        when(contentItemTranslationMapper.toResponseList(translations))
                .thenReturn(responses);

        List<ContentItemTranslationResponse> result =
                contentItemTranslationService.getByCourseId(courseId);

        assertNotNull(result);
        assertEquals(responses, result);
    }

    @Test
    @DisplayName("getByCourseIdAndCode -> returns course translation")
    void getByCourseIdAndCode_ShouldReturnCourseTranslation() {
        ContentItemTranslationResponse response = new ContentItemTranslationResponse(
                translationId,
                "en",
                "Java Backend Fundamentals",
                "Spring Boot course",
                contentItemId,
                userId,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(contentItemTranslationRepository.findByContentItemIdAndCodeIgnoreCase(
                contentItemId,
                "en"
        )).thenReturn(Optional.of(contentItemTranslation));
        when(contentItemTranslationMapper.toResponse(contentItemTranslation))
                .thenReturn(response);

        ContentItemTranslationResponse result =
                contentItemTranslationService.getByCourseIdAndCode(courseId, " EN ");

        assertNotNull(result);
        assertEquals(response, result);
    }

    @Test
    @DisplayName("getByCourseIdAndCode -> throws not found when translation does not exist")
    void getByCourseIdAndCode_ShouldThrowNotFound_WhenTranslationDoesNotExist() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(contentItemTranslationRepository.findByContentItemIdAndCodeIgnoreCase(
                contentItemId,
                "en"
        )).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> contentItemTranslationService.getByCourseIdAndCode(courseId, "en")
        );

        assertEquals(404, ex.getStatusCode().value());
        assertEquals(
                "Course translation not found with course id: " + courseId + " and code: en",
                ex.getReason()
        );
    }

    @Test
    @DisplayName("updateCourseTranslation -> updates course translation")
    void updateCourseTranslation_ShouldUpdateCourseTranslation() {
        UpdateContentItemTranslationRequest request = new UpdateContentItemTranslationRequest(
                "  Advanced Java  ",
                "  Updated description  ",
                " HY "
        );

        ContentItemTranslationResponse response = new ContentItemTranslationResponse(
                translationId,
                "hy",
                "Advanced Java",
                "Updated description",
                contentItemId,
                userId,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(contentItemTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(contentItemTranslation));
        when(languageRepository.existsByCodeIgnoreCase("hy")).thenReturn(true);
        when(contentItemTranslationRepository.existsByContentItemIdAndCodeIgnoreCaseAndIdNot(
                contentItemId,
                "hy",
                translationId
        )).thenReturn(false);
        when(contentItemTranslationRepository.saveAndFlush(contentItemTranslation))
                .thenReturn(contentItemTranslation);
        when(contentItemTranslationMapper.toResponse(contentItemTranslation))
                .thenReturn(response);

        ContentItemTranslationResponse result =
                contentItemTranslationService.updateCourseTranslation(translationId, request);

        assertNotNull(result);
        assertEquals(response, result);
        assertEquals("hy", contentItemTranslation.getCode());
        assertEquals("Advanced Java", contentItemTranslation.getTitle());
        assertEquals("Updated description", contentItemTranslation.getDescription());
    }

    @Test
    @DisplayName("updateCourseTranslation -> throws bad request when nothing to update")
    void updateCourseTranslation_ShouldThrowBadRequest_WhenNothingToUpdate() {
        UpdateContentItemTranslationRequest request = new UpdateContentItemTranslationRequest(
                null,
                null,
                null
        );

        when(contentItemTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(contentItemTranslation));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> contentItemTranslationService.updateCourseTranslation(translationId, request)
        );

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("At least one field must be provided", ex.getReason());

        verify(contentItemTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("updateCourseTranslation -> throws conflict when code already exists")
    void updateCourseTranslation_ShouldThrowConflict_WhenCodeAlreadyExists() {
        UpdateContentItemTranslationRequest request = new UpdateContentItemTranslationRequest(
                null,
                null,
                "hy"
        );

        when(contentItemTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(contentItemTranslation));
        when(languageRepository.existsByCodeIgnoreCase("hy")).thenReturn(true);
        when(contentItemTranslationRepository.existsByContentItemIdAndCodeIgnoreCaseAndIdNot(
                contentItemId,
                "hy",
                translationId
        )).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> contentItemTranslationService.updateCourseTranslation(translationId, request)
        );

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("Course translation with this code already exists for this course", ex.getReason());

        verify(contentItemTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("updateCourseTranslation -> throws conflict when database save fails")
    void updateCourseTranslation_ShouldThrowConflict_WhenDatabaseSaveFails() {
        UpdateContentItemTranslationRequest request = new UpdateContentItemTranslationRequest(
                "Advanced Java",
                "Updated description",
                "hy"
        );

        when(contentItemTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(contentItemTranslation));
        when(languageRepository.existsByCodeIgnoreCase("hy")).thenReturn(true);
        when(contentItemTranslationRepository.existsByContentItemIdAndCodeIgnoreCaseAndIdNot(
                contentItemId,
                "hy",
                translationId
        )).thenReturn(false);
        when(contentItemTranslationRepository.saveAndFlush(contentItemTranslation))
                .thenThrow(new DataIntegrityViolationException("duplicate translation"));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> contentItemTranslationService.updateCourseTranslation(translationId, request)
        );

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("Course translation with this code already exists for this course", ex.getReason());
    }

    @Test
    @DisplayName("deleteCourseTranslation -> deletes course translation")
    void deleteCourseTranslation_ShouldDeleteCourseTranslation() {
        when(contentItemTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(contentItemTranslation));

        contentItemTranslationService.deleteCourseTranslation(translationId);

        verify(contentItemTranslationRepository).delete(contentItemTranslation);
    }

    @Test
    @DisplayName("deleteCourseTranslation -> throws not found when translation does not exist")
    void deleteCourseTranslation_ShouldThrowNotFound_WhenTranslationDoesNotExist() {
        when(contentItemTranslationRepository.findById(translationId))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> contentItemTranslationService.deleteCourseTranslation(translationId)
        );

        assertEquals(404, ex.getStatusCode().value());
        assertEquals("Content item translation not found with id: " + translationId, ex.getReason());

        verify(contentItemTranslationRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteCourseTranslation -> throws not found when translation is not course")
    void deleteCourseTranslation_ShouldThrowNotFound_WhenTranslationIsNotCourse() {
        contentItem.setType(ContentItemType.EXAM);

        when(contentItemTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(contentItemTranslation));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> contentItemTranslationService.deleteCourseTranslation(translationId)
        );

        assertEquals(404, ex.getStatusCode().value());
        assertEquals("Content item translation not found with id: " + translationId, ex.getReason());

        verify(contentItemTranslationRepository, never()).delete(any());
    }
}