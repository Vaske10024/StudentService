import { FormEvent, useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { apiErrorMessage } from '../api/client';
import { leadsApi } from '../api/leads';
import type { Lead } from '../api/types';
import { useAuth } from '../auth/AuthContext';
import { DataTable } from '../components/DataTable';
import { ErrorMessage, Loading } from '../components/Status';
import { useApi } from '../hooks/useApi';

function campaignSource(params: URLSearchParams): string {
  return params.get('source') ?? params.get('utm_campaign') ?? params.get('utm_source') ?? '';
}

function formatDate(value?: string | null): string {
  if (!value) return '';
  return new Date(value).toLocaleString('sr-RS', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
}

export function PublicLeadPage() {
  const [params] = useSearchParams();
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    interestedProgram: '',
    note: '',
    source: campaignSource(params),
    privacyConsent: false
  });
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setForm((current) => ({ ...current, source: campaignSource(params) }));
  }, [params]);

  function update(field: keyof typeof form, value: string | boolean) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setMessage(null);
    try {
      const payload = {
        firstName: form.firstName.trim(),
        lastName: form.lastName.trim(),
        email: form.email.trim(),
        phone: form.phone.trim() || undefined,
        interestedProgram: form.interestedProgram.trim() || undefined,
        source: form.source.trim() || undefined,
        note: form.note.trim() || undefined,
        privacyConsent: form.privacyConsent
      };
      const response = await leadsApi.submit(payload);
      setMessage(response.message);
      setForm({
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        interestedProgram: '',
        note: '',
        source: campaignSource(params),
        privacyConsent: false
      });
    } catch (err) {
      setError(apiErrorMessage(err, 'Slanje nije uspelo.'));
    }
  }

  return (
    <main className="leadPublicPage">
      <section className="leadPublicPanel">
        <div className="leadIntro">
          <p className="eyebrow">Prijava interesovanja</p>
          <h1>Zainteresovan/a sam za studije</h1>
          <p>Ostavite kontakt podatke i studentska sluzba ce vam se javiti sa informacijama o upisu.</p>
        </div>
        <form className="leadForm" onSubmit={submit}>
          <label>Ime *<input required value={form.firstName} onChange={(e) => update('firstName', e.target.value)} autoComplete="given-name" /></label>
          <label>Prezime *<input required value={form.lastName} onChange={(e) => update('lastName', e.target.value)} autoComplete="family-name" /></label>
          <label>Email *<input required type="email" value={form.email} onChange={(e) => update('email', e.target.value)} autoComplete="email" /></label>
          <label>Telefon<input value={form.phone} onChange={(e) => update('phone', e.target.value)} autoComplete="tel" /></label>
          <label>Program koji vas zanima<input value={form.interestedProgram} onChange={(e) => update('interestedProgram', e.target.value)} /></label>
          <label>Poruka<textarea value={form.note} onChange={(e) => update('note', e.target.value)} /></label>
          <label className="checkLabel leadConsent"><input required type="checkbox" checked={form.privacyConsent} onChange={(e) => update('privacyConsent', e.target.checked)} /> Saglasan/a sam da me fakultet kontaktira povodom upisa.</label>
          {error && <p className="error">{error}</p>}
          {message && <p className="success">{message}</p>}
          <button type="submit">Posalji interesovanje</button>
        </form>
      </section>
    </main>
  );
}

export function AdminLeadsPage() {
  const { user } = useAuth();
  const [page, setPage] = useState(0);
  const [downloadError, setDownloadError] = useState<string | null>(null);
  const [downloading, setDownloading] = useState(false);
  const { data, loading, error } = useApi(() => leadsApi.adminList(page, 20), [page]);
  const fullAccess = user?.role === 'HEAD_ADMIN';
  const rows = data?.content ?? [];
  const totalPages = data?.totalPages ?? 1;
  const columns = fullAccess ? [
    { header: 'Ime i prezime', render: (row: Lead) => row.fullName ?? `${row.firstName ?? ''} ${row.lastName ?? ''}`.trim() },
    { header: 'Email', render: (row: Lead) => row.email ?? '' },
    { header: 'Telefon', render: (row: Lead) => row.phone ?? '' },
    { header: 'Program', render: (row: Lead) => row.interestedProgram ?? '' },
    { header: 'Izvor', render: (row: Lead) => row.source ?? '' },
    { header: 'Napomena', render: (row: Lead) => row.note ?? '' },
    { header: 'Kreirano', render: (row: Lead) => formatDate(row.createdAt) }
  ] : [
    { header: 'Inicijali', render: (row: Lead) => row.initials },
    { header: 'Kreirano', render: (row: Lead) => formatDate(row.createdAt) }
  ];

  async function downloadCsv() {
    setDownloadError(null);
    setDownloading(true);
    try {
      await leadsApi.exportCsv();
    } catch (err) {
      setDownloadError(apiErrorMessage(err, 'Export nije uspeo.'));
    } finally {
      setDownloading(false);
    }
  }

  return (
    <section className="card">
      <header className="pageHeader">
        <div>
          <h1>Leadovi</h1>
          <p className="muted">{fullAccess ? 'Prikazujete pune podatke.' : 'Prikaz je maskiran za administratorsku ulogu.'}</p>
        </div>
        <button type="button" onClick={() => void downloadCsv()} disabled={downloading}>{downloading ? 'Preuzimanje...' : 'Download CSV'}</button>
      </header>
      {downloadError && <ErrorMessage message={downloadError} />}
      {loading && <Loading />}
      {error && <ErrorMessage message={error} />}
      {!loading && !error && <DataTable rows={rows} columns={columns} empty="Nema prikupljenih leadova." />}
      <footer className="pager">
        <button disabled={page <= 0} onClick={() => setPage((p) => p - 1)}>Previous</button>
        <span>Page {page + 1} / {Math.max(1, totalPages)}</span>
        <button disabled={page + 1 >= totalPages} onClick={() => setPage((p) => p + 1)}>Next</button>
      </footer>
    </section>
  );
}
