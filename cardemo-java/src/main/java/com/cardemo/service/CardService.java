package com.cardemo.service;

import com.cardemo.dto.CardDetailResponse;
import com.cardemo.dto.CardListPageResponse;
import com.cardemo.dto.CardListResponse;
import com.cardemo.entity.Card;
import com.cardemo.exception.ResourceNotFoundException;
import com.cardemo.repository.CardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Card service converting COCRDLIC.cbl (card list) and COCRDSLC.cbl (card detail) logic.
 *
 * COCRDLIC card list:
 *   - 9500-FILTER-RECORDS (lines 1382-1407): filter by account ID and/or card number
 *   - Uses VSAM browse (STARTBR/READNEXT/ENDBR) -> Spring Pageable
 *   - 7 rows per page in COBOL, configurable in Java
 *
 * COCRDSLC card detail:
 *   - READ CARDDAT by card number
 *   - Returns card details including split expiration date
 */
@Service
public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public CardListPageResponse listCards(Long accountId, String cardNumber, Pageable pageable) {
        Page<Card> page;

        if (accountId != null && cardNumber != null) {
            page = cardRepository.findByAcctIdAndCardNum(accountId, cardNumber, pageable);
        } else if (accountId != null) {
            page = cardRepository.findByAcctId(accountId, pageable);
        } else if (cardNumber != null) {
            page = cardRepository.findByCardNum(cardNumber, pageable);
        } else {
            page = cardRepository.findAll(pageable);
        }

        var content = page.getContent().stream()
                .map(card -> new CardListResponse(
                        card.getAcctId(),
                        card.getCardNum(),
                        card.getActiveStatus()))
                .toList();

        return new CardListPageResponse(
                content,
                page.getNumber(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }

    public CardDetailResponse getCardDetail(String cardNumber) {
        Card card = cardRepository.findByCardNum(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Card number not found: " + cardNumber));

        return buildDetailResponse(card);
    }

    private CardDetailResponse buildDetailResponse(Card card) {
        CardDetailResponse response = new CardDetailResponse();
        response.setCardNumber(card.getCardNum());
        response.setAccountId(card.getAcctId());
        response.setCvvCode(card.getCvvCode());
        response.setEmbossedName(card.getEmbossedName());
        response.setExpirationDate(card.getExpirationDate());
        response.setActiveStatus(card.getActiveStatus());

        // Split expiration date into month/year for display (COCRDSLC.cbl lines 480-482)
        if (card.getExpirationDate() != null && card.getExpirationDate().length() >= 7) {
            String expDate = card.getExpirationDate();
            // Expected format: YYYY-MM-DD or similar
            if (expDate.contains("-")) {
                String[] parts = expDate.split("-");
                if (parts.length >= 2) {
                    response.setExpirationYear(parts[0]);
                    response.setExpirationMonth(parts[1]);
                }
            }
        }

        return response;
    }
}
