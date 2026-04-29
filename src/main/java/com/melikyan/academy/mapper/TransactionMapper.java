package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.Transaction;
import com.melikyan.academy.dto.response.transaction.TransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "productId", source = "product.id")
    TransactionResponse toResponse(Transaction transaction);

    List<TransactionResponse> toResponseList(List<Transaction> transactions);
}
