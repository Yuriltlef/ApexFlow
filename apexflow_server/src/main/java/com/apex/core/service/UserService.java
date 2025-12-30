package com.apex.core.service;

import com.apex.core.dao.IUserDAO;
import com.apex.core.dao.UserDAO;
import com.apex.core.model.SystemUser;
import com.apex.util.JwtTokenUtil;
import com.apex.util.PasswordUtil;
import com.apex.util.Permission;
import com.apex.util.PermissionUtil;

import java.util.HashMap;
import java.util.Map;

public class UserService {

    private final IUserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    /**
     * 用户登录
     */
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> result = new HashMap<>();

        // 验证输入
        if (username == null || username.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "用户名不能为空");
            return result;
        }

        if (password == null || password.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "密码不能为空");
            return result;
        }

        // 查找用户
        SystemUser user = userDAO.findByUsername(username);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }

        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            result.put("success", false);
            result.put("message", "用户已被禁用，请联系管理员");
            return result;
        }

        // 验证密码
        String storedHash = user.getPasswordHash();
        String salt = user.getSalt();

        if (!PasswordUtil.verifyPassword(password, salt, storedHash)) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }

        // 更新最后登录时间
        userDAO.updateLastLoginTime(user.getId());

        // 生成JWT令牌
        String token = JwtTokenUtil.generateToken(user);

        // 获取用户权限
        Map<String, Boolean> permissions = extractPermissions(user);

        // 返回结果
        result.put("success", true);
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("permissions", permissions);
        result.put("isAdmin", user.getAdmin() != null && user.getAdmin());

        return result;
    }

    /**
     * 提取用户权限
     */
    private Map<String, Boolean> extractPermissions(SystemUser user) {
        Map<String, Boolean> permissions = new HashMap<>();

        if (user.getAdmin() != null && user.getAdmin()) {
            // 管理员拥有所有权限
            permissions.put("isAdmin", true);
            permissions.put("canManageOrder", true);
            permissions.put("canManageLogistics", true);
            permissions.put("canManageAfterSales", true);
            permissions.put("canManageReview", true);
            permissions.put("canManageInventory", true);
            permissions.put("canManageIncome", true);
        } else {
            // 普通用户根据具体权限设置
            permissions.put("isAdmin", false);
            permissions.put("canManageOrder", user.getCanManageOrder() != null && user.getCanManageOrder());
            permissions.put("canManageLogistics", user.getCanManageLogistics() != null && user.getCanManageLogistics());
            permissions.put("canManageAfterSales", user.getCanManageAfterSales() != null && user.getCanManageAfterSales());
            permissions.put("canManageReview", user.getCanManageReview() != null && user.getCanManageReview());
            permissions.put("canManageInventory", user.getCanManageInventory() != null && user.getCanManageInventory());
            permissions.put("canManageIncome", user.getCanManageIncome() != null && user.getCanManageIncome());
        }

        return permissions;
    }
    /**
     * 验证用户权限
     */
    public boolean checkPermission(Integer userId, Permission permission) {
        SystemUser user = userDAO.findById(userId);
        if (user == null) return false;

        return PermissionUtil.hasPermission(user, permission);
    }
}