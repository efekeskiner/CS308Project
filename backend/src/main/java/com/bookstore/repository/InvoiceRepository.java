package com.bookstore.repository;

import com.bookstore.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByOrderId(Long orderId);
    List<Invoice> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
