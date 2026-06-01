package com.bookstore.dto;

import com.bookstore.model.RefundRequest;
import com.bookstore.model.RefundStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RefundDto {
    private final Long id;
    private final Long orderItemId;
    private final Long orderId;
    private final String productName;
    private final Integer quantity;
    private final BigDecimal refundAmount;
    private final RefundStatus status;
    private final String reason;
    private final LocalDateTime requestedAt;
    private final LocalDateTime resolvedAt;
    private final String customerName;

    public RefundDto(RefundRequest r) {
        this.id = r.getId();
        this.orderItemId = r.getOrderItem() != null ? r.getOrderItem().getId() : null;
        this.orderId = (r.getOrderItem() != null && r.getOrderItem().getOrder() != null)
                ? r.getOrderItem().getOrder().getId() : null;
        this.productName = (r.getOrderItem() != null && r.getOrderItem().getProduct() != null)
                ? r.getOrderItem().getProduct().getName() : null;
        this.quantity = r.getOrderItem() != null ? r.getOrderItem().getQuantity() : null;
        this.refundAmount = r.getRefundAmount();
        this.status = r.getStatus();
        this.reason = r.getReason();
        this.requestedAt = r.getRequestedAt();
        this.resolvedAt = r.getResolvedAt();
        this.customerName = r.getUser() != null ? r.getUser().getName() : null;
    }

    public Long getId() { return id; }
    public Long getOrderItemId() { return orderItemId; }
    public Long getOrderId() { return orderId; }
    public String getProductName() { return productName; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public RefundStatus getStatus() { return status; }
    public String getReason() { return reason; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public String getCustomerName() { return customerName; }
}
