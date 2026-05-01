package com.bookstore.dto;

import com.bookstore.model.Wishlist;
import java.math.BigDecimal;

public class WishlistDto {
    private final Long productId;
    private final String productName;
    private final BigDecimal price;
    private final String imageUrl;

    public WishlistDto(Wishlist wishlist) {
        this.productId = wishlist.getProduct().getId();
        this.productName = wishlist.getProduct().getName();
        this.price = wishlist.getProduct().getPrice();
        this.imageUrl = wishlist.getProduct().getImageUrl();
    }

    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public BigDecimal getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
}
