package com.apex.core.service;

import com.apex.core.dao.IncomeDAO;
import com.apex.core.dto.CreateIncomeRequest;
import com.apex.core.dto.UpdateIncomeRequest;
import com.apex.core.model.Income;
import com.apex.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 财务收支服务层
 * 提供财务记录的创建、查询、更新、删除及统计分析功能
 */
public class IncomeService {
    private static final Logger logger = LoggerFactory.getLogger(IncomeService.class);
    private final IncomeDAO incomeDAO = new IncomeDAO();

    /**
     * 创建财务记录
     */
    public Map<String, Object> createIncome(String token, CreateIncomeRequest request) {
        Map<String, Object> result = new HashMap<>();

        logger.info("Creating new income record for order: {}", request.getOrderId());

        // 验证Token
        if (!JwtUtil.validateToken(token)) {
            result.put("success", false);
            result.put("message", "无效的Token");
            logger.warn("Create income failed: invalid token");
            return result;
        }

        // 验证输入数据
        Map<String, String> validationErrors = validateCreateIncomeRequest(request);
        if (!validationErrors.isEmpty()) {
            result.put("success", false);
            result.put("message", "输入数据验证失败");
            result.put("errors", validationErrors);
            logger.warn("Create income failed: validation errors for order: {}", request.getOrderId());
            return result;
        }

        // 创建财务记录
        Income income = new Income();
        income.setOrderId(request.getOrderId().trim());
        income.setType(request.getType().trim());
        income.setAmount(request.getAmount());

        if (request.getPaymentMethod() != null && !request.getPaymentMethod().trim().isEmpty()) {
            income.setPaymentMethod(request.getPaymentMethod().trim());
        }

        income.setStatus(request.getStatus() != null ? request.getStatus() : 1); // 默认待入账

        if (request.getTransactionTime() != null) {
            income.setTransactionTime(request.getTransactionTime());
        } else {
            income.setTransactionTime(LocalDateTime.now());
        }

        if (request.getRemark() != null && !request.getRemark().trim().isEmpty()) {
            income.setRemark(request.getRemark().trim());
        }

        boolean createSuccess = incomeDAO.create(income);
        if (!createSuccess) {
            result.put("success", false);
            result.put("message", "创建财务记录失败");
            logger.error("Create income failed: database creation failed for order: {}", request.getOrderId());
            return result;
        }

        // 构建响应数据
        Map<String, Object> incomeData = new HashMap<>();
        incomeData.put("id", income.getId());
        incomeData.put("orderId", income.getOrderId());
        incomeData.put("type", income.getType());
        incomeData.put("amount", income.getAmount());
        incomeData.put("paymentMethod", income.getPaymentMethod());
        incomeData.put("status", income.getStatus());
        incomeData.put("transactionTime", income.getTransactionTime());
        incomeData.put("remark", income.getRemark());

        result.put("success", true);
        result.put("data", incomeData);

        logger.info("Income record created successfully. ID: {} for order: {}", income.getId(), income.getOrderId());
        return result;
    }

    /**
     * 查询财务记录详情
     */
    public Map<String, Object> getIncomeDetail(Integer id) {
        Map<String, Object> result = new HashMap<>();

        logger.info("Getting income detail for ID: {}", id);

        Income income = incomeDAO.findById(id);
        if (income == null) {
            result.put("success", false);
            result.put("message", "财务记录不存在");
            logger.warn("Get income detail failed: record not found - {}", id);
            return result;
        }

        // 构建响应数据
        Map<String, Object> incomeData = new HashMap<>();
        incomeData.put("id", income.getId());
        incomeData.put("orderId", income.getOrderId());
        incomeData.put("type", income.getType());
        incomeData.put("amount", income.getAmount());
        incomeData.put("paymentMethod", income.getPaymentMethod());
        incomeData.put("status", income.getStatus());
        incomeData.put("transactionTime", income.getTransactionTime());
        incomeData.put("remark", income.getRemark());

        result.put("success", true);
        result.put("data", incomeData);

        logger.info("Income detail retrieved for ID: {}", id);
        return result;
    }

