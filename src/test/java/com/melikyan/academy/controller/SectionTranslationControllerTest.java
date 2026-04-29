package com.melikyan.academy.controller;

import com.melikyan.academy.dto.request.sectionTranslation.CreateSectionTranslationRequest;
import com.melikyan.academy.dto.request.sectionTranslation.UpdateSectionTranslationRequest;
import com.melikyan.academy.dto.response.sectionTranslation.SectionTranslationResponse;
import com.melikyan.academy.security.RememberMeSecurityFilter;
import com.melikyan.academy.service.SectionTranslationService;
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
        controllers = SectionTranslationController.class,
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
class SectionTranslationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SectionTranslationService sectionTranslationService;

    @Test
    void createSectionTranslation_shouldReturnCreated() throws Exception {
        UUID examSectionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

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

        when(sectionTranslationService.create(any(CreateSectionTranslationRequest.class)))
                .thenReturn(response);

        String requestJson = """
                {
                  "code": "en",
                  "title": "Section title",
                  "description": "Section description",
                  "examSectionId": "%s",
                  "createdById": "%s"
                }
                """.formatted(examSectionId, userId);

        mockMvc.perform(post("/api/v1/section-translations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());

        verify(sectionTranslationService).create(any(CreateSectionTranslationRequest.class));
    }

    @Test
    void getById_shouldReturnOk() throws Exception {
        UUID examSectionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

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

        when(sectionTranslationService.getById(translationId))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/section-translations/{id}", translationId))
                .andExpect(status().isOk());

        verify(sectionTranslationService).getById(translationId);
    }

    @Test
    void getAll_shouldReturnOk() throws Exception {
        UUID examSectionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        List<SectionTranslationResponse> responses = List.of(
                new SectionTranslationResponse(
                        UUID.randomUUID(),
                        "Section title",
                        "Section description",
                        "en",
                        examSectionId,
                        userId,
                        now,
                        now
                ),
                new SectionTranslationResponse(
                        UUID.randomUUID(),
                        "Բաժին",
                        "Բաժնի նկարագրություն",
                        "hy",
                        examSectionId,
                        userId,
                        now,
                        now
                )
        );

        when(sectionTranslationService.getAll())
                .thenReturn(responses);

        mockMvc.perform(get("/api/v1/section-translations"))
                .andExpect(status().isOk());

        verify(sectionTranslationService).getAll();
    }

    @Test
    void getByCode_shouldReturnOk() throws Exception {
        UUID examSectionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        List<SectionTranslationResponse> responses = List.of(
                new SectionTranslationResponse(
                        UUID.randomUUID(),
                        "Section title",
                        "Section description",
                        "en",
                        examSectionId,
                        userId,
                        now,
                        now
                )
        );

        when(sectionTranslationService.getByCode("en"))
                .thenReturn(responses);

        mockMvc.perform(get("/api/v1/section-translations/code/{code}", "en"))
                .andExpect(status().isOk());

        verify(sectionTranslationService).getByCode("en");
    }

    @Test
    void getByExamSectionId_shouldReturnOk() throws Exception {
        UUID examSectionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        List<SectionTranslationResponse> responses = List.of(
                new SectionTranslationResponse(
                        UUID.randomUUID(),
                        "Section title",
                        "Section description",
                        "en",
                        examSectionId,
                        userId,
                        now,
                        now
                )
        );

        when(sectionTranslationService.getByExamSectionId(examSectionId))
                .thenReturn(responses);

        mockMvc.perform(get("/api/v1/section-translations/exam-section/{examSectionId}", examSectionId))
                .andExpect(status().isOk());

        verify(sectionTranslationService).getByExamSectionId(examSectionId);
    }

    @Test
    void getByExamSectionIdAndCode_shouldReturnOk() throws Exception {
        UUID examSectionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

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

        when(sectionTranslationService.getByExamSectionIdAndCode(examSectionId, "en"))
                .thenReturn(response);

        mockMvc.perform(get(
                        "/api/v1/section-translations/exam-section/{examSectionId}/code/{code}",
                        examSectionId,
                        "en"
                ))
                .andExpect(status().isOk());

        verify(sectionTranslationService).getByExamSectionIdAndCode(examSectionId, "en");
    }

    @Test
    void update_shouldReturnOk() throws Exception {
        UUID examSectionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        SectionTranslationResponse response = new SectionTranslationResponse(
                translationId,
                "Բաժին",
                "Թարմացված նկարագրություն",
                "hy",
                examSectionId,
                userId,
                now,
                now
        );

        when(sectionTranslationService.update(eq(translationId), any(UpdateSectionTranslationRequest.class)))
                .thenReturn(response);

        String requestJson = """
                {
                  "code": "hy",
                  "title": "Բաժին",
                  "description": "Թարմացված նկարագրություն"
                }
                """;

        mockMvc.perform(patch("/api/v1/section-translations/{id}", translationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        verify(sectionTranslationService).update(eq(translationId), any(UpdateSectionTranslationRequest.class));
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        UUID translationId = UUID.randomUUID();

        doNothing().when(sectionTranslationService).delete(translationId);

        mockMvc.perform(delete("/api/v1/section-translations/{id}", translationId))
                .andExpect(status().isNoContent());

        verify(sectionTranslationService).delete(translationId);
    }
}
