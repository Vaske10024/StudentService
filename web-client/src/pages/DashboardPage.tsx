import { Navigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export function DashboardPage() {
  const { user } = useAuth();
  if (!user) return null;
  if (user.role === 'STUDENT') return <Navigate to="/student/dashboard" replace />;
  if (user.role === 'PROFESSOR') return <Navigate to="/professor/dashboard" replace />;
  return <Navigate to="/admin/dashboard" replace />;
}