    /**
     * 查询财务记录列表
     */
    public Map<String, Object> listIncomes(String type, Integer status, Integer page, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();

        logger.info("Listing incomes. Type: {}, Status: {}, Page: {}, PageSize: {}",
                type, status, page, pageSize);

        // 设置默认值
        if (page == null || page < 1) page = 1;
        if (pageSize == null || pageSize < 1) pageSize = 20;

        try {
            List<Income> incomes;

            if (type != null && !type.trim().isEmpty()) {
                incomes = incomeDAO.findByType(type.trim(), page, pageSize);
                // 注意：实际项目中需要实现count方法，这里简化处理
            } else if (status != null) {
                incomes = incomeDAO.findByStatus(status, page, pageSize);
            } else {
                incomes = incomeDAO.findAll(page, pageSize);
            }

            // 获取统计信息
            BigDecimal totalIncome = incomeDAO.calculateTotalIncome();
            BigDecimal totalRefund = incomeDAO.calculateTotalRefund();

            // 构建响应数据
            Map<String, Object> listData = new HashMap<>();
            listData.put("incomes", incomes);
            listData.put("currentPage", page);
            listData.put("pageSize", pageSize);
            listData.put("totalIncome", totalIncome);
            listData.put("totalRefund", totalRefund);
            listData.put("netIncome", totalIncome.subtract(totalRefund));

            long totalCount = incomeDAO.count();

            listData.put("totalCount", totalCount);

            result.put("success", true);
            result.put("data", listData);

            logger.info("Income list retrieved. Count: {}", totalCount);
        } catch (Exception e) {
            // 修复：确保错误消息不为null
            result.put("success", false);
            result.put("message", "查询财务记录列表失败");
            logger.error("List incomes failed: {}", e.getMessage(), e);
        }

        return result;
    }

    /**
     * 更新财务记录
     */
    public Map<String, Object> updateIncome(String token, Integer id, UpdateIncomeRequest request) {
        Map<String, Object> result = new HashMap<>();

        logger.info("Updating income record ID: {}", id);

        // 验证Token
        if (!JwtUtil.validateToken(token)) {
            result.put("success", false);
            result.put("message", "无效的Token");
            logger.warn("Update income failed: invalid token");
            return result;
        }

        // 检查财务记录是否存在
        Income existingIncome = incomeDAO.findById(id);
        if (existingIncome == null) {
            result.put("success", false);
            result.put("message", "财务记录不存在");
            logger.warn("Update income failed: record not found - {}", id);
            return result;
        }

        // 更新字段
        if (request.getOrderId() != null && !request.getOrderId().trim().isEmpty()) {
            existingIncome.setOrderId(request.getOrderId().trim());
        }
        if (request.getType() != null && !request.getType().trim().isEmpty()) {
            existingIncome.setType(request.getType().trim());
        }
        if (request.getAmount() != null) {
            existingIncome.setAmount(request.getAmount());
        }
        if (request.getPaymentMethod() != null) {
            existingIncome.setPaymentMethod(request.getPaymentMethod().trim());
        }
        if (request.getStatus() != null) {
            existingIncome.setStatus(request.getStatus());
        }
        if (request.getTransactionTime() != null) {
            existingIncome.setTransactionTime(request.getTransactionTime());
        }
        if (request.getRemark() != null) {
            existingIncome.setRemark(request.getRemark().trim());
        }

        boolean updateSuccess = incomeDAO.update(existingIncome);
        if (!updateSuccess) {
            result.put("success", false);
            result.put("message", "更新财务记录失败");
            logger.error("Update income failed: database update failed for ID: {}", id);
            return result;
        }

        // 构建响应数据
        Map<String, Object> incomeData = new HashMap<>();
        incomeData.put("id", existingIncome.getId());
        incomeData.put("orderId", existingIncome.getOrderId());
        incomeData.put("type", existingIncome.getType());
        incomeData.put("amount", existingIncome.getAmount());
        incomeData.put("paymentMethod", existingIncome.getPaymentMethod());
        incomeData.put("status", existingIncome.getStatus());
        incomeData.put("transactionTime", existingIncome.getTransactionTime());
        incomeData.put("remark", existingIncome.getRemark());

        result.put("success", true);
        result.put("data", incomeData);

        logger.info("Income record updated successfully. ID: {}", id);
        return result;
    }

    /**
     * 删除财务记录
     */
    public Map<String, Object> deleteIncome(String token, Integer id) {
        Map<String, Object> result = new HashMap<>();

        logger.info("Deleting income record ID: {}", id);

        // 验证Token
        if (!JwtUtil.validateToken(token)) {
            result.put("success", false);
            result.put("message", "无效的Token");
            logger.warn("Delete income failed: invalid token");
            return result;
        }

        // 检查财务记录是否存在
        Income existingIncome = incomeDAO.findById(id);
        if (existingIncome == null) {
            result.put("success", false);
            result.put("message", "财务记录不存在");
            logger.warn("Delete income failed: record not found - {}", id);
            return result;
        }

        boolean deleteSuccess = incomeDAO.delete(id);
        if (!deleteSuccess) {
            result.put("success", false);
            result.put("message", "删除财务记录失败");
            logger.error("Delete income failed: database deletion failed for ID: {}", id);
            return result;
        }

        result.put("success", true);
        result.put("message", "财务记录删除成功");

        logger.info("Income record deleted successfully. ID: {}", id);
        return result;
    }

