package com.bookstore.dto;

import com.bookstore.model.Product;

import java.math.BigDecimal;

public class ProductDto {
    private final Long id;
    private final String name;
    private final String model;
    private final String serialNumber;
    private final String description;
    private final Integer quantityInStock;
    private final BigDecimal price;
    private final BigDecimal originalPrice;
    private final BigDecimal discountRate;
    private final String warrantyStatus;
    private final String distributorInfo;
    private final String imageUrl;
    private final Long categoryId;
    private final String categoryName;
    private final Double averageRating;
    private final Long ratingCount;
    private final boolean inStock;

    public ProductDto(Product p) {
        this(p, null, 0L);
    }

    public ProductDto(Product p, Double averageRating, Long ratingCount) {
        this.id = p.getId();
        this.name = p.getName();
        this.model = p.getModel();
        this.serialNumber = p.getSerialNumber();
        this.description = p.getDescription();
        this.quantityInStock = p.getQuantityInStock();
        this.price = p.getPrice();
        this.originalPrice = p.getOriginalPrice();
        this.discountRate = p.getDiscountRate();
        this.warrantyStatus = p.getWarrantyStatus();
        this.distributorInfo = p.getDistributorInfo();
        this.imageUrl = p.getImageUrl();
        this.categoryId = p.getCategory() != null ? p.getCategory().getId() : null;
        this.categoryName = p.getCategory() != null ? p.getCategory().getName() : null;
        this.averageRating = averageRating;
        this.ratingCount = ratingCount != null ? ratingCount : 0L;
        this.inStock = p.getQuantityInStock() != null && p.getQuantityInStock() > 0;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getModel() { return model; }
    public String getSerialNumber() { return serialNumber; }
    public String getDescription() { return description; }
    public Integer getQuantityInStock() { return quantityInStock; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public BigDecimal getDiscountRate() { return discountRate; }
    public String getWarrantyStatus() { return warrantyStatus; }
    public String getDistributorInfo() { return distributorInfo; }
    public String getImageUrl() { return imageUrl; }
    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public Double getAverageRating() { return averageRating; }
    public Long getRatingCount() { return ratingCount; }
    public boolean isInStock() { return inStock; }
}
