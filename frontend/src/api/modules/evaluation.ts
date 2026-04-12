import http from '@/api/http';
import type {
  ApiResponse,
  IdValue,
  EvaluationRecordItem,
  EvaluationScoreItem,
  EvaluationScoreSummaryData,
  FinalEvaluationDetailData,
  ProcessEvaluationDetailData,
  StudentEvaluationPageData,
  TeacherFinalPendingPageData,
  TeacherProcessPendingPageData
} from '@/types/api';

export interface TeacherProcessPageQuery {
  page: number;
  size: number;
  keyword?: string;
  planId?: IdValue;
  studentNo?: string;
  pendingOnly?: number;
}

export interface TeacherFinalPageQuery {
  page: number;
  size: number;
  keyword?: string;
  planId?: IdValue;
  studentNo?: string;
  pendingOnly?: number;
}

export interface StudentEvaluationPageQuery {
  page: number;
  size: number;
  keyword?: string;
  evaluationType?: 'PROCESS' | 'FINAL' | '';
}

export interface ProcessEvaluationSubmitRequest {
  materialVersionId: IdValue;
  score: number;
  commentText?: string;
  scoreItems?: EvaluationScoreItem[];
}

export interface FinalEvaluationSubmitRequest {
  assignmentId: IdValue;
  score: number;
  commentText?: string;
  scoreItems?: EvaluationScoreItem[];
}

export function queryTeacherProcessPendingPageApi(
  params: TeacherProcessPageQuery
): Promise<ApiResponse<TeacherProcessPendingPageData>> {
  return http.get('/v1/evaluation/teacher/process/page', { params });
}

export function getTeacherProcessDetailApi(
  materialVersionId: IdValue
): Promise<ApiResponse<ProcessEvaluationDetailData>> {
  return http.get(`/v1/evaluation/teacher/process/detail/${materialVersionId}`);
}

export function submitProcessEvaluationApi(
  data: ProcessEvaluationSubmitRequest
): Promise<ApiResponse<EvaluationRecordItem>> {
  return http.post('/v1/evaluation/teacher/process/submit', data);
}

export function queryTeacherFinalPendingPageApi(
  params: TeacherFinalPageQuery
): Promise<ApiResponse<TeacherFinalPendingPageData>> {
  return http.get('/v1/evaluation/teacher/final/page', { params });
}

export function getTeacherFinalDetailApi(
  assignmentId: IdValue
): Promise<ApiResponse<FinalEvaluationDetailData>> {
  return http.get(`/v1/evaluation/teacher/final/detail/${assignmentId}`);
}

export function submitFinalEvaluationApi(
  data: FinalEvaluationSubmitRequest
): Promise<ApiResponse<EvaluationRecordItem>> {
  return http.post('/v1/evaluation/teacher/final/submit', data);
}

export function queryStudentEvaluationPageApi(
  params: StudentEvaluationPageQuery
): Promise<ApiResponse<StudentEvaluationPageData>> {
  return http.get('/v1/evaluation/student/page', { params });
}

export function queryEvaluationScoreSummaryApi(
  assignmentId: IdValue
): Promise<ApiResponse<EvaluationScoreSummaryData>> {
  return http.get(`/v1/evaluation/score/summary/${assignmentId}`);
}
