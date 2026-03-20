package com.aws.carddemo.controller;

import com.aws.carddemo.dto.BillPaymentRequest;
import com.aws.carddemo.entity.Account;
import com.aws.carddemo.service.BillPaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bill-payments")
public class BillPaymentController {

    private final BillPaymentService billPaymentService;

    public BillPaymentController(BillPaymentService billPaymentService) {
        this.billPaymentService = billPaymentService;
    }

    @PostMapping
    public ResponseEntity<Account> processPayment(@Valid @RequestBody BillPaymentRequest request) {
        return ResponseEntity.ok(billPaymentService.processPayment(request.acctId(), request.amount()));
    }
}
