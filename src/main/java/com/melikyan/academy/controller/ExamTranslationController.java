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
@RequestMapping("/api/v1/exam-translations")
public class ExamTranslationController {
    private final ContentItemTranslationService contentItemTranslationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<ContentItemTranslationResponse> create(
            @Valid @RequestBody CreateContentItemTranslationRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(contentItemTranslationService.createExamTranslation(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContentItemTranslationResponse> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(contentItemTranslationService.getExamTranslationById(id));
    }

    @GetMapping
    public ResponseEntity<List<ContentItemTranslationResponse>> getAll() {
        return ResponseEntity.ok(contentItemTranslationService.getAllExamTranslations());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<List<ContentItemTranslationResponse>> getByCode(
            @PathVariable String code
    ) {
        return ResponseEntity.ok(contentItemTranslationService.getExamTranslationsByCode(code));
    }

    @GetMapping("/exam/{examId}")
    public ResponseEntity<List<ContentItemTranslationResponse>> getByExamId(
            @PathVariable UUID examId
    ) {
        return ResponseEntity.ok(contentItemTranslationService.getByExamId(examId));
    }

    @GetMapping("/exam/{examId}/code/{code}")
    public ResponseEntity<ContentItemTranslationResponse> getByExamIdAndCode(
            @PathVariable UUID examId,
            @PathVariable String code
    ) {
        return ResponseEntity.ok(contentItemTranslationService.getByExamIdAndCode(examId, code));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<ContentItemTranslationResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateContentItemTranslationRequest request
    ) {
        return ResponseEntity.ok(contentItemTranslationService.updateExamTranslation(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        contentItemTranslationService.deleteExamTranslation(id);
        return ResponseEntity.noContent().build();
    }
}