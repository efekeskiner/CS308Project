package com.bookstore.dto;

public class RefundRequestCreate {
    private Long orderItemId;
    private String reason;

    public RefundRequestCreate() {}

    public Long getOrderItemId() { return orderItemId; }
    public String getReason() { return reason; }

    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }
    public void setReason(String reason) { this.reason = reason; }
}
