import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext';
import { AppShell } from './components/AppShell';
import { RequireAuth } from './routes/RequireAuth';
import { AccountPage } from './pages/AccountPage';
import { AdminDashboardPage, AdminExamPeriodsPage, AdminExamsPage, AdminStudentDetailPage, AdminStudentIndexesPage, AdminStudentNewPage, AdminStudentsPage } from './pages/AdminPages';
import { DashboardPage } from './pages/DashboardPage';
import { ProfilePage } from './pages/ProfilePage';
import { LoginPage } from './pages/LoginPage';
import { ProfessorDashboardPage, ProfessorExamRegisteredStudentsPage, ProfessorExamResultsPage, ProfessorExamsPage, ProfessorPredispitPage, ProfessorSubjectStudentsPage, ProfessorSubjectsPage } from './pages/ProfessorPages';
import { StudentDashboardPage, StudentExamsPage, StudentGradesPage, StudentPaymentsPage, StudentProfilePage, StudentSubjectsPage } from './pages/StudentPages';
import { AdminEnrollmentsPage, AdminPaymentsPage, AdminReportsPage, AdminRequestsPage, NotificationsPage, StudentRequestsPage } from './pages/ExtendedPages';
import { AdminProfessorsPage, AdminProgramsPage, AdminSchoolYearsPage, AdminSubjectsPage } from './pages/AdminCatalogPages';
import { AdminYearEnrollmentsPage, StudentYearEnrollmentPage } from './pages/YearEnrollmentPages';
import { AdminLeadsPage, HeadAdminLeadMonitoringPage, PublicLeadPage } from './pages/LeadPages';
import type { Role } from './api/types';

const adminRoles: Role[] = ['ADMIN', 'HEAD_ADMIN'];

export function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/interesovanje" element={<PublicLeadPage />} />
          <Route path="/leads" element={<PublicLeadPage />} />
          <Route element={<RequireAuth />}>
            <Route element={<AppShell />}>
              <Route path="/" element={<Navigate to="/dashboard" replace />} />
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route path="/profile" element={<ProfilePage />} />
              <Route path="/account" element={<AccountPage />} />
              <Route path="/forbidden" element={<section className="card"><h1>Pristup nije dozvoljen</h1><p>Ulogovani ste, ali nemate potrebnu dozvolu za ovu akciju.</p></section>} />
              <Route path="/settings" element={<Navigate to="/account" replace />} />

              <Route element={<RequireAuth roles={['STUDENT']} />}>
                <Route path="/student/dashboard" element={<StudentDashboardPage />} />
                <Route path="/student/profile" element={<StudentProfilePage />} />
                <Route path="/student/subjects" element={<StudentSubjectsPage />} />
                <Route path="/student/exams" element={<StudentExamsPage />} />
                <Route path="/student/payments" element={<StudentPaymentsPage />} />
                <Route path="/student/grades" element={<StudentGradesPage />} />
                <Route path="/student/requests" element={<StudentRequestsPage />} />
                <Route path="/student/year-enrollment" element={<StudentYearEnrollmentPage />} />
                <Route path="/student/notifications" element={<NotificationsPage />} />
              </Route>

              <Route element={<RequireAuth roles={['PROFESSOR']} />}>
                <Route path="/professor/dashboard" element={<ProfessorDashboardPage />} />
                <Route path="/professor/subjects" element={<ProfessorSubjectsPage />} />
                <Route path="/professor/subjects/:id/students" element={<ProfessorSubjectStudentsPage />} />
                <Route path="/professor/exams" element={<ProfessorExamsPage />} />
                <Route path="/professor/exams/:id/registered" element={<ProfessorExamRegisteredStudentsPage />} />
                <Route path="/professor/exams/:id/results" element={<ProfessorExamResultsPage />} />
                <Route path="/professor/predispit" element={<ProfessorPredispitPage />} />
              </Route>

              <Route element={<RequireAuth roles={adminRoles} />}>
                <Route path="/admin/dashboard" element={<AdminDashboardPage />} />
                <Route path="/admin/students" element={<AdminStudentsPage />} />
                <Route path="/admin/leads" element={<AdminLeadsPage />} />
                <Route path="/admin/students/new" element={<AdminStudentNewPage />} />
                <Route path="/admin/students/:id" element={<AdminStudentDetailPage />} />
                <Route path="/admin/students/:id/indexes" element={<AdminStudentIndexesPage />} />
                <Route path="/admin/professors" element={<AdminProfessorsPage />} />
                <Route path="/admin/subjects" element={<AdminSubjectsPage />} />
                <Route path="/admin/programs" element={<AdminProgramsPage />} />
                <Route path="/admin/school-years" element={<AdminSchoolYearsPage />} />
                <Route path="/admin/exam-periods" element={<AdminExamPeriodsPage />} />
                <Route path="/admin/exams" element={<AdminExamsPage />} />
                <Route path="/admin/exams/:id/results" element={<ProfessorExamResultsPage />} />
              </Route>
              <Route element={<RequireAuth roles={['HEAD_ADMIN']} />}>
                <Route path="/admin/leads/monitoring" element={<HeadAdminLeadMonitoringPage />} />
              </Route>
              <Route element={<RequireAuth roles={adminRoles} permissions={['ENROLLMENT_WRITE']} />}><Route path="/admin/enrollments" element={<AdminEnrollmentsPage />} /></Route>
              <Route element={<RequireAuth roles={adminRoles} permissions={['ENROLLMENT_WRITE']} />}><Route path="/admin/year-enrollments" element={<AdminYearEnrollmentsPage />} /></Route>
              <Route element={<RequireAuth roles={adminRoles} permissions={['DOCUMENT_DECIDE']} />}><Route path="/admin/requests" element={<AdminRequestsPage />} /></Route>
              <Route element={<RequireAuth roles={adminRoles} permissions={['FINANCE_WRITE']} />}><Route path="/admin/payments" element={<AdminPaymentsPage />} /></Route>
              <Route element={<RequireAuth roles={adminRoles} permissions={['REPORT_EXPORT']} />}><Route path="/admin/reports" element={<AdminReportsPage />} /></Route>

              <Route path="*" element={<Navigate to="/dashboard" replace />} />
            </Route>
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
