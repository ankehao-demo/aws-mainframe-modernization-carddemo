package com.aws.carddemo.controller;

import com.aws.carddemo.dto.CardUpdateRequest;
import com.aws.carddemo.entity.Card;
import com.aws.carddemo.service.CardService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public ResponseEntity<Page<Card>> listCards(
            @RequestParam(required = false) String acctId,
            Pageable pageable) {
        return ResponseEntity.ok(cardService.listCards(acctId, pageable));
    }

    @GetMapping("/{num}")
    public ResponseEntity<Card> getCard(@PathVariable("num") String cardNum) {
        return ResponseEntity.ok(cardService.getCard(cardNum));
    }

    @PutMapping("/{num}")
    public ResponseEntity<Card> updateCard(@PathVariable("num") String cardNum,
                                            @Valid @RequestBody CardUpdateRequest request) {
        return ResponseEntity.ok(cardService.updateCard(cardNum, request));
    }
}
