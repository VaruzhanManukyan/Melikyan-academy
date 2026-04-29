package com.melikyan.academy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.melikyan.academy.service.LessonTranslationService;
import com.melikyan.academy.dto.response.lessonTranslation.LessonTranslationResponse;
import com.melikyan.academy.dto.request.lessonTranslation.CreateLessonTranslationRequest;
import com.melikyan.academy.dto.request.lessonTranslation.UpdateLessonTranslationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lesson-translations")
public class LessonTranslationController {
    private final LessonTranslationService lessonTranslationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<LessonTranslationResponse> create(
            @Valid @RequestBody CreateLessonTranslationRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lessonTranslationService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonTranslationResponse> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(lessonTranslationService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<LessonTranslationResponse>> getAll() {
        return ResponseEntity.ok(lessonTranslationService.getAll());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<List<LessonTranslationResponse>> getByCode(
            @PathVariable String code
    ) {
        return ResponseEntity.ok(lessonTranslationService.getByCode(code));
    }

    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<LessonTranslationResponse>> getByLessonId(
            @PathVariable UUID lessonId
    ) {
        return ResponseEntity.ok(lessonTranslationService.getByLessonId(lessonId));
    }

    @GetMapping("/lesson/{lessonId}/code/{code}")
    public ResponseEntity<LessonTranslationResponse> getByLessonIdAndCode(
            @PathVariable UUID lessonId,
            @PathVariable String code
    ) {
        return ResponseEntity.ok(lessonTranslationService.getByLessonIdAndCode(lessonId, code));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<LessonTranslationResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLessonTranslationRequest request
    ) {
        return ResponseEntity.ok(lessonTranslationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        lessonTranslationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}