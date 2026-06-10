package com.bookstore.service;

import com.bookstore.model.Order;
import com.bookstore.model.OrderItem;
import com.bookstore.model.Product;
import com.bookstore.model.User;
import com.bookstore.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
  class InvoiceServiceTest {

    @Mock
        private OrderRepository orderRepository;

    @InjectMocks
        private InvoiceService invoiceService;

    private Order buildOrder(long orderId, long userId, String status) {
              User user = new User();
              ReflectionTestUtils.setField(user, "id", userId);
              user.setEmail("customer@test.com");
              user.setName("Test User");

            Product product = new Product();
              ReflectionTestUtils.setField(product, "id", 10L);
              product.setName("Test Book");
              product.setPrice(BigDecimal.valueOf(29.99));

            OrderItem item = new OrderItem();
              ReflectionTestUtils.setField(item, "id", 100L);
              item.setProduct(product);
              item.setQuantity(2);
              item.setPrice(BigDecimal.valueOf(29.99));

            Order order = new Order();
              ReflectionTestUtils.setField(order, "id", orderId);
              order.setUser(user);
              order.setStatus(status);
              order.setCreatedAt(LocalDateTime.now());
              order.setItems(List.of(item));
              order.setTotalPrice(BigDecimal.valueOf(59.98));
              return order;
    }

    @Test
        void generateInvoice_returnsInvoiceBytes_forDeliveredOrder() {
                  Order order = buildOrder(1L, 1L, "DELIVERED");
                  when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            byte[] pdf = invoiceService.generateInvoice(1L, 1L);

            assertNotNull(pdf);
                  assertTrue(pdf.length > 0);
        }

    @Test
        void generateInvoice_throwsException_whenOrderNotFound() {
                  when(orderRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class,
                                         () -> invoiceService.generateInvoice(99L, 1L));
        }

    @Test
        void generateInvoice_throwsException_whenUserDoesNotOwnOrder() {
                  Order order = buildOrder(1L, 1L, "DELIVERED");
                  when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThrows(RuntimeException.class,
                                         () -> invoiceService.generateInvoice(1L, 999L));
        }

    @Test
        void generateInvoice_containsPdfHeader_whenSuccessful() {
                  Order order = buildOrder(2L, 2L, "DELIVERED");
                  when(orderRepository.findById(2L)).thenReturn(Optional.of(order));

            byte[] pdf = invoiceService.generateInvoice(2L, 2L);

            // PDF files start with the %PDF magic bytes
            assertEquals('%', (char) pdf[0]);
                  assertEquals('P', (char) pdf[1]);
                  assertEquals('D', (char) pdf[2]);
                  assertEquals('F', (char) pdf[3]);
        }
  }
