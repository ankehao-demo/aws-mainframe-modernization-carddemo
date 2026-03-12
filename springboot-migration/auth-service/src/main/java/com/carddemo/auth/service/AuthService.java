package com.carddemo.auth.service;

import com.carddemo.auth.security.JwtTokenProvider;
import com.carddemo.common.dto.LoginRequest;
import com.carddemo.common.dto.LoginResponse;
import com.carddemo.common.entity.User;
import com.carddemo.common.exception.BusinessException;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Ports COBOL program COSGN00C.cbl (Sign-on processing).
 * Replaces CICS READ of USRSEC file with JPA repository lookup.
 * Replaces RACF authentication with Spring Security + JWT.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        String userId = request.getUserId().toUpperCase();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "userId", userId));

        if (!passwordEncoder.matches(request.getPassword().toUpperCase(), user.getPassword())) {
            throw new BusinessException("Wrong Password. Try again ...");
        }

        String token = tokenProvider.generateToken(user.getUserId(), user.getUserType());

        return LoginResponse.builder()
                .token(token)
                .userId(user.getUserId())
                .userType(user.getUserType())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
}
