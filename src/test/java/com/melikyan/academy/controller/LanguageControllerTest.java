package com.melikyan.academy.controller;

import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;
import com.melikyan.academy.service.LanguageService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.FilterType;
import com.melikyan.academy.security.RememberMeSecurityFilter;
import com.melikyan.academy.dto.response.language.LanguageResponse;
import com.melikyan.academy.dto.request.language.UpdateLanguageRequest;
import com.melikyan.academy.dto.request.language.CreateLanguageRequest;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
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
        controllers = LanguageController.class,
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
class LanguageControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LanguageService languageService;

    @Test
    void createLanguage_shouldReturnCreated() throws Exception {
        UUID languageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        CreateLanguageRequest request = new CreateLanguageRequest(
                "en",
                "English",
                userId
        );

        LanguageResponse response = new LanguageResponse(
                languageId,
                "en",
                "English",
                userId,
                now,
                now
        );

        when(languageService.create(any(CreateLanguageRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/languages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(languageService).create(any(CreateLanguageRequest.class));
    }

    @Test
    void getById_shouldReturnOk() throws Exception {
        UUID languageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        LanguageResponse response = new LanguageResponse(
                languageId,
                "en",
                "English",
                userId,
                now,
                now
        );

        when(languageService.getById(languageId))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/languages/{id}", languageId))
                .andExpect(status().isOk());

        verify(languageService).getById(languageId);
    }

    @Test
    void getByCode_shouldReturnOk() throws Exception {
        UUID languageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        LanguageResponse response = new LanguageResponse(
                languageId,
                "en",
                "English",
                userId,
                now,
                now
        );

        when(languageService.getByCode("en"))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/languages/code/{code}", "en"))
                .andExpect(status().isOk());

        verify(languageService).getByCode("en");
    }

    @Test
    void getAll_shouldReturnOk() throws Exception {
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        List<LanguageResponse> responses = List.of(
                new LanguageResponse(
                        UUID.randomUUID(),
                        "en",
                        "English",
                        userId,
                        now,
                        now
                ),
                new LanguageResponse(
                        UUID.randomUUID(),
                        "hy",
                        "Armenian",
                        userId,
                        now,
                        now
                )
        );

        when(languageService.getAll())
                .thenReturn(responses);

        mockMvc.perform(get("/api/v1/languages"))
                .andExpect(status().isOk());

        verify(languageService).getAll();
    }

    @Test
    void update_shouldReturnOk() throws Exception {
        UUID languageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        UpdateLanguageRequest request = new UpdateLanguageRequest(
                "English Updated",
                "en"
        );

        LanguageResponse response = new LanguageResponse(
                languageId,
                "en",
                "English Updated",
                userId,
                now,
                now
        );

        when(languageService.update(eq(languageId), any(UpdateLanguageRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/languages/{id}", languageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(languageService).update(eq(languageId), any(UpdateLanguageRequest.class));
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        UUID languageId = UUID.randomUUID();

        doNothing().when(languageService).delete(languageId);

        mockMvc.perform(delete("/api/v1/languages/{id}", languageId))
                .andExpect(status().isNoContent());

        verify(languageService).delete(languageId);
    }
}