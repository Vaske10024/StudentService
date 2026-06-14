import { apiRequest } from './client';
import type { AvailableExam, ProfessorDashboard, StudentDashboard, StudentProfile, StudentSubject } from './types';

export const meApi = {
  studentDashboard: () => apiRequest<StudentDashboard>('/api/me/student/dashboard'),
  studentProfile: () => apiRequest<StudentProfile>('/api/me/student/profile'),
  studentSubjects: () => apiRequest<StudentSubject[]>('/api/me/student/subjects'),
  studentExams: () => apiRequest<StudentDashboard>('/api/me/student/exams'),
  availableStudentExams: () => apiRequest<AvailableExam[]>('/api/me/student/available-exams'),
  studentPayments: () => apiRequest<StudentDashboard>('/api/me/student/payments'),
  professorDashboard: () => apiRequest<ProfessorDashboard>('/api/me/professor/dashboard'),
  professorSubjects: () => apiRequest<unknown[]>('/api/me/professor/subjects'),
  professorExams: () => apiRequest<unknown[]>('/api/me/professor/exams')
};
