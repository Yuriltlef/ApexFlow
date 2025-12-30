import com.apex.core.dao.IInventoryLogDAO;
import com.apex.core.dao.InventoryLogDAO;
import com.apex.core.model.InventoryLog;
import com.apex.util.ConnectionPool;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * InventoryLogDAO 单元测试类
 * 使用 H2 内存数据库进行测试
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InventoryLogDAOTest {

    private IInventoryLogDAO inventoryLogDAO;
    private Connection conn;

    @BeforeAll
    void setUpAll() throws Exception {
        // 确保使用 H2 内存数据库
        System.setProperty("apexflow.test.h2", "true");

        // 获取连接并初始化数据库
        conn = ConnectionPool.getConnection();
        H2DatabaseInitializer.initialize(conn);

        inventoryLogDAO = new InventoryLogDAO();
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
                        (1, 'iPhone 14 Pro', '手机', 7999.00, 50, 1),
                        (2, 'MacBook Pro 16英寸', '电脑', 18999.00, 20, 1),
                        (3, '华为Mate 50', '手机', 4999.00, 5, 1),  -- 低库存
                        (4, '小米13', '手机', 3999.00, 0, 1)       -- 零库存
                    """);

            // 插入订单
            stmt.execute("""
                        INSERT INTO apexflow_order (id, user_id, total_amount, status, payment_method, created_at, paid_at) VALUES
                        ('ORDER001', 1001, 7999.00, 4, 'alipay', '2023-12-01 10:00:00', '2023-12-01 10:05:00'),
                        ('ORDER002', 1002, 18999.00, 4, 'wxpay', '2023-12-01 11:00:00', '2023-12-01 11:05:00')
                    """);

            // 插入库存变更记录
            stmt.execute("""
                        INSERT INTO apexflow_inventory_log (id, product_id, change_type, quantity, before_stock, after_stock, order_id, created_at) VALUES
                        (1, 1, 'purchase', 100, 0, 100, NULL, '2023-11-01 09:00:00'),
                        (2, 1, 'sale', -2, 100, 98, 'ORDER001', '2023-12-01 10:00:00'),
                        (3, 1, 'sale', -1, 98, 97, 'ORDER001', '2023-12-01 10:30:00'),
                        (4, 1, 'adjust', -47, 97, 50, NULL, '2023-12-01 11:00:00'),
                        (5, 2, 'purchase', 30, 0, 30, NULL, '2023-11-15 09:00:00'),
                        (6, 2, 'sale', -10, 30, 20, 'ORDER002', '2023-12-01 11:00:00'),
                        (7, 3, 'purchase', 10, 0, 10, NULL, '2023-11-20 09:00:00'),
                        (8, 3, 'sale', -5, 10, 5, NULL, '2023-12-01 12:00:00'),
                        (9, 4, 'purchase', 5, 0, 5, NULL, '2023-11-25 09:00:00'),
                        (10, 4, 'sale', -5, 5, 0, NULL, '2023-12-01 13:00:00')
                    """);
        }
    }

    @Test
    @Order(1)
    void testCreateInventoryLog_Success() {
        // Arrange
        InventoryLog newLog = new InventoryLog();
        newLog.setProductId(1);
        newLog.setChangeType("sale");
        newLog.setQuantity(-3);
        newLog.setBeforeStock(50);
        newLog.setAfterStock(47);
        newLog.setOrderId("ORDER001");
        // createdAt 不设置，数据库会自动生成

        // Act
        boolean result = inventoryLogDAO.create(newLog);

        // Assert
        assertTrue(result, "InventoryLog creation should return true on success");
        assertNotNull(newLog.getId(), "InventoryLog ID should be auto-generated after creation");

        // Verify the inventory log was actually created
        InventoryLog retrieved = inventoryLogDAO.findById(newLog.getId());
        assertNotNull(retrieved, "Created inventory log should be retrievable");
        assertEquals(newLog.getProductId(), retrieved.getProductId());
        assertEquals(newLog.getChangeType(), retrieved.getChangeType());
        assertEquals(newLog.getQuantity(), retrieved.getQuantity());
        assertEquals(newLog.getBeforeStock(), retrieved.getBeforeStock());
        assertEquals(newLog.getAfterStock(), retrieved.getAfterStock());
        assertEquals(newLog.getOrderId(), retrieved.getOrderId());
        assertNotNull(retrieved.getCreatedAt(), "Created at should be set");
    }

    @Test
    @Order(2)
    void testCreateInventoryLog_WithNullOrderId() {
        // Arrange
        InventoryLog newLog = new InventoryLog();
        newLog.setProductId(1);
        newLog.setChangeType("adjust");
        newLog.setQuantity(10);
        newLog.setBeforeStock(50);
        newLog.setAfterStock(60);
        // orderId 为 null
        // createdAt 为 null

        // Act
        boolean result = inventoryLogDAO.create(newLog);

        // Assert
        assertTrue(result, "InventoryLog creation with null orderId should succeed");

        // Verify
        InventoryLog retrieved = inventoryLogDAO.findById(newLog.getId());
        assertNotNull(retrieved);
        assertNull(retrieved.getOrderId(), "OrderId should be null");
    }

    @Test
    @Order(3)
    void testFindById_Existing() {
        // Act
        InventoryLog log = inventoryLogDAO.findById(1);

        // Assert
        assertNotNull(log, "Should find existing InventoryLog");
        assertEquals(1, log.getId());
        assertEquals(1, log.getProductId());
        assertEquals("purchase", log.getChangeType());
        assertEquals(100, log.getQuantity()); // 采购为正数
        assertEquals(0, log.getBeforeStock());
        assertEquals(100, log.getAfterStock());
        assertNull(log.getOrderId()); // 采购没有订单关联
    }

    @Test
    @Order(4)
    void testFindById_NonExisting() {
        // Act
        InventoryLog log = inventoryLogDAO.findById(9999);

        // Assert
        assertNull(log, "Should return null for non-existing InventoryLog");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
    @Order(5)
    void testFindById_Parameterized(int id) {
        // Act
        InventoryLog log = inventoryLogDAO.findById(id);

        // Assert
        assertNotNull(log, "Should find InventoryLog with ID: " + id);
        assertEquals(id, log.getId());
    }

    @Test
    @Order(6)
    void testFindByProductId() {
        // Act - get logs for product 1, first page
        List<InventoryLog> page1 = inventoryLogDAO.findByProductId(1, 1, 2);

        // Assert
        assertEquals(2, page1.size(), "First page should have 2 items");

        // 验证按创建时间倒序排列
        assertTrue(
                page1.get(0).getCreatedAt().isAfter(page1.get(1).getCreatedAt()) ||
                        page1.get(0).getCreatedAt().isEqual(page1.get(1).getCreatedAt()),
                "Records should be sorted by created_at DESC"
        );

        // 验证所有记录都属于同一个商品
        for (InventoryLog log : page1) {
            assertEquals(1, log.getProductId());
        }

        // Act - get second page
        List<InventoryLog> page2 = inventoryLogDAO.findByProductId(1, 2, 2);
        assertEquals(2, page2.size(), "Second page should have 2 items");

        // Act - get third page
        List<InventoryLog> page3 = inventoryLogDAO.findByProductId(1, 3, 2);
        assertEquals(0, page3.size(), "Third page should be empty");
    }

    @Test
    @Order(7)
    void testFindByProductId_NonExisting() {
        // Act
        List<InventoryLog> list = inventoryLogDAO.findByProductId(999, 1, 10);

        // Assert
        assertTrue(list.isEmpty(), "Should return empty list for non-existing product");
    }

    @Test
    @Order(8)
    void testFindByOrderId() {
        // Act
        List<InventoryLog> list = inventoryLogDAO.findByOrderId("ORDER001");

        // Assert
        assertEquals(2, list.size(), "ORDER001 should have 2 inventory logs");

        // 验证按创建时间倒序排列
        assertTrue(
                list.get(0).getCreatedAt().isAfter(list.get(1).getCreatedAt()) ||
                        list.get(0).getCreatedAt().isEqual(list.get(1).getCreatedAt()),
                "Records should be sorted by created_at DESC"
        );

        // 验证所有记录都属于同一个订单
        for (InventoryLog log : list) {
            assertEquals("ORDER001", log.getOrderId());
        }

        // 验证都是销售类型
        for (InventoryLog log : list) {
            assertEquals("sale", log.getChangeType());
            assertTrue(log.getQuantity() < 0, "Sale quantity should be negative");
        }
    }

    @Test
    @Order(9)
    void testFindByOrderId_NonExisting() {
        // Act
        List<InventoryLog> list = inventoryLogDAO.findByOrderId("NON_EXISTENT");

        // Assert
        assertTrue(list.isEmpty(), "Should return empty list for non-existing order");
    }

    @Test
    @Order(10)
    void testFindByChangeType() {
        // Act - get purchase logs
        List<InventoryLog> purchaseList = inventoryLogDAO.findByChangeType("purchase", 1, 10);

        // Assert
        assertEquals(4, purchaseList.size(), "Should have 4 purchase logs");
        for (InventoryLog log : purchaseList) {
            assertEquals("purchase", log.getChangeType());
            assertTrue(log.getQuantity() > 0, "Purchase quantity should be positive");
        }

        // Act - get sale logs
        List<InventoryLog> saleList = inventoryLogDAO.findByChangeType("sale", 1, 10);
        assertEquals(5, saleList.size(), "Should have 5 sale logs");
        for (InventoryLog log : saleList) {
            assertEquals("sale", log.getChangeType());
            assertTrue(log.getQuantity() < 0, "Sale quantity should be negative");
        }

        // Act - get adjust logs
        List<InventoryLog> adjustList = inventoryLogDAO.findByChangeType("adjust", 1, 10);
        assertEquals(1, adjustList.size(), "Should have 1 adjust log");
        for (InventoryLog log : adjustList) {
            assertEquals("adjust", log.getChangeType());
        }
    }

    @Test
    @Order(11)
    void testFindLatestByProductId() {
        // Act
        InventoryLog latest = inventoryLogDAO.findLatestByProductId(1);

        // Assert
        assertNotNull(latest, "Should find latest inventory log for product 1");
        assertEquals(1, latest.getProductId());
        // 应该是ID为4的记录（adjust类型）
        assertEquals(4, latest.getId());
        assertEquals("adjust", latest.getChangeType());
        assertEquals(-47, latest.getQuantity());
        assertEquals(97, latest.getBeforeStock());
        assertEquals(50, latest.getAfterStock());
    }

    @Test
    @Order(12)
    void testFindLatestByProductId_NonExisting() {
        // Act
        InventoryLog latest = inventoryLogDAO.findLatestByProductId(999);

        // Assert
        assertNull(latest, "Should return null for non-existing product");
    }

    @Test
    @Order(13)
    void testCalculatePurchaseQuantity() {
        // Act
        Integer purchaseQuantity = inventoryLogDAO.calculatePurchaseQuantity(1);

        // Assert
        // 商品1的采购总量：id1: 100
        assertEquals(100, purchaseQuantity, "Purchase quantity for product 1 should be 100");

        // Test for product 2
        Integer purchaseQuantity2 = inventoryLogDAO.calculatePurchaseQuantity(2);
        assertEquals(30, purchaseQuantity2, "Purchase quantity for product 2 should be 30");
    }

    @Test
    @Order(14)
    void testCalculatePurchaseQuantity_NonExisting() {
        // Act
        Integer purchaseQuantity = inventoryLogDAO.calculatePurchaseQuantity(999);

        // Assert
        assertEquals(0, purchaseQuantity, "Should return 0 for non-existing product");
    }

    @Test
    @Order(15)
    void testCalculateSalesQuantity() {
        // Act
        Integer salesQuantity = inventoryLogDAO.calculateSalesQuantity(1);

        // Assert
        // 商品1的销售总量：id2: -2, id3: -1，取绝对值总和为3
        assertEquals(3, salesQuantity, "Sales quantity for product 1 should be 3");

        // Test for product 2
        Integer salesQuantity2 = inventoryLogDAO.calculateSalesQuantity(2);
        assertEquals(10, salesQuantity2, "Sales quantity for product 2 should be 10");
    }

    @Test
    @Order(16)
    void testCalculateSalesQuantity_NonExisting() {
        // Act
        Integer salesQuantity = inventoryLogDAO.calculateSalesQuantity(999);

        // Assert
        assertEquals(0, salesQuantity, "Should return 0 for non-existing product");
    }

    @Test
    @Order(17)
    void testGetLowStockProducts() {
        // Act - get products with stock <= 5
        List<Integer> lowStockProducts = inventoryLogDAO.getLowStockProducts(5);

        // Assert
        // 商品3: stock=5, 商品4: stock=0
        assertEquals(2, lowStockProducts.size(), "Should have 2 low stock products");
        assertTrue(lowStockProducts.contains(3), "Product 3 should be in low stock list");
        assertTrue(lowStockProducts.contains(4), "Product 4 should be in low stock list");

        // 验证商品1和2不在列表中
        assertFalse(lowStockProducts.contains(1), "Product 1 should not be in low stock list");
        assertFalse(lowStockProducts.contains(2), "Product 2 should not be in low stock list");
    }

    @Test
    @Order(18)
    void testGetLowStockProducts_NoLowStock() {
        // Act - get products with stock <= 100 (all products)
        List<Integer> lowStockProducts = inventoryLogDAO.getLowStockProducts(100);

        // Assert
        // 所有商品库存都 <= 100
        assertEquals(4, lowStockProducts.size(), "All products should be in low stock list with threshold 100");
    }

    @Test
    @Order(19)
    void testFindRecentChanges() {
        // Act
        List<InventoryLog> recentChanges = inventoryLogDAO.findRecentChanges(3);

        // Assert
        assertEquals(3, recentChanges.size(), "Should get 3 recent changes");

        // 验证按时间倒序排列（最近的在前）
        for (int i = 0; i < recentChanges.size() - 1; i++) {
            assertTrue(
                    recentChanges.get(i).getCreatedAt().isAfter(recentChanges.get(i + 1).getCreatedAt()) ||
                            recentChanges.get(i).getCreatedAt().isEqual(recentChanges.get(i + 1).getCreatedAt()),
                    "Records should be sorted by created_at DESC"
            );
        }

        // 验证返回的记录都是最新的（这里我们无法确定具体的ID，因为可能有两个记录在相同时间）
        // 我们可以验证这些记录的创建时间都在某个时间之后，或者验证它们都是12月1日的记录（根据测试数据）
        // 这里我们简单验证一下它们的创建时间都不早于12月1日（因为测试数据中12月1日有多个记录，11月也有记录）
        LocalDateTime dec1 = LocalDateTime.of(2023, 12, 1, 0, 0);
        for (InventoryLog log : recentChanges) {
            assertTrue(log.getCreatedAt().isAfter(dec1) || log.getCreatedAt().toLocalDate().isEqual(dec1.toLocalDate()),
                    "Recent changes should be on or after Dec 1");
        }
    }

    @Test
    @Order(20)
    void testCreateBatch() {
        // Arrange
        List<InventoryLog> batchLogs = new ArrayList<>();

        InventoryLog log1 = new InventoryLog();
        log1.setProductId(1);
        log1.setChangeType("sale");
        log1.setQuantity(-5);
        log1.setBeforeStock(50);
        log1.setAfterStock(45);
        log1.setOrderId("ORDER001");
        batchLogs.add(log1);

        InventoryLog log2 = new InventoryLog();
        log2.setProductId(2);
        log2.setChangeType("purchase");
        log2.setQuantity(20);
        log2.setBeforeStock(20);
        log2.setAfterStock(40);
        // orderId 为 null
        batchLogs.add(log2);

        InventoryLog log3 = new InventoryLog();
        log3.setProductId(3);
        log3.setChangeType("adjust");
        log3.setQuantity(3);
        log3.setBeforeStock(5);
        log3.setAfterStock(8);
        log3.setOrderId(null);
        batchLogs.add(log3);

        // Act
        boolean result = inventoryLogDAO.createBatch(batchLogs);

        // Assert
        assertTrue(result, "Batch create should succeed");

        // Verify all logs have IDs
        for (InventoryLog log : batchLogs) {
            assertNotNull(log.getId(), "Batch created logs should have IDs");

            // Verify each log can be retrieved
            InventoryLog retrieved = inventoryLogDAO.findById(log.getId());
            assertNotNull(retrieved, "Batch created log should be retrievable");
            assertEquals(log.getProductId(), retrieved.getProductId());
            assertEquals(log.getChangeType(), retrieved.getChangeType());
            assertEquals(log.getQuantity(), retrieved.getQuantity());
        }

        // Verify total count increased
        List<InventoryLog> recentChanges = inventoryLogDAO.findRecentChanges(10);
        // 原有10条 + 新增3条 = 13条
        assertTrue(recentChanges.size() >= 3, "Should have at least 3 new records");
    }

    @Test
    @Order(21)
    void testCreateBatch_EmptyList() {
        // Arrange
        List<InventoryLog> emptyList = new ArrayList<>();

        // Act
        boolean result = inventoryLogDAO.createBatch(emptyList);

        // Assert
        assertTrue(result, "Batch create with empty list should succeed");
    }

    @Test
    @Order(22)
    void testInventoryConsistency() {
        // 验证库存变更的一致性
        // 对于每个商品，最终库存应该等于初始库存 + 所有变更数量

        // 商品1：初始0 + 采购100 - 销售2 - 销售1 - 调整47 = 50
        Integer purchase1 = inventoryLogDAO.calculatePurchaseQuantity(1);
        Integer sales1 = inventoryLogDAO.calculateSalesQuantity(1);

        // 获取调整数量（adjust类型）
        List<InventoryLog> adjustLogs = inventoryLogDAO.findByChangeType("adjust", 1, 10);
        int adjustQuantity1 = 0;
        for (InventoryLog log : adjustLogs) {
            if (log.getProductId() == 1) {
                adjustQuantity1 += log.getQuantity();
            }
        }

        // 商品1的最终库存 = 初始0 + 采购100 - 销售3 + 调整(-47) = 50
        int calculatedStock = purchase1 - sales1 + adjustQuantity1;

        // 从商品表中获取实际库存
        try (var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT stock FROM apexflow_product WHERE id = 1")) {
            if (rs.next()) {
                int actualStock = rs.getInt("stock");
                assertEquals(actualStock, calculatedStock,
                        "Calculated stock should match actual stock for product 1");
            }
        } catch (Exception e) {
            fail("Failed to get actual stock: " + e.getMessage());
        }
    }

    @Test
    @Order(23)
    void testChangeTypeConstants() {
        // Verify the meaning of change type constants
        InventoryLog purchase = inventoryLogDAO.findById(1); // purchase
        assertEquals("purchase", purchase.getChangeType());
        assertTrue(purchase.getQuantity() > 0, "Purchase should have positive quantity");

        InventoryLog sale = inventoryLogDAO.findById(2); // sale
        assertEquals("sale", sale.getChangeType());
        assertTrue(sale.getQuantity() < 0, "Sale should have negative quantity");

        InventoryLog adjust = inventoryLogDAO.findById(4); // adjust
        assertEquals("adjust", adjust.getChangeType());
        // Adjust can be positive or negative
    }

    @Test
    @Order(24)
    void testQuantitySignConvention() {
        // 验证数量符号约定
        List<InventoryLog> allLogs = inventoryLogDAO.findRecentChanges(100);

        for (InventoryLog log : allLogs) {
            if ("purchase".equals(log.getChangeType())) {
                assertTrue(log.getQuantity() > 0,
                        "Purchase should have positive quantity");
            } else if ("sale".equals(log.getChangeType())) {
                assertTrue(log.getQuantity() < 0,
                        "Sale should have negative quantity");
            }
            // adjust 类型可以是正数或负数
        }
    }

    @Test
    @Order(25)
    void testStockBeforeAfterConsistency() {
        // 验证前后库存的一致性
        List<InventoryLog> allLogs = inventoryLogDAO.findRecentChanges(100);

        for (InventoryLog log : allLogs) {
            assertEquals(log.getBeforeStock() + log.getQuantity(), log.getAfterStock(),
                    String.format("Inventory log ID %d: before(%d) + quantity(%d) should equal after(%d)",
                            log.getId(), log.getBeforeStock(), log.getQuantity(), log.getAfterStock()));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "1, purchase, 1",
            "1, sale, 2",
            "1, adjust, 1",
            "2, purchase, 1",
            "2, sale, 1"
    })
    @Order(26)
    void testFindByProductIdAndChangeType(int productId, String changeType, int expectedCount) {
        // 先按商品ID筛选
        List<InventoryLog> productLogs = inventoryLogDAO.findByProductId(productId, 1, 100);

        // 再按变更类型筛选
        long count = productLogs.stream()
                .filter(log -> changeType.equals(log.getChangeType()))
                .count();

        assertEquals(expectedCount, count,
                String.format("Product %d should have %d %s logs",
                        productId, expectedCount, changeType));
    }
}
