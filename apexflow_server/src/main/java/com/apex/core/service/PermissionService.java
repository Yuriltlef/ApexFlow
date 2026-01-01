package com.apex.core.service;

import com.apex.util.Permission;
import com.apex.core.dao.UserDAO;
import com.apex.core.model.SystemUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一权限服务
 */
public class PermissionService {

    private static final Logger logger = LoggerFactory.getLogger(PermissionService.class);
    private final UserDAO userDAO = new UserDAO();

    /**
     * 获取用户所有权限信息
     */
    public Map<String, Boolean> getUserPermissions(Integer userId) {
        logger.debug("获取用户权限信息，用户ID: {}", userId);

        SystemUser user = userDAO.getPermissions(userId);
        if (user == null) {
            logger.error("用户不存在或已被禁用，用户ID: {}", userId);
            return null;
        }

        Map<String, Boolean> permissions = new HashMap<>();

        // 管理员权限
        permissions.put(Permission.ADMIN.toDbField(), user.getAdmin());

        // 业务权限
        permissions.put(Permission.ORDER_MANAGE.toDbField(), user.getCanManageOrder());
        permissions.put(Permission.LOGISTICS_MANAGE.toDbField(), user.getCanManageLogistics());
        permissions.put(Permission.AFTER_SALES_MANAGE.toDbField(), user.getCanManageAfterSales());
        permissions.put(Permission.REVIEW_MANAGE.toDbField(), user.getCanManageReview());
        permissions.put(Permission.INVENTORY_MANAGE.toDbField(), user.getCanManageInventory());
        permissions.put(Permission.INCOME_MANAGE.toDbField(), user.getCanManageIncome());

        // 扩展权限（如果有的话）
        // permissions.put(PermissionType.SYSTEM_CONFIG.toDbField(), user.getCanSystemConfig());
        // permissions.put(PermissionType.USER_MANAGE.toDbField(), user.getCanManageUser());

        logger.debug("用户权限信息: {}", permissions);
        return permissions;
    }

    /**
     * 检查用户是否拥有指定权限
     */
    public boolean hasPermission(Integer userId, Permission permissionType) {
        Map<String, Boolean> permissions = getUserPermissions(userId);
        if (permissions == null) {
            return false;
        }

        Boolean hasPermission = permissions.get(permissionType.toDbField());
        return Boolean.TRUE.equals(hasPermission);
    }

    /**
     * 检查用户是否拥有所有指定权限
     */
    public boolean hasAllPermissions(Integer userId, Permission[] permissionTypes) {
        if (permissionTypes == null || permissionTypes.length == 0) {
            return true;
        }

        Map<String, Boolean> permissions = getUserPermissions(userId);
        if (permissions == null) {
            return false;
        }

        for (Permission permissionType : permissionTypes) {
            Boolean hasPermission = permissions.get(permissionType.toDbField());
            if (hasPermission == null || !hasPermission) {
                return false;
            }
        }

        return true;
    }

    /**
     * 检查用户是否拥有任意一个指定权限
     */
    public boolean hasAnyPermission(Integer userId, Permission[] permissionTypes) {
        if (permissionTypes == null || permissionTypes.length == 0) {
            return true;
        }

        Map<String, Boolean> permissions = getUserPermissions(userId);
        if (permissions == null) {
            return false;
        }

        for (Permission permissionType : permissionTypes) {
            Boolean hasPermission = permissions.get(permissionType.toDbField());
            if (Boolean.TRUE.equals(hasPermission)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查用户是否为管理员
     */
    public boolean isAdmin(Integer userId) {
        return hasPermission(userId, Permission.ADMIN);
    }
}