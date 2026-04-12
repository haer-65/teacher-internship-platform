import http from '@/api/http';
import type {
  ApiResponse,
  IdValue,
  NoticeDetailData,
  NoticeListItem,
  NoticePageData,
  NoticeTriggerResultData,
  NoticeUnreadData
} from '@/types/api';

export interface NoticePageQuery {
  page: number;
  size: number;
  keyword?: string;
  readFlag?: number;
}

export interface NoticeRecentQuery {
  limit?: number;
  unreadOnly?: number;
}

export interface NoticeSendRequest {
  noticeType?: string;
  noticeLevel?: string;
  noticeTitle: string;
  noticeContent: string;
  targetUserIds?: number[];
  targetRoleCode?: string;
  relatedBusinessType?: string;
  relatedBusinessId?: number;
  sendEmail?: boolean;
}

export function queryNoticePageApi(params: NoticePageQuery): Promise<ApiResponse<NoticePageData>> {
  return http.get('/v1/notice/page', { params });
}

export function queryNoticeDetailApi(noticeId: IdValue): Promise<ApiResponse<NoticeDetailData>> {
  return http.get(`/v1/notice/detail/${noticeId}`);
}

export function markNoticeReadApi(noticeId: IdValue): Promise<ApiResponse<NoticeUnreadData>> {
  return http.post(`/v1/notice/read/${noticeId}`);
}

export function markAllNoticeReadApi(): Promise<ApiResponse<NoticeUnreadData>> {
  return http.post('/v1/notice/read-all');
}

export function deleteNoticesApi(noticeIds: IdValue[]): Promise<ApiResponse<NoticeUnreadData>> {
  return http.post('/v1/notice/delete', noticeIds);
}

export function queryNoticeUnreadCountApi(): Promise<ApiResponse<NoticeUnreadData>> {
  return http.get('/v1/notice/unread-count');
}

export function queryRecentNoticesApi(params?: NoticeRecentQuery): Promise<ApiResponse<NoticeListItem[]>> {
  return http.get('/v1/notice/recent', { params });
}

export function sendNoticeApi(data: NoticeSendRequest): Promise<ApiResponse<number>> {
  return http.post('/v1/notice/send', data);
}

export function triggerMaterialDeadlineNoticeApi(): Promise<ApiResponse<NoticeTriggerResultData>> {
  return http.post('/v1/notice/trigger/material-deadline');
}
