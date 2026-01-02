package com.apex.core.dto;

/**
 * 售后统计DTO
 */
public class AfterSalesStatsDTO {
    private long totalCount;      // 总记录数

    public AfterSalesStatsDTO() {}

    // Getters and Setters
    public long getTotalCount() { return totalCount; }
    public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
}
