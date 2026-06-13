import { Navigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export function ProfilePage() {
  const { user } = useAuth();
  if (!user) return null;
  if (user.role === 'STUDENT') return <Navigate to="/student/profile" replace />;
  return <Navigate to="/account" replace />;
}
