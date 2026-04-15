package com.melikyan.academy.controller;

import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;
import com.melikyan.academy.service.LessonService;
import org.springframework.test.web.servlet.MockMvc;
import com.melikyan.academy.entity.enums.SessionType;
import com.melikyan.academy.entity.enums.SessionState;
import org.springframework.context.annotation.FilterType;
import com.melikyan.academy.security.RememberMeSecurityFilter;
import com.melikyan.academy.dto.response.lesson.LessonResponse;
import com.melikyan.academy.dto.request.lesson.CreateLessonRequest;
import com.melikyan.academy.dto.request.lesson.UpdateLessonRequest;
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
        controllers = LessonController.class,
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
class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LessonService lessonService;

    @Test
    @DisplayName("GET /api/v1/lessons/{id} -> returns lesson by id")
    void getById_ShouldReturnLesson() throws Exception {
        UUID lessonId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        LessonResponse response = new LessonResponse(
                lessonId,
                1,
                "Introduction to Spring",
                "First lesson",
                SessionType.MEET_LINK,
                "https://meet.google.com/test",
                SessionState.SCHEDULED,
                OffsetDateTime.parse("2026-04-20T14:00:00+04:00"),
                Duration.ofMinutes(90),
                courseId,
                createdById,
                OffsetDateTime.parse("2026-04-15T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-15T12:30:00+04:00")
        );

        when(lessonService.getById(lessonId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/lessons/{id}", lessonId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lessonId.toString()))
                .andExpect(jsonPath("$.orderIndex").value(1))
                .andExpect(jsonPath("$.title").value("Introduction to Spring"))
                .andExpect(jsonPath("$.description").value("First lesson"))
                .andExpect(jsonPath("$.sessionType").value("MEET_LINK"))
                .andExpect(jsonPath("$.valueUrl").value("https://meet.google.com/test"))
                .andExpect(jsonPath("$.state").value("SCHEDULED"))
                .andExpect(jsonPath("$.courseId").value(courseId.toString()))
                .andExpect(jsonPath("$.createdById").value(createdById.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/lessons -> returns all lessons")
    void getAll_ShouldReturnAllLessons() throws Exception {
        UUID courseId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        LessonResponse first = new LessonResponse(
                UUID.randomUUID(),
                1,
                "Lesson 1",
                "Desc 1",
                SessionType.MEET_LINK,
                "https://meet.google.com/one",
                SessionState.SCHEDULED,
                OffsetDateTime.parse("2026-04-20T14:00:00+04:00"),
                Duration.ofMinutes(90),
                courseId,
                createdById,
                OffsetDateTime.parse("2026-04-15T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-15T12:30:00+04:00")
        );

        LessonResponse second = new LessonResponse(
                UUID.randomUUID(),
                2,
                "Lesson 2",
                "Desc 2",
                SessionType.VIDEO_LINK,
                "https://youtube.com/test",
                SessionState.COMPLETED,
                OffsetDateTime.parse("2026-04-21T14:00:00+04:00"),
                Duration.ofMinutes(60),
                courseId,
                createdById,
                OffsetDateTime.parse("2026-04-15T13:00:00+04:00"),
                OffsetDateTime.parse("2026-04-15T13:30:00+04:00")
        );

        when(lessonService.getAll()).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/v1/lessons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Lesson 1"))
                .andExpect(jsonPath("$[1].title").value("Lesson 2"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/v1/lessons -> creates lesson")
    void create_ShouldReturnCreatedLesson() throws Exception {
        UUID lessonId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        String requestJson = """
                {
                  "orderIndex": 1,
                  "title": "Introduction to Spring",
                  "description": "First lesson",
                  "sessionType": "MEET_LINK",
                  "valueUrl": "https://meet.google.com/test",
                  "state": "SCHEDULED",
                  "startsAt": "2026-04-20T14:00:00+04:00",
                  "duration": "PT1H30M",
                  "courseId": "%s",
                  "createdById": "%s"
                }
                """.formatted(courseId, createdById);

        LessonResponse response = new LessonResponse(
                lessonId,
                1,
                "Introduction to Spring",
                "First lesson",
                SessionType.MEET_LINK,
                "https://meet.google.com/test",
                SessionState.SCHEDULED,
                OffsetDateTime.parse("2026-04-20T14:00:00+04:00"),
                Duration.ofMinutes(90),
                courseId,
                createdById,
                OffsetDateTime.parse("2026-04-15T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-15T12:30:00+04:00")
        );

        when(lessonService.create(any(CreateLessonRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(lessonId.toString()))
                .andExpect(jsonPath("$.title").value("Introduction to Spring"))
                .andExpect(jsonPath("$.orderIndex").value(1))
                .andExpect(jsonPath("$.courseId").value(courseId.toString()))
                .andExpect(jsonPath("$.createdById").value(createdById.toString()));
    }

    @Test
    @WithMockUser(roles = {"PROFESSOR"})
    @DisplayName("PATCH /api/v1/lessons/{id} -> updates lesson")
    void update_ShouldReturnUpdatedLesson() throws Exception {
        UUID lessonId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        String requestJson = """
                {
                  "title": "Updated Spring Lesson",
                  "description": "Updated description",
                  "valueUrl": "https://meet.google.com/updated",
                  "state": "ONGOING"
                }
                """;

        LessonResponse response = new LessonResponse(
                lessonId,
                1,
                "Updated Spring Lesson",
                "Updated description",
                SessionType.MEET_LINK,
                "https://meet.google.com/updated",
                SessionState.ONGOING,
                OffsetDateTime.parse("2026-04-20T14:00:00+04:00"),
                Duration.ofMinutes(90),
                courseId,
                createdById,
                OffsetDateTime.parse("2026-04-15T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-15T12:45:00+04:00")
        );

        when(lessonService.update(eq(lessonId), any(UpdateLessonRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/lessons/{id}", lessonId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lessonId.toString()))
                .andExpect(jsonPath("$.title").value("Updated Spring Lesson"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.valueUrl").value("https://meet.google.com/updated"))
                .andExpect(jsonPath("$.state").value("ONGOING"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("DELETE /api/v1/lessons/{id} -> deletes lesson")
    void delete_ShouldReturnNoContent() throws Exception {
        UUID lessonId = UUID.randomUUID();

        doNothing().when(lessonService).delete(lessonId);

        mockMvc.perform(delete("/api/v1/lessons/{id}", lessonId))
                .andExpect(status().isNoContent());

        verify(lessonService).delete(lessonId);
    }
}