package com.apex.core.dto;

/**
 * 库存调整请求DTO
 */
public class StockAdjustRequest {
    private Integer productId;
    private Integer newStock;
    private String reason;

    // Getter和Setter
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public Integer getNewStock() { return newStock; }
    public void setNewStock(Integer newStock) { this.newStock = newStock; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
