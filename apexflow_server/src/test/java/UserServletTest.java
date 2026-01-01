import com.apex.core.controller.UserServlet;
import com.apex.core.dto.UpdateProfileRequest;
import com.apex.core.dao.UserDAO;
import com.apex.core.model.SystemUser;
import com.apex.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * UserServlet 单元测试类
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServletTest {

    private UserServlet userServlet;
    private ObjectMapper objectMapper;
    private UserDAO userDAOMock;
    private HttpServletRequest requestMock;
    private HttpServletResponse responseMock;
    private StringWriter responseWriter;

    @BeforeAll
    void setUpAll() {
        objectMapper = new ObjectMapper();
    }

    @BeforeEach
    void setUp() throws Exception {
        userServlet = new UserServlet();
        userDAOMock = mock(UserDAO.class);
        requestMock = mock(HttpServletRequest.class);
        responseMock = mock(HttpServletResponse.class);
        responseWriter = new StringWriter();

        // 使用反射注入mock的DAO
        Field daoField = userServlet.getClass().getDeclaredField("userService");
        daoField.setAccessible(true);
        Object userService = daoField.get(userServlet);

        Field serviceDaoField = userService.getClass().getDeclaredField("userDAO");
        serviceDaoField.setAccessible(true);
        serviceDaoField.set(userService, userDAOMock);

        // 设置response writer
        when(responseMock.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    @Order(1)
    void testGetPermissions_Success() throws Exception {
        // Arrange
        String validToken = JwtUtil.generateToken(1, "admin");

        SystemUser mockUser = new SystemUser();
        mockUser.setId(1);
        mockUser.setAdmin(true);
        mockUser.setCanManageOrder(true);
        mockUser.setCanManageLogistics(true);
        mockUser.setCanManageAfterSales(false);
        mockUser.setCanManageReview(true);
        mockUser.setCanManageInventory(false);
        mockUser.setCanManageIncome(true);

        when(requestMock.getPathInfo()).thenReturn("/permissions");
        when(requestMock.getMethod()).thenReturn("GET");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(userDAOMock.getPermissions(1)).thenReturn(mockUser);

        // Act
        userServlet.doGet(requestMock, responseMock);

        // Assert
        String responseContent = responseWriter.toString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);

        assertTrue((Boolean) responseMap.get("success"));
        assertEquals("获取权限成功", responseMap.get("message"));

        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
        assertNotNull(data);
        assertTrue((Boolean) data.get("isAdmin"));
        assertTrue((Boolean) data.get("canManageOrder"));
        assertTrue((Boolean) data.get("canManageLogistics"));
        assertFalse((Boolean) data.get("canManageAfterSales"));
        assertTrue((Boolean) data.get("canManageReview"));
        assertFalse((Boolean) data.get("canManageInventory"));
        assertTrue((Boolean) data.get("canManageIncome"));
    }

    @Test
    @Order(2)
    void testGetPermissions_MissingToken() throws Exception {
        // Arrange
        when(requestMock.getPathInfo()).thenReturn("/permissions");
        when(requestMock.getMethod()).thenReturn("GET");
        when(requestMock.getHeader("Authorization")).thenReturn(null);

        // Act
        userServlet.doGet(requestMock, responseMock);

        // Assert
        verify(responseMock).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    @Order(3)
    void testGetPermissions_InvalidToken() throws Exception {
        // Arrange
        when(requestMock.getPathInfo()).thenReturn("/permissions");
        when(requestMock.getMethod()).thenReturn("GET");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer invalid_token");

        // Act
        userServlet.doGet(requestMock, responseMock);

        // Assert
        String responseContent = responseWriter.toString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);

        assertFalse((Boolean) responseMap.get("success"));
    }

    @Test
    @Order(4)
    void testGetPermissions_UserNotFound() throws Exception {
        // Arrange
        String validToken = JwtUtil.generateToken(999, "nonexistent");

        when(requestMock.getPathInfo()).thenReturn("/permissions");
        when(requestMock.getMethod()).thenReturn("GET");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(userDAOMock.getPermissions(999)).thenReturn(null);

        // Act
        userServlet.doGet(requestMock, responseMock);

        // Assert
        String responseContent = responseWriter.toString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);

        assertFalse((Boolean) responseMap.get("success"));
    }

    @Test
    @Order(5)
    void testUpdateProfile_Success() throws Exception {
        // Arrange
        String validToken = JwtUtil.generateToken(1, "admin");
        UpdateProfileRequest updateRequest = new UpdateProfileRequest();
        updateRequest.setRealName("新姓名");
        updateRequest.setEmail("new@email.com");
        updateRequest.setPhone("13800138000");

        String requestBody = objectMapper.writeValueAsString(updateRequest);

        SystemUser mockUser = new SystemUser();
        mockUser.setId(1);
        mockUser.setRealName("原姓名");
        mockUser.setEmail("old@email.com");
        mockUser.setPhone("13900139000");

        when(requestMock.getPathInfo()).thenReturn("/profile");
        when(requestMock.getMethod()).thenReturn("PUT");
        when(requestMock.getContentType()).thenReturn("application/json");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));
        when(userDAOMock.findById(1)).thenReturn(mockUser);
        when(userDAOMock.update(any(SystemUser.class))).thenReturn(true);

        // Act
        userServlet.doPut(requestMock, responseMock);

        // Assert
        String responseContent = responseWriter.toString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);

        assertTrue((Boolean) responseMap.get("success"));
        assertEquals("个人信息更新成功", responseMap.get("message"));

        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
        assertEquals(1, data.get("id"));
        assertEquals("新姓名", data.get("realName"));
        assertEquals("new@email.com", data.get("email"));
        assertEquals("13800138000", data.get("phone"));
    }

    @ParameterizedTest
    @CsvSource({
            "新姓名,,",           // 只更新姓名
            ",new@email.com,",   // 只更新邮箱
            ",,13800138000",     // 只更新手机号
            "新姓名,new@email.com,", // 更新姓名和邮箱
            "新姓名,,13800138000",   // 更新姓名和手机号
            ",new@email.com,13800138000" // 更新邮箱和手机号
    })
    @Order(6)
    void testUpdateProfile_PartialUpdate(String realName, String email, String phone) throws Exception {
        // Arrange
        String validToken = JwtUtil.generateToken(1, "admin");
        UpdateProfileRequest updateRequest = new UpdateProfileRequest();
        if (!"null".equals(realName) && realName != null) updateRequest.setRealName(realName);
        if (!"null".equals(email) && email != null) updateRequest.setEmail(email);
        if (!"null".equals(phone) && phone != null) updateRequest.setPhone(phone);

        String requestBody = objectMapper.writeValueAsString(updateRequest);

        SystemUser mockUser = new SystemUser();
        mockUser.setId(1);
        mockUser.setRealName("原姓名");
        mockUser.setEmail("old@email.com");
        mockUser.setPhone("13900139000");

        when(requestMock.getPathInfo()).thenReturn("/profile");
        when(requestMock.getMethod()).thenReturn("PUT");
        when(requestMock.getContentType()).thenReturn("application/json");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));
        when(userDAOMock.findById(1)).thenReturn(mockUser);
        when(userDAOMock.update(any(SystemUser.class))).thenReturn(true);

        // Act
        userServlet.doPut(requestMock, responseMock);

        // Assert
        String responseContent = responseWriter.toString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);

        assertTrue((Boolean) responseMap.get("success"));
    }

    @Test
    @Order(7)
    void testUpdateProfile_NoFieldsToUpdate() throws Exception {
        // Arrange
        String validToken = JwtUtil.generateToken(1, "admin");
        UpdateProfileRequest updateRequest = new UpdateProfileRequest(); // 所有字段为空

        String requestBody = objectMapper.writeValueAsString(updateRequest);

        when(requestMock.getPathInfo()).thenReturn("/profile");
        when(requestMock.getMethod()).thenReturn("PUT");
        when(requestMock.getContentType()).thenReturn("application/json");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));

        // Act
        userServlet.doPut(requestMock, responseMock);

        // Assert
        String responseContent = responseWriter.toString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);

        assertFalse((Boolean) responseMap.get("success"));
        assertEquals("至少需要提供一个更新字段", responseMap.get("message"));
    }

    @Test
    @Order(8)
    void testUpdateProfile_InvalidEmail() throws Exception {
        // Arrange
        String validToken = JwtUtil.generateToken(1, "admin");
        UpdateProfileRequest updateRequest = new UpdateProfileRequest();
        updateRequest.setEmail("invalid-email");

        String requestBody = objectMapper.writeValueAsString(updateRequest);

        when(requestMock.getPathInfo()).thenReturn("/profile");
        when(requestMock.getMethod()).thenReturn("PUT");
        when(requestMock.getContentType()).thenReturn("application/json");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));

        // Act
        userServlet.doPut(requestMock, responseMock);

        // Assert
        verify(responseMock).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    @Order(9)
    void testUpdateProfile_InvalidPhone() throws Exception {
        // Arrange
        String validToken = JwtUtil.generateToken(1, "admin");
        UpdateProfileRequest updateRequest = new UpdateProfileRequest();
        updateRequest.setPhone("12345678901"); // 无效的手机号

        String requestBody = objectMapper.writeValueAsString(updateRequest);

        when(requestMock.getPathInfo()).thenReturn("/profile");
        when(requestMock.getMethod()).thenReturn("PUT");
        when(requestMock.getContentType()).thenReturn("application/json");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));

        // Act
        userServlet.doPut(requestMock, responseMock);

        // Assert
        verify(responseMock).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    @Order(10)
    void testUpdateProfile_DatabaseUpdateFailed() throws Exception {
        // Arrange
        String validToken = JwtUtil.generateToken(1, "admin");
        UpdateProfileRequest updateRequest = new UpdateProfileRequest();
        updateRequest.setRealName("新姓名");

        String requestBody = objectMapper.writeValueAsString(updateRequest);

        SystemUser mockUser = new SystemUser();
        mockUser.setId(1);
        mockUser.setRealName("原姓名");

        when(requestMock.getPathInfo()).thenReturn("/profile");
        when(requestMock.getMethod()).thenReturn("PUT");
        when(requestMock.getContentType()).thenReturn("application/json");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));
        when(userDAOMock.findById(1)).thenReturn(mockUser);
        when(userDAOMock.update(any(SystemUser.class))).thenReturn(false);

        // Act
        userServlet.doPut(requestMock, responseMock);

        // Assert
        String responseContent = responseWriter.toString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);

        assertFalse((Boolean) responseMap.get("success"));
        assertEquals("更新个人信息失败", responseMap.get("message"));
    }

    @Test
    @Order(11)
    void testInvalidHttpMethod() throws Exception {
        // Arrange
        when(requestMock.getPathInfo()).thenReturn("/permissions");
        when(requestMock.getMethod()).thenReturn("POST");

        // Act
        userServlet.doPost(requestMock, responseMock);

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
        userServlet.doGet(requestMock, responseMock);

        // Assert
        verify(responseMock).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    @Order(13)
    void testUpdateProfile_UserNotFound() throws Exception {
        // Arrange
        String validToken = JwtUtil.generateToken(999, "nonexistent");
        UpdateProfileRequest updateRequest = new UpdateProfileRequest();
        updateRequest.setRealName("新姓名");

        String requestBody = objectMapper.writeValueAsString(updateRequest);

        when(requestMock.getPathInfo()).thenReturn("/profile");
        when(requestMock.getMethod()).thenReturn("PUT");
        when(requestMock.getContentType()).thenReturn("application/json");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));
        when(userDAOMock.findById(999)).thenReturn(null);

        // Act
        userServlet.doPut(requestMock, responseMock);

        // Assert
        String responseContent = responseWriter.toString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);

        assertFalse((Boolean) responseMap.get("success"));
        assertEquals("用户不存在或已被禁用", responseMap.get("message"));
    }
}