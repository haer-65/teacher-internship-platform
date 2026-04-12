import { defineStore } from 'pinia';
import { deleteNoticesApi, markAllNoticeReadApi, markNoticeReadApi, queryNoticeUnreadCountApi } from '@/api/modules/notice';
import type { IdValue } from '@/types/api';

interface NoticeState {
  unreadCount: number;
  lastRefreshAt: number;
  pollingTimer: number | null;
}

export const useNoticeStore = defineStore('notice', {
  state: (): NoticeState => ({
    unreadCount: 0,
    lastRefreshAt: 0,
    pollingTimer: null
  }),
  actions: {
    setUnreadCount(count: number) {
      this.unreadCount = Math.max(0, Number(count || 0));
      this.lastRefreshAt = Date.now();
    },
    async refreshUnreadCount() {
      const resp = await queryNoticeUnreadCountApi();
      this.setUnreadCount(resp.data.unreadCount);
      return this.unreadCount;
    },
    async markRead(noticeId: IdValue) {
      const resp = await markNoticeReadApi(noticeId);
      this.setUnreadCount(resp.data.unreadCount);
      return this.unreadCount;
    },
    async markAllRead() {
      const resp = await markAllNoticeReadApi();
      this.setUnreadCount(resp.data.unreadCount);
      return this.unreadCount;
    },
    async deleteNotices(noticeIds: IdValue[]) {
      const resp = await deleteNoticesApi(noticeIds);
      this.setUnreadCount(resp.data.unreadCount);
      return this.unreadCount;
    },
    startPolling(intervalMs = 15_000) {
      if (this.pollingTimer !== null || typeof window === 'undefined') {
        return;
      }
      this.pollingTimer = window.setInterval(() => {
        this.refreshUnreadCount().catch(() => undefined);
      }, intervalMs);
    },
    stopPolling() {
      if (this.pollingTimer === null || typeof window === 'undefined') {
        return;
      }
      window.clearInterval(this.pollingTimer);
      this.pollingTimer = null;
    },
    reset() {
      this.stopPolling();
      this.unreadCount = 0;
      this.lastRefreshAt = 0;
    }
  }
});
