package com.apex.core.service;

import com.apex.core.dto.CreateUserRequest;
import com.apex.core.dto.UpdateProfileRequest;
import com.apex.core.dao.UserDAO;
import com.apex.core.model.SystemUser;
import com.apex.util.JwtUtil;
import com.apex.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务
 */
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDAO userDAO = new UserDAO();

    /**
     * 获取用户权限
     */
    public Map<String, Object> getPermissions(String token) {
        Map<String, Object> result = new HashMap<>();

        logger.info("Getting permissions for token");

        // 验证Token
        if (!JwtUtil.validateToken(token)) {
            result.put("success", false);
            result.put("message", "无效的Token");
            logger.warn("Get permissions failed: invalid token");
            return result;
        }

        Integer userId = JwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            result.put("success", false);
            result.put("message", "用户ID无效");
            logger.warn("Get permissions failed: invalid user ID from token");
            return result;
        }

        // 查询用户权限
        SystemUser user = userDAO.getPermissions(userId);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在或已被禁用");
            logger.warn("Get permissions failed: user not found - {}", userId);
            return result;
        }

        // 构建权限响应
        Map<String, Object> permissions = new HashMap<>();
        permissions.put("isAdmin", user.getAdmin());
        permissions.put("canManageOrder", user.getCanManageOrder());
        permissions.put("canManageLogistics", user.getCanManageLogistics());
        permissions.put("canManageAfterSales", user.getCanManageAfterSales());
        permissions.put("canManageReview", user.getCanManageReview());
        permissions.put("canManageInventory", user.getCanManageInventory());
        permissions.put("canManageIncome", user.getCanManageIncome());

        result.put("success", true);
        result.put("data", permissions);

        logger.info("Permissions retrieved for user: {}", userId);
        return result;
    }

    /**
     * 修改个人信息
     */
    public Map<String, Object> updateProfile(String token, UpdateProfileRequest request) {
        Map<String, Object> result = new HashMap<>();

        logger.info("Updating profile for token");

        // 验证Token
        if (!JwtUtil.validateToken(token)) {
            result.put("success", false);
            result.put("message", "无效的Token");
            logger.warn("Update profile failed: invalid token");
            return result;
        }

        Integer userId = JwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            result.put("success", false);
            result.put("message", "用户ID无效");
            logger.warn("Update profile failed: invalid user ID from token");
            return result;
        }

        // 检查至少有一个字段需要更新
        boolean hasUpdate = (request.getRealName() != null && !request.getRealName().trim().isEmpty()) ||
                (request.getEmail() != null && !request.getEmail().trim().isEmpty()) ||
                (request.getPhone() != null && !request.getPhone().trim().isEmpty());

        if (!hasUpdate) {
            result.put("success", false);
            result.put("message", "至少需要提供一个更新字段");
            logger.warn("Update profile failed: no fields to update for user: {}", userId);
            return result;
        }

        // 查询用户
        SystemUser user = userDAO.findById(userId);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在或已被禁用");
            logger.warn("Update profile failed: user not found - {}", userId);
            return result;
        }

        // 更新用户信息
        if (request.getRealName() != null && !request.getRealName().trim().isEmpty()) {
            user.setRealName(request.getRealName().trim());
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            user.setEmail(request.getEmail().trim());
        }
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            user.setPhone(request.getPhone().trim());
        }

        boolean updateSuccess = userDAO.update(user);
        if (!updateSuccess) {
            result.put("success", false);
            result.put("message", "更新个人信息失败");
            logger.error("Update profile failed: database update failed for user: {}", userId);
            return result;
        }

        // 构建响应数据
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("id", user.getId());
        profileData.put("realName", user.getRealName());
        profileData.put("email", user.getEmail());
        profileData.put("phone", user.getPhone());

        result.put("success", true);
        result.put("data", profileData);

        logger.info("Profile updated successfully for user: {}", userId);
        return result;
    }

    /**
     * 创建新用户（管理员权限）
     */
    public Map<String, Object> createUser(String token, CreateUserRequest request) {
        Map<String, Object> result = new HashMap<>();

        logger.info("Creating new user: {}", request.getUsername());

        // 验证Token
        if (!JwtUtil.validateToken(token)) {
            result.put("success", false);
            result.put("message", "无效的Token");
            logger.warn("Create user failed: invalid token");
            return result;
        }

        Integer currentUserId = JwtUtil.getUserIdFromToken(token);
        if (currentUserId == null) {
            result.put("success", false);
            result.put("message", "用户ID无效");
            logger.warn("Create user failed: invalid user ID from token");
            return result;
        }

        // 验证输入数据
        Map<String, String> validationErrors = validateCreateUserRequest(request);
        if (!validationErrors.isEmpty()) {
            result.put("success", false);
            result.put("message", "输入数据验证失败");
            result.put("errors", validationErrors);
            logger.warn("Create user failed: validation errors for user: {}", request.getUsername());
            return result;
        }

        // 检查用户名是否已存在
        if (userDAO.existsByUsername(request.getUsername().trim())) {
            result.put("success", false);
            result.put("message", "用户名已存在");
            logger.warn("Create user failed: username already exists - {}", request.getUsername());
            return result;
        }

        // 创建用户
        SystemUser newUser = new SystemUser();
        newUser.setUsername(request.getUsername().trim());
        newUser.setRealName(request.getRealName().trim());
        newUser.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
        newUser.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        newUser.setAdmin(request.getIsAdmin() != null ? request.getIsAdmin() : false);
        newUser.setCanManageOrder(request.getCanManageOrder() != null ? request.getCanManageOrder() : false);
        newUser.setCanManageLogistics(request.getCanManageLogistics() != null ? request.getCanManageLogistics() : false);
        newUser.setCanManageAfterSales(request.getCanManageAfterSales() != null ? request.getCanManageAfterSales() : false);
        newUser.setCanManageReview(request.getCanManageReview() != null ? request.getCanManageReview() : false);
        newUser.setCanManageInventory(request.getCanManageInventory() != null ? request.getCanManageInventory() : false);
        newUser.setCanManageIncome(request.getCanManageIncome() != null ? request.getCanManageIncome() : false);

        // 生成密码哈希
        String salt = PasswordUtil.generateSalt();
        String passwordHash = PasswordUtil.hashPassword(request.getPassword().trim(), salt);
        newUser.setPasswordHash(passwordHash);
        newUser.setSalt(salt);

        boolean createSuccess = userDAO.create(newUser);
        if (!createSuccess) {
            result.put("success", false);
            result.put("message", "创建用户失败");
            logger.error("Create user failed: database creation failed for user: {}", request.getUsername());
            return result;
        }

        // 构建响应数据
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", newUser.getId());
        userData.put("username", newUser.getUsername());
        userData.put("realName", newUser.getRealName());
        userData.put("email", newUser.getEmail());
        userData.put("phone", newUser.getPhone());
        userData.put("isAdmin", newUser.getAdmin());
        userData.put("canManageOrder", newUser.getCanManageOrder());
        userData.put("canManageLogistics", newUser.getCanManageLogistics());
        userData.put("canManageAfterSales", newUser.getCanManageAfterSales());
        userData.put("canManageReview", newUser.getCanManageReview());
        userData.put("canManageInventory", newUser.getCanManageInventory());
        userData.put("canManageIncome", newUser.getCanManageIncome());
        userData.put("status", 1); // 正常状态
        userData.put("createdAt", LocalDateTime.now().toString());

        result.put("success", true);
        result.put("data", userData);

        logger.info("User created successfully: {} by admin: {}", newUser.getUsername(), currentUserId);
        return result;
    }

    /**
     * 验证创建用户请求
     */
    private Map<String, String> validateCreateUserRequest(CreateUserRequest request) {
        Map<String, String> errors = new HashMap<>();

        // 验证用户名
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            errors.put("username", "用户名不能为空");
        } else {
            String username = request.getUsername().trim();
            if (username.length() < 3 || username.length() > 20) {
                errors.put("username", "用户名长度必须在3-20个字符之间");
            }
            if (!username.matches("^[a-zA-Z0-9_]+$")) {
                errors.put("username", "用户名只能包含字母、数字和下划线");
            }
        }

        // 验证密码
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            errors.put("password", "密码不能为空");
        } else {
            String password = request.getPassword().trim();
            if (password.length() < 6 || password.length() > 20) {
                errors.put("password", "密码长度必须在6-20个字符之间");
            }
        }

        // 验证真实姓名
        if (request.getRealName() == null || request.getRealName().trim().isEmpty()) {
            errors.put("realName", "真实姓名不能为空");
        } else {
            String realName = request.getRealName().trim();
            if (realName.length() < 2 || realName.length() > 20) {
                errors.put("realName", "真实姓名长度必须在2-20个字符之间");
            }
        }

        // 验证邮箱
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            String email = request.getEmail().trim();
            String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
            if (!email.matches(emailRegex)) {
                errors.put("email", "邮箱格式不正确");
            }
        }

        // 验证手机号
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            String phone = request.getPhone().trim();
            String phoneRegex = "^1[3-9]\\d{9}$";
            if (!phone.matches(phoneRegex)) {
                errors.put("phone", "手机号格式不正确");
            }
        }

        return errors;
    }
}