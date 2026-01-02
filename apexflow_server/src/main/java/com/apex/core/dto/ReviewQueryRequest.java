// ReviewQueryRequest.java - 查询评价请求DTO
package com.apex.core.dto;

/**
 * 查询评价请求DTO
 * 简洁查询参数
 */
public class ReviewQueryRequest {
    private Integer productId;  // 商品ID（可选）
    private Integer userId;     // 用户ID（可选）
    private Integer page = 1;   // 页码
    private Integer pageSize = 20; // 每页数量

    // Getters and Setters
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) {
        this.page = page != null && page > 0 ? page : 1;
    }

    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize != null && pageSize > 0 && pageSize <= 100 ? pageSize : 20;
    }
}