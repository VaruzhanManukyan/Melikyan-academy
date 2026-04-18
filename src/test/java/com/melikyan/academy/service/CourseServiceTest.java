package com.melikyan.academy.service;

import com.melikyan.academy.entity.ContentItem;
import com.melikyan.academy.entity.enums.ContentItemType;
import org.mockito.ArgumentCaptor;
import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.Course;
import org.springframework.http.HttpStatus;
import com.melikyan.academy.entity.Category;
import com.melikyan.academy.mapper.CourseMapper;
import org.mockito.junit.jupiter.MockitoExtension;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.repository.CourseRepository;
import com.melikyan.academy.repository.CategoryRepository;
import com.melikyan.academy.repository.ContentItemRepository;
import org.springframework.web.server.ResponseStatusException;
import com.melikyan.academy.dto.response.course.CourseResponse;
import com.melikyan.academy.dto.request.course.CreateCourseRequest;
import com.melikyan.academy.dto.request.course.UpdateCourseRequest;
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

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {
    @Mock
    private CourseMapper courseMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ContentItemRepository contentItemRepository;

    @InjectMocks
    private CourseService courseService;

    private UUID courseId;
    private UUID createdById;
    private UUID contentItemId;

    private User user;
    private Course course;
    private ContentItem contentItem;
    private CourseResponse response;

    @BeforeEach
    void setUp() {
        courseId = UUID.randomUUID();
        contentItemId = UUID.randomUUID();
        createdById = UUID.randomUUID();

        user = new User();
        user.setId(createdById);

        contentItem = new ContentItem();
        contentItem.setId(contentItemId);
        contentItem.setTitle("Java Backend Fundamentals");
        contentItem.setDescription("Spring Boot, JPA, Security");
        contentItem.setType(ContentItemType.COURSE);
        contentItem.setCreatedBy(user);

        course = new Course();
        course.setId(courseId);
        course.setContentItem(contentItem);
        course.setStartDate(OffsetDateTime.parse("2026-05-01T10:00:00+04:00"));
        course.setDurationWeeks(12);

        response = new CourseResponse(
                courseId,
                "Java Backend Fundamentals",
                "Spring Boot, JPA, Security",
                ContentItemType.COURSE,
                12,
                OffsetDateTime.parse("2026-05-01T10:00:00+04:00"),
                createdById,
                contentItemId,
                OffsetDateTime.parse("2026-04-14T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-14T12:30:00+04:00")
        );
    }

    @Test
    @DisplayName("create -> should create course successfully")
    void create_shouldCreateCourseSuccessfully() {
        CreateCourseRequest request = new CreateCourseRequest(
                "  Java Backend Fundamentals  ",
                "  Spring Boot, JPA, Security  ",
                ContentItemType.COURSE,
                OffsetDateTime.parse("2026-05-01T10:00:00+04:00"),
                12,
                createdById
        );

        when(contentItemRepository.existsByTitleIgnoreCase("Java Backend Fundamentals")).thenReturn(false);
        when(userRepository.findById(createdById)).thenReturn(Optional.of(user));
        when(contentItemRepository.save(any(ContentItem.class))).thenReturn(contentItem);
        when(courseRepository.saveAndFlush(any(Course.class))).thenReturn(course);
        when(courseMapper.toResponse(course)).thenReturn(response);

        CourseResponse actual = courseService.create(request);

        assertNotNull(actual);
        assertEquals(response, actual);

        ArgumentCaptor<ContentItem> contentItemCaptor = ArgumentCaptor.forClass(ContentItem.class);
        verify(contentItemRepository).save(contentItemCaptor.capture());

        ContentItem savedContentItem = contentItemCaptor.getValue();
        assertEquals("Java Backend Fundamentals", savedContentItem.getTitle());
        assertEquals("Spring Boot, JPA, Security", savedContentItem.getDescription());
        assertEquals(ContentItemType.COURSE, savedContentItem.getType());
        assertEquals(user, savedContentItem.getCreatedBy());

        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).saveAndFlush(courseCaptor.capture());

        Course savedCourse = courseCaptor.getValue();
        assertEquals(contentItem, savedCourse.getContentItem());
        assertEquals(request.startDate(), savedCourse.getStartDate());
        assertEquals(request.durationWeeks(), savedCourse.getDurationWeeks());

        verify(courseMapper).toResponse(course);
    }

    @Test
    @DisplayName("create -> should throw BAD_REQUEST when type is not COURSE")
    void create_shouldThrowBadRequestWhenTypeIsInvalid() {
        CreateCourseRequest request = new CreateCourseRequest(
                "Java Backend Fundamentals",
                "Spring Boot, JPA, Security",
                ContentItemType.EXAM,
                OffsetDateTime.parse("2026-05-01T10:00:00+04:00"),
                12,
                createdById
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> courseService.create(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Type must be COURSE for course creation", exception.getReason());

        verifyNoInteractions(userRepository, contentItemRepository, courseRepository, courseMapper);
    }

    @Test
    @DisplayName("create -> should throw CONFLICT when title already exists")
    void create_shouldThrowConflictWhenTitleAlreadyExists() {
        CreateCourseRequest request = new CreateCourseRequest(
                "Java Backend Fundamentals",
                "Spring Boot, JPA, Security",
                ContentItemType.COURSE,
                OffsetDateTime.parse("2026-05-01T10:00:00+04:00"),
                12,
                createdById
        );

        when(contentItemRepository.existsByTitleIgnoreCase("Java Backend Fundamentals")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> courseService.create(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Course with this title already exists", exception.getReason());

        verify(contentItemRepository).existsByTitleIgnoreCase("Java Backend Fundamentals");
        verifyNoMoreInteractions(contentItemRepository);
        verifyNoInteractions(userRepository, courseRepository, courseMapper);
    }

    @Test
    @DisplayName("create -> should throw NOT_FOUND when user does not exist")
    void create_shouldThrowNotFoundWhenUserDoesNotExist() {
        CreateCourseRequest request = new CreateCourseRequest(
                "Java Backend Fundamentals",
                "Spring Boot, JPA, Security",
                ContentItemType.COURSE,
                OffsetDateTime.parse("2026-05-01T10:00:00+04:00"),
                12,
                createdById
        );

        when(contentItemRepository.existsByTitleIgnoreCase("Java Backend Fundamentals")).thenReturn(false);
        when(userRepository.findById(createdById)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> courseService.create(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found with id: " + createdById, exception.getReason());

        verify(userRepository).findById(createdById);
        verifyNoInteractions(courseRepository, courseMapper);
    }

    @Test
    @DisplayName("getById -> should return course response")
    void getById_shouldReturnCourseResponse() {
        when(courseRepository.findDetailedById(courseId)).thenReturn(Optional.of(course));
        when(courseMapper.toResponse(course)).thenReturn(response);

        CourseResponse actual = courseService.getById(courseId);

        assertEquals(response, actual);

        verify(courseRepository).findDetailedById(courseId);
        verify(courseMapper).toResponse(course);
    }

    @Test
    @DisplayName("getById -> should throw NOT_FOUND when course does not exist")
    void getById_shouldThrowNotFoundWhenCourseDoesNotExist() {
        when(courseRepository.findDetailedById(courseId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> courseService.getById(courseId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Course not found with id: " + courseId, exception.getReason());

        verify(courseRepository).findDetailedById(courseId);
        verifyNoInteractions(courseMapper);
    }

    @Test
    @DisplayName("getAll -> should return all courses")
    void getAll_shouldReturnAllCourses() {
        Course secondCourse = new Course();
        CourseResponse secondResponse = new CourseResponse(
                UUID.randomUUID(),
                "Algorithms",
                "Data structures and algorithms",
                ContentItemType.COURSE,
                10,
                OffsetDateTime.parse("2026-06-01T10:00:00+04:00"),
                UUID.randomUUID(),
                UUID.randomUUID(),
                OffsetDateTime.parse("2026-04-15T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-15T12:30:00+04:00")
        );

        List<Course> courses = List.of(course, secondCourse);
        List<CourseResponse> responses = List.of(response, secondResponse);

        when(courseRepository.findAllDetailed()).thenReturn(courses);
        when(courseMapper.toResponseList(courses)).thenReturn(responses);

        List<CourseResponse> actual = courseService.getAll();

        assertEquals(2, actual.size());
        assertEquals(responses, actual);

        verify(courseRepository).findAllDetailed();
        verify(courseMapper).toResponseList(courses);
    }

    @Test
    @DisplayName("update -> should update course successfully")
    void update_shouldUpdateCourseSuccessfully() {
        UpdateCourseRequest request = new UpdateCourseRequest(
                "  Advanced Java  ",
                "  Updated description  ",
                ContentItemType.COURSE,
                OffsetDateTime.parse("2026-06-10T10:00:00+04:00"),
                16
        );

        Course updatedCourse = new Course();
        updatedCourse.setId(courseId);
        updatedCourse.setContentItem(contentItem);
        updatedCourse.setStartDate(request.startDate());
        updatedCourse.setDurationWeeks(request.durationWeeks());

        CourseResponse updatedResponse = new CourseResponse(
                courseId,
                "Advanced Java",
                "Updated description",
                ContentItemType.COURSE,
                16,
                OffsetDateTime.parse("2026-06-10T10:00:00+04:00"),
                createdById,
                contentItemId,
                OffsetDateTime.parse("2026-04-14T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-16T14:00:00+04:00")
        );

        when(courseRepository.findDetailedById(courseId)).thenReturn(Optional.of(course));
        when(contentItemRepository.existsByTitleIgnoreCaseAndIdNot("Advanced Java", contentItemId)).thenReturn(false);
        when(courseRepository.saveAndFlush(course)).thenReturn(updatedCourse);
        when(courseMapper.toResponse(updatedCourse)).thenReturn(updatedResponse);

        CourseResponse actual = courseService.update(courseId, request);

        assertEquals(updatedResponse, actual);
        assertEquals("Advanced Java", contentItem.getTitle());
        assertEquals("Updated description", contentItem.getDescription());
        assertEquals(request.startDate(), course.getStartDate());
        assertEquals(request.durationWeeks(), course.getDurationWeeks());

        verify(courseRepository).findDetailedById(courseId);
        verify(contentItemRepository).existsByTitleIgnoreCaseAndIdNot("Advanced Java", contentItemId);
        verify(courseRepository).saveAndFlush(course);
        verify(courseMapper).toResponse(updatedCourse);
    }

    @Test
    @DisplayName("update -> should throw BAD_REQUEST when type is changed to non-COURSE")
    void update_shouldThrowBadRequestWhenTypeIsInvalid() {
        UpdateCourseRequest request = new UpdateCourseRequest(
                "Advanced Java",
                "Updated description",
                ContentItemType.EXAM,
                OffsetDateTime.parse("2026-06-10T10:00:00+04:00"),
                16
        );

        when(courseRepository.findDetailedById(courseId)).thenReturn(Optional.of(course));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> courseService.update(courseId, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Course type cannot be changed to another type", exception.getReason());

        verify(courseRepository).findDetailedById(courseId);
        verifyNoMoreInteractions(courseRepository);
        verifyNoInteractions(contentItemRepository, courseMapper);
    }

    @Test
    @DisplayName("update -> should throw CONFLICT when new title already exists")
    void update_shouldThrowConflictWhenTitleAlreadyExists() {
        UpdateCourseRequest request = new UpdateCourseRequest(
                "Advanced Java",
                "Updated description",
                ContentItemType.COURSE,
                OffsetDateTime.parse("2026-06-10T10:00:00+04:00"),
                16
        );

        when(courseRepository.findDetailedById(courseId)).thenReturn(Optional.of(course));
        when(contentItemRepository.existsByTitleIgnoreCaseAndIdNot("Advanced Java", contentItemId)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> courseService.update(courseId, request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Course with this title already exists", exception.getReason());

        verify(courseRepository).findDetailedById(courseId);
        verify(contentItemRepository).existsByTitleIgnoreCaseAndIdNot("Advanced Java", contentItemId);
        verifyNoMoreInteractions(contentItemRepository);
        verifyNoInteractions(courseMapper);
    }

    @Test
    @DisplayName("update -> should set description to null when request description is blank")
    void update_shouldSetDescriptionToNullWhenBlank() {
        UpdateCourseRequest request = new UpdateCourseRequest(
                null,
                "   ",
                ContentItemType.COURSE,
                null,
                null
        );

        when(courseRepository.findDetailedById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.saveAndFlush(course)).thenReturn(course);
        when(courseMapper.toResponse(course)).thenReturn(response);

        CourseResponse actual = courseService.update(courseId, request);

        assertEquals(response, actual);
        assertNull(contentItem.getDescription());

        verify(courseRepository).saveAndFlush(course);
        verify(courseMapper).toResponse(course);
    }

    @Test
    @DisplayName("delete -> should delete course and content item")
    void delete_shouldDeleteCourseAndContentItem() {
        when(courseRepository.findDetailedById(courseId)).thenReturn(Optional.of(course));

        courseService.delete(courseId);

        verify(courseRepository).findDetailedById(courseId);
        verify(courseRepository).delete(course);
        verify(contentItemRepository).delete(contentItem);
    }
}