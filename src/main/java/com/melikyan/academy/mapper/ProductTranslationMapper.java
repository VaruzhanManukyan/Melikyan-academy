package com.melikyan.academy.mapper;

import org.mapstruct.*;
import com.melikyan.academy.entity.ProductTranslation;
import com.melikyan.academy.dto.response.productTranslation.ProductTranslationResponse;
import com.melikyan.academy.dto.request.productTranslation.CreateProductTranslationRequest;
import com.melikyan.academy.dto.request.productTranslation.UpdateProductTranslationRequest;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductTranslationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProductTranslation toEntity(CreateProductTranslationRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateProductTranslationRequest request, @MappingTarget ProductTranslation translation);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    ProductTranslationResponse toResponse(ProductTranslation translation);

    List<ProductTranslationResponse> toResponseList(List<ProductTranslation> translations);
}
