package com.melikyan.academy.controller;

import com.melikyan.academy.dto.request.lessonTranslation.CreateLessonTranslationRequest;
import com.melikyan.academy.dto.request.lessonTranslation.UpdateLessonTranslationRequest;
import com.melikyan.academy.dto.response.lessonTranslation.LessonTranslationResponse;
import com.melikyan.academy.security.RememberMeSecurityFilter;
import com.melikyan.academy.service.LessonTranslationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = LessonTranslationController.class,
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
class LessonTranslationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LessonTranslationService lessonTranslationService;

    @Test
    void createLessonTranslation_shouldReturnCreated() throws Exception {
        UUID lessonId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

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

        when(lessonTranslationService.create(any(CreateLessonTranslationRequest.class)))
                .thenReturn(response);

        String requestJson = """
                {
                  "code": "en",
                  "title": "Introduction",
                  "description": "Introduction lesson",
                  "valueUrl": "https://example.com/video-en",
                  "lessonId": "%s",
                  "createdById": "%s"
                }
                """.formatted(lessonId, userId);

        mockMvc.perform(post("/api/v1/lesson-translations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());

        verify(lessonTranslationService).create(any(CreateLessonTranslationRequest.class));
    }

    @Test
    void getById_shouldReturnOk() throws Exception {
        UUID lessonId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

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

        when(lessonTranslationService.getById(translationId))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/lesson-translations/{id}", translationId))
                .andExpect(status().isOk());

        verify(lessonTranslationService).getById(translationId);
    }

    @Test
    void getAll_shouldReturnOk() throws Exception {
        UUID lessonId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        List<LessonTranslationResponse> responses = List.of(
                new LessonTranslationResponse(
                        UUID.randomUUID(),
                        "en",
                        "Introduction",
                        "Introduction lesson",
                        "https://example.com/video-en",
                        lessonId,
                        userId,
                        now,
                        now
                ),
                new LessonTranslationResponse(
                        UUID.randomUUID(),
                        "hy",
                        "Ներածություն",
                        "Ներածական դաս",
                        "https://example.com/video-hy",
                        lessonId,
                        userId,
                        now,
                        now
                )
        );

        when(lessonTranslationService.getAll())
                .thenReturn(responses);

        mockMvc.perform(get("/api/v1/lesson-translations"))
                .andExpect(status().isOk());

        verify(lessonTranslationService).getAll();
    }

    @Test
    void getByCode_shouldReturnOk() throws Exception {
        UUID lessonId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        List<LessonTranslationResponse> responses = List.of(
                new LessonTranslationResponse(
                        UUID.randomUUID(),
                        "en",
                        "Introduction",
                        "Introduction lesson",
                        "https://example.com/video-en",
                        lessonId,
                        userId,
                        now,
                        now
                )
        );

        when(lessonTranslationService.getByCode("en"))
                .thenReturn(responses);

        mockMvc.perform(get("/api/v1/lesson-translations/code/{code}", "en"))
                .andExpect(status().isOk());

        verify(lessonTranslationService).getByCode("en");
    }

    @Test
    void getByLessonId_shouldReturnOk() throws Exception {
        UUID lessonId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        List<LessonTranslationResponse> responses = List.of(
                new LessonTranslationResponse(
                        UUID.randomUUID(),
                        "en",
                        "Introduction",
                        "Introduction lesson",
                        "https://example.com/video-en",
                        lessonId,
                        userId,
                        now,
                        now
                )
        );

        when(lessonTranslationService.getByLessonId(lessonId))
                .thenReturn(responses);

        mockMvc.perform(get("/api/v1/lesson-translations/lesson/{lessonId}", lessonId))
                .andExpect(status().isOk());

        verify(lessonTranslationService).getByLessonId(lessonId);
    }

    @Test
    void getByLessonIdAndCode_shouldReturnOk() throws Exception {
        UUID lessonId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

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

        when(lessonTranslationService.getByLessonIdAndCode(lessonId, "en"))
                .thenReturn(response);

        mockMvc.perform(get(
                        "/api/v1/lesson-translations/lesson/{lessonId}/code/{code}",
                        lessonId,
                        "en"
                ))
                .andExpect(status().isOk());

        verify(lessonTranslationService).getByLessonIdAndCode(lessonId, "en");
    }

    @Test
    void update_shouldReturnOk() throws Exception {
        UUID lessonId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        LessonTranslationResponse response = new LessonTranslationResponse(
                translationId,
                "en",
                "Updated Introduction",
                "Updated introduction lesson",
                "https://example.com/video-en-updated",
                lessonId,
                userId,
                now,
                now
        );

        when(lessonTranslationService.update(eq(translationId), any(UpdateLessonTranslationRequest.class)))
                .thenReturn(response);

        String requestJson = """
                {
                  "code": "en",
                  "title": "Updated Introduction",
                  "description": "Updated introduction lesson",
                  "valueUrl": "https://example.com/video-en-updated"
                }
                """;

        mockMvc.perform(patch("/api/v1/lesson-translations/{id}", translationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        verify(lessonTranslationService).update(eq(translationId), any(UpdateLessonTranslationRequest.class));
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        UUID translationId = UUID.randomUUID();

        doNothing().when(lessonTranslationService).delete(translationId);

        mockMvc.perform(delete("/api/v1/lesson-translations/{id}", translationId))
                .andExpect(status().isNoContent());

        verify(lessonTranslationService).delete(translationId);
    }
}