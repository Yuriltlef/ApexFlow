// ReviewServlet.java - 评价管理控制器
package com.apex.core.controller;

import com.apex.api.BaseServlet;
import com.apex.core.dto.*;
import com.apex.core.model.Review;
import com.apex.core.service.ReviewService;
import com.apex.util.RequirePermission;
import com.apex.util.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 评价管理API接口
 * 简洁实现，仅核心功能
 */
@WebServlet("/api/review/*")
public class ReviewServlet extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(ReviewServlet.class);
    private final ReviewService reviewService = new ReviewService();

    @Override
    @RequirePermission(value = {Permission.ADMIN, Permission.ORDER_MANAGE},
            logic = RequirePermission.LogicType.OR)
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            handleCreate(req, resp);
        } else {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                    "API不存在", "API_NOT_FOUND");
        }
    }

    @Override
    @RequirePermission(value = {Permission.ADMIN, Permission.ORDER_MANAGE},
            logic = RequirePermission.LogicType.OR)
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            handleList(req, resp);
        } else if (pathInfo.matches("/\\d+")) { // 数字ID
            handleGetDetail(req, resp);
        } else if (pathInfo.matches("/stats/\\d+")) { // stats/数字
            handleGetStats(req, resp);
        } else {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                    "API不存在", "API_NOT_FOUND");
        }
    }

    @Override
    @RequirePermission(value = {Permission.ADMIN, Permission.ORDER_MANAGE},
            logic = RequirePermission.LogicType.OR)
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.matches("/\\d+")) {
            handleDelete(req, resp);
        } else {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                    "API不存在", "API_NOT_FOUND");
        }
    }

    @Override
    @RequirePermission(value = {Permission.ADMIN, Permission.ORDER_MANAGE},
            logic = RequirePermission.LogicType.OR)
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPut(req, resp);
    }

    /**
     * 创建评价
     */
    private void handleCreate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("[REVIEW] Creating review from IP: {}", getClientIp(req));

        try {
            // 验证Content-Type
            String contentType = req.getContentType();
            if (contentType == null || !contentType.contains("application/json")) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "请求必须是JSON格式", "INVALID_CONTENT_TYPE");
                return;
            }

            // 解析请求体
            ReviewCreateRequest request = parseJsonBody(req, ReviewCreateRequest.class);

            // 验证必要字段
            if (request.getOrderId() == null || request.getProductId() == null ||
                    request.getUserId() == null || request.getRating() == null) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "缺少必要字段", "MISSING_FIELDS");
                return;
            }

            // 调用服务层
            ApiResponse<Review> result = reviewService.create(request);

            // 返回响应
            if (result.isSuccess()) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, result);
                logger.info("[REVIEW] Review created successfully");
            } else {
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, result);
                logger.warn("[REVIEW] Review creation failed: {}", result.getMessage());
            }

        } catch (IOException e) {
            logger.error("[REVIEW] Failed to parse request: {}", e.getMessage());
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "请求格式错误", "INVALID_FORMAT");
        } catch (Exception e) {
            logger.error("[REVIEW] Server error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器错误", "SERVER_ERROR");
        }
    }

    /**
     * 获取评价列表
     */
    private void handleList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("[REVIEW] Querying review list");

        try {
            // 构建查询参数
            ReviewQueryRequest query = new ReviewQueryRequest();

            String productId = req.getParameter("productId");
            String userId = req.getParameter("userId");
            String page = req.getParameter("page");
            String pageSize = req.getParameter("pageSize");

            if (productId != null) {
                query.setProductId(Integer.parseInt(productId));
            }
            if (userId != null) {
                query.setUserId(Integer.parseInt(userId));
            }
            if (page != null) {
                query.setPage(Integer.parseInt(page));
            }
            if (pageSize != null) {
                query.setPageSize(Integer.parseInt(pageSize));
            }

            // 调用服务层
            ApiResponse<ReviewListResponse> result = reviewService.list(query);

            // 返回响应
            if (result.isSuccess()) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, result);
                logger.debug("[REVIEW] Retrieved {} reviews", result.getData().getReviews().size());
            } else {
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, result);
            }

        } catch (NumberFormatException e) {
            logger.error("[REVIEW] Invalid parameter format: {}", e.getMessage());
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "参数格式错误", "INVALID_PARAM");
        } catch (Exception e) {
            logger.error("[REVIEW] Server error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器错误", "SERVER_ERROR");
        }
    }

    /**
     * 获取评价详情
     */
    private void handleGetDetail(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // 提取ID
            String path = req.getPathInfo();
            Integer reviewId = Integer.parseInt(path.substring(1));

            logger.debug("[REVIEW] Getting review details: {}", reviewId);

            // 调用服务层
            ApiResponse<Review> result = reviewService.get(reviewId);

            // 返回响应
            if (result.isSuccess()) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, result);
            } else {
                sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND, result);
            }

        } catch (NumberFormatException e) {
            logger.error("[REVIEW] Invalid review ID format");
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "无效的评价ID", "INVALID_ID");
        } catch (Exception e) {
            logger.error("[REVIEW] Server error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器错误", "SERVER_ERROR");
        }
    }

    /**
     * 获取评价统计
     */
    private void handleGetStats(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // 提取商品ID
            String path = req.getPathInfo();
            Integer productId = Integer.parseInt(path.substring(7)); // 去掉"/stats/"

            logger.debug("[REVIEW] Getting stats for product: {}", productId);

            // 调用服务层
            ApiResponse<ReviewStats> result = reviewService.getStats(productId);

            // 返回响应
            sendJsonResponse(resp, result.isSuccess() ?
                    HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST, result);

        } catch (NumberFormatException e) {
            logger.error("[REVIEW] Invalid product ID format");
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "无效的商品ID", "INVALID_ID");
        } catch (Exception e) {
            logger.error("[REVIEW] Server error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器错误", "SERVER_ERROR");
        }
    }

    /**
     * 删除评价
     */
    private void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // 提取ID
            String path = req.getPathInfo();
            Integer reviewId = Integer.parseInt(path.substring(1));

            logger.warn("[REVIEW] Deleting review: {}", reviewId);

            // 调用服务层
            ApiResponse<Void> result = reviewService.delete(reviewId);

            // 返回响应
            sendJsonResponse(resp, result.isSuccess() ?
                    HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST, result);

        } catch (NumberFormatException e) {
            logger.error("[REVIEW] Invalid review ID format");
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "无效的评价ID", "INVALID_ID");
        } catch (Exception e) {
            logger.error("[REVIEW] Server error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器错误", "SERVER_ERROR");
        }
    }
}