import { FormEvent, useMemo, useState } from 'react';
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
  { header: 'Created', render: (row: Record<string, unknown>) => formatDateTime(row.createdAt) }
];

const requestTypeOptions = [
  { value: 'POTVRDA_O_STUDIRANJU', label: 'Potvrda o studiranju', description: 'Dokument za status aktivnog studenta.' },
  { value: 'UVERENJE_O_POLOZENIM_ISPITIMA', label: 'Spisak polozenih ispita', description: 'PDF uverenje sa polozenim predmetima, ocenama i ESPB.' },
  { value: 'MIROVANJE', label: 'Mirovanje', description: 'Zahtev za promenu statusa u mirovanje.' },
  { value: 'ISPIS', label: 'Ispis', description: 'Zahtev za ispis sa studija.' }
];

const requestColumns = [
  { header: 'Type', render: (row: Record<string, unknown>) => requestTypeLabel(row.type) },
  { header: 'Reason', render: (row: Record<string, unknown>) => pick(row, ['reason']) },
  { header: 'Status', render: (row: Record<string, unknown>) => <span className="statusBadge">{pick(row, ['status'])}</span> },
  { header: 'Submitted', render: (row: Record<string, unknown>) => formatDateTime(row.createdAt) },
  { header: 'Decision note', render: (row: Record<string, unknown>) => pick(row, ['decisionNote']) }
];

const documentColumns = [
  { header: 'Name', render: (row: Record<string, unknown>) => pick(row, ['originalName']) },
  { header: 'Type', render: (row: Record<string, unknown>) => pick(row, ['type']) },
  { header: 'Created', render: (row: Record<string, unknown>) => formatDateTime(row.createdAt) },
  { header: 'Download', render: (row: Record<string, unknown>) => <a className="buttonLink secondaryButton" href={apiUrl(`/api/requests/documents/${String(row.id)}`)}>Download PDF</a> }
];

function requestTypeLabel(value: unknown): string {
  return requestTypeOptions.find((option) => option.value === value)?.label ?? String(value ?? '');
}

function formatDateTime(value: unknown): string {
  if (!value) return '';
  return String(value).replace('T', ' ').slice(0, 16);
}

