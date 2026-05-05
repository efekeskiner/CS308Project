package com.bookstore.dto;

import com.bookstore.model.Review;

import java.time.LocalDateTime;

public class CommentDto {
    private final Long id;
    private final Long userId;
    private final String userName;
    private final Long productId;
    private final String productName;
    private final Integer score;
    private final String content;
    private final Boolean approved;
    private final LocalDateTime createdAt;

    public CommentDto(Review r) {
        this.id = r.getId();
        this.userId = r.getUser().getId();
        this.userName = r.getUser().getName();
        this.productId = r.getProduct().getId();
        this.productName = r.getProduct().getName();
        this.score = r.getScore();
        this.content = r.getContent();
        this.approved = r.getContentApproved();
        this.createdAt = r.getCreatedAt();
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public Integer getScore() { return score; }
    public String getContent() { return content; }
    public Boolean getApproved() { return approved; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
