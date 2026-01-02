import com.apex.core.controller.LogisticsServlet;
import com.apex.core.service.LogisticsService;
import com.apex.core.dao.LogisticsDAO;
import com.apex.util.ConnectionPool;
import com.apex.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * LogisticsServlet 集成测试类
 * 使用H2内存数据库测试物流管理API功能
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("LogisticsServlet 集成测试")
public class LogisticsServletIntegrationTest {

    private LogisticsServlet logisticsServlet;
    private ObjectMapper objectMapper;
    private Connection conn;
    private AutoCloseable mockitoCloseable;

    // 测试数据
    private static final Integer ADMIN_USER_ID = 1;
    private static final String ADMIN_TOKEN = "admin_jwt_token";

    @BeforeAll
    void setUpAll() throws Exception {
        System.out.println("[LOGISTICS-SETUP] 开始初始化H2内存数据库...");

        // 确保使用H2内存数据库
        System.setProperty("apexflow.test.h2", "true");

        // 获取连接并初始化数据库
        conn = ConnectionPool.getConnection();

        // 创建ObjectMapper用于JSON序列化/反序列化
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // 初始化LogisticsServlet
        logisticsServlet = new LogisticsServlet();

        // 创建表（如果不存在）
        createTablesIfNotExists();

        System.out.println("[LOGISTICS-SETUP] H2数据库初始化完成");
    }

