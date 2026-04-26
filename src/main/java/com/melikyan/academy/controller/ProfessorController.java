package com.melikyan.academy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.melikyan.academy.service.ProfessorService;
import com.melikyan.academy.dto.response.professor.ProfessorUserData;
import com.melikyan.academy.dto.response.professor.ProfessorResponse;
import com.melikyan.academy.dto.response.professor.ProfessorUserResponse;
import com.melikyan.academy.dto.request.professor.AssignProfessorRequest;
import com.melikyan.academy.dto.request.professor.CreateProfessorUserRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/professors")
public class ProfessorController {
    private final ProfessorService professorService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfessorUserResponse> create(
            @Valid @RequestBody CreateProfessorUserRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(professorService.create(request));
    }

    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfessorResponse> assign(
            @Valid @RequestBody AssignProfessorRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(professorService.assign(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfessorResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(professorService.getById(id));
    }

    @GetMapping("/courses/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProfessorResponse>> getAllByCourse(
            @PathVariable UUID courseId
    ) {
        return ResponseEntity.ok(professorService.getAllByCourse(courseId));
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProfessorResponse>> getAllByUser(
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(professorService.getAllByUser(userId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProfessorUserData>> getAllProfessorUsers() {
        return ResponseEntity.ok(professorService.getAllProfessorUsers());
    }

    @DeleteMapping("/users/{userId}/courses/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID userId,
            @PathVariable UUID courseId
    ) {
        professorService.delete(userId, courseId);
        return ResponseEntity.noContent().build();
    }
}