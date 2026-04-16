package com.melikyan.academy.controller;

import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;
import com.melikyan.academy.service.HomeworkService;
import org.springframework.security.config.Customizer;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.web.SecurityFilterChain;
import com.melikyan.academy.security.RememberMeSecurityFilter;
import com.melikyan.academy.dto.response.homework.HomeworkResponse;
import com.melikyan.academy.dto.request.homework.UpdateHomeworkRequest;
import com.melikyan.academy.dto.request.homework.CreateHomeworkRequest;
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
        controllers = HomeworkController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = RememberMeSecurityFilter.class
                )
        }
)
@Import(HomeworkControllerTest.TestSecurityConfig.class)
class HomeworkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HomeworkService homeworkService;

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
    void create_shouldReturnCreatedHomework() throws Exception {
        UUID id = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        CreateHomeworkRequest request = new CreateHomeworkRequest(
                1,
                "Homework 1",
                "Homework description",
                OffsetDateTime.parse("2026-04-25T18:00:00+04:00"),
                true,
                lessonId,
                createdById
        );

        HomeworkResponse response = new HomeworkResponse(
                id,
                1,
                "Homework 1",
                "Homework description",
                OffsetDateTime.parse("2026-04-25T18:00:00+04:00"),
                true,
                lessonId,
                createdById,
                OffsetDateTime.parse("2026-04-16T10:00:00+04:00"),
                OffsetDateTime.parse("2026-04-16T10:00:00+04:00")
        );

        when(homeworkService.create(any(CreateHomeworkRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/homeworks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.orderIndex").value(1))
                .andExpect(jsonPath("$.title").value("Homework 1"))
                .andExpect(jsonPath("$.description").value("Homework description"))
                .andExpect(jsonPath("$.lessonId").value(lessonId.toString()))
                .andExpect(jsonPath("$.createdById").value(createdById.toString()))
                .andExpect(jsonPath("$.isPublished").value(true));

        verify(homeworkService).create(any(CreateHomeworkRequest.class));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void create_shouldReturnForbidden_forStudent() throws Exception {
        UUID lessonId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        CreateHomeworkRequest request = new CreateHomeworkRequest(
                1,
                "Homework 1",
                "Homework description",
                OffsetDateTime.parse("2026-04-25T18:00:00+04:00"),
                true,
                lessonId,
                createdById
        );

        mockMvc.perform(post("/api/v1/homeworks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getById_shouldReturnUnauthorized_whenNoAuth() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/homeworks/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void getById_shouldReturnHomework() throws Exception {
        UUID id = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        HomeworkResponse response = new HomeworkResponse(
                id,
                1,
                "Homework 1",
                "Homework description",
                OffsetDateTime.parse("2026-04-25T18:00:00+04:00"),
                true,
                lessonId,
                createdById,
                OffsetDateTime.parse("2026-04-16T10:00:00+04:00"),
                OffsetDateTime.parse("2026-04-16T10:00:00+04:00")
        );

        when(homeworkService.getById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/homeworks/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value("Homework 1"))
                .andExpect(jsonPath("$.isPublished").value(true));

        verify(homeworkService).getById(id);
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void getAll_shouldReturnHomeworkList() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        List<HomeworkResponse> response = List.of(
                new HomeworkResponse(
                        id1,
                        1,
                        "Homework 1",
                        "Description 1",
                        OffsetDateTime.parse("2026-04-25T18:00:00+04:00"),
                        true,
                        lessonId,
                        createdById,
                        OffsetDateTime.parse("2026-04-16T10:00:00+04:00"),
                        OffsetDateTime.parse("2026-04-16T10:00:00+04:00")
                ),
                new HomeworkResponse(
                        id2,
                        2,
                        "Homework 2",
                        "Description 2",
                        OffsetDateTime.parse("2026-04-26T18:00:00+04:00"),
                        false,
                        lessonId,
                        createdById,
                        OffsetDateTime.parse("2026-04-16T10:00:00+04:00"),
                        OffsetDateTime.parse("2026-04-16T10:00:00+04:00")
                )
        );

        when(homeworkService.getAll()).thenReturn(response);

        mockMvc.perform(get("/api/v1/homeworks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Homework 1"))
                .andExpect(jsonPath("$[1].title").value("Homework 2"));

        verify(homeworkService).getAll();
    }

    @Test
    @WithMockUser(roles = {"PROFESSOR"})
    void update_shouldReturnUpdatedHomework() throws Exception {
        UUID id = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        UpdateHomeworkRequest request = new UpdateHomeworkRequest(
                2,
                "Updated Homework",
                "Updated description",
                OffsetDateTime.parse("2026-04-30T20:00:00+04:00"),
                false,
                lessonId
        );

        HomeworkResponse response = new HomeworkResponse(
                id,
                2,
                "Updated Homework",
                "Updated description",
                OffsetDateTime.parse("2026-04-30T20:00:00+04:00"),
                false,
                lessonId,
                createdById,
                OffsetDateTime.parse("2026-04-16T10:00:00+04:00"),
                OffsetDateTime.parse("2026-04-17T10:00:00+04:00")
        );

        when(homeworkService.update(eq(id), any(UpdateHomeworkRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/homeworks/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value("Updated Homework"))
                .andExpect(jsonPath("$.isPublished").value(false));

        verify(homeworkService).update(eq(id), any(UpdateHomeworkRequest.class));
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void update_shouldReturnForbidden_forStudent() throws Exception {
        UUID id = UUID.randomUUID();

        UpdateHomeworkRequest request = new UpdateHomeworkRequest(
                2,
                "Updated Homework",
                "Updated description",
                OffsetDateTime.parse("2026-04-30T20:00:00+04:00"),
                false,
                UUID.randomUUID()
        );

        mockMvc.perform(patch("/api/v1/homeworks/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void delete_shouldReturnNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(homeworkService).delete(id);

        mockMvc.perform(delete("/api/v1/homeworks/{id}", id)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(homeworkService).delete(id);
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void delete_shouldReturnForbidden_forStudent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/homeworks/{id}", id)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}