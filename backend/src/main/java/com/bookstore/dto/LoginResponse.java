package com.bookstore.dto;

public class LoginResponse {
    private final String accessToken;
    private final String refreshToken;
    private final UserDto user;

    public LoginResponse(String accessToken, String refreshToken, UserDto user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public UserDto getUser() { return user; }
}
