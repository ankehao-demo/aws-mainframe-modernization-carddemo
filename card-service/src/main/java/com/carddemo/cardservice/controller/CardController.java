package com.carddemo.cardservice.controller;

import com.carddemo.cardservice.model.dto.CardDetailResponse;
import com.carddemo.cardservice.model.dto.CardListResponse;
import com.carddemo.cardservice.model.dto.CardUpdateRequest;
import com.carddemo.cardservice.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller replacing three CICS transactions:
 * - CCLI (COCRDLIC.cbl) -> GET /api/v1/cards
 * - CCDL (COCRDSLC.cbl) -> GET /api/v1/cards/{cardNum}
 * - CCUP (COCRDUPC.cbl) -> PUT /api/v1/cards/{cardNum}
 */
@RestController
@RequestMapping("/api/v1/cards")
@Tag(name = "Card Management", description = "Card CRUD operations replacing COBOL CICS transactions")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    @Operation(summary = "List cards with optional filters",
            description = "Replaces COCRDLIC.cbl (CCLI transaction). "
                    + "Supports filtering by accountId and/or cardNum prefix with pagination. "
                    + "Default page size is 7 (matching COBOL WS-MAX-SCREEN-LINES).")
    public ResponseEntity<CardListResponse> listCards(
            @Parameter(description = "Filter by account ID")
            @RequestParam(required = false) String accountId,
            @Parameter(description = "Filter by card number prefix")
            @RequestParam(required = false) String cardNum,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default 7)")
            @RequestParam(defaultValue = "7") int size) {
        return ResponseEntity.ok(cardService.listCards(accountId, cardNum, page, size));
    }

    @GetMapping("/{cardNum}")
    @Operation(summary = "Get card detail",
            description = "Replaces COCRDSLC.cbl (CCDL transaction). "
                    + "Returns full card details by card number.")
    public ResponseEntity<CardDetailResponse> getCard(
            @PathVariable String cardNum) {
        return ResponseEntity.ok(cardService.getCard(cardNum));
    }

    @PutMapping("/{cardNum}")
    @Operation(summary = "Update card",
            description = "Replaces COCRDUPC.cbl (CCUP transaction). "
                    + "Updates card fields with validation. Uses optimistic locking via version field.")
    public ResponseEntity<CardDetailResponse> updateCard(
            @PathVariable String cardNum,
            @Valid @RequestBody CardUpdateRequest request) {
        return ResponseEntity.ok(cardService.updateCard(cardNum, request));
    }
}
