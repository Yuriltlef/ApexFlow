package com.apex.core.dao;

import com.apex.core.model.Review;
import com.apex.core.dto.RatingDistribution;

import java.util.List;

/**
 * 商品评价数据访问对象接口
 * 定义评价的增删改查及评分统计操作
 */
public interface IReviewDAO {

    /**
     * 创建评价
     * @param review 评价对象
     * @return 创建成功返回true，失败返回false
     */
    boolean create(Review review);

    /**
     * 根据ID查询评价
     * @param id 评价ID
     * @return 对应的评价对象
     */
    Review findById(Integer id);

    /**
     * 根据订单号查询评价
     * @param orderId 订单ID
     * @return 订单对应的评价
     */
    Review findByOrderId(String orderId);

    /**
     * 根据商品ID查询评价（分页）
     * @param productId 商品ID
     * @param page 页码（从1开始）
     * @param pageSize 每页记录数
     * @return 商品的所有评价列表
     */
    List<Review> findByProductId(Integer productId, int page, int pageSize);

    /**
     * 根据用户ID查询评价（分页）
     * @param userId 用户ID
     * @param page 页码（从1开始）
     * @param pageSize 每页记录数
     * @return 用户的所有评价列表
     */
    List<Review> findByUserId(Integer userId, int page, int pageSize);

    /**
     * 更新评价
     * @param review 包含更新信息的评价对象
     * @return 更新成功返回true，失败返回false
     */
    boolean update(Review review);

    /**
     * 删除评价
     * @param id 要删除的评价ID
     * @return 删除成功返回true，失败返回false
     */
    boolean delete(Integer id);

    /**
     * 获取商品平均评分
     * @param productId 商品ID
     * @return 商品的平均评分
     */
    Double getAverageRating(Integer productId);

    /**
     * 获取商品评分分布
     * @param productId 商品ID
     * @return 包含各星级评分数量的分布对象
     */
    RatingDistribution getRatingDistribution(Integer productId);

    /**
     * 获取带图片的评价
     * @param productId 商品ID
     * @param limit 查询记录数限制
     * @return 带图片的评价列表
     */
    List<Review> findReviewsWithImages(Integer productId, int limit);

    /**
     * 获取最新评价
     * @param limit 查询记录数限制
     * @return 最新的评价列表
     */
    List<Review> findLatestReviews(int limit);

    /**
     * 统计商品评价数量
     * @param productId 商品ID
     * @return 商品的评价总数
     */
    Integer countByProductId(Integer productId);

    /**
     * 统计评论总数
     * @return 订单总数
     */
    long count();
}
