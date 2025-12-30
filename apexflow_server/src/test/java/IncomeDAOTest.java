import com.apex.core.dao.IIncomeDAO;
import com.apex.core.dao.IncomeDAO;
import com.apex.core.model.Income;
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
 * IncomeDAO 单元测试类
 * 使用 H2 内存数据库进行测试
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IncomeDAOTest {

    private IIncomeDAO incomeDAO;
    private Connection conn;

    @BeforeAll
    void setUpAll() throws Exception {
        // 确保使用 H2 内存数据库
        System.setProperty("apexflow.test.h2", "true");

        // 获取连接并初始化数据库
        conn = ConnectionPool.getConnection();
        H2DatabaseInitializer.initialize(conn);

        incomeDAO = new IncomeDAO();
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

            // 插入财务记录
            stmt.execute("""
                        INSERT INTO apexflow_income (id, order_id, type, amount, payment_method, status, transaction_time, remark) VALUES
                        (1, 'ORDER001', 'income', 7999.00, 'alipay', 2, '2023-12-01 10:05:00', '订单支付'),
                        (2, 'ORDER001', 'refund', -500.00, 'alipay', 2, '2023-12-02 10:00:00', '部分退款'),
                        (3, 'ORDER002', 'income', 18999.00, 'wxpay', 2, '2023-12-01 11:05:00', '订单支付'),
                        (4, 'ORDER003', 'income', 299.99, 'alipay', 1, '2023-12-01 12:05:00', '订单支付（待入账）'),
                        (5, 'ORDER001', 'refund', -1000.00, 'alipay', 1, '2023-12-02 11:00:00', '待退款')
                    """);
        }
    }

    @Test
    @Order(1)
    void testCreateIncome_Success() {
        // Arrange
        Income newIncome = new Income();
        newIncome.setOrderId("ORDER001");
        newIncome.setType("income");
        newIncome.setAmount(new BigDecimal("999.99"));
        newIncome.setPaymentMethod("alipay");
        newIncome.setStatus(1); // 待入账
        newIncome.setTransactionTime(LocalDateTime.now());
        newIncome.setRemark("测试收入记录");

        // Act
        boolean result = incomeDAO.create(newIncome);

        // Assert
        assertTrue(result, "Income creation should return true on success");
        assertNotNull(newIncome.getId(), "Income ID should be auto-generated after creation");

        // Verify the income was actually created
        Income retrieved = incomeDAO.findById(newIncome.getId());
        assertNotNull(retrieved, "Created income should be retrievable");
        assertEquals(newIncome.getOrderId(), retrieved.getOrderId());
        assertEquals(newIncome.getType(), retrieved.getType());
        assertEquals(0, new BigDecimal("999.99").compareTo(retrieved.getAmount()));
        assertEquals(1, retrieved.getStatus()); // 默认状态应该是1
        assertEquals("alipay", retrieved.getPaymentMethod());
    }

    @Test
    @Order(2)
    void testCreateIncome_WithNullFields() {
        // Arrange
        Income newIncome = new Income();
        newIncome.setOrderId("ORDER002");
        newIncome.setType("refund");
        newIncome.setAmount(new BigDecimal("-200.00")); // 退款为负数
        // paymentMethod 为 null
        // status 不设置，应该默认为1
        // transactionTime 为 null，应该自动设置为当前时间
        // remark 为 null

        // Act
        boolean result = incomeDAO.create(newIncome);

        // Assert
        assertTrue(result, "Income creation with null fields should succeed");

        // Verify
        Income retrieved = incomeDAO.findById(newIncome.getId());
        assertNotNull(retrieved);
        assertEquals(1, retrieved.getStatus()); // 默认状态
        assertNotNull(retrieved.getTransactionTime()); // 自动生成交易时间
        assertNull(retrieved.getPaymentMethod()); // 支付方式为 null
        assertNull(retrieved.getRemark()); // 备注为 null
    }

    @Test
    @Order(3)
    void testFindById_Existing() {
        // Act
        Income income = incomeDAO.findById(1);

        // Assert
        assertNotNull(income, "Should find existing Income");
        assertEquals(1, income.getId());
        assertEquals("ORDER001", income.getOrderId());
        assertEquals("income", income.getType());
        assertEquals(0, new BigDecimal("7999.00").compareTo(income.getAmount()));
        assertEquals("alipay", income.getPaymentMethod());
        assertEquals(2, income.getStatus()); // 已入账
    }

    @Test
    @Order(4)
    void testFindById_NonExisting() {
        // Act
        Income income = incomeDAO.findById(9999);

        // Assert
        assertNull(income, "Should return null for non-existing Income");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    @Order(5)
    void testFindById_Parameterized(int id) {
        // Act
        Income income = incomeDAO.findById(id);

        // Assert
        assertNotNull(income, "Should find Income with ID: " + id);
        assertEquals(id, income.getId());
    }

    @Test
    @Order(6)
    void testFindByOrderId() {
        // Act
        List<Income> list = incomeDAO.findByOrderId("ORDER001");

        // Assert
        assertEquals(3, list.size(), "ORDER001 should have 3 income records");

        // 验证按交易时间倒序排列
        assertTrue(
                list.get(0).getTransactionTime().isAfter(list.get(1).getTransactionTime()) ||
                        list.get(0).getTransactionTime().isEqual(list.get(1).getTransactionTime()),
                "Records should be sorted by transaction_time DESC"
        );

        // 验证所有记录都属于同一个订单
        for (Income income : list) {
            assertEquals("ORDER001", income.getOrderId());
        }

        // 验证类型和金额
        boolean hasIncome = false;
        boolean hasRefund = false;
        for (Income income : list) {
            if ("income".equals(income.getType())) hasIncome = true;
            if ("refund".equals(income.getType())) hasRefund = true;
        }
        assertTrue(hasIncome, "Should have income type");
        assertTrue(hasRefund, "Should have refund type");
    }

    @Test
    @Order(7)
    void testFindByOrderId_NonExisting() {
        // Act
        List<Income> list = incomeDAO.findByOrderId("NON_EXISTENT");

        // Assert
        assertTrue(list.isEmpty(), "Should return empty list for non-existing order");
    }

    @Test
    @Order(8)
    void testUpdateIncome_Success() {
        // Arrange
        Income income = incomeDAO.findById(1);
        assertNotNull(income);

        // Modify income
        income.setStatus(2); // 已入账 -> 已入账（不变）
        income.setAmount(new BigDecimal("8000.00"));
        income.setRemark("更新后的备注");

        // Act
        boolean result = incomeDAO.update(income);

        // Assert
        assertTrue(result, "Update should succeed");

        // Verify update
        Income updated = incomeDAO.findById(1);
        assertNotNull(updated);
        assertEquals(2, updated.getStatus());
        assertEquals(0, new BigDecimal("8000.00").compareTo(updated.getAmount()));
        assertEquals("更新后的备注", updated.getRemark());
    }

    @Test
    @Order(9)
    void testDeleteIncome_Success() {
        // Arrange - ensure income exists
        Income income = incomeDAO.findById(1);
        assertNotNull(income, "Income should exist before deletion");

        // Act
        boolean result = incomeDAO.delete(1);

        // Assert
        assertTrue(result, "Delete should return true on success");

        // Verify deletion
        Income deleted = incomeDAO.findById(1);
        assertNull(deleted, "Income should be deleted");
    }

    @Test
    @Order(10)
    void testDeleteIncome_NonExisting() {
        // Act
        boolean result = incomeDAO.delete(9999);

        // Assert
        assertFalse(result, "Delete should return false for non-existing Income");
    }

    @Test
    @Order(11)
    void testFindAll_Pagination() {
        // Act - get first page (2 items per page)
        List<Income> page1 = incomeDAO.findAll(1, 2);

        // Assert
        assertEquals(2, page1.size(), "First page should have 2 items");

        // 验证按交易时间倒序排列
        assertTrue(
                page1.get(0).getTransactionTime().isAfter(page1.get(1).getTransactionTime()) ||
                        page1.get(0).getTransactionTime().isEqual(page1.get(1).getTransactionTime()),
                "Records should be sorted by transaction_time DESC"
        );

        // Act - get second page
        List<Income> page2 = incomeDAO.findAll(2, 2);

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
        // 测试无效页码（0或负数）应该被处理为第一页
        List<Income> result1 = incomeDAO.findAll(0, 10);
        assertNotNull(result1, "Should handle page 0 and return first page");

        List<Income> result2 = incomeDAO.findAll(-1, 10);
        assertNotNull(result2, "Should handle negative page and return first page");
    }

    @Test
    @Order(13)
    void testFindByType() {
        // Act - get income type records
        List<Income> incomeList = incomeDAO.findByType("income", 1, 10);

        // Assert
        assertEquals(3, incomeList.size(), "Should have 3 income records");
        for (Income income : incomeList) {
            assertEquals("income", income.getType());
        }

        // Act - get refund type records
        List<Income> refundList = incomeDAO.findByType("refund", 1, 10);
        assertEquals(2, refundList.size(), "Should have 2 refund records");
        for (Income refund : refundList) {
            assertEquals("refund", refund.getType());
            assertTrue(refund.getAmount().compareTo(BigDecimal.ZERO) < 0, "Refund amount should be negative");
        }
    }

    @Test
    @Order(14)
    void testFindByStatus() {
        // Act - get status 1 (待入账) records
        List<Income> pendingList = incomeDAO.findByStatus(1, 1, 10);

        // Assert
        assertEquals(2, pendingList.size(), "Should have 2 pending income records");
        for (Income income : pendingList) {
            assertEquals(1, income.getStatus());
        }

        // Act - get status 2 (已入账) records
        List<Income> completedList = incomeDAO.findByStatus(2, 1, 10);
        assertEquals(3, completedList.size(), "Should have 3 completed income records");
        for (Income income : completedList) {
            assertEquals(2, income.getStatus());
        }
    }

    @Test
    @Order(15)
    void testCalculateTotalIncome() {
        // Act
        BigDecimal totalIncome = incomeDAO.calculateTotalIncome();

        // Assert
        // 计算期望值：只有类型为income且状态为2的记录才计入总收入
        // id1: 7999.00 (income, status 2)
        // id3: 18999.00 (income, status 2)
        // id4: 299.99 (income, status 1) - 不计入
        BigDecimal expected = new BigDecimal("7999.00").add(new BigDecimal("18999.00"));
        assertEquals(0, expected.compareTo(totalIncome),
                "Total income should be sum of completed income records");
    }

    @Test
    @Order(16)
    void testCalculateTotalRefund() {
        // Act
        BigDecimal totalRefund = incomeDAO.calculateTotalRefund();

        // Assert
        // 计算期望值：只有类型为refund且状态为2的记录才计入总退款（取绝对值）
        // id2: -500.00 (refund, status 2) -> 500.00
        // id5: -1000.00 (refund, status 1) - 不计入
        BigDecimal expected = new BigDecimal("500.00");
        assertEquals(0, expected.compareTo(totalRefund),
                "Total refund should be sum of completed refund records (absolute value)");
    }

    @Test
    @Order(17)
    void testUpdateStatus_Success() {
        // Arrange
        Income income = incomeDAO.findById(4);
        assertNotNull(income);
        assertEquals(1, income.getStatus()); // 初始状态为待入账

        // Act
        boolean result = incomeDAO.updateStatus(4, 2); // 更新为已入账

        // Assert
        assertTrue(result, "UpdateStatus should succeed");

        // Verify
        Income updated = incomeDAO.findById(4);
        assertNotNull(updated);
        assertEquals(2, updated.getStatus(), "Status should be updated to 2");
    }

    @Test
    @Order(18)
    void testUpdateStatus_NonExisting() {
        // Act
        boolean result = incomeDAO.updateStatus(9999, 2);

        // Assert
        assertFalse(result, "UpdateStatus should return false for non-existing Income");
    }

    @Test
    @Order(19)
    void testIncomeLifecycle() {
        // Test a complete income lifecycle

        // 1. Create new income
        Income income = new Income();
        income.setOrderId("ORDER002");
        income.setType("income");
        income.setAmount(new BigDecimal("1999.99"));
        income.setPaymentMethod("wxpay");
        income.setStatus(1); // 待入账
        income.setRemark("测试生命周期");

        assertTrue(incomeDAO.create(income));
        Integer newId = income.getId();
        assertNotNull(newId);

        // 2. Verify initial status is 1 (待入账)
        Income created = incomeDAO.findById(newId);
        assertNotNull(created);
        assertEquals(1, created.getStatus());

        // 3. Update status to 2 (已入账)
        assertTrue(incomeDAO.updateStatus(newId, 2));

        Income approved = incomeDAO.findById(newId);
        assertEquals(2, approved.getStatus());

        // 4. Delete the income
        assertTrue(incomeDAO.delete(newId));
        assertNull(incomeDAO.findById(newId));
    }

    @Test
    @Order(20)
    void testAmountSignConvention() {
        // 验证金额符号约定：正数为收入，负数为退款
        List<Income> allRecords = incomeDAO.findAll(1, 10);

        for (Income record : allRecords) {
            if ("income".equals(record.getType())) {
                assertTrue(record.getAmount().compareTo(BigDecimal.ZERO) >= 0,
                        "Income type should have non-negative amount");
            } else if ("refund".equals(record.getType())) {
                assertTrue(record.getAmount().compareTo(BigDecimal.ZERO) <= 0,
                        "Refund type should have non-positive amount");
            }
        }
    }

    @Test
    @Order(21)
    void testFindAll_EmptyResult() throws SQLException {
        // Arrange - clear income data only
        try (var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM apexflow_income");
        }

        // Act
        List<Income> list = incomeDAO.findAll(1, 10);

        // Assert
        assertTrue(list.isEmpty(), "Should return empty list when no income records exist");
    }

    @Test
    @Order(22)
    void testTypeConstants() {
        // Verify the meaning of type constants
        Income income1 = incomeDAO.findById(1); // type=income
        assertEquals("income", income1.getType());

        Income income2 = incomeDAO.findById(2); // type=refund
        assertEquals("refund", income2.getType());
    }

    @Test
    @Order(23)
    void testStatusConstants() {
        // Verify the meaning of status constants
        Income status1 = incomeDAO.findById(4); // status=1
        assertEquals(1, status1.getStatus());

        Income status2 = incomeDAO.findById(1); // status=2
        assertEquals(2, status2.getStatus());

        // 1-待入账, 2-已入账
    }

    @ParameterizedTest
    @CsvSource({
            "income, 1, 1",  // 只有id4是income且状态为1
            "income, 2, 2",  // id1和id3是income且状态为2
            "refund, 1, 1",  // id5是refund且状态为1
            "refund, 2, 1"   // id2是refund且状态为2
    })
    @Order(24)
    void testFindByTypeAndStatus_Combined(String type, int status, int expectedCount) {
        // 先按类型筛选
        List<Income> typeList = incomeDAO.findByType(type, 1, 10);

        // 再筛选状态
        long count = typeList.stream()
                .filter(income -> income.getStatus() == status)
                .count();

        assertEquals(expectedCount, count,
                String.format("Should have %d records with type=%s and status=%d",
                        expectedCount, type, status));
    }
}
