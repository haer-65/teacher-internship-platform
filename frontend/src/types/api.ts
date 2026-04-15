export interface ApiResponse<T = unknown> {
  code: number;
  message: string;
  data: T;
}

export type IdValue = string | number;

export interface LoginUserInfo {
  userId: string;
  accountNo: string;
  realName: string;
  identityType?: string;
  studentNo?: string;
  teacherNo?: string;
  phone?: string;
  email?: string;
  deptId?: IdValue;
  deptName?: string;
  majorName?: string;
  gradeName?: string;
  currentRoleCode: string;
  mustChangePassword?: number;
}

export interface RoleOption {
  roleCode: string;
  roleName: string;
}

export interface MenuNode {
  id: number;
  parentId: number;
  menuName: string;
  routePath: string;
  componentPath?: string | null;
  icon?: string | null;
  sortNo?: number | null;
  children: MenuNode[];
}

export interface AuthContextData {
  token: string | null;
  tokenType: string | null;
  expiresIn: number | null;
  currentRoleCode: string;
  user: LoginUserInfo;
  roles: RoleOption[];
  menuTree: MenuNode[];
  permissionCodes: string[];
}

export interface UserItem {
  id: string;
  accountNo: string;
  realName: string;
  identityType: string;
  roleNames?: string;
  roleCodes?: string[];
  studentNo?: string;
  teacherNo?: string;
  phone?: string;
  email?: string;
  status: string;
  deptId?: IdValue;
  deptName?: string;
  majorId?: IdValue;
  majorName?: string;
  gradeId?: IdValue;
  gradeName?: string;
  mustChangePassword?: number;
  createdTime?: string;
}

export interface UserPageData {
  total: number;
  page: number;
  size: number;
  records: UserItem[];
}

export type PlanStatus = 'DRAFT' | 'PUBLISHED' | 'FINISHED' | 'ARCHIVED';

export interface PlanMaterialTypeItem {
  id?: IdValue;
  planId?: IdValue;
  typeCode: string;
  typeName: string;
  requiredFlag: number;
  allowResubmit: boolean;
  maxSubmitCount: number;
  deadlineTime: string;
  weight: number;
  status: string;
}

export interface PlanAttachmentItem {
  id: IdValue;
  planId: IdValue;
  fileName: string;
  filePath: string;
  downloadUrl?: string;
  fileSize: number;
  fileExt?: string;
  mimeType?: string;
  uploadedBy: IdValue;
  uploadedTime: string;
}

export interface PlanListItem {
  id: IdValue;
  planCode: string;
  planName: string;
  academicYear: string;
  term: string;
  deptId: IdValue;
  deptName?: string;
  startTime: string;
  endTime: string;
  applyDeadline: string;
  studentQuota: number;
  remainingQuota?: number;
  innerTeacherWeight: number;
  baseTeacherWeight: number;
  planStatus: PlanStatus;
  publishedTime?: string;
  archivedTime?: string;
}

export interface PlanPageData {
  total: number;
  page: number;
  size: number;
  records: PlanListItem[];
}

export interface PlanBaseItem {
  id?: IdValue;
  planId?: IdValue;
  baseId: IdValue;
  baseCode?: string;
  baseName?: string;
  baseQuota: number;
  remainingQuota?: number;
  sortNo: number;
  status: string;
  remark?: string;
}

export interface PlanDetailData {
  id: IdValue;
  planCode: string;
  planName: string;
  academicYear: string;
  term: string;
  deptId: IdValue;
  deptName?: string;
  startTime: string;
  endTime: string;
  applyDeadline: string;
  studentQuota: number;
  remainingQuota?: number;
  description?: string;
  innerTeacherWeight: number;
  baseTeacherWeight: number;
  planStatus: PlanStatus;
  scorePublishStatus: string;
  publishedTime?: string;
  archivedTime?: string;
  planBases: PlanBaseItem[];
  materialTypes: PlanMaterialTypeItem[];
  attachments: PlanAttachmentItem[];
}

export type ApplicationStatus = 'SUBMITTED' | 'APPROVED' | 'REJECTED' | 'WITHDRAWN';
export type AssignmentStatus = 'ASSIGNED' | 'ADJUSTED' | 'CANCELLED';

export interface StudentAvailablePlanItem {
  id: IdValue;
  applicationId?: IdValue;
  planCode: string;
  planName: string;
  academicYear: string;
  term: string;
  applyDeadline: string;
  applicationStatus?: ApplicationStatus;
  planBases: PlanBaseItem[];
}

