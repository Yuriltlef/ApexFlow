package com.apex.core.controller;

import com.apex.api.BaseServlet;
import com.apex.core.dto.*;
import com.apex.core.service.LogisticsService;
import com.apex.util.Permission;
import com.apex.util.RequirePermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 物流管理API控制器
 * API列表：
 * POST   /api/logistics              创建物流信息
 * GET    /api/logistics/{id}         根据ID获取物流详情
 * GET    /api/logistics/order/{orderId} 根据订单号获取物流信息
 * GET    /api/logistics              获取物流列表（分页）
 * GET    /api/logistics/pending      获取待发货列表
 * GET    /api/logistics/in-transit   获取运输中列表
 * PUT    /api/logistics/{id}         更新物流信息
 * PUT    /api/logistics/{id}/status  更新物流状态
 * PUT    /api/logistics/{id}/shipping 更新发货信息
 * DELETE /api/logistics/{id}         删除物流记录
 * GET    /api/logistics/stats        获取物流统计
 */
@WebServlet("/api/logistics/*")
public class LogisticsServlet extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(LogisticsServlet.class);
    private final LogisticsService logisticsService = new LogisticsService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long startTime = System.currentTimeMillis();
        String pathInfo = req.getPathInfo();

        try {
            logRequest(req);

            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
                handleCreateLogistics(req, resp);
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        "API路径不正确", "API_PATH_INVALID");
            }

            logResponse(req, resp, startTime, resp.getStatus());
        } catch (Exception e) {
            logger.error("LogisticsServlet POST error: {}", e.getMessage(), e);
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

            if (pathInfo == null) {
                // GET /api/logistics - 获取列表
                handleGetLogisticsList(req, resp);
            } else if (pathInfo.startsWith("/stats")) {
                // GET /api/logistics/stats - 获取统计
                handleGetStats(req, resp);
            } else if (pathInfo.startsWith("/pending")) {
                // GET /api/logistics/pending - 获取待发货列表
                handleGetPendingShipping(req, resp);
            } else if (pathInfo.startsWith("/in-transit")) {
                // GET /api/logistics/in-transit - 获取运输中列表
                handleGetInTransit(req, resp);
            } else {
                // 解析路径参数
                String[] pathParts = pathInfo.substring(1).split("/");

                if (pathParts.length == 1) {
                    // GET /api/logistics/{id} - 根据ID获取详情
                    handleGetLogisticsById(req, resp, pathParts[0]);
                } else if (pathParts.length == 2 && "order".equals(pathParts[0])) {
                    // GET /api/logistics/order/{orderId} - 根据订单号获取
                    handleGetLogisticsByOrder(req, resp, pathParts[1]);
                } else {
                    sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                            "API不存在", "API_NOT_FOUND");
                }
            }

            logResponse(req, resp, startTime, resp.getStatus());
        } catch (Exception e) {
            logger.error("LogisticsServlet GET error: {}", e.getMessage(), e);
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

            if (pathParts.length == 1) {
                // PUT /api/logistics/{id} - 更新物流信息
                handleUpdateLogistics(req, resp, pathParts[0]);
            } else if (pathParts.length == 2) {
                if ("status".equals(pathParts[1])) {
                    // PUT /api/logistics/{id}/status - 更新状态
                    handleUpdateStatus(req, resp, pathParts[0]);
                } else if ("shipping".equals(pathParts[1])) {
                    // PUT /api/logistics/{id}/shipping - 更新发货信息
                    handleUpdateShippingInfo(req, resp, pathParts[0]);
                } else {
                    sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                            "API不存在", "API_NOT_FOUND");
                }
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        "API不存在", "API_NOT_FOUND");
            }

            logResponse(req, resp, startTime, resp.getStatus());
        } catch (Exception e) {
            logger.error("LogisticsServlet PUT error: {}", e.getMessage(), e);
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
                // DELETE /api/logistics/{id} - 删除物流记录
                handleDeleteLogistics(req, resp, pathParts[0]);
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        "API不存在", "API_NOT_FOUND");
            }

            logResponse(req, resp, startTime, resp.getStatus());
        } catch (Exception e) {
            logger.error("LogisticsServlet DELETE error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
            logResponse(req, resp, startTime, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 创建物流信息
     */
    @RequirePermission(
            value = {Permission.ADMIN, Permission.LOGISTICS_MANAGE},
            message = "需要管理员或物流管理权限",
            logic = RequirePermission.LogicType.OR
    )
    private void handleCreateLogistics(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        logger.info("[LOGISTICS_API] Creating logistics record. Client IP: {}", getClientIp(req));

        try {
            // 验证Content-Type
            String contentType = req.getContentType();
            if (contentType == null || !contentType.contains("application/json")) {
                logger.warn("[LOGISTICS_API] Invalid Content-Type: {}", contentType);
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "请求必须是JSON格式", "INVALID_CONTENT_TYPE");
                return;
            }

            // 解析请求体
            LogisticsCreateRequest createRequest = parseJsonBody(req, LogisticsCreateRequest.class);

            // 验证必填字段
            if (createRequest.getOrderId() == null || createRequest.getOrderId().trim().isEmpty()) {
                throw new IllegalArgumentException("订单号不能为空");
            }

            // 调用服务层
            com.apex.core.model.Logistics logistics = logisticsService.createLogistics(createRequest);

            // 发送成功响应
            ApiResponse<com.apex.core.model.Logistics> apiResponse =
                    ApiResponse.success(logistics, "物流信息创建成功");
            sendJsonResponse(resp, HttpServletResponse.SC_CREATED, apiResponse);
            logger.info("[LOGISTICS_API] Logistics record created successfully. ID: {}, Order: {}",
                    logistics.getId(), logistics.getOrderId());

        } catch (IllegalArgumentException e) {
            logger.warn("[LOGISTICS_API] Invalid request: {}", e.getMessage());
            ApiResponse<Void> errorResponse = ApiResponse.error(e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (Exception e) {
            logger.error("[LOGISTICS_API] Failed to create logistics record: {}", e.getMessage(), e);
            ApiResponse<Void> errorResponse = ApiResponse.error("创建物流信息失败");
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorResponse);
        }
    }

    /**
     * 根据ID获取物流详情
     */
    @RequirePermission(
            value = {Permission.ADMIN, Permission.LOGISTICS_MANAGE},
            message = "需要管理员或物流管理权限",
            logic = RequirePermission.LogicType.OR
    )
    private void handleGetLogisticsById(HttpServletRequest req, HttpServletResponse resp, String idStr)
            throws IOException {
        logger.info("[LOGISTICS_API] Getting logistics detail by ID. ID: {}, Client IP: {}",
                idStr, getClientIp(req));

        try {
            // 解析ID
            Integer id = parseId(idStr);

            // 调用服务层
            com.apex.core.model.Logistics logistics = logisticsService.getLogisticsById(id);

            // 发送成功响应
            ApiResponse<com.apex.core.model.Logistics> apiResponse =
                    ApiResponse.success(logistics, "获取物流详情成功");
            sendJsonResponse(resp, HttpServletResponse.SC_OK, apiResponse);
            logger.info("[LOGISTICS_API] Logistics detail retrieved successfully. ID: {}", id);

        } catch (NumberFormatException e) {
            logger.warn("[LOGISTICS_API] Invalid ID format: {}", idStr);
            ApiResponse<Void> errorResponse = ApiResponse.error("物流ID格式不正确");
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (IllegalArgumentException e) {
            logger.warn("[LOGISTICS_API] Invalid request: {}", e.getMessage());
            ApiResponse<Void> errorResponse = ApiResponse.error(e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (Exception e) {
            logger.error("[LOGISTICS_API] Failed to get logistics detail. ID: {}, Error: {}",
                    idStr, e.getMessage(), e);
            ApiResponse<Void> errorResponse = ApiResponse.error("获取物流详情失败");
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorResponse);
        }
    }

    /**
     * 根据订单号获取物流信息
     */
    @RequirePermission(
            value = {Permission.ADMIN, Permission.LOGISTICS_MANAGE},
            message = "需要管理员或物流管理权限",
            logic = RequirePermission.LogicType.OR
    )
    private void handleGetLogisticsByOrder(HttpServletRequest req, HttpServletResponse resp, String orderId)
            throws IOException {
        logger.info("[LOGISTICS_API] Getting logistics by order. Order: {}, Client IP: {}",
                orderId, getClientIp(req));

        try {
            // 调用服务层
            com.apex.core.model.Logistics logistics = logisticsService.getLogisticsByOrder(orderId);

            // 发送成功响应
            ApiResponse<com.apex.core.model.Logistics> apiResponse =
                    ApiResponse.success(logistics, "获取订单物流信息成功");
            sendJsonResponse(resp, HttpServletResponse.SC_OK, apiResponse);
            logger.info("[LOGISTICS_API] Order logistics retrieved successfully. Order: {}", orderId);

        } catch (IllegalArgumentException e) {
            logger.warn("[LOGISTICS_API] Invalid request: {}", e.getMessage());
            ApiResponse<Void> errorResponse = ApiResponse.error(e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (Exception e) {
            logger.error("[LOGISTICS_API] Failed to get order logistics. Order: {}, Error: {}",
                    orderId, e.getMessage(), e);
            ApiResponse<Void> errorResponse = ApiResponse.error("获取订单物流信息失败");
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorResponse);
        }
    }

    /**
     * 获取物流列表（分页）
     */
    @RequirePermission(
            value = {Permission.ADMIN, Permission.LOGISTICS_MANAGE},
            message = "需要管理员或物流管理权限",
            logic = RequirePermission.LogicType.OR
    )
    private void handleGetLogisticsList(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        logger.info("[LOGISTICS_API] Getting logistics list. Client IP: {}", getClientIp(req));

        try {
            // 解析分页参数
            int page = parseIntParameter(req, "page", 1);
            int pageSize = parseIntParameter(req, "pageSize", 20);

            // 调用服务层获取所有物流（简化处理）
            // 实际应根据需要实现按状态筛选等功能
            PagedResult<com.apex.core.model.Logistics> result =
                    logisticsService.getPendingShipping(page, pageSize);

            // 发送成功响应
            ApiResponse<PagedResult<com.apex.core.model.Logistics>> apiResponse =
                    ApiResponse.success(result, "获取物流列表成功");
            sendJsonResponse(resp, HttpServletResponse.SC_OK, apiResponse);
            logger.info("[LOGISTICS_API] Logistics list retrieved successfully. Total: {}, Page: {}",
                    result.getTotalCount(), page);

        } catch (IllegalArgumentException e) {
            logger.warn("[LOGISTICS_API] Invalid parameters: {}", e.getMessage());
            ApiResponse<Void> errorResponse = ApiResponse.error(e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (Exception e) {
            logger.error("[LOGISTICS_API] Failed to get logistics list: {}", e.getMessage(), e);
            ApiResponse<Void> errorResponse = ApiResponse.error("获取物流列表失败");
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorResponse);
        }
    }

    /**
     * 获取待发货列表
     */
    @RequirePermission(
            value = {Permission.ADMIN, Permission.LOGISTICS_MANAGE},
            message = "需要管理员或物流管理权限",
            logic = RequirePermission.LogicType.OR
    )
    private void handleGetPendingShipping(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        logger.info("[LOGISTICS_API] Getting pending shipping list. Client IP: {}", getClientIp(req));

        try {
            // 解析分页参数
            int page = parseIntParameter(req, "page", 1);
            int pageSize = parseIntParameter(req, "pageSize", 20);

            // 调用服务层
            PagedResult<com.apex.core.model.Logistics> result =
                    logisticsService.getPendingShipping(page, pageSize);

            // 发送成功响应
            ApiResponse<PagedResult<com.apex.core.model.Logistics>> apiResponse =
                    ApiResponse.success(result, "获取待发货列表成功");
            sendJsonResponse(resp, HttpServletResponse.SC_OK, apiResponse);
            logger.info("[LOGISTICS_API] Pending shipping list retrieved successfully. Count: {}",
                    result.getData().size());

        } catch (IllegalArgumentException e) {
            logger.warn("[LOGISTICS_API] Invalid parameters: {}", e.getMessage());
            ApiResponse<Void> errorResponse = ApiResponse.error(e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (Exception e) {
            logger.error("[LOGISTICS_API] Failed to get pending shipping list: {}", e.getMessage(), e);
            ApiResponse<Void> errorResponse = ApiResponse.error("获取待发货列表失败");
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorResponse);
        }
    }

    /**
     * 获取运输中列表
     */
    @RequirePermission(
            value = {Permission.ADMIN, Permission.LOGISTICS_MANAGE},
            message = "需要管理员或物流管理权限",
            logic = RequirePermission.LogicType.OR
    )
    private void handleGetInTransit(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        logger.info("[LOGISTICS_API] Getting in-transit list. Client IP: {}", getClientIp(req));

        try {
            // 解析分页参数
            int page = parseIntParameter(req, "page", 1);
            int pageSize = parseIntParameter(req, "pageSize", 20);

            // 调用服务层
            PagedResult<com.apex.core.model.Logistics> result =
                    logisticsService.getInTransit(page, pageSize);

            // 发送成功响应
            ApiResponse<PagedResult<com.apex.core.model.Logistics>> apiResponse =
                    ApiResponse.success(result, "获取运输中列表成功");
            sendJsonResponse(resp, HttpServletResponse.SC_OK, apiResponse);
            logger.info("[LOGISTICS_API] In-transit list retrieved successfully. Count: {}",
                    result.getData().size());

        } catch (IllegalArgumentException e) {
            logger.warn("[LOGISTICS_API] Invalid parameters: {}", e.getMessage());
            ApiResponse<Void> errorResponse = ApiResponse.error(e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (Exception e) {
            logger.error("[LOGISTICS_API] Failed to get in-transit list: {}", e.getMessage(), e);
            ApiResponse<Void> errorResponse = ApiResponse.error("获取运输中列表失败");
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorResponse);
        }
    }

    /**
     * 更新物流信息
     */
    @RequirePermission(
            value = {Permission.ADMIN, Permission.LOGISTICS_MANAGE},
            message = "需要管理员或物流管理权限",
            logic = RequirePermission.LogicType.OR
    )
    private void handleUpdateLogistics(HttpServletRequest req, HttpServletResponse resp, String idStr)
            throws IOException {
        logger.info("[LOGISTICS_API] Updating logistics info. ID: {}, Client IP: {}",
                idStr, getClientIp(req));

        try {
            // 验证Content-Type
            String contentType = req.getContentType();
            if (contentType == null || !contentType.contains("application/json")) {
                logger.warn("[LOGISTICS_API] Invalid Content-Type: {}", contentType);
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "请求必须是JSON格式", "INVALID_CONTENT_TYPE");
                return;
            }

            // 解析请求体
            LogisticsUpdateRequest updateRequest = parseJsonBody(req, LogisticsUpdateRequest.class);

            // 调用服务层
            com.apex.core.model.Logistics logistics = logisticsService.updateLogistics(updateRequest);

            // 发送成功响应
            ApiResponse<com.apex.core.model.Logistics> apiResponse =
                    ApiResponse.success(logistics, "更新物流信息成功");
            sendJsonResponse(resp, HttpServletResponse.SC_OK, apiResponse);
            logger.info("[LOGISTICS_API] Logistics info updated successfully. Order: {}",
                    logistics.getOrderId());

        } catch (NumberFormatException e) {
            logger.warn("[LOGISTICS_API] Invalid ID format: {}", idStr);
            ApiResponse<Void> errorResponse = ApiResponse.error("物流ID格式不正确");
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (IllegalArgumentException e) {
            logger.warn("[LOGISTICS_API] Invalid request: {}", e.getMessage());
            ApiResponse<Void> errorResponse = ApiResponse.error(e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (Exception e) {
            logger.error("[LOGISTICS_API] Failed to update logistics info. ID: {}, Error: {}",
                    idStr, e.getMessage(), e);
            ApiResponse<Void> errorResponse = ApiResponse.error("更新物流信息失败");
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorResponse);
        }
    }

    /**
     * 更新物流状态
     */
    @RequirePermission(
            value = {Permission.ADMIN, Permission.LOGISTICS_MANAGE},
            message = "需要管理员或物流管理权限",
            logic = RequirePermission.LogicType.OR
    )
    private void handleUpdateStatus(HttpServletRequest req, HttpServletResponse resp, String idStr)
            throws IOException {
        logger.info("[LOGISTICS_API] Updating logistics status. ID: {}, Client IP: {}",
                idStr, getClientIp(req));

        try {
            // 验证Content-Type
            String contentType = req.getContentType();
            if (contentType == null || !contentType.contains("application/json")) {
                logger.warn("[LOGISTICS_API] Invalid Content-Type: {}", contentType);
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "请求必须是JSON格式", "INVALID_CONTENT_TYPE");
                return;
            }

            // 解析请求体
            LogisticsStatusUpdateRequest updateRequest = parseJsonBody(req, LogisticsStatusUpdateRequest.class);

            // 验证必填字段
            if (updateRequest.getStatus() == null || updateRequest.getStatus().trim().isEmpty()) {
                throw new IllegalArgumentException("状态不能为空");
            }

            // 获取物流记录以获取订单号
            Integer id = parseId(idStr);
            com.apex.core.model.Logistics logistics = logisticsService.getLogisticsById(id);

            // 调用服务层
            com.apex.core.model.Logistics updatedLogistics =
                    logisticsService.updateLogisticsStatus(logistics.getOrderId(), updateRequest.getStatus());

            // 发送成功响应
            ApiResponse<com.apex.core.model.Logistics> apiResponse =
                    ApiResponse.success(updatedLogistics, "更新物流状态成功");
            sendJsonResponse(resp, HttpServletResponse.SC_OK, apiResponse);
            logger.info("[LOGISTICS_API] Logistics status updated successfully. ID: {}, New status: {}",
                    id, updateRequest.getStatus());

        } catch (NumberFormatException e) {
            logger.warn("[LOGISTICS_API] Invalid ID format: {}", idStr);
            ApiResponse<Void> errorResponse = ApiResponse.error("物流ID格式不正确");
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (IllegalArgumentException e) {
            logger.warn("[LOGISTICS_API] Invalid request: {}", e.getMessage());
            ApiResponse<Void> errorResponse = ApiResponse.error(e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (Exception e) {
            logger.error("[LOGISTICS_API] Failed to update logistics status. ID: {}, Error: {}",
                    idStr, e.getMessage(), e);
            ApiResponse<Void> errorResponse = ApiResponse.error("更新物流状态失败");
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorResponse);
        }
    }

    /**
     * 更新发货信息
     */
    @RequirePermission(
            value = {Permission.ADMIN, Permission.LOGISTICS_MANAGE},
            message = "需要管理员或物流管理权限",
            logic = RequirePermission.LogicType.OR
    )
    private void handleUpdateShippingInfo(HttpServletRequest req, HttpServletResponse resp, String idStr)
            throws IOException {
        logger.info("[LOGISTICS_API] Updating shipping info. ID: {}, Client IP: {}",
                idStr, getClientIp(req));

        try {
            // 验证Content-Type
            String contentType = req.getContentType();
            if (contentType == null || !contentType.contains("application/json")) {
                logger.warn("[LOGISTICS_API] Invalid Content-Type: {}", contentType);
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "请求必须是JSON格式", "INVALID_CONTENT_TYPE");
                return;
            }

            // 解析请求体
            ShippingInfoRequest shippingRequest = parseJsonBody(req, ShippingInfoRequest.class);

            // 获取物流记录以获取订单号
            Integer id = parseId(idStr);
            com.apex.core.model.Logistics logistics = logisticsService.getLogisticsById(id);

            // 确保请求中的订单号与物流记录匹配
            shippingRequest.setOrderId(logistics.getOrderId());

            // 调用服务层
            com.apex.core.model.Logistics updatedLogistics =
                    logisticsService.updateShippingInfo(shippingRequest);

            // 发送成功响应
            ApiResponse<com.apex.core.model.Logistics> apiResponse =
                    ApiResponse.success(updatedLogistics, "更新发货信息成功");
            sendJsonResponse(resp, HttpServletResponse.SC_OK, apiResponse);
            logger.info("[LOGISTICS_API] Shipping info updated successfully. ID: {}, Order: {}",
                    id, logistics.getOrderId());

        } catch (NumberFormatException e) {
            logger.warn("[LOGISTICS_API] Invalid ID format: {}", idStr);
            ApiResponse<Void> errorResponse = ApiResponse.error("物流ID格式不正确");
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (IllegalArgumentException e) {
            logger.warn("[LOGISTICS_API] Invalid request: {}", e.getMessage());
            ApiResponse<Void> errorResponse = ApiResponse.error(e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (Exception e) {
            logger.error("[LOGISTICS_API] Failed to update shipping info. ID: {}, Error: {}",
                    idStr, e.getMessage(), e);
            ApiResponse<Void> errorResponse = ApiResponse.error("更新发货信息失败");
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorResponse);
        }
    }

    /**
     * 删除物流记录
     */
    @RequirePermission(
            value = {Permission.ADMIN, Permission.LOGISTICS_MANAGE},
            message = "需要管理员或物流管理权限",
            logic = RequirePermission.LogicType.OR
    )
    private void handleDeleteLogistics(HttpServletRequest req, HttpServletResponse resp, String idStr)
            throws IOException {
        logger.info("[LOGISTICS_API] Deleting logistics record. ID: {}, Client IP: {}",
                idStr, getClientIp(req));

        try {
            // 解析ID
            Integer id = parseId(idStr);

            // 调用服务层
            boolean success = logisticsService.deleteLogistics(id);

            if (success) {
                ApiResponse<Void> apiResponse = ApiResponse.success(null, "删除物流记录成功");
                sendJsonResponse(resp, HttpServletResponse.SC_OK, apiResponse);
                logger.info("[LOGISTICS_API] Logistics record deleted successfully. ID: {}", id);
            } else {
                ApiResponse<Void> errorResponse = ApiResponse.error("删除物流记录失败");
                sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorResponse);
            }

        } catch (NumberFormatException e) {
            logger.warn("[LOGISTICS_API] Invalid ID format: {}", idStr);
            ApiResponse<Void> errorResponse = ApiResponse.error("物流ID格式不正确");
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (IllegalArgumentException e) {
            logger.warn("[LOGISTICS_API] Invalid request: {}", e.getMessage());
            ApiResponse<Void> errorResponse = ApiResponse.error(e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (Exception e) {
            logger.error("[LOGISTICS_API] Failed to delete logistics record. ID: {}, Error: {}",
                    idStr, e.getMessage(), e);
            ApiResponse<Void> errorResponse = ApiResponse.error("删除物流记录失败");
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorResponse);
        }
    }

    /**
     * 获取物流统计
     */
    @RequirePermission(
            value = {Permission.ADMIN, Permission.LOGISTICS_MANAGE},
            message = "需要管理员或物流管理权限",
            logic = RequirePermission.LogicType.OR
    )
    private void handleGetStats(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        logger.info("[LOGISTICS_API] Getting logistics statistics. Client IP: {}", getClientIp(req));

        try {
            // 调用服务层
            LogisticsStats stats = logisticsService.getLogisticsStats();

            // 发送成功响应
            ApiResponse<LogisticsStats> apiResponse =
                    ApiResponse.success(stats, "获取物流统计成功");
            sendJsonResponse(resp, HttpServletResponse.SC_OK, apiResponse);
            logger.info("[LOGISTICS_API] Logistics statistics retrieved successfully");

        } catch (Exception e) {
            logger.error("[LOGISTICS_API] Failed to get logistics statistics: {}", e.getMessage(), e);
            ApiResponse<Void> errorResponse = ApiResponse.error("获取物流统计失败");
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
                logger.warn("[LOGISTICS_API] Invalid {} parameter: {}", paramName, paramValue);
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
            logger.warn("[LOGISTICS_API] Invalid ID format: {}", idStr);
            throw e;
        }
    }
}
