package com.apex.core.dto;

import com.apex.core.model.OrderInfo;
import com.apex.core.model.OrderItem;

import java.util.List;

/**
 * 订单及订单项响应类
 * Order with items response class
 */
public class OrderWithItemsResponse {
    private OrderInfo order;
    private List<OrderItem> items;

    // Getters and Setters
    public OrderInfo getOrder() {
        return order;
    }

    public void setOrder(OrderInfo order) {
        this.order = order;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}
