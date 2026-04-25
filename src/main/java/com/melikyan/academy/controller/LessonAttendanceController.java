package com.melikyan.academy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.melikyan.academy.service.LessonAttendanceService;
import com.melikyan.academy.dto.response.lessonAttendance.LessonAttendanceResponse;
import com.melikyan.academy.dto.request.lessonAttendance.CreateLessonAttendanceRequest;
import com.melikyan.academy.dto.request.lessonAttendance.UpdateLessonAttendanceRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lesson-attendances")
public class LessonAttendanceController {
    private final LessonAttendanceService lessonAttendanceService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<LessonAttendanceResponse> create(
            @Valid @RequestBody CreateLessonAttendanceRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lessonAttendanceService.create(request));
    }

    @PostMapping("/lessons/{lessonId}/check-in")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LessonAttendanceResponse> checkIn(
            @PathVariable UUID lessonId,
            Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lessonAttendanceService.checkIn(lessonId, authentication));
    }

    @PostMapping("/lessons/{lessonId}/generate-enrolled")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> generateEnrolledForLesson(@PathVariable UUID lessonId) {
        lessonAttendanceService.generateEnrolledForLesson(lessonId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/lessons/{lessonId}/generate-missed")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> generateMissedForLesson(@PathVariable UUID lessonId) {
        lessonAttendanceService.generateMissedForLesson(lessonId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LessonAttendanceResponse>> getMyAll(
            Authentication authentication
    ) {
        return ResponseEntity.ok(lessonAttendanceService.getMyAll(authentication));
    }

    @GetMapping("/me/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LessonAttendanceResponse> getMyById(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(lessonAttendanceService.getMyById(id, authentication));
    }

    @GetMapping("/me/lessons/{lessonId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LessonAttendanceResponse> getMyByLesson(
            @PathVariable UUID lessonId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(lessonAttendanceService.getMyByLesson(lessonId, authentication));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<LessonAttendanceResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(lessonAttendanceService.getById(id));
    }

    @GetMapping("/lessons/{lessonId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<List<LessonAttendanceResponse>> getAllByLesson(
            @PathVariable UUID lessonId
    ) {
        return ResponseEntity.ok(lessonAttendanceService.getAllByLesson(lessonId));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<LessonAttendanceResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLessonAttendanceRequest request
    ) {
        return ResponseEntity.ok(lessonAttendanceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        lessonAttendanceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}