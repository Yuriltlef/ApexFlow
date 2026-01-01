package com.apex.util;


import java.lang.annotation.*;

/**
 * 统一权限验证注解
 * 用于标注需要特定权限才能访问的API方法
 * 废弃@RequireAdmin，所有权限验证使用此注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 需要的权限类型数组
     * 用户需要拥有其中至少一种权限才能访问
     * 默认值：空数组（表示不需要任何权限）
     */
    Permission[] value() default {};

    /**
     * 是否需要所有权限
     * 如果为true，用户需要拥有所有指定的权限才能访问
     * 如果为false，用户只需要拥有其中一种权限即可访问
     * 默认值：false
     */
    boolean requireAll() default false;

    /**
     * 是否允许管理员访问
     * 如果为true，拥有ADMIN权限的用户可以直接访问
     * 如果为false，即使是管理员也需要检查特定权限
     * 默认值：true（管理员默认拥有所有权限）
     */
    boolean allowAdmin() default true;

    /**
     * 错误消息（当权限不足时返回）
     * 默认值："权限不足"
     */
    String message() default "权限不足";

    /**
     * 逻辑操作类型
     * OR: 只需要满足其中一个权限
     * AND: 需要满足所有权限
     * 默认值：OR
     */
    LogicType logic() default LogicType.OR;

    /**
     * 逻辑类型枚举
     */
    enum LogicType {
        OR,   // 或逻辑
        AND   // 与逻辑
    }
}
