package com.melikyan.academy.controller;

import org.springframework.test.web.servlet.MockMvc;
import com.melikyan.academy.service.UserProcessService;
import org.springframework.context.annotation.FilterType;
import com.melikyan.academy.security.RememberMeSecurityFilter;
import com.melikyan.academy.dto.response.userProcess.UserProcessResponse;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(
        controllers = UserProcessController.class,
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
@Import(UserProcessControllerTest.MethodSecurityTestConfig.class)
class UserProcessControllerTest {
    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityTestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserProcessService userProcessService;

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("GET /api/v1/me/progress -> returns my progress list")
    void getMyProgress_ShouldReturnProgressList() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID contentItemId = UUID.randomUUID();

        UserProcessResponse response = new UserProcessResponse(
                id,
                3,
                BigDecimal.valueOf(25.50),
                userId,
                contentItemId,
                OffsetDateTime.parse("2026-04-23T12:00:00Z"),
                OffsetDateTime.parse("2026-04-20T10:00:00Z"),
                OffsetDateTime.parse("2026-04-23T12:00:00Z")
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("test@test.com", null);

        when(userProcessService.getMyProgress("test@test.com"))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/me/progress").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].currentStep").value(3))
                .andExpect(jsonPath("$[0].scoreAccumulated").value(25.50))
                .andExpect(jsonPath("$[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$[0].contentItemId").value(contentItemId.toString()));

        verify(userProcessService).getMyProgress("test@test.com");
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("GET /api/v1/me/progress/content-items/{contentItemId} -> returns my progress by content item id")
    void getMyProgressByContentItemId_ShouldReturnProgress() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID contentItemId = UUID.randomUUID();

        UserProcessResponse response = new UserProcessResponse(
                id,
                2,
                BigDecimal.valueOf(10.00),
                userId,
                contentItemId,
                OffsetDateTime.parse("2026-04-23T12:00:00Z"),
                OffsetDateTime.parse("2026-04-20T10:00:00Z"),
                OffsetDateTime.parse("2026-04-23T12:00:00Z")
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("test@test.com", null);

        when(userProcessService.getMyProgressByContentItemId(contentItemId, "test@test.com"))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/me/progress/content-items/{contentItemId}", contentItemId)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.currentStep").value(2))
                .andExpect(jsonPath("$.scoreAccumulated").value(10.00))
                .andExpect(jsonPath("$.contentItemId").value(contentItemId.toString()));

        verify(userProcessService).getMyProgressByContentItemId(contentItemId, "test@test.com");
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/v1/users/{userId}/progress -> returns user progress list")
    void getUserProgress_ShouldReturnProgressList() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID contentItemId = UUID.randomUUID();

        UserProcessResponse response = new UserProcessResponse(
                id,
                5,
                BigDecimal.valueOf(40.00),
                userId,
                contentItemId,
                OffsetDateTime.parse("2026-04-23T12:00:00Z"),
                OffsetDateTime.parse("2026-04-20T10:00:00Z"),
                OffsetDateTime.parse("2026-04-23T12:00:00Z")
        );

        when(userProcessService.getUserProgress(userId))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/users/{userId}/progress", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$[0].contentItemId").value(contentItemId.toString()))
                .andExpect(jsonPath("$[0].currentStep").value(5));

        verify(userProcessService).getUserProgress(userId);
    }

    @Test
    @WithMockUser(roles = {"PROFESSOR"})
    @DisplayName("GET /api/v1/users/{userId}/progress/content-items/{contentItemId} -> returns user progress by content item id")
    void getUserProgressByContentItemId_ShouldReturnProgress() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID contentItemId = UUID.randomUUID();

        UserProcessResponse response = new UserProcessResponse(
                id,
                1,
                BigDecimal.valueOf(5.00),
                userId,
                contentItemId,
                OffsetDateTime.parse("2026-04-23T12:00:00Z"),
                OffsetDateTime.parse("2026-04-20T10:00:00Z"),
                OffsetDateTime.parse("2026-04-23T12:00:00Z")
        );

        when(userProcessService.getUserProgressByContentItemId(userId, contentItemId))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/users/{userId}/progress/content-items/{contentItemId}", userId, contentItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.contentItemId").value(contentItemId.toString()))
                .andExpect(jsonPath("$.scoreAccumulated").value(5.00));

        verify(userProcessService).getUserProgressByContentItemId(userId, contentItemId);
    }
}