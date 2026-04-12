import type { App } from 'vue';
import type { Pinia } from 'pinia';
import { useAuthStore } from '@/store/modules/auth';

function hasAnyPermission(userPermissions: string[], required: string[]): boolean {
  if (!required.length) {
    return true;
  }
  return required.some((perm) => userPermissions.includes(perm));
}

export function setupPermissionDirective(app: App, pinia: Pinia): void {
  const applyPermission = (el: HTMLElement, value: unknown) => {
    const authStore = useAuthStore(pinia);
    const required = Array.isArray(value) ? value : value ? [String(value)] : [];
    const allowed = hasAnyPermission(authStore.permissionCodes, required);
    if (!allowed) {
      el.style.display = 'none';
    } else {
      el.style.display = '';
    }
  };

  app.directive('permission', {
    mounted(el, binding) {
      applyPermission(el as HTMLElement, binding.value);
    },
    updated(el, binding) {
      applyPermission(el as HTMLElement, binding.value);
    }
  });
}
