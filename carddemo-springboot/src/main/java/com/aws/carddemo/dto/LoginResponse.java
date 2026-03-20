package com.aws.carddemo.dto;

public record LoginResponse(
        String token,
        String userId,
        String userType,
        String firstName,
        String lastName
) {}
