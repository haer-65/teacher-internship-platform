import http from '@/api/http';
import type {
  ApiResponse,
  BaseDepartmentItem,
  BaseGradeItem,
  BaseInternshipBaseItem,
  BaseMajorItem,
  IdNameOption,
  PageResultData,
  SysLoginLogItem,
  SysMenuNode,
  SysOperationLogItem,
  SysParamItem,
  SysRoleItem
} from '@/types/api';

export interface BasePageQuery {
  page: number;
  size: number;
  keyword?: string;
  status?: string;
}

export interface MajorPageQuery extends BasePageQuery {
  deptId?: number;
}

export interface DepartmentSaveRequest {
  deptCode?: string;
  deptName: string;
  parentId?: number;
  leaderName?: string;
  contactPhone?: string;
}

export interface MajorSaveRequest {
  deptId: number;
  majorCode?: string;
  majorName: string;
}

export interface GradeSaveRequest {
  gradeCode?: string;
  gradeName: string;
}

export interface InternshipBaseSaveRequest {
  baseCode?: string;
  baseName: string;
  province?: string;
  city?: string;
  district?: string;
  address?: string;
  contactPerson?: string;
  contactPhone?: string;
}

export interface StatusUpdateRequest {
  status: string;
}

export interface ParamPageQuery {
  page: number;
  size: number;
  keyword?: string;
  paramType?: string;
  status?: string;
}

export interface SysParamSaveRequest {
  paramCode: string;
  paramName: string;
  paramValue: string;
  paramType: string;
  remark?: string;
}

export interface OperationLogPageQuery {
  page: number;
  size: number;
  keyword?: string;
  moduleCode?: string;
  actionCode?: string;
  operationStatus?: string;
  startTime?: string;
  endTime?: string;
}

export interface LoginLogPageQuery {
  page: number;
  size: number;
  keyword?: string;
  loginResult?: string;
  startTime?: string;
  endTime?: string;
}

export interface RoleMenuAssignRequest {
  menuIds: number[];
}

export function queryDepartmentPageApi(
  params: BasePageQuery
): Promise<ApiResponse<PageResultData<BaseDepartmentItem>>> {
  return http.get('/v1/admin/system/base/department/page', { params });
}

export function createDepartmentApi(
  data: DepartmentSaveRequest
): Promise<ApiResponse<BaseDepartmentItem>> {
  return http.post('/v1/admin/system/base/department/create', data);
}

export function updateDepartmentApi(
  id: number,
  data: DepartmentSaveRequest
): Promise<ApiResponse<BaseDepartmentItem>> {
  return http.put(`/v1/admin/system/base/department/update/${id}`, data);
}

export function updateDepartmentStatusApi(
  id: number,
  data: StatusUpdateRequest
): Promise<ApiResponse<BaseDepartmentItem>> {
  return http.put(`/v1/admin/system/base/department/status/${id}`, data);
}

export function deleteDepartmentApi(id: number): Promise<ApiResponse<null>> {
  return http.delete(`/v1/admin/system/base/department/${id}`);
}

export function queryMajorPageApi(
  params: MajorPageQuery
): Promise<ApiResponse<PageResultData<BaseMajorItem>>> {
  return http.get('/v1/admin/system/base/major/page', { params });
}

export function createMajorApi(data: MajorSaveRequest): Promise<ApiResponse<BaseMajorItem>> {
  return http.post('/v1/admin/system/base/major/create', data);
}

export function updateMajorApi(
  id: number,
  data: MajorSaveRequest
): Promise<ApiResponse<BaseMajorItem>> {
  return http.put(`/v1/admin/system/base/major/update/${id}`, data);
}

export function updateMajorStatusApi(
  id: number,
  data: StatusUpdateRequest
): Promise<ApiResponse<BaseMajorItem>> {
  return http.put(`/v1/admin/system/base/major/status/${id}`, data);
}

export function deleteMajorApi(id: number): Promise<ApiResponse<null>> {
  return http.delete(`/v1/admin/system/base/major/${id}`);
}

export function queryGradePageApi(
  params: BasePageQuery
): Promise<ApiResponse<PageResultData<BaseGradeItem>>> {
  return http.get('/v1/admin/system/base/grade/page', { params });
}

