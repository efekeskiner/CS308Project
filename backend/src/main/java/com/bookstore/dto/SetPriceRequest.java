package com.bookstore.dto;

import java.math.BigDecimal;

public class SetPriceRequest {
    private BigDecimal price;

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
