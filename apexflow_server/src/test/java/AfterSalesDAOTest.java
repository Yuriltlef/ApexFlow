import com.apex.core.dao.AfterSalesDAO;
import com.apex.core.dao.IAfterSalesDAO;
import com.apex.core.model.AfterSales;
import com.apex.util.ConnectionPool;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AfterSalesDAO 单元测试类
 * 使用 H2 内存数据库进行测试
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AfterSalesDAOTest {

    private IAfterSalesDAO afterSalesDAO;
    private Connection conn;

    @BeforeAll
    void setUpAll() throws Exception {
        // 确保使用 H2 内存数据库
        System.setProperty("apexflow.test.h2", "true");

        // 获取连接并初始化数据库
        conn = ConnectionPool.getConnection();
        H2DatabaseInitializer.initialize(conn);

        afterSalesDAO = new AfterSalesDAO();
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
                        ('ORDER001', 1001, 7999.00, 4, 'alipay', '2023-12-01 10:00:00', '2023-12-01 10:05:00'),
                        ('ORDER002', 1002, 18999.00, 4, 'wxpay', '2023-12-01 11:00:00', '2023-12-01 11:05:00'),
                        ('ORDER003', 1001, 299.99, 4, 'alipay', '2023-12-01 12:00:00', '2023-12-01 12:05:00')
                    """);

            // 插入订单项
            stmt.execute("""
                        INSERT INTO apexflow_order_item (order_id, product_id, product_name, quantity, price, subtotal) VALUES
                        ('ORDER001', 1, 'iPhone 14 Pro', 1, 7999.00, 7999.00),
                        ('ORDER002', 2, 'MacBook Pro 16英寸', 1, 18999.00, 18999.00)
                    """);

            // 插入售后记录
            stmt.execute("""
                        INSERT INTO apexflow_after_sales (id, order_id, type, reason, status, refund_amount, apply_time, process_time, process_remark) VALUES
                        (1, 'ORDER001', 1, '质量问题', 1, 7999.00, '2023-12-02 10:00:00', NULL, NULL),
                        (2, 'ORDER001', 2, '颜色不喜欢', 2, NULL, '2023-12-02 11:00:00', '2023-12-02 12:00:00', '审核通过，安排换货'),
                        (3, 'ORDER002', 1, '功能故障', 3, NULL, '2023-12-02 12:00:00', '2023-12-02 13:00:00', '审核拒绝，已过退货期'),
                        (4, 'ORDER003', 3, '维修需求', 4, 100.00, '2023-12-02 13:00:00', '2023-12-02 14:00:00', '维修完成')
                    """);
        }
    }

    @Test
    @Order(1)
    void testCreateAfterSales_Success() {
        // Arrange
        AfterSales newAfterSales = new AfterSales();
        newAfterSales.setOrderId("ORDER001");
        newAfterSales.setType(1); // 退货
        newAfterSales.setReason("商品有瑕疵");
        newAfterSales.setStatus(1); // 申请中
        newAfterSales.setRefundAmount(new BigDecimal("199.99"));
        newAfterSales.setApplyTime(LocalDateTime.now());

        // Act
        boolean result = afterSalesDAO.create(newAfterSales);

        // Assert
        assertTrue(result, "AfterSales creation should return true on success");
        assertNotNull(newAfterSales.getId(), "AfterSales ID should be auto-generated after creation");

        // Verify the afterSales was actually created
        AfterSales retrieved = afterSalesDAO.findById(newAfterSales.getId());
        assertNotNull(retrieved, "Created afterSales should be retrievable");
        assertEquals(newAfterSales.getOrderId(), retrieved.getOrderId());
        assertEquals(newAfterSales.getType(), retrieved.getType());
        assertEquals(0, new BigDecimal("199.99").compareTo(retrieved.getRefundAmount()));
        assertEquals(1, retrieved.getStatus()); // 默认状态应该是1
    }

    @Test
    @Order(2)
    void testCreateAfterSales_WithNullFields() {
        // Arrange
        AfterSales newAfterSales = new AfterSales();
        newAfterSales.setOrderId("ORDER002");
        newAfterSales.setType(2); // 换货
        newAfterSales.setReason("尺寸不合适");
        // status 不设置，应该默认为1
        // refundAmount 为 null
        // applyTime 为 null，应该自动设置为当前时间
        // processTime 为 null
        // processRemark 为 null

        // Act
        boolean result = afterSalesDAO.create(newAfterSales);

        // Assert
        assertTrue(result, "AfterSales creation with null fields should succeed");

        // Verify
        AfterSales retrieved = afterSalesDAO.findById(newAfterSales.getId());
        assertNotNull(retrieved);
        assertEquals(1, retrieved.getStatus()); // 默认状态
        assertNotNull(retrieved.getApplyTime()); // 自动生成申请时间
        assertNull(retrieved.getRefundAmount()); // 退款金额为 null
        assertNull(retrieved.getProcessTime()); // 处理时间为 null
    }

    @Test
    @Order(3)
    void testFindById_Existing() {
        // Act
        AfterSales afterSales = afterSalesDAO.findById(1);

        // Assert
        assertNotNull(afterSales, "Should find existing AfterSales");
        assertEquals(1, afterSales.getId());
        assertEquals("ORDER001", afterSales.getOrderId());
        assertEquals(1, afterSales.getType());
        assertEquals("质量问题", afterSales.getReason());
        assertEquals(1, afterSales.getStatus());
        assertEquals(0, new BigDecimal("7999.00").compareTo(afterSales.getRefundAmount()));
    }

    @Test
    @Order(4)
    void testFindById_NonExisting() {
        // Act
        AfterSales afterSales = afterSalesDAO.findById(9999);

        // Assert
        assertNull(afterSales, "Should return null for non-existing AfterSales");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    @Order(5)
    void testFindById_Parameterized(int id) {
        // Act
        AfterSales afterSales = afterSalesDAO.findById(id);

        // Assert
        assertNotNull(afterSales, "Should find AfterSales with ID: " + id);
        assertEquals(id, afterSales.getId());
    }

    @Test
    @Order(6)
    void testFindByOrderId() {
        // Act
        List<AfterSales> list = afterSalesDAO.findByOrderId("ORDER001");

        // Assert
        assertEquals(2, list.size(), "ORDER001 should have 2 after-sales records");

        // 验证按申请时间倒序排列
        assertTrue(
                list.get(0).getApplyTime().isAfter(list.get(1).getApplyTime()) ||
                        list.get(0).getApplyTime().isEqual(list.get(1).getApplyTime()),
                "Records should be sorted by apply_time DESC"
        );

        // 验证所有记录都属于同一个订单
        for (AfterSales as : list) {
            assertEquals("ORDER001", as.getOrderId());
        }
    }

    @Test
    @Order(7)
    void testFindByOrderId_NonExisting() {
        // Act
        List<AfterSales> list = afterSalesDAO.findByOrderId("NON_EXISTENT");

        // Assert
        assertTrue(list.isEmpty(), "Should return empty list for non-existing order");
    }

    @Test
    @Order(8)
    void testUpdateAfterSales_Success() {
        // Arrange
        AfterSales afterSales = afterSalesDAO.findById(1);
        assertNotNull(afterSales);

        // Modify afterSales
        afterSales.setStatus(2); // 审核通过
        afterSales.setRefundAmount(new BigDecimal("7500.00"));
        afterSales.setProcessTime(LocalDateTime.now());
        afterSales.setProcessRemark("部分退款，扣除包装费");

        // Act
        boolean result = afterSalesDAO.update(afterSales);

        // Assert
        assertTrue(result, "Update should succeed");

        // Verify update
        AfterSales updated = afterSalesDAO.findById(1);
        assertNotNull(updated);
        assertEquals(2, updated.getStatus());
        assertEquals(0, new BigDecimal("7500.00").compareTo(updated.getRefundAmount()));
        assertNotNull(updated.getProcessTime());
        assertEquals("部分退款，扣除包装费", updated.getProcessRemark());
    }

    @Test
    @Order(9)
    void testDeleteAfterSales_Success() {
        // Arrange - ensure afterSales exists
        AfterSales afterSales = afterSalesDAO.findById(1);
        assertNotNull(afterSales, "AfterSales should exist before deletion");

        // Act
        boolean result = afterSalesDAO.delete(1);

        // Assert
        assertTrue(result, "Delete should return true on success");

        // Verify deletion
        AfterSales deleted = afterSalesDAO.findById(1);
        assertNull(deleted, "AfterSales should be deleted");
    }

    @Test
    @Order(10)
    void testDeleteAfterSales_NonExisting() {
        // Act
        boolean result = afterSalesDAO.delete(9999);

        // Assert
        assertFalse(result, "Delete should return false for non-existing AfterSales");
    }

    @Test
    @Order(11)
    void testFindAll_Pagination() {
        // Act - get first page (2 items per page)
        List<AfterSales> page1 = afterSalesDAO.findAll(1, 2);

        // Assert
        assertEquals(2, page1.size(), "First page should have 2 items");

        // 验证按申请时间倒序排列
        assertTrue(
                page1.get(0).getApplyTime().isAfter(page1.get(1).getApplyTime()) ||
                        page1.get(0).getApplyTime().isEqual(page1.get(1).getApplyTime()),
                "Records should be sorted by apply_time DESC"
        );

        // Act - get second page
        List<AfterSales> page2 = afterSalesDAO.findAll(2, 2);

        // Assert
        assertEquals(2, page2.size(), "Second page should have 2 items");

        // Verify no overlap
        int firstPageId1 = page1.get(0).getId();
        int secondPageId1 = page2.get(0).getId();
        assertNotEquals(firstPageId1, secondPageId1, "Pages should not have overlapping records");
    }

    @Test
    @Order(12)
    void testFindAll_InvalidPage() {
        // Act - page 0 should be treated as page 1
        List<AfterSales> result = afterSalesDAO.findAll(0, 10);

        // Assert - 实现可能会处理无效页码，这里我们至少验证不抛异常
        assertNotNull(result);
    }

    @Test
    @Order(13)
    void testFindByStatus() {
        // Act - get after-sales with status 1 (申请中)
        List<AfterSales> list = afterSalesDAO.findByStatus(1, 1, 10);

        // Assert
        assertEquals(1, list.size(), "Should have 1 after-sales with status 1");
        assertEquals(1, list.get(0).getId());
        assertEquals(1, list.get(0).getStatus());

        // Act - get after-sales with status 4 (已完成)
        List<AfterSales> completedList = afterSalesDAO.findByStatus(4, 1, 10);
        assertEquals(1, completedList.size(), "Should have 1 after-sales with status 4");
        assertEquals(4, completedList.get(0).getId());
    }

    @ParameterizedTest
    @CsvSource({
            "1, 1",  // 申请中
            "2, 1",  // 审核通过
            "3, 1",  // 审核拒绝
            "4, 1"   // 已完成
    })
    @Order(14)
    void testFindByStatus_Parameterized(int status, int expectedCount) {
        // Act
        List<AfterSales> list = afterSalesDAO.findByStatus(status, 1, 10);

        // Assert
        assertEquals(expectedCount, list.size(),
                String.format("Should have %d after-sales with status %d", expectedCount, status));

        // Verify all records have the correct status
        for (AfterSales as : list) {
            assertEquals(status, as.getStatus());
        }
    }

    @Test
    @Order(15)
    void testCount() {
        // Act
        long count = afterSalesDAO.count();

        // Assert
        assertEquals(4, count, "Should count all 4 test after-sales records");
    }

    @Test
    @Order(16)
    void testCount_EmptyDatabase() {
        // Arrange - clear all data
        H2DatabaseInitializer.clearAllTables(conn);

        // Act
        long count = afterSalesDAO.count();

        // Assert
        assertEquals(0, count, "Should return 0 for empty database");
    }

    @Test
    @Order(17)
    void testUpdateStatus_Success() {
        // Arrange
        String processRemark = "审核通过，安排退款";

        // Act
        boolean result = afterSalesDAO.updateStatus(1, 2, processRemark);

        // Assert
        assertTrue(result, "UpdateStatus should succeed");

        // Verify
        AfterSales updated = afterSalesDAO.findById(1);
        assertNotNull(updated);
        assertEquals(2, updated.getStatus());
        assertEquals(processRemark, updated.getProcessRemark());
        assertNotNull(updated.getProcessTime(), "Process time should be set by NOW()");
    }

    @Test
    @Order(18)
    void testUpdateStatus_NonExisting() {
        // Act
        boolean result = afterSalesDAO.updateStatus(9999, 2, "test");

        // Assert
        assertFalse(result, "UpdateStatus should return false for non-existing AfterSales");
    }

    @Test
    @Order(19)
    void testAfterSalesLifecycle() {
        // Test a complete after-sales lifecycle

        // 1. Create new after-sales
        AfterSales afterSales = new AfterSales();
        afterSales.setOrderId("ORDER002");
        afterSales.setType(1); // 退货
        afterSales.setReason("测试生命周期");
        afterSales.setRefundAmount(new BigDecimal("500.00"));

        assertTrue(afterSalesDAO.create(afterSales));
        Integer newId = afterSales.getId();
        assertNotNull(newId);

        // 2. Verify initial status is 1 (申请中)
        AfterSales created = afterSalesDAO.findById(newId);
        assertNotNull(created);
        assertEquals(1, created.getStatus());
        assertNull(created.getProcessTime());

        // 3. Update status to 2 (审核通过)
        assertTrue(afterSalesDAO.updateStatus(newId, 2, "审核通过"));

        AfterSales approved = afterSalesDAO.findById(newId);
        assertEquals(2, approved.getStatus());
        assertNotNull(approved.getProcessTime());
        assertEquals("审核通过", approved.getProcessRemark());

        // 4. Update status to 4 (已完成)
        assertTrue(afterSalesDAO.updateStatus(newId, 4, "退款完成"));

        AfterSales completed = afterSalesDAO.findById(newId);
        assertEquals(4, completed.getStatus());
        assertEquals("退款完成", completed.getProcessRemark());

        // 5. Delete the after-sales
        assertTrue(afterSalesDAO.delete(newId));
        assertNull(afterSalesDAO.findById(newId));
    }

    @Test
    @Order(20)
    void testFindAll_EmptyResult() throws SQLException {
        // Arrange - clear after-sales data only
        try (var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM apexflow_after_sales");
        }

        // Act
        List<AfterSales> list = afterSalesDAO.findAll(1, 10);

        // Assert
        assertTrue(list.isEmpty(), "Should return empty list when no after-sales records exist");
    }

    @Test
    @Order(21)
    void testTypeConstants() {
        // Verify the meaning of type constants
        AfterSales type1 = afterSalesDAO.findById(1); // type=1
        assertEquals(1, type1.getType());

        AfterSales type2 = afterSalesDAO.findById(2); // type=2
        assertEquals(2, type2.getType());

        AfterSales type3 = afterSalesDAO.findById(4); // type=3
        assertEquals(3, type3.getType());

        // These are based on the test data inserted in setUp()
        // 1-退货, 2-换货, 3-维修
    }

    @Test
    @Order(22)
    void testStatusConstants() {
        // Verify the meaning of status constants
        AfterSales status1 = afterSalesDAO.findById(1); // status=1
        assertEquals(1, status1.getStatus());

        AfterSales status2 = afterSalesDAO.findById(2); // status=2
        assertEquals(2, status2.getStatus());

        AfterSales status3 = afterSalesDAO.findById(3); // status=3
        assertEquals(3, status3.getStatus());

        AfterSales status4 = afterSalesDAO.findById(4); // status=4
        assertEquals(4, status4.getStatus());

        // 1-申请中, 2-审核通过, 3-审核拒绝, 4-已完成
    }
}
