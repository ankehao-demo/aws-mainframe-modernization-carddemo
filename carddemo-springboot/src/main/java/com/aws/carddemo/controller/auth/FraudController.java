package com.aws.carddemo.controller.auth;

import com.aws.carddemo.entity.auth.FraudRecord;
import com.aws.carddemo.service.auth.FraudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Fraud management controller — replaces COPAUS2C CICS program.
 * Allows marking authorizations as fraudulent and viewing fraud records.
 */
@RestController
@RequestMapping("/api/admin/fraud")
public class FraudController {

    private final FraudService fraudService;

    public FraudController(FraudService fraudService) {
        this.fraudService = fraudService;
    }

    @GetMapping
    public ResponseEntity<Page<FraudRecord>> listFraudRecords(
            @RequestParam(required = false) String cardNum,
            Pageable pageable) {
        if (cardNum != null && !cardNum.isBlank()) {
            return ResponseEntity.ok(fraudService.listFraudByCard(cardNum, pageable));
        }
        return ResponseEntity.ok(fraudService.listFraudRecords(pageable));
    }

    @PostMapping
    public ResponseEntity<FraudRecord> markAsFraud(
            @RequestParam String authId,
            @RequestParam String fraudType,
            @RequestParam(required = false, defaultValue = "") String description) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fraudService.markAsFraud(authId, fraudType, description));
    }
}
