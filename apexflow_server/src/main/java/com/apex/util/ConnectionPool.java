package com.apex.util;

import com.apex.config.ApexFlowDBInfo;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * HikariCP Database Connection Pool Manager with comprehensive logging
 */
public class ConnectionPool {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);
    private static final Logger sqlLogger = LoggerFactory.getLogger("SQL_LOGGER");
    private static volatile HikariDataSource dataSource;
    private static volatile boolean initialized = false;

    /**
     * Get or initialize the data source with lazy initialization
     */
    private static synchronized DataSource getDataSource() {
        if (dataSource == null) {
            try {
                logger.info("Initializing HikariCP connection pool...");

                // ========== 关键修复：显式加载MySQL驱动 ==========
                try {
                    // 对于MySQL 8.x
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    logger.info("MySQL JDBC Driver loaded successfully.");
                } catch (ClassNotFoundException e) {
                    // 如果MySQL 8.x驱动不存在，尝试加载旧版驱动
                    try {
                        Class.forName("com.mysql.jdbc.Driver");
                        logger.info("MySQL legacy JDBC Driver loaded.");
                    } catch (ClassNotFoundException e2) {
                        logger.error("MySQL JDBC Driver not found in classpath", e2);
                        throw new RuntimeException("MySQL JDBC Driver not found", e2);
                    }
                }
                // ========== 修复结束 ==========

                HikariConfig config = getConfig();

                // Create data source
                dataSource = new HikariDataSource(config);
                initialized = true;

                logger.info("HikariCP connection pool initialized successfully");
                logger.info("Connection Pool Name: {}", config.getPoolName());
                logger.info("Maximum Pool Size: {}", config.getMaximumPoolSize());
                logger.info("JDBC URL: {}", maskPassword(config.getJdbcUrl()));

            } catch (Exception e) {
                logger.error("Failed to initialize HikariCP connection pool", e);
                throw new RuntimeException("Database connection pool initialization failed", e);
            }
        }
        return dataSource;
    }

    private static HikariConfig getConfig() {
        HikariConfig config = getHikariConfig();

        // Performance optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        return config;
    }

    private static HikariConfig getHikariConfig() {
        HikariConfig config = new HikariConfig();

        // Database connection configuration
        config.setJdbcUrl(ApexFlowDBInfo.getURL());
        config.setUsername(ApexFlowDBInfo.getName());
        config.setPassword(ApexFlowDBInfo.getPassword());

        // Connection pool tuning
        config.setPoolName("ApexFlow-HikariPool");
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setConnectionTestQuery("SELECT 1");
        return config;
    }

    /**
     * Get a database connection from the pool
     */
    public static Connection getConnection() throws SQLException {
        long startTime = System.currentTimeMillis();

        try {
            Connection connection = getDataSource().getConnection();
            long duration = System.currentTimeMillis() - startTime;

            sqlLogger.debug("Acquired database connection in {} ms [Active: {}, Idle: {}, Total: {}]",
                    duration,
                    getActiveConnections(),
                    getIdleConnections(),
                    getTotalConnections());

            return new LoggingConnection(connection, sqlLogger);

        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Failed to acquire database connection after {} ms: {}", duration, e.getMessage());
            throw e;
        }
    }

    /**
     * Close the connection pool (call on application shutdown)
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            try {
                logger.info("Shutting down HikariCP connection pool...");
                dataSource.close();
                logger.info("HikariCP connection pool shutdown completed");
            } catch (Exception e) {
                logger.error("Error during connection pool shutdown", e);
            }
        }
    }

    /**
     * Get pool statistics for monitoring
     */
    public static PoolStats getPoolStats() {
        if (dataSource == null) {
            return new PoolStats(0, 0, 0, 0, 0);
        }

        HikariPool pool = (HikariPool) dataSource.getHikariPoolMXBean();
        return new PoolStats(
                pool.getActiveConnections(),
                pool.getIdleConnections(),
                pool.getTotalConnections(),
                dataSource.getConnectionTimeout(),
                dataSource.getIdleTimeout()
        );
    }

    /**
     * Helper methods for monitoring
     */
    public static int getActiveConnections() {
        return dataSource != null ? dataSource.getHikariPoolMXBean().getActiveConnections() : 0;
    }

    public static int getIdleConnections() {
        return dataSource != null ? dataSource.getHikariPoolMXBean().getIdleConnections() : 0;
    }

    public static int getTotalConnections() {
        return dataSource != null ? dataSource.getHikariPoolMXBean().getTotalConnections() : 0;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Mask password in JDBC URL for logging
     */
    private static String maskPassword(String jdbcUrl) {
        return jdbcUrl.replaceAll("password=.*?(&|$)", "password=******$1");
    }

    /**
     * Inner class for pool statistics
     */
    public static class PoolStats {
        private final int activeConnections;
        private final int idleConnections;
        private final int totalConnections;
        private final long connectionTimeout;
        private final long idleTimeout;

        public PoolStats(int activeConnections, int idleConnections, int totalConnections,
                         long connectionTimeout, long idleTimeout) {
            this.activeConnections = activeConnections;
            this.idleConnections = idleConnections;
            this.totalConnections = totalConnections;
            this.connectionTimeout = connectionTimeout;
            this.idleTimeout = idleTimeout;
        }

        @Override
        public String toString() {
            return String.format("PoolStats{active=%d, idle=%d, total=%d, connTimeout=%dms, idleTimeout=%dms}",
                    activeConnections, idleConnections, totalConnections, connectionTimeout, idleTimeout);
        }
    }
}
