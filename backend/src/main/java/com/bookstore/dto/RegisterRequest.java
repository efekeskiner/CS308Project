package com.bookstore.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be 100 characters or fewer")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid address")
    private String email;

    private String taxId;
    private String homeAddress;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
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