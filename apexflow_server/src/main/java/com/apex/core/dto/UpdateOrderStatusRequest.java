package com.apex.core.dto;

/**
 * 更新订单状态请求
 */
public class UpdateOrderStatusRequest {
    private Integer status;

    public UpdateOrderStatusRequest() {}

    // Getters and Setters
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}