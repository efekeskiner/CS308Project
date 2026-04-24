package com.bookstore.model;

import javax.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String model;

    @Column(name = "serial_number", unique = true)
    private String serialNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "quantity_in_stock", nullable = false)
    private Integer quantityInStock = 0;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "discount_rate", precision = 5, scale = 2)
    private BigDecimal discountRate = BigDecimal.ZERO;

    @Column(name = "warranty_status")
    private String warrantyStatus;

    @Column(name = "distributor_info")
    private String distributorInfo;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

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
    public Category getCategory() { return category; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setName(String name) { this.name = name; }
    public void setModel(String model) { this.model = model; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public void setDescription(String description) { this.description = description; }
    public void setQuantityInStock(Integer quantityInStock) { this.quantityInStock = quantityInStock; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    public void setDiscountRate(BigDecimal discountRate) { this.discountRate = discountRate; }
    public void setWarrantyStatus(String warrantyStatus) { this.warrantyStatus = warrantyStatus; }
    public void setDistributorInfo(String distributorInfo) { this.distributorInfo = distributorInfo; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setCategory(Category category) { this.category = category; }
}
