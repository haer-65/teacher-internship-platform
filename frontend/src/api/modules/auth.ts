import http from '@/api/http';
import type { ApiResponse, AuthContextData } from '@/types/api';

export interface LoginRequest {
  account: string;
  password: string;
  rememberMe: boolean;
  roleCode?: string;
}

export interface SwitchRoleRequest {
  roleCode: string;
}

export function loginApi(data: LoginRequest): Promise<ApiResponse<AuthContextData>> {
  return http.post('/v1/auth/login', data);
}

export function logoutApi(): Promise<ApiResponse<null>> {
  return http.post('/v1/auth/logout');
}

export function meApi(): Promise<ApiResponse<AuthContextData>> {
  return http.get('/v1/auth/me');
}

export function switchRoleApi(data: SwitchRoleRequest): Promise<ApiResponse<AuthContextData>> {
  return http.post('/v1/auth/switch-role', data);
}
