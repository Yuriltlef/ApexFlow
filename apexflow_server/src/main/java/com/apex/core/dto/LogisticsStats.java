package com.apex.core.dto;

/**
 * 物流统计信息
 */
public class LogisticsStats {
    private int pendingCount;
    private int shippedCount;
    private int deliveredCount;

    public int getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(int pendingCount) {
        this.pendingCount = pendingCount;
    }

    public int getShippedCount() {
        return shippedCount;
    }

    public void setShippedCount(int shippedCount) {
        this.shippedCount = shippedCount;
    }

    public int getDeliveredCount() {
        return deliveredCount;
    }

    public void setDeliveredCount(int deliveredCount) {
        this.deliveredCount = deliveredCount;
    }
}