package com.melikyan.academy.controller;

import com.melikyan.academy.dto.request.productTranslation.CreateProductTranslationRequest;
import com.melikyan.academy.dto.request.productTranslation.UpdateProductTranslationRequest;
import com.melikyan.academy.dto.response.productTranslation.ProductTranslationResponse;
import com.melikyan.academy.service.ProductTranslationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/product-translations")
public class ProductTranslationController {
    private final ProductTranslationService productTranslationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<ProductTranslationResponse> create(
            @Valid @RequestBody CreateProductTranslationRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productTranslationService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductTranslationResponse> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(productTranslationService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ProductTranslationResponse>> getAll() {
        return ResponseEntity.ok(productTranslationService.getAll());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<List<ProductTranslationResponse>> getByCode(
            @PathVariable String code
    ) {
        return ResponseEntity.ok(productTranslationService.getByCode(code));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductTranslationResponse>> getByProductId(
            @PathVariable UUID productId
    ) {
        return ResponseEntity.ok(productTranslationService.getByProductId(productId));
    }

    @GetMapping("/product/{productId}/code/{code}")
    public ResponseEntity<ProductTranslationResponse> getByProductIdAndCode(
            @PathVariable UUID productId,
            @PathVariable String code
    ) {
        return ResponseEntity.ok(productTranslationService.getByProductIdAndCode(productId, code));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<ProductTranslationResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductTranslationRequest request
    ) {
        return ResponseEntity.ok(productTranslationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        productTranslationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
