package com.aws.carddemo.service;

import com.aws.carddemo.dto.LoginRequest;
import com.aws.carddemo.dto.LoginResponse;
import com.aws.carddemo.entity.UserSecurity;
import com.aws.carddemo.exception.AuthenticationException;
import com.aws.carddemo.repository.UserSecurityRepository;
import com.aws.carddemo.security.JwtTokenProvider;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserSecurityRepository userSecurityRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserSecurityRepository userSecurityRepository, JwtTokenProvider jwtTokenProvider) {
        this.userSecurityRepository = userSecurityRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse login(LoginRequest request) {
        String userId = request.getUserId().toUpperCase().trim();
        String password = request.getPassword().toUpperCase().trim();

        UserSecurity user = userSecurityRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found. Try again ..."));

        String storedPassword = user.getUsrPwd().trim();
        if (!storedPassword.equals(password)) {
            throw new AuthenticationException("Wrong Password. Try again ...");
        }

        String userType = user.getUsrType().trim();
        String token = jwtTokenProvider.generateToken(userId, userType);

        return LoginResponse.builder()
                .token(token)
                .userType(userType.equals("A") ? "ADMIN" : "USER")
                .message("Login successful")
                .build();
    }
}
