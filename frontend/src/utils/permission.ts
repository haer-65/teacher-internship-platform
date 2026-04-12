export function hasPermission(userPermissions: string[] | undefined | null, permission: string): boolean {
  if (!userPermissions || !permission) {
    return false;
  }
  return userPermissions.includes(permission);
}

export function hasAnyPermission(userPermissions: string[] | undefined | null, permissions: string[]): boolean {
  if (!userPermissions || !permissions?.length) {
    return false;
  }
  return permissions.some((permission) => userPermissions.includes(permission));
}
