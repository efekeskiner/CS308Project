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
 * so cost is estimated as {@code original_unit_price * quantity * costRatio} — half of
 * the ORIGINAL (list) price, not the discounted price paid — configurable via
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
                BigDecimal qty = BigDecimal.valueOf(item.getQuantity());
                BigDecimal lineRevenue = item.getUnitPrice().multiply(qty);
                // Cost is half of the ORIGINAL (list) price, not the discounted price the
                // customer paid — so a discount eats into profit rather than the cost basis.
                BigDecimal lineCost = item.getOriginalUnitPrice().multiply(qty)
                        .multiply(costRatio).setScale(2, RoundingMode.HALF_UP);
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
        BigDecimal totalRefundCostRecovery = BigDecimal.ZERO;
        Map<LocalDate, BigDecimal> refundByDay = new HashMap<>();
        Map<LocalDate, BigDecimal> refundCostRecoveryByDay = new HashMap<>();
        List<RefundRequest> refunds = refundRequestRepository
                .findApprovedByOrderCreatedAtBetween(RefundStatus.APPROVED, start, end);
        for (RefundRequest r : refunds) {
            OrderItem oi = r.getOrderItem();
            // When a refund is approved the product returns to stock, recovering the cost
            // that was originally booked for it — half of the ORIGINAL price, not the refund.
            BigDecimal costRecovery = oi.getOriginalUnitPrice()
                    .multiply(BigDecimal.valueOf(oi.getQuantity()))
                    .multiply(costRatio).setScale(2, RoundingMode.HALF_UP);
            totalRefunds = totalRefunds.add(r.getRefundAmount());
            totalRefundCostRecovery = totalRefundCostRecovery.add(costRecovery);
            LocalDate orderDate = oi.getOrder().getCreatedAt().toLocalDate();
            refundByDay.merge(orderDate, r.getRefundAmount(), BigDecimal::add);
            refundCostRecoveryByDay.merge(orderDate, costRecovery, BigDecimal::add);
        }

        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        List<RevenueDataPoint> points = new ArrayList<>();
        for (Map.Entry<LocalDate, BigDecimal[]> e : byDay.entrySet()) {
            BigDecimal dayRevenue = e.getValue()[0];
            BigDecimal dayCost = e.getValue()[1];
            BigDecimal dayRefund = refundByDay.getOrDefault(e.getKey(), BigDecimal.ZERO);
            BigDecimal netRevenue = dayRevenue.subtract(dayRefund);
            // Net profit impact of a refund = refundAmount − recovered cost (half of original price).
            BigDecimal refundCostRecovery = refundCostRecoveryByDay.getOrDefault(e.getKey(), BigDecimal.ZERO);
            BigDecimal netProfit = dayRevenue.subtract(dayCost).subtract(dayRefund).add(refundCostRecovery);
            points.add(new RevenueDataPoint(
                    e.getKey().format(fmt),
                    netRevenue.setScale(2, RoundingMode.HALF_UP),
                    netProfit.setScale(2, RoundingMode.HALF_UP)));
        }

        BigDecimal netTotalRevenue = totalRevenue.subtract(totalRefunds).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netTotalCost = totalCost.setScale(2, RoundingMode.HALF_UP);
        BigDecimal netProfit = totalRevenue.subtract(totalCost).subtract(totalRefunds).add(totalRefundCostRecovery).setScale(2, RoundingMode.HALF_UP);

        return new RevenueReport(startDate.format(fmt), endDate.format(fmt),
                netTotalRevenue, netTotalCost, netProfit, points);
    }
}
