package com.carddemo.card.controller;

import com.carddemo.card.service.CardService;
import com.carddemo.common.dto.CardDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Ports COBOL programs:
 * - COCRDLIC.cbl (CCLI transaction) → listCards
 * - COCRDSLC.cbl (CCDL transaction) → getCard
 * - COCRDUPC.cbl (CCUP transaction) → updateCard
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Card Management", description = "Card operations — replaces COCRDLIC/COCRDSLC/COCRDUPC")
public class CardController {

    private final CardService cardService;

    @GetMapping("/accounts/{accountId}/cards")
    @Operation(summary = "List cards for account", description = "Get all cards for a given account")
    public ResponseEntity<List<CardDto>> listCards(@PathVariable Long accountId) {
        return ResponseEntity.ok(cardService.listCards(accountId));
    }

    @GetMapping("/cards/{cardNumber}")
    @Operation(summary = "Get card details", description = "Get card details by card number")
    public ResponseEntity<CardDto> getCard(@PathVariable String cardNumber) {
        return ResponseEntity.ok(cardService.getCard(cardNumber));
    }

    @PutMapping("/cards/{cardNumber}")
    @Operation(summary = "Update card", description = "Update card information")
    public ResponseEntity<CardDto> updateCard(@PathVariable String cardNumber,
                                              @RequestBody CardDto cardDto) {
        return ResponseEntity.ok(cardService.updateCard(cardNumber, cardDto));
    }
}
