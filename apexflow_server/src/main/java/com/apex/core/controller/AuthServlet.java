package com.apex.core.controller;

import com.apex.api.BaseServlet;
import com.apex.core.dto.LoginRequest;
import com.apex.core.dto.ApiResponse;
import com.apex.core.service.AuthService;
import com.apex.util.RequirePermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 认证相关API接口
 * POST /api/auth/login - 用户登录
 * POST /api/auth/logout - 用户登出
 */
@WebServlet("/api/auth/*")
public class AuthServlet extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(AuthServlet.class);
    private final AuthService authService = new AuthService();

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
                case "/login":
                    handleLogin(req, resp);
                    break;
                case "/logout":
                    handleLogout(req, resp);
                    break;
                default:
                    sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                            "API不存在", "API_NOT_FOUND");
            }

            logResponse(req, resp, startTime, resp.getStatus());
        } catch (Exception e) {
            logger.error("AuthServlet error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
            logResponse(req, resp, startTime, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理用户登录
     */
    @RequirePermission()
    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("[AUTH_LOGIN] Processing login request from IP: {}", getClientIp(req));

        try {
            // 验证Content-Type
            String contentType = req.getContentType();
            if (contentType == null || !contentType.contains("application/json")) {
                logger.warn("[AUTH_LOGIN] Invalid Content-Type: {}", contentType);
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "请求必须是JSON格式", "INVALID_CONTENT_TYPE");
                return;
            }

            // 解析请求体
            LoginRequest loginRequest = parseJsonBody(req, LoginRequest.class);
            logger.debug("[AUTH_LOGIN] Login request data: {}", loginRequest);

            // 调用服务层
            Map<String, Object> result = authService.login(loginRequest);

            // 发送响应
            if (Boolean.TRUE.equals(result.get("success"))) {
                ApiResponse<Map<String, Object>> response = ApiResponse.success(
                        (Map<String, Object>) result.get("data"), "登录成功"
                );
                sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
                logger.info("[AUTH_LOGIN] Login successful for user: {}", loginRequest.getUsername());
            } else {
                ApiResponse<Void> response = ApiResponse.error((String) result.get("message"));
                sendJsonResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, response);
                logger.warn("[AUTH_LOGIN] Login failed for user: {} - {}",
                        loginRequest.getUsername(), result.get("message"));
            }
        } catch (IOException e) {
            logger.error("[AUTH_LOGIN] Failed to parse request body: {}", e.getMessage());
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "请求格式错误", "INVALID_REQUEST_FORMAT");
        } catch (Exception e) {
            logger.error("[AUTH_LOGIN] Unexpected error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
        }
    }

    /**
     * 处理用户登出
     */
    @RequirePermission()
    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("[AUTH_LOGOUT] Processing logout request from IP: {}", getClientIp(req));

        try {
            String authHeader = req.getHeader("Authorization");
            String token = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                logger.debug("[AUTH_LOGOUT] Token provided for logout");
            }

            // 调用服务层
            Map<String, Object> result = authService.logout(token);

            // 发送响应
            ApiResponse<Void> response = ApiResponse.success(null, "登出成功");
            sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
            logger.info("[AUTH_LOGOUT] Logout successful");
        } catch (Exception e) {
            logger.error("[AUTH_LOGOUT] Unexpected error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long startTime = System.currentTimeMillis();
        logRequest(req);
        sendErrorResponse(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "不支持的HTTP方法", "METHOD_NOT_ALLOWED");
        logResponse(req, resp, startTime, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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