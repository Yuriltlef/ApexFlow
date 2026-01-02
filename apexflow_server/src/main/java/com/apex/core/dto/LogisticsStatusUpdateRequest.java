package com.apex.core.dto;

import javax.validation.constraints.NotBlank;

/**
 * 物流状态更新请求DTO
 */
public class LogisticsStatusUpdateRequest {
    @NotBlank(message = "状态不能为空")
    private String status;

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}