export interface ApplicationPreferenceItem {
  baseId: IdValue;
  baseCode?: string;
  baseName?: string;
  preferenceOrder: number;
}

export interface StudentApplicationItem {
  id: IdValue;
  planId: IdValue;
  planCode?: string;
  planName?: string;
  applyDeadline?: string;
  personalStatement?: string;
  preferredBases: ApplicationPreferenceItem[];
  applicationStatus: ApplicationStatus;
  reviewComment?: string;
  reviewedBy?: IdValue;
  reviewedByName?: string;
  reviewedTime?: string;
  submittedTime?: string;
  canEdit?: boolean;
  canWithdraw?: boolean;
}

export interface StudentApplicationPageData {
  total: number;
  page: number;
  size: number;
  records: StudentApplicationItem[];
}

export interface AdminApplicationItem {
  id: IdValue;
  planId: IdValue;
  planCode?: string;
  planName?: string;
  planStatus?: PlanStatus;
  studentId: IdValue;
  studentNo?: string;
  studentName?: string;
  studentDeptId?: IdValue;
  studentDeptName?: string;
  studentMajorName?: string;
  studentGradeName?: string;
  personalStatement?: string;
  preferredBases: ApplicationPreferenceItem[];
  applicationStatus: ApplicationStatus;
  reviewComment?: string;
  reviewedBy?: IdValue;
  reviewedByName?: string;
  reviewedTime?: string;
  submittedTime?: string;
  currentAssignmentId?: IdValue;
  currentAssignmentVersionNo?: number;
}

export interface AdminApplicationPageData {
  total: number;
  page: number;
  size: number;
  records: AdminApplicationItem[];
}

export interface AssignmentOptionItem {
  id: IdValue;
  code?: string;
  name?: string;
}

export interface AssignmentOptionData {
  internshipBases: AssignmentOptionItem[];
  innerTeachers: AssignmentOptionItem[];
  baseTeachers: AssignmentOptionItem[];
}

export interface AssignmentCandidateItem {
  applicationId: IdValue;
  planId: IdValue;
  planCode?: string;
  planName?: string;
  planStatus?: PlanStatus;
  studentId: IdValue;
  studentNo?: string;
  studentName?: string;
  studentDeptName?: string;
  studentMajorName?: string;
  studentGradeName?: string;
  preferredBases: ApplicationPreferenceItem[];
  submittedTime?: string;
}

export interface AssignmentCandidatePageData {
  total: number;
  page: number;
  size: number;
  records: AssignmentCandidateItem[];
}

export interface AssignmentItem {
  id: IdValue;
  planId: IdValue;
  planCode?: string;
  planName?: string;
  planStatus?: PlanStatus;
  applicationId: IdValue;
  studentId: IdValue;
  studentNo?: string;
  studentName?: string;
  studentDeptName?: string;
  studentMajorName?: string;
  studentGradeName?: string;
  baseId: IdValue;
  baseCode?: string;
  baseName?: string;
  innerTeacherId: IdValue;
  innerTeacherNo?: string;
  innerTeacherName?: string;
  baseTeacherId: IdValue;
  baseTeacherNo?: string;
  baseTeacherName?: string;
  versionNo: number;
  isCurrent: number;
  assignmentStatus: AssignmentStatus;
  adjustReason?: string;
  assignedBy?: IdValue;
  assignedByName?: string;
  assignedTime?: string;
}

export interface AssignmentPageData {
  total: number;
  page: number;
  size: number;
  records: AssignmentItem[];
}

export interface AssignmentImportError {
  rowNumber: number;
  message: string;
}

export interface AssignmentImportResultData {
  totalRows: number;
  successRows: number;
  failedRows: number;
  errors: AssignmentImportError[];
}

export type MaterialStatus = 'NOT_SUBMITTED' | 'SUBMITTED' | 'OVERDUE';

export interface MaterialVersionItem {
  id: IdValue;
  materialId: IdValue;
  versionNo: number;
  fileName: string;
  fileSize: number;
  fileExt?: string;
  mimeType?: string;
  submitRemark?: string;
  submittedBy: IdValue;
  submittedByName?: string;
  submittedTime: string;
  isCurrent: number;
  previewable: boolean;
  previewUrl: string;
  downloadUrl: string;
}

export interface MaterialDocxPreviewData {
  versionId: IdValue;
  materialId: IdValue;
  versionNo: number;
  fileName: string;
  htmlContent: string;
}

