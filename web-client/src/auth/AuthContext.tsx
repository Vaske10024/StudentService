import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import type { AuthUser, Role } from '../api/types';
import * as authApi from '../api/auth';

interface AuthContextValue {
  user: AuthUser | null;
  loading: boolean;
  error: string | null;
  login: (username: string, password: string) => Promise<AuthUser>;
  logout: () => Promise<void>;
  hasRole: (...roles: Role[]) => boolean;
  hasPermission: (...permissions: string[]) => boolean;
  refresh: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const me = await authApi.me();
      setUser(me);
    } catch {
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void refresh();
    const listener = () => setUser(null);
    const mustChangeListener = () => { void refresh(); };
    window.addEventListener('auth:unauthorized', listener);
    window.addEventListener('auth:must-change-password', mustChangeListener);
    return () => {
      window.removeEventListener('auth:unauthorized', listener);
      window.removeEventListener('auth:must-change-password', mustChangeListener);
    };
  }, [refresh]);

  const login = useCallback(async (username: string, password: string) => {
    setError(null);
    const loggedIn = await authApi.login(username, password);
    setUser(loggedIn);
    return loggedIn;
  }, []);

  const logout = useCallback(async () => {
    await authApi.logout();
    setUser(null);
  }, []);

  const hasRole = useCallback((...roles: Role[]) => Boolean(user && roles.includes(user.role)), [user]);
  const hasPermission = useCallback((...permissions: string[]) => Boolean(user && permissions.every((permission) => user.permissions?.includes(permission))), [user]);

  const value = useMemo<AuthContextValue>(() => ({ user, loading, error, login, logout, hasRole, hasPermission, refresh }), [user, loading, error, login, logout, hasRole, hasPermission, refresh]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
