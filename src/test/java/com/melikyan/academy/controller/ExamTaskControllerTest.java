package com.melikyan.academy.controller;

import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;
import com.melikyan.academy.entity.enums.TaskType;
import org.springframework.test.web.servlet.MockMvc;
import com.melikyan.academy.service.ExamTaskService;
import org.springframework.security.config.Customizer;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.web.SecurityFilterChain;
import com.melikyan.academy.security.RememberMeSecurityFilter;
import com.melikyan.academy.dto.response.examTask.ExamTaskResponse;
import com.melikyan.academy.dto.request.examTask.CreateExamTaskRequest;
import com.melikyan.academy.dto.request.examTask.UpdateExamTaskRequest;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.time.Duration;
import java.time.OffsetDateTime;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(
        controllers = ExamTaskController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = RememberMeSecurityFilter.class
                )
        }
)
@Import(ExamTaskControllerTest.TestSecurityConfig.class)
class ExamTaskControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExamTaskService examTaskService;

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(Customizer.withDefaults())
                    .httpBasic(Customizer.withDefaults())
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .build();
        }
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void create_shouldReturnCreatedExamTask() throws Exception {
        UUID id = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        CreateExamTaskRequest request = new CreateExamTaskRequest(
                1,
                10,
                TaskType.QUIZ,
                Duration.ofMinutes(30),
                Map.of(
                        "question", "What is Spring Boot?",
                        "correctAnswer", "Framework"
                ),
                sectionId,
                createdById
        );

        ExamTaskResponse response = new ExamTaskResponse(
                id,
                1,
                10,
                TaskType.QUIZ,
                Duration.ofMinutes(30),
                Map.of(
                        "question", "What is Spring Boot?",
                        "correctAnswer", "Framework"
                ),
                sectionId,
                createdById,
                OffsetDateTime.parse("2026-04-16T10:00:00+04:00"),
                OffsetDateTime.parse("2026-04-16T10:00:00+04:00")
        );

        when(examTaskService.create(any(CreateExamTaskRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/exam-tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.orderIndex").value(1))
                .andExpect(jsonPath("$.point").value(10))
                .andExpect(jsonPath("$.type").value("QUIZ"))
                .andExpect(jsonPath("$.duration").value("PT30M"))
                .andExpect(jsonPath("$.sectionId").value(sectionId.toString()))
                .andExpect(jsonPath("$.createdById").value(createdById.toString()))
                .andExpect(jsonPath("$.contentPayload.question").value("What is Spring Boot?"))
                .andExpect(jsonPath("$.contentPayload.correctAnswer").value("Framework"));

        verify(examTaskService).create(any(CreateExamTaskRequest.class));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void create_shouldReturnForbidden_forStudent() throws Exception {
        CreateExamTaskRequest request = new CreateExamTaskRequest(
                1,
                10,
                TaskType.QUIZ,
                Duration.ofMinutes(30),
                Map.of("question", "Test"),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        mockMvc.perform(post("/api/v1/exam-tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getById_shouldReturnUnauthorized_whenNoAuth() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/exam-tasks/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void getById_shouldReturnExamTask() throws Exception {
        UUID id = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        ExamTaskResponse response = new ExamTaskResponse(
                id,
                1,
                10,
                TaskType.QUIZ,
                Duration.ofMinutes(30),
                Map.of(
                        "question", "What is Spring Boot?",
                        "correctAnswer", "Framework"
                ),
                sectionId,
                createdById,
                OffsetDateTime.parse("2026-04-16T10:00:00+04:00"),
                OffsetDateTime.parse("2026-04-16T10:00:00+04:00")
        );

        when(examTaskService.getById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/exam-tasks/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.orderIndex").value(1))
                .andExpect(jsonPath("$.point").value(10))
                .andExpect(jsonPath("$.type").value("QUIZ"))
                .andExpect(jsonPath("$.duration").value("PT30M"))
                .andExpect(jsonPath("$.sectionId").value(sectionId.toString()));

        verify(examTaskService).getById(id);
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void getAll_shouldReturnExamTaskList() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        List<ExamTaskResponse> response = List.of(
                new ExamTaskResponse(
                        id1,
                        1,
                        10,
                        TaskType.QUIZ,
                        Duration.ofMinutes(30),
                        Map.of("question", "Question 1"),
                        sectionId,
                        createdById,
                        OffsetDateTime.parse("2026-04-16T10:00:00+04:00"),
                        OffsetDateTime.parse("2026-04-16T10:00:00+04:00")
                ),
                new ExamTaskResponse(
                        id2,
                        2,
                        20,
                        TaskType.ESSAY,
                        Duration.ofMinutes(45),
                        Map.of("topic", "Essay topic"),
                        sectionId,
                        createdById,
                        OffsetDateTime.parse("2026-04-16T11:00:00+04:00"),
                        OffsetDateTime.parse("2026-04-16T11:00:00+04:00")
                )
        );

        when(examTaskService.getAll()).thenReturn(response);

        mockMvc.perform(get("/api/v1/exam-tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].orderIndex").value(1))
                .andExpect(jsonPath("$[0].type").value("QUIZ"))
                .andExpect(jsonPath("$[0].duration").value("PT30M"))
                .andExpect(jsonPath("$[1].orderIndex").value(2))
                .andExpect(jsonPath("$[1].type").value("ESSAY"))
                .andExpect(jsonPath("$[1].duration").value("PT45M"));

        verify(examTaskService).getAll();
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void getAllBySectionId_shouldReturnExamTaskList() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        List<ExamTaskResponse> response = List.of(
                new ExamTaskResponse(
                        id1,
                        1,
                        10,
                        TaskType.QUIZ,
                        Duration.ofMinutes(30),
                        Map.of("question", "Question 1"),
                        sectionId,
                        createdById,
                        OffsetDateTime.parse("2026-04-16T10:00:00+04:00"),
                        OffsetDateTime.parse("2026-04-16T10:00:00+04:00")
                ),
                new ExamTaskResponse(
                        id2,
                        2,
                        20,
                        TaskType.ESSAY,
                        Duration.ofMinutes(45),
                        Map.of("topic", "Essay topic"),
                        sectionId,
                        createdById,
                        OffsetDateTime.parse("2026-04-16T11:00:00+04:00"),
                        OffsetDateTime.parse("2026-04-16T11:00:00+04:00")
                )
        );

        when(examTaskService.getAllByExamSectionId(sectionId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/exam-tasks")
                        .param("sectionId", sectionId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].sectionId").value(sectionId.toString()))
                .andExpect(jsonPath("$[0].duration").value("PT30M"))
                .andExpect(jsonPath("$[1].sectionId").value(sectionId.toString()))
                .andExpect(jsonPath("$[1].duration").value("PT45M"));

        verify(examTaskService).getAllByExamSectionId(sectionId);
    }

    @Test
    @WithMockUser(roles = {"PROFESSOR"})
    void update_shouldReturnUpdatedExamTask() throws Exception {
        UUID id = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        UpdateExamTaskRequest request = new UpdateExamTaskRequest(
                2,
                15,
                TaskType.ESSAY,
                Duration.ofHours(1),
                Map.of(
                        "topic", "Explain dependency injection",
                        "minWords", 200
                ),
                sectionId
        );

        ExamTaskResponse response = new ExamTaskResponse(
                id,
                2,
                15,
                TaskType.ESSAY,
                Duration.ofHours(1),
                Map.of(
                        "topic", "Explain dependency injection",
                        "minWords", 200
                ),
                sectionId,
                createdById,
                OffsetDateTime.parse("2026-04-16T10:00:00+04:00"),
                OffsetDateTime.parse("2026-04-17T10:00:00+04:00")
        );

        when(examTaskService.update(eq(id), any(UpdateExamTaskRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/exam-tasks/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.orderIndex").value(2))
                .andExpect(jsonPath("$.point").value(15))
                .andExpect(jsonPath("$.type").value("ESSAY"))
                .andExpect(jsonPath("$.duration").value("PT1H"))
                .andExpect(jsonPath("$.sectionId").value(sectionId.toString()))
                .andExpect(jsonPath("$.contentPayload.topic").value("Explain dependency injection"));

        verify(examTaskService).update(eq(id), any(UpdateExamTaskRequest.class));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void update_shouldReturnForbidden_forStudent() throws Exception {
        UUID id = UUID.randomUUID();

        UpdateExamTaskRequest request = new UpdateExamTaskRequest(
                2,
                15,
                TaskType.ESSAY,
                Duration.ofHours(1),
                Map.of("topic", "Updated"),
                UUID.randomUUID()
        );

        mockMvc.perform(patch("/api/v1/exam-tasks/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void delete_shouldReturnNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(examTaskService).delete(id);

        mockMvc.perform(delete("/api/v1/exam-tasks/{id}", id)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(examTaskService).delete(id);
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void delete_shouldReturnForbidden_forStudent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/exam-tasks/{id}", id)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}