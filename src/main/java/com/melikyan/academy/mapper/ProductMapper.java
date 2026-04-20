package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.Product;
import com.melikyan.academy.entity.Category;
import org.mapstruct.NullValuePropertyMappingStrategy;
import com.melikyan.academy.entity.ProductContentItem;
import com.melikyan.academy.dto.response.product.ProductResponse;
import com.melikyan.academy.dto.request.product.CreateProductRequest;
import com.melikyan.academy.dto.request.product.UpdateProductRequest;
import com.melikyan.academy.dto.response.category.CategoryShortResponse;
import com.melikyan.academy.dto.response.contentItem.ContentItemShortResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "contentItems", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toEntity(CreateProductRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "contentItems", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateProductRequest request, @MappingTarget Product product);

    @Mapping(target = "contentItems", source = "contentItems")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "createdById", source = "createdBy.id")
    ProductResponse toResponse(Product product);

    List<ProductResponse> toResponseList(List<Product> products);

    @Mapping(target = "id", source = "contentItem.id")
    @Mapping(target = "title", source = "contentItem.title")
    @Mapping(target = "description", source = "contentItem.description")
    @Mapping(target = "type", source = "contentItem.type")
    ContentItemShortResponse toContentItemShortResponse(ProductContentItem productContentItem);

    List<ContentItemShortResponse> toContentItemShortResponseList(List<ProductContentItem> productContentItems);

    CategoryShortResponse toCategoryShortResponse(Category category);
}