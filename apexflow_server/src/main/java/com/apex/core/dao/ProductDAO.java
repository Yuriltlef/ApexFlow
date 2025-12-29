package com.apex.core.dao;

import com.apex.core.model.Product;
import com.apex.util.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品数据访问对象
 */
public class ProductDAO implements IProductDAO {
    private static final Logger logger = LoggerFactory.getLogger(ProductDAO.class);

    /**
     * 创建商品
     */
    public boolean create(Product product) {
        String sql = """
            INSERT INTO apexflow_product
            (name, category, price, stock, status, image, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        logger.info("Creating product: {}", product.getName());

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getCategory());
            pstmt.setBigDecimal(3, product.getPrice());
            pstmt.setInt(4, product.getStock() != null ? product.getStock() : 0);
            pstmt.setInt(5, product.getStatus() != null ? product.getStatus() : 1); // 默认上架

            if (product.getImage() != null) {
                pstmt.setString(6, product.getImage());
            } else {
                pstmt.setNull(6, Types.VARCHAR);
            }

            if (product.getCreatedAt() != null) {
                pstmt.setTimestamp(7, Timestamp.valueOf(product.getCreatedAt()));
            } else {
                pstmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            }

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        product.setId(generatedKeys.getInt(1));
                    }
                }
                logger.info("Product created successfully. ID: {}", product.getId());
                return true;
            }

            return false;

        } catch (SQLException e) {
            logger.error("Failed to create product: {}", product.getName(), e);
            return false;
        }
    }

    /**
     * 根据ID查询商品
     */
    public Product findById(Integer id) {
        String sql = "SELECT * FROM apexflow_product WHERE id = ?";

        logger.debug("Finding product by ID: {}", id);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToProduct(rs);
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find product by ID: {}", id, e);
        }

        return null;
    }

    /**
     * 更新商品
     */
    public boolean update(Product product) {
        String sql = """
            UPDATE apexflow_product
            SET name = ?, category = ?, price = ?, stock = ?,
                status = ?, image = ?, created_at = ?
            WHERE id = ?
            """;

        logger.info("Updating product ID: {}", product.getId());

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getCategory());
            pstmt.setBigDecimal(3, product.getPrice());
            pstmt.setInt(4, product.getStock());
            pstmt.setInt(5, product.getStatus());

            if (product.getImage() != null) {
                pstmt.setString(6, product.getImage());
            } else {
                pstmt.setNull(6, Types.VARCHAR);
            }

            if (product.getCreatedAt() != null) {
                pstmt.setTimestamp(7, Timestamp.valueOf(product.getCreatedAt()));
            } else {
                pstmt.setNull(7, Types.TIMESTAMP);
            }

            pstmt.setInt(8, product.getId());

            int rowsAffected = pstmt.executeUpdate();
            logger.info("Product updated. Rows affected: {}", rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Failed to update product ID: {}", product.getId(), e);
            return false;
        }
    }

    /**
     * 删除商品
     */
    public boolean delete(Integer id) {
        String operation = "DELETE_ORDER_DISABLED";

        logger.error("[{}] Direct deletion of product is disabled due to foreign key constraints. Order ID: {}. " +
                        "Please use ProductManager.deleteProductWithItems() method instead.",
                operation, id);

        // 抛出明确的异常，提示调用者使用正确的删除方法
        throw new UnsupportedOperationException(
                "Direct deletion of product '" + id + "' is not allowed. " +
                        "Products have associated product items that must be deleted first. " +
                        "Please use the ProductManager.deleteProductWithItems() method " +
                        "which handles cascade deletion properly.");
    }

    /**
     * 查询所有商品（分页）
     */
    public List<Product> findAll(int page, int pageSize) {
        String sql = "SELECT * FROM apexflow_product ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<Product> list = new ArrayList<>();

        if (page < 1) page = 1;
        int offset = (page - 1) * pageSize;
        logger.debug("Finding all products. Page: {}, PageSize: {}", page, pageSize);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToProduct(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find all products", e);
        }

        return list;
    }

    /**
     * 根据分类查询商品
     */
    public List<Product> findByCategory(String category, int page, int pageSize) {
        String sql = "SELECT * FROM apexflow_product WHERE category = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<Product> list = new ArrayList<>();

        if (page < 1) page = 1;
        int offset = (page - 1) * pageSize;
        logger.debug("Finding products by category: {}", category);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, category);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToProduct(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find products by category: {}", category, e);
        }

        return list;
    }

    /**
     * 根据状态查询商品
     */
    public List<Product> findByStatus(Integer status, int page, int pageSize) {
        String sql = "SELECT * FROM apexflow_product WHERE status = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<Product> list = new ArrayList<>();

        if (page < 1) page = 1;
        int offset = (page - 1) * pageSize;
        logger.debug("Finding products by status: {}", status);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, status);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToProduct(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find products by status: {}", status, e);
        }

        return list;
    }

    /**
     * 根据名称搜索商品
     */
    public List<Product> searchByName(String keyword, int page, int pageSize) {
        String sql = "SELECT * FROM apexflow_product WHERE name LIKE ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<Product> list = new ArrayList<>();

        if (page < 1) page = 1;
        int offset = (page - 1) * pageSize;
        logger.debug("Searching products by name keyword: {}", keyword);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToProduct(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to search products by name keyword: {}", keyword, e);
        }

        return list;
    }

    /**
     * 更新商品库存
     */
    public boolean updateStock(Integer id, Integer newStock) {
        String sql = "UPDATE apexflow_product SET stock = ? WHERE id = ?";

        logger.info("Updating product stock. ID: {}, New Stock: {}", id, newStock);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, newStock);
            pstmt.setInt(2, id);

            int rowsAffected = pstmt.executeUpdate();
            logger.info("Product stock updated. Rows affected: {}", rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Failed to update product stock. ID: {}", id, e);
            return false;
        }
    }

    /**
     * 增加商品库存
     */
    public boolean increaseStock(Integer id, Integer quantity) {
        String sql = "UPDATE apexflow_product SET stock = stock + ? WHERE id = ?";

        logger.info("Increasing product stock. ID: {}, Quantity: {}", id, quantity);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, quantity);
            pstmt.setInt(2, id);

            int rowsAffected = pstmt.executeUpdate();
            logger.info("Product stock increased. Rows affected: {}", rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Failed to increase product stock. ID: {}", id, e);
            return false;
        }
    }

    /**
     * 减少商品库存
     */
    public boolean decreaseStock(Integer id, Integer quantity) {
        String sql = "UPDATE apexflow_product SET stock = stock - ? WHERE id = ? AND stock >= ?";

        logger.info("Decreasing product stock. ID: {}, Quantity: {}", id, quantity);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, quantity);
            pstmt.setInt(2, id);
            pstmt.setInt(3, quantity);

            int rowsAffected = pstmt.executeUpdate();
            logger.info("Product stock decreased. Rows affected: {}", rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Failed to decrease product stock. ID: {}", id, e);
            return false;
        }
    }

    /**
     * 更新商品状态
     */
    public boolean updateStatus(Integer id, Integer status) {
        String sql = "UPDATE apexflow_product SET status = ? WHERE id = ?";

        logger.info("Updating product status. ID: {}, New Status: {}", id, status);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, status);
            pstmt.setInt(2, id);

            int rowsAffected = pstmt.executeUpdate();
            logger.info("Product status updated. Rows affected: {}", rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Failed to update product status. ID: {}", id, e);
            return false;
        }
    }

    /**
     * 统计商品总数
     */
    public long count() {
        String sql = "SELECT COUNT(*) FROM apexflow_product";

        try (Connection conn = ConnectionPool.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getLong(1);
            }

        } catch (SQLException e) {
            logger.error("Failed to count products", e);
        }

        return 0;
    }

    /**
     * 统计分类下的商品数量
     */
    public long countByCategory(String category) {
        String sql = "SELECT COUNT(*) FROM apexflow_product WHERE category = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, category);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to count products by category: {}", category, e);
        }

        return 0;
    }

    /**
     * 映射ResultSet到Product对象
     */
    private Product mapToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setName(rs.getString("name"));
        product.setCategory(rs.getString("category"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setStock(rs.getInt("stock"));
        product.setStatus(rs.getInt("status"));
        product.setImage(rs.getString("image"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            product.setCreatedAt(createdAt.toLocalDateTime());
        }

        return product;
    }
}
