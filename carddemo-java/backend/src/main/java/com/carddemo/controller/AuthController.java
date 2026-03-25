package com.carddemo.controller;

import com.carddemo.service.AuthService;
import com.carddemo.service.AuthService.AuthResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        AuthResult result = authService.login(request.userId(), request.password());

        if (result.isSuccess()) {
            return ResponseEntity.ok(Map.of(
                    "token", result.getToken(),
                    "userType", result.getUserType(),
                    "userId", result.getUserId()
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", result.getErrorMessage()));
        }
    }

    public record LoginRequest(
            @NotBlank String userId,
            @NotBlank String password
    ) {
    }
}
