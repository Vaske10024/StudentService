import { FormEvent, useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { apiErrorMessage } from '../api/client';
import { leadsApi } from '../api/leads';
import type { Lead, LeadAuditLog, LeadEmailMessage, LeadEmailMonitoring, LeadEmailTemplate, LeadExportLog } from '../api/types';
import { useAuth } from '../auth/AuthContext';
import { DataTable } from '../components/DataTable';
import { Modal } from '../components/Modal';
import { ErrorMessage, Loading } from '../components/Status';
import { useApi } from '../hooks/useApi';

const leadStatuses = ['NEW', 'CONTACTED', 'INTERESTED', 'NOT_INTERESTED', 'ENROLLED', 'INVALID'];

function campaignSource(params: URLSearchParams): string {
  return params.get('source') ?? params.get('utm_campaign') ?? params.get('utm_source') ?? '';
}

function formatDate(value?: string | null): string {
  if (!value) return '';
  return new Date(value).toLocaleString('sr-RS', {
    year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'
  });
}

function leadName(lead: Lead): string {
  return lead.fullName ?? lead.maskedName ?? `${lead.firstName ?? ''} ${lead.lastName ?? ''}`.trim();
}

export function PublicLeadPage() {
  const [params] = useSearchParams();
  const [form, setForm] = useState({
    firstName: '', lastName: '', email: '', phone: '', interestedProgram: '',
    note: '', source: campaignSource(params), privacyConsent: false
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
      const response = await leadsApi.submit({
        firstName: form.firstName.trim(),
        lastName: form.lastName.trim(),
        email: form.email.trim(),
        phone: form.phone.trim() || undefined,
        interestedProgram: form.interestedProgram.trim() || undefined,
        source: form.source.trim() || undefined,
        note: form.note.trim() || undefined,
        privacyConsent: form.privacyConsent
      });
      setMessage(response.message);
      setForm({
        firstName: '', lastName: '', email: '', phone: '', interestedProgram: '',
        note: '', source: campaignSource(params), privacyConsent: false
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

function LeadDetailDialog({ leadId, onClose, onChanged }: { leadId: number; onClose: () => void; onChanged: () => void }) {
  const [lead, setLead] = useState<Lead | null>(null);
  const [history, setHistory] = useState<LeadEmailMessage[]>([]);
  const [templates, setTemplates] = useState<LeadEmailTemplate[]>([]);
  const [templateId, setTemplateId] = useState('');
  const [subject, setSubject] = useState('');
  const [body, setBody] = useState('');
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [savingStatus, setSavingStatus] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const [leadData, historyData, templateData] = await Promise.all([
        leadsApi.detail(leadId), leadsApi.history(leadId), leadsApi.templates()
      ]);
      setLead(leadData);
      setHistory(historyData);
      setTemplates(templateData.filter((item) => item.active));
    } catch (err) {
      setError(apiErrorMessage(err, 'Lead nije mogao da se ucita.'));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { void load(); }, [leadId]);

  function selectTemplate(value: string) {
    setTemplateId(value);
    const template = templates.find((item) => String(item.id) === value);
    if (template) {
      setSubject(template.subject);
      setBody(template.body);
    }
  }

  async function send(event: FormEvent) {
    event.preventDefault();
    setSending(true);
    setError(null);
    setMessage(null);
    try {
      const sent = await leadsApi.sendEmail(leadId, {
        templateId: templateId ? Number(templateId) : undefined,
        subject,
        body
      });
      setMessage(sent.status === 'SENT' ? 'Email je poslat.' : `Slanje nije uspelo: ${sent.errorMessage ?? 'nepoznata greska'}`);
      setSubject('');
      setBody('');
      setTemplateId('');
      await load();
      onChanged();
    } catch (err) {
      setError(apiErrorMessage(err, 'Email nije mogao da se posalje.'));
    } finally {
      setSending(false);
    }
  }

  async function updateStatus(status: string) {
    setSavingStatus(true);
    setError(null);
    try {
      const updated = await leadsApi.updateStatus(leadId, status);
      setLead(updated);
      onChanged();
    } catch (err) {
      setError(apiErrorMessage(err, 'Status nije mogao da se promeni.'));
    } finally {
      setSavingStatus(false);
    }
  }

  return (
    <Modal title="Lead CRM" onClose={onClose}>
      {loading && <Loading />}
      {error && <ErrorMessage message={error} />}
      {!loading && lead && <div className="stack">
        <section className="card">
          <header className="pageHeader">
            <div><p className="eyebrow">Kontakt</p><h2>{leadName(lead)}</h2><p className="muted">{lead.email ?? lead.maskedEmail ?? ''}</p></div>
            <label>Status
              <select disabled={savingStatus} value={lead.status} onChange={(e) => void updateStatus(e.target.value)}>
                {leadStatuses.map((status) => <option key={status}>{status}</option>)}
              </select>
            </label>
          </header>
          {lead.fullAccess && <dl className="details">
            <div className="detailItem"><dt>Telefon</dt><dd>{lead.phone ?? ''}</dd></div>
            <div className="detailItem"><dt>Program</dt><dd>{lead.interestedProgram ?? ''}</dd></div>
            <div className="detailItem"><dt>Izvor</dt><dd>{lead.source ?? ''}</dd></div>
            <div className="detailItem"><dt>Napomena</dt><dd>{lead.note ?? ''}</dd></div>
          </dl>}
        </section>

        <section className="card">
          <h2>Posalji email</h2>
          <p className="muted">Primalac se odredjuje na serveru iz lead ID-a. Adresa se ne salje iz browsera.</p>
          {message && <p className={message.startsWith('Email je') ? 'success' : 'error'}>{message}</p>}
          <form className="stack" onSubmit={send}>
            <label>Template
              <select value={templateId} onChange={(e) => selectTemplate(e.target.value)}>
                <option value="">Bez template-a</option>
                {templates.map((template) => <option key={template.id} value={template.id}>{template.name}</option>)}
              </select>
            </label>
            <label>Subject<input required maxLength={255} value={subject} onChange={(e) => setSubject(e.target.value)} /></label>
            <label>Message<textarea required className="leadMessageBody" value={body} onChange={(e) => setBody(e.target.value)} /></label>
            <button disabled={sending}>{sending ? 'Slanje...' : 'Posalji email'}</button>
          </form>
        </section>

        <section className="card">
          <h2>Communication history</h2>
          {!history.length && <p className="muted">Nema prethodnih poruka.</p>}
          <div className="messageHistory">
            {history.map((item) => <article key={item.id} className="messageCard">
              <header><strong>{item.subject}</strong><span className="statusBadge">{item.status}</span></header>
              <p className="messageMeta">{item.sentByUsername} · {formatDate(item.sentAt ?? item.createdAt)}{item.templateName ? ` · ${item.templateName}` : ''}</p>
              <pre>{item.body}</pre>
              {item.errorMessage && <p className="error">{item.errorMessage}</p>}
            </article>)}
          </div>
        </section>
      </div>}
    </Modal>
  );
}

export function AdminLeadsPage() {
  const { user } = useAuth();
  const [page, setPage] = useState(0);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [downloadError, setDownloadError] = useState<string | null>(null);
  const [downloading, setDownloading] = useState(false);
  const { data, loading, error, reload } = useApi(() => leadsApi.adminList(page, 20), [page]);
  const fullAccess = user?.role === 'HEAD_ADMIN';
  const rows = data?.content ?? [];
  const totalPages = data?.totalPages ?? 1;
  const columns = [
    { header: 'Ime i prezime', render: (row: Lead) => leadName(row) },
    { header: 'Email', render: (row: Lead) => row.email ?? row.maskedEmail ?? '' },
    ...(fullAccess ? [
      { header: 'Telefon', render: (row: Lead) => row.phone ?? '' },
      { header: 'Program', render: (row: Lead) => row.interestedProgram ?? '' }
    ] : []),
    { header: 'Status', render: (row: Lead) => <span className="statusBadge">{row.status}</span> },
    { header: 'Kreirano', render: (row: Lead) => formatDate(row.createdAt) },
    { header: 'CRM', render: (row: Lead) => <button type="button" onClick={() => setSelectedId(row.id)}>Open</button> }
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
        <div><h1>Leadovi</h1><p className="muted">{fullAccess ? 'Prikazujete pune podatke.' : 'Prikaz je maskiran za administratorsku ulogu.'}</p></div>
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
      {selectedId !== null && <LeadDetailDialog leadId={selectedId} onClose={() => setSelectedId(null)} onChanged={() => void reload()} />}
    </section>
  );
}

export function HeadAdminLeadMonitoringPage() {
  const emails = useApi(() => leadsApi.monitoringEmails(0, 100), []);
  const audits = useApi(() => leadsApi.monitoringAudit(0, 100), []);
  const exports = useApi(() => leadsApi.monitoringExports(0, 100), []);
  const templates = useApi(leadsApi.templates, []);
  const [editing, setEditing] = useState<LeadEmailTemplate | null>(null);
  const [templateEditorOpen, setTemplateEditorOpen] = useState(false);
  const [form, setForm] = useState({ name: '', subject: '', body: '', active: true });
  const [error, setError] = useState<string | null>(null);

  function edit(template?: LeadEmailTemplate) {
    setEditing(template ?? null);
    setTemplateEditorOpen(true);
    setForm(template
      ? { name: template.name, subject: template.subject, body: template.body, active: template.active }
      : { name: '', subject: '', body: '', active: true });
  }

  async function saveTemplate(event: FormEvent) {
    event.preventDefault();
    setError(null);
    try {
      if (editing) await leadsApi.updateTemplate(editing.id, form);
      else await leadsApi.createTemplate(form);
      setEditing(null);
      setTemplateEditorOpen(false);
      setForm({ name: '', subject: '', body: '', active: true });
      await templates.reload();
    } catch (err) {
      setError(apiErrorMessage(err, 'Template nije sacuvan.'));
    }
  }

  const emailColumns = [
    { header: 'Lead', render: (row: LeadEmailMonitoring) => `${leadName(row.lead)} (${row.lead.email ?? ''})` },
    { header: 'Admin', render: (row: LeadEmailMonitoring) => row.message.sentByUsername },
    { header: 'Status', render: (row: LeadEmailMonitoring) => <span className="statusBadge">{row.message.status}</span> },
    { header: 'Sent', render: (row: LeadEmailMonitoring) => formatDate(row.message.sentAt ?? row.message.createdAt) },
    { header: 'Content', render: (row: LeadEmailMonitoring) => <details><summary>{row.message.subject}</summary><pre className="tableMessageBody">{row.message.body}</pre></details> }
  ];
  const auditColumns = [
    { header: 'Action', render: (row: LeadAuditLog) => row.action },
    { header: 'Lead', render: (row: LeadAuditLog) => row.leadId ?? '' },
    { header: 'Actor', render: (row: LeadAuditLog) => row.actorUsername ?? 'public' },
    { header: 'Change', render: (row: LeadAuditLog) => `${row.oldValue ?? ''}${row.oldValue || row.newValue ? ' → ' : ''}${row.newValue ?? ''}` },
    { header: 'Description', render: (row: LeadAuditLog) => row.details ?? '' },
    { header: 'Created', render: (row: LeadAuditLog) => formatDate(row.createdAt) }
  ];
  const exportColumns = [
    { header: 'Admin', render: (row: LeadExportLog) => row.exportedByUsername },
    { header: 'Role', render: (row: LeadExportLog) => row.exporterRole },
    { header: 'Mode', render: (row: LeadExportLog) => row.masked ? 'MASKED' : 'FULL' },
    { header: 'Records', render: (row: LeadExportLog) => row.recordCount },
    { header: 'Created', render: (row: LeadExportLog) => formatDate(row.createdAt) }
  ];

  return <section>
    <header className="pageHeader"><div><p className="eyebrow">HEAD_ADMIN</p><h1>Lead monitoring</h1><p className="muted">Email content, audit trail, exports, and templates.</p></div></header>
    {error && <ErrorMessage message={error} />}
    <section className="card">
      <header className="pageHeader"><div><h2>Email templates</h2><p className="muted">Admins can edit the full subject and body after selecting one.</p></div><button type="button" onClick={() => edit()}>New template</button></header>
      {templates.loading ? <Loading /> : <DataTable rows={templates.data ?? []} columns={[
        { header: 'Name', render: (row: LeadEmailTemplate) => row.name },
        { header: 'Subject', render: (row: LeadEmailTemplate) => row.subject },
        { header: 'Active', render: (row: LeadEmailTemplate) => row.active ? 'Yes' : 'No' },
        { header: 'Action', render: (row: LeadEmailTemplate) => <button type="button" onClick={() => edit(row)}>Edit</button> }
      ]} />}
      {templateEditorOpen && <form className="stack templateEditor" onSubmit={saveTemplate}>
        <h3>{editing ? 'Edit template' : 'New template'}</h3>
        <label>Name<input required maxLength={180} value={form.name} onChange={(e) => setForm((current) => ({ ...current, name: e.target.value }))} /></label>
        <label>Subject<input required maxLength={255} value={form.subject} onChange={(e) => setForm((current) => ({ ...current, subject: e.target.value }))} /></label>
        <label>Body<textarea required value={form.body} onChange={(e) => setForm((current) => ({ ...current, body: e.target.value }))} /></label>
        <label className="checkLabel"><input type="checkbox" checked={form.active} onChange={(e) => setForm((current) => ({ ...current, active: e.target.checked }))} /> Active</label>
        <div className="buttonGroup"><button>Save</button><button type="button" className="secondaryButton" onClick={() => { setEditing(null); setTemplateEditorOpen(false); setForm({ name: '', subject: '', body: '', active: true }); }}>Cancel</button></div>
      </form>}
    </section>
    <section className="card"><h2>All sent lead emails</h2>{emails.loading ? <Loading /> : emails.error ? <ErrorMessage message={emails.error} /> : <DataTable rows={emails.data?.content ?? []} columns={emailColumns} />}</section>
    <section className="card"><h2>Lead audit and security events</h2>{audits.loading ? <Loading /> : audits.error ? <ErrorMessage message={audits.error} /> : <DataTable rows={audits.data?.content ?? []} columns={auditColumns} />}</section>
    <section className="card"><h2>CSV export logs</h2>{exports.loading ? <Loading /> : exports.error ? <ErrorMessage message={exports.error} /> : <DataTable rows={exports.data?.content ?? []} columns={exportColumns} />}</section>
  </section>;
}
