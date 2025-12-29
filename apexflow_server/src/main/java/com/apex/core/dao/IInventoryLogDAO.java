package com.apex.core.dao;

import com.apex.core.model.InventoryLog;

import java.util.List;

/**
 * 库存变更日志数据访问对象接口
 * 定义库存变更日志的增删改查及统计操作
 */
public interface IInventoryLogDAO {

    /**
     * 创建库存变更日志
     * @param inventoryLog 库存变更日志对象
     * @return 创建成功返回true，失败返回false
     */
    boolean create(InventoryLog inventoryLog);

    /**
     * 根据ID查询库存变更日志
     * @param id 库存变更日志ID
     * @return 对应的库存变更日志对象
     */
    InventoryLog findById(Integer id);

    /**
     * 根据商品ID查询库存变更日志（分页）
     * @param productId 商品ID
     * @param page 页码（从1开始）
     * @param pageSize 每页记录数
     * @return 指定商品的库存变更日志列表
     */
    List<InventoryLog> findByProductId(Integer productId, int page, int pageSize);

    /**
     * 根据订单号查询库存变更日志
     * @param orderId 订单ID
     * @return 指定订单的库存变更日志列表
     */
    List<InventoryLog> findByOrderId(String orderId);

    /**
     * 根据变更类型查询库存变更日志（分页）
     * @param changeType 变更类型（如：purchase, sale等）
     * @param page 页码（从1开始）
     * @param pageSize 每页记录数
     * @return 指定变更类型的库存日志列表
     */
    List<InventoryLog> findByChangeType(String changeType, int page, int pageSize);

    /**
     * 获取商品最近一次库存变更
     * @param productId 商品ID
     * @return 最近一次库存变更记录
     */
    InventoryLog findLatestByProductId(Integer productId);

    /**
     * 统计商品采购总量
     * @param productId 商品ID
     * @return 该商品的总采购数量
     */
    Integer calculatePurchaseQuantity(Integer productId);

    /**
     * 统计商品销售总量
     * @param productId 商品ID
     * @return 该商品的总销售数量
     */
    Integer calculateSalesQuantity(Integer productId);

    /**
     * 获取库存预警商品
     * @param threshold 库存预警阈值
     * @return 库存低于阈值的商品ID列表
     */
    List<Integer> getLowStockProducts(int threshold);

    /**
     * 获取最近库存变更
     * @param limit 查询记录数限制
     * @return 最近的库存变更记录列表
     */
    List<InventoryLog> findRecentChanges(int limit);

    /**
     * 批量创建库存变更日志
     * @param inventoryLogs 库存变更日志对象列表
     * @return 批量创建成功返回true，失败返回false
     */
    boolean createBatch(List<InventoryLog> inventoryLogs);
}
