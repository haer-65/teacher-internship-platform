import http from '@/api/http';
import { useAuthStore } from '@/store/modules/auth';
import type {
  ApiResponse,
  IdValue,
  MaterialDocxPreviewData,
  MaterialPageData,
  MaterialVersionItem
} from '@/types/api';

export interface MaterialPageQuery {
  page: number;
  size: number;
  keyword?: string;
  planId?: IdValue;
  studentNo?: string;
  studentName?: string;
  materialStatus?: string;
}

export interface MaterialResubmitOpenRequest {
  materialId: IdValue;
  openUntil?: string;
  openReason?: string;
}

export interface StudentMaterialUploadRequest {
  materialId: IdValue;
  file: File;
  submitRemark?: string;
  onUploadProgress?: (percent: number) => void;
}

export function queryStudentMaterialPageApi(params: MaterialPageQuery): Promise<ApiResponse<MaterialPageData>> {
  return http.get('/v1/material/student/page', { params });
}

export function queryTeacherMaterialPageApi(params: MaterialPageQuery): Promise<ApiResponse<MaterialPageData>> {
  return http.get('/v1/material/teacher/page', { params });
}

export function queryAdminMaterialPageApi(params: MaterialPageQuery): Promise<ApiResponse<MaterialPageData>> {
  return http.get('/v1/material/admin/page', { params });
}

export function uploadStudentMaterialApi(data: StudentMaterialUploadRequest): Promise<ApiResponse<MaterialVersionItem>> {
  const formData = new FormData();
  formData.append('materialId', String(data.materialId));
  formData.append('file', data.file);
  if (data.submitRemark) {
    formData.append('submitRemark', data.submitRemark);
  }

  return http.post('/v1/material/student/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: (event) => {
      if (!data.onUploadProgress || !event.total) {
        return;
      }
      const percent = Math.min(100, Math.round((event.loaded / event.total) * 100));
      data.onUploadProgress(percent);
    }
  });
}

export function queryMaterialVersionListApi(materialId: IdValue): Promise<ApiResponse<MaterialVersionItem[]>> {
  return http.get(`/v1/material/version/list/${materialId}`);
}

export function fetchMaterialDocxPreviewApi(versionId: IdValue): Promise<ApiResponse<MaterialDocxPreviewData>> {
  return http.get(`/v1/material/file/docx-preview/${versionId}`);
}

export function openMaterialResubmitApi(data: MaterialResubmitOpenRequest): Promise<ApiResponse<null>> {
  return http.post('/v1/material/admin/resubmit/open', data);
}

export function closeMaterialResubmitApi(materialId: IdValue): Promise<ApiResponse<null>> {
  return http.post(`/v1/material/admin/resubmit/close/${materialId}`);
}

async function fetchMaterialFile(versionId: IdValue, mode: 'preview' | 'download'): Promise<Blob> {
  const authStore = useAuthStore();
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api';
  const response = await fetch(`${baseUrl}/v1/material/file/${mode}/${versionId}`, {
    method: 'GET',
    headers: {
      Authorization: `Bearer ${authStore.token}`
    }
  });
  const contentType = response.headers.get('content-type') || '';
  if (contentType.includes('application/json')) {
    const payload = (await response.json()) as ApiResponse<unknown>;
    if (payload.code !== 0) {
      throw new Error(payload.message || '文件请求失败');
    }
  }
  if (!response.ok) {
    const message = response.status === 401 ? '登录状态已过期，请重新登录' : '文件请求失败';
    throw new Error(message);
  }
  return response.blob();
}

export function fetchMaterialPreviewBlobApi(versionId: IdValue): Promise<Blob> {
  return fetchMaterialFile(versionId, 'preview');
}

export function fetchMaterialDownloadBlobApi(versionId: IdValue): Promise<Blob> {
  return fetchMaterialFile(versionId, 'download');
}
