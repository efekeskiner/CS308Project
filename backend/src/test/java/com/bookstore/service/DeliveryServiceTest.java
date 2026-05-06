package com.bookstore.service;

import com.bookstore.model.Delivery;
import com.bookstore.model.Order;
import com.bookstore.model.OrderStatus;
import com.bookstore.repository.DeliveryRepository;
import com.bookstore.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
  class DeliveryServiceTest {

    @Mock
        private DeliveryRepository deliveryRepository;

    @Mock
        private OrderRepository orderRepository;

    @InjectMocks
        private DeliveryService deliveryService;

    @Test
        void updateStatus_throwsWhenNewStatusIsNull() {
                  assertThrows(IllegalArgumentException.class,
                                               () -> deliveryService.updateStatus(1L, null));
        }

    @Test
        void updateStatus_throwsWhenDeliveryNotFound() {
                  when(deliveryRepository.findById(999L)).thenReturn(Optional.empty());
                  assertThrows(NoSuchElementException.class,
                                               () -> deliveryService.updateStatus(999L, OrderStatus.IN_TRANSIT));
        }

    @Test
        void updateStatus_throwsWhenDeliveryHasNoAssociatedOrder() {
                  Delivery delivery = new Delivery();
                  delivery.setId(1L);
                  delivery.setOrder(null);
                  when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));

            assertThrows(IllegalStateException.class,
                                         () -> deliveryService.updateStatus(1L, OrderStatus.IN_TRANSIT));
        }

    @Test
        void updateStatus_throwsWhenOrderIsCancelled() {
                  Order order = new Order();
                  order.setStatus(OrderStatus.CANCELLED);

            Delivery delivery = new Delivery();
                  delivery.setId(1L);
                  delivery.setOrder(order);

            when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));

            assertThrows(IllegalStateException.class,
                                         () -> deliveryService.updateStatus(1L, OrderStatus.IN_TRANSIT));
        }

    @Test
        void updateStatus_throwsWhenOrderAlreadyDeliveredAndSettingInTransit() {
                  Order order = new Order();
                  order.setStatus(OrderStatus.DELIVERED);

            Delivery delivery = new Delivery();
                  delivery.setId(1L);
                  delivery.setOrder(order);

            when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));

            assertThrows(IllegalStateException.class,
                                         () -> deliveryService.updateStatus(1L, OrderStatus.IN_TRANSIT));
        }

    @Test
        void updateStatus_transitionsOrderFromProcessingToInTransit() {
                  Order order = new Order();
                  order.setStatus(OrderStatus.PROCESSING);
                  when(orderRepository.save(order)).thenReturn(order);

            Delivery delivery = new Delivery();
                  delivery.setId(1L);
                  delivery.setOrder(order);
                  when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));
                  when(deliveryRepository.save(delivery)).thenReturn(delivery);

            var dto = deliveryService.updateStatus(1L, OrderStatus.IN_TRANSIT);

            assertNotNull(dto);
                  assertEquals(OrderStatus.IN_TRANSIT, order.getStatus());
                  verify(orderRepository).save(order);
        }
  }
