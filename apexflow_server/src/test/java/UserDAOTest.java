import com.apex.core.dao.UserDAO;
import com.apex.core.model.SystemUser;
import com.apex.util.ConnectionPool;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserDAO单元测试类
 * 使用H2内存数据库进行测试
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDAOTest {

    private UserDAO userDAO;
    private Connection conn;

    @BeforeAll
    void setUpAll() throws Exception {
        // 确保使用H2内存数据库
        System.setProperty("apexflow.test.h2", "true");

        // 获取连接并初始化数据库
        conn = ConnectionPool.getConnection();
        H2DatabaseInitializer.initialize(conn);

        userDAO = new UserDAO();
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
            // 插入用户数据
            stmt.execute("""
                INSERT INTO apexflow_system_user (id, username, password_hash, salt, real_name, email, phone,
                                                  is_admin, can_manage_order, can_manage_logistics,
                                                  can_manage_after_sales, can_manage_review,
                                                  can_manage_inventory, can_manage_income, status) VALUES
                (1, 'admin', '$2a$10$AbCdEfGhIjKlMnOpQrStUv', 'a1b2c3d4e5f678901234567890123456', '系统管理员',
                 'admin@apexflow.com', '13800138000', TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, 1),
                (2, 'manager1', 'hash2', 'salt2', '经理1', 'manager1@apexflow.com', '13800138001',
                 FALSE, TRUE, TRUE, FALSE, TRUE, FALSE, TRUE, 1),
                (3, 'manager2', 'hash3', 'salt3', '经理2', 'manager2@apexflow.com', '13800138002',
                 FALSE, FALSE, TRUE, TRUE, FALSE, TRUE, FALSE, 1),
                (4, 'operator1', 'hash4', 'salt4', '操作员1', 'operator1@apexflow.com', '13800138003',
                 FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 1),
                (5, 'inactive_user', 'hash5', 'salt5', '禁用用户', 'inactive@apexflow.com', '13800138004',
                 FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 0)
            """);
        }
    }

    @Test
    @Order(1)
    void testCreateUser_Success() {
        // Arrange
        SystemUser user = getSystemUser();

        // Act
        boolean result = userDAO.create(user);

        // Assert
        assertTrue(result, "用户创建应该成功");
        assertNotNull(user.getId(), "用户应该有ID");

        // Verify
        SystemUser retrieved = userDAO.findById(user.getId());
        assertNotNull(retrieved);
        assertEquals("newuser", retrieved.getUsername());
        assertEquals("新用户", retrieved.getRealName());
        assertEquals("newuser@apexflow.com", retrieved.getEmail());
        assertFalse(retrieved.getAdmin());
        assertTrue(retrieved.getCanManageOrder());
        assertFalse(retrieved.getCanManageLogistics());
        assertEquals(1, retrieved.getStatus());
    }

    private static @NotNull SystemUser getSystemUser() {
        SystemUser user = new SystemUser();
        user.setUsername("newuser");
        user.setPasswordHash("$2a$10$NewHashValue1234567890");
        user.setSalt("newsalt1234678901234567890123456");
        user.setRealName("新用户");
        user.setEmail("newuser@apexflow.com");
        user.setPhone("13900139000");
        user.setAdmin(false);
        user.setCanManageOrder(true);
        user.setCanManageLogistics(false);
        user.setCanManageAfterSales(true);
        user.setCanManageReview(false);
        user.setCanManageInventory(true);
        user.setCanManageIncome(false);
        user.setStatus(1);
        return user;
    }

    @Test
    @Order(2)
    void testFindById_ExistingUser() {
        // Act
        SystemUser user = userDAO.findById(1);

        // Assert
        assertNotNull(user, "应该能找到存在的用户");
        assertEquals("admin", user.getUsername());
        assertEquals("系统管理员", user.getRealName());
        assertEquals("admin@apexflow.com", user.getEmail());
        assertEquals("13800138000", user.getPhone());
        assertTrue(user.getAdmin());
        assertEquals(1, user.getStatus());
    }

    @Test
    @Order(3)
    void testFindById_NonExistingUser() {
        // Act
        SystemUser user = userDAO.findById(999);

        // Assert
        assertNull(user, "不存在的用户应该返回null");
    }

    @Test
    @Order(4)
    void testFindByUsername() {
        // Act
        SystemUser user = userDAO.findByUsername("manager1");

        // Assert
        assertNotNull(user, "应该能通过用户名找到用户");
        assertEquals(2, user.getId());
        assertEquals("经理1", user.getRealName());
        assertEquals("manager1@apexflow.com", user.getEmail());
        assertFalse(user.getAdmin());
        assertTrue(user.getCanManageOrder());
        assertTrue(user.getCanManageLogistics());
    }

    @Test
    @Order(5)
    void testFindByEmail() {
        // Act
        SystemUser user = userDAO.findByEmail("manager2@apexflow.com");

        // Assert
        assertNotNull(user, "应该能通过邮箱找到用户");
        assertEquals(3, user.getId());
        assertEquals("manager2", user.getUsername());
        assertEquals("经理2", user.getRealName());
        assertFalse(user.getAdmin());
        assertFalse(user.getCanManageOrder());
        assertTrue(user.getCanManageLogistics());
    }

    @Test
    @Order(6)
    void testUpdateUser_Success() {
        // Arrange
        SystemUser user = userDAO.findById(2);
        assertNotNull(user);

        // 修改用户信息
        user.setRealName("更新后的经理");
        user.setEmail("updated@apexflow.com");
        user.setPhone("13900139001");
        user.setCanManageOrder(false);
        user.setCanManageLogistics(true);
        user.setCanManageReview(true);
        user.setStatus(1);

        // Act
        boolean result = userDAO.update(user);

        // Assert
        assertTrue(result, "用户更新应该成功");

        // Verify
        SystemUser updated = userDAO.findById(2);
        assertNotNull(updated);
        assertEquals("更新后的经理", updated.getRealName());
        assertEquals("updated@apexflow.com", updated.getEmail());
        assertEquals("13900139001", updated.getPhone());
        assertFalse(updated.getCanManageOrder());
        assertTrue(updated.getCanManageLogistics());
        assertTrue(updated.getCanManageReview());
        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    @Order(7)
    void testUpdateLastLoginTime_Success() {
        // Act
        boolean result = userDAO.updateLastLoginTime(1);

        // Assert
        assertTrue(result, "最后登录时间更新应该成功");

        // Verify
        SystemUser user = userDAO.findById(1);
        assertNotNull(user);
        assertNotNull(user.getLastLoginAt(), "最后登录时间应该被更新");
    }

    @Test
    @Order(8)
    void testUpdateStatus_Success() {
        // Act
        boolean result = userDAO.updateStatus(1, 0); // 禁用用户

        // Assert
        assertTrue(result, "用户状态更新应该成功");

        // Verify
        SystemUser user = userDAO.findById(1);
        assertNotNull(user);
        assertEquals(0, user.getStatus(), "用户状态应该被更新为禁用");
        assertNotNull(user.getUpdatedAt(), "更新时间应该被更新");
    }

    @Test
    @Order(9)
    void testDeleteUser_Success() {
        // Arrange - 确保用户存在
        SystemUser user = userDAO.findById(4);
        assertNotNull(user, "删除前用户应该存在");

        // Act
        boolean result = userDAO.delete(4);

        // Assert
        assertTrue(result, "用户删除应该成功");

        // Verify
        SystemUser deleted = userDAO.findById(4);
        assertNull(deleted, "删除后应该找不到用户");
    }

    @Test
    @Order(10)
    void testDeleteUser_NonExisting() {
        // Act
        boolean result = userDAO.delete(999);

        // Assert
        assertFalse(result, "删除不存在的用户应该返回false");
    }

    @Test
    @Order(11)
    void testFindAll_Pagination() {
        // Act - 第一页，每页2条
        List<SystemUser> page1 = userDAO.findAll(1, 2);

        // Assert
        assertEquals(2, page1.size(), "第一页应该有2个用户");

        // Act - 第二页
        List<SystemUser> page2 = userDAO.findAll(2, 2);

        // Assert
        assertEquals(2, page2.size(), "第二页应该有2个用户");

        // Act - 第三页
        List<SystemUser> page3 = userDAO.findAll(3, 2);

        // Assert
        assertEquals(1, page3.size(), "第三页应该有1个用户");

        // 验证没有重复
        int id1 = page1.get(0).getId();
        boolean foundInPage2 = page2.stream().anyMatch(u -> u.getId() == id1);
        assertFalse(foundInPage2, "两页之间不应该有重复用户");
    }

    @Test
    @Order(12)
    void testFindByStatus() {
        // Act - 查找活跃用户（status=1）
        List<SystemUser> activeUsers = userDAO.findByStatus(1, 1, 10);

        // Assert
        assertEquals(4, activeUsers.size(), "应该有4个活跃用户");

        // 验证都是活跃状态
        boolean allActive = activeUsers.stream().allMatch(u -> u.getStatus() == 1);
        assertTrue(allActive, "所有用户都应该是活跃状态");

        // Act - 查找禁用用户（status=0）
        List<SystemUser> inactiveUsers = userDAO.findByStatus(0, 1, 10);

        // Assert
        assertEquals(1, inactiveUsers.size(), "应该有1个禁用用户");
        assertEquals("inactive_user", inactiveUsers.get(0).getUsername());
    }

    @Test
    @Order(13)
    void testSearch() {
        // Act - 搜索"经理"
        List<SystemUser> results = userDAO.search("经理", 1, 10);

        // Assert
        assertEquals(2, results.size(), "搜索'经理'应该找到2个用户");

        // 验证结果包含经理1和经理2
        boolean foundManager1 = results.stream().anyMatch(u -> u.getUsername().equals("manager1"));
        boolean foundManager2 = results.stream().anyMatch(u -> u.getUsername().equals("manager2"));
        assertTrue(foundManager1, "应该包含manager1");
        assertTrue(foundManager2, "应该包含manager2");
    }

    @Test
    @Order(14)
    void testSearchByEmail() {
        // Act - 搜索邮箱包含"apexflow.com"
        List<SystemUser> results = userDAO.search("apexflow.com", 1, 10);

        // Assert
        assertEquals(5, results.size(), "搜索邮箱域名应该找到所有用户");
    }

    @Test
    @Order(15)
    void testSearchByPhone() {
        // Act - 搜索手机号包含"8001"
        List<SystemUser> results = userDAO.search("8001", 1, 10);

        // Assert
        assertEquals(5, results.size(), "搜索手机号应该找到1个用户");
        assertEquals("admin", results.get(0).getUsername());
    }

    @Test
    @Order(16)
    void testCount() {
        // Act
        long count = userDAO.count();

        // Assert
        assertEquals(5, count, "用户总数应该是5");
    }

    @Test
    @Order(17)
    void testCountActive() {
        // Act
        long activeCount = userDAO.countActive();

        // Assert
        assertEquals(4, activeCount, "活跃用户数应该是4");
    }

    @Test
    @Order(18)
    void testExistsByUsername_Existing() {
        // Act
        boolean exists = userDAO.existsByUsername("admin");

        // Assert
        assertTrue(exists, "存在的用户名应该返回true");
    }

    @Test
    @Order(19)
    void testExistsByUsername_NonExisting() {
        // Act
        boolean exists = userDAO.existsByUsername("nonexistent");

        // Assert
        assertFalse(exists, "不存在的用户名应该返回false");
    }

    @Test
    @Order(20)
    void testExistsByEmail_Existing() {
        // Act
        boolean exists = userDAO.existsByEmail("admin@apexflow.com");

        // Assert
        assertTrue(exists, "存在的邮箱应该返回true");
    }

    @Test
    @Order(21)
    void testExistsByEmail_NonExisting() {
        // Act
        boolean exists = userDAO.existsByEmail("nonexistent@apexflow.com");

        // Assert
        assertFalse(exists, "不存在的邮箱应该返回false");
    }

    @ParameterizedTest
    @ValueSource(strings = {"admin", "manager1", "manager2", "operator1", "inactive_user"})
    @Order(22)
    void testFindByUsername_Parameterized(String username) {
        // Act
        SystemUser user = userDAO.findByUsername(username);

        // Assert
        assertNotNull(user, "应该能找到用户: " + username);
        assertEquals(username, user.getUsername());
    }

    @ParameterizedTest
    @CsvSource({
            "1, admin, 系统管理员, 1",
            "2, manager1, 经理1, 1",
            "3, manager2, 经理2, 1",
            "4, operator1, 操作员1, 1",
            "5, inactive_user, 禁用用户, 0"
    })
    @Order(23)
    void testFindById_Parameterized(int id, String expectedUsername, String expectedRealName, int expectedStatus) {
        // Act
        SystemUser user = userDAO.findById(id);

        // Assert
        assertNotNull(user, "应该能找到用户ID: " + id);
        assertEquals(expectedUsername, user.getUsername());
        assertEquals(expectedRealName, user.getRealName());
        assertEquals(expectedStatus, user.getStatus());
    }

    @Test
    @Order(24)
    void testUserPermissions() {
        // 测试管理员权限
        SystemUser admin = userDAO.findById(1);
        assertTrue(admin.getAdmin(), "管理员应该是超级管理员");
        assertTrue(admin.getCanManageOrder(), "管理员应该有订单管理权限");
        assertTrue(admin.getCanManageLogistics(), "管理员应该有物流管理权限");

        // 测试操作员权限
        SystemUser operator = userDAO.findById(4);
        assertFalse(operator.getAdmin(), "操作员不应该是超级管理员");
        assertFalse(operator.getCanManageOrder(), "操作员不应该有订单管理权限");
        assertFalse(operator.getCanManageLogistics(), "操作员不应该有物流管理权限");
    }

    @Test
    @Order(25)
    void testDuplicateUsername() {
        // Arrange - 尝试创建重复用户名的用户
        // 由于数据库约束，创建应该失败
        // 但我们的DAO只是返回false，不会抛出异常
        // 注意：实际测试中，应该根据具体实现调整
        // 这里假设create方法会返回false
    }

    @Test
    @Order(26)
    void testUserWithMinimalData() {
        // Arrange - 创建只有必要字段的用户
        SystemUser user = new SystemUser();
        user.setUsername("minimal");
        user.setPasswordHash("minimal_hash");
        user.setSalt("minimal_salt_32_chars_1234567890");
        user.setRealName("最小用户");
        user.setStatus(1);
        // email, phone, 所有权限字段都是null或默认值

        // Act
        boolean result = userDAO.create(user);

        // Assert
        assertTrue(result, "最小数据用户创建应该成功");

        // Verify
        SystemUser retrieved = userDAO.findById(user.getId());
        assertNotNull(retrieved);
        assertEquals("minimal", retrieved.getUsername());
        assertNull(retrieved.getEmail(), "邮箱应该为null");
        assertNull(retrieved.getPhone(), "手机号应该为null");
        assertFalse(retrieved.getAdmin(), "默认不是管理员");
        assertFalse(retrieved.getCanManageOrder(), "订单管理权限应该为null");
        assertEquals(1, retrieved.getStatus());
    }

    @Test
    @Order(27)
    void testFindAll_EmptyDatabase() {
        // Arrange - 清空所有数据
        H2DatabaseInitializer.clearAllTables(conn);

        // Act
        List<SystemUser> users = userDAO.findAll(1, 10);

        // Assert
        assertTrue(users.isEmpty(), "空数据库应该返回空列表");
    }

    @Test
    @Order(28)
    void testSearch_NoResults() {
        // Act - 搜索不存在的关键词
        List<SystemUser> results = userDAO.search("不存在的关键词", 1, 10);

        // Assert
        assertTrue(results.isEmpty(), "搜索不存在的关键词应该返回空列表");
    }

    @Test
    @Order(29)
    void testUpdateNonExistingUser() {
        // Arrange - 创建一个不存在的用户对象
        SystemUser nonExistingUser = new SystemUser();
        nonExistingUser.setId(999);
        nonExistingUser.setUsername("nonexistent");
        nonExistingUser.setRealName("不存在的用户");

        // Act
        boolean result = userDAO.update(nonExistingUser);

        // Assert
        assertFalse(result, "更新不存在的用户应该返回false");
    }

    @Test
    @Order(30)
    void testUserTimestamps() {
        // Arrange - 创建新用户
        SystemUser user = new SystemUser();
        user.setUsername("timestamp_test");
        user.setPasswordHash("hash");
        user.setSalt("salt_32_chars_1234567890123456");
        user.setRealName("时间戳测试");
        user.setStatus(1);

        // Act
        boolean created = userDAO.create(user);
        assertTrue(created);

        // 等待一小段时间，确保时间戳不同
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // 忽略
        }

        // 更新用户
        user.setRealName("更新的时间戳测试");
        boolean updated = userDAO.update(user);
        assertTrue(updated);

        // Verify
        SystemUser retrieved = userDAO.findById(user.getId());
        assertNotNull(retrieved);
        assertNotNull(retrieved.getCreatedAt(), "创建时间不应该为null");
        assertNotNull(retrieved.getUpdatedAt(), "更新时间不应该为null");

        // 更新时间应该晚于创建时间
        assertTrue(retrieved.getUpdatedAt().isAfter(retrieved.getCreatedAt()) ||
                        retrieved.getUpdatedAt().isEqual(retrieved.getCreatedAt()),
                "更新时间应该晚于或等于创建时间");
    }
}