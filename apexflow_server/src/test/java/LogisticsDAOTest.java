import com.apex.core.dao.LogisticsDAO;
import com.apex.core.model.Logistics;
import com.apex.util.ConnectionPool;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LogisticsDAO单元测试类
 * 使用H2内存数据库进行测试
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LogisticsDAOTest {

    private LogisticsDAO logisticsDAO;
    private Connection conn;

    @BeforeAll
    void setUpAll() throws Exception {
        // 确保使用H2内存数据库
        System.setProperty("apexflow.test.h2", "true");

        // 获取连接并初始化数据库
        conn = ConnectionPool.getConnection();
        H2DatabaseInitializer.initialize(conn);

        logisticsDAO = new LogisticsDAO();
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
            // 插入订单
            stmt.execute("""
                INSERT INTO apexflow_order (id, user_id, total_amount, status, payment_method, created_at, paid_at) VALUES
                ('LOG001', 1001, 7999.00, 1, 'alipay', '2023-12-01 10:00:00', NULL),
                ('LOG002', 1002, 18999.00, 2, 'wxpay', '2023-12-01 11:00:00', '2023-12-01 11:05:00'),
                ('LOG003', 1001, 299.99, 3, 'alipay', '2023-12-01 12:00:00', '2023-12-01 12:05:00')
            """);

            // 插入物流数据
            stmt.execute("""
                INSERT INTO apexflow_logistics (id, order_id, express_company, tracking_number, status, sender_address, receiver_address) VALUES
                (1, 'LOG001', '顺丰速运', 'SF1234567890', 'pending', '北京市朝阳区', '上海市浦东新区'),
                (2, 'LOG002', '圆通快递', 'YT9876543210', 'shipped', '广州市天河区', '深圳市南山区'),
                (3, 'LOG003', '中通快递', 'ZT5556667778', 'delivered', '杭州市西湖区', '南京市鼓楼区')
            """);
        }
    }

    @Test
    @Order(1)
    void testCreateLogistics_Success() {
        // Arrange
        Logistics logistics = new Logistics();
        logistics.setOrderId("LOG_NEW_001");
        logistics.setExpressCompany("京东物流");
        logistics.setTrackingNumber("JD123456789");
        logistics.setStatus("pending");
        logistics.setSenderAddress("北京市海淀区");
        logistics.setReceiverAddress("天津市南开区");
        logistics.setShippedAt(LocalDateTime.now());

        // 先插入订单
        try (var stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO apexflow_order (id, user_id, total_amount, status) VALUES ('LOG_NEW_001', 1001, 599.99, 1)");
        } catch (Exception e) {
            fail("Failed to insert test order");
        }

        // Act
        boolean result = logisticsDAO.create(logistics);

        // Assert
        assertTrue(result, "物流记录创建应该成功");
        assertNotNull(logistics.getId(), "物流记录应该有ID");

        // Verify
        Logistics retrieved = logisticsDAO.findById(logistics.getId());
        assertNotNull(retrieved);
        assertEquals("LOG_NEW_001", retrieved.getOrderId());
        assertEquals("京东物流", retrieved.getExpressCompany());
        assertEquals("pending", retrieved.getStatus());
    }

    @Test
    @Order(2)
    void testFindById_ExistingLogistics() {
        // Act
        Logistics logistics = logisticsDAO.findById(1);

        // Assert
        assertNotNull(logistics, "应该能找到存在的物流记录");
        assertEquals("LOG001", logistics.getOrderId());
        assertEquals("顺丰速运", logistics.getExpressCompany());
        assertEquals("pending", logistics.getStatus());
    }

    @Test
    @Order(3)
    void testFindById_NonExistingLogistics() {
        // Act
        Logistics logistics = logisticsDAO.findById(999);

        // Assert
        assertNull(logistics, "不存在的物流记录应该返回null");
    }

    @Test
    @Order(4)
    void testFindByOrderId() {
        // Act
        Logistics logistics = logisticsDAO.findByOrderId("LOG001");

        // Assert
        assertNotNull(logistics, "应该能通过订单号找到物流记录");
        assertEquals(1, logistics.getId());
        assertEquals("顺丰速运", logistics.getExpressCompany());
    }

    @Test
    @Order(5)
    void testUpdateLogistics_Success() {
        // Arrange
        Logistics logistics = logisticsDAO.findByOrderId("LOG001");
        assertNotNull(logistics);

        // 修改物流信息
        logistics.setExpressCompany("EMS");
        logistics.setTrackingNumber("EM123456789");
        logistics.setStatus("shipped");
        logistics.setShippedAt(LocalDateTime.now());

        // Act
        boolean result = logisticsDAO.update(logistics);

        // Assert
        assertTrue(result, "更新应该成功");

        // Verify
        Logistics updated = logisticsDAO.findById(1);
        assertNotNull(updated);
        assertEquals("EMS", updated.getExpressCompany());
        assertEquals("shipped", updated.getStatus());
        assertNotNull(updated.getShippedAt());
    }

    @Test
    @Order(6)
    void testDeleteLogistics_Success() {
        // Arrange - 确保物流记录存在
        Logistics logistics = logisticsDAO.findById(1);
        assertNotNull(logistics, "删除前物流记录应该存在");

        // Act
        boolean result = logisticsDAO.delete(1);

        // Assert
        assertTrue(result, "删除应该成功");

        // Verify
        Logistics deleted = logisticsDAO.findById(1);
        assertNull(deleted, "删除后应该找不到物流记录");
    }

    @Test
    @Order(7)
    void testUpdateStatus_Success() {
        // Act
        boolean result = logisticsDAO.updateStatus("LOG001", "shipped");

        // Assert
        assertTrue(result, "状态更新应该成功");

        // Verify
        Logistics updated = logisticsDAO.findByOrderId("LOG001");
        assertNotNull(updated);
        assertEquals("shipped", updated.getStatus());
    }

    @ParameterizedTest
    @CsvSource({
            "LOG001, pending",
            "LOG002, shipped",
            "LOG003, delivered"
    })
    @Order(8)
    void testUpdateStatus_Parameterized(String orderId, String expectedStatus) {
        // Act
        Logistics logistics = logisticsDAO.findByOrderId(orderId);

        // Assert
        assertNotNull(logistics);
        assertEquals(expectedStatus, logistics.getStatus());
    }

    @Test
    @Order(9)
    void testUpdateShippingInfo_Success() {
        // Arrange
        String expressCompany = "韵达快递";
        String trackingNumber = "YD999888777";
        String senderAddress = "武汉市江汉区";

        // Act
        boolean result = logisticsDAO.updateShippingInfo("LOG001", expressCompany, trackingNumber, senderAddress);

        // Assert
        assertTrue(result, "发货信息更新应该成功");

        // Verify
        Logistics updated = logisticsDAO.findByOrderId("LOG001");
        assertNotNull(updated);
        assertEquals(expressCompany, updated.getExpressCompany());
        assertEquals(trackingNumber, updated.getTrackingNumber());
        assertEquals(senderAddress, updated.getSenderAddress());
        assertEquals("shipped", updated.getStatus());
        assertNotNull(updated.getShippedAt());
    }

    @Test
    @Order(10)
    void testUpdateDeliveryInfo_Success() {
        // Arrange
        LocalDateTime deliveredAt = LocalDateTime.now();

        // Act
        boolean result = logisticsDAO.updateDeliveryInfo("LOG002", deliveredAt);

        // Assert
        assertTrue(result, "送达信息更新应该成功");

        // Verify
        Logistics updated = logisticsDAO.findByOrderId("LOG002");
        assertNotNull(updated);
        assertEquals("delivered", updated.getStatus());
        assertNotNull(updated.getDeliveredAt());
    }

    @Test
    @Order(11)
    void testFindPendingShipping() {
        // Act
        List<Logistics> pendingList = logisticsDAO.findPendingShipping(1, 10);

        // Assert
        assertEquals(1, pendingList.size(), "应该只有1个待发货订单");
        assertEquals("LOG001", pendingList.get(0).getOrderId());
    }

    @Test
    @Order(12)
    void testFindInTransit() {
        // Act
        List<Logistics> inTransitList = logisticsDAO.findInTransit(1, 10);

        // Assert
        assertEquals(1, inTransitList.size(), "应该只有1个运输中订单");
        assertEquals("LOG002", inTransitList.get(0).getOrderId());
    }

    @Test
    @Order(13)
    void testGetLogisticsStats() {
        // Act
        LogisticsDAO.LogisticsStats stats = logisticsDAO.getLogisticsStats();

        // Assert
        assertNotNull(stats);
        assertEquals(1, stats.getPendingCount(), "待发货数量应为1");
        assertEquals(1, stats.getShippedCount(), "已发货数量应为1");
        assertEquals(1, stats.getDeliveredCount(), "已送达数量应为1");
    }

    @Test
    @Order(14)
    void testNullHandling() {
        // Arrange
        Logistics logistics = new Logistics();
        logistics.setOrderId("LOG_NULL_TEST");
        logistics.setStatus("pending");
        // 其他字段为null

        // 先插入订单
        try (var stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO apexflow_order (id, user_id, total_amount, status) VALUES ('LOG_NULL_TEST', 1001, 199.99, 1)");
        } catch (Exception e) {
            fail("Failed to insert test order");
        }

        // Act
        boolean result = logisticsDAO.create(logistics);

        // Assert
        assertTrue(result, "应该能处理null值");

        // Verify
        Logistics retrieved = logisticsDAO.findByOrderId("LOG_NULL_TEST");
        assertNotNull(retrieved);
        assertNull(retrieved.getExpressCompany());
        assertNull(retrieved.getTrackingNumber());
        assertNull(retrieved.getSenderAddress());
        assertNull(retrieved.getReceiverAddress());
        assertNull(retrieved.getShippedAt());
        assertNull(retrieved.getDeliveredAt());
    }

    @Test
    @Order(15)
    void testFindPendingShipping_Pagination() {
        // 插入更多待发货订单
        try (var stmt = conn.createStatement()) {
            for (int i = 4; i <= 12; i++) {
                String orderId = "LOG_PAGE_" + i;
                stmt.execute(String.format(
                        "INSERT INTO apexflow_order (id, user_id, total_amount, status) VALUES ('%s', 1001, 100.00, 1)",
                        orderId
                ));
                stmt.execute(String.format(
                        "INSERT INTO apexflow_logistics (order_id, status) VALUES ('%s', 'pending')",
                        orderId
                ));
            }
        } catch (Exception e) {
            fail("Failed to insert pagination test data");
        }

        // Act - 第一页
        List<Logistics> page1 = logisticsDAO.findPendingShipping(1, 5);

        // Assert
        assertEquals(5, page1.size(), "第一页应该有5条记录");

        // Act - 第二页
        List<Logistics> page2 = logisticsDAO.findPendingShipping(2, 5);

        // Assert
        assertEquals(5, page2.size(), "第二页应该有5条记录");

        // 验证没有重复
        String firstOrderId = page1.get(0).getOrderId();
        boolean foundInPage2 = page2.stream().anyMatch(log -> log.getOrderId().equals(firstOrderId));
        assertFalse(foundInPage2, "两页之间不应该有重复记录");
    }
}
