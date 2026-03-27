package com.cardemo.controller;

import com.cardemo.dto.LoginRequest;
import com.cardemo.dto.LoginResponse;
import com.cardemo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication controller converting COSGN00C.cbl sign-on logic.
 *
 * Original COBOL program handles:
 *   - Receives user ID and password from BMS map COSGN0A
 *   - Reads USRSEC file by user ID key
 *   - Compares password (plain text in COBOL -> BCrypt in Java)
 *   - Routes admin users to COADM01C, regular users to COMEN01C
 *   - Sets COMMAREA fields (user ID, user type, program context)
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
