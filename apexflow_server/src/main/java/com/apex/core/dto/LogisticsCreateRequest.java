package com.apex.core.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 创建物流信息请求DTO
 */
public class LogisticsCreateRequest {
    @NotBlank(message = "订单号不能为空")
    private String orderId;

    private String expressCompany;
    private String trackingNumber;
    private String senderAddress;
    private String receiverAddress;

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getExpressCompany() { return expressCompany; }
    public void setExpressCompany(String expressCompany) { this.expressCompany = expressCompany; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public String getSenderAddress() { return senderAddress; }
    public void setSenderAddress(String senderAddress) { this.senderAddress = senderAddress; }

    public String getReceiverAddress() { return receiverAddress; }
    public void setReceiverAddress(String receiverAddress) { this.receiverAddress = receiverAddress; }
}
