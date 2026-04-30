package com.bookstore.dto;

import com.bookstore.model.Order;
import com.bookstore.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OrderDto {
    private final Long id;
    private final OrderStatus status;
    private final BigDecimal totalPrice;
    private final LocalDateTime createdAt;
    private final Long customerId;
    private final String customerName;
    private final String deliveryAddress;
    private final List<OrderItemDto> items;

    public OrderDto(Order order) {
        this.id = order.getId();
        this.status = order.getStatus();
        this.totalPrice = order.getTotalPrice();
        this.createdAt = order.getCreatedAt();
        this.customerId = order.getUser() != null ? order.getUser().getId() : null;
        this.customerName = order.getUser() != null ? order.getUser().getName() : null;
        this.deliveryAddress = order.getDeliveryAddress();
        this.items = order.getItems() == null ? List.of()
                : order.getItems().stream().map(OrderItemDto::new).collect(Collectors.toList());
    }

    public Long getId() { return id; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public List<OrderItemDto> getItems() { return items; }
}
