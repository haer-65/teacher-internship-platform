import http from '@/api/http';
import type { ApiResponse, UserImportResultData, UserPageData, IdNameOption, IdValue } from '@/types/api';

export type UserIdentityType = 'STUDENT' | 'TEACHER' | 'ADMIN';

export interface UserListQuery {
  page: number;
  size: number;
  keyword?: string;
  status?: string;
}

export interface UpdateUserStatusRequest {
  status: 'ENABLED' | 'DISABLED';
}

export interface ResetPasswordRequest {
  newPassword?: string;
}

export interface UpdateUserRolesRequest {
  roleCodes: string[];
}

export interface CreateUserRequest {
  realName: string;
  identityType: UserIdentityType;
  studentNo?: string;
  teacherNo?: string;
  phone?: string;
  email?: string;
  deptId?: IdValue;
  majorId?: IdValue;
  gradeId?: IdValue;
  password: string;
  roleCodes: string[];
}

export function queryUsersApi(params: UserListQuery): Promise<ApiResponse<UserPageData>> {
  return http.get('/v1/admin/users/list', { params });
}

export function updateUserStatusApi(
  userId: string,
  data: UpdateUserStatusRequest
): Promise<ApiResponse<null>> {
  return http.put(`/v1/admin/users/${userId}/status`, data);
}

export function resetUserPasswordApi(
  userId: string,
  data: ResetPasswordRequest = {}
): Promise<ApiResponse<null>> {
  return http.post(`/v1/admin/users/${userId}/reset-password`, data);
}

export function updateUserRolesApi(
  userId: string,
  data: UpdateUserRolesRequest
): Promise<ApiResponse<null>> {
  return http.put(`/v1/admin/users/${userId}/roles`, data);
}

export function importUsersApi(file: File): Promise<ApiResponse<UserImportResultData>> {
  const formData = new FormData();
  formData.append('file', file);
  return http.post('/v1/admin/users/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

export function batchDeleteUsersApi(userIds: IdValue[]): Promise<ApiResponse<number>> {
  return http.post('/v1/admin/users/batch-delete', userIds);
}

export function createUserApi(data: CreateUserRequest): Promise<ApiResponse<string>> {
  return http.post('/v1/admin/users/create', data);
}

export function queryDepartmentsApi(): Promise<ApiResponse<IdNameOption[]>> {
  return http.get('/v1/admin/users/departments');
}

export function queryMajorsApi(deptId?: IdValue): Promise<ApiResponse<IdNameOption[]>> {
  return http.get('/v1/admin/users/majors', {
    params: { deptId }
  });
}

export function queryGradesApi(): Promise<ApiResponse<IdNameOption[]>> {
  return http.get('/v1/admin/users/grades');
}
