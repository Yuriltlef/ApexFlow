package com.apex.core.dao;

import com.apex.core.model.Income;

import java.math.BigDecimal;
import java.util.List;

/**
 * 财务收支数据访问对象接口
 * 定义财务记录的增删改查及统计操作
 */
public interface IIncomeDAO {

    /**
     * 创建财务记录
     * @param income 财务记录对象
     * @return 创建成功返回true，失败返回false
     */
    boolean create(Income income);

    /**
     * 根据ID查询财务记录
     * @param id 财务记录ID
     * @return 对应的财务记录对象，不存在时返回null
     */
    Income findById(Integer id);

    /**
     * 根据订单号查询财务记录
     * @param orderId 订单ID
     * @return 该订单的所有财务记录列表
     */
    List<Income> findByOrderId(String orderId);

    /**
     * 更新财务记录
     * @param income 包含更新信息的财务记录对象
     * @return 更新成功返回true，失败返回false
     */
    boolean update(Income income);

    /**
     * 删除财务记录
     * @param id 要删除的财务记录ID
     * @return 删除成功返回true，失败返回false
     */
    boolean delete(Integer id);

    /**
     * 查询所有财务记录（分页）
     * @param page 页码（从1开始）
     * @param pageSize 每页记录数
     * @return 分页后的财务记录列表
     */
    List<Income> findAll(int page, int pageSize);

    /**
     * 根据类型查询财务记录
     * @param type 财务类型（如：income, refund等）
     * @param page 页码（从1开始）
     * @param pageSize 每页记录数
     * @return 指定类型的财务记录列表
     */
    List<Income> findByType(String type, int page, int pageSize);

    /**
     * 根据状态查询财务记录
     * @param status 状态码
     * @param page 页码（从1开始）
     * @param pageSize 每页记录数
     * @return 指定状态的财务记录列表
     */
    List<Income> findByStatus(Integer status, int page, int pageSize);

    /**
     * 统计总收入
     * @return 总收入金额（仅包含已入账的收入类型）
     */
    BigDecimal calculateTotalIncome();

    /**
     * 统计总退款
     * @return 总退款金额（绝对值）
     */
    BigDecimal calculateTotalRefund();

    /**
     * 更新财务状态
     * @param id 财务记录ID
     * @param status 新状态码
     * @return 更新成功返回true，失败返回false
     */
    boolean updateStatus(Integer id, Integer status);
}
