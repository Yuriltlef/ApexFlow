package com.apex.core.service;

import com.apex.core.dao.AfterSalesDAO;
import com.apex.core.model.AfterSales;
import com.apex.core.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 售后服务业务逻辑层
 * 提供售后记录的增删改查和状态管理功能
 */
public class AfterSalesService {
    private static final Logger logger = LoggerFactory.getLogger(AfterSalesService.class);
    private final AfterSalesDAO afterSalesDAO = new AfterSalesDAO();

    // 售后状态常量
    private static final int STATUS_APPLIED = 1;    // 申请中
    private static final int STATUS_APPROVED = 2;   // 审核通过
    private static final int STATUS_REJECTED = 3;   // 审核拒绝
    private static final int STATUS_COMPLETED = 4;  // 已完成

    /**
     * 创建售后申请
     * @param createRequest 创建请求DTO
     * @return 创建的售后记录
     */
    public AfterSales createAfterSales(AfterSalesCreateRequest createRequest) {
        logger.info("Creating after sales for order: {}", createRequest.getOrderId());

        AfterSales afterSales = new AfterSales();
        afterSales.setOrderId(createRequest.getOrderId());
        afterSales.setType(createRequest.getType());
        afterSales.setReason(createRequest.getReason());
        afterSales.setStatus(STATUS_APPLIED);
        afterSales.setApplyTime(LocalDateTime.now());

        // 如果是退货类型，设置退款金额
        if (createRequest.getType() == 1 && createRequest.getRefundAmount() != null) {
            afterSales.setRefundAmount(createRequest.getRefundAmount());
        }

        boolean success = afterSalesDAO.create(afterSales);
        if (!success) {
            logger.error("Failed to create after sales record for order: {}", createRequest.getOrderId());
            throw new RuntimeException("创建售后记录失败");
        }

        logger.info("After sales created successfully. ID: {}", afterSales.getId());
        return afterSales;
    }

    /**
     * 获取售后记录详情
     * @param id 售后记录ID
     * @return 售后记录详情
     */
    public AfterSales getAfterSalesById(Integer id) {
        logger.info("Getting after sales details for ID: {}", id);

        AfterSales afterSales = afterSalesDAO.findById(id);
        if (afterSales == null) {
            logger.warn("After sales record not found. ID: {}", id);
            throw new IllegalArgumentException("售后记录不存在");
        }

        logger.info("After sales record found. ID: {}, Order: {}", id, afterSales.getOrderId());
        return afterSales;
    }

    /**
     * 获取订单的售后记录列表
     * @param orderId 订单号
     * @return 售后记录列表
     */
    public List<AfterSales> getAfterSalesByOrder(String orderId) {
        logger.info("Getting after sales list for order: {}", orderId);

        List<AfterSales> afterSalesList = afterSalesDAO.findByOrderId(orderId);
        logger.info("Found {} after sales records for order: {}", afterSalesList.size(), orderId);
        return afterSalesList;
    }

