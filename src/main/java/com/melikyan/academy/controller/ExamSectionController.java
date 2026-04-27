package com.melikyan.academy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.melikyan.academy.service.ExamSectionService;
import com.melikyan.academy.dto.response.examSection.ExamSectionResponse;
import com.melikyan.academy.dto.request.examSection.CreateExamSectionRequest;
import com.melikyan.academy.dto.request.examSection.UpdateExamSectionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/exam-sections")
public class ExamSectionController {
    private final ExamSectionService examSectionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<ExamSectionResponse> create(
            @Valid @RequestBody CreateExamSectionRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(examSectionService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamSectionResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(examSectionService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ExamSectionResponse>> getAll(
            @RequestParam(required = false) UUID examId
    ) {
        if (examId != null) {
            return ResponseEntity.ok(examSectionService.getByExamId(examId));
        }

        return ResponseEntity.ok(examSectionService.getAll());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<ExamSectionResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateExamSectionRequest request
    ) {
        return ResponseEntity.ok(examSectionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        examSectionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
