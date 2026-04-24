package com.melikyan.academy.controller;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.FilterType;
import com.melikyan.academy.security.RememberMeSecurityFilter;
import com.melikyan.academy.service.HomeworkSubmissionService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@WebMvcTest(
        controllers = HomeworkSubmissionController.class,
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
class HomeworkSubmissionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HomeworkSubmissionService homeworkSubmissionService;

    @Test
    @DisplayName("POST /api/v1/homework-submissions -> creates homework submission")
    void create_ShouldReturnCreatedSubmission() throws Exception {
        UUID taskId = UUID.randomUUID();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("student@test.com", null);

        String requestJson = """
                {
                  "answerPayload": {
                    "answer": "My answer"
                  },
                  "taskId": "%s"
                }
                """.formatted(taskId);

        when(homeworkSubmissionService.create(any(), any())).thenReturn(null);

        mockMvc.perform(post("/api/v1/homework-submissions")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());

        verify(homeworkSubmissionService).create(any(), any());
    }

    @Test
    @DisplayName("GET /api/v1/homework-submissions/me -> returns my submissions")
    void getMyAll_ShouldReturnMySubmissions() throws Exception {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("student@test.com", null);

        when(homeworkSubmissionService.getMyAll(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/homework-submissions/me")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(homeworkSubmissionService).getMyAll(any());
    }

    @Test
    @DisplayName("GET /api/v1/homework-submissions/me/{id} -> returns my submission by id")
    void getMyById_ShouldReturnMySubmission() throws Exception {
        UUID submissionId = UUID.randomUUID();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("student@test.com", null);

        when(homeworkSubmissionService.getMyById(eq(submissionId), any())).thenReturn(null);

        mockMvc.perform(get("/api/v1/homework-submissions/me/{id}", submissionId)
                        .principal(authentication))
                .andExpect(status().isOk());

        verify(homeworkSubmissionService).getMyById(eq(submissionId), any());
    }

    @Test
    @DisplayName("GET /api/v1/homework-submissions/me/task/{taskId} -> returns my submission by task")
    void getMyByTask_ShouldReturnMySubmissionByTask() throws Exception {
        UUID taskId = UUID.randomUUID();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("student@test.com", null);

        when(homeworkSubmissionService.getMyByTask(eq(taskId), any())).thenReturn(null);

        mockMvc.perform(get("/api/v1/homework-submissions/me/task/{taskId}", taskId)
                        .principal(authentication))
                .andExpect(status().isOk());

        verify(homeworkSubmissionService).getMyByTask(eq(taskId), any());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/v1/homework-submissions/{id} -> returns submission by id")
    void getById_ShouldReturnSubmission() throws Exception {
        UUID submissionId = UUID.randomUUID();

        when(homeworkSubmissionService.getById(submissionId)).thenReturn(null);

        mockMvc.perform(get("/api/v1/homework-submissions/{id}", submissionId))
                .andExpect(status().isOk());

        verify(homeworkSubmissionService).getById(submissionId);
    }

    @Test
    @WithMockUser(roles = {"PROFESSOR"})
    @DisplayName("GET /api/v1/homework-submissions/task/{taskId} -> returns submissions by task")
    void getAllByTask_ShouldReturnSubmissions() throws Exception {
        UUID taskId = UUID.randomUUID();

        when(homeworkSubmissionService.getAllByTask(taskId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/homework-submissions/task/{taskId}", taskId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(homeworkSubmissionService).getAllByTask(taskId);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("PATCH /api/v1/homework-submissions/{id} -> updates submission status")
    void update_ShouldReturnUpdatedSubmission() throws Exception {
        UUID submissionId = UUID.randomUUID();

        String requestJson = """
                {
                  "status": "PASSED",
                  "note": "Good work"
                }
                """;

        when(homeworkSubmissionService.update(eq(submissionId), any())).thenReturn(null);

        mockMvc.perform(patch("/api/v1/homework-submissions/{id}", submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        verify(homeworkSubmissionService).update(eq(submissionId), any());
    }

    @Test
    @DisplayName("DELETE /api/v1/homework-submissions/me/{id} -> deletes my submission")
    void deleteMy_ShouldReturnNoContent() throws Exception {
        UUID submissionId = UUID.randomUUID();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("student@test.com", null);

        mockMvc.perform(delete("/api/v1/homework-submissions/me/{id}", submissionId)
                        .principal(authentication))
                .andExpect(status().isNoContent());

        verify(homeworkSubmissionService).deleteMy(eq(submissionId), any());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("DELETE /api/v1/homework-submissions/{id} -> deletes submission")
    void delete_ShouldReturnNoContent() throws Exception {
        UUID submissionId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/homework-submissions/{id}", submissionId))
                .andExpect(status().isNoContent());

        verify(homeworkSubmissionService).delete(submissionId);
    }
}