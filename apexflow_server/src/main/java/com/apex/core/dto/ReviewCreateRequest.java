// ReviewCreateRequest.java - 创建评价请求DTO
package com.apex.core.dto;

/**
 * 创建评价请求DTO
 * 简洁字段，仅包含必要信息
 */
public class ReviewCreateRequest {
    private String orderId;     // 订单号
    private Integer productId;  // 商品ID
    private Integer userId;     // 用户ID
    private Integer rating;     // 评分(1-5)
    private String content;     // 评价内容
    private String images;      // 图片URL（逗号分隔）
    private Boolean anonymous;  // 是否匿名

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public Boolean getAnonymous() { return anonymous; }
    public void setAnonymous(Boolean anonymous) { this.anonymous = anonymous; }
}