package com.bookstore.service;

import com.bookstore.dto.DeliveryDto;
import com.bookstore.model.Delivery;
import com.bookstore.model.Order;
import com.bookstore.model.OrderStatus;
import com.bookstore.repository.DeliveryRepository;
import com.bookstore.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;

    public DeliveryService(DeliveryRepository deliveryRepository, OrderRepository orderRepository) {
        this.deliveryRepository = deliveryRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public Page<DeliveryDto> list(int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return deliveryRepository.findAll(pageable).map(DeliveryDto::new);
    }

    @Transactional
    public DeliveryDto updateStatus(Long deliveryId, OrderStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("status is required");
        }
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NoSuchElementException("Delivery " + deliveryId + " not found"));
        Order order = delivery.getOrder();
        if (order == null) {
            throw new IllegalStateException("Delivery " + deliveryId + " has no associated order");
        }
        assertValidTransition(order.getStatus(), newStatus);

        // propagate to order and all sibling deliveries for consistency
        order.setStatus(newStatus);
        orderRepository.save(order);

        List<Delivery> siblings = deliveryRepository.findByOrderId(order.getId());
        boolean completed = (newStatus == OrderStatus.DELIVERED);
        for (Delivery d : siblings) {
            d.setIsCompleted(completed);
            deliveryRepository.save(d);
        }

        // return the refreshed view of the specifically-addressed delivery
        return new DeliveryDto(deliveryRepository.findById(deliveryId).orElseThrow());
    }

    private void assertValidTransition(OrderStatus from, OrderStatus to) {
        if (from == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot change status of a cancelled order");
        }
        if (from == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Order is already delivered");
        }
        // allowed: PROCESSING -> IN_TRANSIT, IN_TRANSIT -> DELIVERED, PROCESSING -> DELIVERED (fast-path)
        boolean ok =
                (from == OrderStatus.PROCESSING && (to == OrderStatus.IN_TRANSIT || to == OrderStatus.DELIVERED))
             || (from == OrderStatus.IN_TRANSIT && to == OrderStatus.DELIVERED);
        if (!ok) {
            throw new IllegalStateException("Invalid status transition: " + from + " -> " + to);
        }
    }
}
