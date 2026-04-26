package com.melikyan.academy.service;

import com.melikyan.academy.entity.User;
import org.springframework.http.HttpStatus;
import com.melikyan.academy.entity.Category;
import com.melikyan.academy.mapper.CategoryMapper;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.repository.CategoryRepository;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import com.melikyan.academy.dto.response.category.CategoryResponse;
import com.melikyan.academy.dto.request.category.UpdateCategoryRequest;
import com.melikyan.academy.dto.request.category.CreateCategoryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryMapper categoryMapper;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    private Category getCategoryEntityById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Category not found with id: " + id
                ));
    }

    public CategoryResponse create(CreateCategoryRequest request) {
        String normalizedTitle = request.title().trim();

        if (categoryRepository.existsByTitleIgnoreCase(normalizedTitle)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Category with this title already exists"
            );
        }

        User createdBy = userRepository.findById(request.createdById())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        Category category = categoryMapper.toEntity(request);
        category.setTitle(normalizedTitle);
        category.setCreatedBy(createdBy);

        if (request.description() != null) {
            category.setDescription(request.description());
        }

        try {
            Category savedCategory = categoryRepository.saveAndFlush(category);
            return categoryMapper.toResponse(savedCategory);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Category with this title already exists",
                    exception
            );
        }
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(UUID id) {
        Category category = getCategoryEntityById(id);
        return categoryMapper.toResponse(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        List<Category> allCategories = categoryRepository.findAll();
        return categoryMapper.toResponseList(allCategories);
    }

    public CategoryResponse update(UUID id, UpdateCategoryRequest request) {
        Category category = getCategoryEntityById(id);

        if (request.title() != null) {
            String normalizedTitle = request.title().trim();

            if (normalizedTitle.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Category title cannot be blank"
                );
            }

            if (!category.getTitle().equalsIgnoreCase(normalizedTitle)
                    && categoryRepository.existsByTitleIgnoreCase(normalizedTitle)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Category with this title already exists"
                );
            }

            category.setTitle(normalizedTitle);
        }

        if (request.description() != null) {
            category.setDescription(request.description().trim());
        }

        try {
            Category savedCategory = categoryRepository.saveAndFlush(category);
            return categoryMapper.toResponse(savedCategory);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Category with this title already exists",
                    exception
            );
        }
    }

    public void delete(UUID categoryId) {
        Category category = getCategoryEntityById(categoryId);
        categoryRepository.delete(category);
    }
}