export function StudentRequestsPage() {
  const dashboard = useApi(meApi.studentDashboard, []);
  const indeksId = String((dashboard.data?.activeIndex as { id?: unknown } | undefined)?.id ?? '');
  const requests = useApi(() => indeksId ? extendedApi.requests(indeksId) : Promise.resolve([]), [indeksId]);
  const documents = useApi(() => indeksId ? extendedApi.documents(indeksId) : Promise.resolve([]), [indeksId]);
  const [type, setType] = useState('POTVRDA_O_STUDIRANJU');
  const [reason, setReason] = useState('');
  const [requestedFrom, setRequestedFrom] = useState('');
  const [requestedTo, setRequestedTo] = useState('');
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const requestRows = useMemo(() => asRows(requests.data), [requests.data]);
  const documentRows = useMemo(() => asRows(documents.data), [documents.data]);
  const selectedType = requestTypeOptions.find((option) => option.value === type) ?? requestTypeOptions[0];

  async function submit(event: FormEvent) {
    event.preventDefault();
    setMessage(null);
    setError(null);
    try {
      await extendedApi.createRequest({
        indeksId: Number(indeksId),
        type,
        reason,
        requestedFrom: requestedFrom || undefined,
        requestedTo: requestedTo || undefined
      });
      setReason('');
      setRequestedFrom('');
      setRequestedTo('');
      setMessage('Zahtev je poslat studentskoj sluzbi.');
      await requests.reload();
    } catch (err) {
      setError(apiErrorMessage(err, 'Slanje zahteva nije uspelo.'));
    }
  }

  if (dashboard.loading || requests.loading || documents.loading) return <Loading />;
  if (dashboard.error) return <ErrorMessage message={dashboard.error} />;

  return (
    <section>
      <header className="pageHeader">
        <div>
          <p className="eyebrow">Studentska sluzba</p>
          <h1>Zahtevi i dokumenti</h1>
          <p className="muted">Posaljite zahtev i preuzmite odobrene potvrde kada ih admin obradi.</p>
        </div>
        <span className="statusBadge">{requestRows.length} zahteva</span>
      </header>
      {!indeksId && <ErrorMessage message="Nemate aktivan indeks povezan sa nalogom." />}
      {message && <p className="success">{message}</p>}
      {error && <ErrorMessage message={error} />}
      <div className="metricGrid">
        <article className="metricCard"><span>Istorija zahteva</span><strong>{requestRows.length}</strong><small>Poslati zahtevi i odluke studentske sluzbe</small></article>
        <article className="metricCard"><span>Dokumenti</span><strong>{documentRows.length}</strong><small>PDF dokumenti spremni za preuzimanje</small></article>
        <article className="metricCard"><span>Izabrani zahtev</span><strong>{selectedType.label}</strong><small>{selectedType.description}</small></article>
      </div>
      <section className="card">
        <header className="pageHeader">
          <div><h2>Novi zahtev</h2><p className="muted">Za potvrdu ili spisak polozenih ispita, PDF se automatski generise posle odobrenja.</p></div>
        </header>
        <form className="formGrid" onSubmit={submit}>
          <label>Tip zahteva<select value={type} onChange={(event) => setType(event.target.value)}>{requestTypeOptions.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}</select></label>
          <label>Razlog<input required value={reason} placeholder="Npr. za stipendiju, konkurs ili licnu evidenciju" onChange={(event) => setReason(event.target.value)} /></label>
          <label>Vazi od<input type="date" value={requestedFrom} onChange={(event) => setRequestedFrom(event.target.value)} /></label>
          <label>Vazi do<input type="date" value={requestedTo} onChange={(event) => setRequestedTo(event.target.value)} /></label>
          <button disabled={!indeksId}>Posalji zahtev</button>
        </form>
      </section>
      {requests.error ? <ErrorMessage message={requests.error} /> : <section className="card"><h2>Istorija zahteva</h2><DataTable rows={requestRows} columns={requestColumns} empty="Jos niste poslali nijedan zahtev." /></section>}
      {documents.error ? <ErrorMessage message={documents.error} /> : <section className="card"><h2>Odobreni dokumenti</h2><DataTable rows={documentRows} columns={documentColumns} empty="Nema odobrenih dokumenata za preuzimanje." /></section>}
    </section>
  );
}

export function NotificationsPage() {
  const result = useApi(extendedApi.notifications, []);
  if (result.loading) return <Loading />;
  if (result.error) return <ErrorMessage message={result.error} />;
  const columns = [
    { header: 'Title', render: (row: Record<string, unknown>) => pick(row, ['title']) },
    { header: 'Message', render: (row: Record<string, unknown>) => pick(row, ['message']) },
    { header: 'Type', render: (row: Record<string, unknown>) => pick(row, ['type']) },
    { header: 'Created', render: (row: Record<string, unknown>) => formatDateTime(row.createdAt) },
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
      } else {
        await extendedApi.rejectEnrollment(String(row.id), note);
        setMessage('Prijava je odbijena.');
      }
      await result.reload();
    } catch (err) {
      setError(apiErrorMessage(err, 'Obrada prijave nije uspela.'));
    }
  }
  if (result.loading) return <Loading />;
  if (result.error) return <ErrorMessage message={result.error} />;
  const columns = [...genericColumns, { header: 'Action', render: (row: Record<string, unknown>) => row.status === 'SUBMITTED' ? <div className="buttonGroup"><button onClick={() => void decide(row, true)}>Approve</button><button className="secondaryButton" onClick={() => void decide(row, false)}>Reject</button></div> : <span className="muted">Processed</span> }];
  return <section className="card"><h1>Enrollment applications</h1>{message && <p className="success">{message}</p>}{error && <ErrorMessage message={error} />}<DataTable rows={asRows(result.data)} columns={columns} /></section>;
}

