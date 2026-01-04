// src/api/order.js
import service from '@/utils/request'

/**
 * 获取订单列表 (支持分页和搜索)
 * @param {Object} params - { page, pageSize, keyword }
 */
export function getOrderList(params) {
  return service({
    url: '/api/orders/list',
    method: 'get',
    params: params
  })
}

/**
 * 获取订单详情
 * @param {String} orderId 
 */
export function getOrderDetail(orderId) {
  return service({
    url: `/api/orders/${orderId}`,
    method: 'get'
  })
}

/**
 * 创建新订单
 * @param {Object} data 
 */
export function createOrder(data) {
  return service({
    url: '/api/orders',
    method: 'post',
    data: data
  })
}

/**
 * 删除订单
 * @param {String} orderId 
 */
export function deleteOrder(orderId) {
  return service({
    url: `/api/orders/${orderId}`,
    method: 'delete'
  })
}

// --- 新增 API ---

/**
 * [新增] 获取用于分析的所有订单数据
 * 说明：由于后端没有独立统计接口，这里请求较大的 pageSize 来获取全量数据进行前端计算
 */
export function getAllOrdersForAnalysis() {
  return service({
    url: '/api/orders/list',
    method: 'get',
    params: {
      page: 1,
      pageSize: 10000 // 获取足够多的数据用于生成图表
    }
  })
}