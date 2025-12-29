package com.apex.core.dao;

import com.apex.core.model.SystemUser;
import com.apex.util.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User Data Access Object for managing SystemUser entities
 */
public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    // SQL 常量
    private static final String TABLE_NAME = "apexflow_system_user";

    /**
     * 创建新用户
     */
    public boolean create(SystemUser user) {
        String operation = "CREATE_USER";
        long startTime = System.currentTimeMillis();

        String sql = String.format("""
            INSERT INTO %s (
                username, password_hash, salt, real_name, email, phone,
                is_admin, can_manage_order, can_manage_logistics,
                can_manage_after_sales, can_manage_review,
                can_manage_inventory, can_manage_income, status
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, TABLE_NAME);

        logger.info("[{}] Creating new user. Username: {}", operation, user.getUsername());

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            int paramIndex = 1;
            pstmt.setString(paramIndex++, user.getUsername());
            pstmt.setString(paramIndex++, user.getPasswordHash());
            pstmt.setString(paramIndex++, user.getSalt());
            pstmt.setString(paramIndex++, user.getRealName());
            pstmt.setString(paramIndex++, user.getEmail());
            pstmt.setString(paramIndex++, user.getPhone());
            pstmt.setBoolean(paramIndex++, Boolean.TRUE.equals(user.getAdmin()));
            pstmt.setBoolean(paramIndex++, Boolean.TRUE.equals(user.getCanManageOrder()));
            pstmt.setBoolean(paramIndex++, Boolean.TRUE.equals(user.getCanManageLogistics()));
            pstmt.setBoolean(paramIndex++, Boolean.TRUE.equals(user.getCanManageAfterSales()));
            pstmt.setBoolean(paramIndex++, Boolean.TRUE.equals(user.getCanManageReview()));
            pstmt.setBoolean(paramIndex++, Boolean.TRUE.equals(user.getCanManageInventory()));
            pstmt.setBoolean(paramIndex++, Boolean.TRUE.equals(user.getCanManageIncome()));
            pstmt.setInt(paramIndex, user.getStatus() != null ? user.getStatus() : 1);

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected == 1;

            if (success) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                        logger.info("[{}] User created with ID: {}", operation, user.getId());
                    }
                }
            }

            long duration = System.currentTimeMillis() - startTime;

            if (success) {
                logger.info("[{}] User created successfully in {} ms. Username: {}, ID: {}",
                        operation, duration, user.getUsername(), user.getId());
            } else {
                logger.warn("[{}] User creation failed. Username: {}", operation, user.getUsername());
            }

            return success;

        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[{}] Failed to create user after {} ms. Username: {}, Error: {}",
                    operation, duration, user.getUsername(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 根据ID查找用户
     */
    public SystemUser findById(Integer id) {
        String operation = "FIND_USER_BY_ID";
        long startTime = System.currentTimeMillis();

        String sql = String.format("SELECT * FROM %s WHERE id = ?", TABLE_NAME);

        logger.debug("[{}] Searching for user by ID: {}", operation, id);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                SystemUser result = rs.next() ? mapToSystemUser(rs) : null;
                long duration = System.currentTimeMillis() - startTime;

                if (result != null) {
                    logger.debug("[{}] User found in {} ms. ID: {}, Username: {}",
                            operation, duration, id, result.getUsername());
                } else {
                    logger.debug("[{}] No user found with ID: {} (searched for {} ms)",
                            operation, id, duration);
                }

                return result;
            }
        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[{}] Error searching for user ID: {} after {} ms. Error: {}",
                    operation, id, duration, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据用户名查找用户
     */
    public SystemUser findByUsername(String username) {
        String operation = "FIND_USER_BY_USERNAME";
        long startTime = System.currentTimeMillis();

        String sql = String.format("SELECT * FROM %s WHERE username = ?", TABLE_NAME);

        logger.debug("[{}] Searching for user by username: {}", operation, username);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                SystemUser result = rs.next() ? mapToSystemUser(rs) : null;
                long duration = System.currentTimeMillis() - startTime;

                if (result != null) {
                    logger.debug("[{}] User found in {} ms. Username: {}, ID: {}",
                            operation, duration, username, result.getId());
                } else {
                    logger.debug("[{}] No user found with username: {} (searched for {} ms)",
                            operation, username, duration);
                }

                return result;
            }
        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[{}] Error searching for username: {} after {} ms. Error: {}",
                    operation, username, duration, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据邮箱查找用户
     */
    public SystemUser findByEmail(String email) {
        String operation = "FIND_USER_BY_EMAIL";
        long startTime = System.currentTimeMillis();

        String sql = String.format("SELECT * FROM %s WHERE email = ?", TABLE_NAME);

        logger.debug("[{}] Searching for user by email: {}", operation, email);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                SystemUser result = rs.next() ? mapToSystemUser(rs) : null;
                long duration = System.currentTimeMillis() - startTime;

                if (result != null) {
                    logger.debug("[{}] User found in {} ms. Email: {}, ID: {}",
                            operation, duration, email, result.getId());
                } else {
                    logger.debug("[{}] No user found with email: {} (searched for {} ms)",
                            operation, email, duration);
                }

                return result;
            }
        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[{}] Error searching for email: {} after {} ms. Error: {}",
                    operation, email, duration, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 更新用户信息
     */
    public boolean update(SystemUser user) {
        String operation = "UPDATE_USER";
        long startTime = System.currentTimeMillis();

        String sql = String.format("""
            UPDATE %s
            SET username = ?, password_hash = ?, salt = ?, real_name = ?,
                email = ?, phone = ?, is_admin = ?, can_manage_order = ?,
                can_manage_logistics = ?, can_manage_after_sales = ?,
                can_manage_review = ?, can_manage_inventory = ?,
                can_manage_income = ?, status = ?, updated_at = ?
            WHERE id = ?
            """, TABLE_NAME);

        logger.info("[{}] Updating user. ID: {}, Username: {}",
                operation, user.getId(), user.getUsername());

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            pstmt.setString(paramIndex++, user.getUsername());
            pstmt.setString(paramIndex++, user.getPasswordHash());
            pstmt.setString(paramIndex++, user.getSalt());
            pstmt.setString(paramIndex++, user.getRealName());
            pstmt.setString(paramIndex++, user.getEmail());
            pstmt.setString(paramIndex++, user.getPhone());
            pstmt.setBoolean(paramIndex++, Boolean.TRUE.equals(user.getAdmin()));
            pstmt.setBoolean(paramIndex++, Boolean.TRUE.equals(user.getCanManageOrder()));
            pstmt.setBoolean(paramIndex++, Boolean.TRUE.equals(user.getCanManageLogistics()));
            pstmt.setBoolean(paramIndex++, Boolean.TRUE.equals(user.getCanManageAfterSales()));
            pstmt.setBoolean(paramIndex++, Boolean.TRUE.equals(user.getCanManageReview()));
            pstmt.setBoolean(paramIndex++, Boolean.TRUE.equals(user.getCanManageInventory()));
            pstmt.setBoolean(paramIndex++, Boolean.TRUE.equals(user.getCanManageIncome()));
            pstmt.setInt(paramIndex++, user.getStatus());
            pstmt.setTimestamp(paramIndex++, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(paramIndex, user.getId());

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected == 1;

            long duration = System.currentTimeMillis() - startTime;

            if (success) {
                logger.info("[{}] User updated successfully in {} ms. ID: {}, Username: {}",
                        operation, duration, user.getId(), user.getUsername());
            } else {
                logger.warn("[{}] User update did not affect any rows. ID: {} (took {} ms)",
                        operation, user.getId(), duration);
            }

            return success;
        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[{}] Failed to update user after {} ms. ID: {}, Error: {}",
                    operation, duration, user.getId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 更新用户最后登录时间
     */
    public boolean updateLastLoginTime(Integer userId) {
        String operation = "UPDATE_LAST_LOGIN_TIME";
        long startTime = System.currentTimeMillis();

        String sql = String.format("UPDATE %s SET last_login_at = ? WHERE id = ?", TABLE_NAME);

        logger.debug("[{}] Updating last login time for user ID: {}", operation, userId);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(2, userId);

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected == 1;

            long duration = System.currentTimeMillis() - startTime;

            if (success) {
                logger.debug("[{}] Last login time updated successfully in {} ms. User ID: {}",
                        operation, duration, userId);
            } else {
                logger.warn("[{}] Failed to update last login time. User ID: {} (took {} ms)",
                        operation, userId, duration);
            }

            return success;
        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[{}] Error updating last login time for user ID: {} after {} ms. Error: {}",
                    operation, userId, duration, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 更新用户状态
     */
    public boolean updateStatus(Integer userId, Integer status) {
        String operation = "UPDATE_USER_STATUS";
        long startTime = System.currentTimeMillis();

        String sql = String.format("UPDATE %s SET status = ?, updated_at = ? WHERE id = ?", TABLE_NAME);

        logger.info("[{}] Updating user status. User ID: {}, New Status: {}",
                operation, userId, status);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, status);
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(3, userId);

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected == 1;

            long duration = System.currentTimeMillis() - startTime;

            if (success) {
                logger.info("[{}] User status updated successfully in {} ms. User ID: {}, New Status: {}",
                        operation, duration, userId, status);
            } else {
                logger.warn("[{}] No user found to update status. User ID: {} (took {} ms)",
                        operation, userId, duration);
            }

            return success;
        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[{}] Failed to update user status after {} ms. User ID: {}, Error: {}",
                    operation, duration, userId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 删除用户
     */
    public boolean delete(Integer userId) {
        String operation = "DELETE_USER";
        long startTime = System.currentTimeMillis();

        String sql = String.format("DELETE FROM %s WHERE id = ?", TABLE_NAME);

        logger.warn("[{}] Attempting to delete user. User ID: {}", operation, userId);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected == 1;

            long duration = System.currentTimeMillis() - startTime;

            if (success) {
                logger.warn("[{}] User deleted successfully in {} ms. User ID: {}",
                        operation, duration, userId);
            } else {
                logger.warn("[{}] No user found to delete. User ID: {} (took {} ms)",
                        operation, userId, duration);
            }

            return success;
        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[{}] Failed to delete user after {} ms. User ID: {}, Error: {}",
                    operation, duration, userId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取所有用户（分页）
     */
    public List<SystemUser> findAll(int page, int pageSize) {
        String operation = "FIND_ALL_USERS";
        long startTime = System.currentTimeMillis();

        List<SystemUser> users = new ArrayList<>();
        String sql = String.format("""
            SELECT * FROM %s
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
            """, TABLE_NAME);

        int offset = (page - 1) * pageSize;

        logger.info("[{}] Retrieving users. Page: {}, PageSize: {}, Offset: {}",
                operation, page, pageSize, offset);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    users.add(mapToSystemUser(rs));
                    count++;
                }

                long duration = System.currentTimeMillis() - startTime;
                logger.info("[{}] Retrieved {} users in {} ms. Page: {}, PageSize: {}",
                        operation, count, duration, page, pageSize);

                return users;
            }
        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[{}] Failed to retrieve users after {} ms. Page: {}, Error: {}",
                    operation, duration, page, e.getMessage(), e);
            return users;
        }
    }

    /**
     * 根据状态筛选用户（分页）
     */
    public List<SystemUser> findByStatus(Integer status, int page, int pageSize) {
        String operation = "FIND_USERS_BY_STATUS";
        long startTime = System.currentTimeMillis();

        List<SystemUser> users = new ArrayList<>();
        String sql = String.format("""
            SELECT * FROM %s
            WHERE status = ?
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
            """, TABLE_NAME);

        int offset = (page - 1) * pageSize;

        logger.info("[{}] Retrieving users by status. Status: {}, Page: {}, PageSize: {}",
                operation, status, page, pageSize);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, status);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    users.add(mapToSystemUser(rs));
                    count++;
                }

                long duration = System.currentTimeMillis() - startTime;
                logger.info("[{}] Retrieved {} users with status {} in {} ms",
                        operation, count, status, duration);

                return users;
            }
        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[{}] Failed to retrieve users by status after {} ms. Status: {}, Error: {}",
                    operation, duration, status, e.getMessage(), e);
            return users;
        }
    }

    /**
     * 搜索用户（按用户名、姓名、邮箱、电话）
     */
    public List<SystemUser> search(String keyword, int page, int pageSize) {
        String operation = "SEARCH_USERS";
        long startTime = System.currentTimeMillis();

        List<SystemUser> users = new ArrayList<>();
        String sql = String.format("""
            SELECT * FROM %s
            WHERE username LIKE ? OR real_name LIKE ? OR email LIKE ? OR phone LIKE ?
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
            """, TABLE_NAME);

        int offset = (page - 1) * pageSize;
        String searchPattern = "%" + keyword + "%";

        logger.info("[{}] Searching users with keyword: '{}', Page: {}, PageSize: {}",
                operation, keyword, page, pageSize);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setString(4, searchPattern);
            pstmt.setInt(5, pageSize);
            pstmt.setInt(6, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    users.add(mapToSystemUser(rs));
                    count++;
                }

                long duration = System.currentTimeMillis() - startTime;
                logger.info("[{}] Found {} users with keyword '{}' in {} ms",
                        operation, count, keyword, duration);

                return users;
            }
        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[{}] Failed to search users after {} ms. Keyword: '{}', Error: {}",
                    operation, duration, keyword, e.getMessage(), e);
            return users;
        }
    }

    /**
     * 统计用户总数
     */
    public long count() {
        String operation = "COUNT_USERS";
        long startTime = System.currentTimeMillis();

        String sql = String.format("SELECT COUNT(*) FROM %s", TABLE_NAME);

        logger.debug("[{}] Counting total users", operation);

        try (Connection conn = ConnectionPool.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            long count = rs.next() ? rs.getLong(1) : 0;
            long duration = System.currentTimeMillis() - startTime;

            logger.info("[{}] Total users: {} (counted in {} ms)", operation, count, duration);
            return count;
        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[{}] Failed to count users after {} ms. Error: {}",
                    operation, duration, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 统计活跃用户数
     */
    public long countActive() {
        String operation = "COUNT_ACTIVE_USERS";
        long startTime = System.currentTimeMillis();

        String sql = String.format("SELECT COUNT(*) FROM %s WHERE status = 1", TABLE_NAME);

        logger.debug("[{}] Counting active users", operation);

        try (Connection conn = ConnectionPool.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            long count = rs.next() ? rs.getLong(1) : 0;
            long duration = System.currentTimeMillis() - startTime;

            logger.info("[{}] Active users: {} (counted in {} ms)", operation, count, duration);
            return count;
        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[{}] Failed to count active users after {} ms. Error: {}",
                    operation, duration, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 检查用户名是否存在
     */
    public boolean existsByUsername(String username) {
        String operation = "CHECK_USERNAME_EXISTS";
        long startTime = System.currentTimeMillis();

        String sql = String.format("SELECT COUNT(*) FROM %s WHERE username = ?", TABLE_NAME);

        logger.debug("[{}] Checking if username exists: {}", operation, username);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                boolean exists = rs.next() && rs.getLong(1) > 0;
                long duration = System.currentTimeMillis() - startTime;

                logger.debug("[{}] Username '{}' exists: {} (checked in {} ms)",
                        operation, username, exists, duration);

                return exists;
            }
        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[{}] Failed to check username existence after {} ms. Username: {}, Error: {}",
                    operation, duration, username, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 检查邮箱是否存在
     */
    public boolean existsByEmail(String email) {
        String operation = "CHECK_EMAIL_EXISTS";
        long startTime = System.currentTimeMillis();

        String sql = String.format("SELECT COUNT(*) FROM %s WHERE email = ?", TABLE_NAME);

        logger.debug("[{}] Checking if email exists: {}", operation, email);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                boolean exists = rs.next() && rs.getLong(1) > 0;
                long duration = System.currentTimeMillis() - startTime;

                logger.debug("[{}] Email '{}' exists: {} (checked in {} ms)",
                        operation, email, exists, duration);

                return exists;
            }
        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[{}] Failed to check email existence after {} ms. Email: {}, Error: {}",
                    operation, duration, email, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 将ResultSet映射到SystemUser对象
     */
    private SystemUser mapToSystemUser(ResultSet rs) throws SQLException {
        SystemUser user = new SystemUser();

        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setSalt(rs.getString("salt"));
        user.setRealName(rs.getString("real_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));

        user.setAdmin(rs.getBoolean("is_admin"));
        user.setCanManageOrder(rs.getBoolean("can_manage_order"));
        user.setCanManageLogistics(rs.getBoolean("can_manage_logistics"));
        user.setCanManageAfterSales(rs.getBoolean("can_manage_after_sales"));
        user.setCanManageReview(rs.getBoolean("can_manage_review"));
        user.setCanManageInventory(rs.getBoolean("can_manage_inventory"));
        user.setCanManageIncome(rs.getBoolean("can_manage_income"));

        user.setStatus(rs.getInt("status"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        user.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        user.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : null);

        Timestamp lastLoginAt = rs.getTimestamp("last_login_at");
        user.setLastLoginAt(lastLoginAt != null ? lastLoginAt.toLocalDateTime() : null);

        return user;
    }
}