export interface MaterialListItem {
  materialId: IdValue;
  assignmentId: IdValue;
  planId: IdValue;
  planCode?: string;
  planName?: string;
  studentId: IdValue;
  studentNo?: string;
  studentName?: string;
  materialTypeId: IdValue;
  materialTypeCode?: string;
  materialTypeName?: string;
  requiredFlag: number;
  maxSubmitCount: number;
  deadlineTime?: string;
  latestVersionNo: number;
  materialStatus: MaterialStatus;
  lastSubmitTime?: string;
  overdue: boolean;
  canSubmit: boolean;
  lateSubmitOpen: boolean;
  lateSubmitUntil?: string;
  lateSubmitReason?: string;
  latestVersion?: MaterialVersionItem;
}

export interface MaterialPageData {
  total: number;
  page: number;
  size: number;
  records: MaterialListItem[];
}

export type EvaluationType = 'PROCESS' | 'FINAL';

export interface EvaluationScoreItem {
  itemName: string;
  itemScore: number;
  itemWeight?: number;
}

export interface EvaluationRecordItem {
  id: number;
  planId: number;
  planCode?: string;
  planName?: string;
  assignmentId: number;
  studentId: number;
  studentNo?: string;
  studentName?: string;
  materialId?: IdValue;
  materialVersionId?: IdValue;
  materialVersionNo?: number;
  materialTypeCode?: string;
  materialTypeName?: string;
  fileName?: string;
  evaluatorId: number;
  evaluatorName?: string;
  evaluatorRole: string;
  evaluationType: EvaluationType;
  recordNo: number;
  score: number;
  commentText?: string;
  evaluatedTime: string;
  scoreItems: EvaluationScoreItem[];
}

export interface TeacherProcessPendingItem {
  materialId: IdValue;
  materialVersionId: IdValue;
  materialVersionNo: number;
  fileName?: string;
  submittedTime?: string;
  assignmentId: IdValue;
  planId: IdValue;
  planCode?: string;
  planName?: string;
  studentId: IdValue;
  studentNo?: string;
  studentName?: string;
  materialTypeId: IdValue;
  materialTypeCode?: string;
  materialTypeName?: string;
  myEvaluationCount: number;
  myLatestEvaluatedTime?: string;
  myLatestScore?: number;
  pending: boolean;
}

export interface TeacherProcessPendingPageData {
  total: number;
  page: number;
  size: number;
  records: TeacherProcessPendingItem[];
}

export interface ProcessEvaluationDetailData {
  materialId: IdValue;
  materialVersionId: IdValue;
  materialVersionNo: number;
  fileName?: string;
  fileSize?: number;
  fileExt?: string;
  mimeType?: string;
  previewUrl?: string;
  downloadUrl?: string;
  submitRemark?: string;
  submittedTime?: string;
  assignmentId: IdValue;
  planId: IdValue;
  planCode?: string;
  planName?: string;
  studentId: IdValue;
  studentNo?: string;
  studentName?: string;
  materialTypeId: IdValue;
  materialTypeCode?: string;
  materialTypeName?: string;
  materialDeadlineTime?: string;
  canEvaluate: boolean;
  records: EvaluationRecordItem[];
}

export interface TeacherFinalPendingItem {
  assignmentId: IdValue;
  planId: IdValue;
  planCode?: string;
  planName?: string;
  planStatus?: string;
  studentId: IdValue;
  studentNo?: string;
  studentName?: string;
  myEvaluationCount: number;
  myLatestEvaluatedTime?: string;
  myLatestScore?: number;
  canEvaluate: boolean;
  pending: boolean;
}

export interface TeacherFinalPendingPageData {
  total: number;
  page: number;
  size: number;
  records: TeacherFinalPendingItem[];
}

export interface FinalEvaluationDetailData {
  assignmentId: IdValue;
  planId: IdValue;
  planCode?: string;
  planName?: string;
  planStatus?: string;
  studentId: IdValue;
  studentNo?: string;
  studentName?: string;
  innerTeacherId?: IdValue;
  baseTeacherId?: IdValue;
  canEvaluate: boolean;
  records: EvaluationRecordItem[];
}

export interface StudentEvaluationItem {
  id: number;
  evaluationType: EvaluationType;
  recordNo: number;
  assignmentId: number;
  planId: number;
  planCode?: string;
  planName?: string;
  materialId?: IdValue;
  materialVersionId?: IdValue;
  materialVersionNo?: number;
  materialTypeName?: string;
  evaluatorId: number;
  evaluatorName?: string;
  evaluatorRole?: string;
  score: number;
  commentText?: string;
  evaluatedTime: string;
}

export interface StudentEvaluationPageData {
  total: number;
  page: number;
  size: number;
  records: StudentEvaluationItem[];
}

