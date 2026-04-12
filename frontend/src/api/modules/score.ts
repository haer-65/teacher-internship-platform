import http from '@/api/http';
import { useAuthStore } from '@/store/modules/auth';
import type {
  ApiResponse,
  IdValue,
  ScoreDetailData,
  ScorePageData,
  ScorePublishPreviewData,
  ScorePublishResultData,
  ScoreRecalculateResultData
} from '@/types/api';

export interface ScorePageQuery {
  page: number;
  size: number;
  keyword?: string;
  planId?: IdValue;
  studentNo?: string;
  studentName?: string;
  status?: string;
}

export interface StudentScorePageQuery {
  page: number;
  size: number;
  keyword?: string;
  planId?: IdValue;
}

export interface ScoreAdjustRequest {
  newTotalScore: number;
  adjustReason: string;
}

export interface ScoreExportQuery {
  planId?: IdValue;
  status?: string;
}

export interface ScorePlanOption {
  id: IdValue;
  name: string;
}

export function queryScorePageApi(params: ScorePageQuery): Promise<ApiResponse<ScorePageData>> {
  return http.get('/v1/score/page', { params });
}

export function getScoreDetailApi(scoreSheetId: IdValue): Promise<ApiResponse<ScoreDetailData>> {
  return http.get(`/v1/score/detail/${scoreSheetId}`);
}

export function recalculateScorePlanApi(planId: IdValue): Promise<ApiResponse<ScoreRecalculateResultData>> {
  return http.post(`/v1/score/recalculate/plan/${planId}`);
}

export function previewScorePublishApi(
  planId: IdValue,
  recalculate = 1
): Promise<ApiResponse<ScorePublishPreviewData>> {
  return http.get(`/v1/score/publish/preview/${planId}`, {
    params: { recalculate }
  });
}

export function publishScorePlanApi(planId: IdValue): Promise<ApiResponse<ScorePublishResultData>> {
  return http.post(`/v1/score/publish/plan/${planId}`);
}

export function adjustScoreApi(
  scoreSheetId: IdValue,
  data: ScoreAdjustRequest
): Promise<ApiResponse<ScoreDetailData>> {
  return http.post(`/v1/score/adjust/${scoreSheetId}`, data);
}

export function queryStudentScorePageApi(
  params: StudentScorePageQuery
): Promise<ApiResponse<ScorePageData>> {
  return http.get('/v1/score/student/page', { params });
}

export function queryScorePlanOptionsApi(): Promise<ApiResponse<ScorePlanOption[]>> {
  return http.get('/v1/score/plan-options');
}

async function fetchScoreFile(url: string): Promise<Blob> {
  const authStore = useAuthStore();
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api';
  const response = await fetch(`${baseUrl}${url}`, {
    method: 'GET',
    headers: {
      Authorization: `Bearer ${authStore.token}`
    }
  });
  if (!response.ok) {
    const message = response.status === 401 ? '登录状态已过期，请重新登录' : '成绩导出失败';
    throw new Error(message);
  }
  return response.blob();
}

export function downloadScoreExcelApi(params: ScoreExportQuery): Promise<Blob> {
  const query = new URLSearchParams();
  if (params.planId !== undefined) {
    query.set('planId', String(params.planId));
  }
  if (params.status) {
    query.set('status', params.status);
  }
  const suffix = query.toString();
  return fetchScoreFile(`/v1/score/export/excel${suffix ? `?${suffix}` : ''}`);
}

export function downloadScorePdfApi(scoreSheetId: IdValue): Promise<Blob> {
  return fetchScoreFile(`/v1/score/export/pdf/${scoreSheetId}`);
}
