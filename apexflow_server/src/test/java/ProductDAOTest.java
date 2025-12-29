import com.apex.core.dao.ProductDAO;
import com.apex.core.dao.IProductDAO;
import com.apex.core.model.Product;
import com.apex.util.ConnectionPool;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ProductDAO 单元测试类
 * 使用 H2 内存数据库进行测试
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductDAOTest {

    private IProductDAO productDAO;
    private Connection conn;

    @BeforeAll
    void setUpAll() throws Exception {
        // 确保使用 H2 内存数据库
        System.setProperty("apexflow.test.h2", "true");

        // 获取连接并初始化数据库
        conn = ConnectionPool.getConnection();
        H2DatabaseInitializer.initialize(conn);

        productDAO = new ProductDAO();
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
        // 注意：不要在这里关闭 ConnectionPool，避免影响其他测试类
        // ConnectionPool.shutdown();
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

            // 插入商品数据
            stmt.execute("""
                INSERT INTO apexflow_product (id, name, category, price, stock, status, image, created_at) VALUES
                (1, 'iPhone 14 Pro', '手机', 7999.00, 50, 1, 'iphone14pro.jpg', '2023-12-01 10:00:00'),
                (2, 'MacBook Pro 16英寸', '电脑', 18999.00, 20, 1, 'macbookpro.jpg', '2023-12-01 11:00:00'),
                (3, '华为Mate 50', '手机', 4999.00, 30, 1, 'mate50.jpg', '2023-12-01 12:00:00'),
                (4, '小米13', '手机', 3999.00, 0, 0, 'xiaomi13.jpg', '2023-12-01 13:00:00'),  -- 下架商品
                (5, '戴尔XPS 13', '电脑', 8999.00, 15, 1, NULL, '2023-12-01 14:00:00'),
                (6, 'iPad Air', '平板', 4999.00, 25, 1, 'ipadair.jpg', '2023-12-01 15:00:00'),
                (7, '三星Galaxy S23', '手机', 5999.00, 10, 1, 'galaxys23.jpg', '2023-12-01 16:00:00'),
                (8, '联想ThinkPad X1', '电脑', 12999.00, 8, 1, 'thinkpad.jpg', '2023-12-01 17:00:00')
            """);

            // 插入订单（用于测试外键约束）
            stmt.execute("""
                INSERT INTO apexflow_order (id, user_id, total_amount, status, payment_method, created_at, paid_at) VALUES
                ('ORDER001', 1001, 7999.00, 4, 'alipay', '2023-12-01 10:00:00', '2023-12-01 10:05:00')
            """);

            // 插入订单项（引用商品）
            stmt.execute("""
                INSERT INTO apexflow_order_item (order_id, product_id, product_name, quantity, price, subtotal) VALUES
                ('ORDER001', 1, 'iPhone 14 Pro', 1, 7999.00, 7999.00)
            """);
        }
    }

    @Test
    @Order(1)
    void testCreateProduct_Success() {
        // Arrange
        Product newProduct = new Product();
        newProduct.setName("测试商品");
        newProduct.setCategory("测试分类");
        newProduct.setPrice(new BigDecimal("99.99"));
        newProduct.setStock(100);
        newProduct.setStatus(1);
        newProduct.setImage("test.jpg");
        newProduct.setCreatedAt(LocalDateTime.now());

        // Act
        boolean result = productDAO.create(newProduct);

        // Assert
        assertTrue(result, "Product creation should return true on success");
        assertNotNull(newProduct.getId(), "Product ID should be auto-generated after creation");

        // Verify the product was actually created
        Product retrieved = productDAO.findById(newProduct.getId());
        assertNotNull(retrieved, "Created product should be retrievable");
        assertEquals(newProduct.getName(), retrieved.getName());
        assertEquals(newProduct.getCategory(), retrieved.getCategory());
        assertEquals(0, new BigDecimal("99.99").compareTo(retrieved.getPrice()));
        assertEquals(newProduct.getStock(), retrieved.getStock());
        assertEquals(1, retrieved.getStatus()); // 默认状态应该是1
        assertEquals("test.jpg", retrieved.getImage());
    }

    @Test
    @Order(2)
    void testCreateProduct_WithNullFields() {
        // Arrange
        Product newProduct = new Product();
        newProduct.setName("无图商品");
        newProduct.setCategory("电子产品");
        newProduct.setPrice(new BigDecimal("199.99"));
        // stock 不设置，应该默认为0
        // status 不设置，应该默认为1
        // image 为 null
        // createdAt 为 null，应该自动设置为当前时间

        // Act
        boolean result = productDAO.create(newProduct);

        // Assert
        assertTrue(result, "Product creation with null fields should succeed");

        // Verify
        Product retrieved = productDAO.findById(newProduct.getId());
        assertNotNull(retrieved);
        assertEquals(0, retrieved.getStock()); // 默认库存
        assertEquals(1, retrieved.getStatus()); // 默认状态
        assertNull(retrieved.getImage()); // 图片为 null
        assertNotNull(retrieved.getCreatedAt()); // 自动生成创建时间
    }

    @Test
    @Order(3)
    void testFindById_Existing() {
        // Act
        Product product = productDAO.findById(1);

        // Assert
        assertNotNull(product, "Should find existing Product");
        assertEquals(1, product.getId());
        assertEquals("iPhone 14 Pro", product.getName());
        assertEquals("手机", product.getCategory());
        assertEquals(0, new BigDecimal("7999.00").compareTo(product.getPrice()));
        assertEquals(50, product.getStock());
        assertEquals(1, product.getStatus()); // 上架状态
        assertEquals("iphone14pro.jpg", product.getImage());
    }

    @Test
    @Order(4)
    void testFindById_NonExisting() {
        // Act
        Product product = productDAO.findById(9999);

        // Assert
        assertNull(product, "Should return null for non-existing Product");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8})
    @Order(5)
    void testFindById_Parameterized(int id) {
        // Act
        Product product = productDAO.findById(id);

        // Assert
        assertNotNull(product, "Should find Product with ID: " + id);
        assertEquals(id, product.getId());
    }

    @Test
    @Order(6)
    void testUpdateProduct_Success() {
        // Arrange
        Product product = productDAO.findById(1);
        assertNotNull(product);

        // Modify product
        product.setName("iPhone 14 Pro 更新版");
        product.setPrice(new BigDecimal("8499.00"));
        product.setStock(60);
        product.setStatus(0); // 下架
        product.setImage("iphone14pro_new.jpg");

        // Act
        boolean result = productDAO.update(product);

        // Assert
        assertTrue(result, "Update should succeed");

        // Verify update
        Product updated = productDAO.findById(1);
        assertNotNull(updated);
        assertEquals("iPhone 14 Pro 更新版", updated.getName());
        assertEquals(0, new BigDecimal("8499.00").compareTo(updated.getPrice()));
        assertEquals(60, updated.getStock());
        assertEquals(0, updated.getStatus()); // 下架状态
        assertEquals("iphone14pro_new.jpg", updated.getImage());
    }

    @Test
    @Order(7)
    void testDeleteProduct_ExpectException() {
        // Arrange
        Product product = productDAO.findById(1);
        assertNotNull(product, "Product should exist before deletion test");

        // Act & Assert
        UnsupportedOperationException exception = assertThrows(
                UnsupportedOperationException.class,
                () -> productDAO.delete(1),
                "Expected delete() to throw UnsupportedOperationException"
        );

        // Verify exception message contains expected text
        String expectedMessage = "Direct deletion of product";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage),
                "Exception message should contain: " + expectedMessage + ", but was: " + actualMessage);

        // Verify product still exists
        Product stillExists = productDAO.findById(1);
        assertNotNull(stillExists, "Product should still exist after delete() throws exception");
    }

    @Test
    @Order(8)
    void testFindAll_Pagination() {
        // Act - get first page (3 items per page)
        List<Product> page1 = productDAO.findAll(1, 3);

        // Assert
        assertEquals(3, page1.size(), "First page should have 3 items");

        // 验证按创建时间倒序排列（最新的在前）
        assertTrue(
                page1.get(0).getCreatedAt().isAfter(page1.get(1).getCreatedAt()) ||
                        page1.get(0).getCreatedAt().isEqual(page1.get(1).getCreatedAt()),
                "Records should be sorted by created_at DESC"
        );

        // Act - get second page
        List<Product> page2 = productDAO.findAll(2, 3);

        // Assert
        assertEquals(3, page2.size(), "Second page should have 3 items");

        // Act - get third page
        List<Product> page3 = productDAO.findAll(3, 3);

        // Assert
        assertEquals(2, page3.size(), "Third page should have 2 items");

        // Verify no overlap between pages
        int firstPageId1 = page1.get(0).getId();
        int secondPageId1 = page2.get(0).getId();
        int thirdPageId1 = page3.get(0).getId();
        assertNotEquals(firstPageId1, secondPageId1, "Page 1 and 2 should not have overlapping products");
        assertNotEquals(secondPageId1, thirdPageId1, "Page 2 and 3 should not have overlapping products");
    }

    @Test
    @Order(9)
    void testFindAll_InvalidPage() {
        // 测试无效页码（0或负数）应该被处理为第一页
        List<Product> result1 = productDAO.findAll(0, 10);
        assertNotNull(result1, "Should handle page 0 and return first page");
        assertFalse(result1.isEmpty(), "First page should not be empty");

        List<Product> result2 = productDAO.findAll(-1, 10);
        assertNotNull(result2, "Should handle negative page and return first page");
        assertFalse(result2.isEmpty(), "First page should not be empty");
    }

    @Test
    @Order(10)
    void testFindByCategory() {
        // Act - get products in category "手机"
        List<Product> phones = productDAO.findByCategory("手机", 1, 10);

        // Assert
        // 商品1,3,4,7是手机，但商品4状态为0（下架），所有手机包括下架的
        assertEquals(4, phones.size(), "Should have 4 products in category '手机'");
        for (Product phone : phones) {
            assertEquals("手机", phone.getCategory());
        }

        // Act - get products in category "电脑"
        List<Product> computers = productDAO.findByCategory("电脑", 1, 10);
        assertEquals(3, computers.size(), "Should have 3 products in category '电脑'");
        for (Product computer : computers) {
            assertEquals("电脑", computer.getCategory());
        }

        // Act - get products in non-existing category
        List<Product> nonExisting = productDAO.findByCategory("不存在", 1, 10);
        assertTrue(nonExisting.isEmpty(), "Should return empty list for non-existing category");
    }

    @Test
    @Order(11)
    void testFindByStatus() {
        // Act - get products with status 1 (上架)
        List<Product> activeProducts = productDAO.findByStatus(1, 1, 10);

        // Assert
        // 商品1,2,3,5,6,7,8状态为1，商品4状态为0
        assertEquals(7, activeProducts.size(), "Should have 7 active products (status=1)");
        for (Product product : activeProducts) {
            assertEquals(1, product.getStatus(), "All products should have status 1");
        }

        // Act - get products with status 0 (下架)
        List<Product> inactiveProducts = productDAO.findByStatus(0, 1, 10);
        assertEquals(1, inactiveProducts.size(), "Should have 1 inactive product (status=0)");
        for (Product product : inactiveProducts) {
            assertEquals(0, product.getStatus(), "All products should have status 0");
        }
    }

    @Test
    @Order(12)
    void testSearchByName() {
        // Act - search for "iPhone"
        List<Product> iphoneResults = productDAO.searchByName("iPhone", 1, 10);

        // Assert
        assertEquals(1, iphoneResults.size(), "Should find 1 product with 'iPhone' in name");
        assertEquals("iPhone 14 Pro", iphoneResults.get(0).getName());

        // Act - search for "Pro" (should match multiple)
        List<Product> proResults = productDAO.searchByName("Pro", 1, 10);
        assertTrue(proResults.size() >= 2, "Should find at least 2 products with 'Pro' in name");

        // Act - search for "华为"
        List<Product> huaweiResults = productDAO.searchByName("华为", 1, 10);
        assertEquals(1, huaweiResults.size(), "Should find 1 product with '华为' in name");
        assertEquals("华为Mate 50", huaweiResults.get(0).getName());

        // Act - search for non-existing keyword
        List<Product> noResults = productDAO.searchByName("不存在", 1, 10);
        assertTrue(noResults.isEmpty(), "Should return empty list for non-existing keyword");
    }

    @Test
    @Order(13)
    void testUpdateStock() {
        // Arrange
        Product original = productDAO.findById(1);
        assertNotNull(original);
        int originalStock = original.getStock();

        // Act - update stock to new value
        boolean result = productDAO.updateStock(1, 100);

        // Assert
        assertTrue(result, "UpdateStock should succeed");

        // Verify
        Product updated = productDAO.findById(1);
        assertNotNull(updated);
        assertEquals(100, updated.getStock(), "Stock should be updated to 100");
        assertNotEquals(originalStock, updated.getStock(), "Stock should be different from original");

        // Act - update stock for non-existing product
        boolean nonExistingResult = productDAO.updateStock(9999, 100);
        assertFalse(nonExistingResult, "UpdateStock should fail for non-existing product");
    }

    @Test
    @Order(14)
    void testIncreaseStock() {
        // Arrange
        Product original = productDAO.findById(1);
        assertNotNull(original);
        int originalStock = original.getStock();

        // Act - increase stock by 20
        boolean result = productDAO.increaseStock(1, 20);

        // Assert
        assertTrue(result, "IncreaseStock should succeed");

        // Verify
        Product updated = productDAO.findById(1);
        assertNotNull(updated);
        assertEquals(originalStock + 20, updated.getStock(), "Stock should be increased by 20");

        // Act - increase stock by negative value (should still work as increase)
        boolean negativeResult = productDAO.increaseStock(1, -10);
        assertTrue(negativeResult, "IncreaseStock with negative value should still succeed");
        Product afterNegative = productDAO.findById(1);
        assertEquals(originalStock + 20 - 10, afterNegative.getStock(),
                "Stock should be decreased when negative value used with increaseStock");
    }

    @Test
    @Order(15)
    void testDecreaseStock_Success() {
        // Arrange
        Product original = productDAO.findById(1);
        assertNotNull(original);
        int originalStock = original.getStock(); // Should be 50

        // Act - decrease stock by 10 (sufficient stock)
        boolean result = productDAO.decreaseStock(1, 10);

        // Assert
        assertTrue(result, "DecreaseStock should succeed when sufficient stock");

        // Verify
        Product updated = productDAO.findById(1);
        assertNotNull(updated);
        assertEquals(originalStock - 10, updated.getStock(), "Stock should be decreased by 10");
    }

    @Test
    @Order(16)
    void testDecreaseStock_InsufficientStock() {
        // Arrange - product 1 has stock 50
        Product original = productDAO.findById(1);
        assertNotNull(original);

        // Act - try to decrease stock by 100 (more than available)
        boolean result = productDAO.decreaseStock(1, 100);

        // Assert
        assertFalse(result, "DecreaseStock should fail when insufficient stock");

        // Verify stock unchanged
        Product unchanged = productDAO.findById(1);
        assertEquals(original.getStock(), unchanged.getStock(),
                "Stock should remain unchanged when decrease fails");
    }

    @Test
    @Order(17)
    void testUpdateStatus() {
        // Arrange
        Product original = productDAO.findById(1);
        assertNotNull(original);
        assertEquals(1, original.getStatus()); // 初始状态为上架

        // Act - update status to 0 (下架)
        boolean result = productDAO.updateStatus(1, 0);

        // Assert
        assertTrue(result, "UpdateStatus should succeed");

        // Verify
        Product updated = productDAO.findById(1);
        assertNotNull(updated);
        assertEquals(0, updated.getStatus(), "Status should be updated to 0");

        // Act - update status back to 1 (上架)
        boolean result2 = productDAO.updateStatus(1, 1);
        assertTrue(result2, "UpdateStatus back to 1 should succeed");
        Product reverted = productDAO.findById(1);
        assertEquals(1, reverted.getStatus(), "Status should be updated back to 1");
    }

    @Test
    @Order(18)
    void testCount() {
        // Act
        long count = productDAO.count();

        // Assert
        assertEquals(8, count, "Should count all 8 test products");
    }

    @Test
    @Order(19)
    void testCountByCategory() {
        // Act
        long phoneCount = productDAO.countByCategory("手机");
        long computerCount = productDAO.countByCategory("电脑");
        long tabletCount = productDAO.countByCategory("平板");
        long nonExistingCount = productDAO.countByCategory("不存在");

        // Assert
        assertEquals(4, phoneCount, "Should have 4 products in '手机' category");
        assertEquals(3, computerCount, "Should have 3 products in '电脑' category");
        assertEquals(1, tabletCount, "Should have 1 product in '平板' category");
        assertEquals(0, nonExistingCount, "Should have 0 products in non-existing category");
    }

    @Test
    @Order(20)
    void testProductLifecycle() {
        // Test a complete product lifecycle (excluding delete)

        // 1. Create new product
        Product newProduct = new Product();
        newProduct.setName("生命周期测试商品");
        newProduct.setCategory("测试");
        newProduct.setPrice(new BigDecimal("199.99"));
        newProduct.setStock(50);
        newProduct.setStatus(1);

        assertTrue(productDAO.create(newProduct));
        Integer newId = newProduct.getId();
        assertNotNull(newId);

        // 2. Verify creation
        Product created = productDAO.findById(newId);
        assertNotNull(created);
        assertEquals("生命周期测试商品", created.getName());
        assertEquals(1, created.getStatus());
        assertEquals(50, created.getStock());

        // 3. Update product
        created.setName("更新后的商品名");
        created.setPrice(new BigDecimal("249.99"));
        assertTrue(productDAO.update(created));

        Product updated = productDAO.findById(newId);
        assertEquals("更新后的商品名", updated.getName());
        assertEquals(0, new BigDecimal("249.99").compareTo(updated.getPrice()));

        // 4. Update stock
        assertTrue(productDAO.increaseStock(newId, 30));
        Product afterIncrease = productDAO.findById(newId);
        assertEquals(80, afterIncrease.getStock());

        assertTrue(productDAO.decreaseStock(newId, 20));
        Product afterDecrease = productDAO.findById(newId);
        assertEquals(60, afterDecrease.getStock());

        // 5. Update status
        assertTrue(productDAO.updateStatus(newId, 0));
        Product afterStatusChange = productDAO.findById(newId);
        assertEquals(0, afterStatusChange.getStatus());

        // 6. Test delete throws exception (as expected)
        assertThrows(UnsupportedOperationException.class,
                () -> productDAO.delete(newId),
                "Delete should throw UnsupportedOperationException");
    }

    @Test
    @Order(21)
    void testFindAll_EmptyDatabase() {}

    @Test
    @Order(22)
    void testStatusConstants() {
        // Verify the meaning of status constants
        Product status1 = productDAO.findById(1); // status=1
        assertEquals(1, status1.getStatus());

        Product status0 = productDAO.findById(4); // status=0
        assertEquals(0, status0.getStatus());

        // 1-上架, 0-下架
    }

    @ParameterizedTest
    @CsvSource({
            "手机, 1, 3",   // 手机分类，状态1，有3个（商品1,3,7）
            "手机, 0, 1",   // 手机分类，状态0，有1个（商品4）
            "电脑, 1, 3",   // 电脑分类，状态1，有3个（商品2,5,8）
            "电脑, 0, 0",   // 电脑分类，状态0，有0个
            "平板, 1, 1"    // 平板分类，状态1，有1个（商品6）
    })
    @Order(23)
    void testFindByCategoryAndStatus_Combined(String category, int status, int expectedCount) {
        // 先按分类筛选
        List<Product> categoryList = productDAO.findByCategory(category, 1, 10);

        // 再筛选状态
        long count = categoryList.stream()
                .filter(product -> product.getStatus() == status)
                .count();

        assertEquals(expectedCount, count,
                String.format("Should have %d products in category '%s' with status %d",
                        expectedCount, category, status));
    }

    @Test
    @Order(24)
    void testSearchWithPagination() {
        // Create additional test products for pagination test
        LocalDateTime baseTime = LocalDateTime.of(2023, 12, 1, 18, 0, 0);
        try (var stmt = conn.createStatement()) {
            // Add more products with "Test" in name, with sequential timestamps
            for (int i = 9; i <= 15; i++) {
                LocalDateTime createTime = baseTime.plusMinutes(i - 9);
                stmt.execute(String.format(
                        "INSERT INTO apexflow_product (id, name, category, price, stock, status, created_at) VALUES " +
                                "(%d, 'Test Product %d', '测试', 99.99, 10, 1, '%s')",
                        i, i, createTime.toString().replace('T', ' ')));
            }
        } catch (Exception e) {
            fail("Failed to insert test data: " + e.getMessage());
        }

        // Test pagination with search
        // Act - first page
        List<Product> page1 = productDAO.searchByName("Test", 1, 5);
        assertEquals(5, page1.size(), "First page should have 5 items");

        // Act - second page
        List<Product> page2 = productDAO.searchByName("Test", 2, 5);
        assertEquals(2, page2.size(), "Second page should have 2 items (7 total - 5 on first page)");

        // Collect all IDs from both pages
        List<Integer> allIds = new ArrayList<>();
        for (Product p : page1) allIds.add(p.getId());
        for (Product p : page2) allIds.add(p.getId());

        // Verify no duplicate IDs (more robust check)
        long distinctCount = allIds.stream().distinct().count();
        assertEquals(7, distinctCount, "Should have 7 distinct products across both pages");

        // Alternative: check that no ID appears in both pages
        Set<Integer> page1Ids = page1.stream().map(Product::getId).collect(java.util.stream.Collectors.toSet());
        Set<Integer> page2Ids = page2.stream().map(Product::getId).collect(java.util.stream.Collectors.toSet());

        for (Integer id : page1Ids) {
            assertFalse(page2Ids.contains(id),
                    String.format("Product ID %d should not appear in both pages", id));
        }
    }

    @Test
    @Order(25)
    void testStockBoundaryConditions() {
        // Test edge cases for stock operations

        // 1. Test decreaseStock with exact available stock
        Product product = productDAO.findById(1);
        int initialStock = product.getStock(); // Should be 50

        // Decrease by exact amount
        assertTrue(productDAO.decreaseStock(1, initialStock));
        Product afterDecrease = productDAO.findById(1);
        assertEquals(0, afterDecrease.getStock(), "Stock should be 0 after decreasing by exact amount");

        // 2. Test decreaseStock with 0 stock (should fail)
        assertFalse(productDAO.decreaseStock(1, 1),
                "DecreaseStock should fail when stock is 0");

        // 3. Test increaseStock with 0
        assertTrue(productDAO.increaseStock(1, 0),
                "IncreaseStock with 0 should succeed (no-op)");
        Product afterZeroIncrease = productDAO.findById(1);
        assertEquals(0, afterZeroIncrease.getStock(),
                "Stock should remain 0 after increasing by 0");

        // 4. Test updateStock with negative value (should work but might not make business sense)
        assertTrue(productDAO.updateStock(1, -10));
        Product afterNegative = productDAO.findById(1);
        assertEquals(-10, afterNegative.getStock(),
                "Stock can be set to negative (though not recommended)");
    }

    @Test
    @Order(26)
    void testCreateProductWithNegativeStock() {
        // Test creating product with negative stock (should be allowed by database)
        Product newProduct = new Product();
        newProduct.setName("负库存商品");
        newProduct.setCategory("测试");
        newProduct.setPrice(new BigDecimal("99.99"));
        newProduct.setStock(-10); // Negative stock
        newProduct.setStatus(1);

        boolean result = productDAO.create(newProduct);
        assertTrue(result, "Should allow creating product with negative stock");

        Product retrieved = productDAO.findById(newProduct.getId());
        assertEquals(-10, retrieved.getStock(), "Stock should be -10");
    }
}
