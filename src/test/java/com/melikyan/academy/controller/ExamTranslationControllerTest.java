package com.melikyan.academy.controller;

import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.FilterType;
import com.melikyan.academy.security.RememberMeSecurityFilter;
import com.melikyan.academy.service.ContentItemTranslationService;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import com.melikyan.academy.dto.response.contentItemTranslation.ContentItemTranslationResponse;
import com.melikyan.academy.dto.request.contentItemTranslation.CreateContentItemTranslationRequest;
import com.melikyan.academy.dto.request.contentItemTranslation.UpdateContentItemTranslationRequest;
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
        controllers = ExamTranslationController.class,
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
class ExamTranslationControllerTest {
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
    @DisplayName("POST /api/v1/exam-translations -> should create exam translation and return 201")
    void create_shouldReturnCreatedExamTranslation() throws Exception {
        UUID translationId = UUID.randomUUID();
        UUID contentItemId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        OffsetDateTime createdAt = OffsetDateTime.parse("2026-04-29T12:00:00+04:00");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-04-29T12:30:00+04:00");

        CreateContentItemTranslationRequest request = new CreateContentItemTranslationRequest(
                "Java Backend Exam",
                "Spring Boot exam",
                "en",
                contentItemId,
                createdById
        );

        ContentItemTranslationResponse response = new ContentItemTranslationResponse(
                translationId,
                "en",
                "Java Backend Exam",
                "Spring Boot exam",
                contentItemId,
                createdById,
                createdAt,
                updatedAt
        );

        when(contentItemTranslationService.createExamTranslation(any(CreateContentItemTranslationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/exam-translations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(translationId.toString()))
                .andExpect(jsonPath("$.code").value("en"))
                .andExpect(jsonPath("$.title").value("Java Backend Exam"))
                .andExpect(jsonPath("$.description").value("Spring Boot exam"))
                .andExpect(jsonPath("$.contentItemId").value(contentItemId.toString()))
                .andExpect(jsonPath("$.createdById").value(createdById.toString()))
                .andExpect(jsonPath("$.createdAt").value(toUtcString(createdAt)))
                .andExpect(jsonPath("$.updatedAt").value(toUtcString(updatedAt)));

        verify(contentItemTranslationService).createExamTranslation(any(CreateContentItemTranslationRequest.class));
    }

    @Test
    @DisplayName("GET /api/v1/exam-translations/{id} -> should return exam translation by id")
    void getById_shouldReturnExamTranslation() throws Exception {
        UUID translationId = UUID.randomUUID();
        UUID contentItemId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        ContentItemTranslationResponse response = new ContentItemTranslationResponse(
                translationId,
                "en",
                "Java Backend Exam",
                "Spring Boot exam",
                contentItemId,
                createdById,
                OffsetDateTime.parse("2026-04-29T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-29T12:30:00+04:00")
        );

        when(contentItemTranslationService.getExamTranslationById(translationId))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/exam-translations/{id}", translationId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(translationId.toString()))
                .andExpect(jsonPath("$.code").value("en"))
                .andExpect(jsonPath("$.title").value("Java Backend Exam"))
                .andExpect(jsonPath("$.description").value("Spring Boot exam"))
                .andExpect(jsonPath("$.contentItemId").value(contentItemId.toString()))
                .andExpect(jsonPath("$.createdById").value(createdById.toString()));

        verify(contentItemTranslationService).getExamTranslationById(translationId);
    }

    @Test
    @DisplayName("GET /api/v1/exam-translations -> should return all exam translations")
    void getAll_shouldReturnAllExamTranslations() throws Exception {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        UUID firstContentItemId = UUID.randomUUID();
        UUID secondContentItemId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        ContentItemTranslationResponse first = new ContentItemTranslationResponse(
                firstId,
                "en",
                "Java Backend Exam",
                "Spring Boot exam",
                firstContentItemId,
                createdById,
                OffsetDateTime.parse("2026-04-29T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-29T12:30:00+04:00")
        );

        ContentItemTranslationResponse second = new ContentItemTranslationResponse(
                secondId,
                "hy",
                "Java Backend Exam",
                "Java քննություն",
                secondContentItemId,
                createdById,
                OffsetDateTime.parse("2026-04-30T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-30T12:30:00+04:00")
        );

        when(contentItemTranslationService.getAllExamTranslations())
                .thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/v1/exam-translations")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(firstId.toString()))
                .andExpect(jsonPath("$[0].code").value("en"))
                .andExpect(jsonPath("$[0].title").value("Java Backend Exam"))
                .andExpect(jsonPath("$[0].contentItemId").value(firstContentItemId.toString()))
                .andExpect(jsonPath("$[1].id").value(secondId.toString()))
                .andExpect(jsonPath("$[1].code").value("hy"))
                .andExpect(jsonPath("$[1].title").value("Java Backend Exam"))
                .andExpect(jsonPath("$[1].contentItemId").value(secondContentItemId.toString()));

        verify(contentItemTranslationService).getAllExamTranslations();
    }

    @Test
    @DisplayName("GET /api/v1/exam-translations/code/{code} -> should return exam translations by code")
    void getByCode_shouldReturnExamTranslationsByCode() throws Exception {
        UUID translationId = UUID.randomUUID();
        UUID contentItemId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        ContentItemTranslationResponse response = new ContentItemTranslationResponse(
                translationId,
                "en",
                "Java Backend Exam",
                "Spring Boot exam",
                contentItemId,
                createdById,
                OffsetDateTime.parse("2026-04-29T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-29T12:30:00+04:00")
        );

        when(contentItemTranslationService.getExamTranslationsByCode("en"))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/exam-translations/code/{code}", "en")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(translationId.toString()))
                .andExpect(jsonPath("$[0].code").value("en"))
                .andExpect(jsonPath("$[0].title").value("Java Backend Exam"))
                .andExpect(jsonPath("$[0].contentItemId").value(contentItemId.toString()));

        verify(contentItemTranslationService).getExamTranslationsByCode("en");
    }

    @Test
    @DisplayName("GET /api/v1/exam-translations/exam/{examId} -> should return translations by exam id")
    void getByExamId_shouldReturnExamTranslations() throws Exception {
        UUID translationId = UUID.randomUUID();
        UUID examId = UUID.randomUUID();
        UUID contentItemId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        ContentItemTranslationResponse response = new ContentItemTranslationResponse(
                translationId,
                "en",
                "Java Backend Exam",
                "Spring Boot exam",
                contentItemId,
                createdById,
                OffsetDateTime.parse("2026-04-29T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-29T12:30:00+04:00")
        );

        when(contentItemTranslationService.getByExamId(examId))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/exam-translations/exam/{examId}", examId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(translationId.toString()))
                .andExpect(jsonPath("$[0].code").value("en"))
                .andExpect(jsonPath("$[0].title").value("Java Backend Exam"))
                .andExpect(jsonPath("$[0].contentItemId").value(contentItemId.toString()));

        verify(contentItemTranslationService).getByExamId(examId);
    }

    @Test
    @DisplayName("GET /api/v1/exam-translations/exam/{examId}/code/{code} -> should return translation by exam id and code")
    void getByExamIdAndCode_shouldReturnExamTranslation() throws Exception {
        UUID translationId = UUID.randomUUID();
        UUID examId = UUID.randomUUID();
        UUID contentItemId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        ContentItemTranslationResponse response = new ContentItemTranslationResponse(
                translationId,
                "en",
                "Java Backend Exam",
                "Spring Boot exam",
                contentItemId,
                createdById,
                OffsetDateTime.parse("2026-04-29T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-29T12:30:00+04:00")
        );

        when(contentItemTranslationService.getByExamIdAndCode(examId, "en"))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/exam-translations/exam/{examId}/code/{code}", examId, "en")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(translationId.toString()))
                .andExpect(jsonPath("$.code").value("en"))
                .andExpect(jsonPath("$.title").value("Java Backend Exam"))
                .andExpect(jsonPath("$.contentItemId").value(contentItemId.toString()));

        verify(contentItemTranslationService).getByExamIdAndCode(examId, "en");
    }

    @Test
    @DisplayName("PATCH /api/v1/exam-translations/{id} -> should update exam translation")
    void update_shouldReturnUpdatedExamTranslation() throws Exception {
        UUID translationId = UUID.randomUUID();
        UUID contentItemId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        UpdateContentItemTranslationRequest request = new UpdateContentItemTranslationRequest(
                "Java Backend Exam",
                "Java քննություն",
                "hy"
        );

        ContentItemTranslationResponse response = new ContentItemTranslationResponse(
                translationId,
                "hy",
                "Java Backend Exam",
                "Java քննություն",
                contentItemId,
                createdById,
                OffsetDateTime.parse("2026-04-29T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-29T13:00:00+04:00")
        );

        when(contentItemTranslationService.updateExamTranslation(
                eq(translationId),
                any(UpdateContentItemTranslationRequest.class)
        )).thenReturn(response);

        mockMvc.perform(patch("/api/v1/exam-translations/{id}", translationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(translationId.toString()))
                .andExpect(jsonPath("$.code").value("hy"))
                .andExpect(jsonPath("$.title").value("Java Backend Exam"))
                .andExpect(jsonPath("$.description").value("Java քննություն"))
                .andExpect(jsonPath("$.contentItemId").value(contentItemId.toString()));

        verify(contentItemTranslationService).updateExamTranslation(
                eq(translationId),
                any(UpdateContentItemTranslationRequest.class)
        );
    }

    @Test
    @DisplayName("DELETE /api/v1/exam-translations/{id} -> should return 204")
    void delete_shouldReturnNoContent() throws Exception {
        UUID translationId = UUID.randomUUID();

        doNothing().when(contentItemTranslationService).deleteExamTranslation(translationId);

        mockMvc.perform(delete("/api/v1/exam-translations/{id}", translationId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(contentItemTranslationService).deleteExamTranslation(translationId);
    }
}