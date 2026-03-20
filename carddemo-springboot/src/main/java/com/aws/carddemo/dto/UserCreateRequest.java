package com.aws.carddemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank @Size(max = 8) String userId,
        @NotBlank @Size(max = 20) String firstName,
        @NotBlank @Size(max = 20) String lastName,
        @NotBlank @Size(max = 8) String password,
        @NotBlank @Size(max = 1) String userType
) {}
