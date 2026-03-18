package com.aws.carddemo.controller;

import com.aws.carddemo.dto.CardDto;
import com.aws.carddemo.dto.PagedResponse;
import com.aws.carddemo.service.CardService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<CardDto>> listCards(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(cardService.listCards(pageable));
    }

    @GetMapping("/{cardNum}")
    public ResponseEntity<CardDto> getCard(@PathVariable String cardNum) {
        return ResponseEntity.ok(cardService.getCard(cardNum));
    }

    @PutMapping("/{cardNum}")
    public ResponseEntity<CardDto> updateCard(@PathVariable String cardNum, @RequestBody CardDto cardDto) {
        return ResponseEntity.ok(cardService.updateCard(cardNum, cardDto));
    }

    @GetMapping("/by-account/{acctId}")
    public ResponseEntity<List<CardDto>> getCardsByAccount(@PathVariable String acctId) {
        return ResponseEntity.ok(cardService.getCardsByAccount(acctId));
    }
}
