package com.apex.core.dto;

/**
 * 更新售后状态请求DTO
 */
public class AfterSalesStatusUpdateRequest {
    private Integer status;        // 新状态
    private String remark;         // 处理备注

    public AfterSalesStatusUpdateRequest() {}

    // Getters and Setters
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
