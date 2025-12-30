import com.apex.core.dao.OrderInfoDAO;
import com.apex.core.model.OrderInfo;
import com.apex.util.ConnectionPool;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OrderInfoDAO单元测试类
 * 使用H2内存数据库进行测试
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderInfoDAOTest {

    private OrderInfoDAO orderInfoDAO;
    private Connection conn;

    @BeforeAll
    void setUpAll() throws Exception {
        // 确保使用H2内存数据库
        System.setProperty("apexflow.test.h2", "true");

        // 获取连接并初始化数据库
        conn = ConnectionPool.getConnection();
        H2DatabaseInitializer.initialize(conn);

        orderInfoDAO = new OrderInfoDAO();
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
            stmt.execute("INSERT INTO apexflow_system_user (id, username, password_hash, salt, real_name) VALUES " +
                    "(1, 'testuser', 'hash', 'salt', 'Test User')");

            // 插入商品
            stmt.execute("""
                        INSERT INTO apexflow_product (id, name, category, price, stock, status) VALUES
                        (1, 'iPhone 14 Pro', '手机', 7999.00, 100, 1),
                        (2, 'MacBook Pro 16英寸', '电脑', 18999.00, 50, 1)
                    """);

            // 插入订单
            stmt.execute("""
                        INSERT INTO apexflow_order (id, user_id, total_amount, status, payment_method, created_at, paid_at) VALUES
                        ('TEST001', 1001, 7999.00, 1, 'alipay', '2023-12-01 10:00:00', NULL),
                        ('TEST002', 1002, 18999.00, 2, 'wxpay', '2023-12-01 11:00:00', '2023-12-01 11:05:00'),
                        ('TEST003', 1001, 299.99, 3, 'alipay', '2023-12-01 12:00:00', '2023-12-01 12:05:00')
                    """);

            // 插入订单项
            stmt.execute("""
                        INSERT INTO apexflow_order_item (order_id, product_id, product_name, quantity, price, subtotal) VALUES
                        ('TEST001', 1, 'iPhone 14 Pro', 1, 7999.00, 7999.00),
                        ('TEST002', 2, 'MacBook Pro 16英寸', 1, 18999.00, 18999.00)
                    """);
        }
    }

    @Test
    @Order(1)
    void testCreateOrder_Success() {
        // Arrange
        OrderInfo newOrder = new OrderInfo();
        newOrder.setId("TEST_CREATE_001");
        newOrder.setUserId(1001);
        newOrder.setTotalAmount(new BigDecimal("149.99"));
        newOrder.setStatus(1);
        newOrder.setPaymentMethod("alipay");
        newOrder.setAddressId(1);
        newOrder.setCreatedAt(LocalDateTime.now());

        // Act
        boolean result = orderInfoDAO.create(newOrder);

        // Assert
        assertTrue(result, "Order creation should return true on success");

        // Verify the order was actually created
        OrderInfo retrievedOrder = orderInfoDAO.findById("TEST_CREATE_001");
        assertNotNull(retrievedOrder, "Created order should be retrievable");
        assertEquals("TEST_CREATE_001", retrievedOrder.getId());
        assertEquals(1001, retrievedOrder.getUserId());
        assertEquals(0, new BigDecimal("149.99").compareTo(retrievedOrder.getTotalAmount()));
    }

    @Test
    @Order(2)
    void testFindById_ExistingOrder() {
        // Act
        OrderInfo order = orderInfoDAO.findById("TEST001");

        // Assert
        assertNotNull(order, "Should find existing order");
        assertEquals("TEST001", order.getId());
        assertEquals(1001, order.getUserId());
        assertEquals(1, order.getStatus());
        assertEquals("alipay", order.getPaymentMethod());
    }

    @Test
    @Order(3)
    void testFindById_NonExistingOrder() {
        // Act
        OrderInfo order = orderInfoDAO.findById("NON_EXISTENT");

        // Assert
        assertNull(order, "Should return null for non-existing order");
    }

    @ParameterizedTest
    @ValueSource(strings = {"TEST001", "TEST002", "TEST003"})
    @Order(4)
    void testFindById_Parameterized(String orderId) {
        // Act
        OrderInfo order = orderInfoDAO.findById(orderId);

        // Assert
        assertNotNull(order, "Should find order with ID: " + orderId);
        assertEquals(orderId, order.getId());
    }

    @Test
    @Order(5)
    void testUpdateOrder_Success() {
        // Arrange
        OrderInfo order = orderInfoDAO.findById("TEST001");
        assertNotNull(order);

        // Modify order
        order.setStatus(2);
        order.setTotalAmount(new BigDecimal("129.99"));
        order.setPaidAt(LocalDateTime.now());

        // Act
        boolean result = orderInfoDAO.update(order);

        // Assert
        assertTrue(result, "Update should succeed");

        // Verify update
        OrderInfo updatedOrder = orderInfoDAO.findById("TEST001");
        assertNotNull(updatedOrder);
        assertEquals(2, updatedOrder.getStatus());
        assertEquals(0, new BigDecimal("129.99").compareTo(updatedOrder.getTotalAmount()));
        assertNotNull(updatedOrder.getPaidAt());
    }

    @Test
    @Order(6)
    void testUpdateStatus_Success() {
        // Act
        boolean result = orderInfoDAO.updateStatus("TEST001", 2);

        // Assert
        assertTrue(result, "Status update should succeed");

        // Verify
        OrderInfo updatedOrder = orderInfoDAO.findById("TEST001");
        assertNotNull(updatedOrder);
        assertEquals(2, updatedOrder.getStatus());
    }

    @Test
    @Order(7)
    void testDeleteOrder_Success() {
        // Arrange - ensure order exists
        OrderInfo order = orderInfoDAO.findById("TEST001");
        assertNotNull(order, "Order should exist before deletion");

        // Act & Assert
        assertThrows(
                UnsupportedOperationException.class,
                () -> orderInfoDAO.delete("TEST001"),
                "Expected delete() to throw UnsupportedOperationException"
        );

        // Verify deletion
        OrderInfo deletedOrder = orderInfoDAO.findById("TEST001");
        assertNotNull(deletedOrder, "Order should be not deleted");
    }

    @Test
    @Order(8)
    void testFindAll_Pagination() {
        // Act - get first page (2 items per page)
        List<OrderInfo> page1 = orderInfoDAO.findAll(1, 2);

        // Assert
        assertEquals(2, page1.size(), "First page should have 2 items");

        // Act - get second page
        List<OrderInfo> page2 = orderInfoDAO.findAll(2, 2);

        // Assert
        assertEquals(1, page2.size(), "Second page should have 1 item");

        // Verify no overlap
        String firstPageId1 = page1.get(0).getId();
        if (!page2.isEmpty()) {
            String secondPageId1 = page2.get(0).getId();
            assertNotEquals(firstPageId1, secondPageId1, "Pages should not have overlapping orders");
        }
    }

    @Test
    @Order(9)
    void testFindByUserId() {
        // Act - get orders for user 1001
        List<OrderInfo> userOrders = orderInfoDAO.findByUserId(1001, 1, 10);

        // Assert
        assertEquals(2, userOrders.size(), "User 1001 should have 2 orders");

        // Verify all orders belong to user 1001
        for (OrderInfo order : userOrders) {
            assertEquals(1001, order.getUserId(), "All orders should belong to user 1001");
        }
    }

    @ParameterizedTest
    @CsvSource({
            "1001, 2",  // User 1001 has 2 orders in test data
            "1002, 1",  // User 1002 has 1 order
            "9999, 0"   // Non-existing user
    })
    @Order(10)
    void testFindByUserId_Parameterized(int userId, int expectedCount) {
        // Act
        List<OrderInfo> orders = orderInfoDAO.findByUserId(userId, 1, 10);

        // Assert
        assertEquals(expectedCount, orders.size(),
                String.format("User %d should have %d orders", userId, expectedCount));
    }

    @Test
    @Order(11)
    void testCount() {
        // Act
        long count = orderInfoDAO.count();

        // Assert
        assertEquals(3, count, "Should count all 3 test orders");
    }

    @Test
    @Order(12)
    void testOrderStatusFlow() {
        // Test a complete order status flow
        OrderInfo order = new OrderInfo();
        order.setId("STATUS_FLOW_TEST");
        order.setUserId(1001);
        order.setTotalAmount(new BigDecimal("199.99"));
        order.setStatus(1); // Pending payment
        order.setPaymentMethod("alipay");
        order.setCreatedAt(LocalDateTime.now());

        // Create order
        assertTrue(orderInfoDAO.create(order));

        // Update to paid
        assertTrue(orderInfoDAO.updateStatus("STATUS_FLOW_TEST", 2));
        OrderInfo paidOrder = orderInfoDAO.findById("STATUS_FLOW_TEST");
        assertNotNull(paidOrder);
        assertEquals(2, paidOrder.getStatus());

        // Update to shipped
        assertTrue(orderInfoDAO.updateStatus("STATUS_FLOW_TEST", 3));

        // Update to completed
        assertTrue(orderInfoDAO.updateStatus("STATUS_FLOW_TEST", 4));
    }

    @Test
    @Order(13)
    void testNullHandling() {
        // Test creating order with null values
        OrderInfo order = new OrderInfo();
        order.setId("NULL_TEST");
        order.setUserId(1001);
        order.setTotalAmount(new BigDecimal("99.99"));
        order.setStatus(1);
        order.setCreatedAt(LocalDateTime.now());
        // addressId is null
        // paymentMethod is null

        // Act
        boolean result = orderInfoDAO.create(order);

        // Assert
        assertTrue(result, "Should handle null values");

        // Verify retrieval
        OrderInfo retrieved = orderInfoDAO.findById("NULL_TEST");
        assertNotNull(retrieved);
        assertNull(retrieved.getAddressId());
        assertNull(retrieved.getPaymentMethod());
    }

    @Test
    @Order(14)
    void testFindAll_EmptyDatabase() {
        // Arrange - clear all data
        H2DatabaseInitializer.clearAllTables(conn);

        // Act
        List<OrderInfo> orders = orderInfoDAO.findAll(1, 10);

        // Assert
        assertTrue(orders.isEmpty(), "Should return empty list for empty database");
    }
}
