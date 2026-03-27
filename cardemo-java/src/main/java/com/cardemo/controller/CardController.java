package com.cardemo.controller;

import com.cardemo.dto.CardDetailResponse;
import com.cardemo.dto.CardListPageResponse;
import com.cardemo.exception.CardDemoException;
import com.cardemo.service.CardService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Card controller converting COCRDLIC.cbl (card list) and COCRDSLC.cbl (card detail) logic.
 *
 * COCRDLIC card list (1459 lines):
 *   - Lists credit cards with pagination (7 per page in COBOL)
 *   - Optional filtering by account ID and card number
 *   - Validation at lines 1003-1067
 *
 * COCRDSLC card detail (887 lines):
 *   - Shows details of a single credit card
 *   - Reads from CARDDAT by card number
 *   - Validation at lines 647-719
 */
@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public ResponseEntity<CardListPageResponse> listCards(
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) String cardNumber,
            @PageableDefault(size = 7) Pageable pageable) {

        Long acctId = null;
        if (accountId != null && !accountId.isBlank()) {
            if (!accountId.matches("\\d{11}")) {
                throw new CardDemoException(
                        "Account ID filter must be an 11-digit number");
            }
            acctId = Long.parseLong(accountId);
        }

        String cardNum = null;
        if (cardNumber != null && !cardNumber.isBlank()) {
            if (!cardNumber.matches("\\d{16}")) {
                throw new CardDemoException(
                        "Card number filter must be a 16-digit number");
            }
            cardNum = cardNumber;
        }

        CardListPageResponse response = cardService.listCards(acctId, cardNum, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{cardNumber}")
    public ResponseEntity<CardDetailResponse> getCardDetail(
            @PathVariable String cardNumber) {
        if (!cardNumber.matches("\\d{16}")) {
            throw new CardDemoException(
                    "Card number must be a 16-digit number");
        }
        CardDetailResponse response = cardService.getCardDetail(cardNumber);
        return ResponseEntity.ok(response);
    }
}
