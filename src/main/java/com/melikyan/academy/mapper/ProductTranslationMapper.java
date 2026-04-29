package com.melikyan.academy.mapper;

import org.mapstruct.*;
import com.melikyan.academy.entity.ProductTranslation;
import com.melikyan.academy.dto.response.productTranslation.ProductTranslationResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductTranslationMapper {
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    ProductTranslationResponse toResponse(ProductTranslation translation);

    List<ProductTranslationResponse> toResponseList(List<ProductTranslation> translations);
}
