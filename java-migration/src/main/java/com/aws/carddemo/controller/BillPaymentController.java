package com.aws.carddemo.controller;

import com.aws.carddemo.dto.BillPaymentRequest;
import com.aws.carddemo.dto.BillPaymentResponse;
import com.aws.carddemo.service.BillPaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bill-payment")
public class BillPaymentController {

    private final BillPaymentService billPaymentService;

    public BillPaymentController(BillPaymentService billPaymentService) {
        this.billPaymentService = billPaymentService;
    }

    @PostMapping
    public ResponseEntity<BillPaymentResponse> processBillPayment(@RequestBody BillPaymentRequest request) {
        return ResponseEntity.ok(billPaymentService.processBillPayment(request));
    }
}
