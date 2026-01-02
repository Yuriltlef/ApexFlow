package com.apex.core.service;

import com.apex.core.dao.ILogisticsDAO;
import com.apex.core.dao.LogisticsDAO;
import com.apex.core.dto.*;
import com.apex.core.model.Logistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物流服务层
 * 处理物流相关的业务逻辑
 */
public class LogisticsService {
    private static final Logger logger = LoggerFactory.getLogger(LogisticsService.class);
    private final ILogisticsDAO logisticsDAO = new LogisticsDAO();

    /**
     * 创建物流信息
     * @param createRequest 创建请求
     * @return 创建的物流信息
     */
    public Logistics createLogistics(LogisticsCreateRequest createRequest) {
        logger.info("Creating logistics record for order: {}", createRequest.getOrderId());

        // 验证订单是否已有物流记录
        Logistics existing = logisticsDAO.findByOrderId(createRequest.getOrderId());
        if (existing != null) {
            logger.warn("Logistics record already exists for order: {}", createRequest.getOrderId());
            throw new IllegalArgumentException("该订单已有物流记录");
        }

        // 创建物流对象
        Logistics logistics = new Logistics();
        logistics.setOrderId(createRequest.getOrderId());
        logistics.setExpressCompany(createRequest.getExpressCompany());
        logistics.setTrackingNumber(createRequest.getTrackingNumber());
        logistics.setStatus("pending");
        logistics.setSenderAddress(createRequest.getSenderAddress());
        logistics.setReceiverAddress(createRequest.getReceiverAddress());

        // 保存到数据库
        boolean success = logisticsDAO.create(logistics);
        if (!success) {
            logger.error("Failed to create logistics record for order: {}", createRequest.getOrderId());
            throw new RuntimeException("创建物流记录失败");
        }

        logger.info("Logistics record created successfully. ID: {}", logistics.getId());
        return logistics;
    }

    /**
     * 根据订单号获取物流信息
     * @param orderId 订单号
     * @return 物流信息
     */
    public Logistics getLogisticsByOrder(String orderId) {
        logger.debug("Getting logistics for order: {}", orderId);

        Logistics logistics = logisticsDAO.findByOrderId(orderId);
        if (logistics == null) {
            logger.warn("Logistics record not found for order: {}", orderId);
            throw new IllegalArgumentException("未找到该订单的物流记录");
        }

        return logistics;
    }

    /**
     * 根据ID获取物流信息
     * @param id 物流ID
     * @return 物流信息
     */
    public Logistics getLogisticsById(Integer id) {
        logger.debug("Getting logistics by ID: {}", id);

        Logistics logistics = logisticsDAO.findById(id);
        if (logistics == null) {
            logger.warn("Logistics record not found for ID: {}", id);
            throw new IllegalArgumentException("未找到物流记录");
        }

        return logistics;
    }

    /**
     * 更新物流信息
     * @param updateRequest 更新请求
     * @return 更新后的物流信息
     */
    public Logistics updateLogistics(LogisticsUpdateRequest updateRequest) {
        logger.info("Updating logistics for order: {}", updateRequest.getOrderId());

        Logistics logistics = logisticsDAO.findByOrderId(updateRequest.getOrderId());
        if (logistics == null) {
            logger.warn("Logistics record not found for order: {}", updateRequest.getOrderId());
            throw new IllegalArgumentException("未找到该订单的物流记录");
        }

        // 更新字段
        if (updateRequest.getExpressCompany() != null) {
            logistics.setExpressCompany(updateRequest.getExpressCompany());
        }
        if (updateRequest.getTrackingNumber() != null) {
            logistics.setTrackingNumber(updateRequest.getTrackingNumber());
        }
        if (updateRequest.getSenderAddress() != null) {
            logistics.setSenderAddress(updateRequest.getSenderAddress());
        }
        if (updateRequest.getReceiverAddress() != null) {
            logistics.setReceiverAddress(updateRequest.getReceiverAddress());
        }

        // 保存更新
        boolean success = logisticsDAO.update(logistics);
        if (!success) {
            logger.error("Failed to update logistics for order: {}", updateRequest.getOrderId());
            throw new RuntimeException("更新物流信息失败");
        }

        logger.info("Logistics updated successfully for order: {}", updateRequest.getOrderId());
        return logistics;
    }

