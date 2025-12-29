package com.apex.core.dao;

import com.apex.core.model.SystemUser;

import java.util.List;

/**
 * 用户数据访问对象接口
 * 定义系统用户的增删改查及权限管理操作
 */
public interface IUserDAO {

    /**
     * 创建新用户
     * @param user 用户对象
     * @return 创建成功返回true，失败返回false
     */
    boolean create(SystemUser user);

    /**
     * 根据ID查找用户
     * @param id 用户ID
     * @return 对应的用户对象
     */
    SystemUser findById(Integer id);

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 对应的用户对象
     */
    SystemUser findByUsername(String username);

    /**
     * 根据邮箱查找用户
     * @param email 邮箱地址
     * @return 对应的用户对象
     */
    SystemUser findByEmail(String email);

    /**
     * 更新用户信息
     * @param user 包含更新信息的用户对象
     * @return 更新成功返回true，失败返回false
     */
    boolean update(SystemUser user);

    /**
     * 更新用户最后登录时间
     * @param userId 用户ID
     * @return 更新成功返回true，失败返回false
     */
    boolean updateLastLoginTime(Integer userId);

    /**
     * 更新用户状态
     * @param userId 用户ID
     * @param status 新状态
     * @return 更新成功返回true，失败返回false
     */
    boolean updateStatus(Integer userId, Integer status);

    /**
     * 删除用户
     * @param userId 要删除的用户ID
     * @return 删除成功返回true，失败返回false
     */
    boolean delete(Integer userId);

    /**
     * 获取所有用户（分页）
     * @param page 页码（从1开始）
     * @param pageSize 每页记录数
     * @return 分页后的用户列表
     */
    List<SystemUser> findAll(int page, int pageSize);

    /**
     * 根据状态筛选用户（分页）
     * @param status 用户状态
     * @param page 页码（从1开始）
     * @param pageSize 每页记录数
     * @return 指定状态的用户列表
     */
    List<SystemUser> findByStatus(Integer status, int page, int pageSize);

    /**
     * 搜索用户（按用户名、姓名、邮箱、电话）
     * @param keyword 搜索关键词
     * @param page 页码（从1开始）
     * @param pageSize 每页记录数
     * @return 包含关键词的用户列表
     */
    List<SystemUser> search(String keyword, int page, int pageSize);

    /**
     * 统计用户总数
     * @return 用户总数
     */
    long count();

    /**
     * 统计活跃用户数
     * @return 活跃用户数（状态为1的用户）
     */
    long countActive();

    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 存在返回true，不存在返回false
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     * @param email 邮箱地址
     * @return 存在返回true，不存在返回false
     */
    boolean existsByEmail(String email);
}
