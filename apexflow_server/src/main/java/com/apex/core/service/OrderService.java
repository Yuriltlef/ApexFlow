package com.apex.core.service;

import com.apex.core.dao.*;
import com.apex.core.dto.OrderDetail;
import com.apex.core.dto.OrderWithItemsResponse;
import com.apex.core.model.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 订单管理服务类
 * 负责协调多个DAO完成复杂的订单业务逻辑
 * 注意：不使用Spring框架，通过构造函数注入依赖
 */
public class OrderService {

    private final IOrderInfoDAO orderInfoDAO;
    private final IOrderItemDAO orderItemDAO;
    private final IProductDAO productDAO;
    private final IInventoryLogDAO inventoryLogDAO;
    private final ILogisticsDAO logisticsDAO;
    private final IIncomeDAO incomeDAO;
    private final IAfterSalesDAO afterSalesDAO;
    private final IReviewDAO reviewDAO;

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    /**
     * 构造函数，注入所有需要的DAO
     */
    public OrderService(IOrderInfoDAO orderInfoDAO,
                        IOrderItemDAO orderItemDAO,
                        IProductDAO productDAO,
                        IInventoryLogDAO inventoryLogDAO,
                        ILogisticsDAO logisticsDAO,
                        IIncomeDAO incomeDAO,
                        IAfterSalesDAO afterSalesDAO,
                        IReviewDAO reviewDAO) {
        this.orderInfoDAO = orderInfoDAO;
        this.orderItemDAO = orderItemDAO;
        this.productDAO = productDAO;
        this.inventoryLogDAO = inventoryLogDAO;
        this.logisticsDAO = logisticsDAO;
        this.incomeDAO = incomeDAO;
        this.afterSalesDAO = afterSalesDAO;
        this.reviewDAO = reviewDAO;
    }

    /**
     * 创建完整订单
     * 步骤：
     * 1. 验证订单信息和商品库存
     * 2. 创建订单主记录
     * 3. 创建订单项记录
     * 4. 更新商品库存
     * 5. 创建物流记录
     * 6. 创建财务记录
     *
     * @param order 订单主信息
     * @param orderItems 订单项列表
     * @return 创建成功返回true，失败返回false
     */
    public boolean createOrder(OrderInfo order, List<OrderItem> orderItems) {
        String operation = "CREATE_ORDER";

        if (order == null) {
            logger.error("[{}] Failed to create order: order is null", operation);
            return false;
        }

        logger.info("[{}] Starting order creation process. Order ID: {}, User ID: {}",
                operation, order.getId(), order.getUserId());

        try {
            // 1. 验证参数
            if (orderItems == null || orderItems.isEmpty()) {
                throw new IllegalArgumentException("Order information and order items cannot be empty");
            }

            // 2. 验证商品库存
            logger.debug("[{}] Validating product stock for {} items", operation, orderItems.size());
            for (OrderItem item : orderItems) {
                Product product = productDAO.findById(item.getProductId());
                if (product == null) {
                    throw new RuntimeException("Product ID " + item.getProductId() + " does not exist");
                }
                if (product.getStock() < item.getQuantity()) {
                    throw new RuntimeException("Product " + product.getName() + " has insufficient stock. " +
                            "Current stock: " + product.getStock() + ", Required: " + item.getQuantity());
                }
            }

            // 3. 保存订单主信息
            logger.debug("[{}] Saving order information", operation);
            boolean orderCreated = orderInfoDAO.create(order);
            if (!orderCreated) {
                throw new RuntimeException("Failed to create order");
            }

            // 4. 批量保存订单项
            logger.debug("[{}] Saving order items (count: {})", operation, orderItems.size());
            boolean itemsCreated = orderItemDAO.createBatch(orderItems);
            if (!itemsCreated) {
                throw new RuntimeException("Failed to create order items");
            }

            // 5. 更新商品库存
            logger.debug("[{}] Updating product stock for {} items", operation, orderItems.size());
            for (OrderItem item : orderItems) {
                boolean stockUpdated = productDAO.decreaseStock(item.getProductId(), item.getQuantity());
                if (!stockUpdated) {
                    throw new RuntimeException("Failed to update product stock. Product ID: " + item.getProductId());
                }

                // 记录库存变更日志
                Product product = productDAO.findById(item.getProductId());
                if (product != null) {
                    InventoryLog inventoryLog = new InventoryLog();
                    inventoryLog.setProductId(item.getProductId());
                    inventoryLog.setChangeType("sale");
                    inventoryLog.setQuantity(-item.getQuantity());
                    inventoryLog.setBeforeStock(product.getStock() + item.getQuantity());
                    inventoryLog.setAfterStock(product.getStock());
                    inventoryLog.setOrderId(order.getId());
                    inventoryLogDAO.create(inventoryLog);
                    logger.debug("[{}] Created inventory log for product {} (ID: {})",
                            operation, product.getName(), product.getId());
                }
            }

            // 6. 创建物流记录
            logger.debug("[{}] Creating logistics record", operation);
            Logistics logistics = new Logistics();
            logistics.setOrderId(order.getId());
            logistics.setStatus("pending");
            logistics.setReceiverAddress("To be filled"); // 实际应用中应从用户地址获取
            boolean logisticsCreated = logisticsDAO.create(logistics);
            if (!logisticsCreated) {
                throw new RuntimeException("Failed to create logistics record");
            }

            // 7. 创建财务记录（仅当订单已支付时）
            if (order.getStatus() == 2) { // 已支付状态
                logger.debug("[{}] Creating income record for paid order", operation);
                Income income = new Income();
                income.setOrderId(order.getId());
                income.setType("income");
                income.setAmount(order.getTotalAmount());
                income.setPaymentMethod(order.getPaymentMethod());
                income.setStatus(2); // 已入账
                income.setTransactionTime(order.getPaidAt());
                incomeDAO.create(income);
            }

            logger.info("[{}] Order created successfully. Order ID: {}, Total amount: {}",
                    operation, order.getId(), order.getTotalAmount());
            return true;
        } catch (Exception e) {
            logger.error("[{}] Failed to create order. Order ID: {}, Error: {}",
                    operation, order.getId(), e.getMessage(), e);
            // 这里应该回滚所有操作，但在不使用事务管理器的情况下需要手动处理
            // 可以考虑使用补偿操作或者将整个操作放在数据库事务中
            return false;
        }
    }

