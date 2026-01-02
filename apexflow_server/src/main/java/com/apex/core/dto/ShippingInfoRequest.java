package com.apex.core.dto;

import javax.validation.constraints.NotBlank;

/**
 * 发货信息请求DTO
 */
public class ShippingInfoRequest {
    @NotBlank(message = "订单号不能为空")
    private String orderId;

    @NotBlank(message = "快递公司不能为空")
    private String expressCompany;

    @NotBlank(message = "运单号不能为空")
    private String trackingNumber;

    @NotBlank(message = "发货地址不能为空")
    private String senderAddress;

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getExpressCompany() { return expressCompany; }
    public void setExpressCompany(String expressCompany) { this.expressCompany = expressCompany; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public String getSenderAddress() { return senderAddress; }
    public void setSenderAddress(String senderAddress) { this.senderAddress = senderAddress; }
}