import com.apex.core.dao.ReviewDAO;
import com.apex.core.model.Review;
import com.apex.core.dao.ReviewDAO.RatingDistribution;
import com.apex.util.ConnectionPool;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ReviewDAO单元测试类
 * 使用H2内存数据库进行测试
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReviewDAOTest {

    private ReviewDAO reviewDAO;
    private Connection conn;

    @BeforeAll
    void setUpAll() throws Exception {
        // 确保使用H2内存数据库
        System.setProperty("apexflow.test.h2", "true");

        // 获取连接并初始化数据库
        conn = ConnectionPool.getConnection();
        H2DatabaseInitializer.initialize(conn);

        reviewDAO = new ReviewDAO();
    }

    @AfterAll
    void tearDownAll() {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                // 忽略关闭异常
            }
        }
        ConnectionPool.shutdown();
    }

    @BeforeEach
    void setUp() throws Exception {
        // 每次测试前清理数据
        H2DatabaseInitializer.clearAllTables(conn);

        // 重新插入基础测试数据
        try (var stmt = conn.createStatement()) {
            // 插入用户
            stmt.execute("""
                INSERT INTO apexflow_system_user (id, username, password_hash, salt, real_name) VALUES
                (1, 'user1', 'hash1', 'salt1', '用户1'),
                (2, 'user2', 'hash2', 'salt2', '用户2'),
                (3, 'user3', 'hash3', 'salt3', '用户3')
            """);

            // 插入商品
            stmt.execute("""
                INSERT INTO apexflow_product (id, name, category, price, stock, status) VALUES
                (1, 'iPhone 14 Pro', '手机', 7999.00, 100, 1),
                (2, 'MacBook Pro 16英寸', '电脑', 18999.00, 50, 1),
                (3, '华为Mate 50', '手机', 4999.00, 150, 1)
            """);

            // 插入订单
            stmt.execute("""
                INSERT INTO apexflow_order (id, user_id, total_amount, status, created_at) VALUES
                ('REVIEW001', 1, 7999.00, 4, '2023-12-01 10:00:00'),
                ('REVIEW002', 2, 18999.00, 4, '2023-12-02 11:00:00'),
                ('REVIEW003', 3, 4999.00, 4, '2023-12-03 12:00:00'),
                ('REVIEW004', 1, 7999.00, 4, '2023-12-04 13:00:00'),
                ('REVIEW005', 2, 18999.00, 4, '2023-12-05 14:00:00')
            """);

            // 插入评价
            stmt.execute("""
                INSERT INTO apexflow_review (id, order_id, product_id, user_id, rating, content, images, is_anonymous, created_at) VALUES
                (1, 'REVIEW001', 1, 1, 5, '手机很好用，拍照很清晰', 'image1.jpg,image2.jpg', FALSE, '2023-12-10 09:00:00'),
                (2, 'REVIEW002', 2, 2, 4, '电脑性能很强，但有点重', 'image3.jpg', FALSE, '2023-12-11 10:00:00'),
                (3, 'REVIEW003', 1, 3, 3, '一般般，电池续航不够', NULL, TRUE, '2023-12-12 11:00:00'),
                (4, 'REVIEW004', 1, 1, 5, '第二次购买了，非常满意', 'image4.jpg,image5.jpg', FALSE, '2023-12-13 12:00:00'),
                (5, 'REVIEW005', 3, 2, 2, '性价比不高，不推荐购买', NULL, FALSE, '2023-12-14 13:00:00')
            """);
        }
    }

    @Test
    @Order(1)
    void testCreateReview_Success() {
        // Arrange
        Review review = new Review();
        review.setOrderId("REVIEW_NEW");
        review.setProductId(2);
        review.setUserId(1);
        review.setRating(5);
        review.setContent("非常棒的商品，强烈推荐！");
        review.setImages("new_image1.jpg,new_image2.jpg");
        review.setAnonymous(false);
        review.setCreatedAt(LocalDateTime.now());

        // 先插入订单
        try (var stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO apexflow_order (id, user_id, total_amount, status) VALUES ('REVIEW_NEW', 1, 18999.00, 4)");
        } catch (Exception e) {
            fail("Failed to insert test order");
        }

        // Act
        boolean result = reviewDAO.create(review);

        // Assert
        assertTrue(result, "评价创建应该成功");
        assertNotNull(review.getId(), "评价应该有ID");

        // Verify
        Review retrieved = reviewDAO.findById(review.getId());
        assertNotNull(retrieved);
        assertEquals("REVIEW_NEW", retrieved.getOrderId());
        assertEquals(2, retrieved.getProductId());
        assertEquals(1, retrieved.getUserId());
        assertEquals(5, retrieved.getRating());
        assertEquals("非常棒的商品，强烈推荐！", retrieved.getContent());
    }

    @Test
    @Order(2)
    void testFindById_ExistingReview() {
        // Act
        Review review = reviewDAO.findById(1);

        // Assert
        assertNotNull(review, "应该能找到存在的评价");
        assertEquals("REVIEW001", review.getOrderId());
        assertEquals(1, review.getProductId());
        assertEquals(1, review.getUserId());
        assertEquals(5, review.getRating());
        assertEquals("手机很好用，拍照很清晰", review.getContent());
        assertFalse(review.getAnonymous());
    }

    @Test
    @Order(3)
    void testFindById_NonExistingReview() {
        // Act
        Review review = reviewDAO.findById(999);

        // Assert
        assertNull(review, "不存在的评价应该返回null");
    }

    @Test
    @Order(4)
    void testFindByOrderId() {
        // Act
        Review review = reviewDAO.findByOrderId("REVIEW001");

        // Assert
        assertNotNull(review, "应该能通过订单号找到评价");
        assertEquals(1, review.getId());
        assertEquals(1, review.getProductId());
        assertEquals(5, review.getRating());
    }

    @Test
    @Order(5)
    void testFindByProductId() {
        // Act - 获取商品1的评价，第一页，每页10条
        List<Review> reviews = reviewDAO.findByProductId(1, 1, 10);

        // Assert
        assertEquals(3, reviews.size(), "商品1应该有3条评价");

        // 验证排序（应该按创建时间倒序）
        assertTrue(reviews.get(0).getCreatedAt().isAfter(reviews.get(1).getCreatedAt()) ||
                        reviews.get(0).getCreatedAt().isEqual(reviews.get(1).getCreatedAt()),
                "应该按创建时间倒序排列");
    }

    @Test
    @Order(6)
    void testFindByProductId_Pagination() {
        // Act - 第一页，每页2条
        List<Review> page1 = reviewDAO.findByProductId(1, 1, 2);

        // Assert
        assertEquals(2, page1.size(), "第一页应该有2条评价");

        // Act - 第二页
        List<Review> page2 = reviewDAO.findByProductId(1, 2, 2);

        // Assert
        assertEquals(1, page2.size(), "第二页应该有1条评价");

        // 验证没有重复
        int id1 = page1.get(0).getId();
        boolean foundInPage2 = page2.stream().anyMatch(r -> r.getId() == id1);
        assertFalse(foundInPage2, "两页之间不应该有重复记录");
    }

    @Test
    @Order(7)
    void testFindByUserId() {
        // Act - 获取用户1的评价
        List<Review> reviews = reviewDAO.findByUserId(1, 1, 10);

        // Assert
        assertEquals(2, reviews.size(), "用户1应该有2条评价");

        // 验证都是用户1的评价
        boolean allUser1 = reviews.stream().allMatch(r -> r.getUserId() == 1);
        assertTrue(allUser1, "所有评价都应该是用户1的");
    }

    @Test
    @Order(8)
    void testUpdateReview_Success() {
        // Arrange
        Review review = reviewDAO.findById(1);
        assertNotNull(review);

        // 修改评价
        review.setRating(4);
        review.setContent("更新后的评价：手机不错，但价格有点高");
        review.setImages("updated_image.jpg");
        review.setAnonymous(true);

        // Act
        boolean result = reviewDAO.update(review);

        // Assert
        assertTrue(result, "更新应该成功");

        // Verify
        Review updated = reviewDAO.findById(1);
        assertNotNull(updated);
        assertEquals(4, updated.getRating());
        assertEquals("更新后的评价：手机不错，但价格有点高", updated.getContent());
        assertEquals("updated_image.jpg", updated.getImages());
        assertTrue(updated.getAnonymous());
    }

    @Test
    @Order(9)
    void testDeleteReview_Success() {
        // Arrange - 确保评价存在
        Review review = reviewDAO.findById(1);
        assertNotNull(review, "删除前评价应该存在");

        // Act
        boolean result = reviewDAO.delete(1);

        // Assert
        assertTrue(result, "删除应该成功");

        // Verify
        Review deleted = reviewDAO.findById(1);
        assertNull(deleted, "删除后应该找不到评价");
    }

    @Test
    @Order(10)
    void testGetAverageRating() {
        // Act - 获取商品1的平均评分
        Double averageRating = reviewDAO.getAverageRating(1);

        // Assert
        assertNotNull(averageRating);
        // 商品1有3条评价：5分、3分、5分，平均分 = (5+3+5)/3 = 4.333...
        assertEquals(4.33, averageRating, 0.01, "商品1的平均评分应该是4.33");
    }

    @Test
    @Order(11)
    void testGetAverageRating_NoReviews() {
        // Act - 获取没有评价的商品
        Double averageRating = reviewDAO.getAverageRating(999);

        // Assert
        assertEquals(0.0, averageRating, 0.0, "没有评价的商品平均评分应该为0");
    }

    @Test
    @Order(12)
    void testGetRatingDistribution() {
        // Act - 获取商品1的评分分布
        RatingDistribution distribution = reviewDAO.getRatingDistribution(1);

        // Assert
        assertNotNull(distribution);
        // 商品1有：5星2个，3星1个，其他0个
        assertEquals(2, distribution.getRating5(), "5星评价应该是2个");
        assertEquals(0, distribution.getRating4(), "4星评价应该是0个");
        assertEquals(1, distribution.getRating3(), "3星评价应该是1个");
        assertEquals(0, distribution.getRating2(), "2星评价应该是0个");
        assertEquals(0, distribution.getRating1(), "1星评价应该是0个");
        assertEquals(3, distribution.getTotal(), "总评价数应该是3个");
    }

    @Test
    @Order(13)
    void testFindReviewsWithImages() {
        // Act - 获取商品1的带图片评价，限制3条
        List<Review> reviewsWithImages = reviewDAO.findReviewsWithImages(1, 3);

        // Assert
        assertEquals(2, reviewsWithImages.size(), "商品1应该有2条带图片的评价");

        // 验证都有图片
        boolean allHaveImages = reviewsWithImages.stream()
                .allMatch(r -> r.getImages() != null && !r.getImages().isEmpty());
        assertTrue(allHaveImages, "所有返回的评价都应该有图片");
    }

    @Test
    @Order(14)
    void testFindLatestReviews() {
        // Act - 获取最新的3条评价
        List<Review> latestReviews = reviewDAO.findLatestReviews(3);

        // Assert
        assertEquals(3, latestReviews.size(), "应该返回3条最新评价");

        // 验证按时间倒序
        for (int i = 0; i < latestReviews.size() - 1; i++) {
            LocalDateTime currentTime = latestReviews.get(i).getCreatedAt();
            LocalDateTime nextTime = latestReviews.get(i + 1).getCreatedAt();
            assertTrue(currentTime.isAfter(nextTime) || currentTime.isEqual(nextTime),
                    "应该按创建时间倒序排列");
        }
    }

    @Test
    @Order(15)
    void testCountByProductId() {
        // Act
        Integer count = reviewDAO.countByProductId(1);

        // Assert
        assertEquals(3, count, "商品1应该有3条评价");
    }

    @Test
    @Order(16)
    void testCountByProductId_NoReviews() {
        // Act
        Integer count = reviewDAO.countByProductId(999);

        // Assert
        assertEquals(0, count, "不存在的商品评价数应该为0");
    }

    @Test
    @Order(17)
    void testAnonymousReview() {
        // Arrange - 创建匿名评价
        Review review = new Review();
        review.setOrderId("REVIEW_ANON");
        review.setProductId(2);
        review.setUserId(1);
        review.setRating(4);
        review.setContent("匿名评价");
        review.setAnonymous(true);

        // 先插入订单
        try (var stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO apexflow_order (id, user_id, total_amount, status) VALUES ('REVIEW_ANON', 1, 18999.00, 4)");
        } catch (Exception e) {
            fail("Failed to insert test order");
        }

        // Act
        boolean result = reviewDAO.create(review);

        // Assert
        assertTrue(result, "匿名评价创建应该成功");

        // Verify
        Review retrieved = reviewDAO.findById(review.getId());
        assertNotNull(retrieved);
        assertTrue(retrieved.getAnonymous(), "评价应该是匿名的");
    }

    @Test
    @Order(18)
    void testReviewWithNullContent() {
        // Arrange - 创建没有内容的评价
        Review review = new Review();
        review.setOrderId("REVIEW_NO_CONTENT");
        review.setProductId(2);
        review.setUserId(1);
        review.setRating(5);
        // content为null
        review.setImages("image.jpg");
        review.setAnonymous(false);

        // 先插入订单
        try (var stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO apexflow_order (id, user_id, total_amount, status) VALUES ('REVIEW_NO_CONTENT', 1, 18999.00, 4)");
        } catch (Exception e) {
            fail("Failed to insert test order");
        }

        // Act
        boolean result = reviewDAO.create(review);

        // Assert
        assertTrue(result, "没有内容的评价创建应该成功");

        // Verify
        Review retrieved = reviewDAO.findById(review.getId());
        assertNotNull(retrieved);
        assertNull(retrieved.getContent(), "评价内容应该为null");
    }

    @ParameterizedTest
    @CsvSource({
            "1, 3",  // 商品1有3条评价
            "2, 1",  // 商品2有1条评价
            "3, 1",  // 商品3有1条评价
            "999, 0" // 不存在的商品
    })
    @Order(19)
    void testCountByProductId_Parameterized(int productId, int expectedCount) {
        // Act
        Integer count = reviewDAO.countByProductId(productId);

        // Assert
        assertEquals(expectedCount, count,
                String.format("商品%d应该有%d条评价", productId, expectedCount));
    }

    @Test
    @Order(20)
    void testRatingDistributionForAllRatings() {
        // 插入更多评价，覆盖所有评分
        try (var stmt = conn.createStatement()) {
            // 商品2：插入1星、2星评价
            stmt.execute("INSERT INTO apexflow_order (id, user_id, total_amount, status) VALUES ('REVIEW_R1', 1, 18999.00, 4)");
            stmt.execute("INSERT INTO apexflow_review (order_id, product_id, user_id, rating) VALUES ('REVIEW_R1', 2, 1, 1)");

            stmt.execute("INSERT INTO apexflow_order (id, user_id, total_amount, status) VALUES ('REVIEW_R2', 2, 18999.00, 4)");
            stmt.execute("INSERT INTO apexflow_review (order_id, product_id, user_id, rating) VALUES ('REVIEW_R2', 2, 2, 2)");

            // 商品2现在有：4星(原数据)、1星、2星
        } catch (Exception e) {
            fail("Failed to insert rating distribution test data");
        }

        // Act
        RatingDistribution distribution = reviewDAO.getRatingDistribution(2);

        // Assert
        assertNotNull(distribution);
        assertEquals(1, distribution.getRating1(), "应该有1个1星评价");
        assertEquals(1, distribution.getRating2(), "应该有1个2星评价");
        assertEquals(0, distribution.getRating3(), "应该有0个3星评价");
        assertEquals(1, distribution.getRating4(), "应该有1个4星评价");
        assertEquals(0, distribution.getRating5(), "应该有0个5星评价");
        assertEquals(3, distribution.getTotal(), "总评价数应该是3个");
    }
}
