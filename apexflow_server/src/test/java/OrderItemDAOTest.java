import com.apex.core.dao.OrderItemDAO;
import com.apex.core.model.OrderItem;
import com.apex.util.ConnectionPool;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OrderItemDAO单元测试类
 * 使用H2内存数据库进行测试
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderItemDAOTest {

    private OrderItemDAO orderItemDAO;
    private Connection conn;

    private static @NotNull List<OrderItem> getOrderItems() {
        List<OrderItem> batchItems = new ArrayList<>();

        OrderItem item1 = new OrderItem();
        item1.setOrderId("ITEM_BATCH_001");
        item1.setProductId(1);
        item1.setProductName("iPhone 14 Pro");
        item1.setQuantity(2);
        item1.setPrice(new BigDecimal("7999.00"));
        item1.setSubtotal(new BigDecimal("15998.00"));

        OrderItem item2 = new OrderItem();
        item2.setOrderId("ITEM_BATCH_001");
        item2.setProductId(2);
        item2.setProductName("MacBook Pro 16英寸");
        item2.setQuantity(1);
        item2.setPrice(new BigDecimal("18999.00"));
        item2.setSubtotal(new BigDecimal("18999.00"));

        batchItems.add(item1);
        batchItems.add(item2);
        return batchItems;
    }

    @BeforeAll
    void setUpAll() throws Exception {
        // 确保使用H2内存数据库
        System.setProperty("apexflow.test.h2", "true");

        // 获取连接并初始化数据库
        conn = ConnectionPool.getConnection();
        H2DatabaseInitializer.initialize(conn);

        orderItemDAO = new OrderItemDAO();
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
            // 插入商品
            stmt.execute("""
                        INSERT INTO apexflow_product (id, name, category, price, stock, status) VALUES
                        (1, 'iPhone 14 Pro', '手机', 7999.00, 100, 1),
                        (2, 'MacBook Pro 16英寸', '电脑', 18999.00, 50, 1),
                        (3, '华为Mate 50', '手机', 4999.00, 150, 1),
                        (4, '小米13', '手机', 3999.00, 200, 1),
                        (5, '戴尔XPS 13', '电脑', 8999.00, 80, 1)
                    """);

            // 插入订单
            stmt.execute("""
                        INSERT INTO apexflow_order (id, user_id, total_amount, status, payment_method, created_at) VALUES
                        ('ITEM001', 1001, 15998.00, 2, 'alipay', '2023-12-01 10:00:00'),
                        ('ITEM002', 1002, 18999.00, 4, 'wxpay', '2023-12-01 11:00:00'),
                        ('ITEM003', 1003, 7998.00, 4, 'alipay', '2023-12-01 12:00:00')
                    """);

            // 插入订单项
            stmt.execute("""
                        INSERT INTO apexflow_order_item (id, order_id, product_id, product_name, quantity, price, subtotal) VALUES
                        (1, 'ITEM001', 1, 'iPhone 14 Pro', 2, 7999.00, 15998.00),
                        (2, 'ITEM002', 2, 'MacBook Pro 16英寸', 1, 18999.00, 18999.00),
                        (3, 'ITEM001', 3, '华为Mate 50', 1, 4999.00, 4999.00),
                        (4, 'ITEM003', 1, 'iPhone 14 Pro', 1, 7999.00, 7999.00)
                    """);
        }
    }

    @Test
    @Order(1)
    void testCreateOrderItem_Success() {
        // Arrange
        OrderItem item = new OrderItem();
        item.setOrderId("ITEM001");
        item.setProductId(4);
        item.setProductName("小米13");
        item.setQuantity(3);
        item.setPrice(new BigDecimal("3999.00"));
        item.setSubtotal(new BigDecimal("11997.00"));

        // Act
        boolean result = orderItemDAO.create(item);

        // Assert
        assertTrue(result, "订单项创建应该成功");
        assertNotNull(item.getId(), "订单项应该有ID");

        // Verify
        OrderItem retrieved = orderItemDAO.findById(item.getId());
        assertNotNull(retrieved);
        assertEquals("ITEM001", retrieved.getOrderId());
        assertEquals(4, retrieved.getProductId());
        assertEquals(3, retrieved.getQuantity());
        assertEquals(0, new BigDecimal("3999.00").compareTo(retrieved.getPrice()));
    }

    @Test
    @Order(2)
    void testFindById_ExistingOrderItem() {
        // Act
        OrderItem item = orderItemDAO.findById(1);

        // Assert
        assertNotNull(item, "应该能找到存在的订单项");
        assertEquals("ITEM001", item.getOrderId());
        assertEquals(1, item.getProductId());
        assertEquals("iPhone 14 Pro", item.getProductName());
        assertEquals(2, item.getQuantity());
        assertEquals(0, new BigDecimal("7999.00").compareTo(item.getPrice()));
    }

    @Test
    @Order(3)
    void testFindById_NonExistingOrderItem() {
        // Act
        OrderItem item = orderItemDAO.findById(999);

        // Assert
        assertNull(item, "不存在的订单项应该返回null");
    }

    @Test
    @Order(4)
    void testFindByOrderId() {
        // Act
        List<OrderItem> items = orderItemDAO.findByOrderId("ITEM001");

        // Assert
        assertEquals(2, items.size(), "订单ITEM001应该有2个商品项");

        // 验证商品信息
        boolean foundiPhone = items.stream().anyMatch(item ->
                item.getProductId() == 1 && item.getProductName().equals("iPhone 14 Pro")
        );
        boolean foundHuawei = items.stream().anyMatch(item ->
                item.getProductId() == 3 && item.getProductName().equals("华为Mate 50")
        );

        assertTrue(foundiPhone, "应该包含iPhone");
        assertTrue(foundHuawei, "应该包含华为手机");
    }

    @Test
    @Order(5)
    void testUpdateOrderItem_Success() {
        // Arrange
        OrderItem item = orderItemDAO.findById(1);
        assertNotNull(item);

        // 修改订单项
        item.setQuantity(3);
        item.setPrice(new BigDecimal("7500.00"));
        item.setSubtotal(new BigDecimal("22500.00"));

        // Act
        boolean result = orderItemDAO.update(item);

        // Assert
        assertTrue(result, "更新应该成功");

        // Verify
        OrderItem updated = orderItemDAO.findById(1);
        assertNotNull(updated);
        assertEquals(3, updated.getQuantity());
        assertEquals(0, new BigDecimal("7500.00").compareTo(updated.getPrice()));
        assertEquals(0, new BigDecimal("22500.00").compareTo(updated.getSubtotal()));
    }

    @Test
    @Order(6)
    void testDeleteOrderItem_Success() {
        // Arrange - 确保订单项存在
        OrderItem item = orderItemDAO.findById(1);
        assertNotNull(item, "删除前订单项应该存在");

        // Act
        boolean result = orderItemDAO.delete(1);

        // Assert
        assertTrue(result, "删除应该成功");

        // Verify
        OrderItem deleted = orderItemDAO.findById(1);
        assertNull(deleted, "删除后应该找不到订单项");
    }

    @Test
    @Order(7)
    void testCreateBatch_Success() {
        // Arrange
        List<OrderItem> batchItems = getOrderItems();

        // 先插入订单
        try (var stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO apexflow_order (id, user_id, total_amount, status) VALUES ('ITEM_BATCH_001', 1001, 34997.00, 1)");
        } catch (Exception e) {
            fail("Failed to insert test order");
        }

        // Act
        boolean result = orderItemDAO.createBatch(batchItems);

        // Assert
        assertTrue(result, "批量创建应该成功");
        assertNotNull(batchItems.get(0).getId(), "第一个订单项应该有ID");
        assertNotNull(batchItems.get(1).getId(), "第二个订单项应该有ID");

        // Verify
        List<OrderItem> retrievedItems = orderItemDAO.findByOrderId("ITEM_BATCH_001");
        assertEquals(2, retrievedItems.size(), "批量创建后应该有2个订单项");
    }

    @Test
    @Order(8)
    void testCalculateOrderTotal() {
        // Act
        BigDecimal total = orderItemDAO.calculateOrderTotal("ITEM001");

        // Assert
        assertNotNull(total);
        assertEquals(0, new BigDecimal("20997.00").compareTo(total),
                "订单ITEM001总金额应该是iPhone(15998) + 华为(4999) = 20997");
    }

    @Test
    @Order(9)
    void testCalculateOrderTotal_EmptyOrder() {
        // Arrange - 创建一个空订单
        try (var stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO apexflow_order (id, user_id, total_amount, status) VALUES ('EMPTY001', 1001, 0, 1)");
        } catch (Exception e) {
            fail("Failed to insert test order");
        }

        // Act
        BigDecimal total = orderItemDAO.calculateOrderTotal("EMPTY001");

        // Assert
        assertNotNull(total);
        assertEquals(0, BigDecimal.ZERO.compareTo(total), "空订单总金额应该为0");
    }

    @Test
    @Order(10)
    void testCountProductSales() {
        // 更新订单状态为已支付
        try (var stmt = conn.createStatement()) {
            stmt.execute("UPDATE apexflow_order SET status = 2 WHERE id = 'ITEM001'");
            stmt.execute("UPDATE apexflow_order SET status = 4 WHERE id = 'ITEM002'");
            stmt.execute("UPDATE apexflow_order SET status = 4 WHERE id = 'ITEM003'");
        } catch (Exception e) {
            fail("Failed to update order status");
        }

        // Act
        Integer salesCount = orderItemDAO.countProductSales(1); // iPhone的销售数量

        // Assert
        assertNotNull(salesCount);
        assertEquals(3, salesCount, "iPhone总销售数量应该是3（ITEM001有2个，ITEM003有1个）");
    }

    @Test
    @Order(11)
    void testCountProductSales_NoSales() {
        // Act
        Integer salesCount = orderItemDAO.countProductSales(999); // 不存在的商品

        // Assert
        assertEquals(0, salesCount, "不存在的商品销售数量应该为0");
    }

    @Test
    @Order(12)
    void testGetTopProducts() {
        // 更新订单状态为已支付
        try (var stmt = conn.createStatement()) {
            stmt.execute("UPDATE apexflow_order SET status = 2 WHERE id = 'ITEM001'");
            stmt.execute("UPDATE apexflow_order SET status = 4 WHERE id = 'ITEM002'");
            stmt.execute("UPDATE apexflow_order SET status = 4 WHERE id = 'ITEM003'");
        } catch (Exception e) {
            fail("Failed to update order status");
        }

        // Act - 获取前3个热门商品
        List<Integer> topProducts = orderItemDAO.getTopProducts(3);

        // Assert
        assertNotNull(topProducts);
        assertEquals(3, topProducts.size(), "应该返回3个热门商品");

        // 验证排序：iPhone销售3个排第一，MacBook销售1个排第二，华为销售1个排第三
        assertEquals(Integer.valueOf(1), topProducts.get(0), "iPhone应该是第一热门商品");
    }

    @ParameterizedTest
    @CsvSource({
            "ITEM001, 20997.00",  // 2个iPhone + 1个华为 = 15998 + 4999 = 20997
            "ITEM002, 18999.00",  // 1个MacBook = 18999
            "ITEM003, 7999.00"    // 1个iPhone = 7999
    })
    @Order(13)
    void testCalculateOrderTotal_Parameterized(String orderId, String expectedTotal) {
        // Act
        BigDecimal total = orderItemDAO.calculateOrderTotal(orderId);

        // Assert
        assertNotNull(total);
        assertEquals(0, new BigDecimal(expectedTotal).compareTo(total),
                String.format("订单%s总金额应该是%s", orderId, expectedTotal));
    }

    @Test
    @Order(14)
    void testBatchOperationWithLargeDataSet() {
        // Arrange
        List<OrderItem> largeBatch = new ArrayList<>();
        int batchSize = 50;

        // 先插入订单
        try (var stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO apexflow_order (id, user_id, total_amount, status) VALUES ('LARGE_BATCH', 1001, 0, 1)");
        } catch (Exception e) {
            fail("Failed to insert test order");
        }

        // 创建大批量订单项
        for (int i = 1; i <= batchSize; i++) {
            OrderItem item = new OrderItem();
            item.setOrderId("LARGE_BATCH");
            item.setProductId(i % 5 + 1); // 循环使用1-5号商品
            item.setProductName("商品" + i);
            item.setQuantity(i);
            item.setPrice(new BigDecimal("100.00"));
            item.setSubtotal(BigDecimal.valueOf(100L * i).setScale(2, RoundingMode.HALF_UP));
            largeBatch.add(item);
        }

        // Act
        boolean result = orderItemDAO.createBatch(largeBatch);

        // Assert
        assertTrue(result, "大批量创建应该成功");

        // Verify
        List<OrderItem> retrieved = orderItemDAO.findByOrderId("LARGE_BATCH");
        assertEquals(batchSize, retrieved.size(), "大批量创建后应该包含所有订单项");

        // 验证ID都被分配了
        boolean allHaveIds = largeBatch.stream().allMatch(item -> item.getId() != null);
        assertTrue(allHaveIds, "所有批量创建的订单项都应该有ID");
    }

    @Test
    @Order(15)
    void testOrderItemDataIntegrity() {
        // 测试删除订单时，外键约束是否生效（订单项应该被级联删除或阻止删除）
        try (var stmt = conn.createStatement()) {
            // 尝试删除有订单项的订单，应该失败
            stmt.execute("DELETE FROM apexflow_order WHERE id = 'ITEM001'");
            fail("应该抛出外键约束异常");
        } catch (Exception e) {
            // 期望的异常，外键约束应该阻止删除
            assertTrue(e.getMessage().contains("REFERENTIAL INTEGRITY") ||
                            e.getMessage().contains("FOREIGN KEY"),
                    "应该因为外键约束而失败");
        }
    }
}
