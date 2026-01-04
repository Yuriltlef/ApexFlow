package com.apex.core.controller;

import com.apex.api.BaseServlet;
import com.apex.core.dto.*;
import com.apex.core.service.AfterSalesService;
import com.apex.util.Permission;
import com.apex.util.RequirePermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 售后服务API控制器
 * API列表：
 * POST   /api/after-sales          创建售后申请
 * GET    /api/after-sales/{id}     获取售后详情
 * GET    /api/after-sales          获取售后列表（分页）
 * GET    /api/after-sales/order/{orderId}  获取订单售后记录
 * GET    /api/after-sales/status/{status}  根据状态获取售后列表
 * PUT    /api/after-sales/{id}/status     更新售后状态
 * DELETE /api/after-sales/{id}     删除售后记录
 * GET    /api/after-sales/stats    获取售后统计
 */
@WebServlet("/api/after-sales/*")
public class AfterSalesServlet extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(AfterSalesServlet.class);
    private final AfterSalesService afterSalesService = new AfterSalesService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long startTime = System.currentTimeMillis();
        String pathInfo = req.getPathInfo();

        try {
            logRequest(req);

            if (pathInfo == null || pathInfo.equals("/")) {
                handleCreateAfterSales(req, resp);
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        "API路径不正确", "API_PATH_INVALID");
            }

            logResponse(req, resp, startTime, resp.getStatus());
        } catch (Exception e) {
            logger.error("AfterSalesServlet POST error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
            logResponse(req, resp, startTime, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long startTime = System.currentTimeMillis();
        String pathInfo = req.getPathInfo();

        try {
            logRequest(req);

            if (pathInfo != null && pathInfo.startsWith("/list")) {
                // GET /api/after-sales - 获取列表
                handleGetAfterSalesList(req, resp);
                return;
            }
            // 2. 处理 /stats (获取统计)
            if (pathInfo != null && pathInfo.startsWith("/stats")) {
                handleGetStats(req, resp);
                return;
            }
            if (pathInfo == null) {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        "API不存在", "API_NOT_FOUND");
                return;
            }
            else {
                // 解析路径参数
                String[] pathParts = pathInfo.substring(1).split("/");

                if (pathParts.length == 1) {
                    // GET /api/after-sales/{id} - 获取详情
                    handleGetAfterSalesDetail(req, resp, pathParts[0]);
                    return;
                } else if (pathParts.length == 2) {
                    if ("order".equals(pathParts[0])) {
                        // GET /api/after-sales/order/{orderId} - 获取订单售后记录
                        handleGetAfterSalesByOrder(req, resp, pathParts[1]);
                        return;
                    } else if ("status".equals(pathParts[0])) {
                        // GET /api/after-sales/status/{status} - 根据状态获取列表
                        handleGetAfterSalesByStatus(req, resp, pathParts[1]);
                        return;
                    } else {
                        sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                                "API不存在", "API_NOT_FOUND");
                    }
                } else {
                    sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                            "API不存在", "API_NOT_FOUND");
                }
            }

            logResponse(req, resp, startTime, resp.getStatus());
        } catch (Exception e) {
            logger.error("AfterSalesServlet GET error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
            logResponse(req, resp, startTime, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long startTime = System.currentTimeMillis();
        String pathInfo = req.getPathInfo();

        try {
            logRequest(req);

            if (pathInfo == null) {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        "API路径不正确", "API_PATH_INVALID");
                logResponse(req, resp, startTime, HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 解析路径参数
            String[] pathParts = pathInfo.substring(1).split("/");

            logger.info("pathParts: {}", (Object) pathParts);

            if (pathParts.length == 2 && "status".equals(pathParts[1])) {
                // PUT /api/after-sales/{id}/status - 更新状态
                handleUpdateAfterSalesStatus(req, resp, pathParts[0]);
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        "API不存在", "API_NOT_FOUND");
            }

            logResponse(req, resp, startTime, resp.getStatus());
        } catch (Exception e) {
            logger.error("AfterSalesServlet PUT error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
            logResponse(req, resp, startTime, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long startTime = System.currentTimeMillis();
        String pathInfo = req.getPathInfo();

        try {
            logRequest(req);

            if (pathInfo == null || pathInfo.equals("/")) {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        "API路径不正确", "API_PATH_INVALID");
                logResponse(req, resp, startTime, HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 解析路径参数
            String[] pathParts = pathInfo.substring(1).split("/");

            if (pathParts.length == 1) {
                // DELETE /api/after-sales/{id} - 删除记录
                handleDeleteAfterSales(req, resp, pathParts[0]);
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        "API不存在", "API_NOT_FOUND");
            }

            logResponse(req, resp, startTime, resp.getStatus());
        } catch (Exception e) {
            logger.error("AfterSalesServlet DELETE error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
            logResponse(req, resp, startTime, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 创建售后申请
     */
    @RequirePermission(
            value = {Permission.ADMIN, Permission.AFTER_SALES_MANAGE},
            message = "需要管理员或售后管理权限",
            logic = RequirePermission.LogicType.OR
    )
    private void handleCreateAfterSales(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        logger.info("[AFTER_SALES_API] Creating after sales application. Client IP: {}", getClientIp(req));

        try {
            // 验证Content-Type
            String contentType = req.getContentType();
            if (contentType == null || !contentType.contains("application/json")) {
                logger.warn("[AFTER_SALES_API_CREATE] Invalid Content-Type: {}", contentType);
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "请求必须是JSON格式", "INVALID_CONTENT_TYPE");
                return;
            }

            // 解析请求体
            AfterSalesCreateRequest createRequest = parseJsonBody(req, AfterSalesCreateRequest.class);

            // 验证必填字段
            if (createRequest.getOrderId() == null || createRequest.getOrderId().trim().isEmpty()) {
                throw new IllegalArgumentException("订单号不能为空");
            }
            if (createRequest.getType() == null || createRequest.getType() < 1 || createRequest.getType() > 3) {
                throw new IllegalArgumentException("售后类型无效，必须是1-3之间的整数");
            }
            if (createRequest.getReason() == null || createRequest.getReason().trim().isEmpty()) {
                throw new IllegalArgumentException("申请原因不能为空");
            }

            // 调用服务层
            com.apex.core.model.AfterSales afterSales = afterSalesService.createAfterSales(createRequest);

            // 发送成功响应
            ApiResponse<com.apex.core.model.AfterSales> apiResponse =
                    ApiResponse.success(afterSales, "售后申请创建成功");
            sendJsonResponse(resp, HttpServletResponse.SC_CREATED, apiResponse);
            logger.info("[AFTER_SALES_API] After sales application created successfully. ID: {}, Order: {}",
                    afterSales.getId(), afterSales.getOrderId());

        } catch (IllegalArgumentException e) {
            logger.warn("[AFTER_SALES_API_CREATE] Invalid request: {}", e.getMessage());
            ApiResponse<Void> errorResponse = ApiResponse.error(e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (Exception e) {
            logger.error("[AFTER_SALES_API] Failed to create after sales application: {}", e.getMessage(), e);
            ApiResponse<Void> errorResponse = ApiResponse.error("创建售后申请失败");
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorResponse);
        }
    }

    /**
     * 获取售后详情
     */
    @RequirePermission(
            value = {Permission.ADMIN, Permission.AFTER_SALES_MANAGE},
            message = "需要管理员或售后管理权限",
            logic = RequirePermission.LogicType.OR
    )
    private void handleGetAfterSalesDetail(HttpServletRequest req, HttpServletResponse resp, String idStr)
            throws IOException {
        logger.info("[AFTER_SALES_API] Getting after sales detail. ID: {}, Client IP: {}",
                idStr, getClientIp(req));

        try {
            // 解析ID
            Integer id = parseId(idStr);

            // 调用服务层
            com.apex.core.model.AfterSales afterSales = afterSalesService.getAfterSalesById(id);

            // 发送成功响应
            ApiResponse<com.apex.core.model.AfterSales> apiResponse =
                    ApiResponse.success(afterSales, "获取售后详情成功");
            sendJsonResponse(resp, HttpServletResponse.SC_OK, apiResponse);
            logger.info("[AFTER_SALES_API] After sales detail retrieved successfully. ID: {}", id);

        } catch (NumberFormatException e) {
            logger.warn("[AFTER_SALES_API_DETAIL] Invalid ID format: {}", idStr);
            ApiResponse<Void> errorResponse = ApiResponse.error("售后ID格式不正确");
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (IllegalArgumentException e) {
            logger.warn("[AFTER_SALES_API_DETAIL] Invalid request: {}", e.getMessage());
            ApiResponse<Void> errorResponse = ApiResponse.error(e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (Exception e) {
            logger.error("[AFTER_SALES_API] Failed to get after sales detail. ID: {}, Error: {}",
                    idStr, e.getMessage(), e);
            ApiResponse<Void> errorResponse = ApiResponse.error("获取售后详情失败");
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorResponse);
        }
    }

    /**
     * 获取售后列表（分页）
     */
    @RequirePermission(
            value = {Permission.ADMIN, Permission.AFTER_SALES_MANAGE},
            message = "需要管理员或售后管理权限",
            logic = RequirePermission.LogicType.OR
    )
    private void handleGetAfterSalesList(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        logger.info("[AFTER_SALES_API] Getting after sales list. Client IP: {}", getClientIp(req));

        try {
            // 解析分页参数
            int page = parseIntParameter(req, "page", 1);
            int pageSize = parseIntParameter(req, "pageSize", 20);

            // 调用服务层
            PagedResult<com.apex.core.model.AfterSales> result =
                    afterSalesService.getAfterSalesList(page, pageSize);

            // 发送成功响应
            ApiResponse<PagedResult<com.apex.core.model.AfterSales>> apiResponse =
                    ApiResponse.success(result, "获取售后列表成功");
            sendJsonResponse(resp, HttpServletResponse.SC_OK, apiResponse);
            logger.info("[AFTER_SALES_API_LIST] After sales list retrieved successfully. Total: {}, Page: {}",
                    result.getTotalCount(), page);

        } catch (IllegalArgumentException e) {
            logger.warn("[AFTER_SALES_API_LIST] Invalid parameters: {}", e.getMessage());
            ApiResponse<Void> errorResponse = ApiResponse.error(e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (Exception e) {
            logger.error("[AFTER_SALES_API_LIST] Failed to get after sales list: {}", e.getMessage(), e);
            ApiResponse<Void> errorResponse = ApiResponse.error("获取售后列表失败");
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorResponse);
        }
    }

    /**
     * 获取订单的售后记录
     */
    @RequirePermission(
            value = {Permission.ADMIN, Permission.AFTER_SALES_MANAGE},
            message = "需要管理员或售后管理权限",
            logic = RequirePermission.LogicType.OR
    )
    private void handleGetAfterSalesByOrder(HttpServletRequest req, HttpServletResponse resp, String orderId)
            throws IOException {
        logger.info("[AFTER_SALES_API] Getting after sales by order. Order: {}, Client IP: {}",
                orderId, getClientIp(req));

        try {
            // 调用服务层
            java.util.List<com.apex.core.model.AfterSales> afterSalesList =
                    afterSalesService.getAfterSalesByOrder(orderId);

            // 发送成功响应
            ApiResponse<java.util.List<com.apex.core.model.AfterSales>> apiResponse =
                    ApiResponse.success(afterSalesList, "获取订单售后记录成功");
            sendJsonResponse(resp, HttpServletResponse.SC_OK, apiResponse);
            logger.info("[AFTER_SALES_API] Order after sales retrieved successfully. Order: {}, Count: {}",
                    orderId, afterSalesList.size());

        } catch (Exception e) {
            logger.error("[AFTER_SALES_API_GET_ORDER] Failed to get order after sales. Order: {}, Error: {}",
                    orderId, e.getMessage(), e);
            ApiResponse<Void> errorResponse = ApiResponse.error("获取订单售后记录失败");
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorResponse);
        }
    }

    /**
     * 根据状态获取售后列表
     */
    @RequirePermission(
            value = {Permission.ADMIN, Permission.AFTER_SALES_MANAGE},
            message = "需要管理员或售后管理权限",
            logic = RequirePermission.LogicType.OR
    )
    private void handleGetAfterSalesByStatus(HttpServletRequest req, HttpServletResponse resp, String statusStr)
            throws IOException {
        logger.info("[AFTER_SALES_API] Getting after sales by status. Status: {}, Client IP: {}",
                statusStr, getClientIp(req));

        try {
            // 解析状态
            Integer status = parseStatus(statusStr);

            // 解析分页参数
            int page = parseIntParameter(req, "page", 1);
            int pageSize = parseIntParameter(req, "pageSize", 20);

            // 调用服务层
            PagedResult<com.apex.core.model.AfterSales> result =
                    afterSalesService.getAfterSalesByStatus(status, page, pageSize);

            // 发送成功响应
            ApiResponse<PagedResult<com.apex.core.model.AfterSales>> apiResponse =
                    ApiResponse.success(result, "获取状态售后列表成功");
            sendJsonResponse(resp, HttpServletResponse.SC_OK, apiResponse);
            logger.info("[AFTER_SALES_API_STATUS] Status after sales retrieved successfully. Status: {}, Count: {}",
                    status, result.getData().size());

        } catch (NumberFormatException e) {
            logger.warn("[AFTER_SALES_API_STATUS] Invalid status format: {}", statusStr);
            ApiResponse<Void> errorResponse = ApiResponse.error("状态格式不正确");
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (IllegalArgumentException e) {
            logger.warn("[AFTER_SALES_API_STATUS] Invalid parameters: {}", e.getMessage());
            ApiResponse<Void> errorResponse = ApiResponse.error(e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (Exception e) {
            logger.error("[AFTER_SALES_API_STATUS] Failed to get status after sales. Status: {}, Error: {}",
                    statusStr, e.getMessage(), e);
            ApiResponse<Void> errorResponse = ApiResponse.error("获取状态售后列表失败");
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorResponse);
        }
    }

    /**
     * 更新售后状态
     */
    @RequirePermission(
            value = {Permission.ADMIN, Permission.AFTER_SALES_MANAGE},
            message = "需要管理员或售后管理权限",
            logic = RequirePermission.LogicType.OR
    )
    private void handleUpdateAfterSalesStatus(HttpServletRequest req, HttpServletResponse resp, String idStr)
            throws IOException {
        logger.info("[AFTER_SALES_API_UPDATE_STATUS] Updating after sales status. ID: {}, Client IP: {}",
                idStr, getClientIp(req));

        try {
            // 验证Content-Type
            String contentType = req.getContentType();
            if (contentType == null || !contentType.contains("application/json")) {
                logger.warn("[AFTER_SALES_API_UPDATE_STATUS] Invalid Content-Type: {}", contentType);
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "请求必须是JSON格式", "INVALID_CONTENT_TYPE");
                return;
            }

            // 解析ID
            Integer id = parseId(idStr);

            // 解析请求体
            AfterSalesStatusUpdateRequest updateRequest = parseJsonBody(req, AfterSalesStatusUpdateRequest.class);

            // 验证必填字段
            if (updateRequest.getStatus() == null) {
                throw new IllegalArgumentException("状态不能为空");
            }

            // 调用服务层
            com.apex.core.model.AfterSales afterSales = afterSalesService.updateAfterSalesStatus(
                    id, updateRequest.getStatus(), updateRequest.getRemark());

            // 发送成功响应
            ApiResponse<com.apex.core.model.AfterSales> apiResponse =
                    ApiResponse.success(afterSales, "更新售后状态成功");
            sendJsonResponse(resp, HttpServletResponse.SC_OK, apiResponse);
            logger.info("[AFTER_SALES_API_UPDATE_STATUS] After sales status updated successfully. ID: {}, New status: {}",
                    id, updateRequest.getStatus());

        } catch (NumberFormatException e) {
            logger.warn("[AFTER_SALES_API_UPDATE_STATUS] Invalid ID format: {}", idStr);
            ApiResponse<Void> errorResponse = ApiResponse.error("售后ID格式不正确");
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (IllegalArgumentException e) {
            logger.warn("[AFTER_SALES_API_UPDATE_STATUS] Invalid request: {}", e.getMessage());
            ApiResponse<Void> errorResponse = ApiResponse.error(e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (Exception e) {
            logger.error("[AFTER_SALES_API] Failed to update after sales status. ID: {}, Error: {}",
                    idStr, e.getMessage(), e);
            ApiResponse<Void> errorResponse = ApiResponse.error("更新售后状态失败");
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorResponse);
        }
    }

    /**
     * 删除售后记录
     */
    @RequirePermission(
            value = {Permission.ADMIN, Permission.AFTER_SALES_MANAGE},
            message = "需要管理员或售后管理权限",
            logic = RequirePermission.LogicType.OR
    )
    private void handleDeleteAfterSales(HttpServletRequest req, HttpServletResponse resp, String idStr)
            throws IOException {
        logger.info("[AFTER_SALES_API_DEL] Deleting after sales. ID: {}, Client IP: {}",
                idStr, getClientIp(req));

        try {
            // 解析ID
            Integer id = parseId(idStr);

            // 调用服务层
            boolean success = afterSalesService.deleteAfterSales(id);

            if (success) {
                ApiResponse<Void> apiResponse = ApiResponse.success(null, "删除售后记录成功");
                sendJsonResponse(resp, HttpServletResponse.SC_OK, apiResponse);
                logger.info("[AFTER_SALES_API_DEL] After sales deleted successfully. ID: {}", id);
            } else {
                ApiResponse<Void> errorResponse = ApiResponse.error("删除售后记录失败");
                sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorResponse);
            }

        } catch (NumberFormatException e) {
            logger.warn("[AFTER_SALES_API_DEL] Invalid ID format: {}", idStr);
            ApiResponse<Void> errorResponse = ApiResponse.error("售后ID格式不正确");
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (IllegalArgumentException e) {
            logger.warn("[AFTER_SALES_API_DEL] Invalid request: {}", e.getMessage());
            ApiResponse<Void> errorResponse = ApiResponse.error(e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (Exception e) {
            logger.error("[AFTER_SALES_API_DEL] Failed to delete after sales. ID: {}, Error: {}",
                    idStr, e.getMessage(), e);
            ApiResponse<Void> errorResponse = ApiResponse.error("删除售后记录失败");
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorResponse);
        }
    }

    /**
     * 获取售后统计
     */
    @RequirePermission(
            value = {Permission.ADMIN, Permission.AFTER_SALES_MANAGE},
            message = "需要管理员或售后管理权限",
            logic = RequirePermission.LogicType.OR
    )
    private void handleGetStats(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        logger.info("[AFTER_SALES_API] Getting after sales statistics. Client IP: {}", getClientIp(req));

        try {
            // 调用服务层
            AfterSalesStatsDTO stats = afterSalesService.getAfterSalesStats();

            // 发送成功响应
            ApiResponse<AfterSalesStatsDTO> apiResponse =
                    ApiResponse.success(stats, "获取售后统计成功");
            sendJsonResponse(resp, HttpServletResponse.SC_OK, apiResponse);
            logger.info("[AFTER_SALES_API] After sales statistics retrieved successfully");

        } catch (Exception e) {
            logger.error("[AFTER_SALES_API] Failed to get after sales statistics: {}", e.getMessage(), e);
            ApiResponse<Void> errorResponse = ApiResponse.error("获取售后统计失败");
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorResponse);
        }
    }

    /**
     * 解析整数参数
     */
    private int parseIntParameter(HttpServletRequest req, String paramName, int defaultValue) {
        String paramValue = req.getParameter(paramName);
        if (paramValue != null && !paramValue.trim().isEmpty()) {
            try {
                return Integer.parseInt(paramValue.trim());
            } catch (NumberFormatException e) {
                logger.warn("[AFTER_SALES_API] Invalid {} parameter: {}", paramName, paramValue);
            }
        }
        return defaultValue;
    }

    /**
     * 解析ID
     */
    private Integer parseId(String idStr) {
        try {
            return Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            logger.warn("[AFTER_SALES_API] Invalid ID format: {}", idStr);
            throw e;
        }
    }

    /**
     * 解析状态
     */
    private Integer parseStatus(String statusStr) {
        try {
            int status = Integer.parseInt(statusStr);
            if (status < 1 || status > 4) {
                throw new IllegalArgumentException("状态值无效，必须是1-4之间的整数");
            }
            return status;
        } catch (NumberFormatException e) {
            logger.warn("[AFTER_SALES_API] Invalid status format: {}", statusStr);
            throw e;
        }
    }
}
