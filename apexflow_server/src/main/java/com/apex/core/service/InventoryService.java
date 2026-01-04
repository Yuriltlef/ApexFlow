package com.apex.core.service;

import com.apex.core.dao.IProductDAO;
import com.apex.core.dao.IInventoryLogDAO;
import com.apex.core.model.Product;
import com.apex.core.model.InventoryLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存管理服务层
 * 负责处理商品库存相关的业务逻辑，包括库存变更、商品管理、库存日志等
 */
public class InventoryService {
    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    private final IProductDAO productDAO;
    private final IInventoryLogDAO inventoryLogDAO;

    public InventoryService(IProductDAO productDAO, IInventoryLogDAO inventoryLogDAO) {
        this.productDAO = productDAO;
        this.inventoryLogDAO = inventoryLogDAO;
        logger.info("[INVENTORY_SERVICE] InventoryService initialized");
    }

    /**
     * 创建商品（需要库存管理权限）
     * @param product 商品信息
     * @return 创建成功返回true
     */
    public boolean createProduct(Product product) {
        logger.info("[INVENTORY_SERVICE] Creating new product: {}", product.getName());

        try {
            // 设置默认值
            if (product.getStock() == null) {
                product.setStock(0);
            }
            if (product.getStatus() == null) {
                product.setStatus(1); // 默认上架
            }
            product.setCreatedAt(LocalDateTime.now());

            boolean success = productDAO.create(product);

            if (success) {
                // 创建初始库存日志
                InventoryLog log = new InventoryLog();
                log.setProductId(product.getId());
                log.setChangeType("purchase"); // 采购入库
                log.setQuantity(product.getStock() != null ? product.getStock() : 0);
                log.setBeforeStock(0);
                log.setAfterStock(product.getStock() != null ? product.getStock() : 0);
                log.setCreatedAt(LocalDateTime.now());

                inventoryLogDAO.create(log);
                logger.info("[INVENTORY_SERVICE] Product created successfully. ID: {}, Stock: {}",
                        product.getId(), product.getStock());
            }

            return success;
        } catch (Exception e) {
            logger.error("[INVENTORY_SERVICE] Failed to create product: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 更新商品信息（需要库存管理权限）
     * @param productId 商品ID
     * @param product 更新后的商品信息
     * @return 更新成功返回true
     */
    public boolean updateProduct(Integer productId, Product product) {
        logger.info("[INVENTORY_SERVICE] Updating product ID: {}", productId);

        try {
            product.setId(productId);
            return productDAO.update(product);
        } catch (Exception e) {
            logger.error("[INVENTORY_SERVICE] Failed to update product ID {}: {}", productId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 安全删除商品（需要库存管理权限）
     * 先删除库存日志，再删除商品
     * @param productId 商品ID
     * @return 删除成功返回true
     */
    public boolean deleteProduct(Integer productId) {
        logger.info("[INVENTORY_SERVICE] Deleting product ID: {}", productId);

        try {
            // 注意：由于外键约束，我们不能直接删除商品
            // 需要先删除关联的库存日志，但库存日志有product_id外键约束
            // 需要检查是否有订单项引用此商品

            // 由于数据库外键约束，直接删除会失败
            // 我们需要在业务层处理级联删除逻辑
            // 这里使用更新状态为下架代替物理删除
            return productDAO.updateStatus(productId, 0); // 下架商品
        } catch (Exception e) {
            logger.error("[INVENTORY_SERVICE] Failed to delete product ID {}: {}", productId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 根据ID获取商品详情
     * @param productId 商品ID
     * @return 商品对象
     */
    public Product getProductById(Integer productId) {
        logger.debug("[INVENTORY_SERVICE] Getting product details for ID: {}", productId);

        try {
            return productDAO.findById(productId);
        } catch (Exception e) {
            logger.error("[INVENTORY_SERVICE] Failed to get product ID {}: {}", productId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取商品列表（分页）
     * @param page 页码
     * @param pageSize 每页大小
     * @return 商品列表
     */
    public List<Product> getProducts(int page, int pageSize) {
        logger.debug("[INVENTORY_SERVICE] Getting products page: {}, pageSize: {}", page, pageSize);

        try {
            return productDAO.findAll(page, pageSize);
        } catch (Exception e) {
            logger.error("[INVENTORY_SERVICE] Failed to get products: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据分类获取商品
     * @param category 分类
     * @param page 页码
     * @param pageSize 每页大小
     * @return 商品列表
     */
    public List<Product> getProductsByCategory(String category, int page, int pageSize) {
        logger.debug("[INVENTORY_SERVICE] Getting products by category: {}", category);

        try {
            return productDAO.findByCategory(category, page, pageSize);
        } catch (Exception e) {
            logger.error("[INVENTORY_SERVICE] Failed to get products by category {}: {}",
                    category, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 搜索商品
     * @param keyword 关键词
     * @param page 页码
     * @param pageSize 每页大小
     * @return 商品列表
     */
    public List<Product> searchProducts(String keyword, int page, int pageSize) {
        logger.debug("[INVENTORY_SERVICE] Searching products with keyword: {}", keyword);

        try {
            return productDAO.searchByName(keyword, page, pageSize);
        } catch (Exception e) {
            logger.error("[INVENTORY_SERVICE] Failed to search products: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 增加库存（采购入库）
     * @param productId 商品ID
     * @param quantity 增加数量
     * @param orderId 订单ID（可为空）
     * @return 操作成功返回true
     */
    public boolean increaseStock(Integer productId, Integer quantity, String orderId) {
        logger.info("[INVENTORY_SERVICE] Increasing stock for product ID: {}, quantity: {}", productId, quantity);

        try {
            Product product = productDAO.findById(productId);
            if (product == null) {
                logger.warn("[INVENTORY_SERVICE] Product not found: {}", productId);
                return false;
            }

            int beforeStock = product.getStock() != null ? product.getStock() : 0;
            boolean success = productDAO.increaseStock(productId, quantity);

            if (success) {
                // 记录库存日志
                InventoryLog log = new InventoryLog();
                log.setProductId(productId);
                log.setChangeType("purchase"); // 采购入库
                log.setQuantity(quantity);
                log.setBeforeStock(beforeStock);
                log.setAfterStock(beforeStock + quantity);
                log.setOrderId(orderId);
                log.setCreatedAt(LocalDateTime.now());

                inventoryLogDAO.create(log);
                logger.info("[INVENTORY_SERVICE] Stock increased. Product: {}, Before: {}, After: {}",
                        productId, beforeStock, beforeStock + quantity);
            }

            return success;
        } catch (Exception e) {
            logger.error("[INVENTORY_SERVICE] Failed to increase stock for product {}: {}",
                    productId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 减少库存（销售出库）
     * @param productId 商品ID
     * @param quantity 减少数量
     * @param orderId 订单ID
     * @return 操作成功返回true
     */
    public boolean decreaseStock(Integer productId, Integer quantity, String orderId) {
        logger.info("[INVENTORY_SERVICE] Decreasing stock for product ID: {}, quantity: {}, order: {}",
                productId, quantity, orderId);

        try {
            Product product = productDAO.findById(productId);
            if (product == null) {
                logger.warn("[INVENTORY_SERVICE] Product not found: {}", productId);
                return false;
            }

            // 检查库存是否充足
            int currentStock = product.getStock() != null ? product.getStock() : 0;
            if (currentStock < quantity) {
                logger.warn("[INVENTORY_SERVICE] Insufficient stock. Product: {}, Current: {}, Required: {}",
                        productId, currentStock, quantity);
                return false;
            }

            int beforeStock = currentStock;
            boolean success = productDAO.decreaseStock(productId, quantity);

            if (success) {
                // 记录库存日志
                InventoryLog log = new InventoryLog();
                log.setProductId(productId);
                log.setChangeType("sale"); // 销售出库
                log.setQuantity(-quantity); // 负数表示减少
                log.setBeforeStock(beforeStock);
                log.setAfterStock(beforeStock - quantity);
                log.setOrderId(orderId);
                log.setCreatedAt(LocalDateTime.now());

                inventoryLogDAO.create(log);
                logger.info("[INVENTORY_SERVICE] Stock decreased. Product: {}, Before: {}, After: {}, Order: {}",
                        productId, beforeStock, beforeStock - quantity, orderId);
            }

            return success;
        } catch (Exception e) {
            logger.error("[INVENTORY_SERVICE] Failed to decrease stock for product {}: {}",
                    productId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 调整库存（库存调整）
     * @param productId 商品ID
     * @param newStock 新库存数量
     * @param reason 调整原因
     * @return 操作成功返回true
     */
    public boolean adjustStock(Integer productId, Integer newStock, String reason) {
        logger.info("[INVENTORY_SERVICE] Adjusting stock for product ID: {}, new stock: {}, reason: {}",
                productId, newStock, reason);

        try {
            Product product = productDAO.findById(productId);
            if (product == null) {
                logger.warn("[INVENTORY_SERVICE] Product not found: {}", productId);
                return false;
            }

            int beforeStock = product.getStock() != null ? product.getStock() : 0;
            int quantity = newStock - beforeStock;

            boolean success = productDAO.updateStock(productId, newStock);

            if (success) {
                // 记录库存日志
                InventoryLog log = new InventoryLog();
                log.setProductId(productId);
                log.setChangeType("adjust"); // 库存调整
                log.setQuantity(quantity);
                log.setBeforeStock(beforeStock);
                log.setAfterStock(newStock);
                log.setOrderId(null);
                log.setCreatedAt(LocalDateTime.now());

                inventoryLogDAO.create(log);
                logger.info("[INVENTORY_SERVICE] Stock adjusted. Product: {}, Before: {}, After: {}, Change: {}",
                        productId, beforeStock, newStock, quantity);
            }

            return success;
        } catch (Exception e) {
            logger.error("[INVENTORY_SERVICE] Failed to adjust stock for product {}: {}",
                    productId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取库存变更日志
     * @param productId 商品ID（可选）
     * @param changeType 变更类型（可选）
     * @param page 页码
     * @param pageSize 每页大小
     * @return 库存日志列表
     */
    public List<InventoryLog> getInventoryLogs(Integer productId, String changeType, int page, int pageSize) {
        logger.debug("[INVENTORY_SERVICE] Getting inventory logs. Product: {}, Type: {}, Page: {}",
                productId, changeType, page);

        try {
            if (productId != null) {
                return inventoryLogDAO.findByProductId(productId, page, pageSize);
            } else if (changeType != null && !changeType.isEmpty()) {
                return inventoryLogDAO.findByChangeType(changeType, page, pageSize);
            } else {
                return inventoryLogDAO.findRecentChanges(pageSize);
            }
        } catch (Exception e) {
            logger.error("[INVENTORY_SERVICE] Failed to get inventory logs: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取低库存商品（库存预警）
     * @param threshold 预警阈值
     * @return 低库存商品ID列表
     */
    public List<Integer> getLowStockProducts(int threshold) {
        logger.debug("[INVENTORY_SERVICE] Getting low stock products (threshold: {})", threshold);

        try {
            return inventoryLogDAO.getLowStockProducts(threshold);
        } catch (Exception e) {
            logger.error("[INVENTORY_SERVICE] Failed to get low stock products: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取商品总数
     * @return 商品总数
     */
    public long getProductCount() {
        logger.debug("[INVENTORY_SERVICE] Getting product count");

        try {
            return productDAO.count();
        } catch (Exception e) {
            logger.error("[INVENTORY_SERVICE] Failed to get product count: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 获取分类商品数量
     * @param category 分类
     * @return 商品数量
     */
    public long getProductCountByCategory(String category) {
        logger.debug("[INVENTORY_SERVICE] Getting product count for category: {}", category);

        try {
            return productDAO.countByCategory(category);
        } catch (Exception e) {
            logger.error("[INVENTORY_SERVICE] Failed to get product count for category {}: {}",
                    category, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 根据状态获取商品列表（分页）
     * @param status 商品状态：1-上架，0-下架
     * @param page 页码
     * @param pageSize 每页大小
     * @return 商品列表
     */
    public List<Product> getProductsByStatus(Integer status, int page, int pageSize) {
        logger.debug("[INVENTORY_SERVICE] Getting products by status: {}, page: {}, pageSize: {}",
                status, page, pageSize);

        try {
            return productDAO.findByStatus(status, page, pageSize);
        } catch (Exception e) {
            logger.error("[INVENTORY_SERVICE] Failed to get products by status {}: {}",
                    status, e.getMessage(), e);
            return null;
        }
    }

    public long countLogs() {
        return inventoryLogDAO.count();
    }
}
