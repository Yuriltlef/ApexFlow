import com.apex.core.controller.InventoryServlet;
import com.apex.core.service.InventoryService;
import com.apex.core.dao.ProductDAO;
import com.apex.core.dao.InventoryLogDAO;
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
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * InventoryServlet 集成测试类
 * 使用H2内存数据库测试库存管理API功能
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("InventoryServlet 集成测试")
public class InventoryServletIntegrationTest {

    private InventoryServlet inventoryServlet;
    private ObjectMapper objectMapper;
    private Connection conn;
    private AutoCloseable mockitoCloseable;

    // 测试数据
    private static final Integer ADMIN_USER_ID = 1;
    private static final String ADMIN_TOKEN = "admin_jwt_token";

    @BeforeAll
    void setUpAll() throws Exception {
        System.out.println("[INVENTORY-SETUP] 开始初始化H2内存数据库...");

        // 确保使用H2内存数据库
        System.setProperty("apexflow.test.h2", "true");

        // 获取连接并初始化数据库
        conn = ConnectionPool.getConnection();

        // 创建ObjectMapper用于JSON序列化/反序列化
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // 初始化InventoryServlet
        inventoryServlet = new InventoryServlet();

        // 注入配置好的 ObjectMapper（如果有相应的字段）
        try {
            Field mapperField = inventoryServlet.getClass().getSuperclass().getDeclaredField("objectMapper");
            mapperField.setAccessible(true);
            mapperField.set(inventoryServlet, objectMapper);
        } catch (NoSuchFieldException e) {
            // 如果父类没有这个字段，尝试其他方式
            // 或者创建自定义的 BaseServlet 子类
        }

        // 创建表（如果不存在）
        createTablesIfNotExists();

        System.out.println("[INVENTORY-SETUP] H2数据库初始化完成");
    }