export function createGradeApi(data: GradeSaveRequest): Promise<ApiResponse<BaseGradeItem>> {
  return http.post('/v1/admin/system/base/grade/create', data);
}

export function updateGradeApi(
  id: number,
  data: GradeSaveRequest
): Promise<ApiResponse<BaseGradeItem>> {
  return http.put(`/v1/admin/system/base/grade/update/${id}`, data);
}

export function updateGradeStatusApi(
  id: number,
  data: StatusUpdateRequest
): Promise<ApiResponse<BaseGradeItem>> {
  return http.put(`/v1/admin/system/base/grade/status/${id}`, data);
}

export function deleteGradeApi(id: number): Promise<ApiResponse<null>> {
  return http.delete(`/v1/admin/system/base/grade/${id}`);
}

export function queryInternshipBasePageApi(
  params: BasePageQuery
): Promise<ApiResponse<PageResultData<BaseInternshipBaseItem>>> {
  return http.get('/v1/admin/system/base/internship-base/page', { params });
}

export function createInternshipBaseApi(
  data: InternshipBaseSaveRequest
): Promise<ApiResponse<BaseInternshipBaseItem>> {
  return http.post('/v1/admin/system/base/internship-base/create', data);
}

export function updateInternshipBaseApi(
  id: number,
  data: InternshipBaseSaveRequest
): Promise<ApiResponse<BaseInternshipBaseItem>> {
  return http.put(`/v1/admin/system/base/internship-base/update/${id}`, data);
}

export function updateInternshipBaseStatusApi(
  id: number,
  data: StatusUpdateRequest
): Promise<ApiResponse<BaseInternshipBaseItem>> {
  return http.put(`/v1/admin/system/base/internship-base/status/${id}`, data);
}

export function deleteInternshipBaseApi(id: number): Promise<ApiResponse<null>> {
  return http.delete(`/v1/admin/system/base/internship-base/${id}`);
}

export function queryDepartmentOptionsApi(): Promise<ApiResponse<IdNameOption[]>> {
  return http.get('/v1/admin/system/base/department/options');
}

export function querySystemParamValueApi(code: string, defaultValue?: string): Promise<ApiResponse<string>> {
  return http.get(`/v1/admin/system/param/value/${code}`, {
    params: defaultValue != null ? { defaultValue } : undefined
  });
}

export function queryParamPageApi(
  params: ParamPageQuery
): Promise<ApiResponse<PageResultData<SysParamItem>>> {
  return http.get('/v1/admin/system/param/page', { params });
}

export function getParamDetailApi(id: number): Promise<ApiResponse<SysParamItem>> {
  return http.get(`/v1/admin/system/param/detail/${id}`);
}

export function createParamApi(data: SysParamSaveRequest): Promise<ApiResponse<SysParamItem>> {
  return http.post('/v1/admin/system/param/create', data);
}

export function updateParamApi(
  id: number,
  data: SysParamSaveRequest
): Promise<ApiResponse<SysParamItem>> {
  return http.put(`/v1/admin/system/param/update/${id}`, data);
}

export function updateParamStatusApi(
  id: number,
  data: StatusUpdateRequest
): Promise<ApiResponse<SysParamItem>> {
  return http.put(`/v1/admin/system/param/status/${id}`, data);
}

export function queryOperationLogPageApi(
  params: OperationLogPageQuery
): Promise<ApiResponse<PageResultData<SysOperationLogItem>>> {
  return http.get('/v1/admin/system/log/operation/page', { params });
}

export function queryLoginLogPageApi(
  params: LoginLogPageQuery
): Promise<ApiResponse<PageResultData<SysLoginLogItem>>> {
  return http.get('/v1/admin/system/log/login/page', { params });
}

export function queryRoleListApi(): Promise<ApiResponse<SysRoleItem[]>> {
  return http.get('/v1/admin/system/rbac/role/list');
}

export function queryMenuTreeApi(roleId?: number): Promise<ApiResponse<SysMenuNode[]>> {
  return http.get('/v1/admin/system/rbac/menu/tree', {
    params: { roleId }
  });
}

export function assignRoleMenusApi(
  roleId: number,
  data: RoleMenuAssignRequest
): Promise<ApiResponse<number[]>> {
  return http.put(`/v1/admin/system/rbac/role/menu/${roleId}`, data);
}
