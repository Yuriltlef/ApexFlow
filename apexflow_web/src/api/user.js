// src/api/user.js
import axios from 'axios';
import userDataManager from '@/utils/userData';
import { API_BASE_URL } from '../tomcat/tomcatURL';

const BASE_URL = API_BASE_URL;

function getAuthHeaders() {
  const token = userDataManager.getToken();
  return {
    'Content-Type': 'application/json;charset=utf-8',
    'Authorization': token ? `Bearer ${token}` : ''
  };
}

// --- 认证与个人信息 ---
export function userLogin(data) {
  return axios({
    url: `${BASE_URL}/auth/login`,
    method: 'post',
    data: data,
    headers: { 'Content-Type': 'application/json;charset=utf-8' },
    timeout: 10000
  });
}

export function getUserPermissions() {
  const token = userDataManager.getToken();
  if (!token) return Promise.reject(new Error('未登录'));
  return axios({
    url: `${BASE_URL}/user/permissions`,
    method: 'get',
    headers: { 'Authorization': `Bearer ${token}` }
  });
}

export function updateUserProfile(data) {
  return axios({
    url: `${BASE_URL}/user/profile`,
    method: 'put',
    headers: getAuthHeaders(),
    data: data
  });
}

// --- 管理员用户管理 ---

/**
 * 1. 获取用户列表
 */
export function getUserList(params) {
  return axios({
    url: `${BASE_URL}/admin/users`,
    method: 'get',
    headers: getAuthHeaders(),
    params: params
  });
}

/**
 * 2. 创建新用户
 * 接收完整对象，包含所有权限字段
 */
export function createUser(data) {
  return axios({
    url: `${BASE_URL}/admin/users`,
    method: 'post',
    headers: getAuthHeaders(),
    data: data
  });
}

/**
 * 3. 更新基本信息 (不含权限，不含密码)
 */
export function updateUser(data) {
  return axios({
    url: `${BASE_URL}/admin/users/${data.id}`,
    method: 'put',
    headers: getAuthHeaders(),
    data: {
      realName: data.username,
      email: data.email,
      phone: data.phone,
      status: data.status
    }
  });
}

/**
 * 4. 更新用户权限 (独立接口)
 * 接收 isAdmin 和所有细分权限
 */
export function updateUserPermissions(data) {
  return axios({
    url: `${BASE_URL}/admin/users/${data.id}/permissions`,
    method: 'put',
    headers: getAuthHeaders(),
    data: {
      isAdmin: data.isAdmin,
      canManageOrder: data.canManageOrder,
      canManageLogistics: data.canManageLogistics,
      canManageAfterSales: data.canManageAfterSales,
      canManageReview: data.canManageReview,
      canManageInventory: data.canManageInventory,
      canManageIncome: data.canManageIncome
    }
  });
}

/**
 * 5. 重置/修改用户密码
 */
export function resetUserPassword(id, password) {
  return axios({
    url: `${BASE_URL}/admin/users/${id}/password`,
    method: 'put',
    headers: getAuthHeaders(),
    data: {
      password: password
    }
  });
}

/**
 * 6. 删除用户
 */
export function deleteUser(userId) {
  return axios({
    url: `${BASE_URL}/admin/users/${userId}`,
    method: 'delete',
    headers: getAuthHeaders()
  });
}