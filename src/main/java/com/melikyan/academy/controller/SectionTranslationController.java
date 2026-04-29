package com.melikyan.academy.controller;

import com.melikyan.academy.dto.request.sectionTranslation.CreateSectionTranslationRequest;
import com.melikyan.academy.dto.request.sectionTranslation.UpdateSectionTranslationRequest;
import com.melikyan.academy.dto.response.sectionTranslation.SectionTranslationResponse;
import com.melikyan.academy.service.SectionTranslationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/section-translations")
public class SectionTranslationController {
    private final SectionTranslationService sectionTranslationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<SectionTranslationResponse> create(
            @Valid @RequestBody CreateSectionTranslationRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(sectionTranslationService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SectionTranslationResponse> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(sectionTranslationService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<SectionTranslationResponse>> getAll() {
        return ResponseEntity.ok(sectionTranslationService.getAll());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<List<SectionTranslationResponse>> getByCode(
            @PathVariable String code
    ) {
        return ResponseEntity.ok(sectionTranslationService.getByCode(code));
    }

    @GetMapping("/exam-section/{examSectionId}")
    public ResponseEntity<List<SectionTranslationResponse>> getByExamSectionId(
            @PathVariable UUID examSectionId
    ) {
        return ResponseEntity.ok(sectionTranslationService.getByExamSectionId(examSectionId));
    }

    @GetMapping("/exam-section/{examSectionId}/code/{code}")
    public ResponseEntity<SectionTranslationResponse> getByExamSectionIdAndCode(
            @PathVariable UUID examSectionId,
            @PathVariable String code
    ) {
        return ResponseEntity.ok(sectionTranslationService.getByExamSectionIdAndCode(examSectionId, code));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<SectionTranslationResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSectionTranslationRequest request
    ) {
        return ResponseEntity.ok(sectionTranslationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        sectionTranslationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