    @AfterAll
    void tearDownAll() {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("[LOGISTICS-TEARDOWN] 数据库连接已关闭");
            } catch (SQLException e) {
                System.err.println("[LOGISTICS-TEARDOWN] 关闭数据库连接失败: " + e.getMessage());
            }
        }
        ConnectionPool.shutdown();
        System.out.println("[LOGISTICS-TEARDOWN] 测试环境清理完成");
    }

    @BeforeEach
    void setUp() throws Exception {
        // 初始化Mockito
        mockitoCloseable = MockitoAnnotations.openMocks(this);

        // 清理并重置测试数据
        clearTestData();
        insertTestData();

        // 重新初始化Servlet以获取新的DAO实例
        initServlet();

        System.out.println("[LOGISTICS-SETUP-EACH] 测试数据已重置");
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mockitoCloseable != null) {
            mockitoCloseable.close();
        }
        System.out.println("[LOGISTICS-TEARDOWN-EACH] 单个测试完成");
        System.out.println("========================================");
    }

    // 重新初始化Servlet以注入依赖
    private void initServlet() throws Exception {
        // 创建真实的DAO实例
        LogisticsDAO logisticsDAO = new LogisticsDAO();

        // 创建LogisticsService实例
        LogisticsService logisticsService = new LogisticsService();

        // 使用反射注入logisticsService
        Field serviceField = LogisticsServlet.class.getDeclaredField("logisticsService");
        serviceField.setAccessible(true);
        serviceField.set(logisticsServlet, logisticsService);

        // 注入配置好的 ObjectMapper
        try {
            Field mapperField = logisticsServlet.getClass().getSuperclass().getDeclaredField("objectMapper");
            mapperField.setAccessible(true);
            mapperField.set(logisticsServlet, objectMapper);
        } catch (NoSuchFieldException e) {
            System.err.println("[LOGISTICS-INIT] 父类没有objectMapper字段: " + e.getMessage());
        }
    }

    // 创建表（如果不存在）
    private void createTablesIfNotExists() throws Exception {
        try (Statement stmt = conn.createStatement()) {
            // 禁用外键约束
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");

            // 检查表是否存在
            boolean logisticsTableExists = false;

            try {
                stmt.executeQuery("SELECT COUNT(*) FROM apexflow_logistics");
                logisticsTableExists = true;
                System.out.println("[LOGISTICS-DB] 物流表已存在");
            } catch (SQLException e) {
                // 表不存在，需要创建
                System.out.println("[LOGISTICS-DB] 物流表不存在，开始创建...");
            }

            if (!logisticsTableExists) {
                // 创建物流表
                stmt.execute("""
                    CREATE TABLE apexflow_logistics (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        order_id VARCHAR(50) NOT NULL UNIQUE,
                        express_company VARCHAR(50),
                        tracking_number VARCHAR(100),
                        status VARCHAR(20) DEFAULT 'pending',
                        sender_address VARCHAR(200),
                        receiver_address VARCHAR(200),
                        shipped_at TIMESTAMP,
                        delivered_at TIMESTAMP,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                """);
                System.out.println("[LOGISTICS-DB] 物流表创建完成");
            }

            // 启用外键约束
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
        } catch (SQLException e) {
            System.err.println("[LOGISTICS-DB] 创建表失败: " + e.getMessage());
            throw e;
        }
    }

    // 清除测试数据
    private void clearTestData() throws Exception {
        try (Statement stmt = conn.createStatement()) {
            // 先检查表是否存在
            try {
                stmt.execute("SELECT 1 FROM apexflow_logistics LIMIT 1");
                stmt.execute("DELETE FROM apexflow_logistics");
                // 重置自增ID
                stmt.execute("ALTER TABLE apexflow_logistics ALTER COLUMN id RESTART WITH 1");
            } catch (SQLException e) {
                // 表不存在，忽略
            }

            System.out.println("[LOGISTICS-DATA] 测试数据已清除");
        } catch (SQLException e) {
            System.err.println("[LOGISTICS-DATA] 清除测试数据失败: " + e.getMessage());
            throw e;
        }
    }

    // 插入测试数据
    private void insertTestData() throws Exception {
        try (Statement stmt = conn.createStatement()) {
            // 插入测试物流数据
            stmt.execute("""
                INSERT INTO apexflow_logistics (order_id, express_company, tracking_number, status,
                                                sender_address, receiver_address, shipped_at) VALUES
                ('ORDER001', '顺丰速运', 'SF1234567890', 'shipped',
                 '北京市朝阳区', '上海市浦东新区', '2023-12-01 10:00:00'),
                ('ORDER002', '京东物流', 'JD9876543210', 'pending',
                 '广东省深圳市', '浙江省杭州市', NULL),
                ('ORDER003', '圆通速递', 'YT5555555555', 'delivered',
                 '江苏省南京市', '四川省成都市', '2023-12-01 09:00:00'),
                ('ORDER004', '中通快递', 'ZT1111111111', 'shipped',
                 '天津市', '重庆市', '2023-12-01 11:00:00')
            """);

            System.out.println("[LOGISTICS-DATA] 测试数据插入完成");

            // 验证数据插入
            var rs = stmt.executeQuery("SELECT COUNT(*) as count FROM apexflow_logistics");
            if (rs.next()) {
                System.out.println("[LOGISTICS-DATA] 当前物流记录数量: " + rs.getInt("count"));
            }

            // 显示各种状态的物流记录数量
            rs = stmt.executeQuery("SELECT status, COUNT(*) as count FROM apexflow_logistics GROUP BY status");
            while (rs.next()) {
                System.out.println("[LOGISTICS-DATA] 状态 " + rs.getString("status") +
                        ": " + rs.getInt("count") + " 条");
            }
        } catch (SQLException e) {
            System.err.println("[LOGISTICS-DATA] 插入测试数据失败: " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(1)
    @DisplayName("1. 测试获取物流详情 - 成功")
    void testGetLogisticsById_Success() throws Exception {
        System.out.println("[LOGISTICS-TEST-1] 开始测试获取物流详情...");

        // 创建模拟请求和响应
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // 设置请求参数
        when(request.getPathInfo()).thenReturn("/1");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/logistics/1");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ADMIN_TOKEN);
        when(request.getHeader("User-Agent")).thenReturn("TestClient/1.0");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // 设置响应写入器
        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // 模拟JWT验证
        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(ADMIN_TOKEN)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.getUserIdFromToken(ADMIN_TOKEN)).thenReturn(ADMIN_USER_ID);

            // 调用 doGet 方法
            invokeProtectedMethod("doGet", request, response);

            printWriter.flush();

            // 验证响应
            verify(response, times(1)).setContentType("application/json");
            verify(response, times(1)).setCharacterEncoding("UTF-8");

            String jsonResponse = responseWriter.toString();
            System.out.println("[LOGISTICS-RESPONSE-1] 物流详情响应: " + jsonResponse);

            // 验证响应不为空
            assertNotNull(jsonResponse, "响应不能为空");
            assertFalse(jsonResponse.isEmpty(), "响应不能为空字符串");
            assertTrue(jsonResponse.contains("success"), "响应应包含success字段");

            System.out.println("[LOGISTICS-PASS-1] 测试获取物流详情成功");
        }
    }

    @Test
    @Order(2)
    @DisplayName("2. 测试根据订单号获取物流信息 - 成功")
    void testGetLogisticsByOrder_Success() throws Exception {
        System.out.println("[LOGISTICS-TEST-2] 开始测试根据订单号获取物流信息...");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/order/ORDER001");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/logistics/order/ORDER001");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ADMIN_TOKEN);
        when(request.getHeader("User-Agent")).thenReturn("TestClient/1.0");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(ADMIN_TOKEN)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.getUserIdFromToken(ADMIN_TOKEN)).thenReturn(ADMIN_USER_ID);
            invokeProtectedMethod("doGet", request, response);

            printWriter.flush();

            String jsonResponse = responseWriter.toString();
            System.out.println("[LOGISTICS-RESPONSE-2] 订单物流响应: " + jsonResponse);

            assertNotNull(jsonResponse);
            assertFalse(jsonResponse.isEmpty());
            assertTrue(jsonResponse.contains("success"), "响应应包含success字段");

            System.out.println("[LOGISTICS-PASS-2] 测试根据订单号获取物流信息成功");
        }
    }

    @Test
    @Order(3)
    @DisplayName("3. 测试创建物流信息 - 成功")
    void testCreateLogistics_Success() throws Exception {
        System.out.println("[LOGISTICS-TEST-3] 开始测试创建物流信息...");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn(null);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/logistics");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ADMIN_TOKEN);
        when(request.getHeader("User-Agent")).thenReturn("TestClient/1.0");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getContentType()).thenReturn("application/json;charset=UTF-8");

        // 准备创建物流请求数据
        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("orderId", "ORDER005");
        createRequest.put("expressCompany", "EMS");
        createRequest.put("trackingNumber", "EM123456789CN");
        createRequest.put("senderAddress", "北京市");
        createRequest.put("receiverAddress", "广州市");

        String requestBody = objectMapper.writeValueAsString(createRequest);
        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(ADMIN_TOKEN)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.getUserIdFromToken(ADMIN_TOKEN)).thenReturn(ADMIN_USER_ID);
            invokeProtectedMethod("doPost", request, response);
