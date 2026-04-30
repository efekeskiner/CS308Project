package com.bookstore.dto;

import com.bookstore.model.OrderStatus;

public class DeliveryStatusRequest {
    private OrderStatus status;

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}
