package com.melikyan.academy.controller;

import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;
import com.melikyan.academy.service.HomeworkTaskService;
import org.springframework.security.config.Customizer;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.web.SecurityFilterChain;
import com.melikyan.academy.security.RememberMeSecurityFilter;
import com.melikyan.academy.dto.response.homeworkTask.HomeworkTaskResponse;
import com.melikyan.academy.dto.request.homeworkTask.UpdateHomeworkTaskRequest;
import com.melikyan.academy.dto.request.homeworkTask.CreateHomeworkTaskRequest;
import com.melikyan.academy.entity.enums.TaskType;
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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import java.util.List;
import java.util.Map;
import java.util.UUID;
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
        controllers = HomeworkTaskController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = RememberMeSecurityFilter.class
                )
        }
)
@Import(HomeworkTaskControllerTest.TestSecurityConfig.class)
class HomeworkTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HomeworkTaskService homeworkTaskService;

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
    void create_shouldReturnCreatedHomeworkTask() throws Exception {
        UUID id = UUID.randomUUID();
        UUID homeworkId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        CreateHomeworkTaskRequest request = new CreateHomeworkTaskRequest(
                1,
                10,
                TaskType.QUIZ,
                Map.of(
                        "question", "What is Spring Boot?",
                        "correctAnswer", "Framework"
                ),
                homeworkId,
                createdById
        );

        HomeworkTaskResponse response = new HomeworkTaskResponse(
                id,
                1,
                10,
                TaskType.QUIZ,
                Map.of(
                        "question", "What is Spring Boot?",
                        "correctAnswer", "Framework"
                ),
                homeworkId,
                createdById,
                OffsetDateTime.parse("2026-04-16T10:00:00+04:00"),
                OffsetDateTime.parse("2026-04-16T10:00:00+04:00")
        );

        when(homeworkTaskService.create(any(CreateHomeworkTaskRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/homework-tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.orderIndex").value(1))
                .andExpect(jsonPath("$.point").value(10))
                .andExpect(jsonPath("$.type").value("QUIZ"))
                .andExpect(jsonPath("$.homeworkId").value(homeworkId.toString()))
                .andExpect(jsonPath("$.createdById").value(createdById.toString()))
                .andExpect(jsonPath("$.payloadContent.question").value("What is Spring Boot?"))
                .andExpect(jsonPath("$.payloadContent.correctAnswer").value("Framework"));

        verify(homeworkTaskService).create(any(CreateHomeworkTaskRequest.class));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void create_shouldReturnForbidden_forStudent() throws Exception {
        CreateHomeworkTaskRequest request = new CreateHomeworkTaskRequest(
                1,
                10,
                TaskType.QUIZ,
                Map.of("question", "Test"),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        mockMvc.perform(post("/api/v1/homework-tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getById_shouldReturnUnauthorized_whenNoAuth() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/homework-tasks/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void getById_shouldReturnHomeworkTask() throws Exception {
        UUID id = UUID.randomUUID();
        UUID homeworkId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        HomeworkTaskResponse response = new HomeworkTaskResponse(
                id,
                1,
                10,
                TaskType.QUIZ,
                Map.of(
                        "question", "What is Spring Boot?",
                        "correctAnswer", "Framework"
                ),
                homeworkId,
                createdById,
                OffsetDateTime.parse("2026-04-16T10:00:00+04:00"),
                OffsetDateTime.parse("2026-04-16T10:00:00+04:00")
        );

        when(homeworkTaskService.getById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/homework-tasks/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.orderIndex").value(1))
                .andExpect(jsonPath("$.point").value(10))
                .andExpect(jsonPath("$.type").value("QUIZ"))
                .andExpect(jsonPath("$.homeworkId").value(homeworkId.toString()));

        verify(homeworkTaskService).getById(id);
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void getAll_shouldReturnHomeworkTaskList() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID homeworkId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        List<HomeworkTaskResponse> response = List.of(
                new HomeworkTaskResponse(
                        id1,
                        1,
                        10,
                        TaskType.QUIZ,
                        Map.of("question", "Question 1"),
                        homeworkId,
                        createdById,
                        OffsetDateTime.parse("2026-04-16T10:00:00+04:00"),
                        OffsetDateTime.parse("2026-04-16T10:00:00+04:00")
                ),
                new HomeworkTaskResponse(
                        id2,
                        2,
                        20,
                        TaskType.ESSAY,
                        Map.of("topic", "Essay topic"),
                        homeworkId,
                        createdById,
                        OffsetDateTime.parse("2026-04-16T11:00:00+04:00"),
                        OffsetDateTime.parse("2026-04-16T11:00:00+04:00")
                )
        );

        when(homeworkTaskService.getAll()).thenReturn(response);

        mockMvc.perform(get("/api/v1/homework-tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].orderIndex").value(1))
                .andExpect(jsonPath("$[0].type").value("QUIZ"))
                .andExpect(jsonPath("$[1].orderIndex").value(2))
                .andExpect(jsonPath("$[1].type").value("ESSAY"));

        verify(homeworkTaskService).getAll();
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void getAllByHomeworkId_shouldReturnHomeworkTaskList() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID homeworkId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        List<HomeworkTaskResponse> response = List.of(
                new HomeworkTaskResponse(
                        id1,
                        1,
                        10,
                        TaskType.QUIZ,
                        Map.of("question", "Question 1"),
                        homeworkId,
                        createdById,
                        OffsetDateTime.parse("2026-04-16T10:00:00+04:00"),
                        OffsetDateTime.parse("2026-04-16T10:00:00+04:00")
                ),
                new HomeworkTaskResponse(
                        id2,
                        2,
                        20,
                        TaskType.ESSAY,
                        Map.of("topic", "Essay topic"),
                        homeworkId,
                        createdById,
                        OffsetDateTime.parse("2026-04-16T11:00:00+04:00"),
                        OffsetDateTime.parse("2026-04-16T11:00:00+04:00")
                )
        );

        when(homeworkTaskService.getAllByHomeworkId(homeworkId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/homework-tasks")
                        .param("homeworkId", homeworkId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].homeworkId").value(homeworkId.toString()))
                .andExpect(jsonPath("$[1].homeworkId").value(homeworkId.toString()));

        verify(homeworkTaskService).getAllByHomeworkId(homeworkId);
    }

    @Test
    @WithMockUser(roles = {"PROFESSOR"})
    void update_shouldReturnUpdatedHomeworkTask() throws Exception {
        UUID id = UUID.randomUUID();
        UUID homeworkId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        UpdateHomeworkTaskRequest request = new UpdateHomeworkTaskRequest(
                2,
                15,
                TaskType.ESSAY,
                Map.of(
                        "topic", "Explain dependency injection",
                        "minWords", 200
                ),
                homeworkId
        );

        HomeworkTaskResponse response = new HomeworkTaskResponse(
                id,
                2,
                15,
                TaskType.ESSAY,
                Map.of(
                        "topic", "Explain dependency injection",
                        "minWords", 200
                ),
                homeworkId,
                createdById,
                OffsetDateTime.parse("2026-04-16T10:00:00+04:00"),
                OffsetDateTime.parse("2026-04-17T10:00:00+04:00")
        );

        when(homeworkTaskService.update(eq(id), any(UpdateHomeworkTaskRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/homework-tasks/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.orderIndex").value(2))
                .andExpect(jsonPath("$.point").value(15))
                .andExpect(jsonPath("$.type").value("ESSAY"))
                .andExpect(jsonPath("$.payloadContent.topic").value("Explain dependency injection"));

        verify(homeworkTaskService).update(eq(id), any(UpdateHomeworkTaskRequest.class));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void update_shouldReturnForbidden_forStudent() throws Exception {
        UUID id = UUID.randomUUID();

        UpdateHomeworkTaskRequest request = new UpdateHomeworkTaskRequest(
                2,
                15,
                TaskType.ESSAY,
                Map.of("topic", "Updated"),
                UUID.randomUUID()
        );

        mockMvc.perform(patch("/api/v1/homework-tasks/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void delete_shouldReturnNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(homeworkTaskService).delete(id);

        mockMvc.perform(delete("/api/v1/homework-tasks/{id}", id)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(homeworkTaskService).delete(id);
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void delete_shouldReturnForbidden_forStudent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/homework-tasks/{id}", id)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}