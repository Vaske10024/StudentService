import { useAuth } from '../auth/AuthContext';

export function AccountPage() {
  const { user } = useAuth();
  if (!user) return null;
  return (
    <section className="card">
      <h1>Account</h1>
      <dl className="details">
        <dt>Username</dt><dd>{user.username}</dd>
        <dt>Role</dt><dd>{user.role}</dd>
        <dt>Status</dt><dd>{user.enabled ? 'Enabled' : 'Disabled'}</dd>
        <dt>Linked student index</dt><dd>{user.linkedStudentIndeksId ?? '—'}</dd>
        <dt>Linked professor</dt><dd>{user.linkedNastavnikId ?? '—'}</dd>
      </dl>
    </section>
  );
}
