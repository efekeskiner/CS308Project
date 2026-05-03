package com.bookstore.dto;

public class RegisterRequest {
    private String name;
    private String email;
    private String taxId;
    private String homeAddress;
    private String password;

    public String getName()        { return name; }
    public String getEmail()       { return email; }
    public String getTaxId()       { return taxId; }
    public String getHomeAddress() { return homeAddress; }
    public String getPassword()    { return password; }

    public void setName(String name)               { this.name = name; }
    public void setEmail(String email)             { this.email = email; }
    public void setTaxId(String taxId)             { this.taxId = taxId; }
    public void setHomeAddress(String homeAddress) { this.homeAddress = homeAddress; }
    public void setPassword(String password)       { this.password = password; }
}