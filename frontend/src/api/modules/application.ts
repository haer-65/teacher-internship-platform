import http from '@/api/http';
import type {
  AdminApplicationItem,
  AdminApplicationPageData,
  ApiResponse,
  ApplicationStatus,
  AssignmentCandidatePageData,
  AssignmentImportResultData,
  AssignmentItem,
  AssignmentOptionData,
  AssignmentPageData,
  IdValue,
  StudentApplicationItem,
  StudentApplicationPageData,
  StudentAvailablePlanItem
} from '@/types/api';

export interface StudentApplicationPageQuery {
  page: number;
  size: number;
  keyword?: string;
  applicationStatus?: ApplicationStatus | '';
}

export interface AdminApplicationPageQuery {
  page: number;
  size: number;
  keyword?: string;
  planId?: IdValue;
  applicationStatus?: ApplicationStatus | '';
  studentNo?: string;
  studentName?: string;
}

export interface AssignmentCandidatePageQuery {
  page: number;
  size: number;
  keyword?: string;
  planId?: IdValue;
  studentNo?: string;
}

export interface AssignmentPageQuery {
  page: number;
  size: number;
  keyword?: string;
  planId?: IdValue;
  studentNo?: string;
  isCurrent?: number;
}

export interface StudentApplicationSaveRequest {
  planId: IdValue;
  preferredBaseIds: IdValue[];
  personalStatement?: string;
}

export interface ApplicationReviewRequest {
  status: 'APPROVED' | 'REJECTED';
  reviewComment?: string;
}

export interface AssignmentManualRequest {
  applicationId: IdValue;
  baseId: IdValue;
  innerTeacherId: IdValue;
  baseTeacherId: IdValue;
  adjustReason?: string;
}

export interface AssignmentAdjustRequest {
  baseId: IdValue;
  innerTeacherId: IdValue;
  baseTeacherId: IdValue;
  adjustReason: string;
}

export function queryStudentAvailablePlansApi(): Promise<ApiResponse<StudentAvailablePlanItem[]>> {
  return http.get('/v1/application/student/available-plans');
}

export function queryStudentApplicationPageApi(
  params: StudentApplicationPageQuery
): Promise<ApiResponse<StudentApplicationPageData>> {
  return http.get('/v1/application/student/page', { params });
}

export function getStudentApplicationDetailApi(
  applicationId: IdValue
): Promise<ApiResponse<StudentApplicationItem>> {
  return http.get(`/v1/application/student/detail/${applicationId}`);
}

export function submitStudentApplicationApi(
  data: StudentApplicationSaveRequest
): Promise<ApiResponse<StudentApplicationItem>> {
  return http.post('/v1/application/student/submit', data);
}

export function updateStudentApplicationApi(
  applicationId: IdValue,
  data: StudentApplicationSaveRequest
): Promise<ApiResponse<StudentApplicationItem>> {
  return http.put(`/v1/application/student/update/${applicationId}`, data);
}

export function withdrawStudentApplicationApi(applicationId: IdValue): Promise<ApiResponse<null>> {
  return http.post(`/v1/application/student/withdraw/${applicationId}`);
}

export function queryAdminApplicationPageApi(
  params: AdminApplicationPageQuery
): Promise<ApiResponse<AdminApplicationPageData>> {
  return http.get('/v1/application/admin/page', { params });
}

export function reviewApplicationApi(
  applicationId: IdValue,
  data: ApplicationReviewRequest
): Promise<ApiResponse<AdminApplicationItem>> {
  return http.post(`/v1/application/admin/review/${applicationId}`, data);
}

export function queryAssignmentCandidatePageApi(
  params: AssignmentCandidatePageQuery
): Promise<ApiResponse<AssignmentCandidatePageData>> {
  return http.get('/v1/application/assignment/candidate/page', { params });
}

export function queryAssignmentOptionsApi(planId?: IdValue): Promise<ApiResponse<AssignmentOptionData>> {
  return http.get('/v1/application/assignment/options', {
    params: planId ? { planId } : undefined
  });
}

export function queryAssignmentPageApi(
  params: AssignmentPageQuery
): Promise<ApiResponse<AssignmentPageData>> {
  return http.get('/v1/application/assignment/page', { params });
}

export function queryMyAssignmentPageApi(
  params: AssignmentPageQuery
): Promise<ApiResponse<AssignmentPageData>> {
  return http.get('/v1/application/assignment/my/page', { params });
}

export function getAssignmentDetailApi(
  assignmentId: IdValue
): Promise<ApiResponse<AssignmentItem>> {
  return http.get(`/v1/application/assignment/detail/${assignmentId}`);
}

export function manualAssignApi(
  data: AssignmentManualRequest
): Promise<ApiResponse<AssignmentItem>> {
  return http.post('/v1/application/assignment/manual', data);
}

export function adjustAssignmentApi(
  assignmentId: IdValue,
  data: AssignmentAdjustRequest
): Promise<ApiResponse<AssignmentItem>> {
  return http.post(`/v1/application/assignment/adjust/${assignmentId}`, data);
}

export function importAssignmentExcelApi(file: File): Promise<ApiResponse<AssignmentImportResultData>> {
  const formData = new FormData();
  formData.append('file', file);
  return http.post('/v1/application/assignment/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}
