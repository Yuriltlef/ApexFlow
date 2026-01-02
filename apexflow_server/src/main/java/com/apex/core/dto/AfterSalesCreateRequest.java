// AfterSalesCreateRequest.java
package com.apex.core.dto;

import java.math.BigDecimal;

/**
 * 创建售后申请请求DTO
 */
public class AfterSalesCreateRequest {
    private String orderId;        // 订单号
    private Integer type;          // 售后类型：1-退货，2-换货，3-维修
    private String reason;         // 申请原因
    private BigDecimal refundAmount; // 退款金额（退货类型需要）

    public AfterSalesCreateRequest() {}

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
}
