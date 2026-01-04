package com.apex.core.controller;

import com.apex.api.BaseServlet;
import com.apex.core.dto.CreateIncomeRequest;
import com.apex.core.dto.UpdateIncomeRequest;
import com.apex.core.dto.ApiResponse;
import com.apex.core.service.IncomeService;
import com.apex.util.RequirePermission;
import com.apex.util.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 财务收支API接口
 *
 * API列表:
 * POST   /api/income            - 创建财务记录
 * GET    /api/income/{id}       - 获取财务记录详情
 * GET    /api/income/list       - 获取财务记录列表（支持分页和筛选）
 * PUT    /api/income/{id}       - 更新财务记录
 * DELETE /api/income/{id}       - 删除财务记录
 * PUT    /api/income/{id}/status - 更新财务状态
 * GET    /api/income/statistics - 获取财务统计
 * GET    /api/income/order/{orderId} - 根据订单号查询财务记录
 */
@WebServlet("/api/income/*")
public class IncomeServlet extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(IncomeServlet.class);
    private final IncomeService incomeService = new IncomeService();

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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

            if (pathInfo.equals("/list")) {
                handleListIncomes(req, resp);
            } else if (pathInfo.equals("/statistics")) {
                handleGetStatistics(req, resp);
            } else if (pathInfo.startsWith("/order/")) {
                handleGetByOrderId(req, resp);
            } else {
                // 尝试解析ID
                try {
                    String idStr = pathInfo.substring(1); // 去掉开头的"/"
                    Integer id = Integer.parseInt(idStr);
                    handleGetIncomeDetail(req, resp, id);
                } catch (NumberFormatException e) {
                    sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                            "API不存在", "API_NOT_FOUND");
                }
            }

            logResponse(req, resp, startTime, resp.getStatus());
        } catch (Exception e) {
            logger.error("IncomeServlet GET error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
            logResponse(req, resp, startTime, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long startTime = System.currentTimeMillis();
        String pathInfo = req.getPathInfo();

        try {
            logRequest(req);

            if (pathInfo != null && !pathInfo.isEmpty() && !pathInfo.equals("/")) {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        "API路径不正确", "API_PATH_INVALID");
                logResponse(req, resp, startTime, HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            handleCreateIncome(req, resp);
            logResponse(req, resp, startTime, resp.getStatus());
        } catch (Exception e) {
            logger.error("IncomeServlet POST error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
            logResponse(req, resp, startTime, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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

            // 检查是否是状态更新
            if (pathInfo.contains("/status")) {
                try {
                    String[] parts = pathInfo.split("/");
                    Integer id = Integer.parseInt(parts[1]);
                    handleUpdateIncomeStatus(req, resp, id);
                } catch (Exception e) {
                    sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                            "ID格式不正确", "INVALID_ID_FORMAT");
                }
            } else {
                try {
                    String idStr = pathInfo.substring(1); // 去掉开头的"/"
                    Integer id = Integer.parseInt(idStr);
                    handleUpdateIncome(req, resp, id);
                } catch (NumberFormatException e) {
                    sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                            "API不存在", "API_NOT_FOUND");
                }
            }

            logResponse(req, resp, startTime, resp.getStatus());
        } catch (Exception e) {
            logger.error("IncomeServlet PUT error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
            logResponse(req, resp, startTime, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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

            try {
                String idStr = pathInfo.substring(1); // 去掉开头的"/"
                Integer id = Integer.parseInt(idStr);
                handleDeleteIncome(req, resp, id);
            } catch (NumberFormatException e) {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        "API不存在", "API_NOT_FOUND");
            }

            logResponse(req, resp, startTime, resp.getStatus());
        } catch (Exception e) {
            logger.error("IncomeServlet DELETE error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
            logResponse(req, resp, startTime, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理创建财务记录
     */
    @RequirePermission(value = {Permission.ADMIN, Permission.INCOME_MANAGE},
            logic = RequirePermission.LogicType.OR,
            message = "需要管理员或财务管理权限")
    private void handleCreateIncome(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("[INCOME_CREATE] Processing create income request from IP: {}", getClientIp(req));

        String token = extractToken(req);
        if (token == null) {
            sendErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    "缺少认证Token", "MISSING_AUTHORIZATION");
            return;
        }

        try {
            // 验证Content-Type
            if (!isJsonContentType(req)) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "请求必须是JSON格式", "INVALID_CONTENT_TYPE");
                return;
            }

            // 解析请求体
            CreateIncomeRequest createRequest = parseJsonBody(req, CreateIncomeRequest.class);
            logger.debug("[INCOME_CREATE] Create income request data: {}", createRequest);

            // 调用服务层
            Map<String, Object> result = incomeService.createIncome(token, createRequest);

            // 发送响应
            if (Boolean.TRUE.equals(result.get("success"))) {
                ApiResponse<Map<String, Object>> response = ApiResponse.success(
                        (Map<String, Object>) result.get("data"), "财务记录创建成功"
                );
                sendJsonResponse(resp, HttpServletResponse.SC_CREATED, response);
                logger.info("[INCOME_CREATE] Income record created successfully");
            } else {
                String message = (String) result.get("message");
                Map<String, String> errors = (Map<String, String>) result.get("errors");

                ApiResponse<Map<String, String>> response = ApiResponse.error(message);
                if (errors != null) {
                    response.setData(errors);
                }

                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, response);
                logger.warn("[INCOME_CREATE] Failed to create income: {}", message);
            }
        } catch (IOException e) {
            logger.error("[INCOME_CREATE] Failed to parse request body: {}", e.getMessage());
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "请求格式错误", "INVALID_REQUEST_FORMAT");
        } catch (Exception e) {
            logger.error("[INCOME_CREATE] Unexpected error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
        }
    }

    /**
     * 处理获取财务记录详情
     */
    @RequirePermission(value = {Permission.ADMIN, Permission.INCOME_MANAGE},
            logic = RequirePermission.LogicType.OR,
            message = "需要管理员或财务管理权限")
    private void handleGetIncomeDetail(HttpServletRequest req, HttpServletResponse resp, Integer id) throws IOException {
        logger.info("[INCOME_DETAIL] Processing get income detail request. ID: {}", id);

        // 调用服务层
        Map<String, Object> result = incomeService.getIncomeDetail(id);

        // 发送响应
        if (Boolean.TRUE.equals(result.get("success"))) {
            ApiResponse<Map<String, Object>> response = ApiResponse.success(
                    (Map<String, Object>) result.get("data"), "财务记录详情获取成功"
            );
            sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
            logger.info("[INCOME_DETAIL] Income detail retrieved successfully for ID: {}", id);
        } else {
            String message = (String) result.get("message");
            ApiResponse<Void> response = ApiResponse.error(message);
            sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND, response);
            logger.warn("[INCOME_DETAIL] Failed to get income detail: {}", message);
        }
    }

    /**
     * 处理获取财务记录列表
     */
    @RequirePermission(value = {Permission.ADMIN, Permission.INCOME_MANAGE},
            logic = RequirePermission.LogicType.OR,
            message = "需要管理员或财务管理权限")
    private void handleListIncomes(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("[INCOME_LIST] Processing list incomes request from IP: {}", getClientIp(req));

        try {
            // 获取查询参数
            String type = req.getParameter("type");
            String statusStr = req.getParameter("status");
            String pageStr = req.getParameter("page");
            String pageSizeStr = req.getParameter("pageSize");

            Integer status = null;
            Integer page = null;
            Integer pageSize = null;

            try {
                if (statusStr != null && !statusStr.trim().isEmpty()) {
                    status = Integer.parseInt(statusStr);
                }
                if (pageStr != null && !pageStr.trim().isEmpty()) {
                    page = Integer.parseInt(pageStr);
                }
                if (pageSizeStr != null && !pageSizeStr.trim().isEmpty()) {
                    pageSize = Integer.parseInt(pageSizeStr);
                }
            } catch (NumberFormatException e) {
                logger.warn("[INCOME_LIST] Invalid number format in query parameters");
                ApiResponse<Void> response = ApiResponse.error("查询参数格式错误");
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, response);
                return;
            }

            // 设置默认值
            if (page == null || page < 1) page = 1;
            if (pageSize == null || pageSize < 1) pageSize = 20;
            if (pageSize > 100) pageSize = 100; // 限制每页最大记录数

            // 调用服务层
            Map<String, Object> result = incomeService.listIncomes(type, status, page, pageSize);

            // 发送响应
            if (Boolean.TRUE.equals(result.get("success"))) {
                ApiResponse<Map<String, Object>> response = ApiResponse.success(
                        (Map<String, Object>) result.get("data"), "财务记录列表获取成功"
                );
                sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
                logger.info("[INCOME_LIST] Income list retrieved successfully");
            } else {
                String message = (String) result.get("message");
                if (message == null) {
                    message = "查询财务记录列表失败";
                }
                ApiResponse<Void> response = ApiResponse.error(message);
                sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
                logger.warn("[INCOME_LIST] Failed to list incomes: {}", message);
            }
        } catch (Exception e) {
            logger.error("[INCOME_LIST] Unexpected error: {}", e.getMessage(), e);
            // 修复：确保错误消息不为null
            ApiResponse<Void> response = ApiResponse.error("查询财务记录列表失败");
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
        }
    }

    /**
     * 处理更新财务记录
     */
    @RequirePermission(value = {Permission.ADMIN, Permission.INCOME_MANAGE},
            logic = RequirePermission.LogicType.OR,
            message = "需要管理员或财务管理权限")
    private void handleUpdateIncome(HttpServletRequest req, HttpServletResponse resp, Integer id) throws IOException {
        logger.info("[INCOME_UPDATE] Processing update income request. ID: {}", id);

        String token = extractToken(req);
        if (token == null) {
            sendErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    "缺少认证Token", "MISSING_AUTHORIZATION");
            return;
        }

        try {
            // 验证Content-Type
            if (!isJsonContentType(req)) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "请求必须是JSON格式", "INVALID_CONTENT_TYPE");
                return;
            }

            // 解析请求体
            UpdateIncomeRequest updateRequest = parseJsonBody(req, UpdateIncomeRequest.class);
            logger.debug("[INCOME_UPDATE] Update income request data: {}", updateRequest);

            // 调用服务层
            Map<String, Object> result = incomeService.updateIncome(token, id, updateRequest);

            // 发送响应
            if (Boolean.TRUE.equals(result.get("success"))) {
                ApiResponse<Map<String, Object>> response = ApiResponse.success(
                        (Map<String, Object>) result.get("data"), "财务记录更新成功"
                );
                sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
                logger.info("[INCOME_UPDATE] Income record updated successfully for ID: {}", id);
            } else {
                String message = (String) result.get("message");
                ApiResponse<Void> response = ApiResponse.error(message);
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, response);
                logger.warn("[INCOME_UPDATE] Failed to update income: {}", message);
            }
        } catch (IOException e) {
            logger.error("[INCOME_UPDATE] Failed to parse request body: {}", e.getMessage());
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "请求格式错误", "INVALID_REQUEST_FORMAT");
        } catch (Exception e) {
            logger.error("[INCOME_UPDATE] Unexpected error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
        }
    }

    /**
     * 处理删除财务记录
     */
    @RequirePermission(value = {Permission.ADMIN, Permission.INCOME_MANAGE},
            logic = RequirePermission.LogicType.OR,
            message = "需要管理员或财务管理权限")
    private void handleDeleteIncome(HttpServletRequest req, HttpServletResponse resp, Integer id) throws IOException {
        logger.info("[INCOME_DELETE] Processing delete income request. ID: {}", id);

        String token = extractToken(req);
        if (token == null) {
            sendErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    "缺少认证Token", "MISSING_AUTHORIZATION");
            return;
        }

        try {
            // 调用服务层
            Map<String, Object> result = incomeService.deleteIncome(token, id);

            // 发送响应
            if (Boolean.TRUE.equals(result.get("success"))) {
                ApiResponse<Void> response = ApiResponse.success(null, "财务记录删除成功");
                sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
                logger.info("[INCOME_DELETE] Income record deleted successfully for ID: {}", id);
            } else {
                String message = (String) result.get("message");
                ApiResponse<Void> response = ApiResponse.error(message);
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, response);
                logger.warn("[INCOME_DELETE] Failed to delete income: {}", message);
            }
        } catch (Exception e) {
            logger.error("[INCOME_DELETE] Unexpected error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
        }
    }

    /**
     * 处理更新财务状态
     */
    @RequirePermission(value = {Permission.ADMIN, Permission.INCOME_MANAGE},
            logic = RequirePermission.LogicType.OR,
            message = "需要管理员或财务管理权限")
    private void handleUpdateIncomeStatus(HttpServletRequest req, HttpServletResponse resp, Integer id) throws IOException {
        logger.info("[INCOME_STATUS] Processing update income status request. ID: {}", id);

        String token = extractToken(req);
        if (token == null) {
            sendErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    "缺少认证Token", "MISSING_AUTHORIZATION");
            return;
        }

        try {
            // 解析请求体获取状态值
            Map<String, Object> requestBody = parseJsonBody(req, Map.class);
            Integer status = (Integer) requestBody.get("status");

            if (status == null) {
                ApiResponse<Void> response = ApiResponse.error("状态值不能为空");
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, response);
                return;
            }

            // 调用服务层
            Map<String, Object> result = incomeService.updateIncomeStatus(token, id, status);

            // 发送响应
            if (Boolean.TRUE.equals(result.get("success"))) {
                ApiResponse<Void> response = ApiResponse.success(null, "财务状态更新成功");
                sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
                logger.info("[INCOME_STATUS] Income status updated successfully for ID: {}", id);
            } else {
                String message = (String) result.get("message");
                ApiResponse<Void> response = ApiResponse.error(message);
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, response);
                logger.warn("[INCOME_STATUS] Failed to update income status: {}", message);
            }
        } catch (IOException e) {
            logger.error("[INCOME_STATUS] Failed to parse request body: {}", e.getMessage());
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "请求格式错误", "INVALID_REQUEST_FORMAT");
        } catch (Exception e) {
            logger.error("[INCOME_STATUS] Unexpected error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
        }
    }

    /**
     * 处理获取财务统计
     */
    @RequirePermission(value = {Permission.ADMIN, Permission.INCOME_MANAGE},
            logic = RequirePermission.LogicType.OR,
            message = "需要管理员或财务管理权限")
    private void handleGetStatistics(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("[INCOME_STATISTICS] Processing get income statistics request");

        try {
            // 调用服务层
            Map<String, Object> result = incomeService.getStatistics();

            // 发送响应
            if (Boolean.TRUE.equals(result.get("success"))) {
                ApiResponse<Map<String, Object>> response = ApiResponse.success(
                        (Map<String, Object>) result.get("data"), "财务统计获取成功"
                );
                sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
                logger.info("[INCOME_STATISTICS] Income statistics retrieved successfully");
            } else {
                String message = (String) result.get("message");
                ApiResponse<Void> response = ApiResponse.error(message);
                sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
                logger.warn("[INCOME_STATISTICS] Failed to get income statistics: {}", message);
            }
        } catch (Exception e) {
            logger.error("[INCOME_STATISTICS] Unexpected error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
        }
    }

    /**
     * 处理根据订单号查询财务记录
     */
    @RequirePermission(value = {Permission.ADMIN, Permission.INCOME_MANAGE},
            logic = RequirePermission.LogicType.OR,
            message = "需要管理员或财务管理权限")
    private void handleGetByOrderId(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("[INCOME_ORDER] Processing get incomes by order ID request");

        try {
            // 从路径中提取订单号
            String pathInfo = req.getPathInfo();
            String orderId = pathInfo.substring(7); // 去掉"/order/"

            if (orderId.trim().isEmpty()) {
                ApiResponse<Void> response = ApiResponse.error("订单号不能为空");
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, response);
                return;
            }

            // 调用服务层
            Map<String, Object> result = incomeService.getIncomesByOrderId(orderId);

            // 发送响应
            if (Boolean.TRUE.equals(result.get("success"))) {
                ApiResponse<Object> response = ApiResponse.success(
                        result.get("data"), "订单财务记录获取成功"
                );
                sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
                logger.info("[INCOME_ORDER] Incomes retrieved successfully for order: {}", orderId);
            } else {
                String message = (String) result.get("message");
                ApiResponse<Void> response = ApiResponse.error(message);
                sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND, response);
                logger.warn("[INCOME_ORDER] Failed to get incomes by order ID: {}", message);
            }
        } catch (Exception e) {
            logger.error("[INCOME_ORDER] Unexpected error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
        }
    }

    /**
     * 从请求中提取Token
     */
    private String extractToken(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * 检查请求是否为JSON格式
     */
    private boolean isJsonContentType(HttpServletRequest req) {
        String contentType = req.getContentType();
        return contentType != null && contentType.contains("application/json");
    }
}
