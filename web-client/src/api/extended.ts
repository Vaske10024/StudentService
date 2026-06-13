import { apiRequest } from './client';

export const extendedApi = {
  notifications: () => apiRequest<unknown[]>('/api/notifications'),
  markNotificationRead: (id: string | number) => apiRequest(`/api/notifications/${id}/read`, { method: 'PATCH' }),
  requests: (indeksId: string) => apiRequest<unknown[]>(`/api/requests?indeksId=${encodeURIComponent(indeksId)}`),
  createRequest: (body: unknown) => apiRequest('/api/requests', { method: 'POST', body: JSON.stringify(body) }),
  enrollments: () => apiRequest<unknown[]>('/api/enrollment/applications'),
  addLedgerPayment: (indeksId: string, amountEur: string, description: string) =>
    apiRequest(`/api/finance/${encodeURIComponent(indeksId)}/payments?amountEur=${encodeURIComponent(amountEur)}&description=${encodeURIComponent(description)}`, { method: 'POST' })
};
