export type Role = 'STUDENT' | 'PROFESSOR' | 'ADMIN';

export interface AuthUser {
  id: number;
  username: string;
  role: Role;
  enabled: boolean;
  linkedStudentPodaciId?: number | null;
  linkedStudentIndeksId?: number | null;
  linkedNastavnikId?: number | null;
}

export interface ApiErrorBody {
  timestamp: string;
  status: number;
  error: string;
  code: string;
  message: string;
  path?: string;
  details?: unknown;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface StudentIndex {
  id: number;
  broj: number;
  godina: number;
  studProgramOznaka: string;
  aktivan: boolean;
  nacinFinansiranja?: string;
}

export interface SubjectInstructor {
  id: number;
  nastavnikId: number;
  nastavnikImePrezime: string;
  uloga?: string | null;
}

export interface StudentSubject {
  listeningId: number;
  realizationId: number;
  subjectId: number;
  code: string;
  name: string;
  description?: string | null;
  ects?: number | null;
  studyYear?: number | null;
  semester?: number | null;
  programCode: string;
  schoolYear: string;
  realizationStatus: string;
  instructors: SubjectInstructor[];
}

export interface StudentProfile {
  indeks?: StudentIndex;
  slusaPredmete?: unknown[];
  nepolozeniPredmeti?: unknown[];
}

export interface StudentDashboard {
  student?: Record<string, unknown>;
  activeIndex?: StudentIndex;
  allIndexes?: StudentIndex[];
  currentSubjects?: StudentSubject[];
  passedSubjects?: unknown[];
  failedOrNotPassedSubjects?: unknown[];
  activeExamRegistrations?: unknown[];
  previousExamAttempts?: unknown[];
  studyEnrollments?: unknown[];
  renewals?: unknown[];
  payments?: unknown[];
  balance?: Record<string, unknown>;
  schoolYear?: Record<string, unknown>;
  status?: Record<string, unknown>;
  statusHistory?: unknown[];
}

export interface ProfessorDashboard {
  user?: AuthUser;
  subjects?: unknown[];
  exams?: unknown[];
}
