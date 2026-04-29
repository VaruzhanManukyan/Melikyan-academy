package com.melikyan.academy.controller;

import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.FilterType;
import com.melikyan.academy.security.RememberMeSecurityFilter;
import com.melikyan.academy.service.ContentItemTranslationService;
import com.melikyan.academy.dto.response.contentItemTranslation.ContentItemTranslationResponse;
import com.melikyan.academy.dto.request.contentItemTranslation.CreateContentItemTranslationRequest;
import com.melikyan.academy.dto.request.contentItemTranslation.UpdateContentItemTranslationRequest;
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
        controllers = CourseTranslationController.class,
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
class CourseTranslationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ContentItemTranslationService contentItemTranslationService;

    private String toUtcString(OffsetDateTime value) {
        return value.toInstant().toString();
    }

    @Test
    @DisplayName("POST /api/v1/course-translations -> should create course translation and return 201")
    void create_shouldReturnCreatedCourseTranslation() throws Exception {
        UUID translationId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        OffsetDateTime createdAt = OffsetDateTime.parse("2026-04-29T12:00:00+04:00");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-04-29T12:30:00+04:00");

        CreateContentItemTranslationRequest request = new CreateContentItemTranslationRequest(
                "Java Backend Fundamentals",
                "Spring Boot, JPA, Security",
                "en",
                courseId,
                createdById
        );

        ContentItemTranslationResponse response = new ContentItemTranslationResponse(
                translationId,
                "en",
                "Java Backend Fundamentals",
                "Spring Boot, JPA, Security",
                courseId,
                createdById,
                createdAt,
                updatedAt
        );

        when(contentItemTranslationService.createCourseTranslation(any(CreateContentItemTranslationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/course-translations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(translationId.toString()))
                .andExpect(jsonPath("$.code").value("en"))
                .andExpect(jsonPath("$.title").value("Java Backend Fundamentals"))
                .andExpect(jsonPath("$.description").value("Spring Boot, JPA, Security"))
                .andExpect(jsonPath("$.contentItemId").value(courseId.toString()))
                .andExpect(jsonPath("$.createdById").value(createdById.toString()))
                .andExpect(jsonPath("$.createdAt").value(toUtcString(createdAt)))
                .andExpect(jsonPath("$.updatedAt").value(toUtcString(updatedAt)));

        verify(contentItemTranslationService).createCourseTranslation(any(CreateContentItemTranslationRequest.class));
    }

    @Test
    @DisplayName("GET /api/v1/course-translations/{id} -> should return course translation by id")
    void getById_shouldReturnCourseTranslation() throws Exception {
        UUID translationId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        ContentItemTranslationResponse response = new ContentItemTranslationResponse(
                translationId,
                "en",
                "Java Backend Fundamentals",
                "Spring Boot, JPA, Security",
                courseId,
                createdById,
                OffsetDateTime.parse("2026-04-29T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-29T12:30:00+04:00")
        );

        when(contentItemTranslationService.getCourseTranslationById(translationId))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/course-translations/{id}", translationId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(translationId.toString()))
                .andExpect(jsonPath("$.code").value("en"))
                .andExpect(jsonPath("$.title").value("Java Backend Fundamentals"))
                .andExpect(jsonPath("$.description").value("Spring Boot, JPA, Security"))
                .andExpect(jsonPath("$.contentItemId").value(courseId.toString()))
                .andExpect(jsonPath("$.createdById").value(createdById.toString()));

        verify(contentItemTranslationService).getCourseTranslationById(translationId);
    }

    @Test
    @DisplayName("GET /api/v1/course-translations -> should return all course translations")
    void getAll_shouldReturnAllCourseTranslations() throws Exception {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        UUID firstCourseId = UUID.randomUUID();
        UUID secondCourseId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        ContentItemTranslationResponse first = new ContentItemTranslationResponse(
                firstId,
                "en",
                "Java Backend Fundamentals",
                "Spring Boot course",
                firstCourseId,
                createdById,
                OffsetDateTime.parse("2026-04-29T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-29T12:30:00+04:00")
        );

        ContentItemTranslationResponse second = new ContentItemTranslationResponse(
                secondId,
                "hy",
                "Java Backend",
                "Java դասընթաց",
                secondCourseId,
                createdById,
                OffsetDateTime.parse("2026-04-30T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-30T12:30:00+04:00")
        );

        when(contentItemTranslationService.getAllCourseTranslations())
                .thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/v1/course-translations")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(firstId.toString()))
                .andExpect(jsonPath("$[0].code").value("en"))
                .andExpect(jsonPath("$[0].title").value("Java Backend Fundamentals"))
                .andExpect(jsonPath("$[0].contentItemId").value(firstCourseId.toString()))
                .andExpect(jsonPath("$[1].id").value(secondId.toString()))
                .andExpect(jsonPath("$[1].code").value("hy"))
                .andExpect(jsonPath("$[1].title").value("Java Backend"))
                .andExpect(jsonPath("$[1].contentItemId").value(secondCourseId.toString()));

        verify(contentItemTranslationService).getAllCourseTranslations();
    }

    @Test
    @DisplayName("GET /api/v1/course-translations/code/{code} -> should return course translations by code")
    void getByCode_shouldReturnCourseTranslationsByCode() throws Exception {
        UUID translationId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        ContentItemTranslationResponse response = new ContentItemTranslationResponse(
                translationId,
                "en",
                "Java Backend Fundamentals",
                "Spring Boot course",
                courseId,
                createdById,
                OffsetDateTime.parse("2026-04-29T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-29T12:30:00+04:00")
        );

        when(contentItemTranslationService.getCourseTranslationsByCode("en"))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/course-translations/code/{code}", "en")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(translationId.toString()))
                .andExpect(jsonPath("$[0].code").value("en"))
                .andExpect(jsonPath("$[0].title").value("Java Backend Fundamentals"))
                .andExpect(jsonPath("$[0].contentItemId").value(courseId.toString()));

        verify(contentItemTranslationService).getCourseTranslationsByCode("en");
    }

    @Test
    @DisplayName("GET /api/v1/course-translations/course/{courseId} -> should return translations by course id")
    void getByCourseId_shouldReturnCourseTranslations() throws Exception {
        UUID translationId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        ContentItemTranslationResponse response = new ContentItemTranslationResponse(
                translationId,
                "en",
                "Java Backend Fundamentals",
                "Spring Boot course",
                courseId,
                createdById,
                OffsetDateTime.parse("2026-04-29T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-29T12:30:00+04:00")
        );

        when(contentItemTranslationService.getByCourseId(courseId))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/course-translations/course/{courseId}", courseId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(translationId.toString()))
                .andExpect(jsonPath("$[0].code").value("en"))
                .andExpect(jsonPath("$[0].title").value("Java Backend Fundamentals"))
                .andExpect(jsonPath("$[0].contentItemId").value(courseId.toString()));

        verify(contentItemTranslationService).getByCourseId(courseId);
    }

    @Test
    @DisplayName("GET /api/v1/course-translations/course/{courseId}/code/{code} -> should return translation by course id and code")
    void getByCourseIdAndCode_shouldReturnCourseTranslation() throws Exception {
        UUID translationId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        ContentItemTranslationResponse response = new ContentItemTranslationResponse(
                translationId,
                "en",
                "Java Backend Fundamentals",
                "Spring Boot course",
                courseId,
                createdById,
                OffsetDateTime.parse("2026-04-29T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-29T12:30:00+04:00")
        );

        when(contentItemTranslationService.getByCourseIdAndCode(courseId, "en"))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/course-translations/course/{courseId}/code/{code}", courseId, "en")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(translationId.toString()))
                .andExpect(jsonPath("$.code").value("en"))
                .andExpect(jsonPath("$.title").value("Java Backend Fundamentals"))
                .andExpect(jsonPath("$.contentItemId").value(courseId.toString()));

        verify(contentItemTranslationService).getByCourseIdAndCode(courseId, "en");
    }

    @Test
    @DisplayName("PATCH /api/v1/course-translations/{id} -> should update course translation")
    void update_shouldReturnUpdatedCourseTranslation() throws Exception {
        UUID translationId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        UpdateContentItemTranslationRequest request = new UpdateContentItemTranslationRequest(
                "Java Backend",
                "Java դասընթաց",
                "hy"
        );

        ContentItemTranslationResponse response = new ContentItemTranslationResponse(
                translationId,
                "hy",
                "Java Backend",
                "Java դասընթաց",
                courseId,
                createdById,
                OffsetDateTime.parse("2026-04-29T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-29T13:00:00+04:00")
        );

        when(contentItemTranslationService.updateCourseTranslation(
                eq(translationId),
                any(UpdateContentItemTranslationRequest.class)
        )).thenReturn(response);

        mockMvc.perform(patch("/api/v1/course-translations/{id}", translationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(translationId.toString()))
                .andExpect(jsonPath("$.code").value("hy"))
                .andExpect(jsonPath("$.title").value("Java Backend"))
                .andExpect(jsonPath("$.description").value("Java դասընթաց"))
                .andExpect(jsonPath("$.contentItemId").value(courseId.toString()));

        verify(contentItemTranslationService).updateCourseTranslation(
                eq(translationId),
                any(UpdateContentItemTranslationRequest.class)
        );
    }

    @Test
    @DisplayName("DELETE /api/v1/course-translations/{id} -> should return 204")
    void delete_shouldReturnNoContent() throws Exception {
        UUID translationId = UUID.randomUUID();

        doNothing().when(contentItemTranslationService).deleteCourseTranslation(translationId);

        mockMvc.perform(delete("/api/v1/course-translations/{id}", translationId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(contentItemTranslationService).deleteCourseTranslation(translationId);
    }
}