// src/api/logistics.js
import service from '@/utils/request'

/**
 * 获取物流列表 (支持分页)
 * @param {Object} params - { page, pageSize }
 */
export function getLogisticsList(params) {
  return service({
    url: '/api/logistics',
    method: 'get',
    params: params
  })
}

/**
 * 获取物流统计信息
 */
export function getLogisticsStats() {
  return service({
    url: '/api/logistics/stats',
    method: 'get'
  })
}

/**
 * 根据ID获取物流详情
 * @param {Number} id 
 */
export function getLogisticsDetail(id) {
  return service({
    url: `/api/logistics/${id}`,
    method: 'get'
  })
}

/**
 * 根据订单号获取物流信息
 * @param {String} orderId 
 */
export function getLogisticsByOrder(orderId) {
  return service({
    url: `/api/logistics/order/${orderId}`,
    method: 'get'
  })
}

/**
 * [发货] 更新发货信息
 * PUT /api/logistics/{id}/shipping
 * @param {Number} id 
 * @param {Object} data - { expressCompany, trackingNumber, senderAddress }
 */
export function shipLogistics(id, data) {
  return service({
    url: `/api/logistics/${id}/shipping`,
    method: 'put',
    data: data
  })
}

/**
 * [更新状态] 例如：确认送达
 * PUT /api/logistics/{id}/status
 * @param {Number} id 
 * @param {String} status - 'pending' | 'shipped' | 'delivered'
 */
export function updateLogisticsStatus(id, status) {
  return service({
    url: `/api/logistics/${id}/status`,
    method: 'put',
    data: { status }
  })
}

/**
 * [更新详情] 修改物流信息
 * PUT /api/logistics/{id}
 */
export function updateLogisticsInfo(id, data) {
  return service({
    url: `/api/logistics/${id}`,
    method: 'put',
    data: data
  })
}

/**
 * 删除物流记录
 * @param {Number} id 
 */
export function deleteLogistics(id) {
  return service({
    url: `/api/logistics/${id}`,
    method: 'delete'
  })
}