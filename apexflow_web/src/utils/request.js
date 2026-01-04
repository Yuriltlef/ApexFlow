import axios from 'axios';
import { ElMessage } from 'element-plus';
import { BASE_URL } from '../tomcat/tomcatURL';

const service = axios.create({
  // 补充项目上下文路径 /ApexFlow，与后端Tomcat配置一致
  baseURL: BASE_URL, 
  timeout: 10000,
  // 强制设置Content-Type为application/json，避免解析失败
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
});

// 请求拦截器：发送请求前的处理（比如携带Token）
service.interceptors.request.use(
  (config) => {
    // 从本地存储中获取Token（登录成功后会保存Token）
    const token = localStorage.getItem('token');
    if (token) {
      // 给请求头添加Authorization字段，格式：Bearer + 空格 + Token（和后端要求一致）
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    // 请求失败的处理
    console.error('请求出错：', error);
    return Promise.reject(error);
  }
);

// 响应拦截器：接收后端响应后的处理（统一处理错误信息）
service.interceptors.response.use(
  (response) => {
    // 后端返回的是ApiResponse格式，我们直接返回data
    const res = response.data;
    return res;
  },
  (error) => {
    // 响应失败的处理（比如Token过期、接口不存在等）
    console.error('响应出错：', error);
    // 可以在这里弹出错误提示（结合Element Plus的Message组件）
    return Promise.reject(error);
  }
);

// 导出Axios实例，供其他组件使用
export default service;