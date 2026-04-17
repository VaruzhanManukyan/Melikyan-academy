package com.melikyan.academy.controller;

import com.melikyan.academy.entity.enums.ContentItemType;
import tools.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import com.melikyan.academy.service.CourseService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.FilterType;
import com.melikyan.academy.security.RememberMeSecurityFilter;
import com.melikyan.academy.dto.response.course.CourseResponse;
import com.melikyan.academy.dto.request.course.CreateCourseRequest;
import com.melikyan.academy.dto.request.course.UpdateCourseRequest;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.util.List;
import java.util.UUID;
import java.time.OffsetDateTime;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@WebMvcTest(
        controllers = CourseController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                ServletWebSecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        },
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = RememberMeSecurityFilter.class
                )
        }
)
@AutoConfigureMockMvc(addFilters = false)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CourseService courseService;

    private String toUtcString(OffsetDateTime value) {
        return value.toInstant().toString();
    }

    @Test
    @DisplayName("POST /api/v1/courses -> should create course and return 201")
    void create_shouldReturnCreatedCourse() throws Exception {
        UUID courseId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();
        UUID purchasableId = UUID.randomUUID();

        OffsetDateTime startDate = OffsetDateTime.parse("2026-05-01T10:00:00+04:00");
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-04-14T12:00:00+04:00");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-04-14T12:30:00+04:00");

        CreateCourseRequest request = new CreateCourseRequest(
                "Java Backend Fundamentals",
                "Spring Boot, JPA, Security",
                ContentItemType.COURSE,
                startDate,
                12,
                categoryId,
                createdById
        );

        CourseResponse response = new CourseResponse(
                courseId,
                "Java Backend Fundamentals",
                "Spring Boot, JPA, Security",
                ContentItemType.COURSE,
                12,
                startDate,
                categoryId,
                createdById,
                purchasableId,
                createdAt,
                updatedAt
        );

        when(courseService.create(any(CreateCourseRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(courseId.toString()))
                .andExpect(jsonPath("$.title").value("Java Backend Fundamentals"))
                .andExpect(jsonPath("$.description").value("Spring Boot, JPA, Security"))
                .andExpect(jsonPath("$.type").value("COURSE"))
                .andExpect(jsonPath("$.durationWeeks").value(12))
                .andExpect(jsonPath("$.startDate").value(toUtcString(startDate)))
                .andExpect(jsonPath("$.categoryId").value(categoryId.toString()))
                .andExpect(jsonPath("$.createdById").value(createdById.toString()))
                .andExpect(jsonPath("$.purchasableId").value(purchasableId.toString()))
                .andExpect(jsonPath("$.createdAt").value(toUtcString(createdAt)))
                .andExpect(jsonPath("$.updatedAt").value(toUtcString(updatedAt)));

        verify(courseService).create(any(CreateCourseRequest.class));
    }

    @Test
    @DisplayName("GET /api/v1/courses/{id} -> should return course by id")
    void getById_shouldReturnCourse() throws Exception {
        UUID courseId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();
        UUID purchasableId = UUID.randomUUID();

        CourseResponse response = new CourseResponse(
                courseId,
                "Java Backend Fundamentals",
                "Spring Boot, JPA, Security",
                ContentItemType.COURSE,
                12,
                OffsetDateTime.parse("2026-05-01T10:00:00+04:00"),
                categoryId,
                createdById,
                purchasableId,
                OffsetDateTime.parse("2026-04-14T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-14T12:30:00+04:00")
        );

        when(courseService.getById(courseId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/courses/{id}", courseId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(courseId.toString()))
                .andExpect(jsonPath("$.title").value("Java Backend Fundamentals"))
                .andExpect(jsonPath("$.type").value("COURSE"))
                .andExpect(jsonPath("$.purchasableId").value(purchasableId.toString()));

        verify(courseService).getById(courseId);
    }

    @Test
    @DisplayName("GET /api/v1/courses -> should return all courses")
    void getAll_shouldReturnAllCourses() throws Exception {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();
        UUID firstPurchasableId = UUID.randomUUID();
        UUID secondPurchasableId = UUID.randomUUID();

        CourseResponse first = new CourseResponse(
                firstId,
                "Java Backend Fundamentals",
                "Spring Boot course",
                ContentItemType.COURSE,
                12,
                OffsetDateTime.parse("2026-05-01T10:00:00+04:00"),
                categoryId,
                createdById,
                firstPurchasableId,
                OffsetDateTime.parse("2026-04-14T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-14T12:30:00+04:00")
        );

        CourseResponse second = new CourseResponse(
                secondId,
                "Algorithms",
                "Data structures and algorithms",
                ContentItemType.COURSE,
                10,
                OffsetDateTime.parse("2026-06-01T10:00:00+04:00"),
                categoryId,
                createdById,
                secondPurchasableId,
                OffsetDateTime.parse("2026-04-15T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-15T12:30:00+04:00")
        );

        when(courseService.getAll()).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/v1/courses")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(firstId.toString()))
                .andExpect(jsonPath("$[0].title").value("Java Backend Fundamentals"))
                .andExpect(jsonPath("$[0].purchasableId").value(firstPurchasableId.toString()))
                .andExpect(jsonPath("$[1].id").value(secondId.toString()))
                .andExpect(jsonPath("$[1].title").value("Algorithms"))
                .andExpect(jsonPath("$[1].purchasableId").value(secondPurchasableId.toString()));

        verify(courseService).getAll();
    }

    @Test
    @DisplayName("PATCH /api/v1/courses/{id} -> should update course")
    void update_shouldReturnUpdatedCourse() throws Exception {
        UUID courseId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();
        UUID purchasableId = UUID.randomUUID();

        UpdateCourseRequest request = new UpdateCourseRequest(
                "Advanced Java",
                "Updated description",
                ContentItemType.COURSE,
                OffsetDateTime.parse("2026-06-10T10:00:00+04:00"),
                16,
                categoryId
        );

        CourseResponse response = new CourseResponse(
                courseId,
                "Advanced Java",
                "Updated description",
                ContentItemType.COURSE,
                16,
                OffsetDateTime.parse("2026-06-10T10:00:00+04:00"),
                categoryId,
                createdById,
                purchasableId,
                OffsetDateTime.parse("2026-04-14T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-16T14:00:00+04:00")
        );

        when(courseService.update(eq(courseId), any(UpdateCourseRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/courses/{id}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(courseId.toString()))
                .andExpect(jsonPath("$.title").value("Advanced Java"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.durationWeeks").value(16))
                .andExpect(jsonPath("$.purchasableId").value(purchasableId.toString()));

        verify(courseService).update(eq(courseId), any(UpdateCourseRequest.class));
    }

    @Test
    @DisplayName("DELETE /api/v1/courses/{id} -> should return 204")
    void delete_shouldReturnNoContent() throws Exception {
        UUID courseId = UUID.randomUUID();

        doNothing().when(courseService).delete(courseId);

        mockMvc.perform(delete("/api/v1/courses/{id}", courseId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(courseService).delete(courseId);
    }
}