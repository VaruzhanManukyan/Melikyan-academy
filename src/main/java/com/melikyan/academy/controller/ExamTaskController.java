package com.melikyan.academy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.melikyan.academy.service.ExamTaskService;
import com.melikyan.academy.dto.response.examTask.ExamTaskResponse;
import com.melikyan.academy.dto.request.examTask.CreateExamTaskRequest;
import com.melikyan.academy.dto.request.examTask.UpdateExamTaskRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/exam-tasks")
public class ExamTaskController {
    private final ExamTaskService examTaskService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<ExamTaskResponse> create(
            @Valid @RequestBody CreateExamTaskRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(examTaskService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamTaskResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(examTaskService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ExamTaskResponse>> getAll(
            @RequestParam(required = false) UUID sectionId
    ) {
        if (sectionId != null) {
            return ResponseEntity.ok(examTaskService.getAllByExamSectionId(sectionId));
        }

        return ResponseEntity.ok(examTaskService.getAll());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<ExamTaskResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateExamTaskRequest request
    ) {
        return ResponseEntity.ok(examTaskService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        examTaskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
