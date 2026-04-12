import http from '@/api/http';
import type {
  ApiResponse,
  IdNameOption,
  IdValue,
  PlanAttachmentItem,
  PlanBaseItem,
  PlanDetailData,
  PlanMaterialTypeItem,
  PlanPageData,
  PlanStatus
} from '@/types/api';

export interface PlanPageQuery {
  page: number;
  size: number;
  keyword?: string;
  status?: PlanStatus | '';
}

export interface PlanSaveRequest {
  planCode?: string;
  planName: string;
  academicYear: string;
  term: string;
  deptId: IdValue;
  startTime: string;
  endTime: string;
  applyDeadline: string;
  studentQuota: number;
  description?: string;
  innerTeacherWeight: number;
  baseTeacherWeight: number;
  planBases: PlanBaseItem[];
  materialTypes: PlanMaterialTypeItem[];
}

export function queryPlanPageApi(params: PlanPageQuery): Promise<ApiResponse<PlanPageData>> {
  return http.get('/v1/plan/page', { params });
}

export function queryStudentPlanPageApi(params: PlanPageQuery): Promise<ApiResponse<PlanPageData>> {
  return http.get('/v1/plan/student/page', { params });
}

export function getPlanDetailApi(planId: IdValue): Promise<ApiResponse<PlanDetailData>> {
  return http.get(`/v1/plan/detail/${planId}`);
}

export function getStudentPlanDetailApi(planId: IdValue): Promise<ApiResponse<PlanDetailData>> {
  return http.get(`/v1/plan/student/detail/${planId}`);
}

export function createPlanApi(data: PlanSaveRequest): Promise<ApiResponse<PlanDetailData>> {
  return http.post('/v1/plan/create', data);
}

export function updatePlanApi(planId: IdValue, data: PlanSaveRequest): Promise<ApiResponse<PlanDetailData>> {
  return http.put(`/v1/plan/update/${planId}`, data);
}

export function publishPlanApi(planId: IdValue): Promise<ApiResponse<PlanDetailData>> {
  return http.post(`/v1/plan/publish/${planId}`);
}

export function finishPlanApi(planId: IdValue): Promise<ApiResponse<PlanDetailData>> {
  return http.post(`/v1/plan/finish/${planId}`);
}

export function archivePlanApi(planId: IdValue): Promise<ApiResponse<PlanDetailData>> {
  return http.post(`/v1/plan/archive/${planId}`);
}

export function deletePlanApi(planId: IdValue): Promise<ApiResponse<null>> {
  return http.delete(`/v1/plan/delete/${planId}`);
}

export function uploadPlanAttachmentApi(planId: IdValue, file: File): Promise<ApiResponse<PlanAttachmentItem>> {
  const formData = new FormData();
  formData.append('planId', String(planId));
  formData.append('file', file);
  return http.post('/v1/plan/attachment/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

export function deletePlanAttachmentApi(attachmentId: IdValue): Promise<ApiResponse<null>> {
  return http.delete(`/v1/plan/attachment/delete/${attachmentId}`);
}

export function downloadPlanAttachmentApi(attachmentId: IdValue) {
  return http.download(`/v1/plan/attachment/download/${attachmentId}`);
}

export function queryMaterialTypeConfigApi(planId: IdValue): Promise<ApiResponse<PlanMaterialTypeItem[]>> {
  return http.get(`/v1/plan/material-type/list/${planId}`);
}

export function queryPlanBaseOptionsApi(): Promise<ApiResponse<IdNameOption[]>> {
  return http.get('/v1/plan/base-options');
}

export function queryPlanDepartmentsApi(): Promise<ApiResponse<IdNameOption[]>> {
  return http.get('/v1/plan/departments');
}
