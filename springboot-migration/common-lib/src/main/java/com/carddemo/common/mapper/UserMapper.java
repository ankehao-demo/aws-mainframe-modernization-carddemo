package com.carddemo.common.mapper;

import com.carddemo.common.dto.CreateUserRequest;
import com.carddemo.common.dto.UpdateUserRequest;
import com.carddemo.common.dto.UserDto;
import com.carddemo.common.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    UserDto toDto(User user);

    List<UserDto> toDtoList(List<User> users);

    @Mapping(target = "password", ignore = true)
    User toEntity(CreateUserRequest request);

    void updateEntityFromRequest(UpdateUserRequest request, @MappingTarget User user);
}
