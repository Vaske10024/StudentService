import type { ApiErrorBody } from './types';

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '';
let csrfToken: string | null = null;
let csrfHeaderName = 'X-XSRF-TOKEN';

export class ApiError extends Error {
  status: number;
  body?: ApiErrorBody;

  constructor(status: number, body?: ApiErrorBody) {
    super(body?.message ?? `HTTP ${status}`);
    this.name = 'ApiError';
    this.status = status;
    this.body = body;
  }
}

export async function ensureCsrf(): Promise<void> {
  if (csrfToken) return;
  const res = await fetch(`${API_BASE}/api/auth/csrf`, {
    credentials: 'include'
  });
  if (!res.ok) throw new ApiError(res.status);
  const data = await res.json() as { headerName: string; token: string };
  csrfHeaderName = data.headerName || csrfHeaderName;
  csrfToken = data.token;
}

type RequestOptions = RequestInit & { skipCsrf?: boolean };

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const method = (options.method ?? 'GET').toUpperCase();
  const mutating = !['GET', 'HEAD', 'OPTIONS'].includes(method);
  if (mutating && !options.skipCsrf) await ensureCsrf();

  const headers = new Headers(options.headers);
  if (!headers.has('Content-Type') && options.body) headers.set('Content-Type', 'application/json');
  if (mutating && csrfToken) headers.set(csrfHeaderName, csrfToken);

  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    method,
    headers,
    credentials: 'include'
  });

  if (res.status === 204) return undefined as T;

  const contentType = res.headers.get('content-type') ?? '';
  const body = contentType.includes('application/json') ? await res.json() : undefined;
  if (!res.ok) {
    if (res.status === 401) window.dispatchEvent(new CustomEvent('auth:unauthorized', { detail: { status: res.status, body } }));
    if (res.status === 403) window.dispatchEvent(new CustomEvent('auth:forbidden', { detail: { status: res.status, body } }));
    throw new ApiError(res.status, body);
  }
  return body as T;
}

export function resetCsrf(): void {
  csrfToken = null;
}

export function apiUrl(path: string): string {
  return `${API_BASE}${path}`;
}

export function apiErrorMessage(error: unknown, fallback = 'Request failed.'): string {
  if (!(error instanceof ApiError)) return error instanceof Error ? error.message : fallback;
  const details = error.body?.details;
  if (!Array.isArray(details)) return error.message;
  const messages = details.map((detail) => {
    if (!detail || typeof detail !== 'object') return '';
    const item = detail as { field?: unknown; message?: unknown };
    return `${String(item.field ?? 'field')}: ${String(item.message ?? 'invalid value')}`;
  }).filter(Boolean);
  return messages.length ? messages.join('; ') : error.message;
}
