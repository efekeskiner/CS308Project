package com.bookstore.dto;

import java.math.BigDecimal;

public class ProductRequest {
    private String name;
    private String model;
    private String serialNumber;
    private String description;
    private Integer quantityInStock;
    private BigDecimal price;
    private String warrantyStatus;
    private String distributorInfo;
    private String imageUrl;
    private Long categoryId;

    public String getName() { return name; }
    public String getModel() { return model; }
    public String getSerialNumber() { return serialNumber; }
    public String getDescription() { return description; }
    public Integer getQuantityInStock() { return quantityInStock; }
    public BigDecimal getPrice() { return price; }
    public String getWarrantyStatus() { return warrantyStatus; }
    public String getDistributorInfo() { return distributorInfo; }
    public String getImageUrl() { return imageUrl; }
    public Long getCategoryId() { return categoryId; }

    public void setName(String name) { this.name = name; }
    public void setModel(String model) { this.model = model; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public void setDescription(String description) { this.description = description; }
    public void setQuantityInStock(Integer quantityInStock) { this.quantityInStock = quantityInStock; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setWarrantyStatus(String warrantyStatus) { this.warrantyStatus = warrantyStatus; }
    public void setDistributorInfo(String distributorInfo) { this.distributorInfo = distributorInfo; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}