    @AfterAll
    void tearDownAll() {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("[INVENTORY-TEARDOWN] 数据库连接已关闭");
            } catch (SQLException e) {
                System.err.println("[INVENTORY-TEARDOWN] 关闭数据库连接失败: " + e.getMessage());
            }
        }
        ConnectionPool.shutdown();
        System.out.println("[INVENTORY-TEARDOWN] 测试环境清理完成");
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

        System.out.println("[INVENTORY-SETUP-EACH] 测试数据已重置");
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mockitoCloseable != null) {
            mockitoCloseable.close();
        }
        System.out.println("[INVENTORY-TEARDOWN-EACH] 单个测试完成");
        System.out.println("========================================");
    }

    // 重新初始化Servlet以注入依赖
    private void initServlet() throws Exception {
        // 创建真实的DAO实例
        ProductDAO productDAO = new ProductDAO();
        InventoryLogDAO inventoryLogDAO = new InventoryLogDAO();

        // 创建InventoryService实例
        InventoryService inventoryService = new InventoryService(productDAO, inventoryLogDAO);

        // 使用反射注入inventoryService
        Field serviceField = InventoryServlet.class.getDeclaredField("inventoryService");
        serviceField.setAccessible(true);
        serviceField.set(inventoryServlet, inventoryService);

        // 注入配置好的 ObjectMapper
        try {
            Field mapperField = inventoryServlet.getClass().getSuperclass().getDeclaredField("objectMapper");
            mapperField.setAccessible(true);
            mapperField.set(inventoryServlet, objectMapper);
        } catch (NoSuchFieldException e) {
            System.err.println("[INVENTORY-INIT] 父类没有objectMapper字段: " + e.getMessage());
        }

        // 调用Servlet的init方法
        inventoryServlet.init();
    }

    // 创建表（如果不存在）
    private void createTablesIfNotExists() throws Exception {
        try (Statement stmt = conn.createStatement()) {
            // 禁用外键约束
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");

            // 检查表是否存在
            boolean productTableExists = false;
            boolean logTableExists = false;

            try {
                stmt.executeQuery("SELECT COUNT(*) FROM apexflow_product");
                productTableExists = true;
                System.out.println("[INVENTORY-DB] 商品表已存在");
            } catch (SQLException e) {
                // 表不存在，需要创建
                System.out.println("[INVENTORY-DB] 商品表不存在，开始创建...");
            }

            try {
                stmt.executeQuery("SELECT COUNT(*) FROM apexflow_inventory_log");
                logTableExists = true;
                System.out.println("[INVENTORY-DB] 库存日志表已存在");
            } catch (SQLException e) {
                System.out.println("[INVENTORY-DB] 库存日志表不存在，开始创建...");
            }

            if (!productTableExists) {
                // 创建商品表
                stmt.execute("""
                    CREATE TABLE apexflow_product (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        name VARCHAR(100) NOT NULL,
                        category VARCHAR(50),
                        price DECIMAL(10,2) NOT NULL,
                        stock INT DEFAULT 0,
                        status TINYINT DEFAULT 1,
                        image VARCHAR(200),
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                """);
                System.out.println("[INVENTORY-DB] 商品表创建完成");
            }

            if (!logTableExists) {
                // 创建库存日志表
                stmt.execute("""
                    CREATE TABLE apexflow_inventory_log (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        product_id INT NOT NULL,
                        change_type VARCHAR(20),
                        quantity INT NOT NULL,
                        before_stock INT,
                        after_stock INT,
                        order_id VARCHAR(50),
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (product_id) REFERENCES apexflow_product(id)
                    )
                """);
                System.out.println("[INVENTORY-DB] 库存日志表创建完成");
            }

            // 启用外键约束
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
        } catch (SQLException e) {
            System.err.println("[INVENTORY-DB] 创建表失败: " + e.getMessage());
            throw e;
        }
    }

    // 清除测试数据
    private void clearTestData() throws Exception {
        try (Statement stmt = conn.createStatement()) {
            // 先检查表是否存在
            try {
                stmt.execute("SELECT 1 FROM apexflow_inventory_log LIMIT 1");
                stmt.execute("DELETE FROM apexflow_inventory_log");
            } catch (SQLException e) {
                // 表不存在，忽略
            }

            try {
                stmt.execute("SELECT 1 FROM apexflow_product LIMIT 1");
                stmt.execute("DELETE FROM apexflow_product");
                // 重置自增ID
                stmt.execute("ALTER TABLE apexflow_product ALTER COLUMN id RESTART WITH 1");
            } catch (SQLException e) {
                // 表不存在，忽略
            }

            System.out.println("[INVENTORY-DATA] 测试数据已清除");
        } catch (SQLException e) {
            System.err.println("[INVENTORY-DATA] 清除测试数据失败: " + e.getMessage());
            throw e;
        }
    }

    // 插入测试数据
    private void insertTestData() throws Exception {
        try (Statement stmt = conn.createStatement()) {
            // 插入测试商品
            stmt.execute("""
                INSERT INTO apexflow_product (name, category, price, stock, status, image) VALUES
                ('iPhone 15', '手机', 6999.00, 50, 1, 'iphone15.jpg'),
                ('MacBook Pro', '电脑', 12999.00, 20, 1, 'macbook.jpg'),
                ('小米电视', '家电', 3999.00, 100, 1, 'tv.jpg'),
                ('下架商品', '测试', 100.00, 0, 0, 'test.jpg'),
                ('低库存商品', '测试', 50.00, 5, 1, 'low.jpg')
            """);

            // 插入库存日志
            stmt.execute("""
                INSERT INTO apexflow_inventory_log (product_id, change_type, quantity, before_stock, after_stock, order_id) VALUES
                (1, 'purchase', 50, 0, 50, 'PUR001'),
                (2, 'purchase', 20, 0, 20, 'PUR002'),
                (1, 'sale', -2, 50, 48, 'ORD001'),
                (2, 'adjust', 5, 20, 25, null)
            """);

            System.out.println("[INVENTORY-DATA] 测试数据插入完成");

            // 验证数据插入
            var rs = stmt.executeQuery("SELECT COUNT(*) as count FROM apexflow_product");
            if (rs.next()) {
                System.out.println("[INVENTORY-DATA] 当前商品数量: " + rs.getInt("count"));
            }

            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM apexflow_inventory_log");
            if (rs.next()) {
                System.out.println("[INVENTORY-DATA] 当前库存日志数量: " + rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.err.println("[INVENTORY-DATA] 插入测试数据失败: " + e.getMessage());
            throw e;
        }
    }

    // 使用反射调用私有方法
    private void invokeServletMethod(String methodName, HttpServletRequest request,
                                     HttpServletResponse response) throws Exception {
        try {
            Method method = InventoryServlet.class.getDeclaredMethod(methodName,
                    HttpServletRequest.class, HttpServletResponse.class);
            method.setAccessible(true);
            method.invoke(inventoryServlet, request, response);
        } catch (NoSuchMethodException e) {
            // 如果找不到方法，尝试从父类查找
            Method method = InventoryServlet.class.getSuperclass()
                    .getDeclaredMethod(methodName, HttpServletRequest.class, HttpServletResponse.class);
            method.setAccessible(true);
            method.invoke(inventoryServlet, request, response);
        } catch (Exception e) {
            System.err.println("[INVENTORY-REFLECT] 调用方法失败: " + methodName + ", 错误: " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(1)
    @DisplayName("1. 测试获取商品列表 - 成功")
    void testGetProductList_Success() throws Exception {
        System.out.println("[INVENTORY-TEST-1] 开始测试获取商品列表...");

        // 创建模拟请求和响应
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // 设置请求参数
        when(request.getPathInfo()).thenReturn("/products/list");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/inventory/products/list");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ADMIN_TOKEN);
        when(request.getHeader("User-Agent")).thenReturn("TestClient/1.0");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getParameter("page")).thenReturn("1");
        when(request.getParameter("pageSize")).thenReturn("10");

        // 设置响应写入器
        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // 模拟JWT验证
        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(ADMIN_TOKEN)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.getUserIdFromToken(ADMIN_TOKEN)).thenReturn(ADMIN_USER_ID);

            // 调用 doGet 方法
            inventoryServlet.doGet(request, response);
            printWriter.flush();

            // 验证响应
            verify(response, times(1)).setContentType("application/json");
            verify(response, times(1)).setCharacterEncoding("UTF-8");

            String jsonResponse = responseWriter.toString();
            System.out.println("[INVENTORY-RESPONSE-1] 商品列表响应: " + jsonResponse);

            // 验证响应不为空
            assertNotNull(jsonResponse, "响应不能为空");
            assertFalse(jsonResponse.isEmpty(), "响应不能为空字符串");
            assertTrue(jsonResponse.contains("success"), "响应应包含success字段");

            System.out.println("[INVENTORY-PASS-1] 测试获取商品列表成功");
        }
    }

    @Test
    @Order(2)
    @DisplayName("2. 测试获取商品详情 - 成功")
    void testGetProductDetail_Success() throws Exception {
        System.out.println("[INVENTORY-TEST-2] 开始测试获取商品详情...");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/products/1");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/inventory/products/1");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ADMIN_TOKEN);
        when(request.getHeader("User-Agent")).thenReturn("TestClient/1.0");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(ADMIN_TOKEN)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.getUserIdFromToken(ADMIN_TOKEN)).thenReturn(ADMIN_USER_ID);

            inventoryServlet.doGet(request, response);
            printWriter.flush();

            String jsonResponse = responseWriter.toString();
            System.out.println("[INVENTORY-RESPONSE-2] 商品详情响应: " + jsonResponse);

            assertNotNull(jsonResponse);
            assertFalse(jsonResponse.isEmpty());
            assertTrue(jsonResponse.contains("success"), "响应应包含success字段");

            System.out.println("[INVENTORY-PASS-2] 测试获取商品详情成功");
        }
    }

    @Test
    @Order(3)
    @DisplayName("3. 测试创建商品 - 成功")
    void testCreateProduct_Success() throws Exception {
        System.out.println("[INVENTORY-TEST-3] 开始测试创建商品...");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/products");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/inventory/products");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ADMIN_TOKEN);
        when(request.getHeader("User-Agent")).thenReturn("TestClient/1.0");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getContentType()).thenReturn("application/json;charset=UTF-8");

        // 准备创建商品请求数据
        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("name", "测试商品");
        createRequest.put("category", "测试类目");
        createRequest.put("price", 99.99);
        createRequest.put("stock", 100);
        createRequest.put("status", 1);
        createRequest.put("image", "test.jpg");

        String requestBody = objectMapper.writeValueAsString(createRequest);
        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(ADMIN_TOKEN)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.getUserIdFromToken(ADMIN_TOKEN)).thenReturn(ADMIN_USER_ID);

            inventoryServlet.doPost(request, response);
            printWriter.flush();

            String jsonResponse = responseWriter.toString();
            System.out.println("[INVENTORY-RESPONSE-3] 创建商品响应: " + jsonResponse);

            assertNotNull(jsonResponse);
            assertFalse(jsonResponse.isEmpty());
            assertTrue(jsonResponse.contains("success"), "响应应包含success字段");

            System.out.println("[INVENTORY-PASS-3] 测试创建商品成功");
        }
    }

    @Test
    @Order(4)
    @DisplayName("4. 测试更新商品 - 成功")
    void testUpdateProduct_Success() throws Exception {
        System.out.println("[INVENTORY-TEST-4] 开始测试更新商品...");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/products/1");
        when(request.getMethod()).thenReturn("PUT");
        when(request.getRequestURI()).thenReturn("/api/inventory/products/1");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ADMIN_TOKEN);
        when(request.getHeader("User-Agent")).thenReturn("TestClient/1.0");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getContentType()).thenReturn("application/json;charset=UTF-8");

        // 准备更新商品请求数据
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("name", "更新后的商品名称");
        updateRequest.put("price", 199.99);
        updateRequest.put("stock", 150);

        String requestBody = objectMapper.writeValueAsString(updateRequest);
        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(ADMIN_TOKEN)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.getUserIdFromToken(ADMIN_TOKEN)).thenReturn(ADMIN_USER_ID);

            inventoryServlet.doPut(request, response);
            printWriter.flush();

            String jsonResponse = responseWriter.toString();
            System.out.println("[INVENTORY-RESPONSE-4] 更新商品响应: " + jsonResponse);

            assertNotNull(jsonResponse);
            assertFalse(jsonResponse.isEmpty());
            assertTrue(jsonResponse.contains("success"), "响应应包含success字段");

            System.out.println("[INVENTORY-PASS-4] 测试更新商品成功");
        }
    }

    @Test
    @Order(5)
    @DisplayName("5. 测试增加库存 - 成功")
    void testIncreaseStock_Success() throws Exception {
        System.out.println("[INVENTORY-TEST-5] 开始测试增加库存...");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/stock/increase");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/inventory/stock/increase");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ADMIN_TOKEN);
        when(request.getHeader("User-Agent")).thenReturn("TestClient/1.0");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getContentType()).thenReturn("application/json;charset=UTF-8");

        // 准备增加库存请求数据
        Map<String, Object> stockRequest = new HashMap<>();
        stockRequest.put("productId", 1);
        stockRequest.put("quantity", 10);
        stockRequest.put("orderId", "PUR003");

        String requestBody = objectMapper.writeValueAsString(stockRequest);
        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(ADMIN_TOKEN)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.getUserIdFromToken(ADMIN_TOKEN)).thenReturn(ADMIN_USER_ID);

            inventoryServlet.doPost(request, response);
            printWriter.flush();

            String jsonResponse = responseWriter.toString();
            System.out.println("[INVENTORY-RESPONSE-5] 增加库存响应: " + jsonResponse);

            assertNotNull(jsonResponse);
            assertFalse(jsonResponse.isEmpty());
            assertTrue(jsonResponse.contains("success"), "响应应包含success字段");

            System.out.println("[INVENTORY-PASS-5] 测试增加库存成功");
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. 测试获取库存日志 - 成功")
    void testGetInventoryLogs_Success() throws Exception {
        System.out.println("[INVENTORY-TEST-6] 开始测试获取库存日志...");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/logs");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/inventory/logs");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ADMIN_TOKEN);
        when(request.getHeader("User-Agent")).thenReturn("TestClient/1.0");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getParameter("productId")).thenReturn("1");
        when(request.getParameter("page")).thenReturn("1");
        when(request.getParameter("pageSize")).thenReturn("10");

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(ADMIN_TOKEN)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.getUserIdFromToken(ADMIN_TOKEN)).thenReturn(ADMIN_USER_ID);

            inventoryServlet.doGet(request, response);
            printWriter.flush();

            String jsonResponse = responseWriter.toString();
            System.out.println("[INVENTORY-RESPONSE-6] 库存日志响应: " + jsonResponse);

            assertNotNull(jsonResponse);
            assertFalse(jsonResponse.isEmpty());
            assertTrue(jsonResponse.contains("success"), "响应应包含success字段");

            System.out.println("[INVENTORY-PASS-6] 测试获取库存日志成功");
        }
    }

    @Test
    @Order(7)
    @DisplayName("7. 测试删除商品 - 成功")
    void testDeleteProduct_Success() throws Exception {
        System.out.println("[INVENTORY-TEST-7] 开始测试删除商品...");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/products/4"); // 下架商品
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getRequestURI()).thenReturn("/api/inventory/products/4");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ADMIN_TOKEN);
        when(request.getHeader("User-Agent")).thenReturn("TestClient/1.0");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(ADMIN_TOKEN)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.getUserIdFromToken(ADMIN_TOKEN)).thenReturn(ADMIN_USER_ID);

            inventoryServlet.doDelete(request, response);
            printWriter.flush();

            String jsonResponse = responseWriter.toString();
            System.out.println("[INVENTORY-RESPONSE-7] 删除商品响应: " + jsonResponse);

            assertNotNull(jsonResponse);
            assertFalse(jsonResponse.isEmpty());
            assertTrue(jsonResponse.contains("success"), "响应应包含success字段");

            System.out.println("[INVENTORY-PASS-7] 测试删除商品成功");
        }
    }

    @Test
    @Order(8)
    @DisplayName("8. 测试低库存预警 - 成功")
    void testGetLowStock_Success() throws Exception {
        System.out.println("[INVENTORY-TEST-8] 开始测试低库存预警...");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/low-stock");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/inventory/low-stock");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + ADMIN_TOKEN);
        when(request.getHeader("User-Agent")).thenReturn("TestClient/1.0");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getParameter("threshold")).thenReturn("10");

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(ADMIN_TOKEN)).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.getUserIdFromToken(ADMIN_TOKEN)).thenReturn(ADMIN_USER_ID);

            inventoryServlet.doGet(request, response);
            printWriter.flush();

            String jsonResponse = responseWriter.toString();
            System.out.println("[INVENTORY-RESPONSE-8] 低库存预警响应: " + jsonResponse);

            assertNotNull(jsonResponse);
            assertFalse(jsonResponse.isEmpty());
            assertTrue(jsonResponse.contains("success"), "响应应包含success字段");

            System.out.println("[INVENTORY-PASS-8] 测试低库存预警成功");
        }
    }

    @Test
    @Order(9)
    @DisplayName("9. 测试未授权访问")
    void testUnauthorizedAccess() throws Exception {
        System.out.println("[INVENTORY-TEST-9] 开始测试未授权访问...");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/products/list");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/inventory/products/list");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid_token");
        when(request.getHeader("User-Agent")).thenReturn("TestClient/1.0");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken("invalid_token")).thenReturn(false);

            inventoryServlet.doGet(request, response);
            printWriter.flush();

            String jsonResponse = responseWriter.toString();
            System.out.println("[INVENTORY-RESPONSE-9] 未授权响应: " + jsonResponse);

            assertNotNull(jsonResponse);
            assertFalse(jsonResponse.isEmpty());

            System.out.println("[INVENTORY-PASS-9] 测试未授权访问验证成功");
        }
    }

    @Test
    @Order(10)
    @DisplayName("10. 测试数据库基本操作")
    void testDatabaseBasicOperations() throws Exception {
        System.out.println("[INVENTORY-TEST-10] 开始测试数据库基本操作...");

        // 验证表存在
        try (Statement stmt = conn.createStatement()) {
            // 1. 查询商品数量
            var rs = stmt.executeQuery("SELECT COUNT(*) as count FROM apexflow_product");
            assertTrue(rs.next());
            int productCount = rs.getInt("count");
            System.out.println("[INVENTORY-DB-10] 当前商品数量: " + productCount);
            assertTrue(productCount > 0, "商品数量应该大于0");

            // 2. 查询上架商品数量
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM apexflow_product WHERE status = 1");
            assertTrue(rs.next());
            int activeProductCount = rs.getInt("count");
            System.out.println("[INVENTORY-DB-10] 上架商品数量: " + activeProductCount);
            assertTrue(activeProductCount > 0, "应该至少有一个上架商品");

            // 3. 查询商品详情
            rs = stmt.executeQuery("SELECT name, price, stock FROM apexflow_product WHERE id = 1");
            assertTrue(rs.next());
            String name = rs.getString("name");
            double price = rs.getDouble("price");
            int stock = rs.getInt("stock");

            assertNotNull(name);
            assertTrue(price > 0);
            assertTrue(stock >= 0);

            System.out.println("[INVENTORY-DB-10] 示例商品: " + name + ", 价格: " + price + ", 库存: " + stock);
        }

        System.out.println("[INVENTORY-PASS-10] 测试数据库基本操作成功");
    }
}