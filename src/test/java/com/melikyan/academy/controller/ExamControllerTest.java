package com.melikyan.academy.controller;

import tools.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import com.melikyan.academy.service.ExamService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.FilterType;
import com.melikyan.academy.security.RememberMeSecurityFilter;
import com.melikyan.academy.entity.enums.ContentItemType;
import com.melikyan.academy.dto.response.exam.ExamResponse;
import com.melikyan.academy.dto.request.exam.CreateExamRequest;
import com.melikyan.academy.dto.request.exam.UpdateExamRequest;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.util.List;
import java.util.UUID;
import java.time.OffsetDateTime;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@WebMvcTest(
        controllers = ExamController.class,
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
class ExamControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExamService examService;

    private String toUtcString(OffsetDateTime value) {
        return value.toInstant().toString();
    }

    @Test
    @DisplayName("POST /api/v1/exams -> should create exam and return 201")
    void create_shouldReturnCreatedExam() throws Exception {
        UUID examId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();
        UUID contentItemId = UUID.randomUUID();

        OffsetDateTime createdAt = OffsetDateTime.parse("2026-04-14T12:00:00+04:00");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-04-14T12:30:00+04:00");

        CreateExamRequest request = new CreateExamRequest(
                "Java Final Exam",
                "Final exam description",
                createdById
        );

        ExamResponse response = new ExamResponse(
                examId,
                "Java Final Exam",
                "Final exam description",
                ContentItemType.EXAM,
                createdById,
                contentItemId,
                createdAt,
                updatedAt
        );

        when(examService.create(any(CreateExamRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(examId.toString()))
                .andExpect(jsonPath("$.title").value("Java Final Exam"))
                .andExpect(jsonPath("$.description").value("Final exam description"))
                .andExpect(jsonPath("$.type").value("EXAM"))
                .andExpect(jsonPath("$.createdById").value(createdById.toString()))
                .andExpect(jsonPath("$.contentItemId").value(contentItemId.toString()))
                .andExpect(jsonPath("$.createdAt").value(toUtcString(createdAt)))
                .andExpect(jsonPath("$.updatedAt").value(toUtcString(updatedAt)));

        verify(examService).create(any(CreateExamRequest.class));
    }

    @Test
    @DisplayName("GET /api/v1/exams/{id} -> should return exam by id")
    void getById_shouldReturnExam() throws Exception {
        UUID examId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();
        UUID contentItemId = UUID.randomUUID();

        ExamResponse response = new ExamResponse(
                examId,
                "Java Final Exam",
                "Final exam description",
                ContentItemType.EXAM,
                createdById,
                contentItemId,
                OffsetDateTime.parse("2026-04-14T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-14T12:30:00+04:00")
        );

        when(examService.getById(examId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/exams/{id}", examId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(examId.toString()))
                .andExpect(jsonPath("$.title").value("Java Final Exam"))
                .andExpect(jsonPath("$.description").value("Final exam description"))
                .andExpect(jsonPath("$.type").value("EXAM"))
                .andExpect(jsonPath("$.createdById").value(createdById.toString()))
                .andExpect(jsonPath("$.contentItemId").value(contentItemId.toString()));

        verify(examService).getById(examId);
    }

    @Test
    @DisplayName("GET /api/v1/exams -> should return all exams")
    void getAll_shouldReturnAllExams() throws Exception {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();
        UUID firstContentItemId = UUID.randomUUID();
        UUID secondContentItemId = UUID.randomUUID();

        ExamResponse first = new ExamResponse(
                firstId,
                "Java Final Exam",
                "Final exam description",
                ContentItemType.EXAM,
                createdById,
                firstContentItemId,
                OffsetDateTime.parse("2026-04-14T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-14T12:30:00+04:00")
        );

        ExamResponse second = new ExamResponse(
                secondId,
                "Algorithms Exam",
                "Algorithms exam description",
                ContentItemType.EXAM,
                createdById,
                secondContentItemId,
                OffsetDateTime.parse("2026-04-15T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-15T12:30:00+04:00")
        );

        when(examService.getAll()).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/v1/exams")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(firstId.toString()))
                .andExpect(jsonPath("$[0].title").value("Java Final Exam"))
                .andExpect(jsonPath("$[0].type").value("EXAM"))
                .andExpect(jsonPath("$[0].contentItemId").value(firstContentItemId.toString()))
                .andExpect(jsonPath("$[1].id").value(secondId.toString()))
                .andExpect(jsonPath("$[1].title").value("Algorithms Exam"))
                .andExpect(jsonPath("$[1].type").value("EXAM"))
                .andExpect(jsonPath("$[1].contentItemId").value(secondContentItemId.toString()));

        verify(examService).getAll();
    }

    @Test
    @DisplayName("PATCH /api/v1/exams/{id} -> should update exam")
    void update_shouldReturnUpdatedExam() throws Exception {
        UUID examId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();
        UUID contentItemId = UUID.randomUUID();

        UpdateExamRequest request = new UpdateExamRequest(
                "Updated Java Exam",
                "Updated exam description"
        );

        ExamResponse response = new ExamResponse(
                examId,
                "Updated Java Exam",
                "Updated exam description",
                ContentItemType.EXAM,
                createdById,
                contentItemId,
                OffsetDateTime.parse("2026-04-14T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-16T14:00:00+04:00")
        );

        when(examService.update(eq(examId), any(UpdateExamRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/exams/{id}", examId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(examId.toString()))
                .andExpect(jsonPath("$.title").value("Updated Java Exam"))
                .andExpect(jsonPath("$.description").value("Updated exam description"))
                .andExpect(jsonPath("$.type").value("EXAM"))
                .andExpect(jsonPath("$.contentItemId").value(contentItemId.toString()));

        verify(examService).update(eq(examId), any(UpdateExamRequest.class));
    }

    @Test
    @DisplayName("DELETE /api/v1/exams/{id} -> should return 204")
    void delete_shouldReturnNoContent() throws Exception {
        UUID examId = UUID.randomUUID();

        doNothing().when(examService).delete(examId);

        mockMvc.perform(delete("/api/v1/exams/{id}", examId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(examService).delete(examId);
    }
}