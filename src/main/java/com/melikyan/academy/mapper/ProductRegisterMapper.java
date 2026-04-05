package com.melikyan.academy.mapper;

import org.mapstruct.NullValuePropertyMappingStrategy;
import com.melikyan.academy.entity.ProductRegistration;
import com.melikyan.academy.dto.response.productRegister.ProductRegisterResponse;
import com.melikyan.academy.dto.request.productRegister.CreateProductRegisterRequest;
import com.melikyan.academy.dto.request.productRegister.UpdateProductRegisterRequest;
import org.mapstruct.Mapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;

import java.util.List;

public interface ProductRegisterMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProductRegistration toEntity(CreateProductRegisterRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateProductRegisterRequest request, @MappingTarget ProductRegistration productRegistration);

    ProductRegisterResponse toResponse(ProductRegistration productRegistration);

    List<ProductRegisterResponse> toResponseList(List<ProductRegistration> productRegistrations);
}
