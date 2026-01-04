// ============= 后端基础配置 ================
// 后端Tomcat服务器地址及项目上下文路径
// 需要根据实际部署环境修改

export const BASE_URL = 'http://localhost:8080/ApexFlow';

// ==========================================


export const API_PREFIX = '/api';
// 完整API地址
export const API_BASE_URL = `${BASE_URL}${API_PREFIX}`;

// 导出所有配置
export default {
  BASE_URL,
  API_PREFIX,
  API_BASE_URL
};