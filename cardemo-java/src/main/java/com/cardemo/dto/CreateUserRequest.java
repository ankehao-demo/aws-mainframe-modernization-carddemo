package com.cardemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new user. Field sizes match COBOL PIC clauses from CSUSR01Y.cpy.
 */
public class CreateUserRequest {

    @NotBlank(message = "User ID is required")
    @Size(max = 8, message = "User ID must be at most 8 characters")
    private String userId;

    @NotBlank(message = "Password is required")
    @Size(max = 8, message = "Password must be at most 8 characters")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 20, message = "First name must be at most 20 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 20, message = "Last name must be at most 20 characters")
    private String lastName;

    @NotBlank(message = "User type is required")
    @Pattern(regexp = "[AaUu]", message = "User type must be 'A' (admin) or 'U' (user)")
    private String userType;

    public CreateUserRequest() {
    }

    public CreateUserRequest(String userId, String password, String firstName,
                             String lastName, String userType) {
        this.userId = userId;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userType = userType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
