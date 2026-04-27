package com.melikyan.academy.service;

import org.mockito.ArgumentCaptor;
import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.Course;
import com.melikyan.academy.entity.ContentItem;
import com.melikyan.academy.mapper.CourseMapper;
import org.mockito.junit.jupiter.MockitoExtension;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.repository.CourseRepository;
import org.springframework.test.util.ReflectionTestUtils;
import com.melikyan.academy.entity.enums.ContentItemType;
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

    private UUID userId;
    private UUID courseId;
    private UUID contentItemId;

    private User user;
    private ContentItem contentItem;
    private Course course;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        courseId = UUID.randomUUID();
        contentItemId = UUID.randomUUID();

        user = new User();
        ReflectionTestUtils.setField(user, "id", userId);

        contentItem = new ContentItem();
        ReflectionTestUtils.setField(contentItem, "id", contentItemId);
        contentItem.setTitle("Old title");
        contentItem.setDescription("Old description");
        contentItem.setType(ContentItemType.COURSE);
        contentItem.setCreatedBy(user);

        course = new Course();
        ReflectionTestUtils.setField(course, "id", courseId);
        course.setContentItem(contentItem);
        course.setStartDate(OffsetDateTime.parse("2026-05-01T10:00:00+04:00"));
        course.setDurationWeeks(8);
    }

    @Test
    @DisplayName("create -> saves content item and course, returns response")
    void create_ShouldSaveContentItemAndCourseAndReturnResponse() {
        CreateCourseRequest request = mock(CreateCourseRequest.class);
        CourseResponse response = mock(CourseResponse.class);

        OffsetDateTime startDate = OffsetDateTime.parse("2026-05-01T10:00:00+04:00");

        when(request.title()).thenReturn("  Java Backend  ");
        when(request.description()).thenReturn("  Course description  ");
        when(request.startDate()).thenReturn(startDate);
        when(request.durationWeeks()).thenReturn(12);
        when(request.createdById()).thenReturn(userId);

        when(contentItemRepository.existsByTypeAndTitleIgnoreCase(
                ContentItemType.COURSE,
                "Java Backend"
        )).thenReturn(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(contentItemRepository.saveAndFlush(any(ContentItem.class))).thenAnswer(invocation -> {
            ContentItem saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", contentItemId);
            return saved;
        });

        when(courseRepository.saveAndFlush(any(Course.class))).thenAnswer(invocation -> {
            Course saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", courseId);
            return saved;
        });

        when(courseMapper.toResponse(any(Course.class))).thenReturn(response);

        CourseResponse result = courseService.create(request);

        assertEquals(response, result);

        ArgumentCaptor<ContentItem> contentCaptor = ArgumentCaptor.forClass(ContentItem.class);
        verify(contentItemRepository).saveAndFlush(contentCaptor.capture());

        ContentItem savedContentItem = contentCaptor.getValue();
        assertEquals(ContentItemType.COURSE, savedContentItem.getType());
        assertEquals("Java Backend", savedContentItem.getTitle());
        assertEquals("Course description", savedContentItem.getDescription());
        assertEquals(0, savedContentItem.getTotalSteps());
        assertEquals(user, savedContentItem.getCreatedBy());

        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).saveAndFlush(courseCaptor.capture());

        Course savedCourse = courseCaptor.getValue();
        assertEquals(savedContentItem, savedCourse.getContentItem());
        assertEquals(startDate, savedCourse.getStartDate());
        assertEquals(12, savedCourse.getDurationWeeks());

        verify(courseMapper).toResponse(any(Course.class));
    }

    @Test
    @DisplayName("create -> throws bad request when title is blank")
    void create_ShouldThrowBadRequest_WhenTitleIsBlank() {
        CreateCourseRequest request = mock(CreateCourseRequest.class);

        when(request.title()).thenReturn("   ");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> courseService.create(request)
        );

        assertEquals(400, exception.getStatusCode().value());
        assertEquals("Course title must not be blank", exception.getReason());

        verifyNoInteractions(userRepository, courseRepository, contentItemRepository, courseMapper);
    }

    @Test
    @DisplayName("create -> throws conflict when title already exists")
    void create_ShouldThrowConflict_WhenTitleAlreadyExists() {
        CreateCourseRequest request = mock(CreateCourseRequest.class);

        when(request.title()).thenReturn("Java Backend");
        when(request.description()).thenReturn(null);

        when(contentItemRepository.existsByTypeAndTitleIgnoreCase(
                ContentItemType.COURSE,
                "Java Backend"
        )).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> courseService.create(request)
        );

        assertEquals(409, exception.getStatusCode().value());
        assertEquals("Course with this title already exists", exception.getReason());

        verify(userRepository, never()).findById(any(UUID.class));
        verify(contentItemRepository, never()).saveAndFlush(any(ContentItem.class));
        verify(courseRepository, never()).saveAndFlush(any(Course.class));
        verifyNoInteractions(courseMapper);
    }

    @Test
    @DisplayName("create -> throws not found when user does not exist")
    void create_ShouldThrowNotFound_WhenUserDoesNotExist() {
        CreateCourseRequest request = mock(CreateCourseRequest.class);

        when(request.title()).thenReturn("Java Backend");
        when(request.description()).thenReturn("Course description");
        when(request.createdById()).thenReturn(userId);

        when(contentItemRepository.existsByTypeAndTitleIgnoreCase(
                ContentItemType.COURSE,
                "Java Backend"
        )).thenReturn(false);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> courseService.create(request)
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("User not found with id: " + userId, exception.getReason());

        verify(contentItemRepository, never()).saveAndFlush(any(ContentItem.class));
        verify(courseRepository, never()).saveAndFlush(any(Course.class));
        verifyNoInteractions(courseMapper);
    }

    @Test
    @DisplayName("getById -> returns mapped response")
    void getById_ShouldReturnMappedResponse() {
        CourseResponse response = mock(CourseResponse.class);

        when(courseRepository.findDetailedById(courseId)).thenReturn(Optional.of(course));
        when(courseMapper.toResponse(course)).thenReturn(response);

        CourseResponse result = courseService.getById(courseId);

        assertEquals(response, result);
        verify(courseRepository).findDetailedById(courseId);
        verify(courseMapper).toResponse(course);
    }

    @Test
    @DisplayName("getById -> throws not found when course does not exist")
    void getById_ShouldThrowNotFound_WhenCourseDoesNotExist() {
        when(courseRepository.findDetailedById(courseId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> courseService.getById(courseId)
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Course not found with id: " + courseId, exception.getReason());

        verifyNoInteractions(courseMapper);
    }

    @Test
    @DisplayName("getAll -> returns mapped list")
    void getAll_ShouldReturnMappedList() {
        Course secondCourse = new Course();
        List<Course> courses = List.of(course, secondCourse);

        CourseResponse first = mock(CourseResponse.class);
        CourseResponse second = mock(CourseResponse.class);
        List<CourseResponse> responses = List.of(first, second);

        when(courseRepository.findAllDetailed()).thenReturn(courses);
        when(courseMapper.toResponseList(courses)).thenReturn(responses);

        List<CourseResponse> result = courseService.getAll();

        assertEquals(responses, result);
        verify(courseRepository).findAllDetailed();
        verify(courseMapper).toResponseList(courses);
    }

    @Test
    @DisplayName("update -> updates content item and course fields")
    void update_ShouldUpdateContentItemAndCourseFields() {
        UpdateCourseRequest request = mock(UpdateCourseRequest.class);
        CourseResponse response = mock(CourseResponse.class);

        OffsetDateTime updatedStartDate = OffsetDateTime.parse("2026-06-01T09:00:00+04:00");

        when(request.title()).thenReturn("  Updated Course  ");
        when(request.description()).thenReturn("   ");
        when(request.startDate()).thenReturn(updatedStartDate);
        when(request.durationWeeks()).thenReturn(16);

        when(courseRepository.findDetailedById(courseId)).thenReturn(Optional.of(course));

        when(contentItemRepository.existsByTypeAndTitleIgnoreCaseAndIdNot(
                ContentItemType.COURSE,
                "Updated Course",
                contentItemId
        )).thenReturn(false);

        when(courseRepository.saveAndFlush(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(courseMapper.toResponse(any(Course.class))).thenReturn(response);

        CourseResponse result = courseService.update(courseId, request);

        assertEquals(response, result);
        assertEquals("Updated Course", contentItem.getTitle());
        assertNull(contentItem.getDescription());
        assertEquals(updatedStartDate, course.getStartDate());
        assertEquals(16, course.getDurationWeeks());

        verify(courseRepository).saveAndFlush(course);
        verify(courseMapper).toResponse(course);
    }

    @Test
    @DisplayName("update -> updates only course fields when title and description are null")
    void update_ShouldUpdateOnlyCourseFields_WhenTitleAndDescriptionAreNull() {
        UpdateCourseRequest request = mock(UpdateCourseRequest.class);
        CourseResponse response = mock(CourseResponse.class);

        OffsetDateTime updatedStartDate = OffsetDateTime.parse("2026-07-01T09:00:00+04:00");

        when(request.title()).thenReturn(null);
        when(request.description()).thenReturn(null);
        when(request.startDate()).thenReturn(updatedStartDate);
        when(request.durationWeeks()).thenReturn(20);

        when(courseRepository.findDetailedById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.saveAndFlush(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(courseMapper.toResponse(any(Course.class))).thenReturn(response);

        CourseResponse result = courseService.update(courseId, request);

        assertEquals(response, result);
        assertEquals("Old title", contentItem.getTitle());
        assertEquals("Old description", contentItem.getDescription());
        assertEquals(updatedStartDate, course.getStartDate());
        assertEquals(20, course.getDurationWeeks());

        verify(contentItemRepository, never()).existsByTypeAndTitleIgnoreCaseAndIdNot(
                any(ContentItemType.class),
                anyString(),
                any(UUID.class)
        );
        verify(courseRepository).saveAndFlush(course);
    }

    @Test
    @DisplayName("update -> throws bad request when title is blank")
    void update_ShouldThrowBadRequest_WhenTitleIsBlank() {
        UpdateCourseRequest request = mock(UpdateCourseRequest.class);

        when(request.title()).thenReturn("   ");
        when(courseRepository.findDetailedById(courseId)).thenReturn(Optional.of(course));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> courseService.update(courseId, request)
        );

        assertEquals(400, exception.getStatusCode().value());
        assertEquals("Course title must not be blank", exception.getReason());

        verify(contentItemRepository, never()).existsByTypeAndTitleIgnoreCaseAndIdNot(
                any(ContentItemType.class),
                anyString(),
                any(UUID.class)
        );
        verify(courseRepository, never()).saveAndFlush(any(Course.class));
        verifyNoInteractions(courseMapper);
    }

    @Test
    @DisplayName("update -> throws conflict when new title already exists")
    void update_ShouldThrowConflict_WhenNewTitleAlreadyExists() {
        UpdateCourseRequest request = mock(UpdateCourseRequest.class);

        when(request.title()).thenReturn("Existing title");

        when(courseRepository.findDetailedById(courseId)).thenReturn(Optional.of(course));

        when(contentItemRepository.existsByTypeAndTitleIgnoreCaseAndIdNot(
                ContentItemType.COURSE,
                "Existing title",
                contentItemId
        )).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> courseService.update(courseId, request)
        );

        assertEquals(409, exception.getStatusCode().value());
        assertEquals("Course with this title already exists", exception.getReason());

        verify(courseRepository, never()).saveAndFlush(any(Course.class));
        verifyNoInteractions(courseMapper);
    }

    @Test
    @DisplayName("delete -> deletes course and content item")
    void delete_ShouldDeleteCourseAndContentItem() {
        when(courseRepository.findDetailedById(courseId)).thenReturn(Optional.of(course));

        courseService.delete(courseId);

        verify(courseRepository).delete(course);
        verify(contentItemRepository).delete(contentItem);
    }

    @Test
    @DisplayName("delete -> throws not found when course does not exist")
    void delete_ShouldThrowNotFound_WhenCourseDoesNotExist() {
        when(courseRepository.findDetailedById(courseId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> courseService.delete(courseId)
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Course not found with id: " + courseId, exception.getReason());

        verify(courseRepository, never()).delete(any(Course.class));
        verify(contentItemRepository, never()).delete(any(ContentItem.class));
    }
}