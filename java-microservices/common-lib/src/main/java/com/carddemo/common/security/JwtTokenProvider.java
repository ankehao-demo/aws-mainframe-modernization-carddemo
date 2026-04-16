package com.carddemo.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

/**
 * JWT utility class shared across all microservices.
 * Token claims carry: userId, userType, fromTranId, fromProgram —
 * mirroring the CARDDEMO-COMMAREA fields from COCOM01Y.cpy.
 */
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider(String secret, long expirationMs) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMs = expirationMs;
    }

    public String generateToken(String userId, String userType,
                                String fromTranId, String fromProgram) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userId)
                .claims(Map.of(
                        "userType", userType,
                        "fromTranId", fromTranId != null ? fromTranId : "",
                        "fromProgram", fromProgram != null ? fromProgram : ""
                ))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public String getUserId(String token) {
        return parseClaims(token).getSubject();
    }

    public String getUserType(String token) {
        return parseClaims(token).get("userType", String.class);
    }

    public String getFromTranId(String token) {
        return parseClaims(token).get("fromTranId", String.class);
    }

    public String getFromProgram(String token) {
        return parseClaims(token).get("fromProgram", String.class);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
