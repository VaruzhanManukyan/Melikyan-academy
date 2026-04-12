package com.melikyan.academy.mapper;

import org.mapstruct.*;
import com.melikyan.academy.entity.Category;
import com.melikyan.academy.dto.response.category.CategoryResponse;
import com.melikyan.academy.dto.request.category.CreateCategoryRequest;
import com.melikyan.academy.dto.request.category.UpdateCategoryRequest;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "purchasables", ignore = true)
    Category toEntity(CreateCategoryRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "purchasables", ignore = true)
    void updateEntity(@MappingTarget Category category, UpdateCategoryRequest request);

    @Mapping(target = "createdById", source = "createdBy.id")
    CategoryResponse toResponse(Category category);

    List<CategoryResponse> toResponseList(List<Category> categories);
}