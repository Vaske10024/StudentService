import { apiRequest } from './client';

export const extendedApi = {
  notifications: () => apiRequest<unknown[]>('/api/notifications'),
  markNotificationRead: (id: string | number) => apiRequest(`/api/notifications/${id}/read`, { method: 'PATCH' }),
  requests: (indeksId: string) => apiRequest<unknown[]>(`/api/requests?indeksId=${encodeURIComponent(indeksId)}`),
  createRequest: (body: unknown) => apiRequest('/api/requests', { method: 'POST', body: JSON.stringify(body) }),
  adminRequests: () => apiRequest<unknown[]>('/api/requests/admin'),
  decideRequest: (id: string | number, approved: boolean, note: string) =>
    apiRequest(`/api/requests/${id}/${approved ? 'approve' : 'reject'}?note=${encodeURIComponent(note)}`, { method: 'POST' }),
  enrollments: () => apiRequest<unknown[]>('/api/enrollment/applications'),
  approveEnrollment: (id: string | number, initialPassword: string) => apiRequest(`/api/enrollment/applications/${id}/approve`, { method: 'POST', body: JSON.stringify({ initialPassword }) }),
  rejectEnrollment: (id: string | number, reason: string) => apiRequest(`/api/enrollment/applications/${id}/reject?reason=${encodeURIComponent(reason)}`, { method: 'POST' }),
  addLedgerPayment: (indeksId: string, amountEur: string, description: string) =>
    apiRequest(`/api/finance/${encodeURIComponent(indeksId)}/payments?amountEur=${encodeURIComponent(amountEur)}&description=${encodeURIComponent(description)}`, { method: 'POST' })
};
