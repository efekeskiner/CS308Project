package com.bookstore.dto;

public class RegisterResponse {
      private final String message;
      private final UserDto user;

    public RegisterResponse(String message, UserDto user) {
              this.message = message;
              this.user    = user;
    }

    public String  getMessage() { return message; }
      public UserDto getUser()    { return user; }
}