//            logisticsServlet.doPost(request, response);
            printWriter.flush();

            String jsonResponse = responseWriter.toString();
            System.out.println("[LOGISTICS-RESPONSE-3] 创建物流响应: " + jsonResponse);

            assertNotNull(jsonResponse);
            assertFalse(jsonResponse.isEmpty());
            assertTrue(jsonResponse.contains("success"), "响应应包含success字段");

            System.out.println("[LOGISTICS-PASS-3] 测试创建物流信息成功");
        }
    }

    @Test
    @Order(4)
    @DisplayName("4. 测试更新物流信息 - 成功")
    void testUpdateLogistics_Success() throws Exception {
        System.out.println("[LOGISTICS-TEST-4] 开始测试更新物流信息...");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/1");
        when(request.getMethod()).thenReturn("PUT");
        when(request.getRequestURI()).thenReturn("/api/logistics/1");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ADMIN_TOKEN);
        when(request.getHeader("User-Agent")).thenReturn("TestClient/1.0");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getContentType()).thenReturn("application/json;charset=UTF-8");

        // 准备更新物流请求数据
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("orderId", "ORDER001");
        updateRequest.put("expressCompany", "顺丰特快");
        updateRequest.put("trackingNumber", "SF1234567890-UPDATED");

        String requestBody = objectMapper.writeValueAsString(updateRequest);
        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(ADMIN_TOKEN)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.getUserIdFromToken(ADMIN_TOKEN)).thenReturn(ADMIN_USER_ID);
            invokeProtectedMethod("doPut", request, response);
