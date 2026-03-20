package com.aws.carddemo.dto;

import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Size(max = 20) String firstName,
        @Size(max = 20) String lastName,
        @Size(max = 8) String password,
        @Size(max = 1) String userType
) {}
