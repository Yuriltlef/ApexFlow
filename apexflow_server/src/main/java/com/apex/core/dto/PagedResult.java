// PagedResult.java (通用分页结果)
package com.apex.core.dto;

import java.util.List;

/**
 * 分页结果DTO
 */
public class PagedResult<T> {
    private List<T> data;
    private int currentPage;
    private int pageSize;
    private int totalCount;
    private int totalPages;

    // Getters and Setters
    public List<T> getData() { return data; }
    public void setData(List<T> data) { this.data = data; }

    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
        this.totalPages = (int) Math.ceil((double) totalCount / pageSize);
    }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}