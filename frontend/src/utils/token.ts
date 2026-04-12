import { STORAGE_KEYS } from '@/constants/storage';
import type { LoginUserInfo, MenuNode, RoleOption } from '@/types/api';

export interface PersistAuthPayload {
  token: string;
  tokenExpireAt: number;
  rememberMe: boolean;
  currentRoleCode: string;
  userInfo: LoginUserInfo | null;
  roleOptions: RoleOption[];
  menuTree: MenuNode[];
  permissionCodes: string[];
}

function getStorage(rememberMe: boolean): Storage {
  return rememberMe ? localStorage : sessionStorage;
}

export function saveAuthState(payload: PersistAuthPayload): void {
  const storage = getStorage(payload.rememberMe);
  const backupStorage = payload.rememberMe ? sessionStorage : localStorage;

  Object.values(STORAGE_KEYS).forEach((key) => backupStorage.removeItem(key));

  storage.setItem(STORAGE_KEYS.TOKEN, payload.token);
  storage.setItem(STORAGE_KEYS.TOKEN_EXPIRE_AT, String(payload.tokenExpireAt));
  storage.setItem(STORAGE_KEYS.REMEMBER_ME, String(payload.rememberMe));
  storage.setItem(STORAGE_KEYS.CURRENT_ROLE_CODE, payload.currentRoleCode || '');
  storage.setItem(STORAGE_KEYS.USER_INFO, JSON.stringify(payload.userInfo));
  storage.setItem(STORAGE_KEYS.ROLE_OPTIONS, JSON.stringify(payload.roleOptions));
  storage.setItem(STORAGE_KEYS.MENU_TREE, JSON.stringify(payload.menuTree));
  storage.setItem(STORAGE_KEYS.PERMISSION_CODES, JSON.stringify(payload.permissionCodes));
}

export function clearAuthState(): void {
  Object.values(STORAGE_KEYS).forEach((key) => {
    localStorage.removeItem(key);
    sessionStorage.removeItem(key);
  });
}

export function loadAuthState() {
  const fromLocal = localStorage.getItem(STORAGE_KEYS.TOKEN);
  const storage = fromLocal ? localStorage : sessionStorage;

  const token = storage.getItem(STORAGE_KEYS.TOKEN);
  const tokenExpireAt = Number(storage.getItem(STORAGE_KEYS.TOKEN_EXPIRE_AT) || 0);
  const rememberMe = storage.getItem(STORAGE_KEYS.REMEMBER_ME) === 'true';
  const currentRoleCode = storage.getItem(STORAGE_KEYS.CURRENT_ROLE_CODE) || '';
  const userInfoRaw = storage.getItem(STORAGE_KEYS.USER_INFO);
  const roleOptionsRaw = storage.getItem(STORAGE_KEYS.ROLE_OPTIONS);
  const menuTreeRaw = storage.getItem(STORAGE_KEYS.MENU_TREE);
  const permissionCodesRaw = storage.getItem(STORAGE_KEYS.PERMISSION_CODES);

  return {
    token,
    tokenExpireAt,
    rememberMe,
    currentRoleCode,
    userInfo: userInfoRaw ? (JSON.parse(userInfoRaw) as LoginUserInfo) : null,
    roleOptions: roleOptionsRaw ? (JSON.parse(roleOptionsRaw) as RoleOption[]) : [],
    menuTree: menuTreeRaw ? (JSON.parse(menuTreeRaw) as MenuNode[]) : [],
    permissionCodes: permissionCodesRaw ? (JSON.parse(permissionCodesRaw) as string[]) : []
  };
}

