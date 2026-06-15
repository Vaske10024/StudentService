import { FormEvent, useState } from 'react';
import { changePassword } from '../api/auth';
import { apiErrorMessage } from '../api/client';
import { useAuth } from '../auth/AuthContext';

export function AccountPage() {
  const { user, refresh } = useAuth();
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [message, setMessage] = useState<string | null>(null);
  const [messageType, setMessageType] = useState<'success' | 'error'>('success');
  if (!user) return null;

  async function submit(event: FormEvent) {
    event.preventDefault();
    setMessage(null);
    try {
      await changePassword(currentPassword, newPassword);
      setCurrentPassword('');
      setNewPassword('');
      await refresh();
      setMessageType('success');
      setMessage('Password changed successfully.');
    } catch (error) {
      setMessageType('error');
      setMessage(apiErrorMessage(error, 'Password change failed.'));
    }
  }

  return <section className="stack">
    <section className="card">
      <header className="pageHeader"><div><p className="eyebrow">Security and access</p><h1>Account</h1><p className="muted">Review your portal identity and manage your password.</p></div><span className="statusBadge">{user.enabled ? 'Enabled' : 'Disabled'}</span></header>
      <dl className="details">
        <dt>Username</dt><dd>{user.username}</dd>
        <dt>Role</dt><dd>{user.role}</dd>
        <dt>Status</dt><dd>{user.enabled ? 'Enabled' : 'Disabled'}</dd>
        <dt>Linked student index</dt><dd>{user.linkedStudentIndeksId ?? '-'}</dd>
        <dt>Linked professor</dt><dd>{user.linkedNastavnikId ?? '-'}</dd>
      </dl>
    </section>
    <section className="card">
      <h2>{user.mustChangePassword ? 'Temporary password must be changed' : 'Change password'}</h2>
      <form className="formGrid" onSubmit={submit}>
        <label>Current password<input type="password" required value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} /></label>
        <label>New password<input type="password" required minLength={8} value={newPassword} onChange={(e) => setNewPassword(e.target.value)} /></label>
        <button type="submit">Change password</button>
        {message && <p className={messageType}>{message}</p>}
      </form>
    </section>
  </section>;
}
