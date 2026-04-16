package com.melikyan.academy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.melikyan.academy.service.HomeworkService;
import com.melikyan.academy.dto.response.homework.HomeworkResponse;
import com.melikyan.academy.dto.request.homework.CreateHomeworkRequest;
import com.melikyan.academy.dto.request.homework.UpdateHomeworkRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/homeworks")
public class HomeworkController {
    private final HomeworkService homeworkService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<HomeworkResponse> create(
            @Valid @RequestBody CreateHomeworkRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(homeworkService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HomeworkResponse> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(homeworkService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<HomeworkResponse>> getAll() {
        return ResponseEntity.ok(homeworkService.getAll());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<HomeworkResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateHomeworkRequest request
    ) {
        return ResponseEntity.ok(homeworkService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<HomeworkResponse> delete(
            @PathVariable UUID id
    ) {
        homeworkService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
