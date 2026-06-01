package com.bookstore.repository;

import com.bookstore.model.RefundRequest;
import com.bookstore.model.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {

    List<RefundRequest> findAllByStatusOrderByRequestedAtDesc(RefundStatus status);

    List<RefundRequest> findAllByOrderByRequestedAtDesc();

    List<RefundRequest> findByUserIdOrderByRequestedAtDesc(Long userId);

    boolean existsByOrderItemIdAndStatusIn(Long orderItemId, Collection<RefundStatus> statuses);

    Optional<RefundRequest> findByOrderItemId(Long orderItemId);
}
