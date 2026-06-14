import { apiRequest } from './client';
import type { Page, StudentDashboard } from './types';

export const adminApi = {
  students: (page = 0, size = 20) => apiRequest<Page<Record<string, unknown>>>(`/api/student/svi?page=${page}&size=${size}`),
  studentDetails: (studentId: string | number) => apiRequest<Record<string, unknown>>(`/api/student/podaci/${studentId}`),
  studentIndexes: (studentId: string | number) => apiRequest<Record<string, unknown>[]>(`/api/student/indeksi/${studentId}`),
  studentDashboard: (indexId: string | number) => apiRequest<StudentDashboard>(`/api/student/dashboard/${indexId}`),
  programs: () => apiRequest<Record<string, unknown>[]>('/api/studprogram/all/sorted'),
  studyTypes: () => apiRequest<Record<string, unknown>[]>('/api/studprogram/vrste'),
  createStudyType: (body: unknown) => apiRequest<number>('/api/studprogram/vrste', { method: 'POST', body: JSON.stringify(body) }),
  updateStudyType: (id: string | number, body: unknown) => apiRequest(`/api/studprogram/vrste/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
  deleteStudyType: (id: string | number) => apiRequest(`/api/studprogram/vrste/${id}`, { method: 'DELETE' }),
  createProgram: (body: unknown) => apiRequest<number>('/api/studprogram', { method: 'POST', body: JSON.stringify(body) }),
  professors: () => apiRequest<Record<string, unknown>[]>('/api/nastavnik/all'),
  professor: (id: string | number) => apiRequest<Record<string, unknown>>(`/api/nastavnik/${id}`),
  createProfessor: (body: unknown) => apiRequest<number>('/api/nastavnik/add', { method: 'POST', body: JSON.stringify(body) }),
  provisionProfessor: (id: string | number) => apiRequest<{ professorId: number; accountId: number; username: string; temporaryPassword?: string | null; accountCreated: boolean }>(`/api/nastavnik/${id}/provision-account`, { method: 'POST' }),
  subjects: () => apiRequest<Record<string, unknown>[]>('/api/predmet/all'),
  createSubject: (body: unknown) => apiRequest<number>('/api/predmet/admin/create', { method: 'POST', body: JSON.stringify(body) }),
  assignments: () => apiRequest<Record<string, unknown>[]>('/api/drzi/all'),
  createAssignment: (body: unknown) => apiRequest<number>('/api/drzi/create', { method: 'POST', body: JSON.stringify(body) }),
  realizations: (schoolYearId?: string | number) => apiRequest<Record<string, unknown>[]>(`/api/realizacija/all${schoolYearId ? `?skolskaGodinaId=${schoolYearId}` : ''}`),
  generateRealizations: (programId: string | number, schoolYearId?: string | number) =>
    apiRequest<Record<string, unknown>[]>(`/api/realizacija/generate?programId=${programId}${schoolYearId ? `&skolskaGodinaId=${schoolYearId}` : ''}`, { method: 'POST' }),
  createIndex: (body: unknown) => apiRequest<{ indexId: number; username: string; temporaryPassword?: string | null; accountCreated: boolean }>('/api/student/saveindeks/provision', { method: 'POST', body: JSON.stringify(body) }),
  enrollStudyYear: (indexId: string | number, studyYear: number) =>
    apiRequest<number>('/api/studij/upis', { method: 'POST', body: JSON.stringify({ indeksId: Number(indexId), upisujeGodinu: studyYear }) }),
  syncStudentSubjects: (indexId: string | number) =>
    apiRequest<number>(`/api/studij/sync-subjects?indeksId=${encodeURIComponent(indexId)}`, { method: 'POST' }),
  addSubjectToIndex: (indexId: string | number, assignmentId: string | number) =>
    apiRequest<number>('/api/slusa/create', { method: 'POST', body: JSON.stringify({ indeksId: Number(indexId), drziPredmetId: Number(assignmentId) }) }),
  schoolYears: () => apiRequest<Record<string, unknown>[]>('/api/sg/all'),
  createSchoolYear: (body: unknown) => apiRequest<number>('/api/sg', { method: 'POST', body: JSON.stringify(body) }),
  activateSchoolYear: (id: string | number) => apiRequest(`/api/sg/${id}/aktiviraj`, { method: 'PATCH' }),
  examPeriods: () => apiRequest<Record<string, unknown>[]>('/api/rok/all'),
  examsForPeriod: (periodId: string | number) => apiRequest<Record<string, unknown>[]>(`/api/rok/${periodId}/ispiti`)
  ,createExamPeriod: (body: unknown) => apiRequest<number>('/api/rok/create', { method: 'POST', body: JSON.stringify(body) })
  ,updateExamPeriod: (id: string | number, body: unknown) => apiRequest(`/api/rok/${id}`, { method: 'PUT', body: JSON.stringify(body) })
  ,createExam: (body: unknown) => apiRequest<number>('/api/ispit/admin/create', { method: 'POST', body: JSON.stringify(body) })
  ,updateExamTime: (id: string | number, body: unknown) => apiRequest(`/api/ispit/admin/${id}/vreme`, { method: 'PATCH', body: JSON.stringify(body) })
  ,lockExam: (id: string | number) => apiRequest(`/api/ispit/admin/${id}/zakljucaj`, { method: 'PATCH' })
  ,voidRegistration: (id: string | number, reason: string) => apiRequest(`/api/ispit/prijava/${id}/ponisti`, { method: 'PATCH', body: JSON.stringify({ reason }) })
  ,recognizeSubject: (studentIndeksId: string | number, predmetId: string | number, ocena: number, napomena?: string) =>
    apiRequest<number>(`/api/ispit/priznaj?studentIndeksId=${encodeURIComponent(studentIndeksId)}&predmetId=${encodeURIComponent(predmetId)}&ocena=${ocena}${napomena ? `&napomena=${encodeURIComponent(napomena)}` : ''}`, { method: 'POST' })
};
