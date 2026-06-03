package com.bookstore.service;

import com.bookstore.dto.RevenueDataPoint;
import com.bookstore.dto.RevenueReport;
import com.bookstore.model.Order;
import com.bookstore.model.OrderItem;
import com.bookstore.model.OrderStatus;
import com.bookstore.model.RefundRequest;
import com.bookstore.model.RefundStatus;
import com.bookstore.repository.OrderRepository;
import com.bookstore.repository.RefundRequestRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Revenue / profit analytics for the sales manager (Req 11).
 *
 * Revenue is the sum of {@code order_items.unit_price * quantity} over non-CANCELLED
 * orders created in the requested range. The schema has no per-product cost column,
 * so cost is estimated as {@code revenue * costRatio}, configurable via
 * {@code app.analytics.cost-ratio} (default 0.5). APPROVED refunds subtract from
 * revenue and profit on the day the original order was placed (not the resolution date).
 * Daily data points are zero-filled across the whole range so the chart has a bar for every day.
 */
@Service
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final RefundRequestRepository refundRequestRepository;
    private final BigDecimal costRatio;

    public AnalyticsService(OrderRepository orderRepository,
                            RefundRequestRepository refundRequestRepository,
                            @Value("${app.analytics.cost-ratio:0.5}") BigDecimal costRatio) {
        this.orderRepository = orderRepository;
        this.refundRequestRepository = refundRequestRepository;
        this.costRatio = costRatio;
    }

    @Transactional(readOnly = true)
    public RevenueReport revenueReport(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("startDate and endDate are required");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must not be before startDate");
        }
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        // zero-filled day buckets: [revenue, cost]
        Map<LocalDate, BigDecimal[]> byDay = new LinkedHashMap<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            byDay.put(d, new BigDecimal[]{ BigDecimal.ZERO, BigDecimal.ZERO });
        }

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        List<Order> orders = orderRepository.findByStatusNotAndCreatedAtBetween(
                OrderStatus.CANCELLED, start, end);
        for (Order o : orders) {
            BigDecimal[] bucket = byDay.get(o.getCreatedAt().toLocalDate());
            for (OrderItem item : o.getItems()) {
                BigDecimal lineRevenue = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                BigDecimal lineCost = lineRevenue.multiply(costRatio).setScale(2, RoundingMode.HALF_UP);
                totalRevenue = totalRevenue.add(lineRevenue);
                totalCost = totalCost.add(lineCost);
                if (bucket != null) {
                    bucket[0] = bucket[0].add(lineRevenue);
                    bucket[1] = bucket[1].add(lineCost);
                }
            }
        }

        // APPROVED refunds reduce revenue & profit on the day the original order was placed.
        BigDecimal totalRefunds = BigDecimal.ZERO;
        Map<LocalDate, BigDecimal> refundByDay = new HashMap<>();
        List<RefundRequest> refunds = refundRequestRepository
                .findApprovedByOrderCreatedAtBetween(RefundStatus.APPROVED, start, end);
        for (RefundRequest r : refunds) {
            totalRefunds = totalRefunds.add(r.getRefundAmount());
            LocalDate orderDate = r.getOrderItem().getOrder().getCreatedAt().toLocalDate();
            refundByDay.merge(orderDate, r.getRefundAmount(), BigDecimal::add);
        }

        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        List<RevenueDataPoint> points = new ArrayList<>();
        for (Map.Entry<LocalDate, BigDecimal[]> e : byDay.entrySet()) {
            BigDecimal dayRevenue = e.getValue()[0];
            BigDecimal dayCost = e.getValue()[1];
            BigDecimal dayRefund = refundByDay.getOrDefault(e.getKey(), BigDecimal.ZERO);
            BigDecimal netRevenue = dayRevenue.subtract(dayRefund);
            // When a refund is approved the product returns to stock, recovering its cost.
            // Net profit impact of refund = refundAmount × (1 − costRatio), not the full amount.
            BigDecimal refundCostRecovery = dayRefund.multiply(costRatio).setScale(2, RoundingMode.HALF_UP);
            BigDecimal netProfit = dayRevenue.subtract(dayCost).subtract(dayRefund).add(refundCostRecovery);
            points.add(new RevenueDataPoint(
                    e.getKey().format(fmt),
                    netRevenue.setScale(2, RoundingMode.HALF_UP),
                    netProfit.setScale(2, RoundingMode.HALF_UP)));
        }

        BigDecimal netTotalRevenue = totalRevenue.subtract(totalRefunds).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netTotalCost = totalCost.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalRefundCostRecovery = totalRefunds.multiply(costRatio).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netProfit = totalRevenue.subtract(totalCost).subtract(totalRefunds).add(totalRefundCostRecovery).setScale(2, RoundingMode.HALF_UP);

        return new RevenueReport(startDate.format(fmt), endDate.format(fmt),
                netTotalRevenue, netTotalCost, netProfit, points);
    }
}
