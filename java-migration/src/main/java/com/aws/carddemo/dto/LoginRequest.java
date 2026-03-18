package com.aws.carddemo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Please enter User ID ...")
    private String userId;

    @NotBlank(message = "Please enter Password ...")
    private String password;
}
