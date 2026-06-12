package com.bookstore.dto;

import java.math.BigDecimal;

public class RevenueDataPoint {
    private final String date;       // ISO yyyy-MM-dd
    private final BigDecimal revenue;
    private final BigDecimal profit;

    public RevenueDataPoint(String date, BigDecimal revenue, BigDecimal profit) {
        this.date = date;
        this.revenue = revenue;
        this.profit = profit;
    }

    public String getDate() { return date; }
    public BigDecimal getRevenue() { return revenue; }
    public BigDecimal getProfit() { return profit; }
}
