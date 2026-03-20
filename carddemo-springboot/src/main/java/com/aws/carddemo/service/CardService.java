package com.aws.carddemo.service;

import com.aws.carddemo.dto.CardUpdateRequest;
import com.aws.carddemo.entity.Card;
import com.aws.carddemo.exception.ResourceNotFoundException;
import com.aws.carddemo.repository.CardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Card management — mirrors COCRDLIC.cbl (list), COCRDSLC.cbl (view), COCRDUPC.cbl (update).
 * COCRDLIC uses STARTBR/READNEXT for pagination — replaced with Spring Data pagination.
 */
@Service
public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public Page<Card> listCards(String acctId, Pageable pageable) {
        if (acctId != null && !acctId.isBlank()) {
            return cardRepository.findByAcctId(acctId, pageable);
        }
        return cardRepository.findAll(pageable);
    }

    public Card getCard(String cardNum) {
        return cardRepository.findById(cardNum)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardNum));
    }

    @Transactional
    public Card updateCard(String cardNum, CardUpdateRequest request) {
        Card card = cardRepository.findById(cardNum)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardNum));

        if (request.embossedName() != null) card.setEmbossedName(request.embossedName());
        if (request.expirationDate() != null) card.setExpirationDate(request.expirationDate());
        if (request.activeStatus() != null) card.setActiveStatus(request.activeStatus());

        return cardRepository.save(card);
    }
}
