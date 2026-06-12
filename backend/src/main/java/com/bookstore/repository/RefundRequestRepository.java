package com.bookstore.repository;

import com.bookstore.model.RefundRequest;
import com.bookstore.model.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {

    List<RefundRequest> findAllByStatusOrderByRequestedAtDesc(RefundStatus status);

    /** Refunds where the original order was placed in the given window (for analytics bucketing by order date). */
    @Query("SELECT r FROM RefundRequest r JOIN FETCH r.orderItem oi JOIN FETCH oi.order o WHERE r.status = :status AND o.createdAt BETWEEN :start AND :end")
    List<RefundRequest> findApprovedByOrderCreatedAtBetween(@Param("status") RefundStatus status,
                                                            @Param("start") LocalDateTime start,
                                                            @Param("end") LocalDateTime end);

    List<RefundRequest> findAllByOrderByRequestedAtDesc();

    List<RefundRequest> findByUserIdOrderByRequestedAtDesc(Long userId);

    boolean existsByOrderItemIdAndStatusIn(Long orderItemId, Collection<RefundStatus> statuses);

    Optional<RefundRequest> findByOrderItemId(Long orderItemId);
}
