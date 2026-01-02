// ReviewService.java - 简洁版服务层
package com.apex.core.service;

import com.apex.core.dao.IReviewDAO;
import com.apex.core.dao.ReviewDAO;
import com.apex.core.dto.*;
import com.apex.core.model.Review;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评价管理服务层
 * 简洁实现，仅提供核心业务方法
 */
public class ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);
    private final IReviewDAO reviewDAO = new ReviewDAO();

    /**
     * 创建评价（简洁版）
     */
    public ApiResponse<Review> create(ReviewCreateRequest request) {
        logger.info("Creating review for order: {}", request.getOrderId());

        try {
            // 检查订单是否已评价
            Review existing = reviewDAO.findByOrderId(request.getOrderId());
            if (existing != null) {
                return ApiResponse.error("该订单已评价", "REVIEW_EXISTS");
            }

            // 创建新评价
            Review review = new Review();
            review.setOrderId(request.getOrderId());
            review.setProductId(request.getProductId());
            review.setUserId(request.getUserId());
            review.setRating(request.getRating());
            review.setContent(request.getContent());
            review.setImages(request.getImages());
            review.setAnonymous(request.getAnonymous());
            review.setCreatedAt(LocalDateTime.now());

            boolean success = reviewDAO.create(review);
            return success ?
                    ApiResponse.success(review, "评价创建成功") :
                    ApiResponse.error("评价创建失败", "CREATE_FAILED");

        } catch (Exception e) {
            logger.error("Failed to create review: {}", e.getMessage());
            return ApiResponse.error("服务器错误", "INTERNAL_ERROR");
        }
    }

    /**
     * 获取评价列表（简洁版）
     */
    public ApiResponse<ReviewListResponse> list(ReviewQueryRequest query) {
        logger.debug("Querying reviews with params: {}", query);

        try {
            List<Review> reviews;
            int total;

            // 根据查询类型获取数据
            if (query.getProductId() != null) {
                reviews = reviewDAO.findByProductId(
                        query.getProductId(),
                        query.getPage(),
                        query.getPageSize()
                );
                total = reviewDAO.countByProductId(query.getProductId());
            } else if (query.getUserId() != null) {
                reviews = reviewDAO.findByUserId(
                        query.getUserId(),
                        query.getPage(),
                        query.getPageSize()
                );
                total = reviews.size(); // 简化处理
            } else {
                reviews = reviewDAO.findLatestReviews(query.getPageSize());
                total = reviews.size();
            }

            // 构建响应
            ReviewListResponse response = new ReviewListResponse();
            response.setReviews(reviews);
            response.setTotal(total);
            response.setPage(query.getPage());
            response.setPageSize(query.getPageSize());

            return ApiResponse.success(response, "获取成功");

        } catch (Exception e) {
            logger.error("Failed to query reviews: {}", e.getMessage());
            return ApiResponse.error("查询失败", "QUERY_FAILED");
        }
    }

    /**
     * 获取评价详情（简洁版）
     */
    public ApiResponse<Review> get(Integer reviewId) {
        logger.debug("Getting review details: {}", reviewId);

        try {
            Review review = reviewDAO.findById(reviewId);
            return review != null ?
                    ApiResponse.success(review, "获取成功") :
                    ApiResponse.error("评价不存在", "NOT_FOUND");

        } catch (Exception e) {
            logger.error("Failed to get review: {}", e.getMessage());
            return ApiResponse.error("查询失败", "QUERY_FAILED");
        }
    }

    /**
     * 删除评价（简洁版）
     */
    public ApiResponse<Void> delete(Integer reviewId) {
        logger.warn("Deleting review: {}", reviewId);

        try {
            boolean success = reviewDAO.delete(reviewId);
            return success ?
                    ApiResponse.success(null, "删除成功") :
                    ApiResponse.error("删除失败", "DELETE_FAILED");

        } catch (Exception e) {
            logger.error("Failed to delete review: {}", e.getMessage());
            return ApiResponse.error("删除失败", "DELETE_FAILED");
        }
    }

    /**
     * 获取商品评分统计（简洁版）
     */
    public ApiResponse<ReviewStats> getStats(Integer productId) {
        logger.debug("Getting stats for product: {}", productId);

        try {
            ReviewStats stats = new ReviewStats();
            stats.setAverageRating(reviewDAO.getAverageRating(productId));
            stats.setRatingDistribution(reviewDAO.getRatingDistribution(productId));
            stats.setTotalReviews(reviewDAO.countByProductId(productId));

            return ApiResponse.success(stats, "获取成功");

        } catch (Exception e) {
            logger.error("Failed to get review stats: {}", e.getMessage());
            return ApiResponse.error("获取失败", "QUERY_FAILED");
        }
    }
}