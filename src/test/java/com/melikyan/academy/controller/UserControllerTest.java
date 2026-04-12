package com.melikyan.academy.controller;

import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;
import com.melikyan.academy.entity.enums.Role;
import com.melikyan.academy.service.UserService;
import com.melikyan.academy.config.SecurityConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import com.melikyan.academy.security.AppUserDetailsService;
import com.melikyan.academy.security.RememberMeCookieService;
import com.melikyan.academy.security.RememberMeLogoutHandler;
import com.melikyan.academy.security.RestAccessDeniedHandler;
import com.melikyan.academy.repository.RememberMeTokenRepository;
import com.melikyan.academy.dto.response.user.UserProfileResponse;
import com.melikyan.academy.security.RestAuthenticationEntryPoint;
import com.melikyan.academy.dto.request.user.ChangePasswordRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;
import java.time.OffsetDateTime;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@Import(SecurityConfig.class)
@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AppUserDetailsService appUserDetailsService;

    @MockitoBean
    private RememberMeCookieService rememberMeCookieService;

    @MockitoBean
    private RememberMeLogoutHandler rememberMeLogoutHandler;

    @MockitoBean
    private RememberMeTokenRepository rememberMeTokenRepository;

    @MockitoBean
    private RestAccessDeniedHandler restAccessDeniedHandler;

    @MockitoBean
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    private UserProfileResponse profileResponse;

    @BeforeEach
    void setUp() throws Exception {
        profileResponse = new UserProfileResponse(
                UUID.randomUUID(),
                "test@test.com",
                "Test",
                "Test",
                null,
                null,
                Role.STUDENT,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        doAnswer(invocation -> {
            var response = invocation.getArgument(1, jakarta.servlet.http.HttpServletResponse.class);
            response.sendError(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }).when(restAuthenticationEntryPoint).commence(any(), any(), any());

        doAnswer(invocation -> {
            var response = invocation.getArgument(1, jakarta.servlet.http.HttpServletResponse.class);
            response.sendError(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
            return null;
        }).when(restAccessDeniedHandler).handle(any(), any(), any());
    }

    @Test
    @WithMockUser(username = "test@test.com", roles = "STUDENT")
    void getCurrentUser_shouldReturn200WithProfile() throws Exception {
        when(userService.getCurrentUserProfile()).thenReturn(profileResponse);

        mockMvc.perform(get("/api/v1/user/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("Test"));
    }

    @Test
    void getCurrentUser_whenNotAuthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/user/me"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getCurrentUserProfile();
    }

    @Test
    @WithMockUser(username = "test@test.com", roles = "STUDENT")
    void updateProfile_withValidData_shouldReturn200() throws Exception {
        when(userService.updateCurrentUserProfile(any())).thenReturn(profileResponse);

        mockMvc.perform(multipart("/api/v1/user/me")
                        .param("firstName", "Jane")
                        .param("lastName", "Smith")
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"));

        verify(userService, times(1)).updateCurrentUserProfile(any());
    }

    @Test
    @WithMockUser(username = "test@test.com", roles = "STUDENT")
    void updateProfile_withAvatar_shouldReturn200() throws Exception {
        when(userService.updateCurrentUserProfile(any())).thenReturn(profileResponse);

        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "avatar.jpg",
                "image/jpeg",
                "fake image bytes".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/user/me")
                        .file(avatar)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

        verify(userService, times(1)).updateCurrentUserProfile(any());
    }

    @Test
    @WithMockUser(username = "test@test.com", roles = "STUDENT")
    void changePassword_withValidData_shouldReturn204() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "current_password",
                "new_password",
                "new_password"
        );

        mockMvc.perform(patch("/api/v1/user/me/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).changePassword(any());
    }

    @Test
    @WithMockUser(username = "test@test.com", roles = "STUDENT")
    void changePassword_whenCurrentPasswordBlank_shouldReturn400() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "",
                "new_password",
                "new_password"
        );

        mockMvc.perform(patch("/api/v1/user/me/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).changePassword(any());
    }

    @Test
    @WithMockUser(username = "test@test.com", roles = "STUDENT")
    void changePassword_whenNewPasswordTooShort_shouldReturn400() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "current_password",
                "short",
                "short"
        );

        mockMvc.perform(patch("/api/v1/user/me/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).changePassword(any());
    }

    @Test
    @WithMockUser(username = "test@test.com", roles = "STUDENT")
    void deleteCurrentUser_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/user/me")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteCurrentUser();
    }

    @Test
    void deleteCurrentUser_whenNotAuthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(delete("/api/v1/user/me").with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).deleteCurrentUser();
    }
}