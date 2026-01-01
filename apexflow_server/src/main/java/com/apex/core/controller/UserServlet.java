package com.apex.core.controller;

import com.apex.api.BaseServlet;
import com.apex.core.dto.UpdateProfileRequest;
import com.apex.core.dto.ApiResponse;
import com.apex.core.service.UserService;
import com.apex.util.RequirePermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 用户相关API接口
 * GET /api/user/permissions - 获取用户权限
 * PUT /api/user/profile - 修改个人信息
 */
@WebServlet("/api/user/*")
public class UserServlet extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(UserServlet.class);
    private final UserService userService = new UserService();

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

            switch (pathInfo) {
                case "/permissions":
                    handleGetPermissions(req, resp);
                    break;
                default:
                    sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                            "API不存在", "API_NOT_FOUND");
            }

            logResponse(req, resp, startTime, resp.getStatus());
        } catch (Exception e) {
            logger.error("UserServlet error: {}", e.getMessage(), e);
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

            switch (pathInfo) {
                case "/profile":
                    handleUpdateProfile(req, resp);
                    break;
                default:
                    sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                            "API不存在", "API_NOT_FOUND");
            }

            logResponse(req, resp, startTime, resp.getStatus());
        } catch (Exception e) {
            logger.error("UserServlet error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
            logResponse(req, resp, startTime, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理获取用户权限
     */
    @RequirePermission()
    private void handleGetPermissions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("[USER_PERMISSIONS] Processing get permissions request from IP: {}", getClientIp(req));

        // 检查认证头
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("[USER_PERMISSIONS] Missing or invalid Authorization header");
            sendErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    "缺少认证Token", "MISSING_AUTHORIZATION");
            return;
        }

        String token = authHeader.substring(7);
        logger.debug("[USER_PERMISSIONS] Token extracted for permissions request");

        try {
            // 调用服务层
            Map<String, Object> result = userService.getPermissions(token);

            // 发送响应
            if (Boolean.TRUE.equals(result.get("success"))) {
                ApiResponse<Map<String, Object>> response = ApiResponse.success(
                        (Map<String, Object>) result.get("data"), "获取权限成功"
                );
                sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
                logger.info("[USER_PERMISSIONS] Permissions retrieved successfully");
            } else {
                ApiResponse<Void> response = ApiResponse.error((String) result.get("message"));
                sendJsonResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, response);
                logger.warn("[USER_PERMISSIONS] Failed to get permissions: {}", result.get("message"));
            }
        } catch (Exception e) {
            logger.error("[USER_PERMISSIONS] Unexpected error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
        }
    }

    /**
     * 处理修改个人信息
     */
    @RequirePermission()
    private void handleUpdateProfile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("[USER_PROFILE] Processing update profile request from IP: {}", getClientIp(req));

        // 检查认证头
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("[USER_PROFILE] Missing or invalid Authorization header");
            sendErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    "缺少认证Token", "MISSING_AUTHORIZATION");
            return;
        }

        String token = authHeader.substring(7);
        logger.debug("[USER_PROFILE] Token extracted for profile update");

        try {
            // 验证Content-Type
            String contentType = req.getContentType();
            if (contentType == null || !contentType.contains("application/json")) {
                logger.warn("[USER_PROFILE] Invalid Content-Type: {}", contentType);
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "请求必须是JSON格式", "INVALID_CONTENT_TYPE");
                return;
            }

            // 解析请求体
            UpdateProfileRequest updateRequest = parseJsonBody(req, UpdateProfileRequest.class);
            logger.debug("[USER_PROFILE] Update profile request data: {}", updateRequest);

            // 调用服务层
            Map<String, Object> result = userService.updateProfile(token, updateRequest);

            // 发送响应
            if (Boolean.TRUE.equals(result.get("success"))) {
                ApiResponse<Map<String, Object>> response = ApiResponse.success(
                        (Map<String, Object>) result.get("data"), "个人信息更新成功"
                );
                sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
                logger.info("[USER_PROFILE] Profile updated successfully");
            } else {
                ApiResponse<Void> response = ApiResponse.error((String) result.get("message"));
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, response);
                logger.warn("[USER_PROFILE] Failed to update profile: {}", result.get("message"));
            }
        } catch (IOException e) {
            logger.error("[USER_PROFILE] Failed to parse request body: {}", e.getMessage());
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "请求格式错误", "INVALID_REQUEST_FORMAT");
        } catch (Exception e) {
            logger.error("[USER_PROFILE] Unexpected error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long startTime = System.currentTimeMillis();
        logRequest(req);
        sendErrorResponse(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "不支持的HTTP方法", "METHOD_NOT_ALLOWED");
        logResponse(req, resp, startTime, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long startTime = System.currentTimeMillis();
        logRequest(req);
        sendErrorResponse(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "不支持的HTTP方法", "METHOD_NOT_ALLOWED");
        logResponse(req, resp, startTime, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}
