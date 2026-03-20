package com.aws.carddemo.controller.auth;

import com.aws.carddemo.entity.auth.PendingAuthSummary;
import com.aws.carddemo.repository.auth.PendingAuthSummaryRepository;
import com.aws.carddemo.service.auth.AuthorizationProcessingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;

/**
 * Pending authorization controller — replaces COPAUS0C/COPAUS1C CICS programs.
 * Provides summary/detail views and authorization request processing.
 */
@RestController
@RequestMapping("/api/authorizations")
public class PendingAuthController {

    private final PendingAuthSummaryRepository summaryRepository;
    private final AuthorizationProcessingService authorizationService;

    public PendingAuthController(PendingAuthSummaryRepository summaryRepository,
                                  AuthorizationProcessingService authorizationService) {
        this.summaryRepository = summaryRepository;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    public ResponseEntity<Page<PendingAuthSummary>> listAuthorizations(
            @RequestParam(required = false) String cardNum,
            @RequestParam(required = false) String acctId,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        if (cardNum != null && !cardNum.isBlank()) {
            return ResponseEntity.ok(summaryRepository.findByCardNum(cardNum, pageable));
        }
        if (acctId != null && !acctId.isBlank()) {
            return ResponseEntity.ok(summaryRepository.findByAcctId(acctId, pageable));
        }
        if (status != null && !status.isBlank()) {
            return ResponseEntity.ok(summaryRepository.findByAuthStatus(status, pageable));
        }
        return ResponseEntity.ok(summaryRepository.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PendingAuthSummary> getAuthorization(@PathVariable("id") String authId) {
        return summaryRepository.findById(authId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PendingAuthSummary> processAuthorization(
            @RequestParam String cardNum,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false, defaultValue = "000000000") String merchantId,
            @RequestParam(required = false, defaultValue = "") String merchantName) {
        PendingAuthSummary result = authorizationService.processAuthorization(
                cardNum, amount, merchantId, merchantName);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
