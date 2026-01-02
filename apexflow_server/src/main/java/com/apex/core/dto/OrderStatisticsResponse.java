package com.apex.core.dto;

import java.math.BigDecimal;

/**
 * 订单统计响应
 */
public class OrderStatisticsResponse {
    private Long totalOrders;
    private BigDecimal totalAmount;
    private Long pendingPayment;
    private Long paid;
    private Long shipped;
    private Long completed;
    private Long cancelled;

    public OrderStatisticsResponse() {}

    // Getters and Setters
    public Long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Long totalOrders) { this.totalOrders = totalOrders; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Long getPendingPayment() { return pendingPayment; }
    public void setPendingPayment(Long pendingPayment) { this.pendingPayment = pendingPayment; }

    public Long getPaid() { return paid; }
    public void setPaid(Long paid) { this.paid = paid; }

    public Long getShipped() { return shipped; }
    public void setShipped(Long shipped) { this.shipped = shipped; }

    public Long getCompleted() { return completed; }
    public void setCompleted(Long completed) { this.completed = completed; }

    public Long getCancelled() { return cancelled; }
    public void setCancelled(Long cancelled) { this.cancelled = cancelled; }
}