//            logisticsServlet.doPut(request, response);
            printWriter.flush();

            String jsonResponse = responseWriter.toString();
            System.out.println("[LOGISTICS-RESPONSE-4] 更新物流响应: " + jsonResponse);

            assertNotNull(jsonResponse);
            assertFalse(jsonResponse.isEmpty());
            assertTrue(jsonResponse.contains("success"), "响应应包含success字段");

            System.out.println("[LOGISTICS-PASS-4] 测试更新物流信息成功");
        }
    }

    @Test
    @Order(5)
    @DisplayName("5. 测试更新物流状态 - 成功")
    void testUpdateLogisticsStatus_Success() throws Exception {
        System.out.println("[LOGISTICS-TEST-5] 开始测试更新物流状态...");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/2/status");
        when(request.getMethod()).thenReturn("PUT");
        when(request.getRequestURI()).thenReturn("/api/logistics/2/status");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ADMIN_TOKEN);
        when(request.getHeader("User-Agent")).thenReturn("TestClient/1.0");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getContentType()).thenReturn("application/json;charset=UTF-8");

        // 准备更新状态请求数据
        Map<String, Object> statusRequest = new HashMap<>();
        statusRequest.put("status", "shipped");

        String requestBody = objectMapper.writeValueAsString(statusRequest);
        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(ADMIN_TOKEN)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.getUserIdFromToken(ADMIN_TOKEN)).thenReturn(ADMIN_USER_ID);
            invokeProtectedMethod("doPut", request, response);

            printWriter.flush();

            String jsonResponse = responseWriter.toString();
            System.out.println("[LOGISTICS-RESPONSE-5] 更新状态响应: " + jsonResponse);

            assertNotNull(jsonResponse);
            assertFalse(jsonResponse.isEmpty());
            assertTrue(jsonResponse.contains("success"), "响应应包含success字段");

            System.out.println("[LOGISTICS-PASS-5] 测试更新物流状态成功");
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. 测试获取待发货列表 - 成功")
    void testGetPendingShipping_Success() throws Exception {
        System.out.println("[LOGISTICS-TEST-6] 开始测试获取待发货列表...");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/pending");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/logistics/pending");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ADMIN_TOKEN);
        when(request.getHeader("User-Agent")).thenReturn("TestClient/1.0");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getParameter("page")).thenReturn("1");
        when(request.getParameter("pageSize")).thenReturn("10");

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(ADMIN_TOKEN)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.getUserIdFromToken(ADMIN_TOKEN)).thenReturn(ADMIN_USER_ID);
            invokeProtectedMethod("doGet", request, response);

            printWriter.flush();

            String jsonResponse = responseWriter.toString();
            System.out.println("[LOGISTICS-RESPONSE-6] 待发货列表响应: " + jsonResponse);

            assertNotNull(jsonResponse);
            assertFalse(jsonResponse.isEmpty());
            assertTrue(jsonResponse.contains("success"), "响应应包含success字段");

            System.out.println("[LOGISTICS-PASS-6] 测试获取待发货列表成功");
        }
    }

    @Test
    @Order(7)
    @DisplayName("7. 测试获取运输中列表 - 成功")
    void testGetInTransit_Success() throws Exception {
        System.out.println("[LOGISTICS-TEST-7] 开始测试获取运输中列表...");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/in-transit");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/logistics/in-transit");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ADMIN_TOKEN);
        when(request.getHeader("User-Agent")).thenReturn("TestClient/1.0");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getParameter("page")).thenReturn("1");
        when(request.getParameter("pageSize")).thenReturn("10");

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(ADMIN_TOKEN)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.getUserIdFromToken(ADMIN_TOKEN)).thenReturn(ADMIN_USER_ID);
            invokeProtectedMethod("doGet", request, response);

            printWriter.flush();

            String jsonResponse = responseWriter.toString();
            System.out.println("[LOGISTICS-RESPONSE-7] 运输中列表响应: " + jsonResponse);

            assertNotNull(jsonResponse);
            assertFalse(jsonResponse.isEmpty());
            assertTrue(jsonResponse.contains("success"), "响应应包含success字段");

            System.out.println("[LOGISTICS-PASS-7] 测试获取运输中列表成功");
        }
    }

    @Test
    @Order(8)
    @DisplayName("8. 测试删除物流记录 - 成功")
    void testDeleteLogistics_Success() throws Exception {
        System.out.println("[LOGISTICS-TEST-8] 开始测试删除物流记录...");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/2");
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getRequestURI()).thenReturn("/api/logistics/2");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ADMIN_TOKEN);
        when(request.getHeader("User-Agent")).thenReturn("TestClient/1.0");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(ADMIN_TOKEN)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.getUserIdFromToken(ADMIN_TOKEN)).thenReturn(ADMIN_USER_ID);
            invokeProtectedMethod("doDelete", request, response);
            printWriter.flush();

            String jsonResponse = responseWriter.toString();
            System.out.println("[LOGISTICS-RESPONSE-8] 删除物流记录响应: " + jsonResponse);

            assertNotNull(jsonResponse);
            assertFalse(jsonResponse.isEmpty());

            System.out.println("[LOGISTICS-PASS-8] 测试删除物流记录成功");
        }
    }

    @Test
    @Order(9)
    @DisplayName("9. 测试获取物流统计 - 成功")
    void testGetLogisticsStats_Success() throws Exception {
        System.out.println("[LOGISTICS-TEST-9] 开始测试获取物流统计...");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/stats");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/logistics/stats");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ADMIN_TOKEN);
        when(request.getHeader("User-Agent")).thenReturn("TestClient/1.0");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(ADMIN_TOKEN)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.getUserIdFromToken(ADMIN_TOKEN)).thenReturn(ADMIN_USER_ID);
            invokeProtectedMethod("doGet", request, response);
            printWriter.flush();

            String jsonResponse = responseWriter.toString();
            System.out.println("[LOGISTICS-RESPONSE-9] 物流统计响应: " + jsonResponse);

            assertNotNull(jsonResponse);
            assertFalse(jsonResponse.isEmpty());
            assertTrue(jsonResponse.contains("success"), "响应应包含success字段");

            System.out.println("[LOGISTICS-PASS-9] 测试获取物流统计成功");
        }
    }

    @Test
    @Order(10)
    @DisplayName("10. 测试数据库基本操作")
    void testDatabaseBasicOperations() throws Exception {
        System.out.println("[LOGISTICS-TEST-10] 开始测试数据库基本操作...");

        // 验证表存在
        try (Statement stmt = conn.createStatement()) {
            // 1. 查询物流记录数量
            var rs = stmt.executeQuery("SELECT COUNT(*) as count FROM apexflow_logistics");
            assertTrue(rs.next());
            int logisticsCount = rs.getInt("count");
            System.out.println("[LOGISTICS-DB-10] 当前物流记录数量: " + logisticsCount);
            assertTrue(logisticsCount > 0, "物流记录数量应该大于0");

            // 2. 查询各种状态的物流记录
            rs = stmt.executeQuery("SELECT status, COUNT(*) as count FROM apexflow_logistics GROUP BY status");
            int total = 0;
            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                total += count;
                System.out.println("[LOGISTICS-DB-10] 状态 " + status + ": " + count + " 条");
            }
            assertEquals(logisticsCount, total, "分组统计总和应等于总数");

            // 3. 查询物流详情
            rs = stmt.executeQuery("SELECT order_id, express_company, tracking_number, status FROM apexflow_logistics WHERE id = 1");
            assertTrue(rs.next());
            String orderId = rs.getString("order_id");
            String expressCompany = rs.getString("express_company");
            String trackingNumber = rs.getString("tracking_number");
            String status = rs.getString("status");

            assertNotNull(orderId);
            assertNotNull(expressCompany);
            assertNotNull(trackingNumber);
            assertNotNull(status);

            System.out.println("[LOGISTICS-DB-10] 示例物流: 订单 " + orderId + ", 快递公司: " +
                    expressCompany + ", 运单号: " + trackingNumber + ", 状态: " + status);
        }

        System.out.println("[LOGISTICS-PASS-10] 测试数据库基本操作成功");
    }

    // 在测试类中添加一个辅助方法
    private void invokeProtectedMethod(String methodName, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Class<?>[] paramTypes = new Class<?>[]{HttpServletRequest.class, HttpServletResponse.class};
        java.lang.reflect.Method method = LogisticsServlet.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true); // 设置可访问
        method.invoke(logisticsServlet, request, response);
    }
}