package com.melikyan.academy.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.melikyan.academy.entity.ProductRegistration;
import com.melikyan.academy.dto.response.productRegistration.ProductRegistrationResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductRegistrationMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "transactionId", source = "transaction.id")
    ProductRegistrationResponse toResponse(ProductRegistration productRegistration);

    List<ProductRegistrationResponse> toResponseList(List<ProductRegistration> productRegistrations);
}