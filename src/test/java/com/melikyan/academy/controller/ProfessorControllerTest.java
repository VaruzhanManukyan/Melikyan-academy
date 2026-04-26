package com.melikyan.academy.controller;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.FilterType;
import com.melikyan.academy.security.RememberMeSecurityFilter;
import com.melikyan.academy.service.ProfessorService;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@WebMvcTest(
        controllers = ProfessorController.class,
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
class ProfessorControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfessorService professorService;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/v1/professors -> creates professor user")
    void create_ShouldReturnCreatedProfessorUser() throws Exception {
        String requestJson = """
                {
                  "firstName": "Aram",
                  "lastName": "Petrosyan",
                  "email": "aram.professor@test.com",
                  "password": "Password123!",
                  "confirmPassword": "Password123!"
                }
                """;

        when(professorService.create(any())).thenReturn(null);

        mockMvc.perform(post("/api/v1/professors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());

        verify(professorService).create(any());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/v1/professors/assign -> assigns professor to course")
    void assign_ShouldReturnCreatedProfessorAssignment() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        String requestJson = """
                {
                  "userId": "%s",
                  "courseId": "%s"
                }
                """.formatted(userId, courseId);

        when(professorService.assign(any())).thenReturn(null);

        mockMvc.perform(post("/api/v1/professors/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());

        verify(professorService).assign(any());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/v1/professors -> returns all professor users")
    void getAllProfessorUsers_ShouldReturnProfessorUsers() throws Exception {
        when(professorService.getAllProfessorUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/professors"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(professorService).getAllProfessorUsers();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/v1/professors/{id} -> returns professor assignment by id")
    void getById_ShouldReturnProfessorAssignment() throws Exception {
        UUID professorId = UUID.randomUUID();

        when(professorService.getById(professorId)).thenReturn(null);

        mockMvc.perform(get("/api/v1/professors/{id}", professorId))
                .andExpect(status().isOk());

        verify(professorService).getById(professorId);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/v1/professors/courses/{courseId} -> returns professor assignments by course")
    void getAllByCourse_ShouldReturnProfessorAssignmentsByCourse() throws Exception {
        UUID courseId = UUID.randomUUID();

        when(professorService.getAllByCourse(courseId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/professors/courses/{courseId}", courseId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(professorService).getAllByCourse(courseId);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/v1/professors/users/{userId} -> returns professor assignments by user")
    void getAllByUser_ShouldReturnProfessorAssignmentsByUser() throws Exception {
        UUID userId = UUID.randomUUID();

        when(professorService.getAllByUser(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/professors/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(professorService).getAllByUser(userId);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("DELETE /api/v1/professors/users/{userId}/courses/{courseId} -> removes professor from course")
    void delete_ShouldReturnNoContent() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/professors/users/{userId}/courses/{courseId}", userId, courseId))
                .andExpect(status().isNoContent());

        verify(professorService).delete(eq(userId), eq(courseId));
    }
}