// src/api/finance.js
import service from '@/utils/request'

/**
 * 获取财务记录列表
 * @param {Object} params - { page, pageSize, type, status, orderId }
 * type: 'income' (收入) | 'refund' (支出/退款)
 */
export function getFinanceList(params) {
  return service({
    url: '/api/income/list',
    method: 'get',
    params: params
  })
}

/**
 * 获取财务统计数据
 */
export function getFinanceStats() {
  return service({
    url: '/api/income/statistics',
    method: 'get'
  })
}

/**
 * 创建财务记录
 * @param {Object} data 
 */
export function createFinanceRecord(data) {
  return service({
    url: '/api/income',
    method: 'post',
    data: data
  })
}

/**
 * 更新财务记录
 * @param {Object} data 
 */
export function updateFinanceRecord(data) {
  return service({
    url: `/api/income/${data.id}`,
    method: 'put',
    data: data
  })
}

/**
 * 更新财务状态 (例如：确认入账)
 * @param {Number} id 
 * @param {Number} status 1-待入账 2-已入账
 */
export function updateFinanceStatus(id, status) {
  return service({
    url: `/api/income/${id}/status`,
    method: 'put',
    data: { status }
  })
}

/**
 * 删除财务记录
 * @param {Number} id 
 */
export function deleteFinanceRecord(id) {
  return service({
    url: `/api/income/${id}`,
    method: 'delete'
  })
}

/**
 * 获取详情
 * @param {Number} id 
 */
export function getFinanceDetail(id) {
  return service({
    url: `/api/income/${id}`,
    method: 'get'
  })
}