import com.apex.core.controller.IncomeServlet;
import com.apex.core.dto.CreateIncomeRequest;
import com.apex.core.dto.UpdateIncomeRequest;
import com.apex.core.service.IncomeService;
import com.apex.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * IncomeServlet 单元测试类
 * 测试财务收支相关的API接口
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IncomeServletTest {

    private IncomeServlet incomeServlet;
    private ObjectMapper objectMapper;
    private IncomeService incomeServiceMock;
    private HttpServletRequest requestMock;
    private HttpServletResponse responseMock;
    private StringWriter responseWriter;

    @BeforeAll
    void setUpAll() {
        objectMapper = new ObjectMapper();
    }

    @BeforeEach
    void setUp() throws Exception {
        incomeServlet = new IncomeServlet();
        incomeServiceMock = mock(IncomeService.class);
        requestMock = mock(HttpServletRequest.class);
        responseMock = mock(HttpServletResponse.class);
        responseWriter = new StringWriter();

        // 使用反射注入mock的Service
        Field serviceField = incomeServlet.getClass().getDeclaredField("incomeService");
        serviceField.setAccessible(true);
        serviceField.set(incomeServlet, incomeServiceMock);

        // 设置response writer
        when(responseMock.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // 设置默认的请求头
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer valid_token");
        when(requestMock.getContentType()).thenReturn("application/json");
    }

    @Test
    @Order(1)
    void testCreateIncome_Success() throws Exception {
        // Arrange
        String validToken = JwtUtil.generateToken(1, "admin");
        CreateIncomeRequest createRequest = new CreateIncomeRequest();
        createRequest.setOrderId("ORDER20240101001");
        createRequest.setType("income");
        createRequest.setAmount(new BigDecimal("2999.00"));
        createRequest.setPaymentMethod("alipay");
        createRequest.setStatus(2);
        createRequest.setRemark("测试商品销售款");

        String requestBody = objectMapper.writeValueAsString(createRequest);

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("success", true);
        mockResult.put("message", "财务记录创建成功");

        Map<String, Object> mockData = new HashMap<>();
        mockData.put("id", 1);
        mockData.put("orderId", "ORDER20240101001");
        mockData.put("type", "income");
        mockData.put("amount", 2999.00); // 注意：这里是Double类型
        mockData.put("paymentMethod", "alipay");
        mockData.put("status", 2);
        mockData.put("remark", "测试商品销售款");

        mockResult.put("data", mockData);

        when(requestMock.getPathInfo()).thenReturn("");
        when(requestMock.getMethod()).thenReturn("POST");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));
        when(incomeServiceMock.createIncome(eq(validToken), any(CreateIncomeRequest.class))).thenReturn(mockResult);

        // Act
        incomeServlet.doPost(requestMock, responseMock);

        // Assert
        String responseContent = responseWriter.toString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);

        assertTrue((Boolean) responseMap.get("success"));
        assertEquals("财务记录创建成功", responseMap.get("message"));

        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
        assertEquals(1, data.get("id"));
        assertEquals("ORDER20240101001", data.get("orderId"));
        assertEquals("income", data.get("type"));

        // 修复类型转换：使用Number来比较
        Object amountObj = data.get("amount");
        assertTrue(amountObj instanceof Number);
        assertEquals(2999.00, ((Number) amountObj).doubleValue(), 0.001);

        assertEquals("alipay", data.get("paymentMethod"));
        assertEquals(2, data.get("status"));
        assertEquals("测试商品销售款", data.get("remark"));
    }

    @Test
    @Order(2)
    void testCreateIncome_ValidationError() throws Exception {
        // Arrange
        String validToken = JwtUtil.generateToken(1, "admin");
        CreateIncomeRequest createRequest = new CreateIncomeRequest();
        // 不设置必填字段，触发验证错误

        String requestBody = objectMapper.writeValueAsString(createRequest);

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("success", false);
        mockResult.put("message", "输入数据验证失败");

        Map<String, String> errors = new HashMap<>();
        errors.put("orderId", "订单号不能为空");
        errors.put("type", "财务类型不能为空");
        errors.put("amount", "金额不能为空");

        mockResult.put("errors", errors);

        when(requestMock.getPathInfo()).thenReturn("");
        when(requestMock.getMethod()).thenReturn("POST");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));
        when(incomeServiceMock.createIncome(eq(validToken), any(CreateIncomeRequest.class))).thenReturn(mockResult);

        // Act
        incomeServlet.doPost(requestMock, responseMock);

        // Assert
        String responseContent = responseWriter.toString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);

        assertFalse((Boolean) responseMap.get("success"));
        assertEquals("输入数据验证失败", responseMap.get("message"));

        Map<String, String> responseErrors = (Map<String, String>) responseMap.get("data");
        assertNotNull(responseErrors);
        assertEquals("订单号不能为空", responseErrors.get("orderId"));
    }

    @Test
    @Order(3)
    void testCreateIncome_MissingToken() throws Exception {
        // Arrange
        when(requestMock.getPathInfo()).thenReturn("");
        when(requestMock.getMethod()).thenReturn("POST");
        when(requestMock.getHeader("Authorization")).thenReturn(null);
        when(requestMock.getContentType()).thenReturn("application/json");

        // Act
        incomeServlet.doPost(requestMock, responseMock);

        // Assert
        verify(responseMock).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    @Order(4)
    void testCreateIncome_InvalidContentType() throws Exception {
        // Arrange
        when(requestMock.getPathInfo()).thenReturn("");
        when(requestMock.getMethod()).thenReturn("POST");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer valid_token");
        when(requestMock.getContentType()).thenReturn("text/plain");

        // Act
        incomeServlet.doPost(requestMock, responseMock);

        // Assert
        verify(responseMock).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    @Order(5)
    void testGetIncomeDetail_Success() throws Exception {
        // Arrange
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("success", true);
        mockResult.put("message", "财务记录详情获取成功");

        Map<String, Object> mockData = new HashMap<>();
        mockData.put("id", 1);
        mockData.put("orderId", "ORDER20231201001");
        mockData.put("type", "income");
        mockData.put("amount", 7999.00); // Double类型
        mockData.put("paymentMethod", "alipay");
        mockData.put("status", 2);
        mockData.put("remark", "iPhone 14 Pro销售款");

        mockResult.put("data", mockData);

        when(requestMock.getPathInfo()).thenReturn("/1");
        when(requestMock.getMethod()).thenReturn("GET");
        when(incomeServiceMock.getIncomeDetail(1)).thenReturn(mockResult);

        // Act
        incomeServlet.doGet(requestMock, responseMock);

        // Assert
        String responseContent = responseWriter.toString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);

        assertTrue((Boolean) responseMap.get("success"));
        assertEquals("财务记录详情获取成功", responseMap.get("message"));

        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
        assertEquals(1, data.get("id"));
        assertEquals("ORDER20231201001", data.get("orderId"));

        Object amountObj = data.get("amount");
        assertTrue(amountObj instanceof Number);
        assertEquals(7999.00, ((Number) amountObj).doubleValue(), 0.001);
    }

    @Test
    @Order(6)
    void testGetIncomeDetail_NotFound() throws Exception {
        // Arrange
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("success", false);
        mockResult.put("message", "财务记录不存在");

        when(requestMock.getPathInfo()).thenReturn("/999");
        when(requestMock.getMethod()).thenReturn("GET");
        when(incomeServiceMock.getIncomeDetail(999)).thenReturn(mockResult);

        // Act
        incomeServlet.doGet(requestMock, responseMock);

        // Assert
        String responseContent = responseWriter.toString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);

        assertFalse((Boolean) responseMap.get("success"));
        assertEquals("财务记录不存在", responseMap.get("message"));
    }

    @Test
    @Order(7)
    void testListIncomes_Success() throws Exception {
        // Arrange
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("success", true);
        mockResult.put("message", "财务记录列表获取成功");

        // 创建模拟的Income列表
        List<Map<String, Object>> mockIncomes = new ArrayList<>();

        Map<String, Object> income1 = new HashMap<>();
        income1.put("id", 1);
        income1.put("orderId", "ORDER20231201001");
        income1.put("type", "income");
        income1.put("amount", 7999.00);
        mockIncomes.add(income1);

        Map<String, Object> income2 = new HashMap<>();
        income2.put("id", 2);
        income2.put("orderId", "ORDER20231201002");
        income2.put("type", "income");
        income2.put("amount", 18999.00);
        mockIncomes.add(income2);

        Map<String, Object> mockData = new HashMap<>();
        mockData.put("incomes", mockIncomes);
        mockData.put("currentPage", 1);
        mockData.put("pageSize", 20);
        mockData.put("totalIncome", 26998.00);
        mockData.put("totalRefund", 0.00);
        mockData.put("netIncome", 26998.00);

        mockResult.put("data", mockData);

        when(requestMock.getPathInfo()).thenReturn("/list");
        when(requestMock.getMethod()).thenReturn("GET");
        when(requestMock.getParameter("page")).thenReturn("1");
        when(requestMock.getParameter("pageSize")).thenReturn("20");
        when(incomeServiceMock.listIncomes(null, null, 1, 20)).thenReturn(mockResult);

        // Act
        incomeServlet.doGet(requestMock, responseMock);

        // Assert
        String responseContent = responseWriter.toString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);

        assertTrue((Boolean) responseMap.get("success"));
        assertEquals("财务记录列表获取成功", responseMap.get("message"));

        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");

        List<Map<String, Object>> incomes = (List<Map<String, Object>>) data.get("incomes");
        assertEquals(2, incomes.size());

        assertEquals(1, incomes.get(0).get("id"));
        assertEquals("ORDER20231201001", incomes.get(0).get("orderId"));

        Object amountObj1 = incomes.get(0).get("amount");
        assertTrue(amountObj1 instanceof Number);
        assertEquals(7999.00, ((Number) amountObj1).doubleValue(), 0.001);

        assertEquals(1, data.get("currentPage"));
        assertEquals(20, data.get("pageSize"));

        Object totalIncomeObj = data.get("totalIncome");
        assertTrue(totalIncomeObj instanceof Number);
        assertEquals(26998.00, ((Number) totalIncomeObj).doubleValue(), 0.001);

        Object totalRefundObj = data.get("totalRefund");
        assertTrue(totalRefundObj instanceof Number);
        assertEquals(0.00, ((Number) totalRefundObj).doubleValue(), 0.001);

        Object netIncomeObj = data.get("netIncome");
        assertTrue(netIncomeObj instanceof Number);
        assertEquals(26998.00, ((Number) netIncomeObj).doubleValue(), 0.001);
    }

    @ParameterizedTest
    @CsvSource({
            "income,2,1,10",
            "refund,,1,20",
            ",1,1,50",
            ",,2,30"
    })
    @Order(8)
    void testListIncomes_WithParameters(String type, String status, String page, String pageSize) throws Exception {
        // Arrange
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("success", true);
        mockResult.put("message", "财务记录列表获取成功");

        mockResult.put("data", new HashMap<>());

        when(requestMock.getPathInfo()).thenReturn("/list");
        when(requestMock.getMethod()).thenReturn("GET");
        when(requestMock.getParameter("type")).thenReturn(type);
        when(requestMock.getParameter("status")).thenReturn(status);
        when(requestMock.getParameter("page")).thenReturn(page);
        when(requestMock.getParameter("pageSize")).thenReturn(pageSize);

        Integer statusInt = status != null && !status.isEmpty() ? Integer.parseInt(status) : null;
        Integer pageInt = Integer.parseInt(page);
        Integer pageSizeInt = Integer.parseInt(pageSize);

        when(incomeServiceMock.listIncomes(type, statusInt, pageInt, pageSizeInt)).thenReturn(mockResult);

        // Act
        incomeServlet.doGet(requestMock, responseMock);

        // Assert
        String responseContent = responseWriter.toString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);

        assertTrue((Boolean) responseMap.get("success"));
    }

    @Test
    @Order(9)
    void testUpdateIncome_Success() throws Exception {
        // Arrange
        String validToken = JwtUtil.generateToken(1, "admin");
        UpdateIncomeRequest updateRequest = new UpdateIncomeRequest();
        updateRequest.setAmount(new BigDecimal("8500.00"));
        updateRequest.setStatus(2);
        updateRequest.setRemark("更新后的备注");

        String requestBody = objectMapper.writeValueAsString(updateRequest);

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("success", true);
        mockResult.put("message", "财务记录更新成功");

        Map<String, Object> mockData = new HashMap<>();
        mockData.put("id", 1);
        mockData.put("orderId", "ORDER20231201001");
        mockData.put("type", "income");
        mockData.put("amount", 8500.00);
        mockData.put("paymentMethod", "alipay");
        mockData.put("status", 2);
        mockData.put("remark", "更新后的备注");

        mockResult.put("data", mockData);

        when(requestMock.getPathInfo()).thenReturn("/1");
        when(requestMock.getMethod()).thenReturn("PUT");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));
        when(incomeServiceMock.updateIncome(eq(validToken), eq(1), any(UpdateIncomeRequest.class))).thenReturn(mockResult);

        // Act
        incomeServlet.doPut(requestMock, responseMock);

        // Assert
        String responseContent = responseWriter.toString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);

        assertTrue((Boolean) responseMap.get("success"));
        assertEquals("财务记录更新成功", responseMap.get("message"));

        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
        assertEquals(1, data.get("id"));

        Object amountObj = data.get("amount");
        assertTrue(amountObj instanceof Number);
        assertEquals(8500.00, ((Number) amountObj).doubleValue(), 0.001);

        assertEquals(2, data.get("status"));
        assertEquals("更新后的备注", data.get("remark"));
    }

    @Test
    @Order(10)
    void testDeleteIncome_Success() throws Exception {
        // Arrange
        String validToken = JwtUtil.generateToken(1, "admin");

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("success", true);
        mockResult.put("message", "财务记录删除成功");

        when(requestMock.getPathInfo()).thenReturn("/1");
        when(requestMock.getMethod()).thenReturn("DELETE");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(incomeServiceMock.deleteIncome(eq(validToken), eq(1))).thenReturn(mockResult);

        // Act
        incomeServlet.doDelete(requestMock, responseMock);

        // Assert
        String responseContent = responseWriter.toString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);

        assertTrue((Boolean) responseMap.get("success"));
        assertEquals("财务记录删除成功", responseMap.get("message"));
    }

    @Test
    @Order(11)
    void testUpdateIncomeStatus_Success() throws Exception {
        // Arrange
        String validToken = JwtUtil.generateToken(1, "admin");
        String requestBody = "{\"status\": 2}";

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("success", true);
        mockResult.put("message", "财务状态更新成功");

        when(requestMock.getPathInfo()).thenReturn("/1/status");
        when(requestMock.getMethod()).thenReturn("PUT");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));
        when(incomeServiceMock.updateIncomeStatus(eq(validToken), eq(1), eq(2))).thenReturn(mockResult);

        // Act
        incomeServlet.doPut(requestMock, responseMock);

        // Assert
        String responseContent = responseWriter.toString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);

        assertTrue((Boolean) responseMap.get("success"));
        assertEquals("财务状态更新成功", responseMap.get("message"));
    }

    @Test
    @Order(12)
    void testGetStatistics_Success() throws Exception {
        // Arrange
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("success", true);
        mockResult.put("message", "财务统计获取成功");

        Map<String, Object> mockData = new HashMap<>();
        mockData.put("totalIncome", 150000.00);
        mockData.put("totalRefund", 5000.00);
        mockData.put("netIncome", 145000.00);

        mockResult.put("data", mockData);

        when(requestMock.getPathInfo()).thenReturn("/statistics");
        when(requestMock.getMethod()).thenReturn("GET");
        when(incomeServiceMock.getStatistics()).thenReturn(mockResult);

        // Act
        incomeServlet.doGet(requestMock, responseMock);

        // Assert
        String responseContent = responseWriter.toString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);

        assertTrue((Boolean) responseMap.get("success"));
        assertEquals("财务统计获取成功", responseMap.get("message"));

        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");

        Object totalIncomeObj = data.get("totalIncome");
        assertTrue(totalIncomeObj instanceof Number);
        assertEquals(150000.00, ((Number) totalIncomeObj).doubleValue(), 0.001);

        Object totalRefundObj = data.get("totalRefund");
        assertTrue(totalRefundObj instanceof Number);
        assertEquals(5000.00, ((Number) totalRefundObj).doubleValue(), 0.001);

        Object netIncomeObj = data.get("netIncome");
        assertTrue(netIncomeObj instanceof Number);
        assertEquals(145000.00, ((Number) netIncomeObj).doubleValue(), 0.001);
    }

    @Test
    @Order(13)
    void testGetIncomesByOrderId_Success() throws Exception {
        // Arrange
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("success", true);
        mockResult.put("message", "订单财务记录获取成功");

        List<Map<String, Object>> mockIncomes = new ArrayList<>();

        Map<String, Object> income = new HashMap<>();
        income.put("id", 1);
        income.put("orderId", "ORDER20231201001");
        income.put("type", "income");
        income.put("amount", 7999.00);
        mockIncomes.add(income);

        mockResult.put("data", mockIncomes);
        mockResult.put("count", 1);

        when(requestMock.getPathInfo()).thenReturn("/order/ORDER20231201001");
        when(requestMock.getMethod()).thenReturn("GET");
        when(incomeServiceMock.getIncomesByOrderId("ORDER20231201001")).thenReturn(mockResult);

        // Act
        incomeServlet.doGet(requestMock, responseMock);

        // Assert
        String responseContent = responseWriter.toString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);

        assertTrue((Boolean) responseMap.get("success"));
        assertEquals("订单财务记录获取成功", responseMap.get("message"));

        List<Map<String, Object>> data = (List<Map<String, Object>>) responseMap.get("data");
        assertEquals(1, data.size());

        Map<String, Object> incomeData = data.get(0);
        assertEquals(1, incomeData.get("id"));
        assertEquals("ORDER20231201001", incomeData.get("orderId"));

        Object amountObj = incomeData.get("amount");
        assertTrue(amountObj instanceof Number);
        assertEquals(7999.00, ((Number) amountObj).doubleValue(), 0.001);
    }

    @Test
    @Order(14)
    void testGetIncomesByOrderId_NotFound() throws Exception {
        // Arrange
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("success", false);
        mockResult.put("message", "未找到该订单的财务记录");

        when(requestMock.getPathInfo()).thenReturn("/order/INVALID_ORDER");
        when(requestMock.getMethod()).thenReturn("GET");
        when(incomeServiceMock.getIncomesByOrderId("INVALID_ORDER")).thenReturn(mockResult);

        // Act
        incomeServlet.doGet(requestMock, responseMock);

        // Assert
        String responseContent = responseWriter.toString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);

        assertFalse((Boolean) responseMap.get("success"));
        assertEquals("未找到该订单的财务记录", responseMap.get("message"));
    }

    @Test
    @Order(15)
    void testInvalidApiPath() throws Exception {
        // Arrange
        when(requestMock.getPathInfo()).thenReturn("/invalid");
        when(requestMock.getMethod()).thenReturn("GET");

        // Act
        incomeServlet.doGet(requestMock, responseMock);

        // Assert
        verify(responseMock).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    @Order(16)
    void testInvalidHttpMethod() throws Exception {
        // Arrange
        when(requestMock.getPathInfo()).thenReturn("/list");
        when(requestMock.getMethod()).thenReturn("POST");

        // Act
        incomeServlet.doPost(requestMock, responseMock);

        // Assert
        verify(responseMock).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    @Order(17)
    void testInvalidNumberFormat() throws Exception {
        // Arrange
        when(requestMock.getPathInfo()).thenReturn("/abc");
        when(requestMock.getMethod()).thenReturn("GET");

        // Act
        incomeServlet.doGet(requestMock, responseMock);

        // Assert
        verify(responseMock).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    @Order(18)
    void testListIncomes_InvalidNumberParameters() throws Exception {
        // Arrange
        when(requestMock.getPathInfo()).thenReturn("/list");
        when(requestMock.getMethod()).thenReturn("GET");
        when(requestMock.getParameter("page")).thenReturn("invalid");
        when(requestMock.getParameter("pageSize")).thenReturn("invalid");

        // Act
        incomeServlet.doGet(requestMock, responseMock);

        // Assert
        String responseContent = responseWriter.toString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);

        assertFalse((Boolean) responseMap.get("success"));
        assertEquals("查询参数格式错误", responseMap.get("message"));
    }

    @Test
    @Order(19)
    void testUpdateIncomeStatus_MissingStatus() throws Exception {
        // Arrange
        String validToken = JwtUtil.generateToken(1, "admin");
        String requestBody = "{}";

        when(requestMock.getPathInfo()).thenReturn("/1/status");
        when(requestMock.getMethod()).thenReturn("PUT");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));

        // Act
        incomeServlet.doPut(requestMock, responseMock);

        // Assert
        String responseContent = responseWriter.toString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);

        assertFalse((Boolean) responseMap.get("success"));
        assertEquals("状态值不能为空", responseMap.get("message"));
    }

    @Test
    @Order(20)
    void testGetIncomesByOrderId_EmptyOrderId() throws Exception {
        // Arrange
        when(requestMock.getPathInfo()).thenReturn("/order/");
        when(requestMock.getMethod()).thenReturn("GET");

        // Act
        incomeServlet.doGet(requestMock, responseMock);

        // Assert
        String responseContent = responseWriter.toString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);

        assertFalse((Boolean) responseMap.get("success"));
        assertEquals("订单号不能为空", responseMap.get("message"));
    }

    @Test
    @Order(21)
    void testServiceLayerExceptionHandling() throws Exception {
        // Arrange
        when(requestMock.getPathInfo()).thenReturn("/list");
        when(requestMock.getMethod()).thenReturn("GET");
        when(incomeServiceMock.listIncomes(null, null, 1, 20)).thenThrow(new RuntimeException("Database connection failed"));

        // Act
        incomeServlet.doGet(requestMock, responseMock);

        // Assert
        String responseContent = responseWriter.toString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);

        assertFalse((Boolean) responseMap.get("success"));
        assertEquals("查询财务记录列表失败", responseMap.get("message"));
    }
}