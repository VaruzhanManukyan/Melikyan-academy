package com.melikyan.academy.controller;

import tools.jackson.databind.ObjectMapper;
import com.melikyan.academy.entity.enums.Role;
import com.melikyan.academy.service.AuthService;
import org.springframework.test.web.servlet.MockMvc;
import com.melikyan.academy.dto.request.auth.LoginRequest;
import com.melikyan.academy.dto.response.auth.LoginResponse;
import com.melikyan.academy.security.RememberMeCookieService;
import com.melikyan.academy.security.RememberMeLogoutHandler;
import com.melikyan.academy.dto.request.auth.RegisterRequest;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import com.melikyan.academy.security.RememberMeSecurityFilter;
import com.melikyan.academy.dto.response.auth.RegisterResponse;
import com.melikyan.academy.repository.RememberMeTokenRepository;
import com.melikyan.academy.dto.response.user.UserProfileResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.util.UUID;
import java.time.OffsetDateTime;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private RememberMeCookieService rememberMeCookieService;

    @MockitoBean
    private RememberMeSecurityFilter rememberMeSecurityFilter;

    @MockitoBean
    private RememberMeLogoutHandler rememberMeLogoutHandler;

    @MockitoBean
    private RememberMeTokenRepository rememberMeTokenRepository;

    @Test
    void getCsrfToken_shouldReturnToken() throws Exception {
        DefaultCsrfToken csrfToken = new DefaultCsrfToken(
                "X-XSRF-TOKEN",
                "_csrf",
                "test-csrf-token"
        );

        mockMvc.perform(get("/api/v1/auth/csrf")
                        .requestAttr("_csrf", csrfToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName").value("X-XSRF-TOKEN"))
                .andExpect(jsonPath("$.parameterName").value("_csrf"))
                .andExpect(jsonPath("$.token").value("test-csrf-token"));
    }

    @Test
    void register_shouldReturnCreated() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "test@test.com",
                "StrongPass123",
                "StrongPass123",
                "Test",
                "User"
        );

        RegisterResponse response = new RegisterResponse(
                "Registration successful",
                new UserProfileResponse(
                        UUID.randomUUID(),
                        "test@test.com",
                        "Test",
                        "User",
                        null,
                        null,
                        Role.STUDENT,
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                )
        );

        given(authService.register(any(RegisterRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registration successful"))
                .andExpect(jsonPath("$.user.email").value("test@test.com"))
                .andExpect(jsonPath("$.user.firstName").value("Test"))
                .andExpect(jsonPath("$.user.lastName").value("User"))
                .andExpect(jsonPath("$.user.role").value("STUDENT"));
    }

    @Test
    void login_shouldReturnOk() throws Exception {
        LoginRequest request = new LoginRequest(
                "test@test.com",
                "StrongPass123",
                true
        );

        LoginResponse response = new LoginResponse(
                true,
                "Login successful",
                new UserProfileResponse(
                        UUID.randomUUID(),
                        "test@test.com",
                        "Test",
                        "User",
                        null,
                        null,
                        Role.STUDENT,
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                )
        );

        given(authService.login(any(LoginRequest.class), any(), any())).willReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.user.email").value("test@test.com"));
    }
}