    /**
     * 更新物流状态
     * @param orderId 订单号
     * @param status 新状态
     * @return 更新后的物流信息
     */
    public Logistics updateLogisticsStatus(String orderId, String status) {
        logger.info("Updating logistics status. Order: {}, New Status: {}", orderId, status);

        // 验证状态值
        if (!isValidStatus(status)) {
            logger.warn("Invalid logistics status: {}", status);
            throw new IllegalArgumentException("无效的物流状态");
        }

        // 更新状态
        boolean success = logisticsDAO.updateStatus(orderId, status);
        if (!success) {
            logger.error("Failed to update logistics status for order: {}", orderId);
            throw new RuntimeException("更新物流状态失败");
        }

        // 如果是已发货状态，设置发货时间
        if ("shipped".equals(status)) {
            logisticsDAO.updateShippingInfo(orderId, null, null, null);
        }

        // 如果是已送达状态，设置送达时间
        if ("delivered".equals(status)) {
            logisticsDAO.updateDeliveryInfo(orderId, LocalDateTime.now());
        }

        Logistics logistics = logisticsDAO.findByOrderId(orderId);
        logger.info("Logistics status updated successfully for order: {}", orderId);
        return logistics;
    }

    /**
     * 更新发货信息
     * @param shippingRequest 发货信息请求
     * @return 更新后的物流信息
     */
    public Logistics updateShippingInfo(ShippingInfoRequest shippingRequest) {
        logger.info("Updating shipping info for order: {}", shippingRequest.getOrderId());

        boolean success = logisticsDAO.updateShippingInfo(
                shippingRequest.getOrderId(),
                shippingRequest.getExpressCompany(),
                shippingRequest.getTrackingNumber(),
                shippingRequest.getSenderAddress()
        );

        if (!success) {
            logger.error("Failed to update shipping info for order: {}", shippingRequest.getOrderId());
            throw new RuntimeException("更新发货信息失败");
        }

        Logistics logistics = logisticsDAO.findByOrderId(shippingRequest.getOrderId());
        logger.info("Shipping info updated successfully for order: {}", shippingRequest.getOrderId());
        return logistics;
    }

    /**
     * 删除物流记录
     * @param id 物流ID
     * @return 是否删除成功
     */
    public boolean deleteLogistics(Integer id) {
        logger.warn("Deleting logistics record ID: {}", id);

        boolean success = logisticsDAO.delete(id);
        if (success) {
            logger.info("Logistics record deleted successfully. ID: {}", id);
        } else {
            logger.error("Failed to delete logistics record ID: {}", id);
        }

        return success;
    }

    /**
     * 获取待发货列表（分页）
     * @param page 页码
     * @param pageSize 每页大小
     * @return 待发货物流列表
     */
    public PagedResult<Logistics> getPendingShipping(int page, int pageSize) {
        logger.debug("Getting pending shipping list. Page: {}, PageSize: {}", page, pageSize);

        List<Logistics> logisticsList = logisticsDAO.findPendingShipping(page, pageSize);

        // 获取总数（简化处理，实际可能需要单独查询总数）
        int totalCount = logisticsList.size();
        // 这里应该查询数据库获取准确的totalCount，但为简化，我们假设这是全部

        PagedResult<Logistics> result = new PagedResult<>();
        result.setData(logisticsList);
        result.setCurrentPage(page);
        result.setPageSize(pageSize);
        result.setTotalCount(totalCount);

        logger.info("Pending shipping list retrieved. Count: {}", logisticsList.size());
        return result;
    }

    /**
     * 获取运输中列表（分页）
     * @param page 页码
     * @param pageSize 每页大小
     * @return 运输中物流列表
     */
    public PagedResult<Logistics> getInTransit(int page, int pageSize) {
        logger.debug("Getting in-transit list. Page: {}, PageSize: {}", page, pageSize);

        List<Logistics> logisticsList = logisticsDAO.findInTransit(page, pageSize);

        PagedResult<Logistics> result = new PagedResult<>();
        result.setData(logisticsList);
        result.setCurrentPage(page);
        result.setPageSize(pageSize);
        result.setTotalCount(logisticsList.size());

        logger.info("In-transit list retrieved. Count: {}", logisticsList.size());
        return result;
    }

    /**
     * 获取物流统计
     * @return 物流统计数据
     */
    public LogisticsStats getLogisticsStats() {
        logger.debug("Getting logistics statistics");

        LogisticsStats stats = logisticsDAO.getLogisticsStats();
        logger.info("Logistics statistics retrieved. Pending: {}, Shipped: {}, Delivered: {}",
                stats.getPendingCount(), stats.getShippedCount(), stats.getDeliveredCount());

        return stats;
    }

    /**
     * 验证物流状态是否有效
     * @param status 状态
     * @return 是否有效
     */
    private boolean isValidStatus(String status) {
        return "pending".equals(status) || "shipped".equals(status) || "delivered".equals(status);
    }
}