package com.apex.core.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建订单请求
 */
public class CreateOrderRequest {
    private Integer userId;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private Integer addressId;
    private List<OrderItemRequest> orderItems;

    public CreateOrderRequest() {}

    // Getters and Setters
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public Integer getAddressId() { return addressId; }
    public void setAddressId(Integer addressId) { this.addressId = addressId; }

    public List<OrderItemRequest> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemRequest> orderItems) { this.orderItems = orderItems; }
}
