package com.melikyan.academy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.melikyan.academy.service.ExamSubmissionService;
import com.melikyan.academy.dto.response.examSubmission.ExamSubmissionResponse;
import com.melikyan.academy.dto.request.examSubmission.CreateExamSubmissionRequest;
import com.melikyan.academy.dto.request.examSubmission.UpdateExamSubmissionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/exam-submissions")
public class ExamSubmissionController {
    private final ExamSubmissionService examSubmissionService;

    @PostMapping
    public ResponseEntity<ExamSubmissionResponse> create(
            @Valid @RequestBody CreateExamSubmissionRequest request,
            Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(examSubmissionService.create(request, authentication));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ExamSubmissionResponse>> getMyAll(
            Authentication authentication
    ) {
        return ResponseEntity.ok(examSubmissionService.getMyAll(authentication));
    }

    @GetMapping("/me/{id}")
    public ResponseEntity<ExamSubmissionResponse> getMyById(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(examSubmissionService.getMyById(id, authentication));
    }

    @GetMapping("/me/task/{taskId}")
    public ResponseEntity<ExamSubmissionResponse> getMyByTask(
            @PathVariable UUID taskId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(examSubmissionService.getMyByTask(taskId, authentication));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<ExamSubmissionResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(examSubmissionService.getById(id));
    }

    @GetMapping("/task/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<List<ExamSubmissionResponse>> getAllByTask(
            @PathVariable UUID taskId
    ) {
        return ResponseEntity.ok(examSubmissionService.getAllByTask(taskId));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<ExamSubmissionResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateExamSubmissionRequest request
    ) {
        return ResponseEntity.ok(examSubmissionService.update(id, request));
    }

    @DeleteMapping("/me/{id}")
    public ResponseEntity<Void> deleteMy(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        examSubmissionService.deleteMy(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        examSubmissionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
