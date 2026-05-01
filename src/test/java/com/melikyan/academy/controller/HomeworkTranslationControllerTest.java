package com.melikyan.academy.controller;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.FilterType;
import com.melikyan.academy.security.RememberMeSecurityFilter;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import com.melikyan.academy.dto.response.homeworkTranslation.HomeworkTranslationResponse;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import com.melikyan.academy.dto.request.homeworkTranslation.CreateHomeworkTranslationRequest;
import com.melikyan.academy.dto.request.homeworkTranslation.UpdateHomeworkTranslationRequest;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.junit.jupiter.api.Test;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@WebMvcTest(
        controllers = HomeworkTranslationController.class,
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
class HomeworkTranslationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HomeworkTranslationService homeworkTranslationService;

    @Test
    void createHomeworkTranslation_shouldReturnCreated() throws Exception {
        UUID homeworkId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

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

        when(homeworkTranslationService.create(any(CreateHomeworkTranslationRequest.class)))
                .thenReturn(response);

        String requestJson = """
                {
                  "code": "en",
                  "title": "Homework title",
                  "description": "Homework description",
                  "homeworkId": "%s",
                  "createdById": "%s"
                }
                """.formatted(homeworkId, userId);

        mockMvc.perform(post("/api/v1/homework-translations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());

        verify(homeworkTranslationService).create(any(CreateHomeworkTranslationRequest.class));
    }

    @Test
    void getById_shouldReturnOk() throws Exception {
        UUID homeworkId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

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

        when(homeworkTranslationService.getById(translationId))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/homework-translations/{id}", translationId))
                .andExpect(status().isOk());

        verify(homeworkTranslationService).getById(translationId);
    }

    @Test
    void getAll_shouldReturnOk() throws Exception {
        UUID homeworkId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        List<HomeworkTranslationResponse> responses = List.of(
                new HomeworkTranslationResponse(
                        UUID.randomUUID(),
                        "en",
                        "Homework title",
                        "Homework description",
                        homeworkId,
                        userId,
                        now,
                        now
                ),
                new HomeworkTranslationResponse(
                        UUID.randomUUID(),
                        "hy",
                        "Տնային աշխատանք",
                        "Տնային աշխատանքի նկարագրություն",
                        homeworkId,
                        userId,
                        now,
                        now
                )
        );

        when(homeworkTranslationService.getAll())
                .thenReturn(responses);

        mockMvc.perform(get("/api/v1/homework-translations"))
                .andExpect(status().isOk());

        verify(homeworkTranslationService).getAll();
    }

    @Test
    void getByCode_shouldReturnOk() throws Exception {
        UUID homeworkId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        List<HomeworkTranslationResponse> responses = List.of(
                new HomeworkTranslationResponse(
                        UUID.randomUUID(),
                        "en",
                        "Homework title",
                        "Homework description",
                        homeworkId,
                        userId,
                        now,
                        now
                )
        );

        when(homeworkTranslationService.getByCode("en"))
                .thenReturn(responses);

        mockMvc.perform(get("/api/v1/homework-translations/code/{code}", "en"))
                .andExpect(status().isOk());

        verify(homeworkTranslationService).getByCode("en");
    }

    @Test
    void getByHomeworkId_shouldReturnOk() throws Exception {
        UUID homeworkId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        List<HomeworkTranslationResponse> responses = List.of(
                new HomeworkTranslationResponse(
                        UUID.randomUUID(),
                        "en",
                        "Homework title",
                        "Homework description",
                        homeworkId,
                        userId,
                        now,
                        now
                )
        );

        when(homeworkTranslationService.getByHomeworkId(homeworkId))
                .thenReturn(responses);

        mockMvc.perform(get("/api/v1/homework-translations/homework/{homeworkId}", homeworkId))
                .andExpect(status().isOk());

        verify(homeworkTranslationService).getByHomeworkId(homeworkId);
    }

    @Test
    void getByHomeworkIdAndCode_shouldReturnOk() throws Exception {
        UUID homeworkId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

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

        when(homeworkTranslationService.getByHomeworkIdAndCode(homeworkId, "en"))
                .thenReturn(response);

        mockMvc.perform(get(
                        "/api/v1/homework-translations/homework/{homeworkId}/code/{code}",
                        homeworkId,
                        "en"
                ))
                .andExpect(status().isOk());

        verify(homeworkTranslationService).getByHomeworkIdAndCode(homeworkId, "en");
    }

    @Test
    void update_shouldReturnOk() throws Exception {
        UUID homeworkId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        HomeworkTranslationResponse response = new HomeworkTranslationResponse(
                translationId,
                "en",
                "Updated homework title",
                "Updated homework description",
                homeworkId,
                userId,
                now,
                now
        );

        when(homeworkTranslationService.update(eq(translationId), any(UpdateHomeworkTranslationRequest.class)))
                .thenReturn(response);

        String requestJson = """
                {
                  "code": "en",
                  "title": "Updated homework title",
                  "description": "Updated homework description"
                }
                """;

        mockMvc.perform(patch("/api/v1/homework-translations/{id}", translationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        verify(homeworkTranslationService).update(eq(translationId), any(UpdateHomeworkTranslationRequest.class));
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        UUID translationId = UUID.randomUUID();

        doNothing().when(homeworkTranslationService).delete(translationId);

        mockMvc.perform(delete("/api/v1/homework-translations/{id}", translationId))
                .andExpect(status().isNoContent());

        verify(homeworkTranslationService).delete(translationId);
    }
}