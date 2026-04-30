package com.bookstore.dto;

import java.util.List;

public class PlaceOrderRequest {
    private List<OrderItemRequest> items;
    private String deliveryAddress;

    public List<OrderItemRequest> getItems() { return items; }
    public String getDeliveryAddress() { return deliveryAddress; }

    public void setItems(List<OrderItemRequest> items) { this.items = items; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
}
