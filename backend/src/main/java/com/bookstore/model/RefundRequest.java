package com.bookstore.model;

import javax.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refund_requests")
public class RefundRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RefundStatus status = RefundStatus.PENDING;

    @Column(name = "refund_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(length = 500)
    private String reason;

    @CreationTimestamp
    @Column(name = "requested_at", updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolver_id")
    private User resolver;

    public RefundRequest() {}

    public RefundRequest(OrderItem orderItem, User user, BigDecimal refundAmount, String reason) {
        this.orderItem = orderItem;
        this.user = user;
        this.refundAmount = refundAmount;
        this.reason = reason;
        this.status = RefundStatus.PENDING;
    }

    public Long getId() { return id; }
    public OrderItem getOrderItem() { return orderItem; }
    public User getUser() { return user; }
    public RefundStatus getStatus() { return status; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public String getReason() { return reason; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public User getResolver() { return resolver; }

    public void setOrderItem(OrderItem orderItem) { this.orderItem = orderItem; }
    public void setUser(User user) { this.user = user; }
    public void setStatus(RefundStatus status) { this.status = status; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    public void setReason(String reason) { this.reason = reason; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public void setResolver(User resolver) { this.resolver = resolver; }
}
