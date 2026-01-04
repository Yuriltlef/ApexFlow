package com.apex.core.controller;

import com.apex.api.BaseServlet;
import com.apex.core.dao.*;
import com.apex.core.dto.*;
import com.apex.core.model.Product;
import com.apex.core.model.InventoryLog;
import com.apex.core.service.InventoryService;
import com.apex.util.RequirePermission;
import com.apex.util.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存管理API接口
 * API列表:
 * POST   /api/inventory/products           - 创建商品
 * GET    /api/inventory/products/{id}      - 获取商品详情
 * GET    /api/inventory/products/list      - 获取商品列表（支持分页、分类、搜索）
 * PUT    /api/inventory/products/{id}      - 更新商品信息
 * DELETE /api/inventory/products/{id}      - 删除商品（下架）
 * PUT    /api/inventory/products/{id}/stock - 调整库存
 * POST   /api/inventory/stock/increase     - 增加库存（采购）
 * POST   /api/inventory/stock/decrease     - 减少库存（销售）
 * GET    /api/inventory/logs              - 获取库存变更日志
 * GET    /api/inventory/low-stock         - 获取低库存预警
 */
@WebServlet("/api/inventory/*")
public class InventoryServlet extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(InventoryServlet.class);

    private InventoryService inventoryService;

    @Override
    public void init() {
        inventoryService = new InventoryService(
                new ProductDAO(),
                new InventoryLogDAO()
        );
        logger.info("[INVENTORY_SERVLET] Initializing InventoryServlet");
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

            // 路由处理
            if (pathInfo.equals("/products/list")) {
                handleListProducts(req, resp);
            } else if (pathInfo.equals("/logs")) {
                handleGetLogs(req, resp);
            } else if (pathInfo.equals("/low-stock")) {
                handleGetLowStock(req, resp);
            } else if (pathInfo.startsWith("/products/")) {
                // 处理商品详情
                try {
                    String idStr = pathInfo.substring("/products/".length());
                    if (idStr.contains("/stock")) {
                        // 跳过库存调整的PUT请求
                        sendErrorResponse(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                                "请使用PUT方法进行库存调整", "METHOD_NOT_ALLOWED");
                    } else {
                        handleGetProductDetail(req, resp, idStr);
                    }
                } catch (NumberFormatException e) {
                    sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                            "API不存在", "API_NOT_FOUND");
                }
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        "API路径不正确", "API_PATH_INVALID");
            }

            logResponse(req, resp, startTime, resp.getStatus());
        } catch (Exception e) {
            logger.error("[INVENTORY_SERVLET] GET error: {}", e.getMessage(), e);
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

            if (pathInfo == null) {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        "API路径不正确", "API_PATH_INVALID");
                logResponse(req, resp, startTime, HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 路由处理
            switch (pathInfo) {
                case "/products" -> handleCreateProduct(req, resp);
                case "/stock/increase" -> handleIncreaseStock(req, resp);
                case "/stock/decrease" -> handleDecreaseStock(req, resp);
                default -> sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        "API路径不正确", "API_PATH_INVALID");
            }

            logResponse(req, resp, startTime, resp.getStatus());
        } catch (Exception e) {
            logger.error("[INVENTORY_SERVLET] POST error: {}", e.getMessage(), e);
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

            // 路由处理
            if (pathInfo.startsWith("/products/")) {
                try {
                    String remainingPath = pathInfo.substring("/products/".length());
                    if (remainingPath.contains("/stock")) {
                        // 库存调整
                        String[] parts = remainingPath.split("/");
                        String productId = parts[0];
                        handleAdjustStock(req, resp, productId);
                    } else {
                        // 更新商品信息
                        handleUpdateProduct(req, resp, remainingPath);
                    }
                } catch (Exception e) {
                    sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                            "请求格式不正确", "INVALID_REQUEST_FORMAT");
                }
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        "API路径不正确", "API_PATH_INVALID");
            }

            logResponse(req, resp, startTime, resp.getStatus());
        } catch (Exception e) {
            logger.error("[INVENTORY_SERVLET] PUT error: {}", e.getMessage(), e);
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

            // 路由处理
            if (pathInfo.startsWith("/products/")) {
                try {
                    String productId = pathInfo.substring("/products/".length());
                    handleDeleteProduct(req, resp, productId);
                } catch (NumberFormatException e) {
                    sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                            "API不存在", "API_NOT_FOUND");
                }
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        "API路径不正确", "API_PATH_INVALID");
            }

            logResponse(req, resp, startTime, resp.getStatus());
        } catch (Exception e) {
            logger.error("[INVENTORY_SERVLET] DELETE error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
            logResponse(req, resp, startTime, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 处理创建商品
     * 需要管理员或库存管理权限
     */
    @RequirePermission(value = {Permission.ADMIN, Permission.INVENTORY_MANAGE},
            logic = RequirePermission.LogicType.OR,
            message = "需要管理员或库存管理权限")
    private void handleCreateProduct(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("[INVENTORY_CREATE] Processing create product request from IP: {}", getClientIp(req));

        try {
            // 验证Content-Type
            if (!isJsonContentType(req)) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "请求必须是JSON格式", "INVALID_CONTENT_TYPE");
                return;
            }

            // 解析请求体
            CreateProductRequest createRequest = parseJsonBody(req, CreateProductRequest.class);
            logger.debug("[INVENTORY_CREATE] Create product request data: {}", createRequest);

            // 验证必填字段
            if (createRequest.getName() == null || createRequest.getName().trim().isEmpty() ||
                    createRequest.getPrice() == null || createRequest.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                ApiResponse<Void> response = ApiResponse.error("商品名称和价格不能为空，且价格必须大于0");
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, response);
                return;
            }

            // 转换为Product对象
            Product product = new Product();
            product.setName(createRequest.getName());
            product.setCategory(createRequest.getCategory());
            product.setPrice(createRequest.getPrice());
            product.setStock(createRequest.getStock() != null ? createRequest.getStock() : 0);
            product.setStatus(createRequest.getStatus() != null ? createRequest.getStatus() : 1);
            product.setImage(createRequest.getImage());
            product.setCreatedAt(LocalDateTime.now());

            // 调用服务层创建商品
            boolean success = inventoryService.createProduct(product);

            if (success && product.getId() != null) {
                // 构建响应数据
                ProductResponse productResponse = new ProductResponse();
                productResponse.setId(product.getId());
                productResponse.setName(product.getName());
                productResponse.setCategory(product.getCategory());
                productResponse.setPrice(product.getPrice());
                productResponse.setStock(product.getStock());
                productResponse.setStatus(product.getStatus());
                productResponse.setImage(product.getImage());
                productResponse.setCreatedAt(product.getCreatedAt());

                ApiResponse<ProductResponse> response = ApiResponse.success(
                        productResponse, "商品创建成功"
                );
                sendJsonResponse(resp, HttpServletResponse.SC_CREATED, response);
                logger.info("[INVENTORY_CREATE] Product created successfully. ID: {}", product.getId());
            } else {
                ApiResponse<Void> response = ApiResponse.error("商品创建失败");
                sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
                logger.warn("[INVENTORY_CREATE] Failed to create product");
            }
        } catch (IOException e) {
            logger.error("[INVENTORY_CREATE] Failed to parse request body: {}", e.getMessage());
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "请求格式错误", "INVALID_REQUEST_FORMAT");
        } catch (Exception e) {
            logger.error("[INVENTORY_CREATE] Unexpected error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
        }
    }

    /**
     * 处理获取商品详情
     * 需要管理员或库存管理权限
     */
    @RequirePermission(value = {Permission.ADMIN, Permission.INVENTORY_MANAGE},
            logic = RequirePermission.LogicType.OR,
            message = "需要管理员或库存管理权限")
    private void handleGetProductDetail(HttpServletRequest req, HttpServletResponse resp, String productIdStr) throws IOException {
        logger.info("[INVENTORY_DETAIL] Processing get product detail request. Product ID: {}", productIdStr);

        try {
            Integer productId = Integer.parseInt(productIdStr);

            // 调用服务层获取商品详情
            Product product = inventoryService.getProductById(productId);

            if (product == null) {
                ApiResponse<Void> response = ApiResponse.error("商品不存在");
                sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND, response);
                logger.warn("[INVENTORY_DETAIL] Product not found. ID: {}", productId);
                return;
            }

            // 构建响应数据
            ProductResponse productResponse = new ProductResponse();
            productResponse.setId(product.getId());
            productResponse.setName(product.getName());
            productResponse.setCategory(product.getCategory());
            productResponse.setPrice(product.getPrice());
            productResponse.setStock(product.getStock());
            productResponse.setStatus(product.getStatus());
            productResponse.setImage(product.getImage());
            productResponse.setCreatedAt(product.getCreatedAt());

            ApiResponse<ProductResponse> response = ApiResponse.success(
                    productResponse, "商品详情获取成功"
            );
            sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
            logger.info("[INVENTORY_DETAIL] Product detail retrieved successfully for ID: {}", productId);

        } catch (NumberFormatException e) {
            logger.error("[INVENTORY_DETAIL] Invalid product ID format: {}", productIdStr);
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "商品ID格式不正确", "INVALID_PRODUCT_ID");
        } catch (Exception e) {
            logger.error("[INVENTORY_DETAIL] Unexpected error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
        }
    }

    /**
     * 处理获取商品列表
     * 需要管理员或库存管理权限
     */
    @RequirePermission(value = {Permission.ADMIN, Permission.INVENTORY_MANAGE},
            logic = RequirePermission.LogicType.OR,
            message = "需要管理员或库存管理权限")
    private void handleListProducts(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("[INVENTORY_LIST] Processing list products request from IP: {}", getClientIp(req));

        try {
            // 获取查询参数
            String category = req.getParameter("category");
            String keyword = req.getParameter("keyword");
            String statusStr = req.getParameter("status");
            String pageStr = req.getParameter("page");
            String pageSizeStr = req.getParameter("pageSize");

            Integer status = null;
            Integer page = null;
            Integer pageSize = null;

            try {
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
                logger.warn("[INVENTORY_LIST] Invalid number format in query parameters");
                ApiResponse<Void> response = ApiResponse.error("查询参数格式错误");
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, response);
                return;
            }

            // 设置默认值
            if (page == null || page < 1) page = 1;
            if (pageSize == null || pageSize < 1) pageSize = 20;
            if (pageSize > 100) pageSize = 100; // 限制每页最大记录数

            List<Product> products;

            // 根据查询条件调用不同的方法
            if (keyword != null && !keyword.trim().isEmpty()) {
                products = inventoryService.searchProducts(keyword, page, pageSize);
            } else if (category != null && !category.trim().isEmpty()) {
                products = inventoryService.getProductsByCategory(category, page, pageSize);
            } else if (status != null) {
                products = inventoryService.getProductsByStatus(status, page, pageSize);
            } else {
                products = inventoryService.getProducts(page, pageSize);
            }

            if (products == null) {
                products = new ArrayList<>();
            }

            // 构建响应数据
            List<ProductListItem> productList = new ArrayList<>();
            for (Product product : products) {
                ProductListItem item = new ProductListItem();
                item.setId(product.getId());
                item.setName(product.getName());
                item.setCategory(product.getCategory());
                item.setPrice(product.getPrice());
                item.setStock(product.getStock());
                item.setStatus(product.getStatus());
                item.setImage(product.getImage());
                item.setCreatedAt(product.getCreatedAt());
                productList.add(item);
            }

            // 获取总商品数（分页信息）
            long totalCount;
            if (category != null && !category.trim().isEmpty()) {
                totalCount = inventoryService.getProductCountByCategory(category);
            } else {
                totalCount = inventoryService.getProductCount();
            }

            Map<String, Object> data = new HashMap<>();
            data.put("products", productList);
            data.put("currentPage", page);
            data.put("pageSize", pageSize);
            data.put("totalCount", totalCount);
            data.put("totalPages", (int) Math.ceil((double) totalCount / pageSize));

            ApiResponse<Map<String, Object>> response = ApiResponse.success(
                    data, "商品列表获取成功"
            );
            sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
            logger.info("[INVENTORY_LIST] Product list retrieved successfully. Count: {}", totalCount);

        } catch (Exception e) {
            logger.error("[INVENTORY_LIST] Unexpected error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
        }
    }

    /**
     * 处理更新商品信息
     * 需要管理员或库存管理权限
     */
    @RequirePermission(value = {Permission.ADMIN, Permission.INVENTORY_MANAGE},
            logic = RequirePermission.LogicType.OR,
            message = "需要管理员或库存管理权限")
    private void handleUpdateProduct(HttpServletRequest req, HttpServletResponse resp, String productIdStr) throws IOException {
        logger.info("[INVENTORY_UPDATE] Processing update product request. Product ID: {}", productIdStr);

        try {
            Integer productId = Integer.parseInt(productIdStr);

            // 验证Content-Type
            if (!isJsonContentType(req)) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "请求必须是JSON格式", "INVALID_CONTENT_TYPE");
                return;
            }

            // 解析请求体
            UpdateProductRequest updateRequest = parseJsonBody(req, UpdateProductRequest.class);
            logger.debug("[INVENTORY_UPDATE] Update product request data: {}", updateRequest);

            // 检查商品是否存在
            Product existingProduct = inventoryService.getProductById(productId);
            if (existingProduct == null) {
                ApiResponse<Void> response = ApiResponse.error("商品不存在");
                sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND, response);
                return;
            }

            // 创建更新后的商品对象
            Product updatedProduct = new Product();
            updatedProduct.setId(productId);
            updatedProduct.setName(updateRequest.getName() != null ? updateRequest.getName() : existingProduct.getName());
            updatedProduct.setCategory(updateRequest.getCategory() != null ? updateRequest.getCategory() : existingProduct.getCategory());
            updatedProduct.setPrice(updateRequest.getPrice() != null ? updateRequest.getPrice() : existingProduct.getPrice());
            updatedProduct.setStock(updateRequest.getStock() != null ? updateRequest.getStock() : existingProduct.getStock());
            updatedProduct.setStatus(updateRequest.getStatus() != null ? updateRequest.getStatus() : existingProduct.getStatus());
            updatedProduct.setImage(updateRequest.getImage() != null ? updateRequest.getImage() : existingProduct.getImage());
            updatedProduct.setCreatedAt(existingProduct.getCreatedAt());

            // 调用服务层更新商品
            boolean success = inventoryService.updateProduct(productId, updatedProduct);

            if (success) {
                ApiResponse<Void> response = ApiResponse.success(null, "商品信息更新成功");
                sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
                logger.info("[INVENTORY_UPDATE] Product updated successfully for ID: {}", productId);
            } else {
                ApiResponse<Void> response = ApiResponse.error("商品信息更新失败");
                sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
                logger.warn("[INVENTORY_UPDATE] Failed to update product: {}", productId);
            }

        } catch (NumberFormatException e) {
            logger.error("[INVENTORY_UPDATE] Invalid product ID format: {}", productIdStr);
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "商品ID格式不正确", "INVALID_PRODUCT_ID");
        } catch (IOException e) {
            logger.error("[INVENTORY_UPDATE] Failed to parse request body: {}", e.getMessage());
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "请求格式错误", "INVALID_REQUEST_FORMAT");
        } catch (Exception e) {
            logger.error("[INVENTORY_UPDATE] Unexpected error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
        }
    }

    /**
     * 处理删除商品（实际是下架）
     * 需要管理员或库存管理权限
     */
    @RequirePermission(value = {Permission.ADMIN, Permission.INVENTORY_MANAGE},
            logic = RequirePermission.LogicType.OR,
            message = "需要管理员或库存管理权限")
    private void handleDeleteProduct(HttpServletRequest req, HttpServletResponse resp, String productIdStr) throws IOException {
        logger.info("[INVENTORY_DELETE] Processing delete product request. Product ID: {}", productIdStr);

        try {
            Integer productId = Integer.parseInt(productIdStr);

            // 调用服务层删除商品（下架）
            boolean success = inventoryService.deleteProduct(productId);

            if (success) {
                ApiResponse<Void> response = ApiResponse.success(null, "商品已下架");
                sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
                logger.info("[INVENTORY_DELETE] Product deleted successfully for ID: {}", productId);
            } else {
                ApiResponse<Void> response = ApiResponse.error("商品下架失败");
                sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
                logger.warn("[INVENTORY_DELETE] Failed to delete product: {}", productId);
            }
        } catch (NumberFormatException e) {
            logger.error("[INVENTORY_DELETE] Invalid product ID format: {}", productIdStr);
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "商品ID格式不正确", "INVALID_PRODUCT_ID");
        } catch (Exception e) {
            logger.error("[INVENTORY_DELETE] Unexpected error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
        }
    }

    /**
     * 处理调整库存
     * 需要管理员或库存管理权限
     */
    @RequirePermission(value = {Permission.ADMIN, Permission.INVENTORY_MANAGE},
            logic = RequirePermission.LogicType.OR,
            message = "需要管理员或库存管理权限")
    private void handleAdjustStock(HttpServletRequest req, HttpServletResponse resp, String productIdStr) throws IOException {
        logger.info("[INVENTORY_ADJUST] Processing adjust stock request. Product ID: {}", productIdStr);

        try {
            Integer productId = Integer.parseInt(productIdStr);

            // 验证Content-Type
            if (!isJsonContentType(req)) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "请求必须是JSON格式", "INVALID_CONTENT_TYPE");
                return;
            }

            // 解析请求体
            StockAdjustRequest adjustRequest = parseJsonBody(req, StockAdjustRequest.class);
            logger.debug("[INVENTORY_ADJUST] Adjust stock request data: {}", adjustRequest);

            // 验证请求数据
            if (adjustRequest.getNewStock() == null || adjustRequest.getNewStock() < 0) {
                ApiResponse<Void> response = ApiResponse.error("新库存数量不能为空且必须大于等于0");
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, response);
                return;
            }

            // 调用服务层调整库存
            boolean success = inventoryService.adjustStock(productId, adjustRequest.getNewStock(),
                    adjustRequest.getReason() != null ? adjustRequest.getReason() : "手动调整");

            if (success) {
                ApiResponse<Void> response = ApiResponse.success(null, "库存调整成功");
                sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
                logger.info("[INVENTORY_ADJUST] Stock adjusted successfully for product ID: {}", productId);
            } else {
                ApiResponse<Void> response = ApiResponse.error("库存调整失败");
                sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
                logger.warn("[INVENTORY_ADJUST] Failed to adjust stock for product: {}", productId);
            }
        } catch (NumberFormatException e) {
            logger.error("[INVENTORY_ADJUST] Invalid product ID format: {}", productIdStr);
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "商品ID格式不正确", "INVALID_PRODUCT_ID");
        } catch (IOException e) {
            logger.error("[INVENTORY_ADJUST] Failed to parse request body: {}", e.getMessage());
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "请求格式错误", "INVALID_REQUEST_FORMAT");
        } catch (Exception e) {
            logger.error("[INVENTORY_ADJUST] Unexpected error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
        }
    }

    /**
     * 处理增加库存（采购入库）
     * 需要管理员或库存管理权限
     */
    @RequirePermission(value = {Permission.ADMIN, Permission.INVENTORY_MANAGE},
            logic = RequirePermission.LogicType.OR,
            message = "需要管理员或库存管理权限")
    private void handleIncreaseStock(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("[INVENTORY_INCREASE] Processing increase stock request from IP: {}", getClientIp(req));

        try {
            // 验证Content-Type
            if (!isJsonContentType(req)) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "请求必须是JSON格式", "INVALID_CONTENT_TYPE");
                return;
            }

            // 解析请求体
            StockChangeRequest changeRequest = parseJsonBody(req, StockChangeRequest.class);
            logger.debug("[INVENTORY_INCREASE] Increase stock request data: {}", changeRequest);

            // 验证请求数据
            if (changeRequest.getProductId() == null || changeRequest.getQuantity() == null ||
                    changeRequest.getQuantity() <= 0) {
                ApiResponse<Void> response = ApiResponse.error("商品ID和增加数量不能为空，且增加数量必须大于0");
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, response);
                return;
            }

            // 调用服务层增加库存
            boolean success = inventoryService.increaseStock(changeRequest.getProductId(),
                    changeRequest.getQuantity(), changeRequest.getOrderId());

            if (success) {
                ApiResponse<Void> response = ApiResponse.success(null, "库存增加成功");
                sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
                logger.info("[INVENTORY_INCREASE] Stock increased successfully for product ID: {}",
                        changeRequest.getProductId());
            } else {
                ApiResponse<Void> response = ApiResponse.error("库存增加失败");
                sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
                logger.warn("[INVENTORY_INCREASE] Failed to increase stock for product: {}",
                        changeRequest.getProductId());
            }
        } catch (IOException e) {
            logger.error("[INVENTORY_INCREASE] Failed to parse request body: {}", e.getMessage());
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "请求格式错误", "INVALID_REQUEST_FORMAT");
        } catch (Exception e) {
            logger.error("[INVENTORY_INCREASE] Unexpected error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
        }
    }

    /**
     * 处理减少库存（销售出库）
     * 需要管理员或库存管理权限
     */
    @RequirePermission(value = {Permission.ADMIN, Permission.INVENTORY_MANAGE},
            logic = RequirePermission.LogicType.OR,
            message = "需要管理员或库存管理权限")
    private void handleDecreaseStock(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("[INVENTORY_DECREASE] Processing decrease stock request from IP: {}", getClientIp(req));

        try {
            // 验证Content-Type
            if (!isJsonContentType(req)) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "请求必须是JSON格式", "INVALID_CONTENT_TYPE");
                return;
            }

            // 解析请求体
            StockChangeRequest changeRequest = parseJsonBody(req, StockChangeRequest.class);
            logger.debug("[INVENTORY_DECREASE] Decrease stock request data: {}", changeRequest);

            // 验证请求数据
            if (changeRequest.getProductId() == null || changeRequest.getQuantity() == null ||
                    changeRequest.getQuantity() <= 0) {
                ApiResponse<Void> response = ApiResponse.error("商品ID和减少数量不能为空，且减少数量必须大于0");
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, response);
                return;
            }

            // 调用服务层减少库存
            boolean success = inventoryService.decreaseStock(changeRequest.getProductId(),
                    changeRequest.getQuantity(), changeRequest.getOrderId());

            if (success) {
                ApiResponse<Void> response = ApiResponse.success(null, "库存减少成功");
                sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
                logger.info("[INVENTORY_DECREASE] Stock decreased successfully for product ID: {}",
                        changeRequest.getProductId());
            } else {
                ApiResponse<Void> response = ApiResponse.error("库存减少失败或库存不足");
                sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
                logger.warn("[INVENTORY_DECREASE] Failed to decrease stock for product: {}",
                        changeRequest.getProductId());
            }
        } catch (IOException e) {
            logger.error("[INVENTORY_DECREASE] Failed to parse request body: {}", e.getMessage());
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "请求格式错误", "INVALID_REQUEST_FORMAT");
        } catch (Exception e) {
            logger.error("[INVENTORY_DECREASE] Unexpected error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
        }
    }

    /**
     * 处理获取库存变更日志
     * 需要管理员或库存管理权限
     */
    @RequirePermission(value = {Permission.ADMIN, Permission.INVENTORY_MANAGE},
            logic = RequirePermission.LogicType.OR,
            message = "需要管理员或库存管理权限")
    private void handleGetLogs(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("[INVENTORY_LOGS] Processing get inventory logs request from IP: {}", getClientIp(req));

        try {
            // 获取查询参数
            String productIdStr = req.getParameter("productId");
            String changeType = req.getParameter("changeType");
            String pageStr = req.getParameter("page");
            String pageSizeStr = req.getParameter("pageSize");

            Integer productId = null;
            Integer page = null;
            Integer pageSize = null;

            try {
                if (productIdStr != null && !productIdStr.trim().isEmpty()) {
                    productId = Integer.parseInt(productIdStr);
                }
                if (pageStr != null && !pageStr.trim().isEmpty()) {
                    page = Integer.parseInt(pageStr);
                }
                if (pageSizeStr != null && !pageSizeStr.trim().isEmpty()) {
                    pageSize = Integer.parseInt(pageSizeStr);
                }
            } catch (NumberFormatException e) {
                logger.warn("[INVENTORY_LOGS] Invalid number format in query parameters");
                ApiResponse<Void> response = ApiResponse.error("查询参数格式错误");
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, response);
                return;
            }

            // 设置默认值
            if (page == null || page < 1) page = 1;
            if (pageSize == null || pageSize < 1) pageSize = 20;

            // 调用服务层获取库存日志
            List<InventoryLog> logs = inventoryService.getInventoryLogs(productId, changeType, page, pageSize);

            if (logs == null) {
                logs = new ArrayList<>();
            }

            // 构建响应数据
            List<InventoryLogResponse> logResponses = new ArrayList<>();
            for (InventoryLog log : logs) {
                InventoryLogResponse logResponse = new InventoryLogResponse();
                logResponse.setId(log.getId());
                logResponse.setProductId(log.getProductId());
                logResponse.setChangeType(log.getChangeType());
                logResponse.setQuantity(log.getQuantity());
                logResponse.setBeforeStock(log.getBeforeStock());
                logResponse.setAfterStock(log.getAfterStock());
                logResponse.setOrderId(log.getOrderId());
                logResponse.setCreatedAt(log.getCreatedAt());
                logResponses.add(logResponse);
            }

            long totalCount = inventoryService.countLogs();

            Map<String, Object> data = new HashMap<>();
            data.put("logs", logResponses);
            data.put("currentPage", page);
            data.put("pageSize", pageSize);
            data.put("totalCount", totalCount); // 注意：这里应该查询总数，简化处理

            ApiResponse<Map<String, Object>> response = ApiResponse.success(
                    data, "库存变更日志获取成功"
            );
            sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
            logger.info("[INVENTORY_LOGS] Inventory logs retrieved successfully. Count: {}", totalCount);

        } catch (Exception e) {
            logger.error("[INVENTORY_LOGS] Unexpected error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
        }
    }

    /**
     * 处理获取低库存预警
     * 需要管理员或库存管理权限
     */
    @RequirePermission(value = {Permission.ADMIN, Permission.INVENTORY_MANAGE},
            logic = RequirePermission.LogicType.OR,
            message = "需要管理员或库存管理权限")
    private void handleGetLowStock(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("[INVENTORY_LOW_STOCK] Processing get low stock request from IP: {}", getClientIp(req));

        try {
            // 获取查询参数
            String thresholdStr = req.getParameter("threshold");

            int threshold = 10; // 默认阈值
            if (thresholdStr != null && !thresholdStr.trim().isEmpty()) {
                try {
                    threshold = Integer.parseInt(thresholdStr);
                } catch (NumberFormatException e) {
                    logger.warn("[INVENTORY_LOW_STOCK] Invalid threshold format, using default: {}", threshold);
                }
            }

            // 调用服务层获取低库存商品
            List<Integer> lowStockProductIds = inventoryService.getLowStockProducts(threshold);

            if (lowStockProductIds == null) {
                lowStockProductIds = new ArrayList<>();
            }

            // 获取商品详情
            List<ProductResponse> lowStockProducts = new ArrayList<>();
            for (Integer productId : lowStockProductIds) {
                Product product = inventoryService.getProductById(productId);
                if (product != null) {
                    ProductResponse productResponse = new ProductResponse();
                    productResponse.setId(product.getId());
                    productResponse.setName(product.getName());
                    productResponse.setCategory(product.getCategory());
                    productResponse.setPrice(product.getPrice());
                    productResponse.setStock(product.getStock());
                    productResponse.setStatus(product.getStatus());
                    productResponse.setImage(product.getImage());
                    productResponse.setCreatedAt(product.getCreatedAt());
                    lowStockProducts.add(productResponse);
                }
            }

            Map<String, Object> data = new HashMap<>();
            data.put("threshold", threshold);
            data.put("lowStockProducts", lowStockProducts);
            data.put("count", lowStockProducts.size());

            ApiResponse<Map<String, Object>> response = ApiResponse.success(
                    data, "低库存预警获取成功"
            );
            sendJsonResponse(resp, HttpServletResponse.SC_OK, response);
            logger.info("[INVENTORY_LOW_STOCK] Low stock products retrieved successfully. Count: {}, Threshold: {}",
                    lowStockProducts.size(), threshold);

        } catch (Exception e) {
            logger.error("[INVENTORY_LOW_STOCK] Unexpected error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误", "INTERNAL_SERVER_ERROR");
        }
    }

    /**
     * 检查请求是否为JSON格式
     */
    private boolean isJsonContentType(HttpServletRequest req) {
        String contentType = req.getContentType();
        return contentType != null && contentType.contains("application/json");
    }
}
