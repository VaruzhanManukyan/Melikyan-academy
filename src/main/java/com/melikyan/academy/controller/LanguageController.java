package com.melikyan.academy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.melikyan.academy.service.LanguageService;
import com.melikyan.academy.dto.response.language.LanguageResponse;
import com.melikyan.academy.dto.request.language.CreateLanguageRequest;
import com.melikyan.academy.dto.request.language.UpdateLanguageRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/languages")
public class LanguageController {
    private final LanguageService languageService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LanguageResponse> create(
            @Valid @RequestBody CreateLanguageRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(languageService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LanguageResponse> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(languageService.getById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<LanguageResponse> getByCode(
            @PathVariable String code
    ) {
        return ResponseEntity.ok(languageService.getByCode(code));
    }

    @GetMapping
    public ResponseEntity<List<LanguageResponse>> getAll() {
        return ResponseEntity.ok(languageService.getAll());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LanguageResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLanguageRequest request
    ) {
        return ResponseEntity.ok(languageService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        languageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}