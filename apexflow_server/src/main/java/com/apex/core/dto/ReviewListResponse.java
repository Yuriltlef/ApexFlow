// ReviewListResponse.java - 评价列表响应DTO
package com.apex.core.dto;

import com.apex.core.model.Review;
import java.util.List;

/**
 * 评价列表响应DTO
 */
public class ReviewListResponse {
    private List<Review> reviews; // 评价列表
    private Integer total;        // 总记录数
    private Integer page;         // 当前页码
    private Integer pageSize;     // 每页数量

    // Getters and Setters
    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }

    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
}