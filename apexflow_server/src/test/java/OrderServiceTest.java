import com.apex.core.dao.*;
import com.apex.core.dto.OrderWithItemsResponse;
import com.apex.core.model.*;
import com.apex.core.service.OrderService;
import com.apex.util.ConnectionPool;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import com.apex.core.dto.OrderDetail;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OrderManagerService 单元测试类
 * 测试订单管理服务的核心功能：创建、修改、删除订单等
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderServiceTest {

    private OrderService orderService;
    private Connection conn;

    // Mocked DAOs
    private IOrderInfoDAO orderInfoDAO;
    private IOrderItemDAO orderItemDAO;
    private IProductDAO productDAO;
    private IInventoryLogDAO inventoryLogDAO;
    private ILogisticsDAO logisticsDAO;
    private IIncomeDAO incomeDAO;
    private IAfterSalesDAO afterSalesDAO;
    private IReviewDAO reviewDAO;

    // 测试数据
    private OrderInfo testOrder;
    private List<OrderItem> testOrderItems;
    private Product testProduct;

    @BeforeAll
    void setUpAll() throws Exception {
        // 设置H2内存数据库
        System.setProperty("apexflow.test.h2", "true");

        // 获取连接并初始化数据库
        conn = ConnectionPool.getConnection();
        H2DatabaseInitializer.initialize(conn);

        // 创建Mock DAOs
        orderInfoDAO = Mockito.mock(IOrderInfoDAO.class);
        orderItemDAO = Mockito.mock(IOrderItemDAO.class);
        productDAO = Mockito.mock(IProductDAO.class);
        inventoryLogDAO = Mockito.mock(IInventoryLogDAO.class);
        logisticsDAO = Mockito.mock(ILogisticsDAO.class);
        incomeDAO = Mockito.mock(IIncomeDAO.class);
        afterSalesDAO = Mockito.mock(IAfterSalesDAO.class);
        reviewDAO = Mockito.mock(IReviewDAO.class);

        // 创建OrderManagerService实例
        orderService = new OrderService(
                orderInfoDAO, orderItemDAO, productDAO, inventoryLogDAO,
                logisticsDAO, incomeDAO, afterSalesDAO, reviewDAO
        );

        // 初始化测试数据
        createTestData();
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
    void setUp() {
        // 重置所有Mock
        resetDAOMocks();

        // 重新设置基本的Mock行为
        setupCommonMockBehaviors();
    }

    private void createTestData() {
        // 创建测试订单
        testOrder = new OrderInfo();
        testOrder.setId("ORDER20231201051");
        testOrder.setUserId(1001);
        testOrder.setTotalAmount(new BigDecimal("7999.00"));
        testOrder.setStatus(1); // 待支付
        testOrder.setPaymentMethod("alipay");
        testOrder.setCreatedAt(LocalDateTime.now());

        // 创建测试商品
        testProduct = new Product();
        testProduct.setId(1);
        testProduct.setName("iPhone 14 Pro");
        testProduct.setPrice(new BigDecimal("7999.00"));
        testProduct.setStock(100);
        testProduct.setStatus(1);

        // 创建测试订单项
        testOrderItems = new ArrayList<>();
        OrderItem orderItem = new OrderItem();
        orderItem.setId(1);
        orderItem.setOrderId(testOrder.getId());
        orderItem.setProductId(testProduct.getId());
        orderItem.setProductName(testProduct.getName());
        orderItem.setQuantity(1);
        orderItem.setPrice(testProduct.getPrice());
        orderItem.setSubtotal(testProduct.getPrice());
        testOrderItems.add(orderItem);
    }

    private void resetDAOMocks() {
        reset(orderInfoDAO, orderItemDAO, productDAO, inventoryLogDAO,
                logisticsDAO, incomeDAO, afterSalesDAO, reviewDAO);
    }

    private void setupCommonMockBehaviors() {
        // OrderInfoDAO 默认行为
        when(orderInfoDAO.findById(anyString())).thenReturn(null);
        when(orderInfoDAO.findById(testOrder.getId())).thenReturn(testOrder);
        when(orderInfoDAO.create(any(OrderInfo.class))).thenReturn(true);
        when(orderInfoDAO.update(any(OrderInfo.class))).thenReturn(true);
        when(orderInfoDAO.updateStatus(anyString(), anyInt())).thenReturn(true);
        when(orderInfoDAO.delete(anyString())).thenReturn(true);

        // ProductDAO 默认行为
        when(productDAO.findById(anyInt())).thenReturn(null);
        when(productDAO.findById(testProduct.getId())).thenReturn(testProduct);
        when(productDAO.decreaseStock(anyInt(), anyInt())).thenReturn(true);
        when(productDAO.increaseStock(anyInt(), anyInt())).thenReturn(true);

        // OrderItemDAO 默认行为
        when(orderItemDAO.findByOrderId(anyString())).thenReturn(new ArrayList<>());
        when(orderItemDAO.findByOrderId(testOrder.getId())).thenReturn(testOrderItems);
        when(orderItemDAO.createBatch(anyList())).thenReturn(true);
        when(orderItemDAO.calculateOrderTotal(anyString())).thenReturn(testOrder.getTotalAmount());

        // 其他DAO默认行为
        when(logisticsDAO.create(any(Logistics.class))).thenReturn(true);
        when(logisticsDAO.findByOrderId(anyString())).thenReturn(null);
        when(logisticsDAO.findByOrderId(testOrder.getId())).thenReturn(new Logistics());
        when(inventoryLogDAO.create(any(InventoryLog.class))).thenReturn(true);
        when(incomeDAO.create(any(Income.class))).thenReturn(true);
        when(incomeDAO.findByOrderId(anyString())).thenReturn(new ArrayList<>());
        when(afterSalesDAO.findByOrderId(anyString())).thenReturn(new ArrayList<>());
        when(reviewDAO.findByOrderId(anyString())).thenReturn(null);
    }

    @Test
    @Order(1)
    void testCreateOrder_Success() {
        // Arrange
        testOrder.setStatus(1); // 待支付状态

        // Act
        boolean result = orderService.createOrder(testOrder, testOrderItems);

        // Assert
        assertTrue(result, "创建订单应该成功");

        // 验证各个DAO方法被正确调用
        verify(orderInfoDAO, times(1)).create(testOrder);
        verify(orderItemDAO, times(1)).createBatch(testOrderItems);
        verify(productDAO, times(1)).decreaseStock(testProduct.getId(), 1);
        verify(inventoryLogDAO, times(1)).create(any(InventoryLog.class));
        verify(logisticsDAO, times(1)).create(any(Logistics.class));

        // 对于待支付状态，不应该创建财务记录
        verify(incomeDAO, never()).create(any(Income.class));
    }

    @Test
    @Order(2)
    void testCreateOrder_WithPaidStatus_CreatesIncomeRecord() {
        // Arrange
        testOrder.setStatus(2); // 已支付状态
        testOrder.setPaidAt(LocalDateTime.now());

        // Act
        boolean result = orderService.createOrder(testOrder, testOrderItems);

        // Assert
        assertTrue(result);

        // 验证财务记录被创建
        verify(incomeDAO, times(1)).create(any(Income.class));
    }

    @Test
    @Order(3)
    void testCreateOrder_InvalidParameters_Fails() {
        // Test Case 1: Null order
        boolean result1 = orderService.createOrder(null, testOrderItems);
        assertFalse(result1, "订单为空应该失败");

        // Test Case 2: Empty order items
        boolean result2 = orderService.createOrder(testOrder, new ArrayList<>());
        assertFalse(result2, "订单项为空应该失败");

        // Test Case 3: Null order items
        boolean result3 = orderService.createOrder(testOrder, null);
        assertFalse(result3, "订单项为null应该失败");
    }

    @Test
    @Order(4)
    void testCreateOrder_ProductNotFound_Fails() {
        // Arrange
        when(productDAO.findById(testProduct.getId())).thenReturn(null);

        // Act
        boolean result = orderService.createOrder(testOrder, testOrderItems);

        // Assert
        assertFalse(result, "商品不存在应该失败");
        verify(orderInfoDAO, never()).create(any(OrderInfo.class));
    }

    @Test
    @Order(5)
    void testCreateOrder_InsufficientStock_Fails() {
        // Arrange
        Product lowStockProduct = new Product();
        lowStockProduct.setId(2);
        lowStockProduct.setName("测试商品");
        lowStockProduct.setPrice(new BigDecimal("100.00"));
        lowStockProduct.setStock(5); // 只有5个库存

        when(productDAO.findById(2)).thenReturn(lowStockProduct);

        OrderItem item = new OrderItem();
        item.setProductId(2);
        item.setQuantity(10); // 需要10个，但只有5个库存
        item.setPrice(new BigDecimal("100.00"));
        item.setSubtotal(new BigDecimal("1000.00"));

        List<OrderItem> items = new ArrayList<>();
        items.add(item);

        // Act
        boolean result = orderService.createOrder(testOrder, items);

        // Assert
        assertFalse(result, "库存不足应该失败");
        verify(orderInfoDAO, never()).create(any(OrderInfo.class));
    }

    @Test
    @Order(6)
    void testUpdateOrder_Success() {
        // Arrange
        OrderInfo updatedOrder = new OrderInfo();
        updatedOrder.setAddressId(101); // 只更新收货地址

        // Act
        boolean result = orderService.updateOrder(testOrder.getId(), updatedOrder);

        // Assert
        assertTrue(result, "更新订单应该成功");
        verify(orderInfoDAO, times(1)).update(any(OrderInfo.class));
    }

    @Test
    @Order(7)
    void testUpdateOrder_OrderNotFound_Fails() {
        // Arrange
        when(orderInfoDAO.findById("NON_EXISTENT_ORDER")).thenReturn(null);
        OrderInfo updatedOrder = new OrderInfo();

        // Act
        boolean result = orderService.updateOrder("NON_EXISTENT_ORDER", updatedOrder);

        // Assert
        assertFalse(result, "订单不存在应该失败");
        verify(orderInfoDAO, never()).update(any(OrderInfo.class));
    }

    @Test
    @Order(8)
    void testUpdateOrder_InvalidStatus_Fails() {
        // Arrange
        OrderInfo shippedOrder = new OrderInfo();
        shippedOrder.setId("SHIPPED_ORDER");
        shippedOrder.setStatus(3); // 已发货状态

        when(orderInfoDAO.findById("SHIPPED_ORDER")).thenReturn(shippedOrder);
        OrderInfo updatedOrder = new OrderInfo();

        // Act
        boolean result = orderService.updateOrder("SHIPPED_ORDER", updatedOrder);

        // Assert
        assertFalse(result, "已发货订单不应该允许修改");
        verify(orderInfoDAO, never()).update(any(OrderInfo.class));
    }

    @Test
    @Order(9)
    void testDeleteOrder_PendingPayment_Success() {
        // Arrange
        OrderInfo pendingOrder = new OrderInfo();
        pendingOrder.setId("PENDING_ORDER");
        pendingOrder.setStatus(1); // 待支付

        when(orderInfoDAO.findById("PENDING_ORDER")).thenReturn(pendingOrder);
        when(orderItemDAO.findByOrderId("PENDING_ORDER")).thenReturn(new ArrayList<>());

        // Act
        boolean result = orderService.deleteOrder("PENDING_ORDER");

        // Assert
        assertTrue(result, "删除待支付订单应该成功");
        verify(orderInfoDAO, times(1)).delete("PENDING_ORDER");
        // 待支付订单删除时不应该恢复库存
        verify(productDAO, never()).increaseStock(anyInt(), anyInt());
    }

    @Test
    @Order(10)
    void testDeleteOrder_PaidOrder_RestoresStock() {
        // Arrange
        OrderInfo paidOrder = new OrderInfo();
        paidOrder.setId("PAID_ORDER");
        paidOrder.setStatus(2); // 已支付

        List<OrderItem> items = new ArrayList<>();
        OrderItem item = new OrderItem();
        item.setId(1);
        item.setProductId(testProduct.getId());
        item.setQuantity(2);
        items.add(item);

        when(orderInfoDAO.findById("PAID_ORDER")).thenReturn(paidOrder);
        when(orderItemDAO.findByOrderId("PAID_ORDER")).thenReturn(items);

        // Act
        boolean result = orderService.deleteOrder("PAID_ORDER");

        // Assert
        assertTrue(result, "删除已支付订单应该成功");
        // 应该恢复库存
        verify(productDAO, times(1)).increaseStock(testProduct.getId(), 2);
        verify(inventoryLogDAO, times(1)).create(any(InventoryLog.class));
    }

    @Test
    @Order(11)
    void testDeleteOrder_ShippedOrder_Fails() {
        // Arrange
        OrderInfo shippedOrder = new OrderInfo();
        shippedOrder.setId("SHIPPED_ORDER");
        shippedOrder.setStatus(3); // 已发货

        when(orderInfoDAO.findById("SHIPPED_ORDER")).thenReturn(shippedOrder);

        // Act
        boolean result = orderService.deleteOrder("SHIPPED_ORDER");

        // Assert
        assertFalse(result, "已发货订单不应该允许删除");
        verify(orderInfoDAO, never()).delete(anyString());
    }

    @Test
    @Order(12)
    void testDeleteOrder_OrderNotFound_Fails() {
        // Arrange
        when(orderInfoDAO.findById("NON_EXISTENT_ORDER")).thenReturn(null);

        // Act
        boolean result = orderService.deleteOrder("NON_EXISTENT_ORDER");

        // Assert
        assertFalse(result, "订单不存在应该失败");
        verify(orderInfoDAO, never()).delete(anyString());
    }

    @Test
    @Order(13)
    void testUpdateOrderStatus_ValidTransition_Success() {
        // Test Case 1: 待支付 -> 已支付
        testOrder.setStatus(1);
        when(orderInfoDAO.updateStatus(testOrder.getId(), 2)).thenReturn(true);

        boolean result1 = orderService.updateOrderStatus(testOrder.getId(), 2);
        assertTrue(result1, "待支付到已支付的状态转换应该成功");
        verify(incomeDAO, times(1)).create(any(Income.class));

        // Test Case 2: 已支付 -> 已发货
        testOrder.setStatus(2);
        resetDAOMocks();
        setupCommonMockBehaviors();
        testOrder.setStatus(2);

        boolean result2 = orderService.updateOrderStatus(testOrder.getId(), 3);
        assertTrue(result2, "已支付到已发货的状态转换应该成功");
        verify(orderInfoDAO, times(1)).update(any(OrderInfo.class));

        // Test Case 3: 已发货 -> 已完成
        testOrder.setStatus(3);
        resetDAOMocks();
        setupCommonMockBehaviors();
        testOrder.setStatus(3);

        boolean result3 = orderService.updateOrderStatus(testOrder.getId(), 4);
        assertTrue(result3, "已发货到已完成的状态转换应该成功");
        verify(orderInfoDAO, times(1)).update(any(OrderInfo.class));

        // Test Case 4: 待支付 -> 已取消
        testOrder.setStatus(1);
        resetDAOMocks();
        setupCommonMockBehaviors();
        testOrder.setStatus(1);
        when(orderItemDAO.findByOrderId(testOrder.getId())).thenReturn(testOrderItems);

        boolean result4 = orderService.updateOrderStatus(testOrder.getId(), 5);
        assertTrue(result4, "待支付到已取消的状态转换应该成功");
        verify(productDAO, times(1)).increaseStock(anyInt(), anyInt());
    }

    @Test
    @Order(14)
    void testUpdateOrderStatus_InvalidTransition_Fails() {
        // Test Case 1: 已完成 -> 任何状态
        testOrder.setStatus(4);
        boolean result1 = orderService.updateOrderStatus(testOrder.getId(), 2);
        assertFalse(result1, "已完成订单不应该允许状态转换");

        // Test Case 2: 已取消 -> 任何状态
        testOrder.setStatus(5);
        boolean result2 = orderService.updateOrderStatus(testOrder.getId(), 2);
        assertFalse(result2, "已取消订单不应该允许状态转换");

        // Test Case 3: 已发货 -> 已支付（无效转换）
        testOrder.setStatus(3);
        boolean result3 = orderService.updateOrderStatus(testOrder.getId(), 2);
        assertFalse(result3, "已发货到已支付是无效的状态转换");

        // Test Case 4: 已支付 -> 待支付（无效转换）
        testOrder.setStatus(2);
        boolean result4 = orderService.updateOrderStatus(testOrder.getId(), 1);
        assertFalse(result4, "已支付到待支付是无效的状态转换");
    }

    @Test
    @Order(15)
    void testUpdateOrderStatus_OrderNotFound_Fails() {
        // Arrange
        when(orderInfoDAO.findById("NON_EXISTENT_ORDER")).thenReturn(null);

        // Act
        boolean result = orderService.updateOrderStatus("NON_EXISTENT_ORDER", 2);

        // Assert
        assertFalse(result, "订单不存在应该失败");
        verify(orderInfoDAO, never()).updateStatus(anyString(), anyInt());
    }

    @Test
    @Order(16)
    void testCalculateOrderTotal() {
        // Arrange
        BigDecimal expectedTotal = new BigDecimal("7999.00");
        when(orderItemDAO.calculateOrderTotal(testOrder.getId())).thenReturn(expectedTotal);

        // Act
        BigDecimal actualTotal = orderService.calculateOrderTotal(testOrder.getId());

        // Assert
        assertEquals(0, expectedTotal.compareTo(actualTotal), "订单总金额应该正确计算");
    }

    @Test
    @Order(17)
    void testGetOrderDetail_FullOrder() {
        // Arrange
        OrderInfo order = new OrderInfo();
        order.setId("DETAIL_ORDER");
        order.setUserId(1001);
        order.setTotalAmount(new BigDecimal("9999.00"));
        order.setStatus(4);

        List<OrderItem> items = new ArrayList<>();
        OrderItem item = new OrderItem();
        item.setId(1);
        item.setProductId(1);
        item.setProductName("测试商品");
        item.setQuantity(1);
        item.setPrice(new BigDecimal("9999.00"));
        items.add(item);

        Logistics logistics = new Logistics();
        logistics.setId(1);
        logistics.setOrderId("DETAIL_ORDER");
        logistics.setStatus("delivered");

        List<Income> incomes = new ArrayList<>();
        Income income = new Income();
        income.setId(1);
        income.setOrderId("DETAIL_ORDER");
        income.setAmount(new BigDecimal("9999.00"));
        incomes.add(income);

        List<AfterSales> afterSalesList = new ArrayList<>();
        Review review = new Review();
        review.setId(1);
        review.setOrderId("DETAIL_ORDER");
        review.setRating(5);

        when(orderInfoDAO.findById("DETAIL_ORDER")).thenReturn(order);
        when(orderItemDAO.findByOrderId("DETAIL_ORDER")).thenReturn(items);
        when(logisticsDAO.findByOrderId("DETAIL_ORDER")).thenReturn(logistics);
        when(incomeDAO.findByOrderId("DETAIL_ORDER")).thenReturn(incomes);
        when(afterSalesDAO.findByOrderId("DETAIL_ORDER")).thenReturn(afterSalesList);
        when(reviewDAO.findByOrderId("DETAIL_ORDER")).thenReturn(review);

        // Act
        OrderDetail detail = orderService.getOrderDetail("DETAIL_ORDER");

        // Assert
        assertNotNull(detail, "订单详情不应该为null");
        assertEquals(order, detail.getOrderInfo());
        assertEquals(items, detail.getOrderItems());
        assertEquals(logistics, detail.getLogistics());
        assertEquals(incomes, detail.getIncomes());
        assertEquals(afterSalesList, detail.getAfterSalesList());
        assertEquals(review, detail.getReview());
    }

    @Test
    @Order(18)
    void testGetOrderDetail_OrderNotFound() {
        // Arrange
        when(orderInfoDAO.findById("NON_EXISTENT_ORDER")).thenReturn(null);

        // Act
        OrderDetail detail = orderService.getOrderDetail("NON_EXISTENT_ORDER");

        // Assert
        assertNull(detail, "不存在的订单应该返回null");
    }

    @Test
    @Order(19)
    void testGetOrderDetail_MissingRelatedData() {
        // Arrange
        OrderInfo order = new OrderInfo();
        order.setId("MINIMAL_ORDER");
        order.setUserId(1001);
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setStatus(1);

        when(orderInfoDAO.findById("MINIMAL_ORDER")).thenReturn(order);
        when(orderItemDAO.findByOrderId("MINIMAL_ORDER")).thenReturn(new ArrayList<>());
        when(logisticsDAO.findByOrderId("MINIMAL_ORDER")).thenReturn(null);
        when(incomeDAO.findByOrderId("MINIMAL_ORDER")).thenReturn(new ArrayList<>());
        when(afterSalesDAO.findByOrderId("MINIMAL_ORDER")).thenReturn(new ArrayList<>());
        when(reviewDAO.findByOrderId("MINIMAL_ORDER")).thenReturn(null);

        // Act
        OrderDetail detail = orderService.getOrderDetail("MINIMAL_ORDER");

        // Assert
        assertNotNull(detail, "最小化订单应该返回详情");
        assertEquals(order, detail.getOrderInfo());
        assertTrue(detail.getOrderItems().isEmpty());
        assertNull(detail.getLogistics());
        assertTrue(detail.getIncomes().isEmpty());
        assertTrue(detail.getAfterSalesList().isEmpty());
        assertNull(detail.getReview());
    }

    @Test
    @Order(20)
    void testOrderLifecycle_CompleteFlow() {
        // 模拟完整的订单生命周期

        // 1. 创建新订单
        String orderId = "LIFECYCLE_ORDER";

        // 创建不同状态的订单对象
        OrderInfo orderStatus1 = new OrderInfo(); // 状态1：待支付
        orderStatus1.setId(orderId);
        orderStatus1.setUserId(1001);
        orderStatus1.setTotalAmount(new BigDecimal("5000.00"));
        orderStatus1.setStatus(1);
        orderStatus1.setCreatedAt(LocalDateTime.now());

        OrderInfo orderStatus2 = new OrderInfo(); // 状态2：已支付
        orderStatus2.setId(orderId);
        orderStatus2.setUserId(1001);
        orderStatus2.setTotalAmount(new BigDecimal("5000.00"));
        orderStatus2.setStatus(2);
        orderStatus2.setCreatedAt(LocalDateTime.now());
        orderStatus2.setPaidAt(LocalDateTime.now());

        OrderInfo orderStatus3 = new OrderInfo(); // 状态3：已发货
        orderStatus3.setId(orderId);
        orderStatus3.setUserId(1001);
        orderStatus3.setTotalAmount(new BigDecimal("5000.00"));
        orderStatus3.setStatus(3);
        orderStatus3.setCreatedAt(LocalDateTime.now());
        orderStatus3.setPaidAt(LocalDateTime.now());
        orderStatus3.setShippedAt(LocalDateTime.now());

        OrderInfo orderStatus4 = new OrderInfo(); // 状态4：已完成
        orderStatus4.setId(orderId);
        orderStatus4.setUserId(1001);
        orderStatus4.setTotalAmount(new BigDecimal("5000.00"));
        orderStatus4.setStatus(4);
        orderStatus4.setCreatedAt(LocalDateTime.now());
        orderStatus4.setPaidAt(LocalDateTime.now());
        orderStatus4.setShippedAt(LocalDateTime.now());
        orderStatus4.setCompletedAt(LocalDateTime.now());

        // 创建订单项
        List<OrderItem> newItems = new ArrayList<>();
        OrderItem item = new OrderItem();
        item.setProductId(1);
        item.setQuantity(1);
        item.setPrice(new BigDecimal("5000.00"));
        item.setSubtotal(new BigDecimal("5000.00"));
        newItems.add(item);

        // 重置所有Mock
        resetDAOMocks();

        // ========== 步骤1：创建订单 ==========
        // 设置创建订单所需的Mock
        when(productDAO.findById(1)).thenReturn(testProduct);
        when(orderInfoDAO.create(any(OrderInfo.class))).thenReturn(true);
        when(orderItemDAO.createBatch(anyList())).thenReturn(true);
        when(productDAO.decreaseStock(anyInt(), anyInt())).thenReturn(true);
        when(inventoryLogDAO.create(any(InventoryLog.class))).thenReturn(true);
        when(logisticsDAO.create(any(Logistics.class))).thenReturn(true);

        // 执行创建订单
        boolean createResult = orderService.createOrder(orderStatus1, newItems);
        assertTrue(createResult, "创建订单应该成功");

        // 验证创建订单的调用
        verify(orderInfoDAO, times(1)).create(any(OrderInfo.class));
        verify(orderItemDAO, times(1)).createBatch(anyList());
        verify(productDAO, times(1)).decreaseStock(anyInt(), anyInt());
        verify(logisticsDAO, times(1)).create(any(Logistics.class));

        // ========== 步骤2：更新订单状态为已支付 ==========
        resetDAOMocks(); // 重置Mock，避免之前的调用干扰

        // 设置状态2的Mock
        when(orderInfoDAO.findById(orderId)).thenReturn(orderStatus1); // 当前是状态1
        when(orderInfoDAO.updateStatus(orderId, 2)).thenReturn(true);
        when(orderItemDAO.findByOrderId(orderId)).thenReturn(newItems);

        // 执行状态更新
        boolean payResult = orderService.updateOrderStatus(orderId, 2);
        assertTrue(payResult, "支付订单应该成功");

        // 验证状态更新和相关操作
        verify(orderInfoDAO, times(1)).updateStatus(orderId, 2);
        verify(incomeDAO, times(1)).create(any(Income.class));

        // ========== 步骤3：更新订单状态为已发货 ==========
        resetDAOMocks(); // 重置Mock

        // 设置状态3的Mock
        when(orderInfoDAO.findById(orderId)).thenReturn(orderStatus2); // 当前是状态2
        when(orderInfoDAO.updateStatus(orderId, 3)).thenReturn(true);

        // 执行状态更新
        boolean shipResult = orderService.updateOrderStatus(orderId, 3);
        assertTrue(shipResult, "发货订单应该成功");

        // 验证状态更新
        verify(orderInfoDAO, times(1)).updateStatus(orderId, 3);

        // ========== 步骤4：更新订单状态为已完成 ==========
        resetDAOMocks(); // 重置Mock

        // 设置状态4的Mock
        when(orderInfoDAO.findById(orderId)).thenReturn(orderStatus3); // 当前是状态3
        when(orderInfoDAO.updateStatus(orderId, 4)).thenReturn(true);

        // 执行状态更新
        boolean completeResult = orderService.updateOrderStatus(orderId, 4);
        assertTrue(completeResult, "完成订单应该成功");

        // 验证状态更新
        verify(orderInfoDAO, times(1)).updateStatus(orderId, 4);
    }
    @Test
    @Order(21)
    void testCreateOrder_DatabaseException_Fails() {
        // Arrange
        when(orderInfoDAO.create(any(OrderInfo.class))).thenThrow(new RuntimeException("Database error"));

        // Act
        boolean result = orderService.createOrder(testOrder, testOrderItems);

        // Assert
        assertFalse(result, "数据库异常应该被捕获并返回false");
    }

    @Test
    @Order(22)
    void testDeleteOrder_CascadingDelete() {
        // Arrange
        OrderInfo order = new OrderInfo();
        order.setId("CASCADE_ORDER");
        order.setStatus(1); // 待支付

        List<OrderItem> items = new ArrayList<>();
        OrderItem item = new OrderItem();
        item.setId(1);
        item.setProductId(1);
        item.setQuantity(1);
        items.add(item);

        Logistics logistics = new Logistics();
        logistics.setId(1);

        List<Income> incomes = new ArrayList<>();
        Income income = new Income();
        income.setId(1);
        incomes.add(income);

        List<AfterSales> afterSalesList = new ArrayList<>();
        AfterSales afterSales = new AfterSales();
        afterSales.setId(1);
        afterSalesList.add(afterSales);

        Review review = new Review();
        review.setId(1);

        when(orderInfoDAO.findById("CASCADE_ORDER")).thenReturn(order);
        when(orderItemDAO.findByOrderId("CASCADE_ORDER")).thenReturn(items);
        when(logisticsDAO.findByOrderId("CASCADE_ORDER")).thenReturn(logistics);
        when(incomeDAO.findByOrderId("CASCADE_ORDER")).thenReturn(incomes);
        when(afterSalesDAO.findByOrderId("CASCADE_ORDER")).thenReturn(afterSalesList);
        when(reviewDAO.findByOrderId("CASCADE_ORDER")).thenReturn(review);

        // 设置删除操作成功
        when(orderItemDAO.delete(1)).thenReturn(true);
        when(logisticsDAO.delete(1)).thenReturn(true);
        when(incomeDAO.delete(1)).thenReturn(true);
        when(afterSalesDAO.delete(1)).thenReturn(true);
        when(reviewDAO.delete(1)).thenReturn(true);
        when(orderInfoDAO.delete("CASCADE_ORDER")).thenReturn(true);

        // Act
        boolean result = orderService.deleteOrder("CASCADE_ORDER");

        // Assert
        assertTrue(result, "级联删除应该成功");

        // 验证所有相关记录都被删除
        verify(orderItemDAO, times(1)).delete(1);
        verify(logisticsDAO, times(1)).delete(1);
        verify(incomeDAO, times(1)).delete(1);
        verify(afterSalesDAO, times(1)).delete(1);
        verify(reviewDAO, times(1)).delete(1);
        verify(orderInfoDAO, times(1)).delete("CASCADE_ORDER");
    }

    @Test
    @Order(23)
    void testErrorHandling_DAOOperationsFail() {
        // Test various DAO operation failures

        // Case 1: Order creation fails
        when(orderInfoDAO.create(any(OrderInfo.class))).thenReturn(false);
        boolean result1 = orderService.createOrder(testOrder, testOrderItems);
        assertFalse(result1, "订单创建失败应该返回false");

        // Case 2: Order item creation fails
        resetDAOMocks();
        setupCommonMockBehaviors();
        when(orderItemDAO.createBatch(anyList())).thenReturn(false);
        boolean result2 = orderService.createOrder(testOrder, testOrderItems);
        assertFalse(result2, "订单项创建失败应该返回false");

        // Case 3: Stock update fails
        resetDAOMocks();
        setupCommonMockBehaviors();
        when(productDAO.decreaseStock(anyInt(), anyInt())).thenReturn(false);
        boolean result3 = orderService.createOrder(testOrder, testOrderItems);
        assertFalse(result3, "库存更新失败应该返回false");
    }

    @Test
    @Order(24)
    void testEdgeCases() {
        // Test various edge cases

        // Case 1: Empty order ID
        boolean result1 = orderService.updateOrderStatus("", 2);
        assertFalse(result1, "空订单ID应该失败");

        // Case 2: Null order ID
        boolean result2 = orderService.updateOrderStatus(null, 2);
        assertFalse(result2, "null订单ID应该失败");

        // Case 3: Invalid status code
        testOrder.setStatus(1);
        boolean result3 = orderService.updateOrderStatus(testOrder.getId(), 99);
        assertFalse(result3, "无效的状态码应该失败");

        // Case 4: Large order quantity
        Product largeStockProduct = new Product();
        largeStockProduct.setId(3);
        largeStockProduct.setStock(10000);
        when(productDAO.findById(3)).thenReturn(largeStockProduct);

        OrderItem largeItem = new OrderItem();
        largeItem.setProductId(3);
        largeItem.setQuantity(10000);

        List<OrderItem> largeItems = new ArrayList<>();
        largeItems.add(largeItem);

        // 这里假设系统可以处理大数量订单
        when(orderInfoDAO.create(any(OrderInfo.class))).thenReturn(true);
        when(orderItemDAO.createBatch(anyList())).thenReturn(true);
        when(productDAO.decreaseStock(3, 10000)).thenReturn(true);

        OrderInfo largeOrder = new OrderInfo();
        largeOrder.setId("LARGE_ORDER");
        largeOrder.setStatus(1);

        boolean result4 = orderService.createOrder(largeOrder, largeItems);
        assertTrue(result4, "大数量订单应该成功处理");
    }

    @Test
    @Order(25)
    void testGetAllOrdersWithItems_Success() {
        // Arrange
        int page = 1;
        int pageSize = 10;

        // 创建测试订单列表
        List<OrderInfo> mockOrders = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            OrderInfo order = new OrderInfo();
            order.setId("ORDER2023120100" + i);
            order.setUserId(1000 + i);
            order.setTotalAmount(new BigDecimal(1000 * i));
            order.setStatus(i % 4 + 1); // 不同状态
            order.setCreatedAt(LocalDateTime.now());
            mockOrders.add(order);
        }

        // 创建测试订单项
        List<OrderItem> mockItems1 = new ArrayList<>();
        OrderItem item1 = new OrderItem();
        item1.setId(1);
        item1.setOrderId("ORDER20231201001");
        item1.setProductId(1);
        item1.setProductName("iPhone 14 Pro");
        item1.setQuantity(1);
        item1.setPrice(new BigDecimal("7999.00"));
        mockItems1.add(item1);

        List<OrderItem> mockItems2 = new ArrayList<>();
        OrderItem item2 = new OrderItem();
        item2.setId(2);
        item2.setOrderId("ORDER20231201002");
        item2.setProductId(2);
        item2.setProductName("MacBook Pro");
        item2.setQuantity(2);
        item2.setPrice(new BigDecimal("18999.00"));
        mockItems2.add(item2);

        // 设置Mock行为
        when(orderInfoDAO.findAll(page, pageSize)).thenReturn(mockOrders);
        when(orderItemDAO.findByOrderId("ORDER20231201001")).thenReturn(mockItems1);
        when(orderItemDAO.findByOrderId("ORDER20231201002")).thenReturn(mockItems2);
        when(orderItemDAO.findByOrderId("ORDER20231201003")).thenReturn(new ArrayList<>());

        // Act
        List<OrderWithItemsResponse> result = orderService.getAllOrdersWithItems(page, pageSize);

        // Assert
        assertNotNull(result, "结果不应该为null");
        assertEquals(3, result.size(), "应该返回3个订单");

        // 验证第一个订单
        OrderWithItemsResponse response1 = result.get(0);
        assertEquals("ORDER20231201001", response1.getOrder().getId());
        assertEquals(1, response1.getItems().size());
        assertEquals("iPhone 14 Pro", response1.getItems().get(0).getProductName());

        // 验证第二个订单
        OrderWithItemsResponse response2 = result.get(1);
        assertEquals("ORDER20231201002", response2.getOrder().getId());
        assertEquals(1, response2.getItems().size());
        assertEquals("MacBook Pro", response2.getItems().get(0).getProductName());

        // 验证第三个订单
        OrderWithItemsResponse response3 = result.get(2);
        assertEquals("ORDER20231201003", response3.getOrder().getId());
        assertTrue(response3.getItems().isEmpty(), "第三个订单应该没有订单项");

        // 验证DAO调用
        verify(orderInfoDAO, times(1)).findAll(page, pageSize);
        verify(orderItemDAO, times(3)).findByOrderId(anyString());
    }

    @Test
    @Order(26)
    void testGetAllOrdersWithItems_NoOrders() {
        // Arrange
        int page = 1;
        int pageSize = 10;

        // 返回空列表
        when(orderInfoDAO.findAll(page, pageSize)).thenReturn(new ArrayList<>());

        // Act
        List<OrderWithItemsResponse> result = orderService.getAllOrdersWithItems(page, pageSize);

        // Assert
        assertNotNull(result, "结果不应该为null，即使没有数据也应该返回空列表");
        assertTrue(result.isEmpty(), "没有订单时应该返回空列表");

        // 验证DAO调用
        verify(orderInfoDAO, times(1)).findAll(page, pageSize);
        verify(orderItemDAO, never()).findByOrderId(anyString());
    }

    @Test
    @Order(27)
    void testGetAllOrdersWithItems_DifferentPageSizes() {
        // Arrange
        List<OrderInfo> mockOrders = new ArrayList<>();

        // 测试不同分页大小
        int[] pageSizes = {5, 10, 20, 50};

        for (int pageSize : pageSizes) {
            resetDAOMocks();
            setupCommonMockBehaviors();

            // 创建指定数量的订单
            mockOrders.clear();
            for (int i = 0; i < pageSize; i++) {
                OrderInfo order = new OrderInfo();
                order.setId("ORDER-PAGE-" + pageSize + "-" + i);
                order.setUserId(1000 + i);
                order.setTotalAmount(new BigDecimal((i + 1) * 100));
                order.setStatus(1);
                mockOrders.add(order);
            }

            when(orderInfoDAO.findAll(1, pageSize)).thenReturn(mockOrders);
            when(orderItemDAO.findByOrderId(anyString())).thenReturn(new ArrayList<>());

            // Act
            List<OrderWithItemsResponse> result = orderService.getAllOrdersWithItems(1, pageSize);

            // Assert
            assertEquals(pageSize, result.size(), "第" + pageSize + "页应该返回" + pageSize + "个订单");

            // 验证DAO调用
            verify(orderInfoDAO, times(1)).findAll(1, pageSize);
            verify(orderItemDAO, times(pageSize)).findByOrderId(anyString());
        }
    }

    @Test
    @Order(28)
    void testGetAllOrdersWithItems_DAOException() {
        // Arrange
        int page = 1;
        int pageSize = 10;

        // 模拟DAO抛出异常
        when(orderInfoDAO.findAll(page, pageSize)).thenThrow(new RuntimeException("Database connection failed"));

        // Act
        List<OrderWithItemsResponse> result = orderService.getAllOrdersWithItems(page, pageSize);

        // Assert
        assertNotNull(result, "异常情况下也应该返回空列表而不是null");
        assertTrue(result.isEmpty(), "DAO异常时应该返回空列表");

        // 验证DAO调用
        verify(orderInfoDAO, times(1)).findAll(page, pageSize);
    }

    @Test
    @Order(29)
    void testGetAllOrdersWithItems_InvalidPageParameters() {
        // Arrange
        List<OrderInfo> mockOrders = new ArrayList<>();

        // 测试各种边界情况
        // 情况1：页码为0（应该从1开始）
        mockOrders.add(testOrder);
        when(orderInfoDAO.findAll(0, 10)).thenReturn(new ArrayList<>());

        List<OrderWithItemsResponse> result1 = orderService.getAllOrdersWithItems(0, 10);
        assertNotNull(result1);
        assertTrue(result1.isEmpty());

        // 情况2：页码为负数
        resetDAOMocks();
        when(orderInfoDAO.findAll(-1, 10)).thenReturn(new ArrayList<>());

        List<OrderWithItemsResponse> result2 = orderService.getAllOrdersWithItems(-1, 10);
        assertNotNull(result2);
        assertTrue(result2.isEmpty());

        // 情况3：页面大小为0
        resetDAOMocks();
        when(orderInfoDAO.findAll(1, 0)).thenReturn(new ArrayList<>());

        List<OrderWithItemsResponse> result3 = orderService.getAllOrdersWithItems(1, 0);
        assertNotNull(result3);
        assertTrue(result3.isEmpty());

        // 情况4：页面大小为负数
        resetDAOMocks();
        when(orderInfoDAO.findAll(1, -5)).thenReturn(new ArrayList<>());

        List<OrderWithItemsResponse> result4 = orderService.getAllOrdersWithItems(1, -5);
        assertNotNull(result4);
        assertTrue(result4.isEmpty());

        // 情况5：非常大的页面大小
        resetDAOMocks();
        when(orderInfoDAO.findAll(1, 1000)).thenReturn(new ArrayList<>());

        List<OrderWithItemsResponse> result5 = orderService.getAllOrdersWithItems(1, 1000);
        assertNotNull(result5);
        assertTrue(result5.isEmpty());
    }

    @Test
    @Order(30)
    void testGetAllOrdersWithItems_OrderItemDAOReturnsNull() {
        // Arrange
        int page = 1;
        int pageSize = 10;

        List<OrderInfo> mockOrders = new ArrayList<>();
        OrderInfo order1 = new OrderInfo();
        order1.setId("ORDER-TEST-001");
        order1.setUserId(1001);
        mockOrders.add(order1);

        OrderInfo order2 = new OrderInfo();
        order2.setId("ORDER-TEST-002");
        order2.setUserId(1002);
        mockOrders.add(order2);

        when(orderInfoDAO.findAll(page, pageSize)).thenReturn(mockOrders);

        // orderItemDAO.findByOrderId 返回 null（模拟异常情况）
        when(orderItemDAO.findByOrderId("ORDER-TEST-001")).thenReturn(null);
        when(orderItemDAO.findByOrderId("ORDER-TEST-002")).thenReturn(null);

        // Act
        List<OrderWithItemsResponse> result = orderService.getAllOrdersWithItems(page, pageSize);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size(), "应该返回2个订单");

        // 即使订单项为null，响应中的items应该为空列表而不是null
        for (OrderWithItemsResponse response : result) {
            assertNotNull(response.getItems(), "订单项列表不应该为null");
            assertTrue(response.getItems().isEmpty(), "当订单项为null时，应该返回空列表");
        }
    }

    @Test
    @Order(31)
    void testGetAllOrdersWithItems_OrderInfoDAOReturnsNull() {
        // Arrange
        int page = 1;
        int pageSize = 10;

        // orderInfoDAO.findAll 返回 null
        when(orderInfoDAO.findAll(page, pageSize)).thenReturn(null);

        // Act
        List<OrderWithItemsResponse> result = orderService.getAllOrdersWithItems(page, pageSize);

        // Assert
        assertNotNull(result, "即使findAll返回null，也应该返回空列表而不是null");
        assertTrue(result.isEmpty(), "当findAll返回null时，应该返回空列表");

        // 验证DAO调用
        verify(orderInfoDAO, times(1)).findAll(page, pageSize);
        verify(orderItemDAO, never()).findByOrderId(anyString());
    }

    @Test
    @Order(32)
    void testGetAllOrdersWithItems_MultiplePages() {
        // Arrange
        int pageSize = 5;

        // 模拟分页查询
        for (int page = 1; page <= 3; page++) {
            resetDAOMocks();
            setupCommonMockBehaviors();

            List<OrderInfo> mockOrders = new ArrayList<>();
            int startIndex = (page - 1) * pageSize;

            // 创建当前页的订单
            for (int i = 0; i < pageSize; i++) {
                OrderInfo order = new OrderInfo();
                int orderNum = startIndex + i + 1;
                order.setId("ORDER-PAGE" + page + "-" + orderNum);
                order.setUserId(1000 + orderNum);
                order.setTotalAmount(new BigDecimal(orderNum * 100));
                order.setStatus((orderNum % 4) + 1);
                mockOrders.add(order);
            }

            when(orderInfoDAO.findAll(page, pageSize)).thenReturn(mockOrders);
            when(orderItemDAO.findByOrderId(anyString())).thenReturn(new ArrayList<>());

            // Act
            List<OrderWithItemsResponse> result = orderService.getAllOrdersWithItems(page, pageSize);

            // Assert
            assertEquals(pageSize, result.size(), "第" + page + "页应该返回" + pageSize + "个订单");

            // 验证订单ID包含正确的页码信息
            for (int i = 0; i < result.size(); i++) {
                String expectedId = "ORDER-PAGE" + page + "-" + (startIndex + i + 1);
                assertEquals(expectedId, result.get(i).getOrder().getId());
            }
        }
    }

    @Test
    @Order(33)
    void testGetAllOrdersWithItems_PerformanceTest() {
        // Arrange
        int page = 1;
        int pageSize = 100;

        List<OrderInfo> mockOrders = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            OrderInfo order = new OrderInfo();
            order.setId("ORDER-PERF-" + i);
            order.setUserId(1000 + i);
            order.setTotalAmount(new BigDecimal((i + 1) * 50));
            order.setStatus(1);
            mockOrders.add(order);
        }

        // 重置并重新设置 Mock
        reset(orderItemDAO);

        // 为第一个订单设置特殊的订单项
        List<OrderItem> mockItems = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            OrderItem item = new OrderItem();
            item.setId(i + 1);
            item.setOrderId("ORDER-PERF-0");
            item.setProductId(i + 1);
            item.setProductName("Product " + (i + 1));
            item.setQuantity(i + 1);
            item.setPrice(new BigDecimal((i + 1) * 10));
            mockItems.add(item);
        }

        // 使用 thenAnswer 来根据订单ID返回不同的值
        when(orderItemDAO.findByOrderId(anyString())).thenAnswer(invocation -> {
            String orderId = invocation.getArgument(0);
            if ("ORDER-PERF-0".equals(orderId)) {
                return mockItems;
            } else {
                return new ArrayList<>();
            }
        });

        // 设置 orderInfoDAO 的 Mock
        when(orderInfoDAO.findAll(page, pageSize)).thenReturn(mockOrders);

        // Act
        List<OrderWithItemsResponse> result = orderService.getAllOrdersWithItems(page, pageSize);

        // Assert
        assertNotNull(result);
        assertEquals(pageSize, result.size());

        // 验证第一个订单有5个订单项
        OrderWithItemsResponse firstOrder = result.get(0);
        assertEquals("ORDER-PERF-0", firstOrder.getOrder().getId());
        assertEquals(5, firstOrder.getItems().size(), "第一个订单应该有5个订单项");

        // 验证其他订单没有订单项（只检查前几个）
        for (int i = 1; i < Math.min(5, result.size()); i++) {
            assertTrue(result.get(i).getItems().isEmpty(), "第" + i + "个订单应该没有订单项");
        }
    }

    @Test
    @Order(34)
    void testGetAllOrdersWithItems_EmptyOrderItems() {
        // Arrange
        int page = 1;
        int pageSize = 10;

        List<OrderInfo> mockOrders = new ArrayList<>();
        OrderInfo order = new OrderInfo();
        order.setId("ORDER-EMPTY-ITEMS");
        order.setUserId(1001);
        order.setTotalAmount(new BigDecimal("0.00"));
        order.setStatus(1);
        mockOrders.add(order);

        when(orderInfoDAO.findAll(page, pageSize)).thenReturn(mockOrders);
        when(orderItemDAO.findByOrderId("ORDER-EMPTY-ITEMS")).thenReturn(new ArrayList<>());

        // Act
        List<OrderWithItemsResponse> result = orderService.getAllOrdersWithItems(page, pageSize);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.get(0).getItems());
        assertTrue(result.get(0).getItems().isEmpty(), "订单项列表应该为空");
    }

    @Test
    @Order(35)
    void testGetAllOrdersWithItems_IntegrationWithOtherMethods() {
        // Arrange
        int page = 1;
        int pageSize = 3;

        // 创建与之前测试一致的订单数据
        List<OrderInfo> mockOrders = new ArrayList<>();
        OrderInfo order1 = new OrderInfo();
        order1.setId(testOrder.getId());
        order1.setUserId(testOrder.getUserId());
        order1.setTotalAmount(testOrder.getTotalAmount());
        order1.setStatus(testOrder.getStatus());
        mockOrders.add(order1);

        when(orderInfoDAO.findAll(page, pageSize)).thenReturn(mockOrders);
        when(orderItemDAO.findByOrderId(testOrder.getId())).thenReturn(testOrderItems);

        // Act
        List<OrderWithItemsResponse> result = orderService.getAllOrdersWithItems(page, pageSize);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        OrderWithItemsResponse response = result.get(0);
        assertEquals(testOrder.getId(), response.getOrder().getId());
        assertEquals(testOrder.getTotalAmount(), response.getOrder().getTotalAmount());

        // 验证订单项
        assertEquals(1, response.getItems().size());
        assertEquals(testProduct.getId(), response.getItems().get(0).getProductId());
        assertEquals(testProduct.getName(), response.getItems().get(0).getProductName());

        // 验证可以使用其他方法获取相同订单的详细信息
        OrderDetail detail = orderService.getOrderDetail(testOrder.getId());
        assertNotNull(detail);
        assertEquals(testOrder.getId(), detail.getOrderInfo().getId());
    }
}
