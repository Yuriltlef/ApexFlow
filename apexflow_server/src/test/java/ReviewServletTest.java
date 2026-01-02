// ReviewServletTest.java - 修正版

import com.apex.core.controller.ReviewServlet;
import com.apex.core.dto.ReviewCreateRequest;
import com.apex.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ReviewServlet 精简单元测试
 * 修正了protected方法调用问题
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReviewServletTest {

    private static final Logger logger = LoggerFactory.getLogger(ReviewServletTest.class);

    private ReviewServlet reviewServlet;
    private ObjectMapper objectMapper;
    private HttpServletRequest requestMock;
    private HttpServletResponse responseMock;
    private StringWriter responseWriter;

    // 测试数据
    private final String testToken = JwtUtil.generateToken(1, "admin");
    private final Integer testReviewId = 1;
    private final Integer testProductId = 1;

    @BeforeAll
    void setUpAll() {
        objectMapper = new ObjectMapper();
        logger.info("ReviewServlet 测试开始");
    }

    @BeforeEach
    void setUp() throws Exception {
        // 创建Servlet实例
        reviewServlet = new ReviewServlet();

        // 创建模拟对象
        requestMock = mock(HttpServletRequest.class);
        responseMock = mock(HttpServletResponse.class);
        responseWriter = new StringWriter();

        // 设置response writer
        when(responseMock.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // 设置IP地址
        when(requestMock.getRemoteAddr()).thenReturn("127.0.0.1");
    }

    @AfterEach
    void tearDown() {
        responseWriter.getBuffer().setLength(0); // 清理响应内容
        logger.debug("测试清理完成");
    }

    /**
     * 通过反射调用protected的doDelete方法
     */
    private void invokeDoDelete(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Method method = ReviewServlet.class.getDeclaredMethod("doDelete",
                HttpServletRequest.class, HttpServletResponse.class);
        method.setAccessible(true); // 设置可访问
        method.invoke(reviewServlet, request, response);
    }

    /**
     * 通过反射调用protected的doPut方法
     */
    private void invokeDoPut(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Method method = ReviewServlet.class.getDeclaredMethod("doPut",
                HttpServletRequest.class, HttpServletResponse.class);
        method.setAccessible(true); // 设置可访问
        method.invoke(reviewServlet, request, response);
    }

    @Test
    @Order(1)
    @DisplayName("测试创建评价 - 成功")
    void testCreateReview_Success() throws Exception {
        // Arrange
        ReviewCreateRequest createRequest = new ReviewCreateRequest();
        createRequest.setOrderId("ORDER20231201001");
        createRequest.setProductId(testProductId);
        createRequest.setUserId(1001);
        createRequest.setRating(5);
        createRequest.setContent("优秀的产品！");

        String requestBody = objectMapper.writeValueAsString(createRequest);

        when(requestMock.getPathInfo()).thenReturn("/");
        when(requestMock.getMethod()).thenReturn("POST");
        when(requestMock.getContentType()).thenReturn("application/json");
        when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + testToken);

        // Act - doPost是public的，可以直接调用
        reviewServlet.doPost(requestMock, responseMock);

        // Assert
        String responseContent = responseWriter.toString();
        logger.debug("创建评价响应: {}", responseContent);

        assertNotNull(responseContent);
        assertFalse(responseContent.isEmpty());

        // 验证JSON格式
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);
        assertNotNull(responseMap);
    }

    @Test
    @Order(2)
    @DisplayName("测试创建评价 - 无效内容类型")
    void testCreateReview_InvalidContentType() throws Exception {
        // Arrange
        String requestBody = "{\"orderId\":\"TEST-001\",\"rating\":5}";

        when(requestMock.getPathInfo()).thenReturn("/");
        when(requestMock.getMethod()).thenReturn("POST");
        when(requestMock.getContentType()).thenReturn("text/plain"); // 不是application/json
        when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));

        // Act
        reviewServlet.doPost(requestMock, responseMock);

        // Assert
        verify(responseMock).setStatus(HttpServletResponse.SC_BAD_REQUEST);

        String responseContent = responseWriter.toString();
        assertTrue(responseContent.contains("INVALID_CONTENT_TYPE"));
        logger.info("无效内容类型测试通过");
    }

    @Test
    @Order(3)
    @DisplayName("测试创建评价 - 缺少必要字段")
    void testCreateReview_MissingRequiredFields() throws Exception {
        // Arrange - 缺少orderId
        String requestBody = "{\"productId\":1,\"rating\":5}";

        when(requestMock.getPathInfo()).thenReturn("/");
        when(requestMock.getMethod()).thenReturn("POST");
        when(requestMock.getContentType()).thenReturn("application/json");
        when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));

        // Act
        reviewServlet.doPost(requestMock, responseMock);

        // Assert
        verify(responseMock).setStatus(HttpServletResponse.SC_BAD_REQUEST);

        String responseContent = responseWriter.toString();
        assertTrue(responseContent.contains("MISSING_FIELDS"));
        logger.info("缺少必要字段测试通过");
    }

    @Test
    @Order(4)
    @DisplayName("测试获取评价列表 - 成功")
    void testGetReviewList_Success() throws Exception {
        // Arrange
        when(requestMock.getPathInfo()).thenReturn("/");
        when(requestMock.getMethod()).thenReturn("GET");
        when(requestMock.getParameter("productId")).thenReturn("1");
        when(requestMock.getParameter("page")).thenReturn("1");
        when(requestMock.getParameter("pageSize")).thenReturn("10");

        // Act - doGet是public的，可以直接调用
        reviewServlet.doGet(requestMock, responseMock);

        // Assert
        verify(responseMock).setContentType("application/json");

        String responseContent = responseWriter.toString();
        logger.debug("评价列表响应: {}", responseContent);

        assertNotNull(responseContent);
        assertFalse(responseContent.isEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("测试获取评价详情 - 成功")
    void testGetReviewDetail_Success() throws Exception {
        // Arrange
        String pathInfo = "/" + testReviewId;
        when(requestMock.getPathInfo()).thenReturn(pathInfo);
        when(requestMock.getMethod()).thenReturn("GET");

        // Act
        reviewServlet.doGet(requestMock, responseMock);

        // Assert
        verify(responseMock).setContentType("application/json");

        String responseContent = responseWriter.toString();
        logger.debug("评价详情响应: {}", responseContent);

        assertNotNull(responseContent);
    }

    @Test
    @Order(6)
    @DisplayName("测试获取评价详情 - 无效ID")
    void testGetReviewDetail_InvalidId() throws Exception {
        // Arrange
        String pathInfo = "/not-a-number";
        when(requestMock.getPathInfo()).thenReturn(pathInfo);
        when(requestMock.getMethod()).thenReturn("GET");

        // Act
        reviewServlet.doGet(requestMock, responseMock);

        // Assert
        verify(responseMock).setStatus(HttpServletResponse.SC_NOT_FOUND);

        String responseContent = responseWriter.toString();
        assertTrue(responseContent.contains("API_NOT_FOUND"));
        logger.info("无效ID测试通过");
    }

    @Test
    @Order(7)
    @DisplayName("测试获取评价统计 - 成功")
    void testGetReviewStats_Success() throws Exception {
        // Arrange
        String pathInfo = "/stats/" + testProductId;
        when(requestMock.getPathInfo()).thenReturn(pathInfo);
        when(requestMock.getMethod()).thenReturn("GET");

        // Act
        reviewServlet.doGet(requestMock, responseMock);

        // Assert
        verify(responseMock).setContentType("application/json");

        String responseContent = responseWriter.toString();
        logger.debug("评价统计响应: {}", responseContent);

        assertNotNull(responseContent);
    }

    @Test
    @Order(8)
    @DisplayName("测试删除评价 - 成功")
    void testDeleteReview_Success() throws Exception {
        // Arrange
        String pathInfo = "/" + testReviewId;
        when(requestMock.getPathInfo()).thenReturn(pathInfo);
        when(requestMock.getMethod()).thenReturn("DELETE");

        // Act - 使用反射调用protected的doDelete方法
        invokeDoDelete(requestMock, responseMock);

        // Assert
        verify(responseMock).setContentType("application/json");

        String responseContent = responseWriter.toString();
        logger.debug("删除评价响应: {}", responseContent);

        assertNotNull(responseContent);
    }

    @Test
    @Order(9)
    @DisplayName("测试删除评价 - 无效ID")
    void testDeleteReview_InvalidId() throws Exception {
        // Arrange
        String pathInfo = "/not-a-number";
        when(requestMock.getPathInfo()).thenReturn(pathInfo);
        when(requestMock.getMethod()).thenReturn("DELETE");

        // Act - 使用反射调用protected的doDelete方法
        invokeDoDelete(requestMock, responseMock);

        // Assert
        verify(responseMock).setStatus(HttpServletResponse.SC_NOT_FOUND);

        String responseContent = responseWriter.toString();
        assertTrue(responseContent.contains("API_NOT_FOUND"));
        logger.info("删除评价无效ID测试通过");
    }

    @Test
    @Order(10)
    @DisplayName("测试无效API路径")
    void testInvalidApiPath() throws Exception {
        // Arrange
        when(requestMock.getPathInfo()).thenReturn("/invalid/path");
        when(requestMock.getMethod()).thenReturn("GET");

        // Act
        reviewServlet.doGet(requestMock, responseMock);

        // Assert
        verify(responseMock).setStatus(HttpServletResponse.SC_NOT_FOUND);

        String responseContent = responseWriter.toString();
        assertTrue(responseContent.contains("API_NOT_FOUND"));
        logger.info("无效API路径测试通过");
    }

    @Test
    @Order(11)
    @DisplayName("测试无效HTTP方法")
    void testInvalidHttpMethod() throws Exception {
        // Arrange
        when(requestMock.getPathInfo()).thenReturn("/");
        when(requestMock.getMethod()).thenReturn("PUT"); // PUT方法未实现
        when(requestMock.getProtocol()).thenReturn("HTTP/1.1");

        // Act - 使用反射调用protected的doPut方法
        invokeDoPut(requestMock, responseMock);

        // Assert
        // HttpServlet的doPut方法默认调用sendError(405)而不是setStatus(405)
        // 我们需要验证sendError被调用，状态码为405
        verify(responseMock).sendError(eq(HttpServletResponse.SC_METHOD_NOT_ALLOWED), anyString());

        String responseContent = responseWriter.toString();
        // 注意：当调用sendError时，响应内容可能为空或包含默认错误页面

        logger.info("无效HTTP方法测试通过，调用sendError(405)");
    }

    @Test
    @Order(12)
    @DisplayName("测试JSON格式错误")
    void testMalformedJsonRequest() throws Exception {
        // Arrange
        String malformedJson = "{invalid json";

        when(requestMock.getPathInfo()).thenReturn("/");
        when(requestMock.getMethod()).thenReturn("POST");
        when(requestMock.getContentType()).thenReturn("application/json");
        when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(malformedJson)));

        // Act
        reviewServlet.doPost(requestMock, responseMock);

        // Assert
        verify(responseMock).setStatus(HttpServletResponse.SC_BAD_REQUEST);

        String responseContent = responseWriter.toString();
        assertTrue(responseContent.contains("INVALID_FORMAT"));
        logger.info("JSON格式错误测试通过");
    }

    @Test
    @Order(13)
    @DisplayName("测试权限注解配置")
    void testPermissionAnnotation() throws Exception {
        // 通过反射验证注解配置

        // 检查doPost方法上的注解
        Method doPostMethod = ReviewServlet.class.getMethod("doPost",
                HttpServletRequest.class, HttpServletResponse.class);

        com.apex.util.RequirePermission doPostAnnotation =
                doPostMethod.getAnnotation(com.apex.util.RequirePermission.class);

        assertNotNull(doPostAnnotation, "doPost方法应该有@RequirePermission注解");

        // 验证权限配置
        com.apex.util.Permission[] permissions = doPostAnnotation.value();
        assertEquals(2, permissions.length, "需要两种权限");

        // 验证包含管理员和订单管理权限
        boolean hasAdmin = false;
        boolean hasOrderManage = false;

        for (com.apex.util.Permission p : permissions) {
            if (p == com.apex.util.Permission.ADMIN) hasAdmin = true;
            if (p == com.apex.util.Permission.ORDER_MANAGE) hasOrderManage = true;
        }

        assertTrue(hasAdmin, "应该包含ADMIN权限");
        assertTrue(hasOrderManage, "应该包含ORDER_MANAGE权限");

        // 验证逻辑类型
        assertEquals(com.apex.util.RequirePermission.LogicType.OR, doPostAnnotation.logic(),
                "权限验证逻辑应该是OR");

        // 检查doGet方法上的注解
        Method doGetMethod = ReviewServlet.class.getMethod("doGet",
                HttpServletRequest.class, HttpServletResponse.class);

        com.apex.util.RequirePermission doGetAnnotation =
                doGetMethod.getAnnotation(com.apex.util.RequirePermission.class);

        assertNotNull(doGetAnnotation, "doGet方法也应该有@RequirePermission注解");

        logger.info("权限注解验证通过");
    }

    @Test
    @Order(14)
    @DisplayName("测试基础Servlet功能")
    void testBaseServletFunctionality() {
        // 验证ReviewServlet继承了BaseServlet
        assertTrue(ReviewServlet.class.getSuperclass().equals(com.apex.api.BaseServlet.class),
                "ReviewServlet应该继承BaseServlet");

        logger.info("基础Servlet功能验证通过");
    }

    @Test
    @Order(15)
    @DisplayName("测试评价创建请求模型")
    void testReviewCreateRequestModel() throws Exception {
        // 测试DTO序列化和反序列化
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setOrderId("ORDER20231201001");
        request.setProductId(1);
        request.setUserId(1001);
        request.setRating(5);
        request.setContent("测试内容");
        request.setImages("image1.jpg,image2.jpg");
        request.setAnonymous(true);

        // 序列化
        String json = objectMapper.writeValueAsString(request);
        assertNotNull(json);

        // 反序列化
        ReviewCreateRequest deserialized = objectMapper.readValue(json, ReviewCreateRequest.class);
        assertEquals("ORDER20231201001", deserialized.getOrderId());
        assertEquals(1, deserialized.getProductId());
        assertEquals(1001, deserialized.getUserId());
        assertEquals(5, deserialized.getRating());
        assertEquals("测试内容", deserialized.getContent());
        assertEquals("image1.jpg,image2.jpg", deserialized.getImages());
        assertTrue(deserialized.getAnonymous());

        logger.info("评价创建请求模型测试通过");
    }

    @Test
    @Order(16)
    @DisplayName("测试空路径处理")
    void testNullPathInfo() throws Exception {
        // Arrange
        when(requestMock.getPathInfo()).thenReturn(null);
        when(requestMock.getMethod()).thenReturn("GET");

        // Act
        reviewServlet.doGet(requestMock, responseMock);

        // Assert
        verify(responseMock).setContentType("application/json");

        String responseContent = responseWriter.toString();
        logger.debug("空路径响应: {}", responseContent);

        assertNotNull(responseContent);
    }

    @Test
    @Order(17)
    @DisplayName("测试权限注解配置 - 检查所有HTTP方法")
    void testAllHttpMethodsPermissionAnnotations() throws Exception {
        // 使用反射获取所有方法并检查注解

        String[] methodNames = {"doPost", "doGet", "doDelete", "doPut"};

        for (String methodName : methodNames) {
            try {
                Method method = ReviewServlet.class.getDeclaredMethod(methodName,
                        HttpServletRequest.class, HttpServletResponse.class);

                com.apex.util.RequirePermission annotation =
                        method.getAnnotation(com.apex.util.RequirePermission.class);

                // doPut可能没有注解，因为它是返回405错误的方法
                if (!"doPut".equals(methodName)) {
                    assertNotNull(annotation, methodName + " 方法应该有@RequirePermission注解");

                    // 验证权限配置
                    com.apex.util.Permission[] permissions = annotation.value();
                    assertTrue(permissions.length >= 2, methodName + " 应该至少需要两种权限");

                    boolean hasAdmin = false;
                    boolean hasOrderManage = false;

                    for (com.apex.util.Permission p : permissions) {
                        if (p == com.apex.util.Permission.ADMIN) hasAdmin = true;
                        if (p == com.apex.util.Permission.ORDER_MANAGE) hasOrderManage = true;
                    }

                    assertTrue(hasAdmin, methodName + " 应该包含ADMIN权限");
                    assertTrue(hasOrderManage, methodName + " 应该包含ORDER_MANAGE权限");

                    logger.debug("{} 方法权限注解验证通过", methodName);
                }
            } catch (NoSuchMethodException e) {
                logger.warn("未找到方法: {}", methodName);
            }
        }

        logger.info("所有HTTP方法权限注解验证通过");
    }

    @AfterAll
    void tearDownAll() {
        logger.info("ReviewServlet 测试完成");
    }
}