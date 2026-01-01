package com.apex.core.service;

import com.apex.core.dto.LoginRequest;
import com.apex.core.dao.UserDAO;
import com.apex.core.model.SystemUser;
import com.apex.util.JwtUtil;
import com.apex.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务
 */
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserDAO userDAO = new UserDAO();

    /**
     * 用户登录
     */
    public Map<String, Object> login(LoginRequest loginRequest) {
        Map<String, Object> result = new HashMap<>();

        logger.info("Login attempt for username: {}", loginRequest.getUsername());

        // 验证输入
        if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "用户名不能为空");
            logger.warn("Login failed: username is empty");
            return result;
        }

        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "密码不能为空");
            logger.warn("Login failed: password is empty for user: {}", loginRequest.getUsername());
            return result;
        }

        // 查询用户
        SystemUser user = userDAO.findByUsername(loginRequest.getUsername().trim());
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            logger.warn("Login failed: user not found - {}", loginRequest.getUsername());
            return result;
        }

        // 验证密码
        boolean passwordValid = PasswordUtil.verifyPassword(
                loginRequest.getPassword(),
                user.getSalt(),
                user.getPasswordHash()
        );

        if (!passwordValid) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            logger.warn("Login failed: invalid password for user: {}", loginRequest.getUsername());
            return result;
        }

        // 生成JWT Token
        String token = JwtUtil.generateToken(user.getId(), user.getUsername());

        // 更新最后登录时间
        userDAO.updateLastLoginTime(user.getId());

        // 构建响应数据
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("isAdmin", user.getAdmin());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", userInfo);

        result.put("success", true);
        result.put("data", data);

        logger.info("Login successful for user: {}", loginRequest.getUsername());
        return result;
    }

    /**
     * 用户登出
     */
    public Map<String, Object> logout(String token) {
        Map<String, Object> result = new HashMap<>();

        if (token != null && JwtUtil.validateToken(token)) {
            String username = JwtUtil.getUsernameFromToken(token);
            logger.info("User logout: {}", username);
        }

        result.put("success", true);
        return result;
    }
}