package com.cardemo.dto;

import com.cardemo.enums.UserType;

public class LoginResponse {

    private String token;
    private String userId;
    private UserType userType;

    public LoginResponse() {
    }

    public LoginResponse(String token, String userId, UserType userType) {
        this.token = token;
        this.userId = userId;
        this.userType = userType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }
}
