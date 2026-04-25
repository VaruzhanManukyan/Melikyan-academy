package com.melikyan.academy.controller;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.FilterType;
import com.melikyan.academy.security.RememberMeSecurityFilter;
import com.melikyan.academy.service.LessonAttendanceService;
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
        controllers = LessonAttendanceController.class,
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
class LessonAttendanceControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LessonAttendanceService lessonAttendanceService;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/v1/lesson-attendances -> creates lesson attendance")
    void create_ShouldReturnCreatedLessonAttendance() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();

        String requestJson = """
                {
                  "note": "Manual attendance",
                  "status": "ENROLLED",
                  "userId": "%s",
                  "lessonId": "%s"
                }
                """.formatted(userId, lessonId);

        when(lessonAttendanceService.create(any())).thenReturn(null);

        mockMvc.perform(post("/api/v1/lesson-attendances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());

        verify(lessonAttendanceService).create(any());
    }

    @Test
    @DisplayName("POST /api/v1/lesson-attendances/lessons/{lessonId}/check-in -> checks in current user")
    void checkIn_ShouldReturnCreatedLessonAttendance() throws Exception {
        UUID lessonId = UUID.randomUUID();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("student@test.com", null);

        when(lessonAttendanceService.checkIn(eq(lessonId), any())).thenReturn(null);

        mockMvc.perform(post("/api/v1/lesson-attendances/lessons/{lessonId}/check-in", lessonId)
                        .principal(authentication))
                .andExpect(status().isCreated());

        verify(lessonAttendanceService).checkIn(eq(lessonId), any());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/v1/lesson-attendances/lessons/{lessonId}/generate-enrolled -> generates enrolled attendance")
    void generateEnrolledForLesson_ShouldReturnNoContent() throws Exception {
        UUID lessonId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/lesson-attendances/lessons/{lessonId}/generate-enrolled", lessonId))
                .andExpect(status().isNoContent());

        verify(lessonAttendanceService).generateEnrolledForLesson(lessonId);
    }

    @Test
    @WithMockUser(roles = {"PROFESSOR"})
    @DisplayName("POST /api/v1/lesson-attendances/lessons/{lessonId}/generate-missed -> generates missed attendance")
    void generateMissedForLesson_ShouldReturnNoContent() throws Exception {
        UUID lessonId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/lesson-attendances/lessons/{lessonId}/generate-missed", lessonId))
                .andExpect(status().isNoContent());

        verify(lessonAttendanceService).generateMissedForLesson(lessonId);
    }

    @Test
    @DisplayName("GET /api/v1/lesson-attendances/me -> returns my attendances")
    void getMyAll_ShouldReturnMyAttendances() throws Exception {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("student@test.com", null);

        when(lessonAttendanceService.getMyAll(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/lesson-attendances/me")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(lessonAttendanceService).getMyAll(any());
    }

    @Test
    @DisplayName("GET /api/v1/lesson-attendances/me/{id} -> returns my attendance by id")
    void getMyById_ShouldReturnMyAttendance() throws Exception {
        UUID attendanceId = UUID.randomUUID();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("student@test.com", null);

        when(lessonAttendanceService.getMyById(eq(attendanceId), any())).thenReturn(null);

        mockMvc.perform(get("/api/v1/lesson-attendances/me/{id}", attendanceId)
                        .principal(authentication))
                .andExpect(status().isOk());

        verify(lessonAttendanceService).getMyById(eq(attendanceId), any());
    }

    @Test
    @DisplayName("GET /api/v1/lesson-attendances/me/lessons/{lessonId} -> returns my attendance by lesson")
    void getMyByLesson_ShouldReturnMyAttendanceByLesson() throws Exception {
        UUID lessonId = UUID.randomUUID();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("student@test.com", null);

        when(lessonAttendanceService.getMyByLesson(eq(lessonId), any())).thenReturn(null);

        mockMvc.perform(get("/api/v1/lesson-attendances/me/lessons/{lessonId}", lessonId)
                        .principal(authentication))
                .andExpect(status().isOk());

        verify(lessonAttendanceService).getMyByLesson(eq(lessonId), any());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/v1/lesson-attendances/{id} -> returns attendance by id")
    void getById_ShouldReturnAttendance() throws Exception {
        UUID attendanceId = UUID.randomUUID();

        when(lessonAttendanceService.getById(attendanceId)).thenReturn(null);

        mockMvc.perform(get("/api/v1/lesson-attendances/{id}", attendanceId))
                .andExpect(status().isOk());

        verify(lessonAttendanceService).getById(attendanceId);
    }

    @Test
    @WithMockUser(roles = {"PROFESSOR"})
    @DisplayName("GET /api/v1/lesson-attendances/lessons/{lessonId} -> returns attendances by lesson")
    void getAllByLesson_ShouldReturnAttendances() throws Exception {
        UUID lessonId = UUID.randomUUID();

        when(lessonAttendanceService.getAllByLesson(lessonId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/lesson-attendances/lessons/{lessonId}", lessonId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(lessonAttendanceService).getAllByLesson(lessonId);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("PATCH /api/v1/lesson-attendances/{id} -> updates attendance")
    void update_ShouldReturnUpdatedAttendance() throws Exception {
        UUID attendanceId = UUID.randomUUID();

        String requestJson = """
                {
                  "note": "Student attended successfully",
                  "status": "ATTENDED"
                }
                """;

        when(lessonAttendanceService.update(eq(attendanceId), any())).thenReturn(null);

        mockMvc.perform(patch("/api/v1/lesson-attendances/{id}", attendanceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        verify(lessonAttendanceService).update(eq(attendanceId), any());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("DELETE /api/v1/lesson-attendances/{id} -> deletes attendance")
    void delete_ShouldReturnNoContent() throws Exception {
        UUID attendanceId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/lesson-attendances/{id}", attendanceId))
                .andExpect(status().isNoContent());

        verify(lessonAttendanceService).delete(attendanceId);
    }
}