package com.bookstore.service;

import com.bookstore.model.Order;
import com.bookstore.model.OrderItem;
import com.bookstore.model.Product;
import com.bookstore.model.User;
import com.bookstore.repository.OrderRepository;
import com.bookstore.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
  class AnalyticsServiceTest {

    @Mock
        private OrderRepository orderRepository;

    @Mock
        private ProductRepository productRepository;

    private AnalyticsService analyticsService;

    @BeforeEach
        void setUp() {
                  analyticsService = new AnalyticsService(orderRepository, productRepository, BigDecimal.valueOf(0.6));
        }

    private Order buildDeliveredOrder(long id, BigDecimal total) {
              User user = new User();
              ReflectionTestUtils.setField(user, "id", 1L);

            Product product = new Product();
              ReflectionTestUtils.setField(product, "id", 10L);
              product.setName("Book");
              product.setPrice(total);

            OrderItem item = new OrderItem();
              item.setProduct(product);
              item.setQuantity(1);
              item.setPrice(total);

            Order order = new Order();
              ReflectionTestUtils.setField(order, "id", id);
              order.setUser(user);
              order.setStatus("DELIVERED");
              order.setTotalPrice(total);
              order.setCreatedAt(LocalDateTime.now());
              order.setItems(List.of(item));
              return order;
    }

    @Test
        void getRevenueReport_returnsReport_withCorrectRevenue() {
                  Order order = buildDeliveredOrder(1L, BigDecimal.valueOf(100.00));
                  when(orderRepository.findByStatusAndCreatedAtBetween(
                                    eq("DELIVERED"), any(LocalDateTime.class), any(LocalDateTime.class)))
                                    .thenReturn(List.of(order));

            var report = analyticsService.getRevenueReport(
                              LocalDateTime.now().minusDays(7), LocalDateTime.now());

            assertNotNull(report);
                  assertEquals(0, BigDecimal.valueOf(100.00).compareTo(report.getTotalRevenue()));
        }

    @Test
        void getRevenueReport_calculatesProfit_usingCostRatio() {
                  Order order = buildDeliveredOrder(1L, BigDecimal.valueOf(200.00));
                  when(orderRepository.findByStatusAndCreatedAtBetween(
                                    eq("DELIVERED"), any(LocalDateTime.class), any(LocalDateTime.class)))
                                    .thenReturn(List.of(order));

            var report = analyticsService.getRevenueReport(
                              LocalDateTime.now().minusDays(7), LocalDateTime.now());

            // profit = revenue - cost = 200 - (200 * 0.6) = 80
            BigDecimal expectedProfit = BigDecimal.valueOf(80.00).setScale(2);
                  assertEquals(0, expectedProfit.compareTo(report.getTotalProfit().setScale(2)));
        }

    @Test
        void getRevenueReport_returnsZeroRevenue_whenNoOrders() {
                  when(orderRepository.findByStatusAndCreatedAtBetween(
                                    eq("DELIVERED"), any(LocalDateTime.class), any(LocalDateTime.class)))
                                    .thenReturn(List.of());

            var report = analyticsService.getRevenueReport(
                              LocalDateTime.now().minusDays(7), LocalDateTime.now());

            assertEquals(0, BigDecimal.ZERO.compareTo(report.getTotalRevenue()));
        }

    @Test
        void getRevenueReport_aggregatesMultipleOrders() {
                  Order o1 = buildDeliveredOrder(1L, BigDecimal.valueOf(50.00));
                  Order o2 = buildDeliveredOrder(2L, BigDecimal.valueOf(150.00));
                  when(orderRepository.findByStatusAndCreatedAtBetween(
                                    eq("DELIVERED"), any(LocalDateTime.class), any(LocalDateTime.class)))
                                    .thenReturn(List.of(o1, o2));

            var report = analyticsService.getRevenueReport(
                              LocalDateTime.now().minusDays(7), LocalDateTime.now());

            assertEquals(0, BigDecimal.valueOf(200.00).compareTo(report.getTotalRevenue()));
        }

    @Test
        void getRevenueReport_dataPointsMatchOrderCount() {
                  Order o1 = buildDeliveredOrder(1L, BigDecimal.valueOf(100.00));
                  Order o2 = buildDeliveredOrder(2L, BigDecimal.valueOf(100.00));
                  when(orderRepository.findByStatusAndCreatedAtBetween(
                                    eq("DELIVERED"), any(LocalDateTime.class), any(LocalDateTime.class)))
                                    .thenReturn(List.of(o1, o2));

            var report = analyticsService.getRevenueReport(
                              LocalDateTime.now().minusDays(7), LocalDateTime.now());

            assertNotNull(report.getDataPoints());
                  assertFalse(report.getDataPoints().isEmpty());
        }
  }
