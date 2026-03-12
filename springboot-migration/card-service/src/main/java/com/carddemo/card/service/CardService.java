package com.carddemo.card.service;

import com.carddemo.common.dto.CardDto;
import com.carddemo.common.entity.Card;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.mapper.CardMapper;
import com.carddemo.common.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Ports COBOL programs:
 * - COCRDLIC.cbl (CCLI) → listCards
 * - COCRDSLC.cbl (CCDL) → getCard
 * - COCRDUPC.cbl (CCUP) → updateCard
 * Replaces CICS VSAM operations on CARDDATA file.
 */
@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    public List<CardDto> listCards(Long accountId) {
        List<Card> cards = cardRepository.findByAccountId(accountId);
        return cardMapper.toDtoList(cards);
    }

    public CardDto getCard(String cardNumber) {
        Card card = cardRepository.findById(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "cardNumber", cardNumber));
        return cardMapper.toDto(card);
    }

    @Transactional
    public CardDto updateCard(String cardNumber, CardDto cardDto) {
        Card card = cardRepository.findById(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "cardNumber", cardNumber));
        cardMapper.updateEntityFromDto(cardDto, card);
        Card saved = cardRepository.save(card);
        return cardMapper.toDto(saved);
    }
}
