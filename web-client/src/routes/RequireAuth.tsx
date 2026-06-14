import { Navigate, Outlet, useLocation } from 'react-router-dom';
import type { Role } from '../api/types';
import { useAuth } from '../auth/AuthContext';

export function RequireAuth({ roles, permissions }: { roles?: Role[]; permissions?: string[] }) {
  const { user, loading } = useAuth();
  const location = useLocation();

  if (loading) return <main className="page"><p>Loading session...</p></main>;
  if (!user) return <Navigate to="/login" replace state={{ from: location }} />;
  if (user.mustChangePassword && location.pathname !== '/account') return <Navigate to="/account" replace />;
  if (roles?.length && !roles.includes(user.role)) return <Navigate to="/forbidden" replace />;
  if (permissions?.length && !permissions.every((permission) => user.permissions?.includes(permission))) return <Navigate to="/forbidden" replace />;
  return <Outlet />;
}
