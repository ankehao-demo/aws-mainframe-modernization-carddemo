package com.carddemo.card.service;

import com.carddemo.card.entity.Card;
import com.carddemo.card.repository.CardRepository;
import com.carddemo.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CardService {
    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public List<Card> getCardsByAccount(Long accountId) {
        return cardRepository.findByAccountId(accountId);
    }

    public Card getCard(String cardNumber) {
        return cardRepository.findById(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "cardNumber", cardNumber));
    }

    public Card updateCard(String cardNumber, Card updated) {
        Card existing = getCard(cardNumber);
        if (updated.getEmbossedName() != null) existing.setEmbossedName(updated.getEmbossedName());
        if (updated.getExpirationDate() != null) existing.setExpirationDate(updated.getExpirationDate());
        if (updated.getActiveStatus() != null) existing.setActiveStatus(updated.getActiveStatus());
        return cardRepository.save(existing);
    }
}
