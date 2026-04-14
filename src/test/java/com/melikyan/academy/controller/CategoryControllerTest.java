package com.melikyan.academy.controller;

import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;
import com.melikyan.academy.service.CategoryService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.FilterType;
import com.melikyan.academy.security.RememberMeSecurityFilter;
import com.melikyan.academy.dto.response.category.CategoryResponse;
import com.melikyan.academy.dto.request.category.CreateCategoryRequest;
import com.melikyan.academy.dto.request.category.UpdateCategoryRequest;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@WebMvcTest(
        controllers = CategoryController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                ServletWebSecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        },
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = RememberMeSecurityFilter.class
                )
        }
)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void createCategory_shouldReturnCreated() throws Exception {
        UUID userId = UUID.randomUUID();

        CreateCategoryRequest request = new CreateCategoryRequest(
                "Backend",
                "Description",
                userId
        );

        CategoryResponse response = mock(CategoryResponse.class);

        when(categoryService.createCategory(any(CreateCategoryRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(categoryService).createCategory(any(CreateCategoryRequest.class));
    }

    @Test
    void getCategoryById_shouldReturnOk() throws Exception {
        UUID categoryId = UUID.randomUUID();

        CategoryResponse response = mock(CategoryResponse.class);

        when(categoryService.getCategoryById(categoryId))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/categories/{id}", categoryId))
                .andExpect(status().isOk());

        verify(categoryService).getCategoryById(categoryId);
    }

    @Test
    void getAllCategories_shouldReturnOk() throws Exception {
        List<CategoryResponse> responses = List.of(
                mock(CategoryResponse.class),
                mock(CategoryResponse.class)
        );

        when(categoryService.getAllCategories())
                .thenReturn(responses);

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk());

        verify(categoryService).getAllCategories();
    }

    @Test
    void updateCategory_shouldReturnOk() throws Exception {
        UUID categoryId = UUID.randomUUID();

        UpdateCategoryRequest request = new UpdateCategoryRequest(
                "Updated title",
                "Updated description"
        );

        CategoryResponse response = mock(CategoryResponse.class);

        when(categoryService.updateCategory(eq(categoryId), any(UpdateCategoryRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(categoryService).updateCategory(eq(categoryId), any(UpdateCategoryRequest.class));
    }

    @Test
    void deleteCategory_shouldReturnNoContent() throws Exception {
        UUID categoryId = UUID.randomUUID();

        doNothing().when(categoryService).deleteCategory(categoryId);

        mockMvc.perform(delete("/api/v1/categories/{id}", categoryId))
                .andExpect(status().isNoContent());

        verify(categoryService).deleteCategory(categoryId);
    }
}