package com.apex.util;

import com.apex.core.service.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统一权限验证拦截器
 * 只使用@RequirePermission注解进行权限验证
 */
public class PermissionInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(PermissionInterceptor.class);
    private final PermissionService permissionService = new PermissionService();

    /**
     * 权限验证前置处理
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        logger.debug("权限拦截器开始验证: {}", request.getRequestURI());

        // 1. 预检请求直接通过
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 2. 获取Token和用户ID
        String token = extractToken(request);
        if (token == null || !JwtUtil.validateToken(token)) {
            sendUnauthorizedResponse(response, "未提供或无效的Token");
            return false;
        }

        Integer userId = JwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            sendUnauthorizedResponse(response, "无效的Token");
            return false;
        }

        // 3. 获取用户权限信息
        Map<String, Boolean> userPermissions = permissionService.getUserPermissions(userId);
        if (userPermissions == null) {
            sendForbiddenResponse(response, "无法获取用户权限信息");
            return false;
        }

        // 4. 检查方法上的权限注解
        if (handler instanceof Method method) {
            RequirePermission requirePermission = method.getAnnotation(RequirePermission.class);

            if (requirePermission != null) {
                boolean hasAccess = checkPermission(requirePermission, userPermissions);

                if (!hasAccess) {
                    sendForbiddenResponse(response, requirePermission.message());
                    return false;
                }

                logger.debug("权限验证通过，用户ID: {}", userId);
            }
        }

        return true;
    }

    /**
     * 检查权限
     */
    private boolean checkPermission(RequirePermission requirePermission,
                                    Map<String, Boolean> userPermissions) {

        Permission[] requiredPermissions = requirePermission.value();

        // 如果没有指定权限，默认通过
        if (requiredPermissions == null || requiredPermissions.length == 0) {
            logger.debug("没有指定权限要求，默认通过");
            return true;
        }

        // 检查是否为管理员
        boolean isAdmin = Boolean.TRUE.equals(userPermissions.get(Permission.ADMIN.toDbField()));

        // 如果允许管理员且用户是管理员，直接通过
        if (requirePermission.allowAdmin() && isAdmin) {
            logger.debug("管理员用户，跳过权限检查");
            return true;
        }

        // 根据逻辑类型检查权限
        if (requirePermission.logic() == RequirePermission.LogicType.OR) {
            return checkOrPermission(requiredPermissions, userPermissions);
        } else {
            return checkAndPermission(requiredPermissions, userPermissions);
        }
    }

    /**
     * 检查OR逻辑权限（只需要满足其中一个）
     */
    private boolean checkOrPermission(Permission[] requiredPermissions,
                                      Map<String, Boolean> userPermissions) {
        for (Permission permission : requiredPermissions) {
            String dbField = permission.toDbField();
            Boolean hasPermission = userPermissions.get(dbField);

            if (Boolean.TRUE.equals(hasPermission)) {
                logger.debug("用户拥有权限: {}", permission);
                return true;
            }
        }

        String permissionNames = Arrays.stream(requiredPermissions)
                .map(Permission::name)
                .collect(Collectors.joining(", "));
        logger.debug("用户缺少以下任意一种权限: {}", permissionNames);
        return false;
    }

    /**
     * 检查AND逻辑权限（需要满足所有）
     */
    private boolean checkAndPermission(Permission[] requiredPermissions,
                                       Map<String, Boolean> userPermissions) {
        for (Permission permission : requiredPermissions) {
            String dbField = permission.toDbField();
            Boolean hasPermission = userPermissions.get(dbField);

            if (hasPermission == null || !hasPermission) {
                logger.debug("用户缺少权限: {}", permission);
                return false;
            }
        }

        logger.debug("用户拥有所有权限: {}", Arrays.toString(requiredPermissions));
        return true;
    }

    /**
     * 从请求中提取Token
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 从查询参数获取Token
        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.trim().isEmpty()) {
            return tokenParam.trim();
        }

        return null;
    }

    /**
     * 发送未授权响应
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws Exception {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String jsonResponse = String.format(
                "{\"success\": false, \"message\": \"%s\", \"errorCode\": \"UNAUTHORIZED\"}",
                message
        );

        response.getWriter().write(jsonResponse);
    }

    /**
     * 发送禁止访问响应
     */
    private void sendForbiddenResponse(HttpServletResponse response, String message) throws Exception {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        String jsonResponse = String.format(
                "{\"success\": false, \"message\": \"%s\", \"errorCode\": \"FORBIDDEN\"}",
                message
        );

        response.getWriter().write(jsonResponse);
    }
}
