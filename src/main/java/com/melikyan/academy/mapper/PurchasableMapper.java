package com.melikyan.academy.mapper;

import org.mapstruct.*;
import com.melikyan.academy.entity.Purchasable;
import com.melikyan.academy.dto.response.purchasable.PurchasableResponse;
import com.melikyan.academy.dto.request.purchasable.CreatePurchasableRequest;
import com.melikyan.academy.dto.request.purchasable.UpdatePurchasableRequest;

import java.util.List;

@Mapper(componentModel = "srping")
public interface PurchasableMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    Purchasable toEntity(CreatePurchasableRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)

    void updateEntity(@MappingTarget Purchasable purchasable, UpdatePurchasableRequest request);
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "category", source = "category")
    PurchasableResponse toResponse(Purchasable purchasable);

    List<PurchasableResponse> toResponseList(List<Purchasable> purchasables);
}
