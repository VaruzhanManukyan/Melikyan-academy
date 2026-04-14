package com.melikyan.academy.service;

import com.melikyan.academy.entity.User;
import org.springframework.http.HttpStatus;
import com.melikyan.academy.entity.Category;
import com.melikyan.academy.mapper.CategoryMapper;
import org.mockito.junit.jupiter.MockitoExtension;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.repository.CategoryRepository;
import org.springframework.web.server.ResponseStatusException;
import com.melikyan.academy.dto.response.category.CategoryResponse;
import com.melikyan.academy.dto.request.category.CreateCategoryRequest;
import com.melikyan.academy.dto.request.category.UpdateCategoryRequest;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createCategory_shouldCreateAndReturnResponse() {
        UUID userId = UUID.randomUUID();

        CreateCategoryRequest request = new CreateCategoryRequest(
                "  Backend  ",
                "Category description",
                userId
        );

        User user = new User();
        user.setId(userId);

        Category category = new Category();
        Category savedCategory = new Category();
        savedCategory.setId(UUID.randomUUID());
        savedCategory.setTitle("Backend");
        savedCategory.setDescription("Category description");
        savedCategory.setCreatedBy(user);

        CategoryResponse response = mock(CategoryResponse.class);

        when(categoryRepository.existsByTitleIgnoreCase("Backend")).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryMapper.toEntity(request)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(savedCategory);
        when(categoryMapper.toResponse(savedCategory)).thenReturn(response);

        CategoryResponse result = categoryService.create(request);

        assertNotNull(result);
        assertEquals(response, result);
        assertEquals("Backend", category.getTitle());
        assertEquals("Category description", category.getDescription());
        assertEquals(user, category.getCreatedBy());

        verify(categoryRepository).existsByTitleIgnoreCase("Backend");
        verify(userRepository).findById(userId);
        verify(categoryMapper).toEntity(request);
        verify(categoryRepository).save(category);
        verify(categoryMapper).toResponse(savedCategory);
    }

    @Test
    void create_shouldThrowConflict_whenTitleAlreadyExists() {
        UUID userId = UUID.randomUUID();

        CreateCategoryRequest request = new CreateCategoryRequest(
                "  Backend  ",
                "desc",
                userId
        );

        when(categoryRepository.existsByTitleIgnoreCase("Backend")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> categoryService.create(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("409 CONFLICT \"Category with this title already exists\"", exception.getMessage());

        verify(categoryRepository).existsByTitleIgnoreCase("Backend");
        verify(userRepository, never()).findById(any());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowNotFound_whenUserNotExists() {
        UUID userId = UUID.randomUUID();

        CreateCategoryRequest request = new CreateCategoryRequest(
                "Backend",
                "desc",
                userId
        );

        when(categoryRepository.existsByTitleIgnoreCase("Backend")).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> categoryService.create(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("404 NOT_FOUND \"User not found\"", exception.getMessage());

        verify(categoryRepository).existsByTitleIgnoreCase("Backend");
        verify(userRepository).findById(userId);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnResponse() {
        UUID categoryId = UUID.randomUUID();

        Category category = new Category();
        category.setId(categoryId);

        CategoryResponse response = mock(CategoryResponse.class);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(response);

        CategoryResponse result = categoryService.getById(categoryId);

        assertNotNull(result);
        assertEquals(response, result);

        verify(categoryRepository).findById(categoryId);
        verify(categoryMapper).toResponse(category);
    }

    @Test
    void getCategoryById_shouldThrowNotFound_whenDoesNotExist() {
        UUID categoryId = UUID.randomUUID();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> categoryService.getById(categoryId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Category not found with id"));

        verify(categoryRepository).findById(categoryId);
        verify(categoryMapper, never()).toResponse(any());
    }

    @Test
    void getAll_shouldReturnResponseList() {
        Category category1 = new Category();
        Category category2 = new Category();

        List<Category> categories = List.of(category1, category2);
        List<CategoryResponse> responses = List.of(
                mock(CategoryResponse.class),
                mock(CategoryResponse.class)
        );

        when(categoryRepository.findAll()).thenReturn(categories);
        when(categoryMapper.toResponseList(categories)).thenReturn(responses);

        List<CategoryResponse> result = categoryService.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(categoryRepository).findAll();
        verify(categoryMapper).toResponseList(categories);
    }

    @Test
    void updateCategory_shouldUpdateTitleAndDescription() {
        UUID categoryId = UUID.randomUUID();

        UpdateCategoryRequest request = new UpdateCategoryRequest(
                "  New Title  ",
                "  New Description  "
        );

        Category category = new Category();
        category.setId(categoryId);
        category.setTitle("Old Title");
        category.setDescription("Old Description");

        Category savedCategory = new Category();
        savedCategory.setId(categoryId);
        savedCategory.setTitle("New Title");
        savedCategory.setDescription("New Description");

        CategoryResponse response = mock(CategoryResponse.class);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByTitleIgnoreCase("New Title")).thenReturn(false);
        when(categoryRepository.save(category)).thenReturn(savedCategory);
        when(categoryMapper.toResponse(savedCategory)).thenReturn(response);

        CategoryResponse result = categoryService.update(categoryId, request);

        assertNotNull(result);
        assertEquals(response, result);
        assertEquals("New Title", category.getTitle());
        assertEquals("New Description", category.getDescription());

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).existsByTitleIgnoreCase("New Title");
        verify(categoryRepository).save(category);
        verify(categoryMapper).toResponse(savedCategory);
    }

    @Test
    void update_shouldThrowBadRequest_whenTitleIsBlank() {
        UUID categoryId = UUID.randomUUID();

        UpdateCategoryRequest request = new UpdateCategoryRequest(
                "   ",
                "Some desc"
        );

        Category category = new Category();
        category.setId(categoryId);
        category.setTitle("Old Title");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> categoryService.update(categoryId, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("400 BAD_REQUEST \"Category title cannot be blank\"", exception.getMessage());

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowConflict_whenNewTitleAlreadyExists() {
        UUID categoryId = UUID.randomUUID();

        UpdateCategoryRequest request = new UpdateCategoryRequest(
                "Existing Title",
                "Some desc"
        );

        Category category = new Category();
        category.setId(categoryId);
        category.setTitle("Old Title");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByTitleIgnoreCase("Existing Title")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> categoryService.update(categoryId, request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("409 CONFLICT \"Category with this title already exists\"", exception.getMessage());

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).existsByTitleIgnoreCase("Existing Title");
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void update_shouldNotCheckDuplicate_whenTitleDidNotChangeIgnoringCase() {
        UUID categoryId = UUID.randomUUID();

        UpdateCategoryRequest request = new UpdateCategoryRequest(
                "backend",
                "updated desc"
        );

        Category category = new Category();
        category.setId(categoryId);
        category.setTitle("Backend");
        category.setDescription("old desc");

        CategoryResponse response = mock(CategoryResponse.class);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(response);

        CategoryResponse result = categoryService.update(categoryId, request);

        assertNotNull(result);
        assertEquals("backend", category.getTitle());
        assertEquals("updated desc", category.getDescription());

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository, never()).existsByTitleIgnoreCase(any());
        verify(categoryRepository).save(category);
        verify(categoryMapper).toResponse(category);
    }

    @Test
    void deleteCategory_shouldDelete() {
        UUID categoryId = UUID.randomUUID();

        Category category = new Category();
        category.setId(categoryId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        categoryService.delete(categoryId);

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategory_shouldThrowNotFound_whenDoesNotExist() {
        UUID categoryId = UUID.randomUUID();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> categoryService.delete(categoryId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Category not found with id"));

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository, never()).delete(any());
    }
}