package com.aws.carddemo.service;

import com.aws.carddemo.config.JwtTokenProvider;
import com.aws.carddemo.dto.LoginRequest;
import com.aws.carddemo.dto.LoginResponse;
import com.aws.carddemo.entity.UserSecurity;
import com.aws.carddemo.repository.UserSecurityRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

/**
 * Authentication service — mirrors COSGN00C.cbl READ-USER-SEC-FILE logic.
 * Reads user_security table, validates password, returns JWT with user type.
 */
@Service
public class AuthService {

    private final UserSecurityRepository userSecurityRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserSecurityRepository userSecurityRepository, JwtTokenProvider jwtTokenProvider) {
        this.userSecurityRepository = userSecurityRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse login(LoginRequest request) {
        String userId = request.userId().toUpperCase().trim();
        String password = request.password().toUpperCase().trim();

        // COSGN00C line 221: EVALUATE WS-RESP-CD — WHEN 0 (found), WHEN 13 (not found)
        UserSecurity user = userSecurityRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("User not found. Try again ..."));

        // COSGN00C line 223: IF SEC-USR-PWD = WS-USER-PWD
        if (!user.getPassword().trim().equals(password)) {
            throw new BadCredentialsException("Wrong Password. Try again ...");
        }

        // COSGN00C lines 226-227: set user info and type in COMMAREA
        String token = jwtTokenProvider.generateToken(userId, user.getUserType());

        return new LoginResponse(
                token,
                userId,
                user.getUserType(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}
