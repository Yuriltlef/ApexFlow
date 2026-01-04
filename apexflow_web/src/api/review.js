// src/api/review.js
import service from '@/utils/request'

/**
 * 获取评价列表
 * @param {Object} params - { page, pageSize, productId, userId }
 */
export function getReviewList(params) {
  return service({
    url: '/api/review',
    method: 'get',
    params: params
  })
}

/**
 * 获取评价详情
 * @param {Number} id
 */
export function getReviewDetail(id) {
  return service({
    url: `/api/review/${id}`,
    method: 'get'
  })
}

/**
 * 删除评价
 * @param {Number} id
 */
export function deleteReview(id) {
  return service({
    url: `/api/review/${id}`,
    method: 'delete'
  })
}

/**
 * 获取评价统计
 * @param {Number} productId
 */
export function getReviewStats(productId) {
  return service({
    url: `/api/review/stats/${productId}`,
    method: 'get'
  })
}