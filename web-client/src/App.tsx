import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { adminApi } from './api/admin';
import { AuthProvider } from './auth/AuthContext';
import { AppShell } from './components/AppShell';
import { RequireAuth } from './routes/RequireAuth';
import { AccountPage } from './pages/AccountPage';
import { AdminDashboardPage, AdminEntityPage, AdminExamsPage, AdminStudentDetailPage, AdminStudentIndexesPage, AdminStudentNewPage, AdminStudentsPage } from './pages/AdminPages';
import { DashboardPage } from './pages/DashboardPage';
import { ProfilePage } from './pages/ProfilePage';
import { LoginPage } from './pages/LoginPage';
import { ProfessorDashboardPage, ProfessorExamResultsPage, ProfessorExamsPage, ProfessorPredispitPage, ProfessorSubjectStudentsPage, ProfessorSubjectsPage } from './pages/ProfessorPages';
import { StudentDashboardPage, StudentExamsPage, StudentGradesPage, StudentPaymentsPage, StudentProfilePage, StudentSubjectsPage } from './pages/StudentPages';
import { AdminEnrollmentsPage, AdminPaymentsPage, AdminReportsPage, NotificationsPage, StudentRequestsPage } from './pages/ExtendedPages';
import { AdminProfessorsPage, AdminSubjectsPage } from './pages/AdminCatalogPages';

export function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route element={<RequireAuth />}>
            <Route element={<AppShell />}>
              <Route path="/" element={<Navigate to="/dashboard" replace />} />
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route path="/profile" element={<ProfilePage />} />
              <Route path="/account" element={<AccountPage />} />
              <Route path="/settings" element={<Navigate to="/account" replace />} />

              <Route element={<RequireAuth roles={['STUDENT']} />}>
                <Route path="/student/dashboard" element={<StudentDashboardPage />} />
                <Route path="/student/profile" element={<StudentProfilePage />} />
                <Route path="/student/subjects" element={<StudentSubjectsPage />} />
                <Route path="/student/exams" element={<StudentExamsPage />} />
                <Route path="/student/payments" element={<StudentPaymentsPage />} />
                <Route path="/student/grades" element={<StudentGradesPage />} />
                <Route path="/student/requests" element={<StudentRequestsPage />} />
                <Route path="/student/notifications" element={<NotificationsPage />} />
              </Route>

              <Route element={<RequireAuth roles={['PROFESSOR']} />}>
                <Route path="/professor/dashboard" element={<ProfessorDashboardPage />} />
                <Route path="/professor/subjects" element={<ProfessorSubjectsPage />} />
                <Route path="/professor/subjects/:id/students" element={<ProfessorSubjectStudentsPage />} />
                <Route path="/professor/exams" element={<ProfessorExamsPage />} />
                <Route path="/professor/exams/:id/results" element={<ProfessorExamResultsPage />} />
                <Route path="/professor/predispit" element={<ProfessorPredispitPage />} />
              </Route>

              <Route element={<RequireAuth roles={['ADMIN']} />}>
                <Route path="/admin/dashboard" element={<AdminDashboardPage />} />
                <Route path="/admin/students" element={<AdminStudentsPage />} />
                <Route path="/admin/students/new" element={<AdminStudentNewPage />} />
                <Route path="/admin/students/:id" element={<AdminStudentDetailPage />} />
                <Route path="/admin/students/:id/indexes" element={<AdminStudentIndexesPage />} />
                <Route path="/admin/professors" element={<AdminProfessorsPage />} />
                <Route path="/admin/subjects" element={<AdminSubjectsPage />} />
                <Route path="/admin/programs" element={<AdminEntityPage title="Study programs" loader={adminApi.programs} />} />
                <Route path="/admin/school-years" element={<AdminEntityPage title="School years" loader={adminApi.schoolYears} />} />
                <Route path="/admin/exam-periods" element={<AdminEntityPage title="Exam periods" loader={adminApi.examPeriods} />} />
                <Route path="/admin/exams" element={<AdminExamsPage />} />
                <Route path="/admin/enrollments" element={<AdminEnrollmentsPage />} />
                <Route path="/admin/payments" element={<AdminPaymentsPage />} />
                <Route path="/admin/reports" element={<AdminReportsPage />} />
              </Route>

              <Route path="*" element={<Navigate to="/dashboard" replace />} />
            </Route>
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
