import { apiRequest } from './client';
import type { YearEnrollmentEligibility, YearEnrollmentRequest } from './types';

export interface YearEnrollmentAdminFilters {
  status?: string;
  type?: string;
  targetSchoolYearId?: string;
  studentIndeksId?: string;
}

function queryString(filters: YearEnrollmentAdminFilters): string {
  const params = new URLSearchParams();
  Object.entries(filters).forEach(([key, value]) => {
    if (value) params.set(key, value);
  });
  const query = params.toString();
  return query ? `?${query}` : '';
}

export const yearEnrollmentApi = {
  eligibility: () => apiRequest<YearEnrollmentEligibility>('/api/enrollment/year-requests/me/eligibility'),
  mine: () => apiRequest<YearEnrollmentRequest[]>('/api/enrollment/year-requests/me'),
  submit: (body: { type: string; targetSchoolYearId: number; transferredSubjectIds: number[]; studentNote?: string }) =>
    apiRequest<YearEnrollmentRequest>('/api/enrollment/year-requests/me', {
      method: 'POST',
      body: JSON.stringify(body)
    }),
  resubmit: (id: number, body: { transferredSubjectIds: number[]; studentNote?: string }) =>
    apiRequest<YearEnrollmentRequest>(`/api/enrollment/year-requests/me/${id}`, {
      method: 'PUT',
      body: JSON.stringify(body)
    }),
  cancel: (id: number) => apiRequest<YearEnrollmentRequest>(`/api/enrollment/year-requests/me/${id}/cancel`, { method: 'POST' }),
  adminList: (filters: YearEnrollmentAdminFilters = {}) =>
    apiRequest<YearEnrollmentRequest[]>(`/api/enrollment/year-requests/admin${queryString(filters)}`),
  adminDetail: (id: number) => apiRequest<YearEnrollmentRequest>(`/api/enrollment/year-requests/admin/${id}`),
  checklist: (id: number, body: { contractReceived: boolean; paymentConfirmed: boolean; documentationComplete: boolean; note?: string }) =>
    apiRequest<YearEnrollmentRequest>(`/api/enrollment/year-requests/admin/${id}/checklist`, {
      method: 'PATCH',
      body: JSON.stringify(body)
    }),
  approve: (id: number) => apiRequest<YearEnrollmentRequest>(`/api/enrollment/year-requests/admin/${id}/approve`, { method: 'POST' }),
  reject: (id: number, reason: string) => apiRequest<YearEnrollmentRequest>(`/api/enrollment/year-requests/admin/${id}/reject`, {
    method: 'POST',
    body: JSON.stringify({ reason })
  }),
  needsChanges: (id: number, reason: string) => apiRequest<YearEnrollmentRequest>(`/api/enrollment/year-requests/admin/${id}/needs-changes`, {
    method: 'POST',
    body: JSON.stringify({ reason })
  })
};
