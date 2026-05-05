package com.bookstore.model;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "content_approved", nullable = false)
    private Boolean contentApproved = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Review() {}

    public Review(User user, Product product, Integer score) {
        this.user = user;
        this.product = product;
        this.score = score;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Product getProduct() { return product; }
    public Integer getScore() { return score; }
    public String getContent() { return content; }
    public Boolean getContentApproved() { return contentApproved; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setScore(Integer score) { this.score = score; }
    public void setContent(String content) { this.content = content; }
    public void setContentApproved(Boolean contentApproved) { this.contentApproved = contentApproved; }
}
