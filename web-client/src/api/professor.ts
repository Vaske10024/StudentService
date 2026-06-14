import { apiRequest } from './client';

export const professorApi = {
  studentsForSubject: (drziPredmetId: string | number) => apiRequest<Record<string, unknown>[]>(`/api/drzi/${drziPredmetId}/studenti`),
  resultsForExam: (examId: string | number) => apiRequest<Record<string, unknown>[]>(`/api/ispit/${examId}/rezultati`),
  registrationsForExam: (examId: string | number) => apiRequest<Record<string, unknown>[]>(`/api/ispit/${examId}/prijave`),
  updateResult: (body: unknown) => apiRequest<number>('/api/ispit/prijava/rezultat', { method: 'PATCH', body: JSON.stringify(body) }),
  recordAttendance: (body: unknown) => apiRequest<number>('/api/ispit/izlazak', { method: 'POST', body: JSON.stringify(body) }),
  lockExam: (examId: string | number) => apiRequest(`/api/ispit/${examId}/zakljucaj`, { method: 'PATCH' }),
  preExamDefinitions: (predmetId: string | number, skolskaGodinaId: string | number) =>
    apiRequest<Record<string, unknown>[]>(`/api/predispit/admin/definicije?predmetId=${predmetId}&skolskaGodinaId=${skolskaGodinaId}`),
  createPreExamDefinition: (body: unknown) => apiRequest<number>('/api/predispit/admin/definicija', { method: 'POST', body: JSON.stringify(body) }),
  upsertPreExam: (body: unknown) => apiRequest<number>('/api/predispit/admin/ostvareno', { method: 'POST', body: JSON.stringify(body) }),
  preExamForStudent: (studentIndeksId: string | number, predmetId: string | number, skolskaGodinaId: string | number) =>
    apiRequest<Record<string, unknown>[]>(`/api/predispit/admin/ostvareno/detalji?studentIndeksId=${studentIndeksId}&predmetId=${predmetId}&skolskaGodinaId=${skolskaGodinaId}`)
};
