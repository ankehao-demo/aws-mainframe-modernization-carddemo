package com.cardemo.service;

import com.cardemo.config.JwtTokenProvider;
import com.cardemo.dto.LoginRequest;
import com.cardemo.dto.LoginResponse;
import com.cardemo.entity.UserSecurity;
import com.cardemo.enums.UserType;
import com.cardemo.exception.CardDemoException;
import com.cardemo.repository.UserSecurityRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Authentication service converting COSGN00C.cbl logic.
 *
 * Original COBOL flow:
 *   1. READ USRSEC file by user ID (RIDFLD)
 *   2. Compare password (plain text in COBOL -> BCrypt in Java)
 *   3. Check user type: 'A' -> admin menu, 'U' -> user menu
 *   4. Set COMMAREA fields (user ID, user type, program context)
 *
 * Error conditions from COBOL:
 *   - RESP 0 + wrong password: "Wrong Password. Try again ..."
 *   - RESP 13 (NOTFND):        "User not found. Try again ..."
 *   - OTHER:                    "Unable to verify the User ..."
 */
@Service
public class AuthService {

    private final UserSecurityRepository userSecurityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserSecurityRepository userSecurityRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userSecurityRepository = userSecurityRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse login(LoginRequest request) {
        String userId = request.getUserId().toUpperCase();

        UserSecurity user = userSecurityRepository.findById(userId)
                .orElseThrow(() -> new CardDemoException("User not found. Try again ..."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CardDemoException("Wrong Password. Try again ...");
        }

        UserType userType = UserType.fromCode(user.getUserType());
        String token = jwtTokenProvider.generateToken(userId, user.getUserType());

        return new LoginResponse(token, userId, userType);
    }
}
