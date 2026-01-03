package com.apex.config;

/**
 * 数据库连接配置信息
 * 支持MySQL和H2内存数据库（用于测试）
 */
public class ApexFlowDBInfo {
    // 默认使用H2内存数据库进行测试
    private static final boolean USE_H2 = Boolean.parseBoolean(System.getProperty("apexflow.test.h2", "false"));

    // H2内存数据库配置
    private static final String H2_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL";
    private static final String H2_NAME = "sa";
    private static final String H2_PASSWORD = "";

    // MySQL数据库配置
    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/apexflow";
    private static final String MYSQL_NAME = "root";
    private static final String MYSQL_PASSWORD = "20050823Yzj";

    /**
     * 根据配置返回数据库URL
     */
    static public String getURL() {
        return USE_H2 ? H2_URL : MYSQL_URL;
    }

    /**
     * 根据配置返回数据库用户名
     */
    static public String getName() {
        return USE_H2 ? H2_NAME : MYSQL_NAME;
    }

    /**
     * 根据配置返回数据库密码
     */
    static public String getPassword() {
        return USE_H2 ? H2_PASSWORD : MYSQL_PASSWORD;
    }

    /**
     * 检查当前是否使用H2内存数据库
     */
    static public boolean isUsingH2() {
        return USE_H2;
    }
}
