package com.bookstore.repository;

import com.bookstore.model.Order;
import com.bookstore.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Order> findByStatusNotAndCreatedAtBetween(OrderStatus status,
                                                   LocalDateTime start,
                                                   LocalDateTime end);

    @Query("SELECT COUNT(o) > 0 FROM Order o JOIN o.items i " +
           "WHERE o.user.id = :userId AND i.product.id = :productId AND o.status = 'DELIVERED'")
    boolean existsDeliveredOrderForUserAndProduct(@Param("userId") Long userId,
                                                  @Param("productId") Long productId);
}
