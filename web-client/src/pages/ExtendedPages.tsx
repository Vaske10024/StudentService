import { FormEvent, useState } from 'react';
import { apiErrorMessage, apiUrl } from '../api/client';
import { extendedApi } from '../api/extended';
import { meApi } from '../api/me';
import { DataTable } from '../components/DataTable';
import { ErrorMessage, Loading } from '../components/Status';
import { useApi } from '../hooks/useApi';
import { asRows, pick } from './dataHelpers';

const genericColumns = [
  { header: 'ID', render: (row: Record<string, unknown>) => pick(row, ['id']) },
  { header: 'Type', render: (row: Record<string, unknown>) => pick(row, ['type', 'status']) },
  { header: 'Title / reason', render: (row: Record<string, unknown>) => pick(row, ['title', 'reason', 'message']) },
  { header: 'Created', render: (row: Record<string, unknown>) => pick(row, ['createdAt']) }
];

const requestColumns = [
  { header: 'Type', render: (row: Record<string, unknown>) => pick(row, ['type']) },
  { header: 'Reason', render: (row: Record<string, unknown>) => pick(row, ['reason']) },
  { header: 'Status', render: (row: Record<string, unknown>) => <span className="statusBadge">{pick(row, ['status'])}</span> },
  { header: 'Submitted', render: (row: Record<string, unknown>) => pick(row, ['createdAt']) },
  { header: 'Decision note', render: (row: Record<string, unknown>) => pick(row, ['decisionNote']) }
];

export function StudentRequestsPage() {
  const dashboard = useApi(meApi.studentDashboard, []);
  const indeksId = String((dashboard.data?.activeIndex as { id?: unknown } | undefined)?.id ?? '');
  const requests = useApi(() => indeksId ? extendedApi.requests(indeksId) : Promise.resolve([]), [indeksId]);
  const [type, setType] = useState('POTVRDA_O_STUDIRANJU');
  const [reason, setReason] = useState('');
  const [message, setMessage] = useState<string | null>(null);
  async function submit(event: FormEvent) {
    event.preventDefault();
    try {
      await extendedApi.createRequest({ indeksId: Number(indeksId), type, reason });
      setReason(''); setMessage('Request submitted.'); await requests.reload();
    } catch (error) { setMessage(error instanceof Error ? error.message : 'Request failed.'); }
  }
  if (dashboard.loading || requests.loading) return <Loading />;
  return <section><section className="card"><h1>Requests and documents</h1><p className="muted">Submit certificates, status changes, withdrawals, and personal-data updates to student services.</p><form className="formGrid" onSubmit={submit}>
    <label>Request type<select value={type} onChange={(e) => setType(e.target.value)}><option>POTVRDA_O_STUDIRANJU</option><option>UVERENJE_O_POLOZENIM_ISPITIMA</option><option>MIROVANJE</option><option>ISPIS</option></select></label>
    <label>Reason<input required value={reason} onChange={(e) => setReason(e.target.value)} /></label><button disabled={!indeksId}>Submit</button>{message && <p>{message}</p>}
  </form></section>{requests.error ? <ErrorMessage message={requests.error} /> : <section className="card"><h2>Request history</h2><DataTable rows={asRows(requests.data)} columns={requestColumns} empty="You have not submitted any requests." /></section>}</section>;
}

export function NotificationsPage() {
  const result = useApi(extendedApi.notifications, []);
  if (result.loading) return <Loading />; if (result.error) return <ErrorMessage message={result.error} />;
  const columns = [
    { header: 'Title', render: (row: Record<string, unknown>) => pick(row, ['title']) },
    { header: 'Message', render: (row: Record<string, unknown>) => pick(row, ['message']) },
    { header: 'Type', render: (row: Record<string, unknown>) => pick(row, ['type']) },
    { header: 'Created', render: (row: Record<string, unknown>) => pick(row, ['createdAt']) },
    { header: 'Status', render: (row: Record<string, unknown>) => row.readFlag ? <span className="muted">Read</span> : <button type="button" onClick={async () => { await extendedApi.markNotificationRead(String(row.id)); await result.reload(); }}>Mark as read</button> }
  ];
  const unread = asRows(result.data).filter((item) => !item.readFlag).length;
  return <section className="card"><header className="pageHeader"><div><h1>Notifications</h1><p className="muted">Academic, exam, finance, and request updates.</p></div><span className="statusBadge">{unread} unread</span></header><DataTable rows={asRows(result.data)} columns={columns} empty="You have no notifications." /></section>;
}

