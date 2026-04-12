import axios, { type AxiosError, type AxiosRequestConfig, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/store/modules/auth';
import type { ApiResponse } from '@/types/api';

type ApiErrorBody = {
  code?: number;
  message?: string;
  data?: unknown;
};

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000
});

http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const authStore = useAuthStore();
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`;
  }
  return config;
});

http.interceptors.response.use(undefined, (error: AxiosError<ApiErrorBody>) => {
  const authStore = useAuthStore();
  const status = error.response?.status;

  if (status === 401) {
    authStore.resetAuth();
    ElMessage.error('登录状态已过期，请重新登录');
    if (window.location.pathname !== '/login') {
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }

  ElMessage.error(resolveAxiosErrorMessage(error));
  return Promise.reject(error);
});

function resolveAxiosErrorMessage(error: AxiosError<ApiErrorBody>): string {
  const status = error.response?.status;
  const serverMessage = error.response?.data?.message?.trim();

  if (!error.response) {
    return '网络连接失败，请检查后端服务是否可用';
  }

  if (status === 500) {
    return serverMessage || '服务器处理请求失败，请稍后重试';
  }

  if (serverMessage) {
    return serverMessage;
  }

  if (status) {
    return `请求失败（HTTP ${status}）`;
  }

  return '请求失败';
}

async function request<T>(config: AxiosRequestConfig): Promise<ApiResponse<T>> {
  const response = await http.request<ApiResponse<T>>(config);
  const resp = response.data;
  if (resp.code !== 0) {
    ElMessage.error(resp.message || '请求失败');
    throw new Error(resp.message || '请求失败');
  }
  return resp;
}

const apiClient = {
  get<T>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return request<T>({ ...config, url, method: 'GET' });
  },
  post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return request<T>({ ...config, url, data, method: 'POST' });
  },
  put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return request<T>({ ...config, url, data, method: 'PUT' });
  },
  delete<T>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return request<T>({ ...config, url, method: 'DELETE' });
  },
  download<T = Blob>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return http.request<T>({ ...config, url, method: 'GET', responseType: 'blob' });
  }
};

export default apiClient;
