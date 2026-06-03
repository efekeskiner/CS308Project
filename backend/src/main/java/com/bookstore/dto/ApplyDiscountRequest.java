package com.bookstore.dto;

import java.math.BigDecimal;
import java.util.List;

public class ApplyDiscountRequest {
    private List<Long> productIds;
    private BigDecimal discountRate;

    public List<Long> getProductIds() { return productIds; }
    public void setProductIds(List<Long> productIds) { this.productIds = productIds; }

    public BigDecimal getDiscountRate() { return discountRate; }
    public void setDiscountRate(BigDecimal discountRate) { this.discountRate = discountRate; }
}
