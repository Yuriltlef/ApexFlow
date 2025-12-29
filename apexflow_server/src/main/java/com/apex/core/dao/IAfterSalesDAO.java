package com.apex.core.dao;

import com.apex.core.model.AfterSales;

import java.util.List;

/**
 * 售后服务数据访问对象接口
 * 定义售后服务记录的增删改查操作
 */
public interface IAfterSalesDAO {

    /**
     * 创建售后服务记录
     * @param afterSales 售后服务对象，包含订单ID、类型、原因等信息
     * @return 创建成功返回true，失败返回false
     */
    boolean create(AfterSales afterSales);

    /**
     * 根据ID查询售后服务记录
     * @param id 售后服务记录ID
     * @return 返回对应的售后服务对象，不存在时返回null
     */
    AfterSales findById(Integer id);

    /**
     * 根据订单号查询售后服务记录
     * @param orderId 订单ID
     * @return 返回该订单的所有售后服务记录列表
     */
    List<AfterSales> findByOrderId(String orderId);

    /**
     * 更新售后服务记录
     * @param afterSales 包含更新信息的售后服务对象，必须包含有效的ID
     * @return 更新成功返回true，失败返回false
     */
    boolean update(AfterSales afterSales);

    /**
     * 删除售后服务记录
     * @param id 要删除的售后服务记录ID
     * @return 删除成功返回true，失败返回false
     */
    boolean delete(Integer id);

    /**
     * 查询所有售后服务记录（分页）
     * @param page 页码（从1开始）
     * @param pageSize 每页记录数
     * @return 返回分页后的售后服务记录列表
     */
    List<AfterSales> findAll(int page, int pageSize);

    /**
     * 根据状态查询售后服务记录
     * @param status 状态码
     * @param page 页码（从1开始）
     * @param pageSize 每页记录数
     * @return 返回指定状态的售后服务记录列表
     */
    List<AfterSales> findByStatus(Integer status, int page, int pageSize);

    /**
     * 统计售后服务记录总数
     * @return 售后服务记录总数
     */
    long count();

    /**
     * 更新售后状态
     * @param id 售后服务记录ID
     * @param status 新状态码
     * @param processRemark 处理备注
     * @return 更新成功返回true，失败返回false
     */
    boolean updateStatus(Integer id, Integer status, String processRemark);
}