export interface EvaluationScoreSummaryData {
  assignmentId: number;
  planId: number;
  studentId: number;
  innerTeacherProcessAvg?: number;
  baseTeacherProcessAvg?: number;
  innerTeacherFinalLatest?: number;
  baseTeacherFinalLatest?: number;
  processCompositeAvg?: number;
  finalCompositeAvg?: number;
}

export type ScoreStatus = 'DRAFT' | 'PUBLISHED';

export interface ScoreListItem {
  id: IdValue;
  planId: IdValue;
  planCode?: string;
  planName?: string;
  assignmentId: IdValue;
  studentId: IdValue;
  studentNo?: string;
  studentName?: string;
  deptName?: string;
  majorName?: string;
  gradeName?: string;
  processScore?: number;
  finalScore?: number;
  autoTotalScore?: number;
  totalScore?: number;
  status: ScoreStatus;
  adjustedFlag?: number;
  adjustTimes?: number;
  adjustReason?: string;
  adjustByName?: string;
  adjustTime?: string;
  publishedTime?: string;
  lastCalcTime?: string;
}

export interface ScorePageData {
  total: number;
  page: number;
  size: number;
  records: ScoreListItem[];
}

export interface ScoreMaterialDetail {
  materialTypeId?: number;
  materialTypeCode?: string;
  materialTypeName?: string;
  materialWeight?: number;
  materialId?: IdValue;
  materialVersionId?: IdValue;
  materialVersionNo?: number;
  fileName?: string;
  innerTeacherProcessScore?: number;
  baseTeacherProcessScore?: number;
  materialCompositeScore?: number;
  weightedContribution?: number;
}

export interface ScoreDetailData {
  id: IdValue;
  planId: IdValue;
  planCode?: string;
  planName?: string;
  assignmentId: IdValue;
  studentId: IdValue;
  studentNo?: string;
  studentName?: string;
  deptName?: string;
  majorName?: string;
  gradeName?: string;
  innerTeacherWeight?: number;
  baseTeacherWeight?: number;
  processScore?: number;
  finalScore?: number;
  autoTotalScore?: number;
  totalScore?: number;
  innerTeacherFinalScore?: number;
  baseTeacherFinalScore?: number;
  formulaText?: string;
  status: ScoreStatus;
  adjustedFlag?: number;
  adjustTimes?: number;
  adjustReason?: string;
  adjustBy?: IdValue;
  adjustByName?: string;
  adjustTime?: string;
  publishedBy?: IdValue;
  publishedByName?: string;
  publishedTime?: string;
  lastCalcTime?: string;
  materialDetails: ScoreMaterialDetail[];
}

export interface ScoreRecalculateResultData {
  planId: number;
  planCode?: string;
  planName?: string;
  totalAssignments: number;
  generatedSheets: number;
}

export interface ScorePublishPreviewData {
  planId: number;
  planCode?: string;
  planName?: string;
  planStatus?: string;
  scorePublishStatus?: string;
  canPublish?: boolean;
  publishBlockedReason?: string;
  total: number;
  records: ScoreListItem[];
}

export interface ScorePublishResultData {
  planId: number;
  planCode?: string;
  planName?: string;
  publishedCount: number;
  publishedTime?: string;
}

export type NoticeType = 'SYSTEM' | 'BUSINESS';
export type NoticeLevel = 'NORMAL' | 'URGENT';

export interface NoticeListItem {
  noticeId: IdValue;
  noticeType: NoticeType | string;
  noticeLevel: NoticeLevel | string;
  noticeTitle: string;
  noticeContentPreview?: string;
  senderId?: IdValue;
  senderName?: string;
  targetRoleCode?: string;
  relatedBusinessType?: string;
  relatedBusinessId?: IdValue;
  sendTime?: string;
  readFlag: number;
  readTime?: string;
}

export interface NoticePageData {
  total: number;
  page: number;
  size: number;
  records: NoticeListItem[];
}

export interface NoticeDetailData {
  noticeId: IdValue;
  noticeType: NoticeType | string;
  noticeLevel: NoticeLevel | string;
  noticeTitle: string;
  noticeContent: string;
  senderId?: IdValue;
  senderName?: string;
  targetRoleCode?: string;
  relatedBusinessType?: string;
  relatedBusinessId?: IdValue;
  status?: string;
  sendTime?: string;
  readFlag: number;
  readTime?: string;
}

export interface NoticeUnreadData {
  unreadCount: number;
}

export interface NoticeTriggerResultData {
  triggerType: string;
  generatedCount: number;
}

