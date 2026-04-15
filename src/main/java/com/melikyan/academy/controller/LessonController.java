package com.melikyan.academy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.melikyan.academy.service.LessonService;
import com.melikyan.academy.dto.response.lesson.LessonResponse;
import com.melikyan.academy.dto.request.lesson.CreateLessonRequest;
import com.melikyan.academy.dto.request.lesson.UpdateLessonRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lessons")
public class LessonController {
    private final LessonService lessonService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<LessonResponse> create(
            @Valid @RequestBody CreateLessonRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lessonService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(lessonService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<LessonResponse>> getAll() {
        return ResponseEntity.ok(lessonService.getAll());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<LessonResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLessonRequest request
    ) {
        return ResponseEntity.ok(lessonService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        lessonService.delete(id);
        return ResponseEntity.noContent().build();
    }
}