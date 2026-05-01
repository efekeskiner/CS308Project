package com.bookstore.dto;

import com.bookstore.model.Delivery;
import com.bookstore.model.OrderStatus;

import java.math.BigDecimal;

public class DeliveryDto {
    private final Long id;
    private final Long customerId;
    private final String customerName;
    private final Long productId;
    private final String productName;
    private final Integer quantity;
    private final BigDecimal totalPrice;
    private final String deliveryAddress;
    private final boolean isCompleted;
    private final Long orderId;
    private final OrderStatus orderStatus;

    public DeliveryDto(Delivery d) {
        this.id = d.getId();
        this.customerId = d.getCustomer() != null ? d.getCustomer().getId() : null;
        this.customerName = d.getCustomer() != null ? d.getCustomer().getName() : null;
        this.productId = d.getProduct() != null ? d.getProduct().getId() : null;
        this.productName = d.getProduct() != null ? d.getProduct().getName() : null;
        this.quantity = d.getQuantity();
        this.totalPrice = d.getTotalPrice();
        this.deliveryAddress = d.getDeliveryAddress();
        this.isCompleted = Boolean.TRUE.equals(d.getIsCompleted());
        this.orderId = d.getOrder() != null ? d.getOrder().getId() : null;
        this.orderStatus = d.getOrder() != null ? d.getOrder().getStatus() : null;
    }

    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public boolean isCompleted() { return isCompleted; }
    public Long getOrderId() { return orderId; }
    public OrderStatus getOrderStatus() { return orderStatus; }
}
