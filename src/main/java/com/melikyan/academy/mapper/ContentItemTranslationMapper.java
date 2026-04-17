package com.melikyan.academy.mapper;

import org.mapstruct.*;
import com.melikyan.academy.entity.ContentItemTranslation;
import com.melikyan.academy.dto.response.purchasableTranslation.PurchasableTranslationResponse;
import com.melikyan.academy.dto.request.purchasableTranslation.CreateContentItemTranslationRequest;
import com.melikyan.academy.dto.request.purchasableTranslation.UpdatePurchasableTranslationRequest;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContentItemTranslationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdItem", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ContentItemTranslation toEntity(CreateContentItemTranslationRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdItem", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdatePurchasableTranslationRequest request, @MappingTarget ContentItemTranslation translation);

    @Mapping(target = "contentId", source = "purchasable.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    PurchasableTranslationResponse toResponse(ContentItemTranslation translation);

    List<PurchasableTranslationResponse> toResponseList(List<ContentItemTranslation> translations);
}