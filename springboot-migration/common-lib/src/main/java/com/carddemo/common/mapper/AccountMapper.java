package com.carddemo.common.mapper;

import com.carddemo.common.dto.AccountDto;
import com.carddemo.common.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AccountMapper {

    AccountDto toDto(Account account);

    Account toEntity(AccountDto dto);

    List<AccountDto> toDtoList(List<Account> accounts);

    void updateEntityFromDto(AccountDto dto, @MappingTarget Account account);
}
