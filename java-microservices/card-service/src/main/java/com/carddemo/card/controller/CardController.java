package com.carddemo.card.controller;

import com.carddemo.card.entity.Card;
import com.carddemo.card.service.CardService;
import com.carddemo.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
@Tag(name = "Card Management", description = "Card list/view/update - replaces COCRDLIC/COCRDSLC/COCRDUPC.cbl")
public class CardController {
    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    @Operation(summary = "List cards for account")
    public ResponseEntity<ApiResponse<List<Card>>> listCards(@RequestParam Long accountId) {
        return ResponseEntity.ok(ApiResponse.ok(cardService.getCardsByAccount(accountId)));
    }

    @GetMapping("/{cardNumber}")
    @Operation(summary = "View card details")
    public ResponseEntity<ApiResponse<Card>> getCard(@PathVariable String cardNumber) {
        return ResponseEntity.ok(ApiResponse.ok(cardService.getCard(cardNumber)));
    }

    @PutMapping("/{cardNumber}")
    @Operation(summary = "Update card")
    public ResponseEntity<ApiResponse<Card>> updateCard(@PathVariable String cardNumber, @RequestBody Card card) {
        return ResponseEntity.ok(ApiResponse.ok("Card updated", cardService.updateCard(cardNumber, card)));
    }
}
