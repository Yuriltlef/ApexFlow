package com.apex.core.controller;

import com.apex.api.BaseServlet;
import com.apex.core.dto.*;
import com.apex.core.service.AdminService;
import com.apex.util.Permission;
import com.apex.util.RequirePermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 管理员API接口
 * GET /api/admin/users - 获取用户列表
 * PUT /api/admin/users/{id} - 更新用户信息
 * PUT /api/admin/users/{id}/permissions - 更新用户权限
 * PUT /api/admin/users/{id}/password - 重置用户密码
 * GET /api/admin/users/search - 搜索用户
 */
@WebServlet("/api/admin/*")
public class AdminServlet extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(AdminServlet.class);
    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
                case "/users":
                    handleGetUserList(req, resp);
                    break;
                case "/users/search":
                    handleSearchUsers(req, resp);
                    break;
                default:
                    sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                            "API不存在", "API_NOT_FOUND");
            }

            logResponse(req, resp, startTime, resp.getStatus());
        } catch (Exception e) {
            logger.error("AdminServlet GET error: {}", e.getMessage(), e);
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
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length < 3) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "API路径参数不正确", "INVALID_PATH_PARAMETER");
                return;
            }

            Integer userId = parseUserId(pathParts[2]);
            if (userId == null) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "用户ID无效", "INVALID_USER_ID");
                return;
            }

            if (pathParts.length == 3) {
                // /api/admin/users/{id}
                handleUpdateUserInfo(req, resp, userId);
            } else if (pathParts.length == 4) {
                switch (pathParts[3]) {
                    case "permissions":
                        handleUpdateUserPermissions(req, resp, userId);
                        break;
                    case "password":
                        handleResetUserPassword(req, resp, userId);
                        break;
                    default:
                        sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                                "API不存在", "API_NOT_FOUND");
                }
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        "API不存在", "API_NOT_FOUND");
            }

            logResponse(req, resp, startTime, resp.getStatus());
        } catch (Exception e) {
            logger.error("AdminServlet PUT error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
            logResponse(req, resp, startTime, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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

    /**
     * 处理获取用户列表请求
     */
    @RequirePermission(value = Permission.ADMIN, message = "需要管理员权限")
    private void handleGetUserList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("[ADMIN_API] Getting user list. Client IP: {}", getClientIp(req));

        try {
            // 解析分页参数
            int page = parseIntParameter(req, "page", 1);
            int pageSize = parseIntParameter(req, "pageSize", 20);

            // 调用服务层
            AdminUserListResponse response = adminService.getUserList(page, pageSize);

            // 发送成功响应
            ApiResponse<AdminUserListResponse> apiResponse = ApiResponse.success(response, "获取用户列表成功");
            sendJsonResponse(resp, HttpServletResponse.SC_OK, apiResponse);
            logger.info("[ADMIN_API] User list retrieved successfully. Total users: {}",
                    response.getTotalCount());

        } catch (IllegalArgumentException e) {
            logger.warn("[ADMIN_API] Invalid parameters: {}", e.getMessage());
            ApiResponse<Void> errorResponse = ApiResponse.error(e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (Exception e) {
            logger.error("[ADMIN_API] Failed to get user list: {}", e.getMessage(), e);
            ApiResponse<Void> errorResponse = ApiResponse.error("获取用户列表失败");
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorResponse);
        }
    }

    /**
     * 处理搜索用户请求
     */
    @RequirePermission(value = Permission.ADMIN, message = "需要管理员权限")
    private void handleSearchUsers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String keyword = req.getParameter("keyword");
        logger.info("[ADMIN_API] Searching users. Keyword: {}, Client IP: {}", keyword, getClientIp(req));

        try {
            // 解析分页参数
            int page = parseIntParameter(req, "page", 1);
            int pageSize = parseIntParameter(req, "pageSize", 20);

            // 调用服务层
            AdminUserListResponse response = adminService.searchUsers(keyword, page, pageSize);

            // 发送成功响应
            ApiResponse<AdminUserListResponse> apiResponse = ApiResponse.success(response, "搜索用户成功");
            sendJsonResponse(resp, HttpServletResponse.SC_OK, apiResponse);
            logger.info("[ADMIN_API] User search completed. Keyword: {}, Found {} users",
                    keyword, response.getUsers().size());

        } catch (IllegalArgumentException e) {
            logger.warn("[ADMIN_API] Invalid parameters: {}", e.getMessage());
            ApiResponse<Void> errorResponse = ApiResponse.error(e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (Exception e) {
            logger.error("[ADMIN_API] Failed to search users: {}", e.getMessage(), e);
            ApiResponse<Void> errorResponse = ApiResponse.error("搜索用户失败");
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorResponse);
        }
    }

    /**
     * 处理更新用户信息请求
     */
    @RequirePermission(value = Permission.ADMIN, message = "需要管理员权限")
    private void handleUpdateUserInfo(HttpServletRequest req, HttpServletResponse resp, Integer userId)
            throws IOException {
        logger.info("[ADMIN_API] Updating user info. User ID: {}, Client IP: {}", userId, getClientIp(req));

        try {
            // 验证Content-Type
            String contentType = req.getContentType();
            if (contentType == null || !contentType.contains("application/json")) {
                logger.warn("[ADMIN_API] Invalid Content-Type: {}", contentType);
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "请求必须是JSON格式", "INVALID_CONTENT_TYPE");
                return;
            }

            // 解析请求体
            UpdateUserInfoRequest updateRequest = parseJsonBody(req, UpdateUserInfoRequest.class);
            logger.debug("[ADMIN_API] Update user info request: User ID: {}, Data: {}", userId, updateRequest);

            // 调用服务层
            AdminUserDTO updatedUser = adminService.updateUserInfo(userId, updateRequest);

            // 发送成功响应
            ApiResponse<AdminUserDTO> apiResponse = ApiResponse.success(updatedUser, "更新用户信息成功");
            sendJsonResponse(resp, HttpServletResponse.SC_OK, apiResponse);
            logger.info("[ADMIN_API] User info updated successfully. User ID: {}", userId);

        } catch (IllegalArgumentException e) {
            logger.warn("[ADMIN_API] Invalid request: {}", e.getMessage());
            ApiResponse<Void> errorResponse = ApiResponse.error(e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (Exception e) {
            logger.error("[ADMIN_API] Failed to update user info. User ID: {}, Error: {}",
                    userId, e.getMessage(), e);
            ApiResponse<Void> errorResponse = ApiResponse.error("更新用户信息失败");
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorResponse);
        }
    }

    /**
     * 处理更新用户权限请求
     */
    @RequirePermission(value = Permission.ADMIN, message = "需要管理员权限")
    private void handleUpdateUserPermissions(HttpServletRequest req, HttpServletResponse resp, Integer userId)
            throws IOException {
        logger.info("[ADMIN_API] Updating user permissions. User ID: {}, Client IP: {}",
                userId, getClientIp(req));

        try {
            // 验证Content-Type
            String contentType = req.getContentType();
            if (contentType == null || !contentType.contains("application/json")) {
                logger.warn("[ADMIN_API] Invalid Content-Type: {}", contentType);
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "请求必须是JSON格式", "INVALID_CONTENT_TYPE");
                return;
            }

            // 解析请求体
            UpdateUserPermissionsRequest permissionsRequest = parseJsonBody(req, UpdateUserPermissionsRequest.class);
            logger.debug("[ADMIN_API] Update permissions request: User ID: {}, Data: {}",
                    userId, permissionsRequest);

            // 调用服务层
            UserPermissionsDTO updatedPermissions = adminService.updateUserPermissions(userId, permissionsRequest);

            // 发送成功响应
            ApiResponse<UserPermissionsDTO> apiResponse = ApiResponse.success(updatedPermissions, "更新用户权限成功");
            sendJsonResponse(resp, HttpServletResponse.SC_OK, apiResponse);
            logger.info("[ADMIN_API] User permissions updated successfully. User ID: {}", userId);

        } catch (IllegalArgumentException e) {
            logger.warn("[ADMIN_API] Invalid request: {}", e.getMessage());
            ApiResponse<Void> errorResponse = ApiResponse.error(e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (Exception e) {
            logger.error("[ADMIN_API] Failed to update user permissions. User ID: {}, Error: {}",
                    userId, e.getMessage(), e);
            ApiResponse<Void> errorResponse = ApiResponse.error("更新用户权限失败");
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorResponse);
        }
    }

    /**
     * 处理重置用户密码请求
     */
    @RequirePermission(value = Permission.ADMIN, message = "需要管理员权限")
    private void handleResetUserPassword(HttpServletRequest req, HttpServletResponse resp, Integer userId)
            throws IOException {
        logger.info("[ADMIN_API] Resetting user password. User ID: {}, Client IP: {}",
                userId, getClientIp(req));

        try {
            // 验证Content-Type
            String contentType = req.getContentType();
            if (contentType == null || !contentType.contains("application/json")) {
                logger.warn("[ADMIN_API] Invalid Content-Type: {}", contentType);
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "请求必须是JSON格式", "INVALID_CONTENT_TYPE");
                return;
            }

            // 解析请求体
            ResetPasswordRequest passwordRequest = parseJsonBody(req, ResetPasswordRequest.class);
            logger.debug("[ADMIN_API] Reset password request: User ID: {}", userId);

            // 调用服务层
            Map<String, Object> result = adminService.resetUserPassword(userId, passwordRequest);

            // 发送成功响应
            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success(result, "重置密码成功");
            sendJsonResponse(resp, HttpServletResponse.SC_OK, apiResponse);
            logger.info("[ADMIN_API] User password reset successfully. User ID: {}", userId);

        } catch (IllegalArgumentException e) {
            logger.warn("[ADMIN_API] Invalid request: {}", e.getMessage());
            ApiResponse<Void> errorResponse = ApiResponse.error(e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
        } catch (Exception e) {
            logger.error("[ADMIN_API] Failed to reset user password. User ID: {}, Error: {}",
                    userId, e.getMessage(), e);
            ApiResponse<Void> errorResponse = ApiResponse.error("重置密码失败");
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
                logger.warn("[ADMIN_API] Invalid {} parameter: {}", paramName, paramValue);
            }
        }
        return defaultValue;
    }

    /**
     * 解析用户ID
     */
    private Integer parseUserId(String userIdStr) {
        try {
            return Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            logger.warn("[ADMIN_API] Invalid user ID format: {}", userIdStr);
            return null;
        }
    }
}