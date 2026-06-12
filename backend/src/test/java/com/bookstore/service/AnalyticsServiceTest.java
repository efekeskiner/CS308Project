package com.bookstore.service;

import com.bookstore.dto.RevenueReport;
import com.bookstore.model.Order;
import com.bookstore.model.OrderItem;
import com.bookstore.model.OrderStatus;
import com.bookstore.model.Product;
import com.bookstore.repository.OrderRepository;
import com.bookstore.repository.RefundRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private RefundRequestRepository refundRequestRepository;

    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        // cost is modelled as revenue * 0.5
        analyticsService = new AnalyticsService(orderRepository, refundRequestRepository, BigDecimal.valueOf(0.5));
    }

    private Order deliveredOrder(long id, String unitPrice, int qty) {
        Product product = new Product();
        ReflectionTestUtils.setField(product, "id", id + 100);
        product.setName("Book " + id);

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(qty);
        item.setUnitPrice(new BigDecimal(unitPrice));

        Order order = new Order();
        ReflectionTestUtils.setField(order, "id", id);
        order.setStatus(OrderStatus.DELIVERED);
        ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.now().minusDays(1));
        order.setItems(new ArrayList<>(List.of(item)));
        return order;
    }

    private RevenueReport report() {
        return analyticsService.revenueReport(LocalDate.now().minusDays(7), LocalDate.now());
    }

    @Test
    void revenueReport_sumsRevenueOverOrders() {
        when(orderRepository.findByStatusNotAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of(deliveredOrder(1L, "100.00", 1)));
        when(refundRequestRepository.findApprovedByOrderCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of());

        RevenueReport r = report();

        assertEquals(0, new BigDecimal("100.00").compareTo(r.getTotalRevenue()));
    }

    @Test
    void revenueReport_computesCostAndProfit_fromCostRatio() {
        when(orderRepository.findByStatusNotAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of(deliveredOrder(1L, "200.00", 1)));
        when(refundRequestRepository.findApprovedByOrderCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of());

        RevenueReport r = report();

        // cost = 200 * 0.5 = 100 ; profit = 200 - 100 = 100
        assertEquals(0, new BigDecimal("100.00").compareTo(r.getTotalCost()));
        assertEquals(0, new BigDecimal("100.00").compareTo(r.getProfit()));
    }

    @Test
    void revenueReport_returnsZero_whenNoOrders() {
        when(orderRepository.findByStatusNotAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of());
        when(refundRequestRepository.findApprovedByOrderCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of());

        RevenueReport r = report();

        assertEquals(0, BigDecimal.ZERO.compareTo(r.getTotalRevenue()));
        assertEquals(0, BigDecimal.ZERO.compareTo(r.getProfit()));
    }

    @Test
    void revenueReport_aggregatesMultipleOrders() {
        when(orderRepository.findByStatusNotAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of(deliveredOrder(1L, "50.00", 1), deliveredOrder(2L, "150.00", 1)));
        when(refundRequestRepository.findApprovedByOrderCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of());

        RevenueReport r = report();

        assertEquals(0, new BigDecimal("200.00").compareTo(r.getTotalRevenue()));
    }

    @Test
    void revenueReport_zeroFillsDataPointsAcrossRange() {
        when(orderRepository.findByStatusNotAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of(deliveredOrder(1L, "100.00", 1)));
        when(refundRequestRepository.findApprovedByOrderCreatedAtBetween(any(), any(), any()))
                .thenReturn(List.of());

        RevenueReport r = report();

        // a 7-day span, inclusive of both ends, produces 8 daily buckets
        assertEquals(8, r.getDataPoints().size());
    }

    @Test
    void revenueReport_throws_whenEndBeforeStart() {
        assertThrows(IllegalArgumentException.class,
                () -> analyticsService.revenueReport(LocalDate.now(), LocalDate.now().minusDays(1)));
    }
}
