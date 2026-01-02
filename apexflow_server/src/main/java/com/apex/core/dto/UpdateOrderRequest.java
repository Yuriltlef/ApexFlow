package com.apex.core.dto;

/**
 * 更新订单请求
 */
public class UpdateOrderRequest {
    private Integer addressId;
    private String paymentMethod;

    public UpdateOrderRequest() {}

    // Getters and Setters
    public Integer getAddressId() { return addressId; }
    public void setAddressId(Integer addressId) { this.addressId = addressId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}