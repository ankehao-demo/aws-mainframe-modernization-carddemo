package com.carddemo.billing.controller;

import com.carddemo.billing.service.BillingService;
import com.carddemo.common.dto.BillPaymentRequest;
import com.carddemo.common.dto.TransactionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ports COBOL program COBIL00C.cbl (CBIL transaction).
 * Bill payment endpoint.
 */
@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
@Tag(name = "Billing", description = "Bill payment operations — replaces COBIL00C")
public class BillingController {

    private final BillingService billingService;

    @PostMapping("/payments")
    @Operation(summary = "Make payment", description = "Process a bill payment")
    public ResponseEntity<TransactionDto> makePayment(
            @Valid @RequestBody BillPaymentRequest request) {
        TransactionDto result = billingService.makePayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
