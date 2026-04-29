package com.melikyan.academy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.melikyan.academy.service.ContentItemTranslationService;
import com.melikyan.academy.dto.response.contentItemTranslation.ContentItemTranslationResponse;
import com.melikyan.academy.dto.request.contentItemTranslation.CreateContentItemTranslationRequest;
import com.melikyan.academy.dto.request.contentItemTranslation.UpdateContentItemTranslationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/course-translations")
public class CourseTranslationController {
    private final ContentItemTranslationService contentItemTranslationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<ContentItemTranslationResponse> create(
            @Valid @RequestBody CreateContentItemTranslationRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(contentItemTranslationService.createCourseTranslation(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContentItemTranslationResponse> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(contentItemTranslationService.getCourseTranslationById(id));
    }

    @GetMapping
    public ResponseEntity<List<ContentItemTranslationResponse>> getAll() {
        return ResponseEntity.ok(contentItemTranslationService.getAllCourseTranslations());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<List<ContentItemTranslationResponse>> getByCode(
            @PathVariable String code
    ) {
        return ResponseEntity.ok(contentItemTranslationService.getCourseTranslationsByCode(code));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<ContentItemTranslationResponse>> getByCourseId(
            @PathVariable UUID courseId
    ) {
        return ResponseEntity.ok(contentItemTranslationService.getByCourseId(courseId));
    }

    @GetMapping("/course/{courseId}/code/{code}")
    public ResponseEntity<ContentItemTranslationResponse> getByCourseIdAndCode(
            @PathVariable UUID courseId,
            @PathVariable String code
    ) {
        return ResponseEntity.ok(contentItemTranslationService.getByCourseIdAndCode(courseId, code));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<ContentItemTranslationResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateContentItemTranslationRequest request
    ) {
        return ResponseEntity.ok(contentItemTranslationService.updateCourseTranslation(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        contentItemTranslationService.deleteCourseTranslation(id);
        return ResponseEntity.noContent().build();
    }
}