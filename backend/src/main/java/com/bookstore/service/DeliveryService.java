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

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot change status of a cancelled order");
        }

        if (newStatus == OrderStatus.IN_TRANSIT) {
            if (order.getStatus() == OrderStatus.DELIVERED) {
                throw new IllegalStateException("Order is already delivered");
            }
            // Advance order-level status; individual delivery isCompleted flags are unchanged
            if (order.getStatus() == OrderStatus.PROCESSING) {
                order.setStatus(OrderStatus.IN_TRANSIT);
                orderRepository.save(order);
            }
        } else if (newStatus == OrderStatus.DELIVERED) {
            if (Boolean.TRUE.equals(delivery.getIsCompleted())) {
                throw new IllegalStateException("Delivery " + deliveryId + " is already marked as delivered");
            }
            // Mark only this specific delivery as completed
            delivery.setIsCompleted(true);
            deliveryRepository.save(delivery);

            // Advance order from PROCESSING on first completed delivery
            if (order.getStatus() == OrderStatus.PROCESSING) {
                order.setStatus(OrderStatus.IN_TRANSIT);
            }
            // Only mark the order as DELIVERED when every delivery is complete
            List<Delivery> siblings = deliveryRepository.findByOrderId(order.getId());
            boolean allDelivered = siblings.stream().allMatch(d -> Boolean.TRUE.equals(d.getIsCompleted()));
            if (allDelivered) {
                order.setStatus(OrderStatus.DELIVERED);
            }
            orderRepository.save(order);
        } else {
            throw new IllegalArgumentException("Unsupported status: " + newStatus + ". Use IN_TRANSIT or DELIVERED.");
        }

        return new DeliveryDto(deliveryRepository.findById(deliveryId).orElseThrow());
    }
}
