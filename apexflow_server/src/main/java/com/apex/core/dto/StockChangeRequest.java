package com.apex.core.dto;

/**
 * 库存变更请求DTO
 */
public class StockChangeRequest {
    private Integer productId;
    private Integer quantity;
    private String orderId; // 可选，关联订单
    private String reason; // 可选，调整原因

    // Getter和Setter
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
