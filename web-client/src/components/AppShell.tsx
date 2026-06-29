import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

const pageTitles: Record<string, string> = {
  dashboard: 'Dashboard',
  profile: 'Profile',
  account: 'Account settings',
  subjects: 'Subjects',
  exams: 'Exams',
  payments: 'Payments',
  grades: 'Grades',
  requests: 'Requests',
  notifications: 'Notifications',
  enrollments: 'Enrollments',
  reports: 'Reports',
  leads: 'Leadovi',
  monitoring: 'Lead monitoring',
  students: 'Students',
  professors: 'Professors',
  programs: 'Study programs',
  'school-years': 'School years',
  'exam-periods': 'Exam periods',
  'year-enrollments': 'Year enrollments',
  predispit: 'Pre-exam activities'
};

function NavGroup({ label, children }: { label: string; children: React.ReactNode }) {
  return <div className="navGroup"><p className="navLabel">{label}</p>{children}</div>;
}

export function AppShell() {
  const { user, logout, hasPermission } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const pathSegments = location.pathname.split('/').filter(Boolean);
  const currentSegment = pathSegments[pathSegments.length - 1] ?? 'dashboard';
  const pageTitle = pageTitles[currentSegment] ?? 'Student Service';
  const isAdmin = user?.role === 'ADMIN' || user?.role === 'HEAD_ADMIN';
  const roleLabel = user?.role === 'HEAD_ADMIN' ? 'Head administrator' : user?.role === 'ADMIN' ? 'Administrator' : user?.role === 'PROFESSOR' ? 'Professor' : 'Student';
  const initials = (user?.username ?? 'U').slice(0, 2).toUpperCase();

  async function onLogout() {
    await logout();
    navigate('/login');
  }

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brandMark" aria-hidden="true">SS</span>
          <div><strong>Student Service</strong><small>Academic portal</small></div>
        </div>
        <div className="sidebarUser">
          <span className="userAvatar" aria-hidden="true">{initials}</span>
          <div><strong>{user?.username}</strong><span>{roleLabel}</span></div>
        </div>
        <nav aria-label="Main navigation">
          <NavGroup label="Overview">
            <NavLink to="/dashboard">Dashboard</NavLink>
            <NavLink to="/profile">Profile</NavLink>
          </NavGroup>
          {user?.role === 'STUDENT' && <NavGroup label="Student services">
            <NavLink to="/student/subjects">Subjects</NavLink>
            <NavLink to="/student/exams">Exams</NavLink>
            <NavLink to="/student/grades">Grades</NavLink>
            <NavLink to="/student/payments">Payments</NavLink>
            <NavLink to="/student/year-enrollment">Year enrollment</NavLink>
            <NavLink to="/student/requests">Requests</NavLink>
            <NavLink to="/student/notifications">Notifications</NavLink>
          </NavGroup>}
          {user?.role === 'PROFESSOR' && <NavGroup label="Professor">
            <NavLink to="/professor/subjects">My subjects</NavLink>
            <NavLink to="/professor/exams">Exams and results</NavLink>
            <NavLink to="/professor/predispit">Pre-exam activities</NavLink>
          </NavGroup>}
          {isAdmin && <NavGroup label="Administration">
            <NavLink to="/admin/students">Students</NavLink>
            <NavLink to="/admin/leads">Leadovi</NavLink>
            {user?.role === 'HEAD_ADMIN' && <NavLink to="/admin/leads/monitoring">Lead monitoring</NavLink>}
            <NavLink to="/admin/professors">Professors</NavLink>
            <NavLink to="/admin/subjects">Subjects</NavLink>
            <NavLink to="/admin/programs">Study programs</NavLink>
            <NavLink to="/admin/school-years">School years</NavLink>
            <NavLink to="/admin/exam-periods">Exam periods</NavLink>
            <NavLink to="/admin/exams">Exams</NavLink>
            {hasPermission('ENROLLMENT_WRITE') && <NavLink to="/admin/enrollments">Enrollments</NavLink>}
            {hasPermission('ENROLLMENT_WRITE') && <NavLink to="/admin/year-enrollments">Year enrollment requests</NavLink>}
            {hasPermission('DOCUMENT_DECIDE') && <NavLink to="/admin/requests">Student requests</NavLink>}
            {hasPermission('FINANCE_WRITE') && <NavLink to="/admin/payments">Ledger payments</NavLink>}
            {hasPermission('REPORT_EXPORT') && <NavLink to="/admin/reports">Reports</NavLink>}
          </NavGroup>}
          <NavGroup label="Account">
            <NavLink to="/account">Account settings</NavLink>
          </NavGroup>
        </nav>
        <button type="button" className="logoutButton" onClick={onLogout}>Sign out</button>
      </aside>
      <div className="mainArea">
        <header className="topbar">
          <div><p className="topbarEyebrow">{roleLabel} portal</p><strong>{pageTitle}</strong></div>
          <span className="topbarUser">{user?.username}</span>
        </header>
        <main className="content">
          {user?.mustChangePassword && <p className="error">Koristite privremenu lozinku. Promenite je na stranici Account pre nastavka rada.</p>}
          <Outlet />
        </main>
      </div>
    </div>
  );
}
