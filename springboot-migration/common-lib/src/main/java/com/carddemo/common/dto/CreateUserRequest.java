package com.carddemo.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "User ID is required")
    @Size(max = 8, message = "User ID must be at most 8 characters")
    @JsonProperty("user_id")
    private String userId;

    @NotBlank(message = "Password is required")
    @Size(max = 8, message = "Password must be at most 8 characters")
    private String password;

    @Size(max = 20)
    @JsonProperty("first_name")
    private String firstName;

    @Size(max = 20)
    @JsonProperty("last_name")
    private String lastName;

    @NotBlank(message = "User type is required")
    @Pattern(regexp = "[AU]", message = "User type must be 'A' (admin) or 'U' (user)")
    @JsonProperty("user_type")
    private String userType;
}
