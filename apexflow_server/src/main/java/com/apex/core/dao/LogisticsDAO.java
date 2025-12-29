package com.apex.core.dao;

import com.apex.core.model.Logistics;
import com.apex.util.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 物流信息数据访问对象
 */
public class LogisticsDAO implements ILogisticsDAO {
    private static final Logger logger = LoggerFactory.getLogger(LogisticsDAO.class);

    /**
     * 创建物流信息
     */
    public boolean create(Logistics logistics) {
        String sql = """
            INSERT INTO apexflow_logistics
            (order_id, express_company, tracking_number, status,
             sender_address, receiver_address, shipped_at, delivered_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        logger.info("Creating logistics record for order: {}", logistics.getOrderId());

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, logistics.getOrderId());

            if (logistics.getExpressCompany() != null) {
                pstmt.setString(2, logistics.getExpressCompany());
            } else {
                pstmt.setNull(2, Types.VARCHAR);
            }

            if (logistics.getTrackingNumber() != null) {
                pstmt.setString(3, logistics.getTrackingNumber());
            } else {
                pstmt.setNull(3, Types.VARCHAR);
            }

            pstmt.setString(4, logistics.getStatus() != null ? logistics.getStatus() : "pending");

            if (logistics.getSenderAddress() != null) {
                pstmt.setString(5, logistics.getSenderAddress());
            } else {
                pstmt.setNull(5, Types.VARCHAR);
            }

            if (logistics.getReceiverAddress() != null) {
                pstmt.setString(6, logistics.getReceiverAddress());
            } else {
                pstmt.setNull(6, Types.VARCHAR);
            }

            if (logistics.getShippedAt() != null) {
                pstmt.setTimestamp(7, Timestamp.valueOf(logistics.getShippedAt()));
            } else {
                pstmt.setNull(7, Types.TIMESTAMP);
            }

            if (logistics.getDeliveredAt() != null) {
                pstmt.setTimestamp(8, Timestamp.valueOf(logistics.getDeliveredAt()));
            } else {
                pstmt.setNull(8, Types.TIMESTAMP);
            }

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        logistics.setId(generatedKeys.getInt(1));
                    }
                }
                logger.info("Logistics record created successfully. ID: {}", logistics.getId());
                return true;
            }

            return false;

        } catch (SQLException e) {
            logger.error("Failed to create logistics record for order: {}", logistics.getOrderId(), e);
            return false;
        }
    }

    /**
     * 根据ID查询物流信息
     */
    public Logistics findById(Integer id) {
        String sql = "SELECT * FROM apexflow_logistics WHERE id = ?";

        logger.debug("Finding logistics record by ID: {}", id);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToLogistics(rs);
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find logistics record by ID: {}", id, e);
        }

        return null;
    }

    /**
     * 根据订单号查询物流信息
     */
    public Logistics findByOrderId(String orderId) {
        String sql = "SELECT * FROM apexflow_logistics WHERE order_id = ?";

        logger.debug("Finding logistics record by order ID: {}", orderId);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, orderId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToLogistics(rs);
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find logistics record by order ID: {}", orderId, e);
        }

        return null;
    }

    /**
     * 更新物流信息
     */
    public boolean update(Logistics logistics) {
        String sql = """
            UPDATE apexflow_logistics
            SET express_company = ?, tracking_number = ?, status = ?,
                sender_address = ?, receiver_address = ?,
                shipped_at = ?, delivered_at = ?
            WHERE order_id = ?
            """;

        logger.info("Updating logistics record for order: {}", logistics.getOrderId());

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (logistics.getExpressCompany() != null) {
                pstmt.setString(1, logistics.getExpressCompany());
            } else {
                pstmt.setNull(1, Types.VARCHAR);
            }

            if (logistics.getTrackingNumber() != null) {
                pstmt.setString(2, logistics.getTrackingNumber());
            } else {
                pstmt.setNull(2, Types.VARCHAR);
            }

            pstmt.setString(3, logistics.getStatus());

            if (logistics.getSenderAddress() != null) {
                pstmt.setString(4, logistics.getSenderAddress());
            } else {
                pstmt.setNull(4, Types.VARCHAR);
            }

            if (logistics.getReceiverAddress() != null) {
                pstmt.setString(5, logistics.getReceiverAddress());
            } else {
                pstmt.setNull(5, Types.VARCHAR);
            }

            if (logistics.getShippedAt() != null) {
                pstmt.setTimestamp(6, Timestamp.valueOf(logistics.getShippedAt()));
            } else {
                pstmt.setNull(6, Types.TIMESTAMP);
            }

            if (logistics.getDeliveredAt() != null) {
                pstmt.setTimestamp(7, Timestamp.valueOf(logistics.getDeliveredAt()));
            } else {
                pstmt.setNull(7, Types.TIMESTAMP);
            }

            pstmt.setString(8, logistics.getOrderId());

            int rowsAffected = pstmt.executeUpdate();
            logger.info("Logistics record updated. Rows affected: {}", rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Failed to update logistics record for order: {}", logistics.getOrderId(), e);
            return false;
        }
    }

    /**
     * 删除物流信息
     */
    public boolean delete(Integer id) {
        String sql = "DELETE FROM apexflow_logistics WHERE id = ?";

        logger.warn("Deleting logistics record ID: {}", id);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            logger.warn("Logistics record deleted. Rows affected: {}", rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Failed to delete logistics record ID: {}", id, e);
            return false;
        }
    }

    /**
     * 更新物流状态
     */
    public boolean updateStatus(String orderId, String status) {
        String sql = "UPDATE apexflow_logistics SET status = ? WHERE order_id = ?";

        logger.info("Updating logistics status. Order: {}, New Status: {}", orderId, status);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setString(2, orderId);

            int rowsAffected = pstmt.executeUpdate();
            logger.info("Logistics status updated. Rows affected: {}", rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Failed to update logistics status for order: {}", orderId, e);
            return false;
        }
    }

    /**
     * 更新发货信息
     */
    public boolean updateShippingInfo(String orderId, String expressCompany,
                                      String trackingNumber, String senderAddress) {
        String sql = """
            UPDATE apexflow_logistics
            SET express_company = ?, tracking_number = ?,
                sender_address = ?, shipped_at = NOW(), status = 'shipped'
            WHERE order_id = ?
            """;

        logger.info("Updating shipping info for order: {}", orderId);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, expressCompany);
            pstmt.setString(2, trackingNumber);
            pstmt.setString(3, senderAddress);
            pstmt.setString(4, orderId);

            int rowsAffected = pstmt.executeUpdate();
            logger.info("Shipping info updated. Rows affected: {}", rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Failed to update shipping info for order: {}", orderId, e);
            return false;
        }
    }

    /**
     * 更新送达信息
     */
    public boolean updateDeliveryInfo(String orderId, LocalDateTime deliveredAt) {
        String sql = "UPDATE apexflow_logistics SET delivered_at = ?, status = 'delivered' WHERE order_id = ?";

        logger.info("Updating delivery info for order: {}", orderId);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(deliveredAt));
            pstmt.setString(2, orderId);

            int rowsAffected = pstmt.executeUpdate();
            logger.info("Delivery info updated. Rows affected: {}", rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Failed to update delivery info for order: {}", orderId, e);
            return false;
        }
    }

    /**
     * 查询待发货的订单
     */
    public List<Logistics> findPendingShipping(int page, int pageSize) {
        String sql = "SELECT * FROM apexflow_logistics WHERE status = 'pending' ORDER BY created_at LIMIT ? OFFSET ?";
        List<Logistics> list = new ArrayList<>();

        if (page < 1) page = 1;
        int offset = (page - 1) * pageSize;
        logger.debug("Finding pending shipping logistics. Page: {}, PageSize: {}", page, pageSize);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToLogistics(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find pending shipping logistics", e);
        }

        return list;
    }

    /**
     * 查询运输中的订单
     */
    public List<Logistics> findInTransit(int page, int pageSize) {
        String sql = "SELECT * FROM apexflow_logistics WHERE status = 'shipped' ORDER BY shipped_at DESC LIMIT ? OFFSET ?";
        List<Logistics> list = new ArrayList<>();

        if (page < 1) page = 1;
        int offset = (page - 1) * pageSize;
        logger.debug("Finding in-transit logistics. Page: {}, PageSize: {}", page, pageSize);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToLogistics(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find in-transit logistics", e);
        }

        return list;
    }

    /**
     * 统计各状态物流数量
     */
    public LogisticsStats getLogisticsStats() {
        String sql = """
            SELECT
                COUNT(CASE WHEN status = 'pending' THEN 1 END) as pending_count,
                COUNT(CASE WHEN status = 'shipped' THEN 1 END) as shipped_count,
                COUNT(CASE WHEN status = 'delivered' THEN 1 END) as delivered_count
            FROM apexflow_logistics
            """;

        logger.debug("Getting logistics statistics");

        try (Connection conn = ConnectionPool.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                LogisticsStats stats = new LogisticsStats();
                stats.setPendingCount(rs.getInt("pending_count"));
                stats.setShippedCount(rs.getInt("shipped_count"));
                stats.setDeliveredCount(rs.getInt("delivered_count"));

                logger.info("Logistics stats: Pending={}, Shipped={}, Delivered={}",
                        stats.getPendingCount(), stats.getShippedCount(), stats.getDeliveredCount());

                return stats;
            }

        } catch (SQLException e) {
            logger.error("Failed to get logistics statistics", e);
        }

        return new LogisticsStats();
    }

    /**
     * 映射ResultSet到Logistics对象
     */
    private Logistics mapToLogistics(ResultSet rs) throws SQLException {
        Logistics logistics = new Logistics();
        logistics.setId(rs.getInt("id"));
        logistics.setOrderId(rs.getString("order_id"));
        logistics.setExpressCompany(rs.getString("express_company"));
        logistics.setTrackingNumber(rs.getString("tracking_number"));
        logistics.setStatus(rs.getString("status"));
        logistics.setSenderAddress(rs.getString("sender_address"));
        logistics.setReceiverAddress(rs.getString("receiver_address"));

        Timestamp shippedAt = rs.getTimestamp("shipped_at");
        if (shippedAt != null) {
            logistics.setShippedAt(shippedAt.toLocalDateTime());
        }

        Timestamp deliveredAt = rs.getTimestamp("delivered_at");
        if (deliveredAt != null) {
            logistics.setDeliveredAt(deliveredAt.toLocalDateTime());
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            logistics.setCreatedAt(createdAt.toLocalDateTime());
        }

        return logistics;
    }

    /**
     * 物流统计信息
     */
    public static class LogisticsStats {
        private int pendingCount;
        private int shippedCount;
        private int deliveredCount;

        public int getPendingCount() {
            return pendingCount;
        }

        public void setPendingCount(int pendingCount) {
            this.pendingCount = pendingCount;
        }

        public int getShippedCount() {
            return shippedCount;
        }

        public void setShippedCount(int shippedCount) {
            this.shippedCount = shippedCount;
        }

        public int getDeliveredCount() {
            return deliveredCount;
        }

        public void setDeliveredCount(int deliveredCount) {
            this.deliveredCount = deliveredCount;
        }
    }
}
