package com.bookstore.dto;

public class CreditCardRequest {
    private String number;
    private String expiry;
    private String cvv;

    public String getNumber() { return number; }
    public String getExpiry() { return expiry; }
    public String getCvv() { return cvv; }

    public void setNumber(String number) { this.number = number; }
    public void setExpiry(String expiry) { this.expiry = expiry; }
    public void setCvv(String cvv) { this.cvv = cvv; }
}
