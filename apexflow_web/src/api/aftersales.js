// src/api/aftersales.js
import service from '@/utils/request'

/**
 * 获取售后单列表
 * @param {Object} params - { page, pageSize, status, keyword }
 * status: 0-待处理, 1-处理中, 2-已完成, 3-已拒绝 (根据后端定义调整)
 */
export function getAfterSalesList(params) {
  return service({
    url: '/api/after-sales/list',
    method: 'get',
    params: params
  })
}

/**
 * 获取售后单详情
 * @param {String|Number} id 售后单ID
 */
export function getAfterSalesDetail(id) {
  return service({
    url: `/api/after-sales/${id}`,
    method: 'get'
  })
}

/**
 * [修复] 更新售后状态 (审核/结单)
 * URL: PUT /api/after-sales/{id}/status
 */
export function auditAfterSales(data) {
  return service({
    url: `/api/after-sales/${data.id}/status`, // 将 ID 拼接到 URL
    method: 'put',
    data: {
      status: data.status,
      remark: data.remark
    }
  })
}