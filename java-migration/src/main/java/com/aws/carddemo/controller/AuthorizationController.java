package com.aws.carddemo.controller;

import com.aws.carddemo.dto.AuthorizationRequest;
import com.aws.carddemo.dto.AuthorizationResponse;
import com.aws.carddemo.service.AuthorizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authorization")
public class AuthorizationController {

    private final AuthorizationService authorizationService;

    public AuthorizationController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @PostMapping
    public ResponseEntity<AuthorizationResponse> processAuthorization(
            @RequestBody AuthorizationRequest request) {
        return ResponseEntity.ok(authorizationService.processAuthorization(request));
    }

    @PostMapping("/fraud")
    public ResponseEntity<Void> markFraud(@RequestBody AuthorizationRequest request) {
        authorizationService.markFraud(request.getCardNum(), request.getTranId(), request.getMerchantId());
        return ResponseEntity.ok().build();
    }
}
