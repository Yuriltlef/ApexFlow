// [file name]: OrderInfoDAO.java (修复版本)
package com.apex.core.dao;

import com.apex.core.model.OrderInfo;
import com.apex.util.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderInfo Data Access Object with comprehensive English logging
 */
public class OrderInfoDAO implements IOrderInfoDAO {
    private static final Logger logger = LoggerFactory.getLogger(OrderInfoDAO.class);

    /**
     * Create a new order
     */
    public boolean create(OrderInfo order) {
        String operation = "INSERT_ORDER";
        long startTime = System.currentTimeMillis();

        String sql = """
            INSERT INTO apexflow_order (id, user_id, total_amount, status,
                                       payment_method, address_id, created_at,
                                       paid_at, shipped_at, completed_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        logger.info("[{}] Starting order creation. Order ID: {}, User ID: {}",
                operation, order.getId(), order.getUserId());

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 正确设置10个参数
            pstmt.setString(1, order.getId());
            pstmt.setInt(2, order.getUserId());
            pstmt.setBigDecimal(3, order.getTotalAmount());
            pstmt.setInt(4, order.getStatus());

            if (order.getPaymentMethod() != null) {
                pstmt.setString(5, order.getPaymentMethod());
            } else {
                pstmt.setNull(5, Types.VARCHAR);
            }

            if (order.getAddressId() != null) {
                pstmt.setInt(6, order.getAddressId());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }

            pstmt.setTimestamp(7, Timestamp.valueOf(order.getCreatedAt()));

            if (order.getPaidAt() != null) {
                pstmt.setTimestamp(8, Timestamp.valueOf(order.getPaidAt()));
            } else {
                pstmt.setNull(8, Types.TIMESTAMP);
            }

            if (order.getShippedAt() != null) {
                pstmt.setTimestamp(9, Timestamp.valueOf(order.getShippedAt()));
            } else {
                pstmt.setNull(9, Types.TIMESTAMP);
            }

            if (order.getCompletedAt() != null) {
                pstmt.setTimestamp(10, Timestamp.valueOf(order.getCompletedAt()));
            } else {
                pstmt.setNull(10, Types.TIMESTAMP);
            }

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected == 1;

            long duration = System.currentTimeMillis() - startTime;

            if (success) {
                logger.info("[{}] Order created successfully in {} ms. Order ID: {}, Rows affected: {}",
                        operation, duration, order.getId(), rowsAffected);
            } else {
                logger.warn("[{}] Order creation failed. No rows affected. Order ID: {}",
                        operation, order.getId());
            }

            return success;

        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[{}] Failed to create order after {} ms. Order ID: {}, Error: {}",
                    operation, duration, order.getId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Find order by ID
     */
    public OrderInfo findById(String orderId) {
        String operation = "SELECT_ORDER_BY_ID";
        long startTime = System.currentTimeMillis();

        String sql = "SELECT * FROM apexflow_order WHERE id = ?";

        logger.debug("[{}] Searching for order by ID: {}", operation, orderId);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, orderId);

            try (ResultSet rs = pstmt.executeQuery()) {
                OrderInfo result = rs.next() ? mapToOrderInfo(rs) : null;
                long duration = System.currentTimeMillis() - startTime;

                if (result != null) {
                    logger.debug("[{}] Order found in {} ms. Order ID: {}, Status: {}",
                            operation, duration, orderId, result.getStatus());
                } else {
                    logger.debug("[{}] No order found with ID: {} (searched for {} ms)",
                            operation, orderId, duration);
                }

                return result;
            }
        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[{}] Error searching for order ID: {} after {} ms. Error: {}",
                    operation, orderId, duration, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Update order information
     */
    public boolean update(OrderInfo order) {
        String operation = "UPDATE_ORDER";
        long startTime = System.currentTimeMillis();

        String sql = """
            UPDATE apexflow_order
            SET user_id = ?, total_amount = ?, status = ?,
                payment_method = ?, address_id = ?, created_at = ?,
                paid_at = ?, shipped_at = ?, completed_at = ?
            WHERE id = ?
            """;

        logger.info("[{}] Starting order update. Order ID: {}, New Status: {}",
                operation, order.getId(), order.getStatus());

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 正确设置10个参数，id在第10个位置
            pstmt.setInt(1, order.getUserId());
            pstmt.setBigDecimal(2, order.getTotalAmount());
            pstmt.setInt(3, order.getStatus());

            if (order.getPaymentMethod() != null) {
                pstmt.setString(4, order.getPaymentMethod());
            } else {
                pstmt.setNull(4, Types.VARCHAR);
            }

            if (order.getAddressId() != null) {
                pstmt.setInt(5, order.getAddressId());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }

            pstmt.setTimestamp(6, Timestamp.valueOf(order.getCreatedAt()));

            if (order.getPaidAt() != null) {
                pstmt.setTimestamp(7, Timestamp.valueOf(order.getPaidAt()));
            } else {
                pstmt.setNull(7, Types.TIMESTAMP);
            }

            if (order.getShippedAt() != null) {
                pstmt.setTimestamp(8, Timestamp.valueOf(order.getShippedAt()));
            } else {
                pstmt.setNull(8, Types.TIMESTAMP);
            }

            if (order.getCompletedAt() != null) {
                pstmt.setTimestamp(9, Timestamp.valueOf(order.getCompletedAt()));
            } else {
                pstmt.setNull(9, Types.TIMESTAMP);
            }

            pstmt.setString(10, order.getId());

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected == 1;

            long duration = System.currentTimeMillis() - startTime;

            if (success) {
                logger.info("[{}] Order updated successfully in {} ms. Order ID: {}, Rows affected: {}",
                        operation, duration, order.getId(), rowsAffected);
            } else {
                logger.warn("[{}] Order update did not affect any rows. Order ID: {} (took {} ms)",
                        operation, order.getId(), duration);
            }

            return success;
        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[{}] Failed to update order after {} ms. Order ID: {}, Error: {}",
                    operation, duration, order.getId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Update order status only
     */
    public boolean updateStatus(String orderId, int status) {
        String operation = "UPDATE_ORDER_STATUS";
        long startTime = System.currentTimeMillis();

        String sql = "UPDATE apexflow_order SET status = ? WHERE id = ?";

        logger.info("[{}] Updating order status. Order ID: {}, New Status: {}",
                operation, orderId, status);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, status);
            pstmt.setString(2, orderId);

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected == 1;

            long duration = System.currentTimeMillis() - startTime;

            if (success) {
                logger.info("[{}] Order status updated successfully in {} ms. Order ID: {}, New Status: {}",
                        operation, duration, orderId, status);
            } else {
                logger.warn("[{}] No order found to update status. Order ID: {} (took {} ms)",
                        operation, orderId, duration);
            }

            return success;
        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[{}] Failed to update order status after {} ms. Order ID: {}, Error: {}",
                    operation, duration, orderId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Delete an order
     */
    public boolean delete(String orderId) {
        String operation = "DELETE_ORDER_DISABLED";

        logger.error("[{}] Direct deletion of orders is disabled due to foreign key constraints. Order ID: {}. " +
                        "Please use OrderManager.deleteOrderWithItems() method instead.",
                operation, orderId);

        // 抛出明确的异常，提示调用者使用正确的删除方法
        throw new UnsupportedOperationException(
                "Direct deletion of order '" + orderId + "' is not allowed. " +
                        "Orders have associated order items that must be deleted first. " +
                        "Please use the OrderManager.deleteOrderWithItems() method " +
                        "which handles cascade deletion properly.");
    }

    /**
     * Count total number of orders
     */
    public long count() {
        String operation = "COUNT_ORDERS";
        long startTime = System.currentTimeMillis();

        String sql = "SELECT COUNT(*) FROM apexflow_order";

        logger.debug("[{}] Counting total orders", operation);

        try (Connection conn = ConnectionPool.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            long count = rs.next() ? rs.getLong(1) : 0;
            long duration = System.currentTimeMillis() - startTime;

            logger.info("[{}] Total orders: {} (counted in {} ms)", operation, count, duration);
            return count;
        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[{}] Failed to count orders after {} ms. Error: {}",
                    operation, duration, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Find all orders with pagination
     */
    public List<OrderInfo> findAll(int page, int pageSize) {
        String operation = "SELECT_ALL_ORDERS_PAGINATED";
        long startTime = System.currentTimeMillis();

        List<OrderInfo> orders = new ArrayList<>();
        String sql = """
            SELECT * FROM apexflow_order
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
            """;

        if (page < 1) page = 1;
        int offset = (page - 1) * pageSize;

        logger.info("[{}] Retrieving orders. Page: {}, PageSize: {}, Offset: {}",
                operation, page, pageSize, offset);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    orders.add(mapToOrderInfo(rs));
                    count++;
                }

                long duration = System.currentTimeMillis() - startTime;
                logger.info("[{}] Retrieved {} orders in {} ms. Page: {}, PageSize: {}",
                        operation, count, duration, page, pageSize);

                return orders;
            }
        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[{}] Failed to retrieve orders after {} ms. Page: {}, Error: {}",
                    operation, duration, page, e.getMessage(), e);
            return orders;
        }
    }

    /**
     * Find orders by user ID with pagination
     */
    public List<OrderInfo> findByUserId(int userId, int page, int pageSize) {
        String operation = "SELECT_ORDERS_BY_USER";
        long startTime = System.currentTimeMillis();

        List<OrderInfo> orders = new ArrayList<>();
        String sql = """
            SELECT * FROM apexflow_order
            WHERE user_id = ?
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
            """;

        if (page < 1) page = 1;
        int offset = (page - 1) * pageSize;

        logger.info("[{}] Retrieving orders for user. User ID: {}, Page: {}, PageSize: {}",
                operation, userId, page, pageSize);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    orders.add(mapToOrderInfo(rs));
                    count++;
                }

                long duration = System.currentTimeMillis() - startTime;
                logger.info("[{}] Retrieved {} orders for user {} in {} ms",
                        operation, count, userId, duration);

                return orders;
            }
        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[{}] Failed to retrieve orders for user {} after {} ms. Error: {}",
                    operation, userId, duration, e.getMessage(), e);
            return orders;
        }
    }

    /**
     * Map ResultSet to OrderInfo object
     */
    private OrderInfo mapToOrderInfo(ResultSet rs) throws SQLException {
        OrderInfo order = new OrderInfo();
        order.setId(rs.getString("id"));
        order.setUserId(rs.getInt("user_id"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setStatus(rs.getInt("status"));
        order.setPaymentMethod(rs.getString("payment_method"));

        int addressId = rs.getInt("address_id");
        order.setAddressId(rs.wasNull() ? null : addressId);

        order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

        Timestamp paidAt = rs.getTimestamp("paid_at");
        order.setPaidAt(paidAt != null ? paidAt.toLocalDateTime() : null);

        Timestamp shippedAt = rs.getTimestamp("shipped_at");
        order.setShippedAt(shippedAt != null ? shippedAt.toLocalDateTime() : null);

        Timestamp completedAt = rs.getTimestamp("completed_at");
        order.setCompletedAt(completedAt != null ? completedAt.toLocalDateTime() : null);

        return order;
    }
}
