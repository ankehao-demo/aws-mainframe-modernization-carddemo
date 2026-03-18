package com.aws.carddemo.service;

import com.aws.carddemo.dto.CardDto;
import com.aws.carddemo.dto.PagedResponse;
import com.aws.carddemo.entity.Card;
import com.aws.carddemo.exception.ResourceNotFoundException;
import com.aws.carddemo.repository.CardRepository;
import com.aws.carddemo.repository.CardXrefRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final CardXrefRepository cardXrefRepository;

    public CardService(CardRepository cardRepository, CardXrefRepository cardXrefRepository) {
        this.cardRepository = cardRepository;
        this.cardXrefRepository = cardXrefRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<CardDto> listCards(Pageable pageable) {
        Page<Card> page = cardRepository.findAll(pageable);
        List<CardDto> cards = page.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return PagedResponse.<CardDto>builder()
                .content(cards)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public CardDto getCard(String cardNum) {
        Card card = cardRepository.findById(cardNum)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardNum));
        return mapToDto(card);
    }

    @Transactional
    public CardDto updateCard(String cardNum, CardDto cardDto) {
        Card card = cardRepository.findById(cardNum)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardNum));

        if (cardDto.getCardEmbossedName() != null) {
            card.setCardEmbossedName(cardDto.getCardEmbossedName());
        }
        if (cardDto.getCardActiveStatus() != null) {
            card.setCardActiveStatus(cardDto.getCardActiveStatus());
        }
        if (cardDto.getCardExpirationDate() != null) {
            card.setCardExpirationDate(cardDto.getCardExpirationDate());
        }

        Card saved = cardRepository.save(card);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<CardDto> getCardsByAccount(String acctId) {
        return cardRepository.findByCardAcctId(acctId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private CardDto mapToDto(Card card) {
        return CardDto.builder()
                .cardNum(card.getCardNum())
                .cardAcctId(card.getCardAcctId())
                .cardCvvCd(card.getCardCvvCd())
                .cardEmbossedName(card.getCardEmbossedName())
                .cardExpirationDate(card.getCardExpirationDate())
                .cardActiveStatus(card.getCardActiveStatus())
                .build();
    }
}
