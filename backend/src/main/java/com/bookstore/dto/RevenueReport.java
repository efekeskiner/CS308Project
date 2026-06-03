package com.bookstore.dto;

import java.math.BigDecimal;
import java.util.List;

public class RevenueReport {
    private final String startDate;
    private final String endDate;
    private final BigDecimal totalRevenue;
    private final BigDecimal totalCost;
    private final BigDecimal profit;
    private final List<RevenueDataPoint> dataPoints;

    public RevenueReport(String startDate, String endDate,
                         BigDecimal totalRevenue, BigDecimal totalCost,
                         BigDecimal profit, List<RevenueDataPoint> dataPoints) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalRevenue = totalRevenue;
        this.totalCost = totalCost;
        this.profit = profit;
        this.dataPoints = dataPoints;
    }

    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public BigDecimal getTotalCost() { return totalCost; }
    public BigDecimal getProfit() { return profit; }
    public List<RevenueDataPoint> getDataPoints() { return dataPoints; }
}
