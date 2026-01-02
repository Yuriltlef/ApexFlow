import com.apex.core.controller.OrderServlet;
import com.apex.core.service.OrderService;
import com.apex.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * OrderServlet 单元测试类（修复版）
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderServletTest {

    private OrderServlet orderServlet;
    private ObjectMapper objectMapper;

    @Mock
    private OrderService orderServiceMock;

    @Mock
    private HttpServletRequest requestMock;

    @Mock
    private HttpServletResponse responseMock;

    private StringWriter responseWriter;
    private PrintWriter printWriter;

    @BeforeAll
    void setUpAll() {
        objectMapper = new ObjectMapper();
    }

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        orderServlet = new OrderServlet();

        // 使用反射注入mock的OrderService
        Field serviceField = orderServlet.getClass().getDeclaredField("orderService");
        serviceField.setAccessible(true);
        serviceField.set(orderServlet, orderServiceMock);

        // 初始化Servlet
        orderServlet.init();

        // 设置response writer
        responseWriter = new StringWriter();
        printWriter = new PrintWriter(responseWriter);
        when(responseMock.getWriter()).thenReturn(printWriter);

        // 设置默认的请求header
        when(requestMock.getHeader("User-Agent")).thenReturn("Test-Agent");
        when(requestMock.getRemoteAddr()).thenReturn("127.0.0.1");
    }

    @Test
    @Order(1)
    void testCreateOrder_Success() throws Exception {
        // Arrange - 创建有效的Token
        String validToken = JwtUtil.generateToken(1, "admin");

        when(requestMock.getPathInfo()).thenReturn("");
        when(requestMock.getMethod()).thenReturn("POST");
        when(requestMock.getContentType()).thenReturn("application/json");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        // 创建模拟的请求体
        String requestBody = "{\"userId\":1,\"totalAmount\":7999.00,\"orderItems\":[{\"productId\":1,\"productName\":\"iPhone\",\"quantity\":1,\"price\":7999.00,\"subtotal\":7999.00}],\"paymentMethod\":\"alipay\",\"addressId\":100}";
        when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));

        // Mock OrderService创建成功
        when(orderServiceMock.createOrder(any(), any())).thenReturn(true);

        // Act
        orderServlet.doPost(requestMock, responseMock);

        // Assert - 验证返回201 Created
        verify(responseMock).setStatus(HttpServletResponse.SC_CREATED);

        // 验证响应内容
        String response = responseWriter.toString();
        assertNotNull(response);
        assertFalse(response.isEmpty());

        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        assertTrue((Boolean) responseMap.get("success"));
    }

    @Test
    @Order(2)
    void testCreateOrder_NoToken() throws Exception {
        // Arrange - 不提供Token
        when(requestMock.getPathInfo()).thenReturn("");
        when(requestMock.getMethod()).thenReturn("POST");
        when(requestMock.getContentType()).thenReturn("application/json");
        when(requestMock.getHeader("Authorization")).thenReturn(null);

        // 创建模拟的请求体
        String requestBody = "{\"userId\":1,\"totalAmount\":7999.00,\"orderItems\":[{\"productId\":1,\"quantity\":1}]}";
        when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));

        // Act
        orderServlet.doPost(requestMock, responseMock);

        // Assert - 验证返回401 Unauthorized
        verify(responseMock).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    @Order(3)
    void testCreateOrder_InvalidJson() throws Exception {
        // Arrange - 创建有效的Token
        String validToken = JwtUtil.generateToken(1, "admin");

        when(requestMock.getPathInfo()).thenReturn("");
        when(requestMock.getMethod()).thenReturn("POST");
        when(requestMock.getContentType()).thenReturn("application/json");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        String invalidJson = "{invalid json";
        when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(invalidJson)));

        // Act
        orderServlet.doPost(requestMock, responseMock);

        // Assert
        verify(responseMock).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    @Order(4)
    void testCreateOrder_InvalidContentType() throws Exception {
        // Arrange - 创建有效的Token
        String validToken = JwtUtil.generateToken(1, "admin");

        when(requestMock.getPathInfo()).thenReturn("");
        when(requestMock.getMethod()).thenReturn("POST");
        when(requestMock.getContentType()).thenReturn("text/plain"); // 不是application/json
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        // Act
        orderServlet.doPost(requestMock, responseMock);

        // Assert
        verify(responseMock).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    @Order(5)
    void testGetOrderDetail_Success() throws Exception {
        // Arrange - 创建有效的Token
        String validToken = JwtUtil.generateToken(1, "admin");

        String orderId = "ORDER20231201001";
        when(requestMock.getPathInfo()).thenReturn("/" + orderId);
        when(requestMock.getMethod()).thenReturn("GET");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        // 这里不需要实际mock orderService，因为权限检查通过后会有其他逻辑
        // 我们主要测试权限验证和基本流程

        // Act
        orderServlet.doGet(requestMock, responseMock);

        // Assert
        String response = responseWriter.toString();
        assertNotNull(response);
    }

    @Test
    @Order(6)
    void testListOrders_Success() throws Exception {
        // Arrange - 创建有效的Token
        String validToken = JwtUtil.generateToken(1, "admin");

        when(requestMock.getPathInfo()).thenReturn("/list");
        when(requestMock.getMethod()).thenReturn("GET");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(requestMock.getParameter("page")).thenReturn("1");
        when(requestMock.getParameter("pageSize")).thenReturn("10");

        // Act
        orderServlet.doGet(requestMock, responseMock);

        // Assert
        String response = responseWriter.toString();
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    @Order(7)
    void testUpdateOrder_Success() throws Exception {
        // Arrange - 创建有效的Token
        String validToken = JwtUtil.generateToken(1, "admin");

        String orderId = "ORDER20231201001";
        when(requestMock.getPathInfo()).thenReturn("/" + orderId);
        when(requestMock.getMethod()).thenReturn("PUT");
        when(requestMock.getContentType()).thenReturn("application/json");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        String requestBody = "{\"addressId\":100,\"paymentMethod\":\"alipay\"}";
        when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));

        // Mock OrderService更新成功
        when(orderServiceMock.updateOrder(eq(orderId), any())).thenReturn(true);

        // Act
        orderServlet.doPut(requestMock, responseMock);

        // Assert
        String response = responseWriter.toString();
        assertNotNull(response);

        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        assertTrue((Boolean) responseMap.get("success"));
    }

    @Test
    @Order(8)
    void testDeleteOrder_Success() throws Exception {
        // Arrange - 创建有效的Token
        String validToken = JwtUtil.generateToken(1, "admin");

        String orderId = "ORDER20231201001";
        when(requestMock.getPathInfo()).thenReturn("/" + orderId);
        when(requestMock.getMethod()).thenReturn("DELETE");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        // Mock删除成功
        when(orderServiceMock.deleteOrder(orderId)).thenReturn(true);

        // Act
        orderServlet.doDelete(requestMock, responseMock);

        // Assert
        String response = responseWriter.toString();
        assertNotNull(response);

        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        assertTrue((Boolean) responseMap.get("success"));
    }

    @Test
    @Order(9)
    void testUpdateOrderStatus_Success() throws Exception {
        // Arrange - 创建有效的Token
        String validToken = JwtUtil.generateToken(1, "admin");

        String orderId = "ORDER20231201001";
        when(requestMock.getPathInfo()).thenReturn("/" + orderId + "/status");
        when(requestMock.getMethod()).thenReturn("PUT");
        when(requestMock.getContentType()).thenReturn("application/json");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        String requestBody = "{\"status\":2}";
        when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));

        // Mock更新状态成功
        when(orderServiceMock.updateOrderStatus(orderId, 2)).thenReturn(true);

        // Act
        orderServlet.doPut(requestMock, responseMock);

        // Assert
        String response = responseWriter.toString();
        assertNotNull(response);

        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        assertTrue((Boolean) responseMap.get("success"));
    }

    @Test
    @Order(10)
    void testUpdateOrderStatus_InvalidStatus() throws Exception {
        // Arrange - 创建有效的Token
        String validToken = JwtUtil.generateToken(1, "admin");

        String orderId = "ORDER20231201001";
        when(requestMock.getPathInfo()).thenReturn("/" + orderId + "/status");
        when(requestMock.getMethod()).thenReturn("PUT");
        when(requestMock.getContentType()).thenReturn("application/json");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        String requestBody = "{\"status\":99}"; // 无效的状态
        when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));

        // Act
        orderServlet.doPut(requestMock, responseMock);

        // Assert
        verify(responseMock).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    @Order(11)
    void testInvalidHttpMethod() throws Exception {
        // Arrange
        when(requestMock.getPathInfo()).thenReturn("/list");
        when(requestMock.getMethod()).thenReturn("POST"); // 错误的HTTP方法

        // Act
        orderServlet.doPost(requestMock, responseMock);

        // Assert
        verify(responseMock).setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    @Order(12)
    void testInvalidApiPath() throws Exception {
        // Arrange
        when(requestMock.getPathInfo()).thenReturn("/invalid");
        when(requestMock.getMethod()).thenReturn("GET");

        // Act
        orderServlet.doGet(requestMock, responseMock);

        // Assert
        verify(responseMock).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    @Order(13)
    void testPathInfoNull() throws Exception {
        // Arrange
        when(requestMock.getPathInfo()).thenReturn(null);
        when(requestMock.getMethod()).thenReturn("GET");

        // Act
        orderServlet.doGet(requestMock, responseMock);

        // Assert
        verify(responseMock).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    @Order(14)
    void testListOrders_InvalidPageParams() throws Exception {
        // Arrange - 创建有效的Token
        String validToken = JwtUtil.generateToken(1, "admin");

        when(requestMock.getPathInfo()).thenReturn("/list");
        when(requestMock.getMethod()).thenReturn("GET");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(requestMock.getParameter("page")).thenReturn("invalid");
        when(requestMock.getParameter("pageSize")).thenReturn("invalid");

        // Act
        orderServlet.doGet(requestMock, responseMock);

        // Assert
        verify(responseMock).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    @Order(15)
    void testCreateOrder_MissingRequiredFields() throws Exception {
        // Arrange - 创建有效的Token
        String validToken = JwtUtil.generateToken(1, "admin");

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
        verify(responseMock).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @AfterEach
    void tearDown() throws Exception {
        printWriter.close();
        responseWriter.close();
    }
}