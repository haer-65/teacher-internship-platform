import http from '@/api/http';
import type { ApiResponse, IdNameOption, IdValue } from '@/types/api';

export interface StudentRegisterRequest {
  studentNo: string;
  realName: string;
  password: string;
  confirmPassword: string;
  deptId: IdValue;
  majorId: IdValue;
  gradeId: IdValue;
  phone?: string;
  email?: string;
}

export interface UpdateProfileRequest {
  realName: string;
  phone?: string;
  email?: string;
}

export interface UpdatePasswordRequest {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export function registerApi(data: StudentRegisterRequest): Promise<ApiResponse<null>> {
  return http.post('/v1/users/register', data);
}

export function queryUserDepartmentsApi(): Promise<ApiResponse<IdNameOption[]>> {
  return http.get('/v1/users/departments');
}

export function queryUserMajorsApi(deptId?: IdValue): Promise<ApiResponse<IdNameOption[]>> {
  return http.get('/v1/users/majors', {
    params: { deptId }
  });
}

export function queryUserGradesApi(): Promise<ApiResponse<IdNameOption[]>> {
  return http.get('/v1/users/grades');
}

export function updateProfileApi(data: UpdateProfileRequest): Promise<ApiResponse<null>> {
  return http.put('/v1/users/profile', data);
}

export function updatePasswordApi(data: UpdatePasswordRequest): Promise<ApiResponse<null>> {
  return http.put('/v1/users/password', data);
}
