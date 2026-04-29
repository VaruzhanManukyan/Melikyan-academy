package com.melikyan.academy.controller;

import com.melikyan.academy.dto.request.homeworkTranslation.CreateHomeworkTranslationRequest;
import com.melikyan.academy.dto.request.homeworkTranslation.UpdateHomeworkTranslationRequest;
import com.melikyan.academy.dto.response.homeworkTranslation.HomeworkTranslationResponse;
import com.melikyan.academy.service.HomeworkTranslationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/homework-translations")
public class HomeworkTranslationController {
    private final HomeworkTranslationService homeworkTranslationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<HomeworkTranslationResponse> create(
            @Valid @RequestBody CreateHomeworkTranslationRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(homeworkTranslationService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HomeworkTranslationResponse> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(homeworkTranslationService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<HomeworkTranslationResponse>> getAll() {
        return ResponseEntity.ok(homeworkTranslationService.getAll());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<List<HomeworkTranslationResponse>> getByCode(
            @PathVariable String code
    ) {
        return ResponseEntity.ok(homeworkTranslationService.getByCode(code));
    }

    @GetMapping("/homework/{homeworkId}")
    public ResponseEntity<List<HomeworkTranslationResponse>> getByHomeworkId(
            @PathVariable UUID homeworkId
    ) {
        return ResponseEntity.ok(homeworkTranslationService.getByHomeworkId(homeworkId));
    }

    @GetMapping("/homework/{homeworkId}/code/{code}")
    public ResponseEntity<HomeworkTranslationResponse> getByHomeworkIdAndCode(
            @PathVariable UUID homeworkId,
            @PathVariable String code
    ) {
        return ResponseEntity.ok(homeworkTranslationService.getByHomeworkIdAndCode(homeworkId, code));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<HomeworkTranslationResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateHomeworkTranslationRequest request
    ) {
        return ResponseEntity.ok(homeworkTranslationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        homeworkTranslationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}