package com.apex.core.dao;

import com.apex.core.model.OrderItem;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单商品明细数据访问对象接口
 * 定义订单项的增删改查及统计操作
 */
public interface IOrderItemDAO {

    /**
     * 创建订单项
     * @param orderItem 订单项对象
     * @return 创建成功返回true，失败返回false
     */
    boolean create(OrderItem orderItem);

    /**
     * 根据ID查询订单项
     * @param id 订单项ID
     * @return 对应的订单项对象
     */
    OrderItem findById(Integer id);

    /**
     * 根据订单号查询订单项
     * @param orderId 订单ID
     * @return 订单的所有商品项列表
     */
    List<OrderItem> findByOrderId(String orderId);

    /**
     * 更新订单项
     * @param orderItem 包含更新信息的订单项对象
     * @return 更新成功返回true，失败返回false
     */
    boolean update(OrderItem orderItem);

    /**
     * 删除订单项
     * @param id 要删除的订单项ID
     * @return 删除成功返回true，失败返回false
     */
    boolean delete(Integer id);

    /**
     * 批量创建订单项
     * @param orderItems 订单项对象列表
     * @return 批量创建成功返回true，失败返回false
     */
    boolean createBatch(List<OrderItem> orderItems);

    /**
     * 统计订单总金额
     * @param orderId 订单ID
     * @return 订单总金额
     */
    BigDecimal calculateOrderTotal(String orderId);

    /**
     * 统计商品销售数量
     * @param productId 商品ID
     * @return 该商品的销售总数量
     */
    Integer countProductSales(Integer productId);

    /**
     * 获取热门商品
     * @param limit 返回的商品数量限制
     * @return 按销售数量排序的热门商品ID列表
     */
    List<Integer> getTopProducts(int limit);
}
