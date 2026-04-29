package com.melikyan.academy.service;

import org.mockito.ArgumentCaptor;
import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.Exam;
import com.melikyan.academy.entity.Course;
import com.melikyan.academy.entity.ContentItem;
import org.mockito.junit.jupiter.MockitoExtension;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.repository.ExamRepository;
import com.melikyan.academy.repository.CourseRepository;
import com.melikyan.academy.entity.enums.ContentItemType;
import com.melikyan.academy.entity.ContentItemTranslation;
import com.melikyan.academy.repository.LanguageRepository;
import com.melikyan.academy.repository.ContentItemRepository;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import com.melikyan.academy.mapper.ContentItemTranslationMapper;
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
    private ContentItemRepository contentItemRepository;

    @Mock
    private ContentItemTranslationMapper contentItemTranslationMapper;

    @Mock
    private ContentItemTranslationRepository contentItemTranslationRepository;

    @InjectMocks
    private ContentItemTranslationService contentItemTranslationService;

    private UUID userId;
    private UUID courseId;
    private UUID examId;
    private UUID courseContentItemId;
    private UUID examContentItemId;
    private UUID courseTranslationId;
    private UUID examTranslationId;

    private User user;
    private Course course;
    private Exam exam;
    private ContentItem courseContentItem;
    private ContentItem examContentItem;
    private ContentItemTranslation courseTranslation;
    private ContentItemTranslation examTranslation;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        courseId = UUID.randomUUID();
        examId = UUID.randomUUID();
        courseContentItemId = UUID.randomUUID();
        examContentItemId = UUID.randomUUID();
        courseTranslationId = UUID.randomUUID();
        examTranslationId = UUID.randomUUID();

        user = new User();
        user.setId(userId);

        courseContentItem = new ContentItem();
        courseContentItem.setId(courseContentItemId);
        courseContentItem.setType(ContentItemType.COURSE);

        examContentItem = new ContentItem();
        examContentItem.setId(examContentItemId);
        examContentItem.setType(ContentItemType.EXAM);

        course = new Course();
        course.setId(courseId);
        course.setContentItem(courseContentItem);

        exam = new Exam();
        exam.setId(examId);
        exam.setContentItem(examContentItem);

        courseTranslation = new ContentItemTranslation();
        courseTranslation.setId(courseTranslationId);
        courseTranslation.setCode("en");
        courseTranslation.setTitle("Java Backend Fundamentals");
        courseTranslation.setDescription("Spring Boot course");
        courseTranslation.setContentItem(courseContentItem);
        courseTranslation.setCreatedBy(user);

        examTranslation = new ContentItemTranslation();
        examTranslation.setId(examTranslationId);
        examTranslation.setCode("en");
        examTranslation.setTitle("Java Backend Exam");
        examTranslation.setDescription("Spring Boot exam");
        examTranslation.setContentItem(examContentItem);
        examTranslation.setCreatedBy(user);
    }

    @Test
    @DisplayName("createCourseTranslation -> creates course translation")
    void createCourseTranslation_ShouldCreateCourseTranslation() {
        CreateContentItemTranslationRequest request = new CreateContentItemTranslationRequest(
                "  Java Backend Fundamentals  ",
                "  Spring Boot course  ",
                " EN ",
                courseContentItemId,
                userId
        );

        ContentItemTranslationResponse response = new ContentItemTranslationResponse(
                courseTranslationId,
                "en",
                "Java Backend Fundamentals",
                "Spring Boot course",
                courseContentItemId,
                userId,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(contentItemRepository.findById(courseContentItemId)).thenReturn(Optional.of(courseContentItem));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(contentItemTranslationRepository.existsByContentItemIdAndCodeIgnoreCase(
                courseContentItemId,
                "en"
        )).thenReturn(false);

        when(contentItemTranslationRepository.saveAndFlush(any(ContentItemTranslation.class)))
                .thenAnswer(invocation -> {
                    ContentItemTranslation saved = invocation.getArgument(0);
                    saved.setId(courseTranslationId);
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
        assertEquals(courseContentItem, savedContentItemTranslation.getContentItem());
        assertEquals(user, savedContentItemTranslation.getCreatedBy());
    }

    @Test
    @DisplayName("createCourseTranslation -> saves null description when description is blank")
    void createCourseTranslation_ShouldSetDescriptionNull_WhenDescriptionIsBlank() {
        CreateContentItemTranslationRequest request = new CreateContentItemTranslationRequest(
                "Java Backend Fundamentals",
                "   ",
                "en",
                courseContentItemId,
                userId
        );

        ContentItemTranslationResponse response = new ContentItemTranslationResponse(
                courseTranslationId,
                "en",
                "Java Backend Fundamentals",
                null,
                courseContentItemId,
                userId,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(contentItemRepository.findById(courseContentItemId)).thenReturn(Optional.of(courseContentItem));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(contentItemTranslationRepository.existsByContentItemIdAndCodeIgnoreCase(
                courseContentItemId,
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
    @DisplayName("createCourseTranslation -> throws not found when content item does not exist")
    void createCourseTranslation_ShouldThrowNotFound_WhenContentItemDoesNotExist() {
        CreateContentItemTranslationRequest request = new CreateContentItemTranslationRequest(
                "Java Backend Fundamentals",
                "Spring Boot course",
                "en",
                courseContentItemId,
                userId
        );

        when(contentItemRepository.findById(courseContentItemId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> contentItemTranslationService.createCourseTranslation(request)
        );

        assertEquals(404, ex.getStatusCode().value());
        assertEquals("Content item not found with id: " + courseContentItemId, ex.getReason());

        verify(contentItemTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("createCourseTranslation -> throws bad request when content item is not course")
    void createCourseTranslation_ShouldThrowBadRequest_WhenContentItemIsNotCourse() {
        CreateContentItemTranslationRequest request = new CreateContentItemTranslationRequest(
                "Java Backend Fundamentals",
                "Spring Boot course",
                "en",
                examContentItemId,
                userId
        );

        when(contentItemRepository.findById(examContentItemId)).thenReturn(Optional.of(examContentItem));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> contentItemTranslationService.createCourseTranslation(request)
        );

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Content item type must be COURSE", ex.getReason());

        verify(contentItemTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("createCourseTranslation -> throws not found when language does not exist")
    void createCourseTranslation_ShouldThrowNotFound_WhenLanguageDoesNotExist() {
        CreateContentItemTranslationRequest request = new CreateContentItemTranslationRequest(
                "Java Backend Fundamentals",
                "Spring Boot course",
                "en",
                courseContentItemId,
                userId
        );

        when(contentItemRepository.findById(courseContentItemId)).thenReturn(Optional.of(courseContentItem));
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
                courseContentItemId,
                userId
        );

        when(contentItemRepository.findById(courseContentItemId)).thenReturn(Optional.of(courseContentItem));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(contentItemTranslationRepository.existsByContentItemIdAndCodeIgnoreCase(
                courseContentItemId,
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
                courseContentItemId,
                userId
        );

        when(contentItemRepository.findById(courseContentItemId)).thenReturn(Optional.of(courseContentItem));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(contentItemTranslationRepository.existsByContentItemIdAndCodeIgnoreCase(
                courseContentItemId,
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
    @DisplayName("createExamTranslation -> creates exam translation")
    void createExamTranslation_ShouldCreateExamTranslation() {
        CreateContentItemTranslationRequest request = new CreateContentItemTranslationRequest(
                "  Java Backend Exam  ",
                "  Spring Boot exam  ",
                " EN ",
                examContentItemId,
                userId
        );

        ContentItemTranslationResponse response = new ContentItemTranslationResponse(
                examTranslationId,
                "en",
                "Java Backend Exam",
                "Spring Boot exam",
                examContentItemId,
                userId,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(contentItemRepository.findById(examContentItemId)).thenReturn(Optional.of(examContentItem));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(contentItemTranslationRepository.existsByContentItemIdAndCodeIgnoreCase(
                examContentItemId,
                "en"
        )).thenReturn(false);

        when(contentItemTranslationRepository.saveAndFlush(any(ContentItemTranslation.class)))
                .thenAnswer(invocation -> {
                    ContentItemTranslation saved = invocation.getArgument(0);
                    saved.setId(examTranslationId);
                    return saved;
                });

        when(contentItemTranslationMapper.toResponse(any(ContentItemTranslation.class)))
                .thenReturn(response);

        ContentItemTranslationResponse result = contentItemTranslationService.createExamTranslation(request);

        assertNotNull(result);
        assertEquals(response, result);

        ArgumentCaptor<ContentItemTranslation> captor =
                ArgumentCaptor.forClass(ContentItemTranslation.class);

        verify(contentItemTranslationRepository).saveAndFlush(captor.capture());

        ContentItemTranslation savedContentItemTranslation = captor.getValue();

        assertEquals("en", savedContentItemTranslation.getCode());
        assertEquals("Java Backend Exam", savedContentItemTranslation.getTitle());
        assertEquals("Spring Boot exam", savedContentItemTranslation.getDescription());
        assertEquals(examContentItem, savedContentItemTranslation.getContentItem());
        assertEquals(user, savedContentItemTranslation.getCreatedBy());
    }

    @Test
    @DisplayName("createExamTranslation -> throws bad request when content item is not exam")
    void createExamTranslation_ShouldThrowBadRequest_WhenContentItemIsNotExam() {
        CreateContentItemTranslationRequest request = new CreateContentItemTranslationRequest(
                "Java Backend Exam",
                "Spring Boot exam",
                "en",
                courseContentItemId,
                userId
        );

        when(contentItemRepository.findById(courseContentItemId)).thenReturn(Optional.of(courseContentItem));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> contentItemTranslationService.createExamTranslation(request)
        );

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Content item type must be EXAM", ex.getReason());

        verify(contentItemTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("createExamTranslation -> throws conflict when translation already exists")
    void createExamTranslation_ShouldThrowConflict_WhenTranslationAlreadyExists() {
        CreateContentItemTranslationRequest request = new CreateContentItemTranslationRequest(
                "Java Backend Exam",
                "Spring Boot exam",
                "en",
                examContentItemId,
                userId
        );

        when(contentItemRepository.findById(examContentItemId)).thenReturn(Optional.of(examContentItem));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(contentItemTranslationRepository.existsByContentItemIdAndCodeIgnoreCase(
                examContentItemId,
                "en"
        )).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> contentItemTranslationService.createExamTranslation(request)
        );

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("Exam translation with this code already exists for this exam", ex.getReason());

        verify(contentItemTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("getCourseTranslationById -> returns mapped course translation")
    void getCourseTranslationById_ShouldReturnMappedCourseTranslation() {
        ContentItemTranslationResponse response = new ContentItemTranslationResponse(
                courseTranslationId,
                "en",
                "Java Backend Fundamentals",
                "Spring Boot course",
                courseContentItemId,
                userId,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(contentItemTranslationRepository.findById(courseTranslationId))
                .thenReturn(Optional.of(courseTranslation));
        when(contentItemTranslationMapper.toResponse(courseTranslation))
                .thenReturn(response);

        ContentItemTranslationResponse result =
                contentItemTranslationService.getCourseTranslationById(courseTranslationId);

        assertNotNull(result);
        assertEquals(response, result);
    }

    @Test
    @DisplayName("getExamTranslationById -> returns mapped exam translation")
    void getExamTranslationById_ShouldReturnMappedExamTranslation() {
        ContentItemTranslationResponse response = new ContentItemTranslationResponse(
                examTranslationId,
                "en",
                "Java Backend Exam",
                "Spring Boot exam",
                examContentItemId,
                userId,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(contentItemTranslationRepository.findById(examTranslationId))
                .thenReturn(Optional.of(examTranslation));
        when(contentItemTranslationMapper.toResponse(examTranslation))
                .thenReturn(response);

        ContentItemTranslationResponse result =
                contentItemTranslationService.getExamTranslationById(examTranslationId);

        assertNotNull(result);
        assertEquals(response, result);
    }

    @Test
    @DisplayName("getCourseTranslationById -> throws not found when translation does not exist")
    void getCourseTranslationById_ShouldThrowNotFound_WhenTranslationDoesNotExist() {
        when(contentItemTranslationRepository.findById(courseTranslationId))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> contentItemTranslationService.getCourseTranslationById(courseTranslationId)
        );

        assertEquals(404, ex.getStatusCode().value());
        assertEquals("Content item translation not found with id: " + courseTranslationId, ex.getReason());
    }

    @Test
    @DisplayName("getCourseTranslationById -> throws not found when translation is not course")
    void getCourseTranslationById_ShouldThrowNotFound_WhenTranslationIsNotCourse() {
        when(contentItemTranslationRepository.findById(examTranslationId))
                .thenReturn(Optional.of(examTranslation));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> contentItemTranslationService.getCourseTranslationById(examTranslationId)
        );

        assertEquals(404, ex.getStatusCode().value());
        assertEquals("Content item translation not found with id: " + examTranslationId, ex.getReason());
    }

    @Test
    @DisplayName("getAllCourseTranslations -> returns mapped course translations")
    void getAllCourseTranslations_ShouldReturnMappedCourseTranslations() {
        List<ContentItemTranslation> translations = List.of(courseTranslation);
        List<ContentItemTranslationResponse> responses = List.of(
                new ContentItemTranslationResponse(
                        courseTranslationId,
                        "en",
                        "Java Backend Fundamentals",
                        "Spring Boot course",
                        courseContentItemId,
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
    @DisplayName("getAllExamTranslations -> returns mapped exam translations")
    void getAllExamTranslations_ShouldReturnMappedExamTranslations() {
        List<ContentItemTranslation> translations = List.of(examTranslation);
        List<ContentItemTranslationResponse> responses = List.of(
                new ContentItemTranslationResponse(
                        examTranslationId,
                        "en",
                        "Java Backend Exam",
                        "Spring Boot exam",
                        examContentItemId,
                        userId,
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                )
        );

        when(contentItemTranslationRepository.findByContentItemType(ContentItemType.EXAM))
                .thenReturn(translations);
        when(contentItemTranslationMapper.toResponseList(translations))
                .thenReturn(responses);

        List<ContentItemTranslationResponse> result =
                contentItemTranslationService.getAllExamTranslations();

        assertNotNull(result);
        assertEquals(responses, result);
    }

    @Test
    @DisplayName("getCourseTranslationsByCode -> normalizes code and returns mapped list")
    void getCourseTranslationsByCode_ShouldNormalizeCodeAndReturnMappedList() {
        List<ContentItemTranslation> translations = List.of(courseTranslation);
        List<ContentItemTranslationResponse> responses = List.of(
                new ContentItemTranslationResponse(
                        courseTranslationId,
                        "en",
                        "Java Backend Fundamentals",
                        "Spring Boot course",
                        courseContentItemId,
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
    @DisplayName("getExamTranslationsByCode -> normalizes code and returns mapped list")
    void getExamTranslationsByCode_ShouldNormalizeCodeAndReturnMappedList() {
        List<ContentItemTranslation> translations = List.of(examTranslation);
        List<ContentItemTranslationResponse> responses = List.of(
                new ContentItemTranslationResponse(
                        examTranslationId,
                        "en",
                        "Java Backend Exam",
                        "Spring Boot exam",
                        examContentItemId,
                        userId,
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                )
        );

        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(contentItemTranslationRepository.findByContentItemTypeAndCodeIgnoreCase(
                ContentItemType.EXAM,
                "en"
        )).thenReturn(translations);
        when(contentItemTranslationMapper.toResponseList(translations))
                .thenReturn(responses);

        List<ContentItemTranslationResponse> result =
                contentItemTranslationService.getExamTranslationsByCode(" EN ");

        assertNotNull(result);
        assertEquals(responses, result);
    }

    @Test
    @DisplayName("getByCourseId -> returns course translations")
    void getByCourseId_ShouldReturnCourseTranslations() {
        List<ContentItemTranslation> translations = List.of(courseTranslation);
        List<ContentItemTranslationResponse> responses = List.of(
                new ContentItemTranslationResponse(
                        courseTranslationId,
                        "en",
                        "Java Backend Fundamentals",
                        "Spring Boot course",
                        courseContentItemId,
                        userId,
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                )
        );

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(contentItemTranslationRepository.findByContentItemId(courseContentItemId))
                .thenReturn(translations);
        when(contentItemTranslationMapper.toResponseList(translations))
                .thenReturn(responses);

        List<ContentItemTranslationResponse> result =
                contentItemTranslationService.getByCourseId(courseId);

        assertNotNull(result);
        assertEquals(responses, result);
    }

    @Test
    @DisplayName("getByExamId -> returns exam translations")
    void getByExamId_ShouldReturnExamTranslations() {
        List<ContentItemTranslation> translations = List.of(examTranslation);
        List<ContentItemTranslationResponse> responses = List.of(
                new ContentItemTranslationResponse(
                        examTranslationId,
                        "en",
                        "Java Backend Exam",
                        "Spring Boot exam",
                        examContentItemId,
                        userId,
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                )
        );

        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
        when(contentItemTranslationRepository.findByContentItemId(examContentItemId))
                .thenReturn(translations);
        when(contentItemTranslationMapper.toResponseList(translations))
                .thenReturn(responses);

        List<ContentItemTranslationResponse> result =
                contentItemTranslationService.getByExamId(examId);

        assertNotNull(result);
        assertEquals(responses, result);
    }

    @Test
    @DisplayName("getByCourseIdAndCode -> returns course translation")
    void getByCourseIdAndCode_ShouldReturnCourseTranslation() {
        ContentItemTranslationResponse response = new ContentItemTranslationResponse(
                courseTranslationId,
                "en",
                "Java Backend Fundamentals",
                "Spring Boot course",
                courseContentItemId,
                userId,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(contentItemTranslationRepository.findByContentItemIdAndCodeIgnoreCase(
                courseContentItemId,
                "en"
        )).thenReturn(Optional.of(courseTranslation));
        when(contentItemTranslationMapper.toResponse(courseTranslation))
                .thenReturn(response);

        ContentItemTranslationResponse result =
                contentItemTranslationService.getByCourseIdAndCode(courseId, " EN ");

        assertNotNull(result);
        assertEquals(response, result);
    }

    @Test
    @DisplayName("getByExamIdAndCode -> returns exam translation")
    void getByExamIdAndCode_ShouldReturnExamTranslation() {
        ContentItemTranslationResponse response = new ContentItemTranslationResponse(
                examTranslationId,
                "en",
                "Java Backend Exam",
                "Spring Boot exam",
                examContentItemId,
                userId,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(contentItemTranslationRepository.findByContentItemIdAndCodeIgnoreCase(
                examContentItemId,
                "en"
        )).thenReturn(Optional.of(examTranslation));
        when(contentItemTranslationMapper.toResponse(examTranslation))
                .thenReturn(response);

        ContentItemTranslationResponse result =
                contentItemTranslationService.getByExamIdAndCode(examId, " EN ");

        assertNotNull(result);
        assertEquals(response, result);
    }

    @Test
    @DisplayName("getByCourseIdAndCode -> throws not found when translation does not exist")
    void getByCourseIdAndCode_ShouldThrowNotFound_WhenTranslationDoesNotExist() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(contentItemTranslationRepository.findByContentItemIdAndCodeIgnoreCase(
                courseContentItemId,
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
    @DisplayName("getByExamIdAndCode -> throws not found when translation does not exist")
    void getByExamIdAndCode_ShouldThrowNotFound_WhenTranslationDoesNotExist() {
        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(contentItemTranslationRepository.findByContentItemIdAndCodeIgnoreCase(
                examContentItemId,
                "en"
        )).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> contentItemTranslationService.getByExamIdAndCode(examId, "en")
        );

        assertEquals(404, ex.getStatusCode().value());
        assertEquals(
                "Exam translation not found with exam id: " + examId + " and code: en",
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
                courseTranslationId,
                "hy",
                "Advanced Java",
                "Updated description",
                courseContentItemId,
                userId,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(contentItemTranslationRepository.findById(courseTranslationId))
                .thenReturn(Optional.of(courseTranslation));
        when(languageRepository.existsByCodeIgnoreCase("hy")).thenReturn(true);
        when(contentItemTranslationRepository.existsByContentItemIdAndCodeIgnoreCaseAndIdNot(
                courseContentItemId,
                "hy",
                courseTranslationId
        )).thenReturn(false);
        when(contentItemTranslationRepository.saveAndFlush(courseTranslation))
                .thenReturn(courseTranslation);
        when(contentItemTranslationMapper.toResponse(courseTranslation))
                .thenReturn(response);

        ContentItemTranslationResponse result =
                contentItemTranslationService.updateCourseTranslation(courseTranslationId, request);

        assertNotNull(result);
        assertEquals(response, result);
        assertEquals("hy", courseTranslation.getCode());
        assertEquals("Advanced Java", courseTranslation.getTitle());
        assertEquals("Updated description", courseTranslation.getDescription());
    }

    @Test
    @DisplayName("updateExamTranslation -> updates exam translation")
    void updateExamTranslation_ShouldUpdateExamTranslation() {
        UpdateContentItemTranslationRequest request = new UpdateContentItemTranslationRequest(
                "  Advanced Exam  ",
                "  Updated exam description  ",
                " HY "
        );

        ContentItemTranslationResponse response = new ContentItemTranslationResponse(
                examTranslationId,
                "hy",
                "Advanced Exam",
                "Updated exam description",
                examContentItemId,
                userId,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(contentItemTranslationRepository.findById(examTranslationId))
                .thenReturn(Optional.of(examTranslation));
        when(languageRepository.existsByCodeIgnoreCase("hy")).thenReturn(true);
        when(contentItemTranslationRepository.existsByContentItemIdAndCodeIgnoreCaseAndIdNot(
                examContentItemId,
                "hy",
                examTranslationId
        )).thenReturn(false);
        when(contentItemTranslationRepository.saveAndFlush(examTranslation))
                .thenReturn(examTranslation);
        when(contentItemTranslationMapper.toResponse(examTranslation))
                .thenReturn(response);

        ContentItemTranslationResponse result =
                contentItemTranslationService.updateExamTranslation(examTranslationId, request);

        assertNotNull(result);
        assertEquals(response, result);
        assertEquals("hy", examTranslation.getCode());
        assertEquals("Advanced Exam", examTranslation.getTitle());
        assertEquals("Updated exam description", examTranslation.getDescription());
    }

    @Test
    @DisplayName("updateCourseTranslation -> throws bad request when nothing to update")
    void updateCourseTranslation_ShouldThrowBadRequest_WhenNothingToUpdate() {
        UpdateContentItemTranslationRequest request = new UpdateContentItemTranslationRequest(
                null,
                null,
                null
        );

        when(contentItemTranslationRepository.findById(courseTranslationId))
                .thenReturn(Optional.of(courseTranslation));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> contentItemTranslationService.updateCourseTranslation(courseTranslationId, request)
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

        when(contentItemTranslationRepository.findById(courseTranslationId))
                .thenReturn(Optional.of(courseTranslation));
        when(languageRepository.existsByCodeIgnoreCase("hy")).thenReturn(true);
        when(contentItemTranslationRepository.existsByContentItemIdAndCodeIgnoreCaseAndIdNot(
                courseContentItemId,
                "hy",
                courseTranslationId
        )).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> contentItemTranslationService.updateCourseTranslation(courseTranslationId, request)
        );

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("Course translation with this code already exists for this course", ex.getReason());

        verify(contentItemTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("updateExamTranslation -> throws conflict when code already exists")
    void updateExamTranslation_ShouldThrowConflict_WhenCodeAlreadyExists() {
        UpdateContentItemTranslationRequest request = new UpdateContentItemTranslationRequest(
                null,
                null,
                "hy"
        );

        when(contentItemTranslationRepository.findById(examTranslationId))
                .thenReturn(Optional.of(examTranslation));
        when(languageRepository.existsByCodeIgnoreCase("hy")).thenReturn(true);
        when(contentItemTranslationRepository.existsByContentItemIdAndCodeIgnoreCaseAndIdNot(
                examContentItemId,
                "hy",
                examTranslationId
        )).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> contentItemTranslationService.updateExamTranslation(examTranslationId, request)
        );

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("Exam translation with this code already exists for this exam", ex.getReason());

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

        when(contentItemTranslationRepository.findById(courseTranslationId))
                .thenReturn(Optional.of(courseTranslation));
        when(languageRepository.existsByCodeIgnoreCase("hy")).thenReturn(true);
        when(contentItemTranslationRepository.existsByContentItemIdAndCodeIgnoreCaseAndIdNot(
                courseContentItemId,
                "hy",
                courseTranslationId
        )).thenReturn(false);
        when(contentItemTranslationRepository.saveAndFlush(courseTranslation))
                .thenThrow(new DataIntegrityViolationException("duplicate translation"));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> contentItemTranslationService.updateCourseTranslation(courseTranslationId, request)
        );

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("Course translation with this code already exists for this course", ex.getReason());
    }

    @Test
    @DisplayName("deleteCourseTranslation -> deletes course translation")
    void deleteCourseTranslation_ShouldDeleteCourseTranslation() {
        when(contentItemTranslationRepository.findById(courseTranslationId))
                .thenReturn(Optional.of(courseTranslation));

        contentItemTranslationService.deleteCourseTranslation(courseTranslationId);

        verify(contentItemTranslationRepository).delete(courseTranslation);
    }

    @Test
    @DisplayName("deleteExamTranslation -> deletes exam translation")
    void deleteExamTranslation_ShouldDeleteExamTranslation() {
        when(contentItemTranslationRepository.findById(examTranslationId))
                .thenReturn(Optional.of(examTranslation));

        contentItemTranslationService.deleteExamTranslation(examTranslationId);

        verify(contentItemTranslationRepository).delete(examTranslation);
    }

    @Test
    @DisplayName("deleteCourseTranslation -> throws not found when translation does not exist")
    void deleteCourseTranslation_ShouldThrowNotFound_WhenTranslationDoesNotExist() {
        when(contentItemTranslationRepository.findById(courseTranslationId))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> contentItemTranslationService.deleteCourseTranslation(courseTranslationId)
        );

        assertEquals(404, ex.getStatusCode().value());
        assertEquals("Content item translation not found with id: " + courseTranslationId, ex.getReason());

        verify(contentItemTranslationRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteCourseTranslation -> throws not found when translation is not course")
    void deleteCourseTranslation_ShouldThrowNotFound_WhenTranslationIsNotCourse() {
        when(contentItemTranslationRepository.findById(examTranslationId))
                .thenReturn(Optional.of(examTranslation));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> contentItemTranslationService.deleteCourseTranslation(examTranslationId)
        );

        assertEquals(404, ex.getStatusCode().value());
        assertEquals("Content item translation not found with id: " + examTranslationId, ex.getReason());

        verify(contentItemTranslationRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteExamTranslation -> throws not found when translation is not exam")
    void deleteExamTranslation_ShouldThrowNotFound_WhenTranslationIsNotExam() {
        when(contentItemTranslationRepository.findById(courseTranslationId))
                .thenReturn(Optional.of(courseTranslation));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> contentItemTranslationService.deleteExamTranslation(courseTranslationId)
        );

        assertEquals(404, ex.getStatusCode().value());
        assertEquals("Content item translation not found with id: " + courseTranslationId, ex.getReason());

        verify(contentItemTranslationRepository, never()).delete(any());
    }
}