package com.cardemo.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating an existing user. All fields optional; only non-null fields are updated.
 * Field sizes match COBOL PIC clauses from CSUSR01Y.cpy.
 */
public class UpdateUserRequest {

    @Size(max = 8, message = "Password must be at most 8 characters")
    private String password;

    @Size(max = 20, message = "First name must be at most 20 characters")
    private String firstName;

    @Size(max = 20, message = "Last name must be at most 20 characters")
    private String lastName;

    @Pattern(regexp = "[AaUu]", message = "User type must be 'A' (admin) or 'U' (user)")
    private String userType;

    public UpdateUserRequest() {
    }

    public UpdateUserRequest(String password, String firstName, String lastName, String userType) {
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userType = userType;
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
