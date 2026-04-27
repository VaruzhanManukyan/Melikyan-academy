package com.melikyan.academy.controller;

import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;
import com.melikyan.academy.service.ExamSectionService;
import org.springframework.context.annotation.FilterType;
import com.melikyan.academy.security.RememberMeSecurityFilter;
import com.melikyan.academy.dto.response.examSection.ExamSectionResponse;
import com.melikyan.academy.dto.request.examSection.CreateExamSectionRequest;
import com.melikyan.academy.dto.request.examSection.UpdateExamSectionRequest;
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
import java.time.Duration;
import java.time.OffsetDateTime;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(
        controllers = ExamSectionController.class,
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
class ExamSectionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExamSectionService examSectionService;

    @Test
    @DisplayName("GET /api/v1/exam-sections/{id} -> returns exam section by id")
    void getById_ShouldReturnExamSection() throws Exception {
        UUID examSectionId = UUID.randomUUID();
        UUID examId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        ExamSectionResponse response = new ExamSectionResponse(
                examSectionId,
                1,
                "Introduction Section",
                "First section",
                Duration.ofMinutes(90),
                examId,
                createdById,
                OffsetDateTime.parse("2026-04-15T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-15T12:30:00+04:00")
        );

        when(examSectionService.getById(examSectionId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/exam-sections/{id}", examSectionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(examSectionId.toString()))
                .andExpect(jsonPath("$.orderIndex").value(1))
                .andExpect(jsonPath("$.title").value("Introduction Section"))
                .andExpect(jsonPath("$.description").value("First section"))
                .andExpect(jsonPath("$.examId").value(examId.toString()))
                .andExpect(jsonPath("$.createdById").value(createdById.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/exam-sections -> returns all exam sections")
    void getAll_ShouldReturnAllExamSections() throws Exception {
        UUID examId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        ExamSectionResponse first = new ExamSectionResponse(
                UUID.randomUUID(),
                1,
                "Section 1",
                "Desc 1",
                Duration.ofMinutes(90),
                examId,
                createdById,
                OffsetDateTime.parse("2026-04-15T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-15T12:30:00+04:00")
        );

        ExamSectionResponse second = new ExamSectionResponse(
                UUID.randomUUID(),
                2,
                "Section 2",
                "Desc 2",
                Duration.ofMinutes(60),
                examId,
                createdById,
                OffsetDateTime.parse("2026-04-15T13:00:00+04:00"),
                OffsetDateTime.parse("2026-04-15T13:30:00+04:00")
        );

        when(examSectionService.getAll()).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/v1/exam-sections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Section 1"))
                .andExpect(jsonPath("$[1].title").value("Section 2"));
    }

    @Test
    @DisplayName("GET /api/v1/exam-sections?examId={examId} -> returns sections by exam id")
    void getAll_ShouldReturnExamSectionsByExamId() throws Exception {
        UUID examId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        ExamSectionResponse first = new ExamSectionResponse(
                UUID.randomUUID(),
                1,
                "Section 1",
                "Desc 1",
                Duration.ofMinutes(90),
                examId,
                createdById,
                OffsetDateTime.parse("2026-04-15T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-15T12:30:00+04:00")
        );

        ExamSectionResponse second = new ExamSectionResponse(
                UUID.randomUUID(),
                2,
                "Section 2",
                "Desc 2",
                Duration.ofMinutes(60),
                examId,
                createdById,
                OffsetDateTime.parse("2026-04-15T13:00:00+04:00"),
                OffsetDateTime.parse("2026-04-15T13:30:00+04:00")
        );

        when(examSectionService.getByExamId(examId)).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/v1/exam-sections")
                        .param("examId", examId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].examId").value(examId.toString()))
                .andExpect(jsonPath("$[0].title").value("Section 1"))
                .andExpect(jsonPath("$[1].examId").value(examId.toString()))
                .andExpect(jsonPath("$[1].title").value("Section 2"));

        verify(examSectionService).getByExamId(examId);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/v1/exam-sections -> creates exam section")
    void create_ShouldReturnCreatedExamSection() throws Exception {
        UUID examSectionId = UUID.randomUUID();
        UUID examId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        String requestJson = """
                {
                  "orderIndex": 1,
                  "title": "Introduction Section",
                  "description": "First section",
                  "duration": "PT1H30M",
                  "examId": "%s",
                  "createdById": "%s"
                }
                """.formatted(examId, createdById);

        ExamSectionResponse response = new ExamSectionResponse(
                examSectionId,
                1,
                "Introduction Section",
                "First section",
                Duration.ofMinutes(90),
                examId,
                createdById,
                OffsetDateTime.parse("2026-04-15T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-15T12:30:00+04:00")
        );

        when(examSectionService.create(any(CreateExamSectionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/exam-sections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(examSectionId.toString()))
                .andExpect(jsonPath("$.orderIndex").value(1))
                .andExpect(jsonPath("$.title").value("Introduction Section"))
                .andExpect(jsonPath("$.description").value("First section"))
                .andExpect(jsonPath("$.examId").value(examId.toString()))
                .andExpect(jsonPath("$.createdById").value(createdById.toString()));

        verify(examSectionService).create(any(CreateExamSectionRequest.class));
    }

    @Test
    @WithMockUser(roles = {"PROFESSOR"})
    @DisplayName("PATCH /api/v1/exam-sections/{id} -> updates exam section")
    void update_ShouldReturnUpdatedExamSection() throws Exception {
        UUID examSectionId = UUID.randomUUID();
        UUID examId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        String requestJson = """
                {
                  "orderIndex": 2,
                  "title": "Updated Section",
                  "description": "Updated description",
                  "duration": "PT1H"
                }
                """;

        ExamSectionResponse response = new ExamSectionResponse(
                examSectionId,
                2,
                "Updated Section",
                "Updated description",
                Duration.ofMinutes(60),
                examId,
                createdById,
                OffsetDateTime.parse("2026-04-15T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-15T12:45:00+04:00")
        );

        when(examSectionService.update(eq(examSectionId), any(UpdateExamSectionRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/exam-sections/{id}", examSectionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(examSectionId.toString()))
                .andExpect(jsonPath("$.orderIndex").value(2))
                .andExpect(jsonPath("$.title").value("Updated Section"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.examId").value(examId.toString()))
                .andExpect(jsonPath("$.createdById").value(createdById.toString()));

        verify(examSectionService).update(eq(examSectionId), any(UpdateExamSectionRequest.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("DELETE /api/v1/exam-sections/{id} -> deletes exam section")
    void delete_ShouldReturnNoContent() throws Exception {
        UUID examSectionId = UUID.randomUUID();

        doNothing().when(examSectionService).delete(examSectionId);

        mockMvc.perform(delete("/api/v1/exam-sections/{id}", examSectionId))
                .andExpect(status().isNoContent());

        verify(examSectionService).delete(examSectionId);
    }
}