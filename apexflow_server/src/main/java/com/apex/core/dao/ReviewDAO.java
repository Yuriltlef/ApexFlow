package com.apex.core.dao;

import com.apex.core.dto.RatingDistribution;
import com.apex.core.model.Review;
import com.apex.util.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品评价数据访问对象
 */
public class ReviewDAO implements IReviewDAO {
    private static final Logger logger = LoggerFactory.getLogger(ReviewDAO.class);

    /**
     * 创建评价
     */
    public boolean create(Review review) {
        String sql = """
            INSERT INTO apexflow_review
            (order_id, product_id, user_id, rating, content, images, is_anonymous)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        logger.info("Creating review for product: {} by user: {}", review.getProductId(), review.getUserId());

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, review.getOrderId());
            pstmt.setInt(2, review.getProductId());
            pstmt.setInt(3, review.getUserId());
            pstmt.setInt(4, review.getRating());

            if (review.getContent() != null) {
                pstmt.setString(5, review.getContent());
            } else {
                pstmt.setNull(5, Types.VARCHAR);
            }

            if (review.getImages() != null) {
                pstmt.setString(6, review.getImages());
            } else {
                pstmt.setNull(6, Types.VARCHAR);
            }

            pstmt.setBoolean(7, review.getAnonymous() != null ? review.getAnonymous() : false);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        review.setId(generatedKeys.getInt(1));
                    }
                }
                logger.info("Review created successfully. ID: {}", review.getId());
                return true;
            }

            return false;

        } catch (SQLException e) {
            logger.error("Failed to create review for product: {}", review.getProductId(), e);
            return false;
        }
    }

    /**
     * 根据ID查询评价
     */
    public Review findById(Integer id) {
        String sql = "SELECT * FROM apexflow_review WHERE id = ?";

        logger.debug("Finding review by ID: {}", id);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToReview(rs);
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find review by ID: {}", id, e);
        }

        return null;
    }

    /**
     * 根据订单号查询评价
     */
    public Review findByOrderId(String orderId) {
        String sql = "SELECT * FROM apexflow_review WHERE order_id = ?";

        logger.debug("Finding review by order ID: {}", orderId);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, orderId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToReview(rs);
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find review by order ID: {}", orderId, e);
        }

        return null;
    }

    /**
     * 根据商品ID查询评价（分页）
     */
    public List<Review> findByProductId(Integer productId, int page, int pageSize) {
        String sql = "SELECT * FROM apexflow_review WHERE product_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<Review> list = new ArrayList<>();

        if (page < 1) page = 1;
        int offset = (page - 1) * pageSize;
        logger.debug("Finding reviews for product: {}. Page: {}, PageSize: {}", productId, page, pageSize);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToReview(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find reviews for product: {}", productId, e);
        }

        return list;
    }

    /**
     * 根据用户ID查询评价（分页）
     */
    public List<Review> findByUserId(Integer userId, int page, int pageSize) {
        String sql = "SELECT * FROM apexflow_review WHERE user_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<Review> list = new ArrayList<>();

        if (page < 1) page = 1;
        int offset = (page - 1) * pageSize;
        logger.debug("Finding reviews by user: {}. Page: {}, PageSize: {}", userId, page, pageSize);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToReview(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find reviews by user: {}", userId, e);
        }

        return list;
    }

    /**
     * 更新评价
     */
    public boolean update(Review review) {
        String sql = """
            UPDATE apexflow_review
            SET rating = ?, content = ?, images = ?, is_anonymous = ?
            WHERE id = ?
            """;

        logger.info("Updating review ID: {}", review.getId());

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, review.getRating());

            if (review.getContent() != null) {
                pstmt.setString(2, review.getContent());
            } else {
                pstmt.setNull(2, Types.VARCHAR);
            }

            if (review.getImages() != null) {
                pstmt.setString(3, review.getImages());
            } else {
                pstmt.setNull(3, Types.VARCHAR);
            }

            pstmt.setBoolean(4, review.getAnonymous() != null ? review.getAnonymous() : false);
            pstmt.setInt(5, review.getId());

            int rowsAffected = pstmt.executeUpdate();
            logger.info("Review updated. Rows affected: {}", rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Failed to update review ID: {}", review.getId(), e);
            return false;
        }
    }

    /**
     * 删除评价
     */
    public boolean delete(Integer id) {
        String sql = "DELETE FROM apexflow_review WHERE id = ?";

        logger.warn("Deleting review ID: {}", id);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            logger.warn("Review deleted. Rows affected: {}", rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Failed to delete review ID: {}", id, e);
            return false;
        }
    }

    /**
     * 获取商品平均评分
     */
    public Double getAverageRating(Integer productId) {
        String sql = "SELECT AVG(rating) as avg_rating FROM apexflow_review WHERE product_id = ?";

        logger.debug("Getting average rating for product: {}", productId);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Double avgRating = rs.getDouble("avg_rating");
                    logger.info("Average rating for product {}: {}", productId, avgRating);
                    return rs.wasNull() ? 0.0 : avgRating;
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to get average rating for product: {}", productId, e);
        }

        return 0.0;
    }

    /**
     * 获取商品评分分布
     */
    public RatingDistribution getRatingDistribution(Integer productId) {
        String sql = """
            SELECT
                COUNT(CASE WHEN rating = 1 THEN 1 END) as rating_1,
                COUNT(CASE WHEN rating = 2 THEN 1 END) as rating_2,
                COUNT(CASE WHEN rating = 3 THEN 1 END) as rating_3,
                COUNT(CASE WHEN rating = 4 THEN 1 END) as rating_4,
                COUNT(CASE WHEN rating = 5 THEN 1 END) as rating_5
            FROM apexflow_review WHERE product_id = ?
            """;

        logger.debug("Getting rating distribution for product: {}", productId);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    RatingDistribution distribution = new RatingDistribution();
                    distribution.setRating1(rs.getInt("rating_1"));
                    distribution.setRating2(rs.getInt("rating_2"));
                    distribution.setRating3(rs.getInt("rating_3"));
                    distribution.setRating4(rs.getInt("rating_4"));
                    distribution.setRating5(rs.getInt("rating_5"));

                    logger.info("Rating distribution for product {}: {}", productId, distribution);
                    return distribution;
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to get rating distribution for product: {}", productId, e);
        }

        return new RatingDistribution();
    }

    /**
     * 获取带图片的评价
     */
    public List<Review> findReviewsWithImages(Integer productId, int limit) {
        String sql = "SELECT * FROM apexflow_review WHERE product_id = ? AND images IS NOT NULL ORDER BY created_at DESC LIMIT ?";
        List<Review> list = new ArrayList<>();

        logger.debug("Finding reviews with images for product: {}. Limit: {}", productId, limit);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            pstmt.setInt(2, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToReview(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find reviews with images for product: {}", productId, e);
        }

        return list;
    }

    /**
     * 获取最新评价
     */
    public List<Review> findLatestReviews(int limit) {
        String sql = "SELECT * FROM apexflow_review ORDER BY created_at DESC LIMIT ?";
        List<Review> list = new ArrayList<>();

        logger.debug("Finding latest reviews. Limit: {}", limit);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToReview(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to find latest reviews", e);
        }

        return list;
    }

    /**
     * 统计商品评价数量
     */
    public Integer countByProductId(Integer productId) {
        String sql = "SELECT COUNT(*) FROM apexflow_review WHERE product_id = ?";

        logger.debug("Counting reviews for product: {}", productId);

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    logger.info("Review count for product {}: {}", productId, count);
                    return count;
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to count reviews for product: {}", productId, e);
        }

        return 0;
    }

    /**
     * 映射ResultSet到Review对象
     */
    private Review mapToReview(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setId(rs.getInt("id"));
        review.setOrderId(rs.getString("order_id"));
        review.setProductId(rs.getInt("product_id"));
        review.setUserId(rs.getInt("user_id"));
        review.setRating(rs.getInt("rating"));
        review.setContent(rs.getString("content"));
        review.setImages(rs.getString("images"));
        review.setAnonymous(rs.getBoolean("is_anonymous"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            review.setCreatedAt(createdAt.toLocalDateTime());
        }

        return review;
    }

    /**
     * Count total number of reviews
     */
    public long count() {
        String operation = "COUNT_REVIEWS";
        long startTime = System.currentTimeMillis();

        String sql = "SELECT COUNT(*) FROM apexflow_review";

        logger.debug("[{}] Counting total reviews", operation);

        try (Connection conn = ConnectionPool.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            long count = rs.next() ? rs.getLong(1) : 0;
            long duration = System.currentTimeMillis() - startTime;

            logger.info("[{}] Total reviews: {} (counted in {} ms)", operation, count, duration);
            return count;
        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[{}] Failed to count reviews after {} ms. Error: {}",
                    operation, duration, e.getMessage(), e);
            return 0;
        }
    }
}
