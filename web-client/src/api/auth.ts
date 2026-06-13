import { apiRequest, resetCsrf } from './client';
import type { AuthUser } from './types';

export async function login(username: string, password: string): Promise<AuthUser> {
  const response = await apiRequest<{ user: AuthUser }>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password })
  });
  return response.user;
}

export async function logout(): Promise<void> {
  await apiRequest<void>('/api/auth/logout', { method: 'POST' });
  resetCsrf();
}

export async function me(): Promise<AuthUser> {
  const response = await apiRequest<{ user: AuthUser }>('/api/auth/me');
  return response.user;
}

export async function changePassword(currentPassword: string, newPassword: string): Promise<void> {
  await apiRequest<void>('/api/auth/password', {
    method: 'POST',
    body: JSON.stringify({ currentPassword, newPassword })
  });
}
