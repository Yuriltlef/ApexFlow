// src/api/user.js
import axios from 'axios';
import { ElMessage } from 'element-plus';
import userDataManager from '@/utils/userData';  // 导入用户数据管理器

// 登录接口：直接写完整正确的URL，无重复/api
export function userLogin(data) {
  return axios({
    url: 'http://localhost:8080/ApexFlow/api/auth/login',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=utf-8'
    },
    timeout: 10000
  });
}

// 获取用户权限 - 修正：添加Token到请求头
export function getUserPermissions() {
  // 从用户数据管理器获取Token
  const token = userDataManager.getToken();
  
  if (!token) {
    console.error('获取权限时Token为空');
    return Promise.reject(new Error('用户未登录'));
  }
  
  return axios({
    url: 'http://localhost:8080/ApexFlow/api/user/permissions',
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=utf-8',
      'Authorization': `Bearer ${token}`
    },
    timeout: 10000
  });
}

// 更新用户信息
export function updateUserProfile(data) {
  // 从用户数据管理器获取Token
  const token = userDataManager.getToken();
  
  if (!token) {
    console.error('更新用户信息时Token为空');
    return Promise.reject(new Error('用户未登录'));
  }
  
  return axios({
    url: 'http://localhost:8080/ApexFlow/api/user/profile',
    method: 'put',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=utf-8',
      'Authorization': `Bearer ${token}`
    },
    timeout: 10000
  });
}