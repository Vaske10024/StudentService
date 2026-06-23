export type Role = 'STUDENT' | 'PROFESSOR' | 'ADMIN' | 'HEAD_ADMIN';

export interface AuthUser {
  id: number;
  username: string;
  role: Role;
  enabled: boolean;
  linkedStudentPodaciId?: number | null;
  linkedStudentIndeksId?: number | null;
  linkedNastavnikId?: number | null;
  mustChangePassword: boolean;
  permissions: string[];
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

export interface Lead {
  id: number;
  initials: string;
  firstName?: string | null;
  lastName?: string | null;
  fullName?: string | null;
  email?: string | null;
  phone?: string | null;
  interestedProgram?: string | null;
  source?: string | null;
  note?: string | null;
  createdAt: string;
  fullAccess: boolean;
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

export interface AvailableExam {
  examId: number;
  periodId?: number | null;
  subjectId: number;
  subjectCode?: string | null;
  subjectName: string;
  professorName?: string | null;
  examDate?: string | null;
  examTime?: string | null;
  registrationStart?: string | null;
  registrationEnd?: string | null;
  cancellationEnd?: string | null;
  periodActive: boolean;
  locked: boolean;
  eligible: boolean;
  eligibilityCode: string;
  eligibilityMessage: string;
  activeRegistrationId?: number | null;
  cancellationAllowed: boolean;
}

export interface ProfessorDashboard {
  user?: AuthUser;
  subjects?: unknown[];
  exams?: unknown[];
}

export interface SchoolYear {
  id: number;
  godina: string;
  aktivna?: boolean;
}

export interface EnrollmentSubject {
  id: number;
  sifra: string;
  naziv: string;
  opis?: string | null;
  espb?: number | null;
}

export interface YearEnrollmentEligibility {
  indeksId: number;
  currentStudyYear?: number | null;
  requestedStudyYear?: number | null;
  programDuration?: number | null;
  earnedEcts: number;
  regularEnrollmentThreshold?: number | null;
  conditionalEnrollmentThreshold?: number | null;
  suggestedType?: string | null;
  canSubmit: boolean;
  message: string;
  currentSchoolYear?: SchoolYear | null;
  targetSchoolYear?: SchoolYear | null;
  passedSubjects: Array<Record<string, unknown>>;
  transferableSubjects: EnrollmentSubject[];
}

export interface YearEnrollmentRequestHistory {
  id: number;
  oldStatus?: string | null;
  newStatus: string;
  note?: string | null;
  actorUserId?: number | null;
  createdAt: string;
}

export interface YearEnrollmentRequest {
  id: number;
  indeksId: number;
  studentName?: string | null;
  indexLabel?: string | null;
  type: string;
  status: string;
  currentStudyYear: number;
  requestedStudyYear: number;
  earnedEctsSnapshot: number;
  currentSchoolYear: SchoolYear;
  targetSchoolYear: SchoolYear;
  contractReceived: boolean;
  paymentConfirmed: boolean;
  documentationComplete: boolean;
  studentNote?: string | null;
  adminNote?: string | null;
  approvedEnrollmentId?: number | null;
  approvedRenewalId?: number | null;
  submittedAt: string;
  updatedAt: string;
  decidedAt?: string | null;
  transferredSubjects: EnrollmentSubject[];
  history: YearEnrollmentRequestHistory[];
}
