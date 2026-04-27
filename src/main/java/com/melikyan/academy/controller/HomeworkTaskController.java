package com.melikyan.academy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.melikyan.academy.service.HomeworkTaskService;
import com.melikyan.academy.dto.response.homeworkTask.HomeworkTaskResponse;
import com.melikyan.academy.dto.request.homeworkTask.CreateHomeworkTaskRequest;
import com.melikyan.academy.dto.request.homeworkTask.UpdateHomeworkTaskRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/homework-tasks")
public class HomeworkTaskController {
    private final HomeworkTaskService homeworkTaskService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<HomeworkTaskResponse> create(
            @Valid @RequestBody CreateHomeworkTaskRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(homeworkTaskService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HomeworkTaskResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(homeworkTaskService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<HomeworkTaskResponse>> getAll(
            @RequestParam(required = false) UUID homeworkId
    ) {
        if (homeworkId != null) {
            return ResponseEntity.ok(homeworkTaskService.getAllByHomeworkId(homeworkId));
        }

        return ResponseEntity.ok(homeworkTaskService.getAll());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<HomeworkTaskResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateHomeworkTaskRequest request
    ) {
        return ResponseEntity.ok(homeworkTaskService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        homeworkTaskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
