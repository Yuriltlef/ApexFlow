package com.apex.core.dto;

import com.apex.core.model.*;

import java.util.List;

/**
 * 订单详情类（用于聚合订单相关所有信息）
 */
public class OrderDetail {
    private OrderInfo orderInfo;
    private List<OrderItem> orderItems;
    private Logistics logistics;
    private List<Income> incomes;
    private List<AfterSales> afterSalesList;
    private Review review;

    // getters and setters
    public OrderInfo getOrderInfo() { return orderInfo; }
    public void setOrderInfo(OrderInfo orderInfo) { this.orderInfo = orderInfo; }

    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }

    public Logistics getLogistics() { return logistics; }
    public void setLogistics(Logistics logistics) { this.logistics = logistics; }

    public List<Income> getIncomes() { return incomes; }
    public void setIncomes(List<Income> incomes) { this.incomes = incomes; }

    public List<AfterSales> getAfterSalesList() { return afterSalesList; }
    public void setAfterSalesList(List<AfterSales> afterSalesList) { this.afterSalesList = afterSalesList; }

    public Review getReview() { return review; }
    public void setReview(Review review) { this.review = review; }
}