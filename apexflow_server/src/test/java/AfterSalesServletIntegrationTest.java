import com.apex.core.controller.AfterSalesServlet;
import com.apex.util.ConnectionPool;
import com.apex.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * AfterSalesServlet 集成测试
 * 专注于测试控制器API，使用H2内存数据库
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("AfterSalesServlet 集成测试")
public class AfterSalesServletIntegrationTest {

    private AfterSalesServlet afterSalesServlet;
    private ObjectMapper objectMapper;
    private Connection conn;

    // 测试数据
    private static final String ADMIN_TOKEN = "admin_jwt_token";
    private static final Integer ADMIN_USER_ID = 1;

    @BeforeAll
    void setUpAll() throws Exception {
        System.out.println("=== 初始化测试环境 ===");

        // 获取数据库连接
        conn = ConnectionPool.getConnection();

        // 初始化ObjectMapper
        objectMapper = new ObjectMapper();

        // 初始化Servlet
        afterSalesServlet = new AfterSalesServlet();

        // 配置ObjectMapper支持Java 8时间类型
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // 注入配置好的 ObjectMapper（如果有相应的字段）
        try {
            Field mapperField = afterSalesServlet.getClass().getSuperclass().getDeclaredField("objectMapper");
            mapperField.setAccessible(true);
            mapperField.set(afterSalesServlet, objectMapper);
        } catch (NoSuchFieldException e) {
            // 如果父类没有这个字段，尝试其他方式
            // 或者创建自定义的 BaseServlet 子类
        }

        // 创建测试表
        createTestTables();

        System.out.println("=== 测试环境初始化完成 ===");
    }

    @AfterAll
    void tearDownAll() throws Exception {
        System.out.println("=== 清理测试环境 ===");

        if (conn != null) {
            conn.close();
        }
        ConnectionPool.shutdown();

        System.out.println("=== 测试环境清理完成 ===");
    }

    @BeforeEach
    void setUp() throws Exception {
        System.out.println("--- 准备测试数据 ---");

        // 清理并插入测试数据
        clearTestData();
        insertTestData();

        System.out.println("--- 测试数据准备完成 ---");
    }

