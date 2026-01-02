package com.apex.core.dto;

import java.time.LocalDateTime; /**
 * 库存日志响应DTO
 */
public class InventoryLogResponse {
    private Integer id;
    private Integer productId;
    private String changeType;
    private Integer quantity;
    private Integer beforeStock;
    private Integer afterStock;
    private String orderId;
    private LocalDateTime createdAt;

    // Getter和Setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getBeforeStock() { return beforeStock; }
    public void setBeforeStock(Integer beforeStock) { this.beforeStock = beforeStock; }

    public Integer getAfterStock() { return afterStock; }
    public void setAfterStock(Integer afterStock) { this.afterStock = afterStock; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
