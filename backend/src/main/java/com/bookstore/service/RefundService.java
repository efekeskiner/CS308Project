package com.bookstore.service;

import com.bookstore.dto.RefundDto;
import com.bookstore.dto.RefundRequestCreate;
import com.bookstore.model.*;
import com.bookstore.repository.OrderItemRepository;
import com.bookstore.repository.ProductRepository;
import com.bookstore.repository.RefundRequestRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class RefundService {

    private static final long REFUND_WINDOW_DAYS = 30;

    private final RefundRequestRepository refundRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;

    public RefundService(RefundRequestRepository refundRepository,
                         OrderItemRepository orderItemRepository,
                         ProductRepository productRepository,
                         EmailService emailService) {
        this.refundRepository = refundRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.emailService = emailService;
    }

    @Transactional
    public RefundDto requestRefund(User caller, RefundRequestCreate req) {
        if (req == null || req.getOrderItemId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "orderItemId is required");
        }

        OrderItem orderItem = orderItemRepository.findById(req.getOrderItemId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Order item " + req.getOrderItemId() + " not found"));

        Order order = orderItem.getOrder();
        if (order == null || order.getUser() == null
                || !order.getUser().getId().equals(caller.getId())) {
            throw new SecurityException("You can only request refunds on your own orders");
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Refunds can only be requested for delivered orders");
        }

        LocalDateTime now = LocalDateTime.now();
        long daysSincePurchase = ChronoUnit.DAYS.between(order.getCreatedAt(), now);
        if (daysSincePurchase > REFUND_WINDOW_DAYS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Refund window has expired (limit: " + REFUND_WINDOW_DAYS + " days)");
        }

        if (refundRepository.existsByOrderItemIdAndStatusIn(
                orderItem.getId(), List.of(RefundStatus.PENDING, RefundStatus.APPROVED))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A refund request for this item already exists");
        }

        // refund amount uses the unit price stamped at order time, which already
        // reflects any discount applied at the time of purchase (Req 15)
        BigDecimal refundAmount = orderItem.getUnitPrice()
                .multiply(BigDecimal.valueOf(orderItem.getQuantity()));

        RefundRequest refund = new RefundRequest(orderItem, caller, refundAmount,
                req.getReason() == null || req.getReason().isBlank() ? null : req.getReason().trim());
        return new RefundDto(refundRepository.save(refund));
    }

    @Transactional(readOnly = true)
    public List<RefundDto> listForUser(User caller) {
        return refundRepository.findByUserIdOrderByRequestedAtDesc(caller.getId()).stream()
                .map(RefundDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RefundDto> listForManager(RefundStatus statusFilter) {
        List<RefundRequest> rows = (statusFilter == null)
                ? refundRepository.findAllByStatusOrderByRequestedAtDesc(RefundStatus.PENDING)
                : refundRepository.findAllByStatusOrderByRequestedAtDesc(statusFilter);
        return rows.stream().map(RefundDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RefundDto> listAllForManager() {
        return refundRepository.findAllByOrderByRequestedAtDesc().stream()
                .map(RefundDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public RefundDto approve(User resolver, Long refundId) {
        RefundRequest refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new NoSuchElementException("Refund " + refundId + " not found"));

        if (refund.getStatus() != RefundStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Refund is already " + refund.getStatus());
        }

        OrderItem item = refund.getOrderItem();
        Long productId = item.getProduct().getId();
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new NoSuchElementException("Product " + productId + " not found"));
        product.setQuantityInStock(product.getQuantityInStock() + item.getQuantity());
        productRepository.save(product);

        refund.setStatus(RefundStatus.APPROVED);
        refund.setResolvedAt(LocalDateTime.now());
        refund.setResolver(resolver);
        RefundDto dto = new RefundDto(refundRepository.save(refund));

        // Notify the customer that their refund was authorized (Req 15 / demo Step 6).
        emailService.sendRefundApprovedNotification(
                refund.getUser().getEmail(),
                refund.getUser().getName(),
                product.getName(),
                refund.getRefundAmount());

        return dto;
    }

    @Transactional
    public RefundDto reject(User resolver, Long refundId) {
        RefundRequest refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new NoSuchElementException("Refund " + refundId + " not found"));

        if (refund.getStatus() != RefundStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Refund is already " + refund.getStatus());
        }

        refund.setStatus(RefundStatus.REJECTED);
        refund.setResolvedAt(LocalDateTime.now());
        refund.setResolver(resolver);
        return new RefundDto(refundRepository.save(refund));
    }
}