    /**
     * 创建测试表
     */
    private void createTestTables() throws Exception {
        try (Statement stmt = conn.createStatement()) {
            // 禁用外键约束
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");

            // 创建用户表
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS apexflow_system_user (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    password_hash VARCHAR(255) NOT NULL,
                    salt VARCHAR(32) NOT NULL,
                    is_admin BOOLEAN DEFAULT FALSE,
                    can_manage_after_sales BOOLEAN DEFAULT FALSE,
                    status TINYINT DEFAULT 1
                )
            """);

            // 创建订单表
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS apexflow_order (
                    id VARCHAR(50) PRIMARY KEY,
                    user_id INT NOT NULL,
                    total_amount DECIMAL(10,2) NOT NULL,
                    status TINYINT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // 创建售后服务表
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS apexflow_after_sales (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    order_id VARCHAR(50) NOT NULL,
                    type TINYINT NOT NULL,
                    reason VARCHAR(500),
                    status TINYINT DEFAULT 1,
                    refund_amount DECIMAL(10,2),
                    apply_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    process_time TIMESTAMP,
                    process_remark VARCHAR(200)
                )
            """);

            // 启用外键约束
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");

            System.out.println("[DB] 测试表创建完成");
        }
    }

    /**
     * 清理测试数据
     */
    private void clearTestData() throws Exception {
        try (Statement stmt = conn.createStatement()) {
            // 清理顺序：售后表 -> 订单表 -> 用户表
            stmt.execute("DELETE FROM apexflow_after_sales");
            stmt.execute("DELETE FROM apexflow_order");
            stmt.execute("DELETE FROM apexflow_system_user");

            System.out.println("[DB] 测试数据已清理");
        }
    }

    /**
     * 插入测试数据
     */
    private void insertTestData() throws Exception {
        try (Statement stmt = conn.createStatement()) {
            // 插入管理员用户
            stmt.execute("INSERT INTO apexflow_system_user (id, username, password_hash, salt, is_admin, can_manage_after_sales) VALUES " +
                    "(1, 'admin', 'hash1', 'salt1', TRUE, TRUE)");

            // 插入普通用户（有售后权限）
            stmt.execute("INSERT INTO apexflow_system_user (id, username, password_hash, salt, is_admin, can_manage_after_sales) VALUES " +
                    "(2, 'sales_user', 'hash2', 'salt2', FALSE, TRUE)");

            // 插入测试订单
            stmt.execute("INSERT INTO apexflow_order (id, user_id, total_amount, status) VALUES " +
                    "('ORDER20231201001', 1001, 7999.00, 4)");

            stmt.execute("INSERT INTO apexflow_order (id, user_id, total_amount, status) VALUES " +
                    "('ORDER20231201002', 1002, 18999.00, 4)");

            // 插入测试售后数据
            for (int i = 1; i <= 5; i++) {
                stmt.execute(String.format(
                        "INSERT INTO apexflow_after_sales (id, order_id, type, reason, status, refund_amount) VALUES " +
                                "(%d, 'ORDER20231201001', %d, '测试原因%d', %d, %d.00)",
                        i, (i % 3) + 1, i, (i % 4) + 1, i * 100));
            }

            System.out.println("[DATA] 测试数据插入完成");
        }
    }

    /**
     * 使用反射调用Servlet方法
     */
    private void invokeServletMethod(String methodName, HttpServletRequest request,
                                     HttpServletResponse response) throws Exception {
        try {
            Method method = AfterSalesServlet.class.getDeclaredMethod(
                    methodName, HttpServletRequest.class, HttpServletResponse.class);
            method.setAccessible(true);
            method.invoke(afterSalesServlet, request, response);
        } catch (NoSuchMethodException e) {
            // 尝试从父类查找
            Method method = AfterSalesServlet.class.getSuperclass()
                    .getDeclaredMethod(methodName, HttpServletRequest.class, HttpServletResponse.class);
            method.setAccessible(true);
            method.invoke(afterSalesServlet, request, response);
        }
    }

    @Test
    @Order(1)
    @DisplayName("1. 测试创建售后申请")
    void testCreateAfterSales() throws Exception {
        System.out.println("[TEST-1] 测试创建售后申请");

        // 准备请求和响应
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // 配置请求
        when(request.getPathInfo()).thenReturn("/");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/after-sales");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ADMIN_TOKEN);
        when(request.getHeader("User-Agent")).thenReturn("TestClient");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getContentType()).thenReturn("application/json;charset=UTF-8");

        // 准备请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("orderId", "ORDER20231201002");
        requestBody.put("type", 1);
        requestBody.put("reason", "测试创建售后申请");
        requestBody.put("refundAmount", 18999.00);

        String jsonBody = objectMapper.writeValueAsString(requestBody);
        BufferedReader reader = new BufferedReader(new StringReader(jsonBody));
        when(request.getReader()).thenReturn(reader);

        // 准备响应
        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // 模拟JWT验证
        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(ADMIN_TOKEN)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.getUserIdFromToken(ADMIN_TOKEN)).thenReturn(ADMIN_USER_ID);

            // 调用Servlet
            invokeServletMethod("doPost", request, response);
            printWriter.flush();

            // 验证响应
            String jsonResponse = responseWriter.toString();
            System.out.println("[RESPONSE-1] " + jsonResponse);

            assertNotNull(jsonResponse);
            assertFalse(jsonResponse.isEmpty());
            assertTrue(jsonResponse.contains("success"));
            assertTrue(jsonResponse.contains("售后申请创建成功"));

            System.out.println("[PASS-1] 创建售后申请测试通过");
        }
    }

    @Test
    @Order(2)
    @DisplayName("2. 测试获取售后详情")
    void testGetAfterSalesDetail() throws Exception {
        System.out.println("[TEST-2] 测试获取售后详情");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/1");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/after-sales/1");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ADMIN_TOKEN);
        when(request.getHeader("User-Agent")).thenReturn("TestClient");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(ADMIN_TOKEN)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.getUserIdFromToken(ADMIN_TOKEN)).thenReturn(ADMIN_USER_ID);

            invokeServletMethod("doGet", request, response);
            printWriter.flush();

            String jsonResponse = responseWriter.toString();
            System.out.println("[RESPONSE-2] " + jsonResponse);

            assertNotNull(jsonResponse);
            assertFalse(jsonResponse.isEmpty());
            assertTrue(jsonResponse.contains("success"));
            assertTrue(jsonResponse.contains("获取售后详情成功"));

            System.out.println("[PASS-2] 获取售后详情测试通过");
        }
    }

    @Test
    @Order(3)
    @DisplayName("3. 测试获取售后列表")
    void testGetAfterSalesList() throws Exception {
        System.out.println("[TEST-3] 测试获取售后列表");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn(null);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/after-sales");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ADMIN_TOKEN);
        when(request.getHeader("User-Agent")).thenReturn("TestClient");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getParameter("page")).thenReturn("1");
        when(request.getParameter("pageSize")).thenReturn("10");

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(ADMIN_TOKEN)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.getUserIdFromToken(ADMIN_TOKEN)).thenReturn(ADMIN_USER_ID);

            invokeServletMethod("doGet", request, response);
            printWriter.flush();

            String jsonResponse = responseWriter.toString();
            System.out.println("[RESPONSE-3] " + jsonResponse);

            assertNotNull(jsonResponse);
            assertFalse(jsonResponse.isEmpty());
            assertTrue(jsonResponse.contains("success"));
            assertTrue(jsonResponse.contains("获取售后列表成功"));

            System.out.println("[PASS-3] 获取售后列表测试通过");
        }
    }

    @Test
    @Order(4)
    @DisplayName("4. 测试获取订单售后记录")
    void testGetAfterSalesByOrder() throws Exception {
        System.out.println("[TEST-4] 测试获取订单售后记录");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/order/ORDER20231201001");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/after-sales/order/ORDER20231201001");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ADMIN_TOKEN);
        when(request.getHeader("User-Agent")).thenReturn("TestClient");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(ADMIN_TOKEN)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.getUserIdFromToken(ADMIN_TOKEN)).thenReturn(ADMIN_USER_ID);

            invokeServletMethod("doGet", request, response);
            printWriter.flush();

            String jsonResponse = responseWriter.toString();
            System.out.println("[RESPONSE-4] " + jsonResponse);

            assertNotNull(jsonResponse);
            assertFalse(jsonResponse.isEmpty());
            assertTrue(jsonResponse.contains("success"));
            assertTrue(jsonResponse.contains("获取订单售后记录成功"));

            System.out.println("[PASS-4] 获取订单售后记录测试通过");
        }
    }

    @Test
    @Order(5)
    @DisplayName("5. 测试根据状态获取售后列表")
    void testGetAfterSalesByStatus() throws Exception {
        System.out.println("[TEST-5] 测试根据状态获取售后列表");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/status/1");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/after-sales/status/1");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ADMIN_TOKEN);
        when(request.getHeader("User-Agent")).thenReturn("TestClient");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getParameter("page")).thenReturn("1");
        when(request.getParameter("pageSize")).thenReturn("5");

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(ADMIN_TOKEN)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.getUserIdFromToken(ADMIN_TOKEN)).thenReturn(ADMIN_USER_ID);

            invokeServletMethod("doGet", request, response);
            printWriter.flush();

            String jsonResponse = responseWriter.toString();
            System.out.println("[RESPONSE-5] " + jsonResponse);

            assertNotNull(jsonResponse);
            assertFalse(jsonResponse.isEmpty());
            assertTrue(jsonResponse.contains("success"));
            assertTrue(jsonResponse.contains("获取状态售后列表成功"));

            System.out.println("[PASS-5] 根据状态获取售后列表测试通过");
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. 测试更新售后状态")
    void testUpdateAfterSalesStatus() throws Exception {
        System.out.println("[TEST-6] 测试更新售后状态");

        // 先查询一个状态为"申请中"的售后记录
        int afterSalesId;
        try (Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT id FROM apexflow_after_sales WHERE status = 1 LIMIT 1")) {
            assertTrue(rs.next(), "应该能找到状态为'申请中'的售后记录");
            afterSalesId = rs.getInt("id");
            System.out.println("[DATA] 找到状态为'申请中'的售后记录ID: " + afterSalesId);
        }

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // 使用找到的售后记录ID
        when(request.getPathInfo()).thenReturn("/" + afterSalesId + "/status");
        when(request.getMethod()).thenReturn("PUT");
        when(request.getRequestURI()).thenReturn("/api/after-sales/" + afterSalesId + "/status");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ADMIN_TOKEN);
        when(request.getHeader("User-Agent")).thenReturn("TestClient");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getContentType()).thenReturn("application/json;charset=UTF-8");

        // 准备请求体 - 将状态从"申请中"(1)更新为"审核通过"(2)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("status", 2); // 审核通过
        requestBody.put("remark", "审核通过，同意处理");

        String jsonBody = objectMapper.writeValueAsString(requestBody);
        BufferedReader reader = new BufferedReader(new StringReader(jsonBody));
        when(request.getReader()).thenReturn(reader);

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(ADMIN_TOKEN)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.getUserIdFromToken(ADMIN_TOKEN)).thenReturn(ADMIN_USER_ID);

            invokeServletMethod("doPut", request, response);
            printWriter.flush();

            String jsonResponse = responseWriter.toString();
            System.out.println("[RESPONSE-6] " + jsonResponse);

            assertNotNull(jsonResponse);
            assertFalse(jsonResponse.isEmpty());
            assertTrue(jsonResponse.contains("success"));
            assertTrue(jsonResponse.contains("更新售后状态成功"));

            System.out.println("[PASS-6] 更新售后状态测试通过");
        }
    }
    @Test
    @Order(7)
    @DisplayName("7. 测试删除售后记录")
    void testDeleteAfterSales() throws Exception {
        System.out.println("[TEST-7] 测试删除售后记录");

        // 先插入一个状态为"申请中"的售后记录
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO apexflow_after_sales (id, order_id, type, reason, status) VALUES " +
                    "(100, 'ORDER20231201002', 1, '测试删除记录', 1)");
        }

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/100");
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getRequestURI()).thenReturn("/api/after-sales/100");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ADMIN_TOKEN);
        when(request.getHeader("User-Agent")).thenReturn("TestClient");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(ADMIN_TOKEN)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.getUserIdFromToken(ADMIN_TOKEN)).thenReturn(ADMIN_USER_ID);

            invokeServletMethod("doDelete", request, response);
            printWriter.flush();

            String jsonResponse = responseWriter.toString();
            System.out.println("[RESPONSE-7] " + jsonResponse);

            assertNotNull(jsonResponse);
            assertFalse(jsonResponse.isEmpty());
            assertTrue(jsonResponse.contains("success"));
            assertTrue(jsonResponse.contains("删除售后记录成功"));

            System.out.println("[PASS-7] 删除售后记录测试通过");
        }
    }

    @Test
    @Order(8)
    @DisplayName("8. 测试获取售后统计")
    void testGetAfterSalesStats() throws Exception {
        System.out.println("[TEST-8] 测试获取售后统计");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/stats");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/after-sales/stats");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ADMIN_TOKEN);
        when(request.getHeader("User-Agent")).thenReturn("TestClient");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(ADMIN_TOKEN)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.getUserIdFromToken(ADMIN_TOKEN)).thenReturn(ADMIN_USER_ID);

            invokeServletMethod("doGet", request, response);
            printWriter.flush();

            String jsonResponse = responseWriter.toString();
            System.out.println("[RESPONSE-8] " + jsonResponse);

            assertNotNull(jsonResponse);
            assertFalse(jsonResponse.isEmpty());
            assertTrue(jsonResponse.contains("success"));
            assertTrue(jsonResponse.contains("获取售后统计成功"));

            System.out.println("[PASS-8] 获取售后统计测试通过");
        }
    }

    @Test
    @Order(9)
    @DisplayName("9. 测试无效API路径")
    void testInvalidApiPath() throws Exception {
        System.out.println("[TEST-9] 测试无效API路径");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/invalid/path");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/after-sales/invalid/path");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ADMIN_TOKEN);
        when(request.getHeader("User-Agent")).thenReturn("TestClient");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(ADMIN_TOKEN)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.getUserIdFromToken(ADMIN_TOKEN)).thenReturn(ADMIN_USER_ID);

            invokeServletMethod("doGet", request, response);
            printWriter.flush();

            String jsonResponse = responseWriter.toString();
            System.out.println("[RESPONSE-9] " + jsonResponse);

            assertNotNull(jsonResponse);
            assertFalse(jsonResponse.isEmpty());
            assertTrue(jsonResponse.contains("404") || jsonResponse.contains("NOT_FOUND"));

            System.out.println("[PASS-9] 无效API路径测试通过");
        }
    }

    @Test
    @Order(10)
    @DisplayName("10. 测试空参数处理")
    void testEmptyParameterHandling() throws Exception {
        System.out.println("[TEST-10] 测试空参数处理");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn(null);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/after-sales");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ADMIN_TOKEN);
        when(request.getHeader("User-Agent")).thenReturn("TestClient");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        // 不提供page和pageSize参数，测试默认值

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(ADMIN_TOKEN)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.getUserIdFromToken(ADMIN_TOKEN)).thenReturn(ADMIN_USER_ID);

            invokeServletMethod("doGet", request, response);
            printWriter.flush();

            String jsonResponse = responseWriter.toString();
            System.out.println("[RESPONSE-10] " + jsonResponse);

            assertNotNull(jsonResponse);
            assertFalse(jsonResponse.isEmpty());
            assertTrue(jsonResponse.contains("success"));

            System.out.println("[PASS-10] 空参数处理测试通过");
        }
    }

    @Test
    @Order(11)
    @DisplayName("11. 验证数据库表结构")
    void testDatabaseTableStructure() throws Exception {
        System.out.println("[TEST-11] 验证数据库表结构");

        try (Statement stmt = conn.createStatement()) {
            // 验证用户表
            var rs = stmt.executeQuery("SELECT COUNT(*) as count FROM apexflow_system_user");
            assertTrue(rs.next());
            int userCount = rs.getInt("count");
            System.out.println("[DB] 用户表记录数: " + userCount);
            assertTrue(userCount > 0);

            // 验证订单表
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM apexflow_order");
            assertTrue(rs.next());
            int orderCount = rs.getInt("count");
            System.out.println("[DB] 订单表记录数: " + orderCount);
            assertTrue(orderCount > 0);

            // 验证售后表
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM apexflow_after_sales");
            assertTrue(rs.next());
            int afterSalesCount = rs.getInt("count");
            System.out.println("[DB] 售后表记录数: " + afterSalesCount);
            assertTrue(afterSalesCount > 0);

            System.out.println("[PASS-11] 数据库表结构验证通过");
        }
    }
}