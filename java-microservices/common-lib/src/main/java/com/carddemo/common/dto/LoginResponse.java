package com.carddemo.common.dto;

public record LoginResponse(
        String token,
        String userId,
        String userType,
        String firstName,
        String lastName
) {}
