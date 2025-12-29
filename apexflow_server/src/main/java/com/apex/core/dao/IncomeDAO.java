package com.apex.core.dao;

import com.apex.core.model.Income;
import com.apex.util.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 财务收支数据访问对象
 */
public class IncomeDAO implements IIncomeDAO{
    private static final Logger logger = LoggerFactory.getLogger(IncomeDAO.class);

    /**
     * 创建财务记录
     */
    public boolean create(Income income) {
        String sql = """
            INSERT INTO apexflow_income
            (order_id, type, amount, payment_method, status, transaction_time, remark)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        logger.info("Creating income record for order: {}", income.getOrderId());

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, income.getOrderId());
            pstmt.setString(2, income.getType());
            pstmt.setBigDecimal(3, income.getAmount());

            if (income.getPaymentMethod() != null) {
                pstmt.setString(4, income.getPaymentMethod());
            } else {
                pstmt.setNull(4, Types.VARCHAR);
            }

            pstmt.setInt(5, income.getStatus() != null ? income.getStatus() : 1); // 默认待入账

            if (income.getTransactionTime() != null) {
                pstmt.setTimestamp(6, Timestamp.valueOf(income.getTransactionTime()));
            } else {
                pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            }

            pstmt.setString(7, income.getRemark());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        income.setId(generatedKeys.getInt(1));
                    }
                }
                logger.info("Income record created successfully. ID: {}", income.getId());
                return true;
            }

            return false;

        } catch (SQLException e) {
            logger.error("Failed to create income record for order: {}", income.getOrderId(), e);
            return false;
        }
    }

    /**
     * 根据ID查询财务记录
     */
    public Income findById(Integer id) {
        String sql = "SELECT * FROM apexflow_income WHERE id = ?";

        logger.debug("Finding income record by ID: {}", id);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToIncome(rs);
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find income record by ID: {}", id, e);
        }

        return null;
    }

    /**
     * 根据订单号查询财务记录
     */
    public List<Income> findByOrderId(String orderId) {
        String sql = "SELECT * FROM apexflow_income WHERE order_id = ? ORDER BY transaction_time DESC";
        List<Income> list = new ArrayList<>();

        logger.debug("Finding income records by order ID: {}", orderId);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, orderId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToIncome(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find income records by order ID: {}", orderId, e);
        }

        return list;
    }

    /**
     * 更新财务记录
     */
    public boolean update(Income income) {
        String sql = """
            UPDATE apexflow_income
            SET order_id = ?, type = ?, amount = ?, payment_method = ?,
                status = ?, transaction_time = ?, remark = ?
            WHERE id = ?
            """;

        logger.info("Updating income record ID: {}", income.getId());

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, income.getOrderId());
            pstmt.setString(2, income.getType());
            pstmt.setBigDecimal(3, income.getAmount());

            if (income.getPaymentMethod() != null) {
                pstmt.setString(4, income.getPaymentMethod());
            } else {
                pstmt.setNull(4, Types.VARCHAR);
            }

            pstmt.setInt(5, income.getStatus());

            if (income.getTransactionTime() != null) {
                pstmt.setTimestamp(6, Timestamp.valueOf(income.getTransactionTime()));
            } else {
                pstmt.setNull(6, Types.TIMESTAMP);
            }

            pstmt.setString(7, income.getRemark());
            pstmt.setInt(8, income.getId());

            int rowsAffected = pstmt.executeUpdate();
            logger.info("Income record updated. Rows affected: {}", rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Failed to update income record ID: {}", income.getId(), e);
            return false;
        }
    }

    /**
     * 删除财务记录
     */
    public boolean delete(Integer id) {
        String sql = "DELETE FROM apexflow_income WHERE id = ?";

        logger.warn("Deleting income record ID: {}", id);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            logger.warn("Income record deleted. Rows affected: {}", rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Failed to delete income record ID: {}", id, e);
            return false;
        }
    }

    /**
     * 查询所有财务记录（分页）
     */
    public List<Income> findAll(int page, int pageSize) {
        String sql = "SELECT * FROM apexflow_income ORDER BY transaction_time DESC LIMIT ? OFFSET ?";
        List<Income> list = new ArrayList<>();

        if (page < 1) page = 1;
        int offset = (page - 1) * pageSize;
        logger.debug("Finding all income records. Page: {}, PageSize: {}", page, pageSize);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToIncome(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find all income records", e);
        }

        return list;
    }

    /**
     * 根据类型查询财务记录
     */
    public List<Income> findByType(String type, int page, int pageSize) {
        String sql = "SELECT * FROM apexflow_income WHERE type = ? ORDER BY transaction_time DESC LIMIT ? OFFSET ?";
        List<Income> list = new ArrayList<>();

        if (page < 1) page = 1;
        int offset = (page - 1) * pageSize;
        logger.debug("Finding income records by type: {}", type);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, type);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToIncome(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find income records by type: {}", type, e);
        }

        return list;
    }

    /**
     * 根据状态查询财务记录
     */
    public List<Income> findByStatus(Integer status, int page, int pageSize) {
        String sql = "SELECT * FROM apexflow_income WHERE status = ? ORDER BY transaction_time DESC LIMIT ? OFFSET ?";
        List<Income> list = new ArrayList<>();

        if (page < 1) page = 1;
        int offset = (page - 1) * pageSize;
        logger.debug("Finding income records by status: {}", status);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, status);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToIncome(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find income records by status: {}", status, e);
        }

        return list;
    }

    /**
     * 统计总收入
     */
    public BigDecimal calculateTotalIncome() {
        String sql = "SELECT SUM(amount) FROM apexflow_income WHERE type = 'income' AND status = 2";

        try (Connection conn = ConnectionPool.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getBigDecimal(1);
            }

        } catch (SQLException e) {
            logger.error("Failed to calculate total income", e);
        }

        return BigDecimal.ZERO;
    }

    /**
     * 统计总退款
     */
    public BigDecimal calculateTotalRefund() {
        String sql = "SELECT SUM(amount) FROM apexflow_income WHERE type = 'refund' AND status = 2";

        try (Connection conn = ConnectionPool.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                BigDecimal result = rs.getBigDecimal(1);
                return result != null ? result.abs() : BigDecimal.ZERO;
            }

        } catch (SQLException e) {
            logger.error("Failed to calculate total refund", e);
        }

        return BigDecimal.ZERO;
    }

    /**
     * 更新财务状态
     */
    public boolean updateStatus(Integer id, Integer status) {
        String sql = "UPDATE apexflow_income SET status = ? WHERE id = ?";

        logger.info("Updating income status. ID: {}, New Status: {}", id, status);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, status);
            pstmt.setInt(2, id);

            int rowsAffected = pstmt.executeUpdate();
            logger.info("Income status updated. Rows affected: {}", rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Failed to update income status. ID: {}", id, e);
            return false;
        }
    }

    /**
     * 映射ResultSet到Income对象
     */
    private Income mapToIncome(ResultSet rs) throws SQLException {
        Income income = new Income();
        income.setId(rs.getInt("id"));
        income.setOrderId(rs.getString("order_id"));
        income.setType(rs.getString("type"));
        income.setAmount(rs.getBigDecimal("amount"));
        income.setPaymentMethod(rs.getString("payment_method"));
        income.setStatus(rs.getInt("status"));

        Timestamp transactionTime = rs.getTimestamp("transaction_time");
        if (transactionTime != null) {
            income.setTransactionTime(transactionTime.toLocalDateTime());
        }

        income.setRemark(rs.getString("remark"));

        return income;
    }
}
