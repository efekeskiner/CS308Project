package com.bookstore.model;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "invoice_pdf_path", length = 500)
    private String pdfPath;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Invoice() {}

    public Invoice(Order order, String pdfPath) {
        this.order = order;
        this.pdfPath = pdfPath;
    }

    public Long getId() { return id; }
    public Order getOrder() { return order; }
    public String getPdfPath() { return pdfPath; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath; }
}
