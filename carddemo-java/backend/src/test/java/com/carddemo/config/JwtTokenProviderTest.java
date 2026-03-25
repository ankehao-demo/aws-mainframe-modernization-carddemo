package com.carddemo.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                "testSecretKeyForJWTTokenGenerationAndValidationInTestEnvironment2024",
                86400000L);
    }

    @Test
    void shouldGenerateToken() {
        String token = jwtTokenProvider.generateToken("USER0001", "U");

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void shouldGetUserIdFromToken() {
        String token = jwtTokenProvider.generateToken("USER0001", "U");

        String userId = jwtTokenProvider.getUserIdFromToken(token);

        assertThat(userId).isEqualTo("USER0001");
    }

    @Test
    void shouldGetUserTypeFromToken() {
        String token = jwtTokenProvider.generateToken("ADMIN001", "A");

        String userType = jwtTokenProvider.getUserTypeFromToken(token);

        assertThat(userType).isEqualTo("A");
    }

    @Test
    void shouldValidateValidToken() {
        String token = jwtTokenProvider.generateToken("USER0001", "U");

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void shouldRejectExpiredToken() {
        JwtTokenProvider shortLived = new JwtTokenProvider(
                "testSecretKeyForJWTTokenGenerationAndValidationInTestEnvironment2024",
                -1000L); // Already expired

        String token = shortLived.generateToken("USER0001", "U");

        assertThat(jwtTokenProvider.validateToken(token)).isFalse();
    }

    @Test
    void shouldRejectTamperedToken() {
        String token = jwtTokenProvider.generateToken("USER0001", "U");
        String tampered = token + "tampered";

        assertThat(jwtTokenProvider.validateToken(tampered)).isFalse();
    }

    @Test
    void shouldRejectNullToken() {
        assertThat(jwtTokenProvider.validateToken(null)).isFalse();
    }

    @Test
    void shouldRejectEmptyToken() {
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
    }

    @Test
    void shouldRejectTokenFromDifferentSecret() {
        JwtTokenProvider otherProvider = new JwtTokenProvider(
                "aDifferentSecretKeyForJWTTokenWhichIsLongEnoughToWork12345678901234",
                86400000L);

        String token = otherProvider.generateToken("USER0001", "U");

        assertThat(jwtTokenProvider.validateToken(token)).isFalse();
    }

    @Test
    void shouldGenerateDifferentTokensForDifferentUsers() {
        String token1 = jwtTokenProvider.generateToken("USER0001", "U");
        String token2 = jwtTokenProvider.generateToken("USER0002", "A");

        assertThat(token1).isNotEqualTo(token2);
    }
}
