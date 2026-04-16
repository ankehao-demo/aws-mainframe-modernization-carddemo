package com.carddemo.auth.service;

import com.carddemo.auth.entity.User;
import com.carddemo.auth.repository.UserRepository;
import com.carddemo.common.dto.LoginRequest;
import com.carddemo.common.dto.LoginResponse;
import com.carddemo.common.exception.UnauthorizedException;
import com.carddemo.common.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findById(request.userId().toUpperCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid user ID or password"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid user ID or password");
        }
        String token = jwtTokenProvider.generateToken(user.getUserId(), user.getUserType(), "CC00", "COSGN00C");
        return new LoginResponse(token, user.getUserId(), user.getUserType(), user.getFirstName(), user.getLastName());
    }
}
