package com.bookstore.dto;

import com.bookstore.model.Invoice;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InvoiceDto {
    private final Long id;
    private final Long orderId;
    private final String customerName;
    private final String customerEmail;
    private final BigDecimal totalPrice;
    private final LocalDateTime createdAt;

    public InvoiceDto(Invoice invoice) {
        this.id = invoice.getId();
        this.orderId = invoice.getOrder().getId();
        this.customerName = invoice.getOrder().getUser() != null ? invoice.getOrder().getUser().getName() : null;
        this.customerEmail = invoice.getOrder().getUser() != null ? invoice.getOrder().getUser().getEmail() : null;
        this.totalPrice = invoice.getOrder().getTotalPrice();
        this.createdAt = invoice.getCreatedAt();
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
