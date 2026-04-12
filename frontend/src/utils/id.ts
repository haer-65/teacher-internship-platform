export function normalizeRouteId(value: unknown): string {
  const rawValue = Array.isArray(value) ? value[0] : value;
  if (rawValue === undefined || rawValue === null) {
    return '';
  }
  return String(rawValue).trim();
}

export function isValidRouteId(value: unknown): boolean {
  return normalizeRouteId(value) !== '';
}

