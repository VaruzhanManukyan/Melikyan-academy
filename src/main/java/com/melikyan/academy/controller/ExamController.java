package com.melikyan.academy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.melikyan.academy.service.ExamService;
import org.springframework.web.bind.annotation.*;
import com.melikyan.academy.dto.response.exam.ExamResponse;
import com.melikyan.academy.dto.request.exam.UpdateExamRequest;
import com.melikyan.academy.dto.request.exam.CreateExamRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/exams")
public class ExamController {
    private final ExamService examService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<ExamResponse> create(
            @Valid @RequestBody CreateExamRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(examService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(examService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ExamResponse>> getAll() {
        return ResponseEntity.ok(examService.getAll());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<ExamResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateExamRequest request
    ) {
        return ResponseEntity.ok(examService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        examService.delete(id);
        return ResponseEntity.noContent().build();
    }
}