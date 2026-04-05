package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.Transaction;
import org.mapstruct.NullValuePropertyMappingStrategy;
import com.melikyan.academy.dto.response.transaction.TransactionResponse;
import com.melikyan.academy.dto.request.transaction.CreateTransactionRequest;
import com.melikyan.academy.dto.request.transaction.UpdateTransactionRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "productRegistrations", ignore = true)
    Transaction toEntity(CreateTransactionRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "productRegistrations", ignore = true)
    void updateEntityFromRequest(UpdateTransactionRequest request, @MappingTarget Transaction transaction);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "productId", source = "product.id")
    TransactionResponse toResponse(Transaction transaction);

    List<TransactionResponse> toResponseList(List<Transaction> transactions);
}
