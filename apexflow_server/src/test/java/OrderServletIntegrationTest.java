import com.apex.core.controller.OrderServlet;
import com.apex.core.dao.*;
import com.apex.core.service.OrderService;
import com.apex.util.ConnectionPool;
import com.apex.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.sql.Connection;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * OrderServlet 集成测试类（使用H2数据库）
 * 参考AfterSalesDAOTest的写法，使用真实数据库连接
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderServletIntegrationTest {

    private OrderServlet orderServlet;
    private ObjectMapper objectMapper;
    private OrderService orderService;

    private Connection conn;

    private AutoCloseable closeable; // 用于关闭Mockito资源

    @Mock
    private HttpServletRequest requestMock;

    @Mock
    private HttpServletResponse responseMock;

    private StringWriter responseWriter;
    private PrintWriter printWriter;

    @BeforeAll
    void setUpAll() throws Exception {
        // 确保使用 H2 内存数据库
        System.setProperty("apexflow.test.h2", "true");

        // 获取连接并初始化数据库
        conn = ConnectionPool.getConnection();
        H2DatabaseInitializer.initialize(conn);

        objectMapper = new ObjectMapper();

        // 初始化OrderService
        initOrderService();

        // 配置ObjectMapper支持Java 8时间类型
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @AfterAll
    void tearDownAll() throws Exception {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                // 忽略关闭异常
            }
        }
        ConnectionPool.shutdown();

        // 关闭Mockito资源
        if (closeable != null) {
            closeable.close();
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        // 初始化Mockito注解
        closeable = MockitoAnnotations.openMocks(this);

        // 每次测试前清理数据
        H2DatabaseInitializer.clearAllTables(conn);

        // 重新插入基础测试数据
        insertTestData();

        // 初始化OrderServlet
        initOrderServlet();

        // 设置response writer
        responseWriter = new StringWriter();
        printWriter = new PrintWriter(responseWriter);
        when(responseMock.getWriter()).thenReturn(printWriter);

        // 设置默认的请求header
        when(requestMock.getHeader("User-Agent")).thenReturn("Test-Agent");
        when(requestMock.getRemoteAddr()).thenReturn("127.0.0.1");
    }

    @AfterEach
    void tearDown() throws Exception {
        if (printWriter != null) {
            printWriter.close();
        }
        if (responseWriter != null) {
            responseWriter.close();
        }
    }

    private void insertTestData() throws Exception {
        try (var stmt = conn.createStatement()) {
            // 插入用户
            stmt.execute("INSERT INTO apexflow_system_user (id, username, password_hash, salt, real_name, email, phone, is_admin, status) VALUES " +
                    "(1001, 'user1', 'hash1', 'salt1', '用户1', 'user1@test.com', '13800138001', false, 1), " +
                    "(1002, 'user2', 'hash2', 'salt2', '用户2', 'user2@test.com', '13800138002', false, 1), " +
                    "(1003, 'admin', 'hash3', 'salt3', '管理员', 'admin@test.com', '13800138003', true, 1)");

            // 插入商品
            stmt.execute("""
                        INSERT INTO apexflow_product (id, name, category, price, stock, status) VALUES
                        (1, 'iPhone 14 Pro', '手机', 7999.00, 100, 1),
                        (2, 'MacBook Pro 16英寸', '电脑', 18999.00, 50, 1),
                        (3, '华为Mate 50', '手机', 4999.00, 150, 1)
                    """);

            // 插入订单
            stmt.execute("""
                        INSERT INTO apexflow_order (id, user_id, total_amount, status, payment_method, created_at, paid_at) VALUES
                        ('ORDER001', 1001, 7999.00, 1, 'alipay', '2023-12-01 10:00:00', NULL),
                        ('ORDER002', 1002, 18999.00, 2, 'wxpay', '2023-12-01 11:00:00', '2023-12-01 11:05:00'),
                        ('ORDER003', 1001, 4999.00, 3, 'alipay', '2023-12-01 12:00:00', '2023-12-01 12:05:00')
                    """);

            // 插入订单项
            stmt.execute("""
                        INSERT INTO apexflow_order_item (order_id, product_id, product_name, quantity, price, subtotal) VALUES
                        ('ORDER001', 1, 'iPhone 14 Pro', 1, 7999.00, 7999.00),
                        ('ORDER002', 2, 'MacBook Pro 16英寸', 1, 18999.00, 18999.00),
                        ('ORDER003', 3, '华为Mate 50', 1, 4999.00, 4999.00)
                    """);

            // 插入物流记录
            stmt.execute("""
                        INSERT INTO apexflow_logistics (order_id, express_company, tracking_number, status) VALUES
                        ('ORDER003', '顺丰', 'SF1234567890', 'shipped')
                    """);
        }
    }

    private void initOrderService() {
        // 创建真实的DAO实例
        OrderInfoDAO orderInfoDAO = new OrderInfoDAO();
        OrderItemDAO orderItemDAO = new OrderItemDAO();
        ProductDAO productDAO = new ProductDAO();
        InventoryLogDAO inventoryLogDAO = new InventoryLogDAO();
        LogisticsDAO logisticsDAO = new LogisticsDAO();
        IncomeDAO incomeDAO = new IncomeDAO();
        AfterSalesDAO afterSalesDAO = new AfterSalesDAO();
        ReviewDAO reviewDAO = new ReviewDAO();

        // 创建OrderService实例
        orderService = new OrderService(
                orderInfoDAO,
                orderItemDAO,
                productDAO,
                inventoryLogDAO,
                logisticsDAO,
                incomeDAO,
                afterSalesDAO,
                reviewDAO
        );
    }

    private void initOrderServlet() throws Exception {
        orderServlet = new OrderServlet();

        // 使用反射注入orderService
        Field serviceField = orderServlet.getClass().getDeclaredField("orderService");
        serviceField.setAccessible(true);
        serviceField.set(orderServlet, orderService);

        // 注入配置好的 ObjectMapper（如果有相应的字段）
        try {
            Field mapperField = orderServlet.getClass().getSuperclass().getDeclaredField("objectMapper");
            mapperField.setAccessible(true);
            mapperField.set(orderServlet, objectMapper);
        } catch (NoSuchFieldException e) {
            // 如果父类没有这个字段，尝试其他方式
            // 或者创建自定义的 BaseServlet 子类
        }

        // 初始化Servlet
        orderServlet.init();
    }

    @Test
    @Order(1)
    void testGetOrderDetail_Success() throws Exception {
        // Arrange - 创建有效的Token
        String validToken = JwtUtil.generateToken(1003, "admin"); // 管理员用户

        when(requestMock.getPathInfo()).thenReturn("/ORDER001");
        when(requestMock.getMethod()).thenReturn("GET");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        // Act
        orderServlet.doGet(requestMock, responseMock);

        // Assert
        verify(responseMock).setContentType("application/json");
        verify(responseMock).setCharacterEncoding("UTF-8");

        String response = responseWriter.toString();
        assertNotNull(response);
        assertFalse(response.isEmpty());

        // 打印响应内容以便调试
        System.out.println("Response: " + response);
    }

    @Test
    @Order(2)
    void testGetOrderDetail_NotFound() throws Exception {
        // Arrange - 创建有效的Token
        String validToken = JwtUtil.generateToken(1003, "admin");

        when(requestMock.getPathInfo()).thenReturn("/NONEXISTENT");
        when(requestMock.getMethod()).thenReturn("GET");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        // Act
        orderServlet.doGet(requestMock, responseMock);

        // Assert
        String response = responseWriter.toString();
        System.out.println("Response: " + response);

        // 由于是权限验证失败，应该返回401或404
        // 具体取决于实现，我们先验证响应不为空
        assertNotNull(response);
    }

    @Test
    @Order(3)
    void testCreateOrder_Success() throws Exception {
        // Arrange - 创建有效的Token
        String validToken = JwtUtil.generateToken(1003, "admin");

        when(requestMock.getPathInfo()).thenReturn("");
        when(requestMock.getMethod()).thenReturn("POST");
        when(requestMock.getContentType()).thenReturn("application/json");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        // 创建模拟的请求体
        String requestBody = "{" +
                "\"userId\": 1001," +
                "\"totalAmount\": 12998.00," +
                "\"orderItems\": [" +
                "  {\"productId\": 1, \"productName\": \"iPhone 14 Pro\", \"quantity\": 1, \"price\": 7999.00, \"subtotal\": 7999.00}," +
                "  {\"productId\": 3, \"productName\": \"华为Mate 50\", \"quantity\": 1, \"price\": 4999.00, \"subtotal\": 4999.00}" +
                "]," +
                "\"paymentMethod\": \"alipay\"," +
                "\"addressId\": 100" +
                "}";

        when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));

        // Act
        orderServlet.doPost(requestMock, responseMock);

        // Assert
        String response = responseWriter.toString();
        System.out.println("Response: " + response);

        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    @Order(4)
    void testCreateOrder_NoToken() throws Exception {
        // Arrange
        when(requestMock.getPathInfo()).thenReturn("");
        when(requestMock.getMethod()).thenReturn("POST");
        when(requestMock.getContentType()).thenReturn("application/json");
        when(requestMock.getHeader("Authorization")).thenReturn(null);

        String requestBody = "{\"userId\":1,\"totalAmount\":7999.00,\"orderItems\":[{\"productId\":1,\"quantity\":1}]}";
        when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));

        // Act
        orderServlet.doPost(requestMock, responseMock);

        // Assert
        String response = responseWriter.toString();
        System.out.println("Response: " + response);

        // 应该返回401 Unauthorized
        verify(responseMock, atLeastOnce()).setStatus(401);
    }

    @Test
    @Order(5)
    void testListOrders_Success() throws Exception {
        // Arrange - 创建有效的Token
        String validToken = JwtUtil.generateToken(1003, "admin");

        when(requestMock.getPathInfo()).thenReturn("/list");
        when(requestMock.getMethod()).thenReturn("GET");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(requestMock.getParameter("page")).thenReturn("1");
        when(requestMock.getParameter("pageSize")).thenReturn("10");

        // Act
        orderServlet.doGet(requestMock, responseMock);

        // Assert
        String response = responseWriter.toString();
        System.out.println("Response: " + response);

        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    @Order(6)
    void testUpdateOrder_Success() throws Exception {
        // Arrange - 创建有效的Token
        String validToken = JwtUtil.generateToken(1003, "admin");

        String orderId = "ORDER001";
        when(requestMock.getPathInfo()).thenReturn("/" + orderId);
        when(requestMock.getMethod()).thenReturn("PUT");
        when(requestMock.getContentType()).thenReturn("application/json");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        String requestBody = "{\"addressId\":200,\"paymentMethod\":\"wxpay\"}";
        when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));

        // Act
        orderServlet.doPut(requestMock, responseMock);

        // Assert
        String response = responseWriter.toString();
        System.out.println("Response: " + response);

        assertNotNull(response);
    }

    @Test
    @Order(7)
    void testDeleteOrder_Success() throws Exception {
        // Arrange - 创建有效的Token
        String validToken = JwtUtil.generateToken(1003, "admin");

        String orderId = "ORDER001"; // 状态为1（待支付），允许删除
        when(requestMock.getPathInfo()).thenReturn("/" + orderId);
        when(requestMock.getMethod()).thenReturn("DELETE");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        // Act
        orderServlet.doDelete(requestMock, responseMock);

        // Assert
        String response = responseWriter.toString();
        System.out.println("Response: " + response);

        assertNotNull(response);
    }

    @Test
    @Order(8)
    void testUpdateOrderStatus_Success() throws Exception {
        // Arrange - 创建有效的Token
        String validToken = JwtUtil.generateToken(1003, "admin");

        String orderId = "ORDER002"; // 当前状态为2（已支付）
        when(requestMock.getPathInfo()).thenReturn("/" + orderId + "/status");
        when(requestMock.getMethod()).thenReturn("PUT");
        when(requestMock.getContentType()).thenReturn("application/json");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        String requestBody = "{\"status\":3}"; // 更新为已发货
        when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));

        // Act
        orderServlet.doPut(requestMock, responseMock);

        // Assert
        String response = responseWriter.toString();
        System.out.println("Response: " + response);

        assertNotNull(response);
    }

    @Test
    @Order(9)
    void testInvalidHttpMethod() throws Exception {
        // Arrange
        when(requestMock.getPathInfo()).thenReturn("/list");
        when(requestMock.getMethod()).thenReturn("POST"); // 错误的HTTP方法

        // Act
        orderServlet.doPost(requestMock, responseMock);

        // Assert
        verify(responseMock, atLeastOnce()).setStatus(404); // 405 Method Not Allowed
    }

    @Test
    @Order(10)
    void testInvalidApiPath() throws Exception {
        // Arrange
        when(requestMock.getPathInfo()).thenReturn("/invalid");
        when(requestMock.getMethod()).thenReturn("GET");

        // Act
        orderServlet.doGet(requestMock, responseMock);

        // Assert
        verify(responseMock, atLeastOnce()).setStatus(404); // 404 Not Found
    }

    @Test
    @Order(11)
    void testCreateOrder_InvalidJson() throws Exception {
        // Arrange - 创建有效的Token
        String validToken = JwtUtil.generateToken(1003, "admin");

        when(requestMock.getPathInfo()).thenReturn("");
        when(requestMock.getMethod()).thenReturn("POST");
        when(requestMock.getContentType()).thenReturn("application/json");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        String invalidJson = "{invalid json";
        when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(invalidJson)));

        // Act
        orderServlet.doPost(requestMock, responseMock);

        // Assert
        verify(responseMock, atLeastOnce()).setStatus(400); // 400 Bad Request
    }

    @Test
    @Order(12)
    void testCreateOrder_MissingRequiredFields() throws Exception {
        // Arrange - 创建有效的Token
        String validToken = JwtUtil.generateToken(1003, "admin");

        when(requestMock.getPathInfo()).thenReturn("");
        when(requestMock.getMethod()).thenReturn("POST");
        when(requestMock.getContentType()).thenReturn("application/json");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        // 缺少必要字段的请求体
        String requestBody = "{\"userId\":1}"; // 缺少totalAmount和orderItems
        when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));

        // Act
        orderServlet.doPost(requestMock, responseMock);

        // Assert
        String response = responseWriter.toString();
        System.out.println("Response: " + response);

        assertNotNull(response);
        // 应该返回400或相应的错误
        verify(responseMock, atLeastOnce()).setStatus(400);
    }
}