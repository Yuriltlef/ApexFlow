package com.apex.core.dao;

import com.apex.core.model.Product;

import java.util.List;

/**
 * 商品数据访问对象接口
 * 定义商品的增删改查及库存管理操作
 */
public interface IProductDAO {

    /**
     * 创建商品
     * @param product 商品对象
     * @return 创建成功返回true，失败返回false
     */
    boolean create(Product product);

    /**
     * 根据ID查询商品
     * @param id 商品ID
     * @return 对应的商品对象
     */
    Product findById(Integer id);

    /**
     * 更新商品信息
     * @param product 包含更新信息的商品对象
     * @return 更新成功返回true，失败返回false
     */
    boolean update(Product product);

    /**
     * 删除商品
     * @param id 要删除的商品ID
     * @return 删除成功返回true，失败返回false
     */
    boolean delete(Integer id);

    /**
     * 查询所有商品（分页）
     * @param page 页码（从1开始）
     * @param pageSize 每页记录数
     * @return 分页后的商品列表
     */
    List<Product> findAll(int page, int pageSize);

    /**
     * 根据分类查询商品
     * @param category 商品分类
     * @param page 页码（从1开始）
     * @param pageSize 每页记录数
     * @return 指定分类的商品列表
     */
    List<Product> findByCategory(String category, int page, int pageSize);

    /**
     * 根据状态查询商品
     * @param status 商品状态
     * @param page 页码（从1开始）
     * @param pageSize 每页记录数
     * @return 指定状态的商品列表
     */
    List<Product> findByStatus(Integer status, int page, int pageSize);

    /**
     * 根据名称搜索商品
     * @param keyword 搜索关键词
     * @param page 页码（从1开始）
     * @param pageSize 每页记录数
     * @return 包含关键词的商品列表
     */
    List<Product> searchByName(String keyword, int page, int pageSize);

    /**
     * 更新商品库存
     * @param id 商品ID
     * @param newStock 新库存数量
     * @return 更新成功返回true，失败返回false
     */
    boolean updateStock(Integer id, Integer newStock);

    /**
     * 增加商品库存
     * @param id 商品ID
     * @param quantity 增加的数量
     * @return 更新成功返回true，失败返回false
     */
    boolean increaseStock(Integer id, Integer quantity);

    /**
     * 减少商品库存
     * @param id 商品ID
     * @param quantity 减少的数量
     * @return 更新成功返回true，失败返回false
     */
    boolean decreaseStock(Integer id, Integer quantity);

    /**
     * 更新商品状态
     * @param id 商品ID
     * @param status 新状态
     * @return 更新成功返回true，失败返回false
     */
    boolean updateStatus(Integer id, Integer status);

    /**
     * 统计商品总数
     * @return 商品总数
     */
    long count();

    /**
     * 统计分类下的商品数量
     * @param category 商品分类
     * @return 该分类下的商品数量
     */
    long countByCategory(String category);
}