    /**
     * 修改订单信息
     * 只能修改某些特定字段，如收货地址
     *
     * @param orderId 订单ID
     * @param updatedOrder 更新后的订单信息
     * @return 更新成功返回true，失败返回false
     */
    public boolean updateOrder(String orderId, OrderInfo updatedOrder) {
        String operation = "UPDATE_ORDER";
        logger.info("[{}] Starting order update. Order ID: {}", operation, orderId);

        try {
            // 1. 检查订单是否存在
            OrderInfo existingOrder = orderInfoDAO.findById(orderId);
            if (existingOrder == null) {
                throw new RuntimeException("Order does not exist: " + orderId);
            }

            // 2. 验证订单状态是否允许修改
            // 只有待支付和已支付状态的订单可以修改某些信息
            if (existingOrder.getStatus() == 3 || existingOrder.getStatus() == 4 || existingOrder.getStatus() == 5) {
                throw new RuntimeException("Order status does not allow modification: " + existingOrder.getStatus());
            }

            // 3. 只允许修改特定字段（防止恶意修改）
            logger.debug("[{}] Updating address ID from {} to {}",
                    operation, existingOrder.getAddressId(), updatedOrder.getAddressId());
            existingOrder.setAddressId(updatedOrder.getAddressId());

            // 4. 更新订单
            boolean result = orderInfoDAO.update(existingOrder);
            if (result) {
                logger.info("[{}] Order updated successfully. Order ID: {}", operation, orderId);
            } else {
                logger.warn("[{}] Failed to update order. Order ID: {}", operation, orderId);
            }
            return result;
        } catch (Exception e) {
            logger.error("[{}] Failed to update order. Order ID: {}, Error: {}",
                    operation, orderId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 删除订单（级联删除）
     * 需要删除所有相关的记录：
     * 1. 检查订单状态（只有特定状态可以删除）
     * 2. 恢复商品库存
     * 3. 删除订单项
     * 4. 删除物流记录
     * 5. 删除财务记录
     * 6. 删除售后记录
     * 7. 删除评价记录
     * 8. 删除订单主记录
     *
     * @param orderId 订单ID
     * @return 删除成功返回true，失败返回false
     */
    public boolean deleteOrder(String orderId) {
        String operation = "DELETE_ORDER";
        logger.info("[{}] Starting order deletion. Order ID: {}", operation, orderId);

        try {
            // 1. 检查订单是否存在
            OrderInfo order = orderInfoDAO.findById(orderId);
            if (order == null) {
                throw new RuntimeException("Order does not exist: " + orderId);
            }

            // 2. 检查订单状态（通常只允许删除待支付或已取消的订单）
            if (order.getStatus() == 3 || order.getStatus() == 4) {
                throw new RuntimeException("Shipped or completed orders cannot be deleted");
            }

            // 3. 恢复商品库存（如果订单已支付或已发货）
            if (order.getStatus() == 2) {
                List<OrderItem> orderItems = orderItemDAO.findByOrderId(orderId);
                logger.debug("[{}] Restoring stock for {} order items", operation, orderItems.size());

                for (OrderItem item : orderItems) {
                    productDAO.increaseStock(item.getProductId(), item.getQuantity());

                    // 记录库存恢复日志
                    Product product = productDAO.findById(item.getProductId());
                    if (product != null) {
                        InventoryLog inventoryLog = new InventoryLog();
                        inventoryLog.setProductId(item.getProductId());
                        inventoryLog.setChangeType("cancel");
                        inventoryLog.setQuantity(item.getQuantity());
                        inventoryLog.setBeforeStock(product.getStock() - item.getQuantity());
                        inventoryLog.setAfterStock(product.getStock());
                        inventoryLog.setOrderId(orderId);
                        inventoryLogDAO.create(inventoryLog);
                    }
                }
            }

            // 4. 删除订单项
            List<OrderItem> orderItems = orderItemDAO.findByOrderId(orderId);
            logger.debug("[{}] Deleting {} order items", operation, orderItems.size());
            for (OrderItem item : orderItems) {
                orderItemDAO.delete(item.getId());
            }

            // 5. 删除物流记录
            Logistics logistics = logisticsDAO.findByOrderId(orderId);
            if (logistics != null && logistics.getId() != null) {
                logger.debug("[{}] Deleting logistics record", operation);
                logisticsDAO.delete(logistics.getId());
            }

            // 6. 删除财务记录
            List<Income> incomes = incomeDAO.findByOrderId(orderId);
            logger.debug("[{}] Deleting {} income records", operation, incomes.size());
            for (Income income : incomes) {
                incomeDAO.delete(income.getId());
            }

            // 7. 删除售后记录
            List<AfterSales> afterSalesList = afterSalesDAO.findByOrderId(orderId);
            logger.debug("[{}] Deleting {} after-sales records", operation, afterSalesList.size());
            for (AfterSales afterSales : afterSalesList) {
                afterSalesDAO.delete(afterSales.getId());
            }

            // 8. 删除评价记录
            Review review = reviewDAO.findByOrderId(orderId);
            if (review != null && review.getId() != null) {
                logger.debug("[{}] Deleting review record", operation);
                reviewDAO.delete(review.getId());
            }

            // 9. 删除订单主记录
            logger.debug("[{}] Deleting main order record", operation);
            boolean result = orderInfoDAO.delete(orderId);

            if (result) {
                logger.info("[{}] Order deleted successfully. Order ID: {}", operation, orderId);
            } else {
                logger.warn("[{}] Failed to delete order. Order ID: {}", operation, orderId);
            }

            return result;
        } catch (Exception e) {
            logger.error("[{}] Failed to delete order. Order ID: {}, Error: {}",
                    operation, orderId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 更新订单状态
     *
     * @param orderId 订单ID
     * @param newStatus 新状态
     * @return 更新成功返回true，失败返回false
     */
    public boolean updateOrderStatus(String orderId, int newStatus) {
        String operation = "UPDATE_ORDER_STATUS";
        logger.info("[{}] Updating order status. Order ID: {}, New Status: {}",
                operation, orderId, newStatus);

        try {
            OrderInfo order = orderInfoDAO.findById(orderId);
            if (order == null) {
                throw new RuntimeException("Order does not exist: " + orderId);
            }

            int oldStatus = order.getStatus();

            // 如果状态相同，直接返回 true（允许幂等操作）
            if (oldStatus == newStatus) {
                logger.debug("[{}] Order status unchanged. Order ID: {}, Status: {}",
                        operation, orderId, newStatus);
                return true;
            }

            // 状态转换验证
            if (!isValidStatusTransition(oldStatus, newStatus)) {
                throw new RuntimeException("Invalid status transition: " + oldStatus + " -> " + newStatus);
            }

            // 更新订单状态
            boolean success = orderInfoDAO.updateStatus(orderId, newStatus);

            if (success) {
                logger.info("[{}] Order status updated successfully. Order ID: {}, " +
                                "Old Status: {}, New Status: {}",
                        operation, orderId, oldStatus, newStatus);

                // 根据状态变化执行相应操作
                handleStatusChangeActions(orderId, oldStatus, newStatus);
            } else {
                logger.warn("[{}] Failed to update order status. Order ID: {}", operation, orderId);
            }

            return success;
        } catch (Exception e) {
            logger.error("[{}] Failed to update order status. Order ID: {}, Error: {}",
                    operation, orderId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 验证状态转换是否有效
     */
    private boolean isValidStatusTransition(int oldStatus, int newStatus) {
        // 状态转换规则
        // 1-待支付 -> 2-已支付, 5-已取消
        // 2-已支付 -> 3-已发货, 5-已取消
        // 3-已发货 -> 4-已完成
        // 4-已完成 -> 不允许再改变
        // 5-已取消 -> 不允许再改变

        return switch (oldStatus) {
            case 1 -> // 待支付
                    newStatus == 2 || newStatus == 5;
            case 2 -> // 已支付
                    newStatus == 3 || newStatus == 5;
            case 3 -> // 已发货
                    newStatus == 4; // 已完成
            case 4, 5 -> // 已取消
                    false;
            default -> false;
        };
    }

    /**
     * 处理状态变化相关的操作
     */
    private void handleStatusChangeActions(String orderId, int oldStatus, int newStatus) {
        OrderInfo order = orderInfoDAO.findById(orderId);
        if (order == null) return;

        logger.info("[UPDATE_ORDER_STATUS] update from old state:{}", oldStatus);

        switch (newStatus) {
            case 2: // 已支付
                logger.debug("[UPDATE_ORDER_STATUS] Creating income record for paid order");
                // 创建财务记录
                Income income = new Income();
                income.setOrderId(orderId);
                income.setType("income");
                income.setAmount(order.getTotalAmount());
                income.setPaymentMethod(order.getPaymentMethod());
                income.setStatus(2); // 已入账
                income.setTransactionTime(LocalDateTime.now());
                incomeDAO.create(income);
                break;

            case 3: // 已发货
                logger.debug("[UPDATE_ORDER_STATUS] Updating shipment time");
                // 更新发货时间
                order.setShippedAt(LocalDateTime.now());
                orderInfoDAO.update(order);
                break;

            case 4: // 已完成
                logger.debug("[UPDATE_ORDER_STATUS] Updating completion time");
                // 更新完成时间
                order.setCompletedAt(LocalDateTime.now());
                orderInfoDAO.update(order);
                break;

            case 5: // 已取消
                logger.debug("[UPDATE_ORDER_STATUS] Restoring product stock for cancelled order");
                // 恢复库存
                List<OrderItem> orderItems = orderItemDAO.findByOrderId(orderId);
                for (OrderItem item : orderItems) {
                    productDAO.increaseStock(item.getProductId(), item.getQuantity());

                    // 记录库存恢复日志
                    Product product = productDAO.findById(item.getProductId());
                    if (product != null) {
                        InventoryLog inventoryLog = new InventoryLog();
                        inventoryLog.setProductId(item.getProductId());
                        inventoryLog.setChangeType("cancel");
                        inventoryLog.setQuantity(item.getQuantity());
                        inventoryLog.setBeforeStock(product.getStock() - item.getQuantity());
                        inventoryLog.setAfterStock(product.getStock());
                        inventoryLog.setOrderId(orderId);
                        inventoryLogDAO.create(inventoryLog);
                    }
                }
                break;
        }
    }

    /**
     * 计算订单总金额（从订单项汇总）
     *
     * @param orderId 订单ID
     * @return 订单总金额
     */
    public BigDecimal calculateOrderTotal(String orderId) {
        String operation = "CALCULATE_ORDER_TOTAL";
        logger.debug("[{}] Calculating total for order ID: {}", operation, orderId);

        BigDecimal total = orderItemDAO.calculateOrderTotal(orderId);
        logger.debug("[{}] Order total calculated. Order ID: {}, Total: {}",
                operation, orderId, total);

        return total;
    }

    /**
     * 获取订单详情（包括订单项）
     *
     * @param orderId 订单ID
     * @return 包含订单项的订单详情对象
     */
    public OrderDetail getOrderDetail(String orderId) {
        String operation = "GET_ORDER_DETAIL";
        logger.debug("[{}] Retrieving order detail. Order ID: {}", operation, orderId);

        OrderDetail detail = new OrderDetail();

        // 获取订单主信息
        OrderInfo order = orderInfoDAO.findById(orderId);
        if (order == null) {
            logger.warn("[{}] Order not found. Order ID: {}", operation, orderId);
            return null;
        }

        detail.setOrderInfo(order);

        // 获取订单项
        List<OrderItem> items = orderItemDAO.findByOrderId(orderId);
        detail.setOrderItems(items);
        logger.debug("[{}] Retrieved {} order items", operation, items.size());

        // 获取物流信息
        Logistics logistics = logisticsDAO.findByOrderId(orderId);
        detail.setLogistics(logistics);

        // 获取财务记录
        List<Income> incomes = incomeDAO.findByOrderId(orderId);
        detail.setIncomes(incomes);
        logger.debug("[{}] Retrieved {} income records", operation, incomes.size());

        // 获取售后记录
        List<AfterSales> afterSalesList = afterSalesDAO.findByOrderId(orderId);
        detail.setAfterSalesList(afterSalesList);
        logger.debug("[{}] Retrieved {} after-sales records", operation, afterSalesList.size());

        // 获取评价
        Review review = reviewDAO.findByOrderId(orderId);
        detail.setReview(review);

        logger.info("[{}] Order detail retrieved successfully. Order ID: {}, User ID: {}",
                operation, orderId, order.getUserId());

        return detail;
    }

    /**
     * 获取所有订单及其条目信息（分页）
     * Get all orders with their item information (paged)
     *
     * @param page 页码，从1开始 / Page number, starting from 1
     * @param pageSize 每页记录数 / Number of records per page
     * @return 包含订单和订单项信息的列表 / List containing order and order item information
     */
    public List<OrderWithItemsResponse> getAllOrdersWithItems(int page, int pageSize) {
        String operation = "GET_ALL_ORDERS_WITH_ITEMS";
        long startTime = System.currentTimeMillis();

        logger.info("[{}] Starting to get all orders with items. Page: {}, PageSize: {}",
                operation, page, pageSize);

        List<OrderWithItemsResponse> result = new ArrayList<>();

        try {
            // 1. 获取分页订单主信息
            // Get paged order main information
            List<OrderInfo> orders = orderInfoDAO.findAll(page, pageSize);

            if (orders == null || orders.isEmpty()) {
                long duration = System.currentTimeMillis() - startTime;
                logger.info("[{}] No orders found. Page: {}, Duration: {} ms",
                        operation, page, duration);
                return result;
            }

            logger.debug("[{}] Retrieved {} orders for page {}",
                    operation, orders.size(), page);

            // 2. 为每个订单获取订单项
            // Get order items for each order
            for (OrderInfo order : orders) {
                OrderWithItemsResponse response = new OrderWithItemsResponse();
                response.setOrder(order);

                // 获取订单项
                // Get order items
                List<OrderItem> items = orderItemDAO.findByOrderId(order.getId());
                response.setItems(items != null ? items : new ArrayList<>());

                result.add(response);
            }

            long duration = System.currentTimeMillis() - startTime;
            logger.info("[{}] Successfully retrieved {} orders with items in {} ms. Page: {}",
                    operation, result.size(), duration, page);

            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[{}] Failed to get all orders with items after {} ms. Page: {}, Error: {}",
                    operation, duration, page, e.getMessage(), e);
            return result; // 返回空列表而不是null / Return empty list instead of null
        }
    }

    public long getCount() {
        return orderInfoDAO.count();
    }
}
