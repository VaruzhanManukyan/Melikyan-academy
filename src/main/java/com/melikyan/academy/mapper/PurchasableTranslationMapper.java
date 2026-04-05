package com.melikyan.academy.mapper;

import org.mapstruct.*;
import com.melikyan.academy.entity.PurchasableTranslation;
import com.melikyan.academy.dto.response.purchasableTranslation.PurchasableTranslationResponse;
import com.melikyan.academy.dto.request.purchasableTranslation.CreatePurchasableTranslationRequest;
import com.melikyan.academy.dto.request.purchasableTranslation.UpdatePurchasableTranslationRequest;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PurchasableTranslationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "purchasable", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PurchasableTranslation toEntity(CreatePurchasableTranslationRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "purchasable", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdatePurchasableTranslationRequest request, @MappingTarget PurchasableTranslation translation);

    @Mapping(target = "purchasableId", source = "purchasable.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    PurchasableTranslationResponse toResponse(PurchasableTranslation translation);

    List<PurchasableTranslationResponse> toResponseList(List<PurchasableTranslation> translations);
}