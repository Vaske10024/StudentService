import { apiRequest, apiUrl, ApiError } from './client';
import type { Lead, LeadAuditLog, LeadEmailMessage, LeadEmailMonitoring, LeadEmailTemplate, LeadExportLog, Page } from './types';

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
  detail: (leadId: number) => apiRequest<Lead>(`/api/leads/admin/${leadId}`),
  history: (leadId: number) => apiRequest<LeadEmailMessage[]>(`/api/leads/admin/${leadId}/emails`),
  templates: () => apiRequest<LeadEmailTemplate[]>('/api/leads/admin/templates'),
  sendEmail: (leadId: number, body: { templateId?: number; subject: string; body: string }) =>
    apiRequest<LeadEmailMessage>(`/api/leads/admin/${leadId}/emails`, { method: 'POST', body: JSON.stringify(body) }),
  updateStatus: (leadId: number, status: string) =>
    apiRequest<Lead>(`/api/leads/admin/${leadId}/status`, { method: 'PATCH', body: JSON.stringify({ status }) }),
  createTemplate: (body: { name: string; subject: string; body: string; active: boolean }) =>
    apiRequest<LeadEmailTemplate>('/api/leads/admin/templates', { method: 'POST', body: JSON.stringify(body) }),
  updateTemplate: (id: number, body: { name: string; subject: string; body: string; active: boolean }) =>
    apiRequest<LeadEmailTemplate>(`/api/leads/admin/templates/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
  monitoringEmails: (page = 0, size = 50) =>
    apiRequest<Page<LeadEmailMonitoring>>(`/api/leads/admin/monitoring/emails?page=${page}&size=${size}`),
  monitoringAudit: (page = 0, size = 50) =>
    apiRequest<Page<LeadAuditLog>>(`/api/leads/admin/monitoring/audit?page=${page}&size=${size}`),
  monitoringExports: (page = 0, size = 50) =>
    apiRequest<Page<LeadExportLog>>(`/api/leads/admin/monitoring/exports?page=${page}&size=${size}`),
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
