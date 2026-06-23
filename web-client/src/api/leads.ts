import { apiRequest, apiUrl, ApiError } from './client';
import type { Lead, Page } from './types';

export interface LeadFormPayload {
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  interestedProgram?: string;
  source?: string;
  note?: string;
  privacyConsent: boolean;
}

export const leadsApi = {
  submit: (body: LeadFormPayload) =>
    apiRequest<{ message: string }>('/api/leads', { method: 'POST', body: JSON.stringify(body) }),
  adminList: (page = 0, size = 20) =>
    apiRequest<Page<Lead>>(`/api/leads/admin?page=${page}&size=${size}`),
  exportCsv: async () => {
    const response = await fetch(apiUrl('/api/leads/admin/export.csv'), {
      credentials: 'include'
    });
    if (!response.ok) throw new ApiError(response.status);
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'potential-student-leads.csv';
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  }
};
