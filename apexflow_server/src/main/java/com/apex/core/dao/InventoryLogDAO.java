package com.apex.core.dao;

import com.apex.core.model.InventoryLog;
import com.apex.util.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 库存变更日志数据访问对象
 */
public class InventoryLogDAO implements IInventoryLogDAO {
    private static final Logger logger = LoggerFactory.getLogger(InventoryLogDAO.class);

    /**
     * 创建库存变更日志
     */
    public boolean create(InventoryLog inventoryLog) {
        String sql = """
            INSERT INTO apexflow_inventory_log
            (product_id, change_type, quantity, before_stock, after_stock, order_id)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        logger.info("Creating inventory log for product: {}, type: {}, quantity: {}",
                inventoryLog.getProductId(), inventoryLog.getChangeType(), inventoryLog.getQuantity());

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, inventoryLog.getProductId());
            pstmt.setString(2, inventoryLog.getChangeType());
            pstmt.setInt(3, inventoryLog.getQuantity());
            pstmt.setInt(4, inventoryLog.getBeforeStock());
            pstmt.setInt(5, inventoryLog.getAfterStock());

            if (inventoryLog.getOrderId() != null) {
                pstmt.setString(6, inventoryLog.getOrderId());
            } else {
                pstmt.setNull(6, Types.VARCHAR);
            }

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        inventoryLog.setId(generatedKeys.getInt(1));
                    }
                }
                logger.info("Inventory log created successfully. ID: {}", inventoryLog.getId());
                return true;
            }

            return false;

        } catch (SQLException e) {
            logger.error("Failed to create inventory log for product: {}", inventoryLog.getProductId(), e);
            return false;
        }
    }

    /**
     * 根据ID查询库存变更日志
     */
    public InventoryLog findById(Integer id) {
        String sql = "SELECT * FROM apexflow_inventory_log WHERE id = ?";

        logger.debug("Finding inventory log by ID: {}", id);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToInventoryLog(rs);
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find inventory log by ID: {}", id, e);
        }

        return null;
    }

    /**
     * 根据商品ID查询库存变更日志（分页）
     */
    public List<InventoryLog> findByProductId(Integer productId, int page, int pageSize) {
        String sql = "SELECT * FROM apexflow_inventory_log WHERE product_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<InventoryLog> list = new ArrayList<>();

        if (page < 1) page = 1;
        int offset = (page - 1) * pageSize;
        logger.debug("Finding inventory logs for product: {}. Page: {}, PageSize: {}", productId, page, pageSize);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToInventoryLog(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find inventory logs for product: {}", productId, e);
        }

        return list;
    }

    /**
     * 根据订单号查询库存变更日志
     */
    public List<InventoryLog> findByOrderId(String orderId) {
        String sql = "SELECT * FROM apexflow_inventory_log WHERE order_id = ? ORDER BY created_at DESC";
        List<InventoryLog> list = new ArrayList<>();

        logger.debug("Finding inventory logs for order: {}", orderId);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, orderId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToInventoryLog(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find inventory logs for order: {}", orderId, e);
        }

        return list;
    }

    /**
     * 根据变更类型查询库存变更日志（分页）
     */
    public List<InventoryLog> findByChangeType(String changeType, int page, int pageSize) {
        String sql = "SELECT * FROM apexflow_inventory_log WHERE change_type = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<InventoryLog> list = new ArrayList<>();

        if (page < 1) page = 1;
        int offset = (page - 1) * pageSize;
        logger.debug("Finding inventory logs by type: {}. Page: {}, PageSize: {}", changeType, page, pageSize);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, changeType);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToInventoryLog(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find inventory logs by type: {}", changeType, e);
        }

        return list;
    }

    /**
     * 获取商品最近一次库存变更
     */
    public InventoryLog findLatestByProductId(Integer productId) {
        String sql = "SELECT * FROM apexflow_inventory_log WHERE product_id = ? ORDER BY created_at DESC LIMIT 1";

        logger.debug("Finding latest inventory log for product: {}", productId);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToInventoryLog(rs);
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find latest inventory log for product: {}", productId, e);
        }

        return null;
    }

    /**
     * 统计商品采购总量
     */
    public Integer calculatePurchaseQuantity(Integer productId) {
        String sql = "SELECT SUM(quantity) FROM apexflow_inventory_log WHERE product_id = ? AND change_type = 'purchase'";

        logger.debug("Calculating purchase quantity for product: {}", productId);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int quantity = rs.getInt(1);
                    logger.info("Purchase quantity for product {}: {}", productId, quantity);
                    return rs.wasNull() ? 0 : quantity;
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to calculate purchase quantity for product: {}", productId, e);
        }

        return 0;
    }

    /**
     * 统计商品销售总量
     */
    public Integer calculateSalesQuantity(Integer productId) {
        String sql = "SELECT ABS(SUM(quantity)) FROM apexflow_inventory_log WHERE product_id = ? AND change_type = 'sale'";

        logger.debug("Calculating sales quantity for product: {}", productId);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int quantity = rs.getInt(1);
                    logger.info("Sales quantity for product {}: {}", productId, quantity);
                    return rs.wasNull() ? 0 : quantity;
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to calculate sales quantity for product: {}", productId, e);
        }

        return 0;
    }

    /**
     * 获取库存预警商品
     */
    public List<Integer> getLowStockProducts(int threshold) {
        String sql = """
            SELECT p.id, p.stock, p.name
            FROM apexflow_product p
            WHERE p.stock <= ? AND p.status = 1
            ORDER BY p.stock ASC
            """;

        List<Integer> productIds = new ArrayList<>();
        logger.debug("Finding low stock products (threshold: {})", threshold);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, threshold);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    productIds.add(rs.getInt("id"));
                    logger.warn("Low stock product: {} (ID: {}, Stock: {})",
                            rs.getString("name"), rs.getInt("id"), rs.getInt("stock"));
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find low stock products", e);
        }

        return productIds;
    }

    /**
     * 获取最近库存变更
     */
    public List<InventoryLog> findRecentChanges(int limit) {
        String sql = "SELECT * FROM apexflow_inventory_log ORDER BY created_at DESC LIMIT ?";
        List<InventoryLog> list = new ArrayList<>();

        logger.debug("Finding recent inventory changes. Limit: {}", limit);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToInventoryLog(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find recent inventory changes", e);
        }

        return list;
    }

    /**
     * 批量创建库存变更日志
     */
    public boolean createBatch(List<InventoryLog> inventoryLogs) {
        String sql = """
            INSERT INTO apexflow_inventory_log
            (product_id, change_type, quantity, before_stock, after_stock, order_id)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        logger.info("Creating batch inventory logs. Count: {}", inventoryLogs.size());

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            for (InventoryLog log : inventoryLogs) {
                pstmt.setInt(1, log.getProductId());
                pstmt.setString(2, log.getChangeType());
                pstmt.setInt(3, log.getQuantity());
                pstmt.setInt(4, log.getBeforeStock());
                pstmt.setInt(5, log.getAfterStock());

                if (log.getOrderId() != null) {
                    pstmt.setString(6, log.getOrderId());
                } else {
                    pstmt.setNull(6, Types.VARCHAR);
                }

                pstmt.addBatch();
            }

            int[] rowsAffected = pstmt.executeBatch();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                int index = 0;
                while (generatedKeys.next() && index < inventoryLogs.size()) {
                    inventoryLogs.get(index).setId(generatedKeys.getInt(1));
                    index++;
                }
            }

            logger.info("Batch inventory logs created successfully. Total rows affected: {}", rowsAffected.length);
            return true;

        } catch (SQLException e) {
            logger.error("Failed to create batch inventory logs", e);
            return false;
        }
    }

    /**
     * 映射ResultSet到InventoryLog对象
     */
    private InventoryLog mapToInventoryLog(ResultSet rs) throws SQLException {
        InventoryLog inventoryLog = new InventoryLog();
        inventoryLog.setId(rs.getInt("id"));
        inventoryLog.setProductId(rs.getInt("product_id"));
        inventoryLog.setChangeType(rs.getString("change_type"));
        inventoryLog.setQuantity(rs.getInt("quantity"));
        inventoryLog.setBeforeStock(rs.getInt("before_stock"));
        inventoryLog.setAfterStock(rs.getInt("after_stock"));
        inventoryLog.setOrderId(rs.getString("order_id"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            inventoryLog.setCreatedAt(createdAt.toLocalDateTime());
        }

        return inventoryLog;
    }
}
