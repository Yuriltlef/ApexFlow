package com.apex.core.dao;

import com.apex.core.model.OrderItem;
import com.apex.util.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单商品明细数据访问对象
 */
public class OrderItemDAO implements IOrderItemDAO {
    private static final Logger logger = LoggerFactory.getLogger(OrderItemDAO.class);

    /**
     * 创建订单项
     */
    public boolean create(OrderItem orderItem) {
        String sql = """
            INSERT INTO apexflow_order_item
            (order_id, product_id, product_name, quantity, price, subtotal)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        logger.info("Creating order item for order: {}", orderItem.getOrderId());

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, orderItem.getOrderId());
            pstmt.setInt(2, orderItem.getProductId());
            pstmt.setString(3, orderItem.getProductName());
            pstmt.setInt(4, orderItem.getQuantity());
            pstmt.setBigDecimal(5, orderItem.getPrice());
            pstmt.setBigDecimal(6, orderItem.getSubtotal());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        orderItem.setId(generatedKeys.getInt(1));
                    }
                }
                logger.info("Order item created successfully. ID: {}", orderItem.getId());
                return true;
            }

            return false;

        } catch (SQLException e) {
            logger.error("Failed to create order item for order: {}", orderItem.getOrderId(), e);
            return false;
        }
    }

    /**
     * 根据ID查询订单项
     */
    public OrderItem findById(Integer id) {
        String sql = "SELECT * FROM apexflow_order_item WHERE id = ?";

        logger.debug("Finding order item by ID: {}", id);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToOrderItem(rs);
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find order item by ID: {}", id, e);
        }

        return null;
    }

    /**
     * 根据订单号查询订单项
     */
    public List<OrderItem> findByOrderId(String orderId) {
        String sql = "SELECT * FROM apexflow_order_item WHERE order_id = ?";
        List<OrderItem> list = new ArrayList<>();

        logger.debug("Finding order items by order ID: {}", orderId);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, orderId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToOrderItem(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find order items by order ID: {}", orderId, e);
        }

        return list;
    }

    /**
     * 更新订单项
     */
    public boolean update(OrderItem orderItem) {
        String sql = """
            UPDATE apexflow_order_item
            SET order_id = ?, product_id = ?, product_name = ?,
                quantity = ?, price = ?, subtotal = ?
            WHERE id = ?
            """;

        logger.info("Updating order item ID: {}", orderItem.getId());

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, orderItem.getOrderId());
            pstmt.setInt(2, orderItem.getProductId());
            pstmt.setString(3, orderItem.getProductName());
            pstmt.setInt(4, orderItem.getQuantity());
            pstmt.setBigDecimal(5, orderItem.getPrice());
            pstmt.setBigDecimal(6, orderItem.getSubtotal());
            pstmt.setInt(7, orderItem.getId());

            int rowsAffected = pstmt.executeUpdate();
            logger.info("Order item updated. Rows affected: {}", rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Failed to update order item ID: {}", orderItem.getId(), e);
            return false;
        }
    }

    /**
     * 删除订单项
     */
    public boolean delete(Integer id) {
        String sql = "DELETE FROM apexflow_order_item WHERE id = ?";

        logger.warn("Deleting order item ID: {}", id);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            logger.warn("Order item deleted. Rows affected: {}", rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Failed to delete order item ID: {}", id, e);
            return false;
        }
    }

    /**
     * 批量创建订单项
     */
    public boolean createBatch(List<OrderItem> orderItems) {
        String sql = """
            INSERT INTO apexflow_order_item
            (order_id, product_id, product_name, quantity, price, subtotal)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        logger.info("Creating batch order items. Count: {}", orderItems.size());

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            for (OrderItem item : orderItems) {
                pstmt.setString(1, item.getOrderId());
                pstmt.setInt(2, item.getProductId());
                pstmt.setString(3, item.getProductName());
                pstmt.setInt(4, item.getQuantity());
                pstmt.setBigDecimal(5, item.getPrice());
                pstmt.setBigDecimal(6, item.getSubtotal());
                pstmt.addBatch();
            }

            int[] rowsAffected = pstmt.executeBatch();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                int index = 0;
                while (generatedKeys.next() && index < orderItems.size()) {
                    orderItems.get(index).setId(generatedKeys.getInt(1));
                    index++;
                }
            }

            logger.info("Batch order items created successfully. Total rows affected: {}", rowsAffected.length);
            return true;

        } catch (SQLException e) {
            logger.error("Failed to create batch order items", e);
            return false;
        }
    }

    /**
     * 统计订单总金额
     */
    public BigDecimal calculateOrderTotal(String orderId) {
        String sql = "SELECT SUM(subtotal) FROM apexflow_order_item WHERE order_id = ?";

        logger.debug("Calculating total for order: {}", orderId);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, orderId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal(1);
                    logger.info("Order total calculated: {}", total);
                    return total != null ? total : BigDecimal.ZERO;
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to calculate order total for order: {}", orderId, e);
        }

        return BigDecimal.ZERO;
    }

    /**
     * 统计商品销售数量
     */
    public Integer countProductSales(Integer productId) {
        String sql = """
            SELECT SUM(quantity) FROM apexflow_order_item oi
            JOIN apexflow_order o ON oi.order_id = o.id
            WHERE oi.product_id = ? AND o.status IN (2, 3, 4)
            """;

        logger.debug("Counting sales for product: {}", productId);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    logger.info("Product sales count: {}", count);
                    return count;
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to count product sales for product: {}", productId, e);
        }

        return 0;
    }

    /**
     * 获取热门商品
     */
    public List<Integer> getTopProducts(int limit) {
        String sql = """
            SELECT product_id, SUM(quantity) as total_quantity
            FROM apexflow_order_item oi
            JOIN apexflow_order o ON oi.order_id = o.id
            WHERE o.status IN (2, 3, 4)
            GROUP BY product_id
            ORDER BY total_quantity DESC
            LIMIT ?
            """;

        List<Integer> productIds = new ArrayList<>();
        logger.debug("Getting top {} products", limit);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    productIds.add(rs.getInt("product_id"));
                }
            }

            logger.info("Found {} top products", productIds.size());

        } catch (SQLException e) {
            logger.error("Failed to get top products", e);
        }

        return productIds;
    }

    /**
     * 映射ResultSet到OrderItem对象
     */
    private OrderItem mapToOrderItem(ResultSet rs) throws SQLException {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(rs.getInt("id"));
        orderItem.setOrderId(rs.getString("order_id"));
        orderItem.setProductId(rs.getInt("product_id"));
        orderItem.setProductName(rs.getString("product_name"));
        orderItem.setQuantity(rs.getInt("quantity"));
        orderItem.setPrice(rs.getBigDecimal("price"));
        orderItem.setSubtotal(rs.getBigDecimal("subtotal"));

        return orderItem;
    }
}
