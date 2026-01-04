package com.apex.core.dao;

import com.apex.core.model.Logistics;
import com.apex.core.dto.LogisticsStats;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物流信息数据访问对象接口
 * 定义物流信息的增删改查及状态更新操作
 */
public interface ILogisticsDAO {

    /**
     * 创建物流信息
     * @param logistics 物流信息对象
     * @return 创建成功返回true，失败返回false
     */
    boolean create(Logistics logistics);

    /**
     * 根据ID查询物流信息
     * @param id 物流记录ID
     * @return 对应的物流信息对象
     */
    Logistics findById(Integer id);

    /**
     * 根据订单号查询物流信息
     * @param orderId 订单ID
     * @return 订单对应的物流信息
     */
    Logistics findByOrderId(String orderId);

    /**
     * 更新物流信息
     * @param logistics 包含更新信息的物流对象
     * @return 更新成功返回true，失败返回false
     */
    boolean update(Logistics logistics);

    /**
     * 删除物流信息
     * @param id 要删除的物流记录ID
     * @return 删除成功返回true，失败返回false
     */
    boolean delete(Integer id);

    /**
     * 更新物流状态
     * @param orderId 订单ID
     * @param status 新状态
     * @return 更新成功返回true，失败返回false
     */
    boolean updateStatus(String orderId, String status);

    /**
     * 更新发货信息
     * @param orderId 订单ID
     * @param expressCompany 快递公司
     * @param trackingNumber 快递单号
     * @param senderAddress 发货地址
     * @return 更新成功返回true，失败返回false
     */
    boolean updateShippingInfo(String orderId, String expressCompany,
                               String trackingNumber, String senderAddress);

    /**
     * 更新送达信息
     * @param orderId 订单ID
     * @param deliveredAt 送达时间
     * @return 更新成功返回true，失败返回false
     */
    boolean updateDeliveryInfo(String orderId, LocalDateTime deliveredAt);

    /**
     * 查询待发货的订单
     * @param page 页码（从1开始）
     * @param pageSize 每页记录数
     * @return 待发货的物流信息列表
     */
    List<Logistics> findPendingShipping(int page, int pageSize);

    /**
     * 查询运输中的订单
     * @param page 页码（从1开始）
     * @param pageSize 每页记录数
     * @return 运输中的物流信息列表
     */
    List<Logistics> findInTransit(int page, int pageSize);

    /**
     * 统计各状态物流数量
     * @return 包含待发货、运输中、已送达数量的统计对象
     */
    LogisticsStats getLogisticsStats();

    /**
     * 统计订单总数
     * @return 订单总数
     */
    long count();

    /**
     * 查询所有物流（分页）
     * @param page 页码（从1开始）
     * @param pageSize 每页记录数
     * @return 分页后的订单列表
     */
    List<Logistics> findAll(int page, int pageSize);
}