    /**
     * 获取售后记录列表（分页）
     * @param page 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    public PagedResult<AfterSales> getAfterSalesList(int page, int pageSize) {
        logger.info("Getting after sales list. Page: {}, PageSize: {}", page, pageSize);

        validatePageParams(page, pageSize);

        List<AfterSales> afterSalesList = afterSalesDAO.findAll(page, pageSize);
        long totalCount = afterSalesDAO.count();

        PagedResult<AfterSales> result = new PagedResult<>();
        result.setData(afterSalesList);
        result.setCurrentPage(page);
        result.setPageSize(pageSize);
        result.setTotalCount((int) totalCount);

        logger.info("After sales list retrieved. Total: {}, Current page: {}", totalCount, page);
        return result;
    }

    /**
     * 根据状态获取售后记录列表
     * @param status 状态码
     * @param page 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    public PagedResult<AfterSales> getAfterSalesByStatus(Integer status, int page, int pageSize) {
        logger.info("Getting after sales list by status: {}", status);

        validatePageParams(page, pageSize);

        List<AfterSales> afterSalesList = afterSalesDAO.findByStatus(status, page, pageSize);
        long totalCount = afterSalesDAO.count();

        PagedResult<AfterSales> result = new PagedResult<>();
        result.setData(afterSalesList);
        result.setCurrentPage(page);
        result.setPageSize(pageSize);
        result.setTotalCount((int) totalCount);

        logger.info("After sales list by status retrieved. Status: {}, Count: {}", status, afterSalesList.size());
        return result;
    }

    /**
     * 更新售后记录状态
     * @param id 售后记录ID
     * @param status 新状态
     * @param remark 处理备注
     * @return 更新后的售后记录
     */
    public AfterSales updateAfterSalesStatus(Integer id, Integer status, String remark) {
        logger.info("Updating after sales status. ID: {}, New status: {}", id, status);

        // 验证状态值
        validateStatus(status);

        AfterSales afterSales = afterSalesDAO.findById(id);
        if (afterSales == null) {
            logger.warn("After sales record not found. ID: {}", id);
            throw new IllegalArgumentException("售后记录不存在");
        }

        // 验证状态转换是否合法
        validateStatusTransition(afterSales.getStatus(), status);

        // 更新状态
        boolean success = afterSalesDAO.updateStatus(id, status, remark);
        if (!success) {
            logger.error("Failed to update after sales status. ID: {}", id);
            throw new RuntimeException("更新售后状态失败");
        }

        // 重新获取更新后的记录
        afterSales = afterSalesDAO.findById(id);
        logger.info("After sales status updated successfully. ID: {}", id);
        return afterSales;
    }

    /**
     * 删除售后记录
     * @param id 售后记录ID
     * @return 删除结果
     */
    public boolean deleteAfterSales(Integer id) {
        logger.warn("Deleting after sales record. ID: {}", id);

        AfterSales afterSales = afterSalesDAO.findById(id);
        if (afterSales == null) {
            logger.warn("After sales record not found. ID: {}", id);
            throw new IllegalArgumentException("售后记录不存在");
        }

        // 检查是否可以删除（只有申请中的记录可以删除）
        if (afterSales.getStatus() != STATUS_APPLIED) {
            logger.warn("Cannot delete after sales record in status: {}", afterSales.getStatus());
            throw new IllegalArgumentException("只有申请中的售后记录可以删除");
        }

        boolean success = afterSalesDAO.delete(id);
        if (success) {
            logger.info("After sales record deleted successfully. ID: {}", id);
        } else {
            logger.error("Failed to delete after sales record. ID: {}", id);
        }
        return success;
    }

    /**
     * 获取售后统计信息
     * @return 统计结果
     */
    public AfterSalesStatsDTO getAfterSalesStats() {
        logger.info("Getting after sales statistics");

        // 这里可以扩展实现更复杂的统计逻辑
        // 当前简单返回总记录数
        long totalCount = afterSalesDAO.count();

        AfterSalesStatsDTO stats = new AfterSalesStatsDTO();
        stats.setTotalCount(totalCount);

        logger.info("After sales statistics retrieved. Total count: {}", totalCount);
        return stats;
    }

    /**
     * 验证页码参数
     */
    private void validatePageParams(int page, int pageSize) {
        if (page < 1) {
            throw new IllegalArgumentException("页码必须大于0");
        }
        if (pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("每页大小必须在1-100之间");
        }
    }

    /**
     * 验证状态值
     */
    private void validateStatus(Integer status) {
        if (status == null || status < 1 || status > 4) {
            throw new IllegalArgumentException("状态值无效，必须是1-4之间的整数");
        }
    }

    /**
     * 验证状态转换是否合法
     */
    private void validateStatusTransition(int currentStatus, int newStatus) {
        // 申请中 -> 审核通过/审核拒绝
        if (currentStatus == STATUS_APPLIED && (newStatus != STATUS_APPROVED && newStatus != STATUS_REJECTED)) {
            throw new IllegalArgumentException("申请中的记录只能转为审核通过或审核拒绝");
        }
        // 审核通过 -> 已完成
        if (currentStatus == STATUS_APPROVED && newStatus != STATUS_COMPLETED) {
            throw new IllegalArgumentException("审核通过的记录只能转为已完成");
        }
        // 已完成不能修改
        if (currentStatus == STATUS_COMPLETED) {
            throw new IllegalArgumentException("已完成的记录不能修改状态");
        }
    }
}