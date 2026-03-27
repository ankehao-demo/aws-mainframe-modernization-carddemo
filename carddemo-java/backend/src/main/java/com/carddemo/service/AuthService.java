package com.carddemo.service;

import com.carddemo.config.JwtTokenProvider;
import com.carddemo.model.User;
import com.carddemo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Authenticate a user following the COBOL COSGN00C logic:
     * 1. Uppercase the userId
     * 2. Look up user in the security file
     * 3. Compare password
     * 4. Return JWT with userId and userType
     */
    public AuthResult login(String userId, String password) {
        if (userId == null || userId.trim().isEmpty()) {
            return AuthResult.failure("Please enter User ID ...");
        }
        if (password == null || password.trim().isEmpty()) {
            return AuthResult.failure("Please enter Password ...");
        }

        String upperUserId = userId.toUpperCase().trim();
        String upperPassword = password.toUpperCase().trim();

        Optional<User> optUser = userRepository.findById(upperUserId);
        if (optUser.isEmpty()) {
            return AuthResult.failure("User not found. Try again ...");
        }

        User user = optUser.get();
        if (!user.getPassword().equals(upperPassword)) {
            return AuthResult.failure("Wrong Password. Try again ...");
        }

        String token = jwtTokenProvider.generateToken(user.getUsrId(), user.getUserType());
        return AuthResult.success(token, user.getUserType(), user.getUsrId());
    }

    public static class AuthResult {
        private final boolean success;
        private final String token;
        private final String userType;
        private final String userId;
        private final String errorMessage;

        private AuthResult(boolean success, String token, String userType, String userId, String errorMessage) {
            this.success = success;
            this.token = token;
            this.userType = userType;
            this.userId = userId;
            this.errorMessage = errorMessage;
        }

        public static AuthResult success(String token, String userType, String userId) {
            return new AuthResult(true, token, userType, userId, null);
        }

        public static AuthResult failure(String errorMessage) {
            return new AuthResult(false, null, null, null, errorMessage);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getToken() {
            return token;
        }

        public String getUserType() {
            return userType;
        }

        public String getUserId() {
            return userId;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
