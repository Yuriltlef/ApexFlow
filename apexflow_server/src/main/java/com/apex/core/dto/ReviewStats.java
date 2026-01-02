// ReviewStats.java - 评价统计DTO
package com.apex.core.dto;

/**
 * 商品评价统计DTO
 */
public class ReviewStats {
    private Double averageRating;      // 平均评分
    private RatingDistribution distribution; // 评分分布
    private Integer totalReviews;      // 总评价数

    // Getters and Setters
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public RatingDistribution getRatingDistribution() { return distribution; }
    public void setRatingDistribution(RatingDistribution distribution) { this.distribution = distribution; }

    public Integer getTotalReviews() { return totalReviews; }
    public void setTotalReviews(Integer totalReviews) { this.totalReviews = totalReviews; }
}