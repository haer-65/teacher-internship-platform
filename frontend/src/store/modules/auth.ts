import { defineStore } from 'pinia';
import type { AuthContextData, LoginUserInfo, MenuNode, RoleOption } from '@/types/api';
import { clearAuthState, loadAuthState, saveAuthState } from '@/utils/token';
import { meApi } from '@/api/modules/auth';

interface AuthState {
  token: string;
  tokenExpireAt: number;
  rememberMe: boolean;
  contextSynced: boolean;
  currentRoleCode: string;
  userInfo: LoginUserInfo | null;
  roleOptions: RoleOption[];
  menuTree: MenuNode[];
  permissionCodes: string[];
}

const EMPTY_STATE: AuthState = {
  token: '',
  tokenExpireAt: 0,
  rememberMe: false,
  contextSynced: false,
  currentRoleCode: '',
  userInfo: null,
  roleOptions: [],
  menuTree: [],
  permissionCodes: []
};

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({ ...EMPTY_STATE }),
  getters: {
    isTokenExpired(state): boolean {
      if (!state.token || !state.tokenExpireAt) {
        return true;
      }
      return Date.now() >= state.tokenExpireAt;
    },
    hasContext(state): boolean {
      return !!state.userInfo;
    }
  },
  actions: {
    bootstrapFromStorage() {
      const loaded = loadAuthState();
      this.token = loaded.token || '';
      this.tokenExpireAt = loaded.tokenExpireAt || 0;
      this.rememberMe = loaded.rememberMe || false;
      this.contextSynced = false;
      this.currentRoleCode = loaded.currentRoleCode || '';
      this.userInfo = loaded.userInfo || null;
      this.roleOptions = loaded.roleOptions || [];
      this.menuTree = loaded.menuTree || [];
      this.permissionCodes = loaded.permissionCodes || [];
      this.ensureAuthStateValid();
    },
    ensureAuthStateValid() {
      if (this.token && this.isTokenExpired) {
        this.resetAuth();
      }
    },
    applyAuthContext(payload: {
      context: AuthContextData;
      rememberMe: boolean;
      keepTokenIfMissing?: boolean;
    }) {
      const context = payload.context;

      if (context.token) {
        this.token = context.token;
        this.tokenExpireAt = Date.now() + Number(context.expiresIn || 0) * 1000;
      } else if (!payload.keepTokenIfMissing) {
        this.token = '';
        this.tokenExpireAt = 0;
      }

      this.rememberMe = payload.rememberMe;
      this.contextSynced = true;
      this.currentRoleCode = context.currentRoleCode;
      this.userInfo = context.user;
      this.roleOptions = context.roles || [];
      this.menuTree = context.menuTree || [];
      this.permissionCodes = context.permissionCodes || [];

      if (this.token) {
        saveAuthState({
          token: this.token,
          tokenExpireAt: this.tokenExpireAt,
          rememberMe: this.rememberMe,
          currentRoleCode: this.currentRoleCode,
          userInfo: this.userInfo,
          roleOptions: this.roleOptions,
          menuTree: this.menuTree,
          permissionCodes: this.permissionCodes
        });
      }
    },
    async refreshMe() {
      if (!this.token) {
        return;
      }
      const resp = await meApi();
      this.applyAuthContext({
        context: resp.data,
        rememberMe: this.rememberMe,
        keepTokenIfMissing: true
      });
    },
    resetAuth() {
      this.token = '';
      this.tokenExpireAt = 0;
      this.rememberMe = false;
      this.contextSynced = false;
      this.currentRoleCode = '';
      this.userInfo = null;
      this.roleOptions = [];
      this.menuTree = [];
      this.permissionCodes = [];
      clearAuthState();
    }
  }
});
