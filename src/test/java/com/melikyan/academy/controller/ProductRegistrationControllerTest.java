package com.melikyan.academy.controller;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.FilterType;
import com.melikyan.academy.security.RememberMeSecurityFilter;
import com.melikyan.academy.service.ProductRegistrationService;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

@WebMvcTest(
        controllers = ProductRegistrationController.class,
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
class ProductRegistrationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductRegistrationService productRegistrationService;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/v1/product-registrations/grant -> grants access")
    void grantAccess_ShouldReturnCreatedRegistration() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        String requestJson = """
                {
                  "userId": "%s",
                  "productId": "%s",
                  "transactionId": "%s"
                }
                """.formatted(userId, productId, transactionId);

        when(productRegistrationService.grantAccess(any())).thenReturn(null);

        mockMvc.perform(post("/api/v1/product-registrations/grant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());

        verify(productRegistrationService).grantAccess(any());
    }

    @Test
    @WithMockUser(roles = {"PROFESSOR"})
    @DisplayName("GET /api/v1/product-registrations/{id} -> returns registration by id")
    void getById_ShouldReturnRegistration() throws Exception {
        UUID registrationId = UUID.randomUUID();

        when(productRegistrationService.getById(registrationId)).thenReturn(null);

        mockMvc.perform(get("/api/v1/product-registrations/{id}", registrationId))
                .andExpect(status().isOk());

        verify(productRegistrationService).getById(registrationId);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/v1/product-registrations/users/{userId} -> returns registrations by user id")
    void getAllByUserId_ShouldReturnRegistrations() throws Exception {
        UUID userId = UUID.randomUUID();

        when(productRegistrationService.getByUserId(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/product-registrations/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(productRegistrationService).getByUserId(userId);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/v1/product-registrations/products/{productId} -> returns registrations by product id")
    void getAllByProductId_ShouldReturnRegistrations() throws Exception {
        UUID productId = UUID.randomUUID();

        when(productRegistrationService.getByProductId(productId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/product-registrations/products/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(productRegistrationService).getByProductId(productId);
    }

    @Test
    @DisplayName("GET /api/v1/product-registrations/me -> returns my registrations")
    void getMyRegistrations_ShouldReturnRegistrations() throws Exception {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("test@test.com", null);

        when(productRegistrationService.getMyRegistrations("test@test.com"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/product-registrations/me")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(productRegistrationService).getMyRegistrations("test@test.com");
    }

    @Test
    @DisplayName("GET /api/v1/product-registrations/me/{id} -> returns my registration by id")
    void getMyRegistrationById_ShouldReturnRegistration() throws Exception {
        UUID registrationId = UUID.randomUUID();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("test@test.com", null);

        when(productRegistrationService.getMyRegistrationById(registrationId, "test@test.com"))
                .thenReturn(null);

        mockMvc.perform(get("/api/v1/product-registrations/me/{id}", registrationId)
                        .principal(authentication))
                .andExpect(status().isOk());

        verify(productRegistrationService).getMyRegistrationById(registrationId, "test@test.com");
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("PATCH /api/v1/product-registrations/{id}/activate -> activates registration")
    void activate_ShouldReturnUpdatedRegistration() throws Exception {
        UUID registrationId = UUID.randomUUID();

        when(productRegistrationService.activate(registrationId)).thenReturn(null);

        mockMvc.perform(patch("/api/v1/product-registrations/{id}/activate", registrationId))
                .andExpect(status().isOk());

        verify(productRegistrationService).activate(registrationId);
    }

    @Test
    @WithMockUser(roles = {"PROFESSOR"})
    @DisplayName("PATCH /api/v1/product-registrations/{id}/suspend -> suspends registration")
    void suspend_ShouldReturnUpdatedRegistration() throws Exception {
        UUID registrationId = UUID.randomUUID();

        when(productRegistrationService.suspend(registrationId)).thenReturn(null);

        mockMvc.perform(patch("/api/v1/product-registrations/{id}/suspend", registrationId))
                .andExpect(status().isOk());

        verify(productRegistrationService).suspend(registrationId);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("PATCH /api/v1/product-registrations/{id}/expire -> expires registration")
    void expire_ShouldReturnUpdatedRegistration() throws Exception {
        UUID registrationId = UUID.randomUUID();

        when(productRegistrationService.expire(registrationId)).thenReturn(null);

        mockMvc.perform(patch("/api/v1/product-registrations/{id}/expire", registrationId))
                .andExpect(status().isOk());

        verify(productRegistrationService).expire(registrationId);
    }
}