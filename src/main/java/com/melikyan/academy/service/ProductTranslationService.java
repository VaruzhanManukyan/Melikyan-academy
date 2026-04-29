package com.melikyan.academy.service;

import com.melikyan.academy.dto.request.productTranslation.CreateProductTranslationRequest;
import com.melikyan.academy.dto.request.productTranslation.UpdateProductTranslationRequest;
import com.melikyan.academy.dto.response.productTranslation.ProductTranslationResponse;
import com.melikyan.academy.entity.Product;
import com.melikyan.academy.entity.ProductTranslation;
import com.melikyan.academy.entity.User;
import com.melikyan.academy.mapper.ProductTranslationMapper;
import com.melikyan.academy.repository.LanguageRepository;
import com.melikyan.academy.repository.ProductRepository;
import com.melikyan.academy.repository.ProductTranslationRepository;
import com.melikyan.academy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductTranslationService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final LanguageRepository languageRepository;
    private final ProductTranslationMapper productTranslationMapper;
    private final ProductTranslationRepository productTranslationRepository;

    private String normalizeCode(String code) {
        String normalizedCode = code.trim().toLowerCase(Locale.ROOT);

        if (normalizedCode.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product translation code must not be blank"
            );
        }

        if (normalizedCode.length() != 2) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product translation code must contain exactly 2 characters"
            );
        }

        return normalizedCode;
    }

    private String normalizeTitle(String title) {
        String normalizedTitle = title.trim();

        if (normalizedTitle.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product translation title must not be blank"
            );
        }

        return normalizedTitle;
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }

        String normalizedDescription = description.trim();
        return normalizedDescription.isBlank() ? null : normalizedDescription;
    }

    private User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + id
                ));
    }

    private Product getProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found with id: " + id
                ));
    }

    private ProductTranslation getProductTranslationById(UUID id) {
        return productTranslationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product translation not found with id: " + id
                ));
    }

    private void validateLanguageExists(String code) {
        if (!languageRepository.existsByCodeIgnoreCase(code)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Language not found with code: " + code
            );
        }
    }

    public ProductTranslationResponse create(CreateProductTranslationRequest request) {
        String normalizedCode = normalizeCode(request.code());
        String normalizedTitle = normalizeTitle(request.title());
        String normalizedDescription = normalizeDescription(request.description());

        Product product = getProductById(request.productId());
        User createdBy = getUserById(request.createdById());

        validateLanguageExists(normalizedCode);

        if (productTranslationRepository.existsByProductIdAndCodeIgnoreCase(
                request.productId(),
                normalizedCode
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product translation with this code already exists for this product"
            );
        }

        ProductTranslation productTranslation = new ProductTranslation();
        productTranslation.setCode(normalizedCode);
        productTranslation.setTitle(normalizedTitle);
        productTranslation.setDescription(normalizedDescription);
        productTranslation.setProduct(product);
        productTranslation.setCreatedBy(createdBy);

        try {
            ProductTranslation savedProductTranslation = productTranslationRepository.saveAndFlush(productTranslation);
            return productTranslationMapper.toResponse(savedProductTranslation);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product translation with this code already exists for this product",
                    exception
            );
        }
    }

    @Transactional(readOnly = true)
    public ProductTranslationResponse getById(UUID id) {
        ProductTranslation productTranslation = getProductTranslationById(id);
        return productTranslationMapper.toResponse(productTranslation);
    }

    @Transactional(readOnly = true)
    public List<ProductTranslationResponse> getAll() {
        List<ProductTranslation> productTranslations = productTranslationRepository.findAll();
        return productTranslationMapper.toResponseList(productTranslations);
    }

    @Transactional(readOnly = true)
    public List<ProductTranslationResponse> getByCode(String code) {
        String normalizedCode = normalizeCode(code);
        validateLanguageExists(normalizedCode);

        List<ProductTranslation> productTranslations = productTranslationRepository.findByCodeIgnoreCase(normalizedCode);
        return productTranslationMapper.toResponseList(productTranslations);
    }

    @Transactional(readOnly = true)
    public List<ProductTranslationResponse> getByProductId(UUID productId) {
        getProductById(productId);

        List<ProductTranslation> productTranslations = productTranslationRepository.findByProductId(productId);
        return productTranslationMapper.toResponseList(productTranslations);
    }

    @Transactional(readOnly = true)
    public ProductTranslationResponse getByProductIdAndCode(UUID productId, String code) {
        String normalizedCode = normalizeCode(code);

        getProductById(productId);
        validateLanguageExists(normalizedCode);

        ProductTranslation productTranslation = productTranslationRepository
                .findByProductIdAndCodeIgnoreCase(productId, normalizedCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product translation not found with product id: " +
                                productId + " and code: " + normalizedCode
                ));

        return productTranslationMapper.toResponse(productTranslation);
    }

    public ProductTranslationResponse update(UUID id, UpdateProductTranslationRequest request) {
        ProductTranslation productTranslation = getProductTranslationById(id);

        if (
                request.code() == null &&
                        request.title() == null &&
                        request.description() == null
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one field must be provided"
            );
        }

        if (request.code() != null) {
            String normalizedCode = normalizeCode(request.code());
            validateLanguageExists(normalizedCode);

            if (productTranslationRepository.existsByProductIdAndCodeIgnoreCaseAndIdNot(
                    productTranslation.getProduct().getId(),
                    normalizedCode,
                    id
            )) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Product translation with this code already exists for this product"
                );
            }

            productTranslation.setCode(normalizedCode);
        }

        if (request.title() != null) {
            productTranslation.setTitle(normalizeTitle(request.title()));
        }

        if (request.description() != null) {
            productTranslation.setDescription(normalizeDescription(request.description()));
        }

        try {
            ProductTranslation savedProductTranslation = productTranslationRepository.saveAndFlush(productTranslation);
            return productTranslationMapper.toResponse(savedProductTranslation);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product translation with this code already exists for this product",
                    exception
            );
        }
    }

    public void delete(UUID id) {
        ProductTranslation productTranslation = getProductTranslationById(id);
        productTranslationRepository.delete(productTranslation);
    }
}
