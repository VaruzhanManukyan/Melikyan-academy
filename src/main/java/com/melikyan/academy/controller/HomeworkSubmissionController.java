package com.melikyan.academy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.melikyan.academy.service.HomeworkSubmissionService;
import com.melikyan.academy.dto.response.homeworkSubmission.HomeworkSubmissionResponse;
import com.melikyan.academy.dto.request.homeworkSubmission.UpdateHomeworkSubmissionRequest;
import com.melikyan.academy.dto.request.homeworkSubmission.CreateHomeworkSubmissionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/homework-submissions")
public class HomeworkSubmissionController {
    private final HomeworkSubmissionService homeworkSubmissionService;

    @PostMapping
    public ResponseEntity<HomeworkSubmissionResponse> create(
            @Valid @RequestBody CreateHomeworkSubmissionRequest request,
            Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(homeworkSubmissionService.create(request, authentication));
    }

    @GetMapping("/me")
    public ResponseEntity<List<HomeworkSubmissionResponse>> getMyAll(
            Authentication authentication
    ) {
        return ResponseEntity.ok(homeworkSubmissionService.getMyAll(authentication));
    }

    @GetMapping("/me/{id}")
    public ResponseEntity<HomeworkSubmissionResponse> getMyById(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(homeworkSubmissionService.getMyById(id, authentication));
    }

    @GetMapping("/me/task/{taskId}")
    public ResponseEntity<HomeworkSubmissionResponse> getMyByTask(
            @PathVariable UUID taskId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(homeworkSubmissionService.getMyByTask(taskId, authentication));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<HomeworkSubmissionResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(homeworkSubmissionService.getById(id));
    }

    @GetMapping("/task/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<List<HomeworkSubmissionResponse>> getAllByTask(
            @PathVariable UUID taskId
    ) {
        return ResponseEntity.ok(homeworkSubmissionService.getAllByTask(taskId));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<HomeworkSubmissionResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateHomeworkSubmissionRequest request
    ) {
        return ResponseEntity.ok(homeworkSubmissionService.update(id, request));
    }

    @DeleteMapping("/me/{id}")
    public ResponseEntity<Void> deleteMy(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        homeworkSubmissionService.deleteMy(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        homeworkSubmissionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
