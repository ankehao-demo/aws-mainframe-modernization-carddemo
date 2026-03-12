package com.carddemo.common.mapper;

import com.carddemo.common.dto.CardDto;
import com.carddemo.common.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CardMapper {

    CardDto toDto(Card card);

    Card toEntity(CardDto dto);

    List<CardDto> toDtoList(List<Card> cards);

    void updateEntityFromDto(CardDto dto, @MappingTarget Card card);
}
