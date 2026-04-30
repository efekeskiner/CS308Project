package com.bookstore.dto;

import com.bookstore.model.OrderItem;

import java.math.BigDecimal;

public class OrderItemDto {
    private final Long id;
    private final Long productId;
    private final String productName;
    private final Integer quantity;
    private final BigDecimal unitPrice;

    public OrderItemDto(OrderItem item) {
        this.id = item.getId();
        this.productId = item.getProduct() != null ? item.getProduct().getId() : null;
        this.productName = item.getProduct() != null ? item.getProduct().getName() : null;
        this.quantity = item.getQuantity();
        this.unitPrice = item.getUnitPrice();
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
}
