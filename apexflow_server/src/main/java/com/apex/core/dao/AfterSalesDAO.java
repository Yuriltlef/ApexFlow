package com.apex.core.dao;

import com.apex.core.model.AfterSales;
import com.apex.util.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 售后服务数据访问对象
 */
public class AfterSalesDAO implements IAfterSalesDAO {
    private static final Logger logger = LoggerFactory.getLogger(AfterSalesDAO.class);

    /**
     * 创建售后服务记录
     */
    public boolean create(AfterSales afterSales) {
        String sql = """
            INSERT INTO apexflow_after_sales
            (order_id, type, reason, status, refund_amount, apply_time, process_time, process_remark)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        logger.info("Creating after sales record for order: {}", afterSales.getOrderId());

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, afterSales.getOrderId());
            pstmt.setInt(2, afterSales.getType());
            pstmt.setString(3, afterSales.getReason());
            pstmt.setInt(4, afterSales.getStatus() != null ? afterSales.getStatus() : 1); // 默认申请中

            if (afterSales.getRefundAmount() != null) {
                pstmt.setBigDecimal(5, afterSales.getRefundAmount());
            } else {
                pstmt.setNull(5, Types.DECIMAL);
            }

            if (afterSales.getApplyTime() != null) {
                pstmt.setTimestamp(6, Timestamp.valueOf(afterSales.getApplyTime()));
            } else {
                pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            }

            if (afterSales.getProcessTime() != null) {
                pstmt.setTimestamp(7, Timestamp.valueOf(afterSales.getProcessTime()));
            } else {
                pstmt.setNull(7, Types.TIMESTAMP);
            }

            pstmt.setString(8, afterSales.getProcessRemark());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        afterSales.setId(generatedKeys.getInt(1));
                    }
                }
                logger.info("After sales record created successfully. ID: {}", afterSales.getId());
                return true;
            }

            return false;

        } catch (SQLException e) {
            logger.error("Failed to create after sales record for order: {}", afterSales.getOrderId(), e);
            return false;
        }
    }

    /**
     * 根据ID查询售后服务记录
     */
    public AfterSales findById(Integer id) {
        String sql = "SELECT * FROM apexflow_after_sales WHERE id = ?";

        logger.debug("Finding after sales record by ID: {}", id);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToAfterSales(rs);
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find after sales record by ID: {}", id, e);
        }

        return null;
    }

    /**
     * 根据订单号查询售后服务记录
     */
    public List<AfterSales> findByOrderId(String orderId) {
        String sql = "SELECT * FROM apexflow_after_sales WHERE order_id = ? ORDER BY apply_time DESC";
        List<AfterSales> list = new ArrayList<>();

        logger.debug("Finding after sales records by order ID: {}", orderId);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, orderId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToAfterSales(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find after sales records by order ID: {}", orderId, e);
        }

        return list;
    }

    /**
     * 更新售后服务记录
     */
    public boolean update(AfterSales afterSales) {
        String sql = """
            UPDATE apexflow_after_sales
            SET order_id = ?, type = ?, reason = ?, status = ?,
                refund_amount = ?, apply_time = ?, process_time = ?, process_remark = ?
            WHERE id = ?
            """;

        logger.info("Updating after sales record ID: {}", afterSales.getId());

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, afterSales.getOrderId());
            pstmt.setInt(2, afterSales.getType());
            pstmt.setString(3, afterSales.getReason());
            pstmt.setInt(4, afterSales.getStatus());

            if (afterSales.getRefundAmount() != null) {
                pstmt.setBigDecimal(5, afterSales.getRefundAmount());
            } else {
                pstmt.setNull(5, Types.DECIMAL);
            }

            if (afterSales.getApplyTime() != null) {
                pstmt.setTimestamp(6, Timestamp.valueOf(afterSales.getApplyTime()));
            } else {
                pstmt.setNull(6, Types.TIMESTAMP);
            }

            if (afterSales.getProcessTime() != null) {
                pstmt.setTimestamp(7, Timestamp.valueOf(afterSales.getProcessTime()));
            } else {
                pstmt.setNull(7, Types.TIMESTAMP);
            }

            pstmt.setString(8, afterSales.getProcessRemark());
            pstmt.setInt(9, afterSales.getId());

            int rowsAffected = pstmt.executeUpdate();
            logger.info("After sales record updated. Rows affected: {}", rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Failed to update after sales record ID: {}", afterSales.getId(), e);
            return false;
        }
    }

    /**
     * 删除售后服务记录
     */
    public boolean delete(Integer id) {
        String sql = "DELETE FROM apexflow_after_sales WHERE id = ?";

        logger.warn("Deleting after sales record ID: {}", id);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            logger.warn("After sales record deleted. Rows affected: {}", rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Failed to delete after sales record ID: {}", id, e);
            return false;
        }
    }

    /**
     * 查询所有售后服务记录（分页）
     */
    public List<AfterSales> findAll(int page, int pageSize) {
        String sql = "SELECT * FROM apexflow_after_sales ORDER BY apply_time DESC LIMIT ? OFFSET ?";
        List<AfterSales> list = new ArrayList<>();

        if (page < 1) page = 1;
        int offset = (page - 1) * pageSize;
        logger.debug("Finding all after sales records. Page: {}, PageSize: {}", page, pageSize);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToAfterSales(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find all after sales records", e);
        }

        return list;
    }

    /**
     * 根据状态查询售后服务记录
     */
    public List<AfterSales> findByStatus(Integer status, int page, int pageSize) {
        String sql = "SELECT * FROM apexflow_after_sales WHERE status = ? ORDER BY apply_time DESC LIMIT ? OFFSET ?";
        List<AfterSales> list = new ArrayList<>();

        if (page < 1) page = 1;
        int offset = (page - 1) * pageSize;
        logger.debug("Finding after sales records by status: {}", status);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, status);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToAfterSales(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find after sales records by status: {}", status, e);
        }

        return list;
    }

    /**
     * 统计售后服务记录总数
     */
    public long count() {
        String sql = "SELECT COUNT(*) FROM apexflow_after_sales";

        try (Connection conn = ConnectionPool.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getLong(1);
            }

        } catch (SQLException e) {
            logger.error("Failed to count after sales records", e);
        }

        return 0;
    }

    /**
     * 更新售后状态
     */
    public boolean updateStatus(Integer id, Integer status, String processRemark) {
        String sql = "UPDATE apexflow_after_sales SET status = ?, process_time = NOW(), process_remark = ? WHERE id = ?";

        logger.info("Updating after sales status. ID: {}, New Status: {}", id, status);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, status);
            pstmt.setString(2, processRemark);
            pstmt.setInt(3, id);

            int rowsAffected = pstmt.executeUpdate();
            logger.info("After sales status updated. Rows affected: {}", rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Failed to update after sales status. ID: {}", id, e);
            return false;
        }
    }

    /**
     * 映射ResultSet到AfterSales对象
     */
    private AfterSales mapToAfterSales(ResultSet rs) throws SQLException {
        AfterSales afterSales = new AfterSales();
        afterSales.setId(rs.getInt("id"));
        afterSales.setOrderId(rs.getString("order_id"));
        afterSales.setType(rs.getInt("type"));
        afterSales.setReason(rs.getString("reason"));
        afterSales.setStatus(rs.getInt("status"));
        afterSales.setRefundAmount(rs.getBigDecimal("refund_amount"));

        Timestamp applyTime = rs.getTimestamp("apply_time");
        if (applyTime != null) {
            afterSales.setApplyTime(applyTime.toLocalDateTime());
        }

        Timestamp processTime = rs.getTimestamp("process_time");
        if (processTime != null) {
            afterSales.setProcessTime(processTime.toLocalDateTime());
        }

        afterSales.setProcessRemark(rs.getString("process_remark"));

        return afterSales;
    }
}
