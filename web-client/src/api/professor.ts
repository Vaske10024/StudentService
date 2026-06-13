import { apiRequest } from './client';

export const professorApi = {
  studentsForSubject: (drziPredmetId: string | number) => apiRequest<Record<string, unknown>[]>(`/api/drzi/${drziPredmetId}/studenti`),
  resultsForExam: (examId: string | number) => apiRequest<Record<string, unknown>[]>(`/api/ispit/${examId}/rezultati`),
  preExamForStudent: (studentIndeksId: string | number, predmetId: string | number, skolskaGodinaId: string | number) =>
    apiRequest<Record<string, unknown>[]>(`/api/predispit/admin/ostvareno/detalji?studentIndeksId=${studentIndeksId}&predmetId=${predmetId}&skolskaGodinaId=${skolskaGodinaId}`)
};
