import axios from 'axios';
import ToUrl from '@/api/api';
// 创建 axios 实例
const instance = axios.create({
  baseURL: ToUrl.url, // 后端 API 地址
  timeout: 15000, // 设置请求超时为15秒
});

// 请求拦截器
instance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token') || '';  // 从 localStorage 获取 token
    if (token) {
      // 如果 token 存在，添加到请求头
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
instance.interceptors.response.use(
  (response) => {
    // 在这里可以根据不同的返回数据进行处理
    if (response.data.code === 401) {
      // 如果 401 错误，可以清除 token 并跳转到登录页
      localStorage.removeItem('token');
      window.location.href = '/login';  // 跳转到登录页
    }
    return response;
  },
  (error) => {
    return Promise.reject(error);
  }
);
axios.defaults.withCredentials=true;
export default instance;