export function AdminRequestsPage() {
  const result = useApi(extendedApi.adminRequests, []);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  async function decide(row: Record<string, unknown>, approved: boolean) {
    const note = window.prompt(approved ? 'Komentar odluke' : 'Razlog odbijanja') ?? '';
    if (!approved && !note.trim()) return;
    setError(null);
    setMessage(null);
    try {
      await extendedApi.decideRequest(String(row.id), approved, note);
      setMessage(approved ? 'Zahtev je odobren i dokument je generisan kada je primenljivo.' : 'Zahtev je odbijen.');
      await result.reload();
    } catch (err) {
      setError(apiErrorMessage(err, 'Obrada zahteva nije uspela.'));
    }
  }
  if (result.loading) return <Loading />;
  if (result.error) return <ErrorMessage message={result.error} />;
  const rows = asRows(result.data);
  const pending = rows.filter((row) => ['SUBMITTED', 'IN_REVIEW'].includes(String(row.status))).length;
  const columns = [
    { header: 'Student', render: (row: Record<string, unknown>) => <><strong>{pick(row, ['studentName'])}</strong><br /><span className="muted">{pick(row, ['indexLabel'])}</span></> },
    ...requestColumns,
    { header: 'Action', render: (row: Record<string, unknown>) => ['SUBMITTED', 'IN_REVIEW'].includes(String(row.status)) ? <div className="buttonGroup"><button onClick={() => void decide(row, true)}>Approve</button><button className="secondaryButton" onClick={() => void decide(row, false)}>Reject</button></div> : <span className="muted">Processed</span> }
  ];
  return (
    <section>
      <header className="pageHeader">
        <div><p className="eyebrow">Administracija</p><h1>Zahtevi studenata</h1><p className="muted">Odobravanje potvrda, uverenja i statusnih zahteva.</p></div>
        <span className="statusBadge">{pending} na cekanju</span>
      </header>
      {message && <p className="success">{message}</p>}
      {error && <ErrorMessage message={error} />}
      <section className="card"><DataTable rows={rows} columns={columns} empty="Nema studentskih zahteva." /></section>
    </section>
  );
}

export function AdminPaymentsPage() {
  const [indeksId, setIndeksId] = useState('');
  const [amount, setAmount] = useState('');
  const [message, setMessage] = useState<string | null>(null);
  async function submit(event: FormEvent) {
    event.preventDefault();
    try {
      await extendedApi.addLedgerPayment(indeksId, amount, 'Admin payment');
      setMessage('Payment posted to ledger.');
    } catch (error) {
      setMessage(apiErrorMessage(error, 'Payment failed.'));
    }
  }
  return <section className="card"><h1>Ledger payment</h1><form className="formGrid" onSubmit={submit}><label>Index ID<input required value={indeksId} onChange={(event) => setIndeksId(event.target.value)} /></label><label>Amount EUR<input required type="number" min="0.01" step="0.01" value={amount} onChange={(event) => setAmount(event.target.value)} /></label><button>Post payment</button>{message && <p>{message}</p>}</form></section>;
}

export function AdminReportsPage() {
  const reports = [
    { title: 'Aktivni studenti', text: 'Indeks, ime, prezime, program, status, email i finansiranje.', href: '/api/reports/active-students.csv' },
    { title: 'Dugovanja', text: 'Indeks, ime, prezime, dug, pretplata, saldo i kontakt email.', href: '/api/reports/debts.csv' },
    { title: 'Prolaznost ispita', text: 'Predmet, rok, termin, nastavnik, izasli, polozili, procenat i prosek.', href: '/api/reports/pass-rates.csv' }
  ];
  return (
    <section>
      <header className="pageHeader">
        <div><p className="eyebrow">Administracija</p><h1>Reporti</h1><p className="muted">CSV izvozi sa detaljnijim podacima za operativni rad studentske sluzbe.</p></div>
      </header>
      <div className="gridCards">
        {reports.map((report) => (
          <article className="metricCard" key={report.href}>
            <span>CSV report</span>
            <strong>{report.title}</strong>
            <small>{report.text}</small>
            <a className="buttonLink" href={apiUrl(report.href)}>Download CSV</a>
          </article>
        ))}
      </div>
    </section>
  );
}
