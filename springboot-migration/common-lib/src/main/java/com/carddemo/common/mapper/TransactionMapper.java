package com.carddemo.common.mapper;

import com.carddemo.common.dto.TransactionDto;
import com.carddemo.common.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TransactionMapper {

    TransactionDto toDto(Transaction transaction);

    Transaction toEntity(TransactionDto dto);

    List<TransactionDto> toDtoList(List<Transaction> transactions);
}
