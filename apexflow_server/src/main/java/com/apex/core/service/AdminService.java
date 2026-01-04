package com.apex.core.service;

import com.apex.core.dao.IUserDAO;
import com.apex.core.dao.UserDAO;
import com.apex.core.dto.*;
import com.apex.core.model.SystemUser;
import com.apex.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员服务类
 * 提供用户管理相关功能，包括用户列表、信息修改、权限管理和密码重置
 */
public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    private final IUserDAO userDAO = new UserDAO();

    /**
     * 获取用户列表（分页）
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     * @return 用户列表和分页信息
     */
    public AdminUserListResponse getUserList(int page, int pageSize) {
        logger.info("[ADMIN_SERVICE] Getting user list. Page: {}, PageSize: {}", page, pageSize);

        // 参数验证
        if (page < 1) {
            logger.warn("[ADMIN_SERVICE] Invalid page number: {}, reset to 1", page);
            page = 1;
        }

        if (pageSize < 1 || pageSize > 100) {
            logger.warn("[ADMIN_SERVICE] Invalid page size: {}, reset to 20", pageSize);
            pageSize = 20;
        }

        try {
            // 获取用户列表
            List<SystemUser> users = userDAO.findAll(page, pageSize);

            // 转换为DTO列表
            List<AdminUserDTO> userDTOs = new ArrayList<>();
            for (SystemUser user : users) {
                userDTOs.add(convertToAdminUserDTO(user));
            }

            // 获取总用户数
            long totalCount = userDAO.count();
            int totalPages = (int) Math.ceil((double) totalCount / pageSize);

            AdminUserListResponse response = new AdminUserListResponse();
            response.setUsers(userDTOs);
            response.setCurrentPage(page);
            response.setPageSize(pageSize);
            response.setTotalCount(totalCount);
            response.setTotalPages(totalPages);

            logger.info("[ADMIN_SERVICE] User list retrieved successfully. Total users: {}, Current page: {}",
                    totalCount, page);
            return response;

        } catch (Exception e) {
            logger.error("[ADMIN_SERVICE] Failed to get user list. Page: {}, Error: {}",
                    page, e.getMessage(), e);
            throw new RuntimeException("获取用户列表失败", e);
        }
    }

    /**
     * 更新用户基本信息
     * @param userId 用户ID
     * @param request 更新请求数据
     * @return 更新后的用户信息
     */
    public AdminUserDTO updateUserInfo(Integer userId, UpdateUserInfoRequest request) {
        logger.info("[ADMIN_SERVICE] Updating user info. User ID: {}", userId);

        // 参数验证
        if (userId == null || userId <= 0) {
            logger.error("[ADMIN_SERVICE] Invalid user ID: {}", userId);
            throw new IllegalArgumentException("用户ID无效");
        }

        if (request == null) {
            logger.error("[ADMIN_SERVICE] Update request is null");
            throw new IllegalArgumentException("更新请求不能为空");
        }

        try {
            // 获取现有用户信息
            SystemUser user = userDAO.findById(userId);
            if (user == null) {
                logger.error("[ADMIN_SERVICE] User not found. User ID: {}", userId);
                throw new IllegalArgumentException("用户不存在");
            }

            // 更新可修改的字段
            boolean updated = false;

            if (request.getRealName() != null && !request.getRealName().trim().isEmpty()) {
                user.setRealName(request.getRealName().trim());
                updated = true;
                logger.debug("[ADMIN_SERVICE] Updated real name for user ID: {}", userId);
            }

            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                // 检查邮箱是否已被其他用户使用
                SystemUser existingUser = userDAO.findByEmail(request.getEmail().trim());
                if (existingUser != null && !existingUser.getId().equals(userId)) {
                    logger.error("[ADMIN_SERVICE] Email already in use by another user. Email: {}", request.getEmail());
                    throw new IllegalArgumentException("邮箱已被其他用户使用");
                }
                user.setEmail(request.getEmail().trim());
                updated = true;
                logger.debug("[ADMIN_SERVICE] Updated email for user ID: {}", userId);
            }

            if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
                user.setPhone(request.getPhone().trim());
                updated = true;
                logger.debug("[ADMIN_SERVICE] Updated phone for user ID: {}", userId);
            }

            if (request.getStatus() != null) {
                user.setStatus(request.getStatus());
                updated = true;
                logger.debug("[ADMIN_SERVICE] Updated status for user ID: {}", userId);
            }

            if (!updated) {
                logger.warn("[ADMIN_SERVICE] No fields to update for user ID: {}", userId);
                throw new IllegalArgumentException("没有要更新的字段");
            }

            // 保存更新
            boolean success = userDAO.update(user);
            if (!success) {
                logger.error("[ADMIN_SERVICE] Failed to update user in database. User ID: {}", userId);
                throw new RuntimeException("更新用户信息失败");
            }

            logger.info("[ADMIN_SERVICE] User info updated successfully. User ID: {}", userId);
            return convertToAdminUserDTO(user);

        } catch (IllegalArgumentException e) {
            logger.warn("[ADMIN_SERVICE] Validation error when updating user. User ID: {}, Error: {}",
                    userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("[ADMIN_SERVICE] Failed to update user info. User ID: {}, Error: {}",
                    userId, e.getMessage(), e);
            throw new RuntimeException("更新用户信息失败", e);
        }
    }

    /**
     * 更新用户权限
     * @param userId 用户ID
     * @param request 权限更新请求
     * @return 更新后的用户权限信息
     */
    public UserPermissionsDTO updateUserPermissions(Integer userId, UpdateUserPermissionsRequest request) {
        logger.info("[ADMIN_SERVICE] Updating user permissions. User ID: {}", userId);

        // 参数验证
        if (userId == null || userId <= 0) {
            logger.error("[ADMIN_SERVICE] Invalid user ID: {}", userId);
            throw new IllegalArgumentException("用户ID无效");
        }

        if (request == null) {
            logger.error("[ADMIN_SERVICE] Permissions request is null");
            throw new IllegalArgumentException("权限请求不能为空");
        }

        try {
            // 获取现有用户信息
            SystemUser user = userDAO.findById(userId);
            if (user == null) {
                logger.error("[ADMIN_SERVICE] User not found. User ID: {}", userId);
                throw new IllegalArgumentException("用户不存在");
            }

            // 更新权限字段
            boolean updated = false;

            if (request.getIsAdmin() != null) {
                user.setAdmin(request.getIsAdmin());
                updated = true;
                logger.debug("[ADMIN_SERVICE] Updated admin permission for user ID: {}", userId);
            }

            if (request.getCanManageOrder() != null) {
                user.setCanManageOrder(request.getCanManageOrder());
                updated = true;
                logger.debug("[ADMIN_SERVICE] Updated order management permission for user ID: {}", userId);
            }

            if (request.getCanManageLogistics() != null) {
                user.setCanManageLogistics(request.getCanManageLogistics());
                updated = true;
                logger.debug("[ADMIN_SERVICE] Updated logistics management permission for user ID: {}", userId);
            }

            if (request.getCanManageAfterSales() != null) {
                user.setCanManageAfterSales(request.getCanManageAfterSales());
                updated = true;
                logger.debug("[ADMIN_SERVICE] Updated after-sales management permission for user ID: {}", userId);
            }

            if (request.getCanManageReview() != null) {
                user.setCanManageReview(request.getCanManageReview());
                updated = true;
                logger.debug("[ADMIN_SERVICE] Updated review management permission for user ID: {}", userId);
            }

            if (request.getCanManageInventory() != null) {
                user.setCanManageInventory(request.getCanManageInventory());
                updated = true;
                logger.debug("[ADMIN_SERVICE] Updated inventory management permission for user ID: {}", userId);
            }

            if (request.getCanManageIncome() != null) {
                user.setCanManageIncome(request.getCanManageIncome());
                updated = true;
                logger.debug("[ADMIN_SERVICE] Updated income management permission for user ID: {}", userId);
            }

            if (!updated) {
                logger.warn("[ADMIN_SERVICE] No permissions to update for user ID: {}", userId);
                throw new IllegalArgumentException("没有要更新的权限字段");
            }

            // 保存更新
            boolean success = userDAO.update(user);
            if (!success) {
                logger.error("[ADMIN_SERVICE] Failed to update user permissions in database. User ID: {}", userId);
                throw new RuntimeException("更新用户权限失败");
            }

            // 获取更新后的权限信息
            UserPermissionsDTO permissionsDTO = new UserPermissionsDTO();
            permissionsDTO.setUserId(userId);
            permissionsDTO.setIsAdmin(user.getAdmin());
            permissionsDTO.setCanManageOrder(user.getCanManageOrder());
            permissionsDTO.setCanManageLogistics(user.getCanManageLogistics());
            permissionsDTO.setCanManageAfterSales(user.getCanManageAfterSales());
            permissionsDTO.setCanManageReview(user.getCanManageReview());
            permissionsDTO.setCanManageInventory(user.getCanManageInventory());
            permissionsDTO.setCanManageIncome(user.getCanManageIncome());

            logger.info("[ADMIN_SERVICE] User permissions updated successfully. User ID: {}", userId);
            return permissionsDTO;

        } catch (IllegalArgumentException e) {
            logger.warn("[ADMIN_SERVICE] Validation error when updating permissions. User ID: {}, Error: {}",
                    userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("[ADMIN_SERVICE] Failed to update user permissions. User ID: {}, Error: {}",
                    userId, e.getMessage(), e);
            throw new RuntimeException("更新用户权限失败", e);
        }
    }

    /**
     * 重置用户密码
     * @param userId 用户ID
     * @param request 密码重置请求
     * @return 操作结果
     */
    public Map<String, Object> resetUserPassword(Integer userId, ResetPasswordRequest request) {
        logger.info("[ADMIN_SERVICE] Resetting user password. User ID: {}", userId);

        // 参数验证
        if (userId == null || userId <= 0) {
            logger.error("[ADMIN_SERVICE] Invalid user ID: {}", userId);
            throw new IllegalArgumentException("用户ID无效");
        }

        if (request == null) {
            logger.error("[ADMIN_SERVICE] Password reset request is null");
            throw new IllegalArgumentException("密码重置请求不能为空");
        }

        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            logger.error("[ADMIN_SERVICE] New password is empty");
            throw new IllegalArgumentException("新密码不能为空");
        }

        if (request.getNewPassword().length() < 6) {
            logger.error("[ADMIN_SERVICE] New password is too short. Length: {}", request.getNewPassword().length());
            throw new IllegalArgumentException("密码长度不能少于6位");
        }

        try {
            // 获取用户信息
            SystemUser user = userDAO.findById(userId);
            if (user == null) {
                logger.error("[ADMIN_SERVICE] User not found. User ID: {}", userId);
                throw new IllegalArgumentException("用户不存在");
            }

            // 生成新的盐和哈希密码
            String newSalt = PasswordUtil.generateSalt();
            String newPasswordHash = PasswordUtil.hashPassword(request.getNewPassword(), newSalt);

            // 更新用户密码和盐
            user.setSalt(newSalt);
            user.setPasswordHash(newPasswordHash);

            // 保存更新
            boolean success = userDAO.update(user);
            if (!success) {
                logger.error("[ADMIN_SERVICE] Failed to reset password in database. User ID: {}", userId);
                throw new RuntimeException("重置密码失败");
            }

            logger.info("[ADMIN_SERVICE] Password reset successfully. User ID: {}", userId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "密码重置成功");
            result.put("userId", userId);
            return result;

        } catch (IllegalArgumentException e) {
            logger.warn("[ADMIN_SERVICE] Validation error when resetting password. User ID: {}, Error: {}",
                    userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("[ADMIN_SERVICE] Failed to reset user password. User ID: {}, Error: {}",
                    userId, e.getMessage(), e);
            throw new RuntimeException("重置密码失败", e);
        }
    }

    /**
     * 搜索用户
     * @param keyword 搜索关键词
     * @param page 页码
     * @param pageSize 每页大小
     * @return 搜索结果
     */
    public AdminUserListResponse searchUsers(String keyword, int page, int pageSize) {
        logger.info("[ADMIN_SERVICE] Searching users. Keyword: {}, Page: {}, PageSize: {}",
                keyword, page, pageSize);

        // 参数验证
        if (keyword == null || keyword.trim().isEmpty()) {
            logger.warn("[ADMIN_SERVICE] Empty search keyword, returning all users");
            return getUserList(page, pageSize);
        }

        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 20;

        try {
            // 搜索用户
            List<SystemUser> users = userDAO.search(keyword.trim(), page, pageSize);

            // 转换为DTO列表
            List<AdminUserDTO> userDTOs = new ArrayList<>();
            for (SystemUser user : users) {
                userDTOs.add(convertToAdminUserDTO(user));
            }

            // 由于分页搜索的计数较复杂，这里简化处理
            AdminUserListResponse response = new AdminUserListResponse();
            response.setUsers(userDTOs);
            response.setCurrentPage(page);
            response.setPageSize(pageSize);
            response.setTotalCount((long) userDTOs.size());
            response.setTotalPages(1);

            logger.info("[ADMIN_SERVICE] User search completed. Keyword: {}, Found {} users",
                    keyword, userDTOs.size());
            return response;

        } catch (Exception e) {
            logger.error("[ADMIN_SERVICE] Failed to search users. Keyword: {}, Error: {}",
                    keyword, e.getMessage(), e);
            throw new RuntimeException("搜索用户失败", e);
        }
    }

    // --- [新增] 创建用户 ---
    public void createUser(CreateUserRequest request) {
        logger.info("[ADMIN_SERVICE] Creating user: {}", request.getUsername());

        // 1. 基础校验 (DAO层可能还有唯一性约束校验)
        if (userDAO.findByUsername(request.getUsername()) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // 2. 映射 DTO 到 Entity
        SystemUser user = new SystemUser();
        user.setUsername(request.getUsername());
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(1); // 默认启用

        // 3. 处理密码 (生成盐 + 哈希)
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(request.getPassword(), salt);
        user.setSalt(salt);
        user.setPasswordHash(hash);

        // 4. 处理权限
        user.setAdmin(Boolean.TRUE.equals(request.getIsAdmin()));
        // 如果是 Admin，通常赋予所有权限，或者按照前端传来的细分权限
        if (user.getAdmin()) {
            user.setCanManageOrder(true);
            user.setCanManageLogistics(true);
            user.setCanManageAfterSales(true);
            user.setCanManageReview(true);
            user.setCanManageInventory(true);
            user.setCanManageIncome(true);
        } else {
            // 普通用户，使用请求中的具体权限
            user.setCanManageOrder(Boolean.TRUE.equals(request.getCanManageOrder()));
            user.setCanManageLogistics(Boolean.TRUE.equals(request.getCanManageLogistics()));
            user.setCanManageAfterSales(Boolean.TRUE.equals(request.getCanManageAfterSales()));
            user.setCanManageReview(Boolean.TRUE.equals(request.getCanManageReview()));
            user.setCanManageInventory(Boolean.TRUE.equals(request.getCanManageInventory()));
            user.setCanManageIncome(Boolean.TRUE.equals(request.getCanManageIncome()));
        }

        // 5. 保存
        boolean success = userDAO.create(user);
        if (!success) {
            throw new RuntimeException("数据库写入失败");
        }
    }

    // --- [新增] 删除用户 ---
    public void deleteUser(Integer userId) {
        logger.info("[ADMIN_SERVICE] Deleting user ID: {}", userId);

        // 防止删除自己 (可选逻辑，需要获取当前登录用户ID，此处略过)
        // SystemUser user = userDAO.findUserById(userId);

        boolean success = userDAO.delete(userId);
        if (!success) {
            throw new IllegalArgumentException("用户不存在或删除失败");
        }
    }

    /**
     * 将SystemUser转换为AdminUserDTO
     */
    private AdminUserDTO convertToAdminUserDTO(SystemUser user) {
        AdminUserDTO dto = new AdminUserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setIsAdmin(user.getAdmin());
        dto.setCanManageOrder(user.getCanManageOrder());
        dto.setCanManageLogistics(user.getCanManageLogistics());
        dto.setCanManageAfterSales(user.getCanManageAfterSales());
        dto.setCanManageReview(user.getCanManageReview());
        dto.setCanManageInventory(user.getCanManageInventory());
        dto.setCanManageIncome(user.getCanManageIncome());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setLastLoginAt(user.getLastLoginAt());
        return dto;
    }
}