import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export function AppShell() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  async function onLogout() {
    await logout();
    navigate('/login');
  }

  return (
    <div className="shell">
      <aside className="sidebar">
        <h1>Student Service</h1>
        <p className="muted">{user?.username} · {user?.role}</p>
        <nav>
          <NavLink to="/dashboard">Dashboard</NavLink>
          <NavLink to="/profile">Profile</NavLink>
          {user?.role === 'STUDENT' && <>
            <NavLink to="/student/subjects">Subjects</NavLink>
            <NavLink to="/student/exams">Exams</NavLink>
            <NavLink to="/student/payments">Payments</NavLink>
            <NavLink to="/student/grades">Grades</NavLink>
            <NavLink to="/student/requests">Requests</NavLink>
            <NavLink to="/student/notifications">Notifications</NavLink>
          </>}
          {user?.role === 'PROFESSOR' && <>
            <NavLink to="/professor/subjects">My subjects</NavLink>
            <NavLink to="/professor/exams">Exams</NavLink>
            <NavLink to="/professor/predispit">Predispit</NavLink>
          </>}
          {user?.role === 'ADMIN' && <>
            <NavLink to="/admin/students">Students</NavLink>
            <NavLink to="/admin/professors">Professors</NavLink>
            <NavLink to="/admin/subjects">Subjects</NavLink>
            <NavLink to="/admin/programs">Programs</NavLink>
            <NavLink to="/admin/school-years">School years</NavLink>
            <NavLink to="/admin/reports">Reports</NavLink>
            <NavLink to="/admin/enrollments">Enrollments</NavLink>
            <NavLink to="/admin/payments">Ledger payments</NavLink>
          </>}
          <NavLink to="/account">Account</NavLink>
        </nav>
        <button type="button" onClick={onLogout}>Logout</button>
      </aside>
      <main className="content"><Outlet /></main>
    </div>
  );
}
