package com.melikyan.academy.controller;

import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;
import com.melikyan.academy.entity.enums.ExamStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.Authentication;
import org.springframework.context.annotation.FilterType;
import com.melikyan.academy.service.ExamSubmissionService;
import com.melikyan.academy.security.RememberMeSecurityFilter;
import com.melikyan.academy.dto.response.examSubmission.ExamSubmissionResponse;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import com.melikyan.academy.dto.request.examSubmission.CreateExamSubmissionRequest;
import com.melikyan.academy.dto.request.examSubmission.UpdateExamSubmissionRequest;
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

import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.time.OffsetDateTime;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(
        controllers = ExamSubmissionController.class,
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
class ExamSubmissionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExamSubmissionService examSubmissionService;

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("POST /api/v1/exam-submissions -> creates exam submission")
    void create_ShouldReturnCreatedExamSubmission() throws Exception {
        UUID submissionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        String requestJson = """
                {
                  "answerPayload": {
                    "answer": "My exam answer",
                    "language": "java"
                  },
                  "taskId": "%s"
                }
                """.formatted(taskId);

        ExamSubmissionResponse response = new ExamSubmissionResponse(
                submissionId,
                null,
                ExamStatus.PENDING_REVIEW,
                Map.of(
                        "answer", "My exam answer",
                        "language", "java"
                ),
                userId,
                taskId,
                OffsetDateTime.parse("2026-04-20T14:00:00+04:00"),
                OffsetDateTime.parse("2026-04-20T14:00:00+04:00")
        );

        when(examSubmissionService.create(
                any(CreateExamSubmissionRequest.class),
                nullable(Authentication.class)
        )).thenReturn(response);

        mockMvc.perform(post("/api/v1/exam-submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(submissionId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING_REVIEW"))
                .andExpect(jsonPath("$.answerPayload.answer").value("My exam answer"))
                .andExpect(jsonPath("$.answerPayload.language").value("java"))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.taskId").value(taskId.toString()));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("GET /api/v1/exam-submissions/me -> returns my exam submissions")
    void getMyAll_ShouldReturnMyExamSubmissions() throws Exception {
        UUID userId = UUID.randomUUID();

        ExamSubmissionResponse first = new ExamSubmissionResponse(
                UUID.randomUUID(),
                null,
                ExamStatus.PENDING_REVIEW,
                Map.of("answer", "First answer"),
                userId,
                UUID.randomUUID(),
                OffsetDateTime.parse("2026-04-20T14:00:00+04:00"),
                OffsetDateTime.parse("2026-04-20T14:00:00+04:00")
        );

        ExamSubmissionResponse second = new ExamSubmissionResponse(
                UUID.randomUUID(),
                "Good work",
                ExamStatus.PASSED,
                Map.of("answer", "Second answer"),
                userId,
                UUID.randomUUID(),
                OffsetDateTime.parse("2026-04-21T14:00:00+04:00"),
                OffsetDateTime.parse("2026-04-21T15:00:00+04:00")
        );

        when(examSubmissionService.getMyAll(nullable(Authentication.class)))
                .thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/v1/exam-submissions/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("PENDING_REVIEW"))
                .andExpect(jsonPath("$[0].answerPayload.answer").value("First answer"))
                .andExpect(jsonPath("$[1].status").value("PASSED"))
                .andExpect(jsonPath("$[1].note").value("Good work"))
                .andExpect(jsonPath("$[1].answerPayload.answer").value("Second answer"));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("GET /api/v1/exam-submissions/me/{id} -> returns my exam submission by id")
    void getMyById_ShouldReturnMyExamSubmission() throws Exception {
        UUID submissionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        ExamSubmissionResponse response = new ExamSubmissionResponse(
                submissionId,
                null,
                ExamStatus.PENDING_REVIEW,
                Map.of("answer", "My answer"),
                userId,
                taskId,
                OffsetDateTime.parse("2026-04-20T14:00:00+04:00"),
                OffsetDateTime.parse("2026-04-20T14:00:00+04:00")
        );

        when(examSubmissionService.getMyById(eq(submissionId), nullable(Authentication.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/exam-submissions/me/{id}", submissionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(submissionId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING_REVIEW"))
                .andExpect(jsonPath("$.answerPayload.answer").value("My answer"))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.taskId").value(taskId.toString()));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("GET /api/v1/exam-submissions/me/task/{taskId} -> returns my exam submission by task id")
    void getMyByTask_ShouldReturnMyExamSubmissionByTask() throws Exception {
        UUID submissionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        ExamSubmissionResponse response = new ExamSubmissionResponse(
                submissionId,
                "Need review",
                ExamStatus.PENDING_REVIEW,
                Map.of("answer", "Task answer"),
                userId,
                taskId,
                OffsetDateTime.parse("2026-04-20T14:00:00+04:00"),
                OffsetDateTime.parse("2026-04-20T14:00:00+04:00")
        );

        when(examSubmissionService.getMyByTask(eq(taskId), nullable(Authentication.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/exam-submissions/me/task/{taskId}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(submissionId.toString()))
                .andExpect(jsonPath("$.note").value("Need review"))
                .andExpect(jsonPath("$.status").value("PENDING_REVIEW"))
                .andExpect(jsonPath("$.answerPayload.answer").value("Task answer"))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.taskId").value(taskId.toString()));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/v1/exam-submissions/{id} -> returns exam submission by id")
    void getById_ShouldReturnExamSubmission() throws Exception {
        UUID submissionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        ExamSubmissionResponse response = new ExamSubmissionResponse(
                submissionId,
                "Passed",
                ExamStatus.PASSED,
                Map.of("answer", "Admin view answer"),
                userId,
                taskId,
                OffsetDateTime.parse("2026-04-20T14:00:00+04:00"),
                OffsetDateTime.parse("2026-04-20T15:00:00+04:00")
        );

        when(examSubmissionService.getById(submissionId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/exam-submissions/{id}", submissionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(submissionId.toString()))
                .andExpect(jsonPath("$.note").value("Passed"))
                .andExpect(jsonPath("$.status").value("PASSED"))
                .andExpect(jsonPath("$.answerPayload.answer").value("Admin view answer"))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.taskId").value(taskId.toString()));
    }

    @Test
    @WithMockUser(roles = {"PROFESSOR"})
    @DisplayName("GET /api/v1/exam-submissions/task/{taskId} -> returns all exam submissions by task")
    void getAllByTask_ShouldReturnAllExamSubmissionsByTask() throws Exception {
        UUID taskId = UUID.randomUUID();

        ExamSubmissionResponse first = new ExamSubmissionResponse(
                UUID.randomUUID(),
                null,
                ExamStatus.PENDING_REVIEW,
                Map.of("answer", "First student answer"),
                UUID.randomUUID(),
                taskId,
                OffsetDateTime.parse("2026-04-20T14:00:00+04:00"),
                OffsetDateTime.parse("2026-04-20T14:00:00+04:00")
        );

        ExamSubmissionResponse second = new ExamSubmissionResponse(
                UUID.randomUUID(),
                "Failed",
                ExamStatus.FAILED,
                Map.of("answer", "Second student answer"),
                UUID.randomUUID(),
                taskId,
                OffsetDateTime.parse("2026-04-21T14:00:00+04:00"),
                OffsetDateTime.parse("2026-04-21T15:00:00+04:00")
        );

        when(examSubmissionService.getAllByTask(taskId)).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/v1/exam-submissions/task/{taskId}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("PENDING_REVIEW"))
                .andExpect(jsonPath("$[0].answerPayload.answer").value("First student answer"))
                .andExpect(jsonPath("$[0].taskId").value(taskId.toString()))
                .andExpect(jsonPath("$[1].status").value("FAILED"))
                .andExpect(jsonPath("$[1].note").value("Failed"))
                .andExpect(jsonPath("$[1].answerPayload.answer").value("Second student answer"))
                .andExpect(jsonPath("$[1].taskId").value(taskId.toString()));
    }

    @Test
    @WithMockUser(roles = {"PROFESSOR"})
    @DisplayName("PATCH /api/v1/exam-submissions/{id} -> updates exam submission")
    void update_ShouldReturnUpdatedExamSubmission() throws Exception {
        UUID submissionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        String requestJson = """
                {
                  "status": "PASSED",
                  "note": "Good work"
                }
                """;

        ExamSubmissionResponse response = new ExamSubmissionResponse(
                submissionId,
                "Good work",
                ExamStatus.PASSED,
                Map.of("answer", "Student answer"),
                userId,
                taskId,
                OffsetDateTime.parse("2026-04-20T14:00:00+04:00"),
                OffsetDateTime.parse("2026-04-20T15:00:00+04:00")
        );

        when(examSubmissionService.update(eq(submissionId), any(UpdateExamSubmissionRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/exam-submissions/{id}", submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(submissionId.toString()))
                .andExpect(jsonPath("$.note").value("Good work"))
                .andExpect(jsonPath("$.status").value("PASSED"))
                .andExpect(jsonPath("$.answerPayload.answer").value("Student answer"))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.taskId").value(taskId.toString()));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    @DisplayName("DELETE /api/v1/exam-submissions/me/{id} -> deletes my exam submission")
    void deleteMy_ShouldReturnNoContent() throws Exception {
        UUID submissionId = UUID.randomUUID();

        doNothing().when(examSubmissionService)
                .deleteMy(eq(submissionId), nullable(Authentication.class));

        mockMvc.perform(delete("/api/v1/exam-submissions/me/{id}", submissionId))
                .andExpect(status().isNoContent());

        verify(examSubmissionService)
                .deleteMy(eq(submissionId), nullable(Authentication.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("DELETE /api/v1/exam-submissions/{id} -> deletes exam submission")
    void delete_ShouldReturnNoContent() throws Exception {
        UUID submissionId = UUID.randomUUID();

        doNothing().when(examSubmissionService).delete(submissionId);

        mockMvc.perform(delete("/api/v1/exam-submissions/{id}", submissionId))
                .andExpect(status().isNoContent());

        verify(examSubmissionService).delete(submissionId);
    }
}