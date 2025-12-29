package com.apex.core.dao;

import com.apex.core.model.OrderInfo;

import java.util.List;

/**
 * 订单信息数据访问对象接口
 * 定义订单信息的增删改查及状态管理操作
 */
public interface IOrderInfoDAO {

    /**
     * 创建新订单
     * @param order 订单信息对象
     * @return 创建成功返回true，失败返回false
     */
    boolean create(OrderInfo order);

    /**
     * 根据ID查询订单
     * @param orderId 订单ID
     * @return 对应的订单信息对象
     */
    OrderInfo findById(String orderId);

    /**
     * 更新订单信息
     * @param order 包含更新信息的订单对象
     * @return 更新成功返回true，失败返回false
     */
    boolean update(OrderInfo order);

    /**
     * 更新订单状态
     * @param orderId 订单ID
     * @param status 新状态码
     * @return 更新成功返回true，失败返回false
     */
    boolean updateStatus(String orderId, int status);

    /**
     * 删除订单（已禁用，需要在业务层处理级联删除）
     * @param orderId 要删除的订单ID
     * @return 删除成功返回true，失败返回false
     */
    boolean delete(String orderId);

    /**
     * 统计订单总数
     * @return 订单总数
     */
    long count();

    /**
     * 查询所有订单（分页）
     * @param page 页码（从1开始）
     * @param pageSize 每页记录数
     * @return 分页后的订单列表
     */
    List<OrderInfo> findAll(int page, int pageSize);

    /**
     * 根据用户ID查询订单（分页）
     * @param userId 用户ID
     * @param page 页码（从1开始）
     * @param pageSize 每页记录数
     * @return 用户的所有订单列表
     */
    List<OrderInfo> findByUserId(int userId, int page, int pageSize);
}