    /**
     * 更新财务状态
     */
    public Map<String, Object> updateIncomeStatus(String token, Integer id, Integer status) {
        Map<String, Object> result = new HashMap<>();

        logger.info("Updating income status. ID: {}, New Status: {}", id, status);

        // 验证Token
        if (!JwtUtil.validateToken(token)) {
            result.put("success", false);
            result.put("message", "无效的Token");
            logger.warn("Update income status failed: invalid token");
            return result;
        }

        // 验证状态值
        if (status == null || (status != 1 && status != 2)) {
            result.put("success", false);
            result.put("message", "状态值无效，必须为1(待入账)或2(已入账)");
            logger.warn("Update income status failed: invalid status value - {}", status);
            return result;
        }

        boolean updateSuccess = incomeDAO.updateStatus(id, status);
        if (!updateSuccess) {
            result.put("success", false);
            result.put("message", "更新财务状态失败");
            logger.error("Update income status failed: database update failed for ID: {}", id);
            return result;
        }

        result.put("success", true);
        result.put("message", "财务状态更新成功");

        logger.info("Income status updated successfully. ID: {}, Status: {}", id, status);
        return result;
    }

    /**
     * 获取财务统计
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> result = new HashMap<>();

        logger.info("Getting income statistics");

        try {
            BigDecimal totalIncome = incomeDAO.calculateTotalIncome();
            BigDecimal totalRefund = incomeDAO.calculateTotalRefund();
            BigDecimal netIncome = totalIncome.subtract(totalRefund);

            Map<String, Object> statsData = new HashMap<>();
            statsData.put("totalIncome", totalIncome);
            statsData.put("totalRefund", totalRefund);
            statsData.put("netIncome", netIncome);

            result.put("success", true);
            result.put("data", statsData);

            logger.info("Income statistics retrieved. Total Income: {}, Total Refund: {}",
                    totalIncome, totalRefund);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取财务统计失败");
            logger.error("Get income statistics failed: {}", e.getMessage(), e);
        }

        return result;
    }

    /**
     * 根据订单号查询财务记录
     */
    public Map<String, Object> getIncomesByOrderId(String orderId) {
        Map<String, Object> result = new HashMap<>();

        logger.info("Getting incomes by order ID: {}", orderId);

        if (orderId == null || orderId.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "订单号不能为空");
            logger.warn("Get incomes by order ID failed: order ID is empty");
            return result;
        }

        List<Income> incomes = incomeDAO.findByOrderId(orderId.trim());

        if (incomes.isEmpty()) {
            result.put("success", false);
            result.put("message", "未找到该订单的财务记录");
            logger.warn("Get incomes by order ID failed: no records found for order: {}", orderId);
            return result;
        }

        result.put("success", true);
        result.put("data", incomes);
        result.put("count", incomes.size());

        logger.info("Incomes retrieved for order: {}. Count: {}", orderId, incomes.size());
        return result;
    }

    /**
     * 验证创建财务记录请求
     */
    private Map<String, String> validateCreateIncomeRequest(CreateIncomeRequest request) {
        Map<String, String> errors = new HashMap<>();

        // 验证订单号
        if (request.getOrderId() == null || request.getOrderId().trim().isEmpty()) {
            errors.put("orderId", "订单号不能为空");
        } else if (request.getOrderId().trim().length() > 50) {
            errors.put("orderId", "订单号长度不能超过50个字符");
        }

        // 验证类型
        if (request.getType() == null || request.getType().trim().isEmpty()) {
            errors.put("type", "财务类型不能为空");
        } else {
            String type = request.getType().trim();
            if (!"income".equals(type) && !"refund".equals(type)) {
                errors.put("type", "财务类型必须是'income'或'refund'");
            }
        }

        // 验证金额
        if (request.getAmount() == null) {
            errors.put("amount", "金额不能为空");
        } else if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.put("amount", "金额必须大于0");
        }

        // 验证状态（如果有）
        if (request.getStatus() != null && request.getStatus() != 1 && request.getStatus() != 2) {
            errors.put("status", "状态必须是1(待入账)或2(已入账)");
        }

        // 验证备注长度
        if (request.getRemark() != null && request.getRemark().length() > 200) {
            errors.put("remark", "备注长度不能超过200个字符");
        }

        return errors;
    }
}
