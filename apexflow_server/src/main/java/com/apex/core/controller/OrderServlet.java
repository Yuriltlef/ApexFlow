// [file name]: OrderServlet.java
package com.apex.core.controller;

import com.apex.api.BaseServlet;
import com.apex.core.dao.*;
import com.apex.core.dto.*;
import com.apex.core.model.OrderInfo;
import com.apex.core.model.OrderItem;
import com.apex.core.service.OrderService;
import com.apex.util.RequirePermission;
import com.apex.util.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单管理API接口
 *
 * API列表:
 * POST   /api/orders           - 创建订单
 * GET    /api/orders/{id}      - 获取订单详情
 * GET    /api/orders/list      - 获取订单列表（支持分页和筛选）
 * PUT    /api/orders/{id}      - 更新订单信息
 * DELETE /api/orders/{id}      - 删除订单
 * PUT    /api/orders/{id}/status - 更新订单状态
 */
@WebServlet("/api/orders/*")
public class OrderServlet extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(OrderServlet.class);

    // 注意：在实际项目中，应该使用依赖注入，这里简化直接创建
    private OrderService orderService;

    @Override
    public void init() {
        orderService = new OrderService(
                new OrderInfoDAO(),
                new OrderItemDAO(),
                new ProductDAO(),
                new InventoryLogDAO(),
                new LogisticsDAO(),
                new IncomeDAO(),
                new AfterSalesDAO(),
                new ReviewDAO()
        );
        logger.info("[ORDER_SERVLET] Initializing OrderServlet");
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long startTime = System.currentTimeMillis();
        String pathInfo = req.getPathInfo();

        try {
            logRequest(req);

            if (pathInfo == null) {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        "API路径不正确", "API_PATH_INVALID");
                logResponse(req, resp, startTime, HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            if (pathInfo.equals("/list")) {
                handleListOrders(req, resp);
            } else {
                // 尝试解析ID
                try {
                    String idStr = pathInfo.substring(1); // 去掉开头的"/"
                    handleGetOrderDetail(req, resp, idStr);
                } catch (NumberFormatException e) {
                    sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                            "API不存在", "API_NOT_FOUND");
                }
            }

            logResponse(req, resp, startTime, resp.getStatus());
        } catch (Exception e) {
            logger.error("OrderServlet GET error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
            logResponse(req, resp, startTime, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long startTime = System.currentTimeMillis();
        String pathInfo = req.getPathInfo();

        try {
            logRequest(req);

            if (pathInfo == null || !pathInfo.equals("")) {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        "API路径不正确", "API_PATH_INVALID");
                logResponse(req, resp, startTime, HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            handleCreateOrder(req, resp);
            logResponse(req, resp, startTime, resp.getStatus());
        } catch (Exception e) {
            logger.error("OrderServlet POST error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
            logResponse(req, resp, startTime, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long startTime = System.currentTimeMillis();
        String pathInfo = req.getPathInfo();

        try {
            logRequest(req);

            if (pathInfo == null) {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        "API路径不正确", "API_PATH_INVALID");
                logResponse(req, resp, startTime, HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 检查是否是状态更新
            if (pathInfo.contains("/status")) {
                try {
                    String[] parts = pathInfo.split("/");
                    String orderId = parts[1];
                    handleUpdateOrderStatus(req, resp, orderId);
                } catch (Exception e) {
                    sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                            "订单ID格式不正确", "INVALID_ORDER_ID_FORMAT");
                }
            } else {
                try {
                    String orderId = pathInfo.substring(1); // 去掉开头的"/"
                    handleUpdateOrder(req, resp, orderId);
                } catch (NumberFormatException e) {
                    sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                            "API不存在", "API_NOT_FOUND");
                }
            }

            logResponse(req, resp, startTime, resp.getStatus());
        } catch (Exception e) {
            logger.error("OrderServlet PUT error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
            logResponse(req, resp, startTime, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long startTime = System.currentTimeMillis();
        String pathInfo = req.getPathInfo();

        try {
            logRequest(req);

            if (pathInfo == null) {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        "API路径不正确", "API_PATH_INVALID");
                logResponse(req, resp, startTime, HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            try {
                String orderId = pathInfo.substring(1); // 去掉开头的"/"
                handleDeleteOrder(req, resp, orderId);
            } catch (NumberFormatException e) {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        "API不存在", "API_NOT_FOUND");
            }

            logResponse(req, resp, startTime, resp.getStatus());
        } catch (Exception e) {
            logger.error("OrderServlet DELETE error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
            logResponse(req, resp, startTime, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理创建订单
     * 需要管理员或订单管理权限
     */
    @RequirePermission(value = {Permission.ADMIN, Permission.ORDER_MANAGE},
            logic = RequirePermission.LogicType.OR,
            message = "需要管理员或订单管理权限")
    private void handleCreateOrder(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("[ORDER_CREATE] Processing create order request from IP: {}", getClientIp(req));

        String token = extractToken(req);
        if (token == null) {
            sendErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    "缺少认证Token", "MISSING_AUTHORIZATION");
            return;
        }

        try {
            // 验证Content-Type
            if (!isJsonContentType(req)) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "请求必须是JSON格式", "INVALID_CONTENT_TYPE");
                return;
            }

            // 解析请求体
            CreateOrderRequest createRequest = parseJsonBody(req, CreateOrderRequest.class);
            logger.debug("[ORDER_CREATE] Create order request data: {}", createRequest);

            // 验证必填字段
            if (createRequest.getUserId() == null || createRequest.getTotalAmount() == null ||
                    createRequest.getOrderItems() == null || createRequest.getOrderItems().isEmpty()) {
                ApiResponse<Void> response = ApiResponse.error("用户ID、总金额和订单项不能为空");
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, response);
                return;
            }

            // 转换为OrderInfo和OrderItem对象
            OrderInfo order = new OrderInfo();
            order.setId(generateOrderId());
            order.setUserId(createRequest.getUserId());
            order.setTotalAmount(createRequest.getTotalAmount());
            order.setStatus(1); // 默认待支付状态
            order.setPaymentMethod(createRequest.getPaymentMethod());
            order.setAddressId(createRequest.getAddressId());
            order.setCreatedAt(LocalDateTime.now());

            List<OrderItem> orderItems = new ArrayList<>();
            for (OrderItemRequest itemRequest : createRequest.getOrderItems()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(order.getId());
                orderItem.setProductId(itemRequest.getProductId());
                orderItem.setProductName(itemRequest.getProductName());
                orderItem.setQuantity(itemRequest.getQuantity());
                orderItem.setPrice(itemRequest.getPrice());
                orderItem.setSubtotal(itemRequest.getSubtotal());
                orderItems.add(orderItem);
            }

            // 调用服务层创建订单
            // 注意：这里需要实际的OrderService实例，此处为示例代码
            boolean success = orderService.createOrder(order, orderItems);

            if (success) {
                // 构建响应数据
                Map<String, Object> data = new HashMap<>();
                data.put("orderId", order.getId());
                data.put("totalAmount", order.getTotalAmount());
                data.put("status", order.getStatus());
                data.put("createdAt", order.getCreatedAt());

                ApiResponse<Map<String, Object>> response = ApiResponse.success(
                        data, "订单创建成功"
                );
                sendJsonResponse(resp, HttpServletResponse.SC_CREATED, response);
                logger.info("[ORDER_CREATE] Order created successfully. Order ID: {}", order.getId());
            } else {
                ApiResponse<Void> response = ApiResponse.error("订单创建失败");
                sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
                logger.warn("[ORDER_CREATE] Failed to create order");
            }
        } catch (IOException e) {
            logger.error("[ORDER_CREATE] Failed to parse request body: {}", e.getMessage());
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "请求格式错误", "INVALID_REQUEST_FORMAT");
        } catch (Exception e) {
            logger.error("[ORDER_CREATE] Unexpected error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
        }
    }

    /**
     * 处理获取订单详情
     * 需要管理员或订单管理权限
     */
    @RequirePermission(value = {Permission.ADMIN, Permission.ORDER_MANAGE},
            logic = RequirePermission.LogicType.OR,
            message = "需要管理员或订单管理权限")
    private void handleGetOrderDetail(HttpServletRequest req, HttpServletResponse resp, String orderId) throws IOException {
        logger.info("[ORDER_DETAIL] Processing get order detail request. Order ID: {}", orderId);

        try {
            // 调用服务层获取订单详情 - 只调用一次
            OrderDetail orderDetail = orderService.getOrderDetail(orderId);

            if (orderDetail == null || orderDetail.getOrderInfo() == null) {
                ApiResponse<Void> response = ApiResponse.error("订单不存在");
                sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND, response);
                logger.warn("[ORDER_DETAIL] Order not found. Order ID: {}", orderId);
                return;
            }

            // 从orderDetail中获取订单信息
            OrderInfo orderInfo = orderDetail.getOrderInfo();
            List<OrderItem> orderItems = orderDetail.getOrderItems() != null ?
                    orderDetail.getOrderItems() : new ArrayList<>();

            // 构建响应数据
            OrderDetailResponse detailResponse = new OrderDetailResponse();
            detailResponse.setOrderId(orderInfo.getId());
            detailResponse.setUserId(orderInfo.getUserId());
            detailResponse.setTotalAmount(orderInfo.getTotalAmount());
            detailResponse.setStatus(orderInfo.getStatus());
            detailResponse.setPaymentMethod(orderInfo.getPaymentMethod());
            detailResponse.setAddressId(orderInfo.getAddressId());
            detailResponse.setCreatedAt(orderInfo.getCreatedAt());
            detailResponse.setPaidAt(orderInfo.getPaidAt());
            detailResponse.setShippedAt(orderInfo.getShippedAt());
            detailResponse.setCompletedAt(orderInfo.getCompletedAt());

            List<OrderItemResponse> itemResponses = new ArrayList<>();
            for (OrderItem item : orderItems) {
                OrderItemResponse itemResponse = new OrderItemResponse();
                itemResponse.setId(item.getId());
                itemResponse.setOrderId(item.getOrderId());
                itemResponse.setProductId(item.getProductId());
                itemResponse.setProductName(item.getProductName());
                itemResponse.setQuantity(item.getQuantity());
                itemResponse.setPrice(item.getPrice());
                itemResponse.setSubtotal(item.getSubtotal());
                itemResponses.add(itemResponse);
            }
            detailResponse.setOrderItems(itemResponses);

            ApiResponse<OrderDetailResponse> response = ApiResponse.success(
                    detailResponse, "订单详情获取成功"
            );
            sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
            logger.info("[ORDER_DETAIL] Order detail retrieved successfully for Order ID: {}", orderId);

        } catch (Exception e) {
            logger.error("[ORDER_DETAIL] Unexpected error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
        }
    }

    /**
     * 处理获取订单列表
     * 需要管理员或订单管理权限
     */
    @RequirePermission(value = {Permission.ADMIN, Permission.ORDER_MANAGE},
            logic = RequirePermission.LogicType.OR,
            message = "需要管理员或订单管理权限")
    private void handleListOrders(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("[ORDER_LIST] Processing list orders request from IP: {}", getClientIp(req));

        try {
            // 获取查询参数
            String userIdStr = req.getParameter("userId");
            String statusStr = req.getParameter("status");
            String pageStr = req.getParameter("page");
            String pageSizeStr = req.getParameter("pageSize");

            Integer userId = null;
            Integer status = null;
            Integer page = null;
            Integer pageSize = null;

            try {
                if (userIdStr != null && !userIdStr.trim().isEmpty()) {
                    userId = Integer.parseInt(userIdStr);
                }
                if (statusStr != null && !statusStr.trim().isEmpty()) {
                    status = Integer.parseInt(statusStr);
                }
                if (pageStr != null && !pageStr.trim().isEmpty()) {
                    page = Integer.parseInt(pageStr);
                }
                if (pageSizeStr != null && !pageSizeStr.trim().isEmpty()) {
                    pageSize = Integer.parseInt(pageSizeStr);
                }
            } catch (NumberFormatException e) {
                logger.warn("[ORDER_LIST] Invalid number format in query parameters");
                ApiResponse<Void> response = ApiResponse.error("查询参数格式错误");
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, response);
                return;
            }

            // 设置默认值
            if (page == null || page < 1) page = 1;
            if (pageSize == null || pageSize < 1) pageSize = 20;
            if (pageSize > 100) pageSize = 100; // 限制每页最大记录数

            // 调用服务层获取订单列表
            // 注意：这里需要实际的OrderService实例，此处为示例代码
            List<OrderWithItemsResponse> ordersWithItems = orderService.getAllOrdersWithItems(page, pageSize);
            List<OrderInfo> orders = new ArrayList<>();

            for (OrderWithItemsResponse os : ordersWithItems) {
                orders.add(os.getOrder());
            }

            // 构建响应数据
            List<OrderListItem> orderList = new ArrayList<>();
            for (OrderInfo order : orders) {
                OrderListItem item = new OrderListItem();
                item.setOrderId(order.getId());
                item.setUserId(order.getUserId());
                item.setTotalAmount(order.getTotalAmount());
                item.setStatus(order.getStatus());
                item.setPaymentMethod(order.getPaymentMethod());
                item.setCreatedAt(order.getCreatedAt());
                orderList.add(item);
            }

            // 获取总订单数（分页信息）
            long totalCount = orderService.getCount();

            Map<String, Object> data = new HashMap<>();
            data.put("orders", orderList);
            data.put("currentPage", page);
            data.put("pageSize", pageSize);
            data.put("totalCount", totalCount);
            data.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

            ApiResponse<Map<String, Object>> response = ApiResponse.success(
                    data, "订单列表获取成功"
            );
            sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
            logger.info("[ORDER_LIST] Order list retrieved successfully. Count: {}", orderList.size());

        } catch (Exception e) {
            logger.error("[ORDER_LIST] Unexpected error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
        }
    }

    /**
     * 处理更新订单信息
     * 需要管理员或订单管理权限
     */
    @RequirePermission(value = {Permission.ADMIN, Permission.ORDER_MANAGE},
            logic = RequirePermission.LogicType.OR,
            message = "需要管理员或订单管理权限")
    private void handleUpdateOrder(HttpServletRequest req, HttpServletResponse resp, String orderId) throws IOException {
        logger.info("[ORDER_UPDATE] Processing update order request. Order ID: {}", orderId);

        String token = extractToken(req);
        if (token == null) {
            sendErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    "缺少认证Token", "MISSING_AUTHORIZATION");
            return;
        }

        try {
            // 验证Content-Type
            if (!isJsonContentType(req)) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "请求必须是JSON格式", "INVALID_CONTENT_TYPE");
                return;
            }

            // 解析请求体
            UpdateOrderRequest updateRequest = parseJsonBody(req, UpdateOrderRequest.class);
            logger.debug("[ORDER_UPDATE] Update order request data: {}", updateRequest);

            // 检查订单是否存在
            OrderInfo existingOrder = orderService.getOrderDetail(orderId).getOrderInfo();
            if (existingOrder == null) {
                ApiResponse<Void> response = ApiResponse.error("订单不存在");
                sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND, response);
                return;
            }

            // 更新订单信息
            boolean updated = false;
            if (updateRequest.getAddressId() != null) {
                existingOrder.setAddressId(updateRequest.getAddressId());
                updated = true;
            }
            if (updateRequest.getPaymentMethod() != null) {
                existingOrder.setPaymentMethod(updateRequest.getPaymentMethod());
                updated = true;
            }

            if (updated) {
                // 调用服务层更新订单
                boolean success = orderService.updateOrder(existingOrder.getId(), existingOrder);

                if (success) {
                    ApiResponse<Void> response = ApiResponse.success(null, "订单信息更新成功");
                    sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
                    logger.info("[ORDER_UPDATE] Order updated successfully for Order ID: {}", orderId);
                } else {
                    ApiResponse<Void> response = ApiResponse.error("订单信息更新失败");
                    sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
                    logger.warn("[ORDER_UPDATE] Failed to update order: {}", orderId);
                }
            } else {
                ApiResponse<Void> response = ApiResponse.error("没有提供有效的更新字段");
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, response);
            }

        } catch (IOException e) {
            logger.error("[ORDER_UPDATE] Failed to parse request body: {}", e.getMessage());
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "请求格式错误", "INVALID_REQUEST_FORMAT");
        } catch (Exception e) {
            logger.error("[ORDER_UPDATE] Unexpected error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
        }
    }

    /**
     * 处理删除订单
     * 需要管理员或订单管理权限
     */
    @RequirePermission(value = {Permission.ADMIN, Permission.ORDER_MANAGE},
            logic = RequirePermission.LogicType.OR,
            message = "需要管理员或订单管理权限")
    private void handleDeleteOrder(HttpServletRequest req, HttpServletResponse resp, String orderId) throws IOException {
        logger.info("[ORDER_DELETE] Processing delete order request. Order ID: {}", orderId);

        String token = extractToken(req);
        if (token == null) {
            sendErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    "缺少认证Token", "MISSING_AUTHORIZATION");
            return;
        }

        try {
            // 调用服务层删除订单
            // 注意：这里需要实际的OrderService实例，此处为示例代码
            boolean success = orderService.deleteOrder(orderId);

            if (success) {
                ApiResponse<Void> response = ApiResponse.success(null, "订单删除成功");
                sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
                logger.info("[ORDER_DELETE] Order deleted successfully for Order ID: {}", orderId);
            } else {
                ApiResponse<Void> response = ApiResponse.error("订单删除失败");
                sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
                logger.warn("[ORDER_DELETE] Failed to delete order: {}", orderId);
            }
        } catch (Exception e) {
            logger.error("[ORDER_DELETE] Unexpected error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
        }
    }

    /**
     * 处理更新订单状态
     * 需要管理员或订单管理权限
     */
    @RequirePermission(value = {Permission.ADMIN, Permission.ORDER_MANAGE},
            logic = RequirePermission.LogicType.OR,
            message = "需要管理员或订单管理权限")
    private void handleUpdateOrderStatus(HttpServletRequest req, HttpServletResponse resp, String orderId) throws IOException {
        logger.info("[ORDER_STATUS] Processing update order status request. Order ID: {}", orderId);

        String token = extractToken(req);
        if (token == null) {
            sendErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    "缺少认证Token", "MISSING_AUTHORIZATION");
            return;
        }

        try {
            // 解析请求体获取状态值
            UpdateOrderStatusRequest requestBody = parseJsonBody(req, UpdateOrderStatusRequest.class);
            Integer status = requestBody.getStatus();

            if (status == null) {
                ApiResponse<Void> response = ApiResponse.error("状态值不能为空");
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, response);
                return;
            }

            // 验证状态值是否有效
            if (status < 1 || status > 5) {
                ApiResponse<Void> response = ApiResponse.error("无效的订单状态");
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, response);
                return;
            }

            // 调用服务层更新订单状态
            // 注意：这里需要实际的OrderService实例，此处为示例代码
            boolean success = orderService.updateOrderStatus(orderId, status);

            if (success) {
                ApiResponse<Void> response = ApiResponse.success(null, "订单状态更新成功");
                sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
                logger.info("[ORDER_STATUS] Order status updated successfully for Order ID: {}", orderId);
            } else {
                ApiResponse<Void> response = ApiResponse.error("订单状态更新失败");
                sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
                logger.warn("[ORDER_STATUS] Failed to update order status: {}", orderId);
            }
        } catch (IOException e) {
            logger.error("[ORDER_STATUS] Failed to parse request body: {}", e.getMessage());
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "请求格式错误", "INVALID_REQUEST_FORMAT");
        } catch (Exception e) {
            logger.error("[ORDER_STATUS] Unexpected error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
        }
    }

    /**
     * 生成订单ID
     */
    private String generateOrderId() {
        return "ORDER" + System.currentTimeMillis();
    }

    /**
     * 从请求中提取Token
     */
    private String extractToken(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * 检查请求是否为JSON格式
     */
    private boolean isJsonContentType(HttpServletRequest req) {
        String contentType = req.getContentType();
        return contentType != null && contentType.contains("application/json");
    }
}
