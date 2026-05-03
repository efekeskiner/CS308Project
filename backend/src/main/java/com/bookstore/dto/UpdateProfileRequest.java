package com.bookstore.dto;

public class UpdateProfileRequest {
    private String name;
    private String homeAddress;

    public String getName() {
        return name;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }
}