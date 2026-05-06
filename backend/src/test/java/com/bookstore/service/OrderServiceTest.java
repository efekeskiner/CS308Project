package com.bookstore.service;

import com.bookstore.dto.OrderItemRequest;
import com.bookstore.dto.PlaceOrderRequest;
import com.bookstore.model.*;
import com.bookstore.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
  class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
        @Mock private ProductRepository productRepository;
        @Mock private DeliveryRepository deliveryRepository;
        @Mock private InvoiceRepository invoiceRepository;
        @Mock private MockPaymentService mockPaymentService;
        @Mock private InvoiceService invoiceService;
        @Mock private EmailService emailService;

    @InjectMocks
        private OrderService orderService;

    @Test
        void placeOrder_throwsWhenRequestIsNull() {
                  User user = new User();
                  assertThrows(IllegalArgumentException.class,
                                               () -> orderService.placeOrder(user, null));
        }

    @Test
        void placeOrder_throwsWhenItemsListIsEmpty() {
                  User user = new User();
                  PlaceOrderRequest req = new PlaceOrderRequest();
                  req.setItems(List.of());
                  assertThrows(IllegalArgumentException.class,
                                               () -> orderService.placeOrder(user, req));
        }

    @Test
        void placeOrder_throwsWhenProductNotFound() {
                  User user = new User();
                  OrderItemRequest item = new OrderItemRequest();
                  item.setProductId(42L);
                  item.setQuantity(1);

            PlaceOrderRequest req = new PlaceOrderRequest();
                  req.setItems(List.of(item));

            when(productRepository.findByIdForUpdate(42L)).thenReturn(Optional.empty());

            assertThrows(NoSuchElementException.class,
                                         () -> orderService.placeOrder(user, req));
        }

    @Test
        void placeOrder_throwsWhenInsufficientStock() {
                  User user = new User();

            Product product = new Product();
                  product.setId(1L);
                  product.setQuantityInStock(2);

            OrderItemRequest item = new OrderItemRequest();
                  item.setProductId(1L);
                  item.setQuantity(5);

            PlaceOrderRequest req = new PlaceOrderRequest();
                  req.setItems(List.of(item));

            when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));

            assertThrows(IllegalStateException.class,
                                         () -> orderService.placeOrder(user, req));
        }

    @Test
        void cancelOrder_throwsWhenOrderNotFound() {
                  when(orderRepository.findById(999L)).thenReturn(Optional.empty());
                  User user = new User();
                  assertThrows(NoSuchElementException.class,
                                               () -> orderService.cancelOrder(user, 999L));
        }

    @Test
        void cancelOrder_throwsWhenOrderIsNotProcessing() {
                  User user = new User();
                  user.setId(1L);
                  user.setRole(Role.CUSTOMER);

            Order order = new Order();
                  order.setUser(user);
                  order.setStatus(OrderStatus.IN_TRANSIT);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThrows(IllegalStateException.class,
                                         () -> orderService.cancelOrder(user, 10L));
        }

    @Test
        void cancelOrder_throwsSecurityExceptionForDifferentUser() {
                  User owner = new User();
                  owner.setId(1L);
                  owner.setRole(Role.CUSTOMER);

            User attacker = new User();
                  attacker.setId(2L);
                  attacker.setRole(Role.CUSTOMER);

            Order order = new Order();
                  order.setId(10L);
                  order.setUser(owner);
                  order.setStatus(OrderStatus.PROCESSING);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThrows(SecurityException.class,
                                         () -> orderService.cancelOrder(attacker, 10L));
        }

    @Test
        void getById_throwsWhenOrderNotFound() {
                  when(orderRepository.findById(77L)).thenReturn(Optional.empty());
                  User user = new User();
                  assertThrows(NoSuchElementException.class,
                                               () -> orderService.getById(user, 77L));
        }

    @Test
        void getById_throwsSecurityExceptionForWrongUser() {
                  User owner = new User();
                  owner.setId(1L);
                  owner.setRole(Role.CUSTOMER);

            User other = new User();
                  other.setId(2L);
                  other.setRole(Role.CUSTOMER);

            Order order = new Order();
                  order.setUser(owner);

            when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

            assertThrows(SecurityException.class,
                                         () -> orderService.getById(other, 5L));
        }

    @Test
        void getById_managerCanViewAnyOrder() {
                  User manager = new User();
                  manager.setId(99L);
                  manager.setRole(Role.PRODUCT_MANAGER);

            User owner = new User();
                  owner.setId(1L);

            Order order = new Order();
                  order.setId(5L);
                  order.setUser(owner);

            when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
                  when(invoiceRepository.findByOrderId(5L)).thenReturn(Optional.empty());

            var dto = orderService.getById(manager, 5L);
                  assertNotNull(dto);
        }
  }
