// src/api/warehouse.js
import service from '@/utils/request'

// --- 商品管理 ---

/**
 * 获取商品列表
 * @param {Object} params - { page, pageSize, category, keyword, status }
 */
export function getProductList(params) {
  return service({
    url: '/api/inventory/products/list',
    method: 'get',
    params: params
  })
}

/**
 * 获取商品详情
 * @param {Number} id 
 */
export function getProductDetail(id) {
  return service({
    url: `/api/inventory/products/${id}`,
    method: 'get'
  })
}

/**
 * 创建商品
 * @param {Object} data 
 */
export function createProduct(data) {
  return service({
    url: '/api/inventory/products',
    method: 'post',
    data: data
  })
}

/**
 * 更新商品信息
 * @param {Object} data 
 */
export function updateProduct(data) {
  return service({
    url: `/api/inventory/products/${data.id}`,
    method: 'put',
    data: data
  })
}

/**
 * 删除商品（下架）
 * @param {Number} id 
 */
export function deleteProduct(id) {
  return service({
    url: `/api/inventory/products/${id}`,
    method: 'delete'
  })
}

// --- 库存操作 ---

/**
 * 调整库存 (盘点)
 * PUT /api/inventory/products/{id}/stock
 * @param {Number} id 
 * @param {Object} data - { newStock, reason }
 */
export function adjustStock(id, data) {
  return service({
    url: `/api/inventory/products/${id}/stock`,
    method: 'put',
    data: data
  })
}

/**
 * 获取低库存预警列表
 * @param {Number} threshold 
 */
export function getLowStockList(threshold = 10) {
  return service({
    url: '/api/inventory/low-stock',
    method: 'get',
    params: { threshold }
  })
}

// --- 出入库记录 ---

/**
 * 获取库存变更日志
 * @param {Object} params - { page, pageSize, productId, changeType }
 */
export function getInventoryLogs(params) {
  return service({
    url: '/api/inventory/logs',
    method: 'get',
    params: params
  })
}