export function AdminEnrollmentsPage() {
  const result = useApi(extendedApi.enrollments, []);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  async function decide(row: Record<string, unknown>, approved: boolean) {
    const note = window.prompt(approved ? 'Napomena odluke (opciono)' : 'Razlog odbijanja') ?? '';
    if (!approved && !note.trim()) return;
    try {
      if (approved) {
        const password = crypto.randomUUID().replace(/-/g, '').slice(0, 18);
        await extendedApi.approveEnrollment(String(row.id), password);
        setMessage(`Prijava je odobrena. Privremena lozinka (prikazuje se samo sada): ${password}`);
      } else { await extendedApi.rejectEnrollment(String(row.id), note); setMessage('Prijava je odbijena.'); }
      await result.reload();
    } catch (err) { setError(apiErrorMessage(err, 'Obrada prijave nije uspela.')); }
  }
  if (result.loading) return <Loading />; if (result.error) return <ErrorMessage message={result.error} />;
  const columns = [...genericColumns, { header: 'Action', render: (row: Record<string, unknown>) => row.status === 'SUBMITTED' ? <div className="buttonGroup"><button onClick={() => void decide(row, true)}>Approve</button><button className="secondaryButton" onClick={() => void decide(row, false)}>Reject</button></div> : <span className="muted">Processed</span> }];
  return <section className="card"><h1>Enrollment applications</h1>{message && <p className="success">{message}</p>}{error && <ErrorMessage message={error} />}<DataTable rows={asRows(result.data)} columns={columns} /></section>;
}

export function AdminRequestsPage() {
  const result = useApi(extendedApi.adminRequests, []);
  const [message, setMessage] = useState<string | null>(null);
  async function decide(row: Record<string, unknown>, approved: boolean) {
    const note = window.prompt(approved ? 'Komentar odluke' : 'Razlog odbijanja') ?? '';
    if (!approved && !note.trim()) return;
    try { await extendedApi.decideRequest(String(row.id), approved, note); setMessage('Zahtev je obrađen.'); await result.reload(); }
    catch (err) { setMessage(apiErrorMessage(err, 'Obrada zahteva nije uspela.')); }
  }
  if (result.loading) return <Loading />; if (result.error) return <ErrorMessage message={result.error} />;
  const columns = [...requestColumns, { header: 'Action', render: (row: Record<string, unknown>) => ['SUBMITTED', 'IN_REVIEW'].includes(String(row.status)) ? <div className="buttonGroup"><button onClick={() => void decide(row, true)}>Approve</button><button className="secondaryButton" onClick={() => void decide(row, false)}>Reject</button></div> : <span className="muted">Processed</span> }];
  return <section className="card"><h1>Student requests</h1>{message && <p className="success">{message}</p>}<DataTable rows={asRows(result.data)} columns={columns} /></section>;
}

export function AdminPaymentsPage() {
  const [indeksId, setIndeksId] = useState(''); const [amount, setAmount] = useState(''); const [message, setMessage] = useState<string | null>(null);
  async function submit(event: FormEvent) { event.preventDefault(); try { await extendedApi.addLedgerPayment(indeksId, amount, 'Admin payment'); setMessage('Payment posted to ledger.'); } catch (e) { setMessage(e instanceof Error ? e.message : 'Payment failed.'); } }
  return <section className="card"><h1>Ledger payment</h1><form className="formGrid" onSubmit={submit}><label>Index ID<input required value={indeksId} onChange={(e) => setIndeksId(e.target.value)} /></label><label>Amount EUR<input required type="number" min="0.01" step="0.01" value={amount} onChange={(e) => setAmount(e.target.value)} /></label><button>Post payment</button>{message && <p>{message}</p>}</form></section>;
}

export function AdminReportsPage() {
  return <section className="card"><h1>Reports</h1><p>Exports require the REPORT_EXPORT permission.</p><div className="toolbar"><a className="buttonLink" href={apiUrl('/api/reports/active-students.csv')}>Active students CSV</a><a className="buttonLink" href={apiUrl('/api/reports/debts.csv')}>Debts CSV</a><a className="buttonLink" href={apiUrl('/api/reports/pass-rates.csv')}>Pass rates CSV</a></div></section>;
}
