package com.carddemo.cardservice.service;

import com.carddemo.cardservice.exception.CardNotFoundException;
import com.carddemo.cardservice.exception.OptimisticLockException;
import com.carddemo.cardservice.model.Card;
import com.carddemo.cardservice.model.dto.CardDetailResponse;
import com.carddemo.cardservice.model.dto.CardListResponse;
import com.carddemo.cardservice.model.dto.CardListResponse.CardSummary;
import com.carddemo.cardservice.model.dto.CardUpdateRequest;
import com.carddemo.cardservice.repository.CardRepository;
import com.carddemo.cardservice.validation.CardValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer implementing business logic from COCRDLIC, COCRDSLC, and COCRDUPC.
 */
@Service
public class CardService {

    private final CardRepository cardRepository;
    private final CardValidator cardValidator;

    public CardService(CardRepository cardRepository, CardValidator cardValidator) {
        this.cardRepository = cardRepository;
        this.cardValidator = cardValidator;
    }

    /**
     * List cards with optional filters, replacing COCRDLIC.cbl (CCLI transaction).
     * The COBOL program uses CICS STARTBR/READNEXT with GTEQ positioning
     * and reads up to WS-MAX-SCREEN-LINES (7) records per page.
     * This is replaced with Spring Data JPA pagination sorted by card_num (VSAM KSDS key order).
     */
    @Transactional(readOnly = true)
    public CardListResponse listCards(String accountId, String cardNum, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("cardNum").ascending());

        Page<Card> cardPage;
        if (accountId != null && !accountId.isBlank() && cardNum != null && !cardNum.isBlank()) {
            cardPage = cardRepository.findByFilters(accountId, cardNum, pageable);
        } else if (accountId != null && !accountId.isBlank()) {
            cardPage = cardRepository.findByCardAcctId(accountId, pageable);
        } else if (cardNum != null && !cardNum.isBlank()) {
            cardPage = cardRepository.findByCardNumStartingWith(cardNum, pageable);
        } else {
            cardPage = cardRepository.findAll(pageable);
        }

        List<CardSummary> summaries = cardPage.getContent().stream()
                .map(c -> new CardSummary(c.getCardNum(), c.getCardAcctId(), c.getCardActiveStatus()))
                .toList();

        return new CardListResponse(
                summaries,
                cardPage.getNumber(),
                cardPage.getSize(),
                cardPage.getTotalElements(),
                cardPage.getTotalPages(),
                cardPage.hasNext(),
                cardPage.hasPrevious()
        );
    }

    /**
     * Get card detail, replacing COCRDSLC.cbl (CCDL transaction).
     * The COBOL program performs a direct CICS READ on CARDDAT by card number.
     */
    @Transactional(readOnly = true)
    public CardDetailResponse getCard(String cardNum) {
        Card card = cardRepository.findById(cardNum)
                .orElseThrow(() -> new CardNotFoundException(cardNum));
        return toDetailResponse(card);
    }

    /**
     * Update a card, replacing COCRDUPC.cbl (CCUP transaction).
     * The COBOL program:
     * 1. Validates input fields (paragraphs 1210-1260)
     * 2. Compares old vs new card data for change detection
     * 3. Rewrites the VSAM record if changes are confirmed
     *
     * JPA @Version replaces the manual CCUP-OLD-CARDDATA vs CCUP-NEW-CARDDATA comparison.
     */
    @Transactional
    public CardDetailResponse updateCard(String cardNum, CardUpdateRequest request) {
        cardValidator.validateCardUpdate(request);

        Card card = cardRepository.findById(cardNum)
                .orElseThrow(() -> new CardNotFoundException(cardNum));

        if (!card.getVersion().equals(request.version())) {
            throw new OptimisticLockException(cardNum);
        }

        card.setCardAcctId(request.cardAcctId());
        card.setCardEmbossedName(request.cardEmbossedName());
        card.setCardActiveStatus(request.cardActiveStatus());
        card.setCardExpirationDate(request.cardExpirationDate());

        Card saved = cardRepository.save(card);
        return toDetailResponse(saved);
    }

    private CardDetailResponse toDetailResponse(Card card) {
        return new CardDetailResponse(
                card.getCardNum(),
                card.getCardAcctId(),
                card.getCardCvvCd(),
                card.getCardEmbossedName(),
                card.getCardExpirationDate(),
                card.getCardActiveStatus(),
                card.getVersion()
        );
    }
}