export type StatsDimensionType = 'SUMMARY' | 'DEPARTMENT' | 'MAJOR' | 'GRADE' | 'BASE' | 'TEACHER' | 'PLAN';

export interface StatsOverviewData {
  internshipStudentCount: number;
  materialTotalCount: number;
  materialSubmittedCount: number;
  materialOverdueCount: number;
  materialSubmitRate: number;
  materialOverdueRate: number;
  assignmentTotalCount: number;
  evaluationCompletedCount: number;
  evaluationCompletionRate: number;
}

export interface StatsScoreSummaryData {
  scoreStudentCount: number;
  averageScore: number;
  excellentRate: number;
  passRate: number;
}

export interface StatsScoreDistributionItem {
  bucketKey: string;
  bucketLabel: string;
  scoreCount: number;
}

export interface StatsDashboardData {
  overview: StatsOverviewData;
  scoreSummary: StatsScoreSummaryData;
  scoreDistribution: StatsScoreDistributionItem[];
}

export interface StatsDimensionItem {
  dimensionCode: StatsDimensionType | string;
  dimensionId?: IdValue;
  dimensionName: string;
  internshipStudentCount: number;
  materialTotalCount: number;
  materialSubmittedCount: number;
  materialOverdueCount: number;
  materialSubmitRate: number;
  materialOverdueRate: number;
  assignmentTotalCount: number;
  evaluationCompletedCount: number;
  evaluationCompletionRate: number;
  scoreStudentCount: number;
  averageScore: number;
  excellentRate: number;
  passRate: number;
}

export interface StatsDimensionPageData {
  total: number;
  page: number;
  size: number;
  records: StatsDimensionItem[];
}

export interface StatsOptionItem {
  id: IdValue;
  name: string;
}

export interface StatsFilterOptionsData {
  departments: StatsOptionItem[];
  majors: StatsOptionItem[];
  grades: StatsOptionItem[];
  bases: StatsOptionItem[];
  teachers: StatsOptionItem[];
  plans: StatsOptionItem[];
}

export interface PageResultData<T> {
  page: number;
  size: number;
  total: number;
  records: T[];
}

export interface IdNameOption {
  id: IdValue;
  name: string;
}

export interface BaseDepartmentItem {
  id: number;
  deptCode: string;
  deptName: string;
  parentId?: number;
  leaderName?: string;
  contactPhone?: string;
  status: string;
  createdTime?: string;
  updatedTime?: string;
}

export interface BaseMajorItem {
  id: number;
  deptId: number;
  deptName?: string;
  majorCode: string;
  majorName: string;
  status: string;
  createdTime?: string;
  updatedTime?: string;
}

export interface BaseGradeItem {
  id: number;
  gradeCode: string;
  gradeName: string;
  status: string;
  createdTime?: string;
  updatedTime?: string;
}

export interface BaseInternshipBaseItem {
  id: number;
  baseCode: string;
  baseName: string;
  province?: string;
  city?: string;
  district?: string;
  address?: string;
  contactPerson?: string;
  contactPhone?: string;
  status: string;
  createdTime?: string;
  updatedTime?: string;
}

export interface SysParamItem {
  id: number;
  paramCode: string;
  paramName: string;
  paramValue: string;
  paramType: string;
  status: string;
  remark?: string;
  updatedTime?: string;
}

export interface SysOperationLogItem {
  id: number;
  operatorId?: number;
  operatorName?: string;
  moduleCode: string;
  actionCode: string;
  businessType?: string;
  businessId?: number;
  requestMethod?: string;
  requestUri?: string;
  requestIp?: string;
  requestParams?: string;
  responseData?: string;
  operationStatus: string;
  errorMessage?: string;
  operateTime?: string;
}

export interface SysLoginLogItem {
  id: number;
  accountNo: string;
  userId?: number;
  loginIp?: string;
  userAgent?: string;
  loginResult: string;
  failReason?: string;
  loginTime?: string;
}

export interface SysRoleItem {
  id: number;
  roleCode: string;
  roleName: string;
  dataScope: string;
  status: string;
  remark?: string;
}

export interface SysMenuNode {
  id: number;
  parentId?: number;
  menuName: string;
  menuType: string;
  routePath?: string;
  componentPath?: string;
  permissionCode?: string;
  icon?: string;
  sortNo?: number;
  visible?: number;
  status?: string;
  selected?: boolean;
  children: SysMenuNode[];
}

export interface UserImportError {
  rowNumber: number;
  message: string;
}

export interface UserImportResultData {
  totalRows: number;
  successRows: number;
  failedRows: number;
  errors: UserImportError[];
}
