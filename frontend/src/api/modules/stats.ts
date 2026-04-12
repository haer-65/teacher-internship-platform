import http from '@/api/http';
import { useAuthStore } from '@/store/modules/auth';
import type {
  ApiResponse,
  IdValue,
  StatsDashboardData,
  StatsDimensionItem,
  StatsDimensionPageData,
  StatsFilterOptionsData,
  StatsDimensionType
} from '@/types/api';

export interface StatsQuery {
  dimension?: StatsDimensionType | string;
  deptId?: IdValue;
  majorId?: IdValue;
  gradeId?: IdValue;
  baseId?: IdValue;
  teacherId?: IdValue;
  planId?: IdValue;
  planStatus?: string;
}

export interface StatsDimensionPageQuery extends StatsQuery {
  page: number;
  size: number;
}

export interface StatsDimensionListQuery extends StatsQuery {
  top?: number;
}

function buildParams(params?: StatsQuery): Record<string, string> {
  const query: Record<string, string> = {};
  if (!params) {
    return query;
  }
  if (params.dimension) {
    query.dimension = String(params.dimension);
  }
  if (params.deptId !== undefined) {
    query.deptId = String(params.deptId);
  }
  if (params.majorId !== undefined) {
    query.majorId = String(params.majorId);
  }
  if (params.gradeId !== undefined) {
    query.gradeId = String(params.gradeId);
  }
  if (params.baseId !== undefined) {
    query.baseId = String(params.baseId);
  }
  if (params.teacherId !== undefined) {
    query.teacherId = String(params.teacherId);
  }
  if (params.planId !== undefined) {
    query.planId = String(params.planId);
  }
  if (params.planStatus) {
    query.planStatus = params.planStatus;
  }
  return query;
}

export function queryStatsOverviewApi(params: StatsQuery): Promise<ApiResponse<StatsDashboardData>> {
  return http.get('/v1/stats/overview', { params });
}

export function queryStatsDimensionListApi(
  params: StatsDimensionListQuery
): Promise<ApiResponse<StatsDimensionItem[]>> {
  return http.get('/v1/stats/dimension/list', { params });
}

export function queryStatsDimensionPageApi(
  params: StatsDimensionPageQuery
): Promise<ApiResponse<StatsDimensionPageData>> {
  return http.get('/v1/stats/dimension/page', { params });
}

export function queryStatsFilterOptionsApi(params?: StatsQuery): Promise<ApiResponse<StatsFilterOptionsData>> {
  return http.get('/v1/stats/options', { params });
}

export async function downloadStatsExportApi(params: StatsQuery): Promise<Blob> {
  const authStore = useAuthStore();
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api';
  const query = new URLSearchParams(buildParams(params));
  const queryString = query.toString();
  const response = await fetch(`${baseUrl}/v1/stats/export${queryString ? `?${queryString}` : ''}`, {
    method: 'GET',
    headers: {
      Authorization: `Bearer ${authStore.token}`
    }
  });
  if (!response.ok) {
    const message = response.status === 401 ? '登录状态已过期，请重新登录' : '统计导出失败';
    throw new Error(message);
  }
  return response.blob();
}
