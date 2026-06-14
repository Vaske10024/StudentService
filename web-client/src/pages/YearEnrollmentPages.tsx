import { FormEvent, useEffect, useState } from 'react';
import { apiErrorMessage } from '../api/client';
import type { EnrollmentSubject, YearEnrollmentRequest } from '../api/types';
import { yearEnrollmentApi } from '../api/yearEnrollment';
import { DataTable } from '../components/DataTable';
import { Modal } from '../components/Modal';
import { ErrorMessage, Loading } from '../components/Status';
import { useApi } from '../hooks/useApi';

const activeStatuses = ['SUBMITTED', 'PENDING_DOCUMENTS', 'PENDING_ADMIN_APPROVAL', 'NEEDS_CHANGES'];

const typeLabels: Record<string, string> = {
  ENROLL_NEXT_YEAR: 'Upis naredne godine',
  CONDITIONAL_ENROLLMENT: 'Uslovni upis',
  RENEW_YEAR: 'Obnova godine'
};

const statusLabels: Record<string, string> = {
  SUBMITTED: 'Podnet',
  PENDING_DOCUMENTS: 'Ceka dokumentaciju',
  PENDING_ADMIN_APPROVAL: 'Ceka odobrenje admina',
  APPROVED: 'Odobren',
  REJECTED: 'Odbijen',
  NEEDS_CHANGES: 'Potrebna dopuna',
  CANCELLED: 'Otkazan'
};

function typeLabel(value?: string | null): string {
  return value ? typeLabels[value] ?? value : '-';
}

function statusLabel(value?: string | null): string {
  return value ? statusLabels[value] ?? value : '-';
}

function subjectLabel(subject: EnrollmentSubject): string {
  return `${subject.sifra} - ${subject.naziv} (${subject.espb ?? 0} ESPB)`;
}

function ChecklistSummary({ request }: { request: YearEnrollmentRequest }) {
  return <div className="checklistSummary">
    <span className={request.contractReceived ? 'checkDone' : 'checkMissing'}>Ugovor</span>
    <span className={request.paymentConfirmed ? 'checkDone' : 'checkMissing'}>Uplata</span>
    <span className={request.documentationComplete ? 'checkDone' : 'checkMissing'}>Dokumentacija</span>
  </div>;
}

export function StudentYearEnrollmentPage() {
  const eligibility = useApi(yearEnrollmentApi.eligibility, []);
  const requests = useApi(yearEnrollmentApi.mine, []);
  const [selected, setSelected] = useState<number[]>([]);
  const [studentNote, setStudentNote] = useState('');
  const [editingId, setEditingId] = useState<number | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  function toggleSubject(id: number) {
    setSelected((current) => current.includes(id) ? current.filter((value) => value !== id) : [...current, id]);
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (!eligibility.data?.suggestedType || !eligibility.data.targetSchoolYear) return;
    setSubmitting(true); setMessage(null); setActionError(null);
    try {
      if (editingId) {
        await yearEnrollmentApi.resubmit(editingId, { transferredSubjectIds: selected, studentNote });
        setMessage('Dopunjeni zahtev je ponovo poslat.');
      } else {
        await yearEnrollmentApi.submit({
          type: eligibility.data.suggestedType,
          targetSchoolYearId: eligibility.data.targetSchoolYear.id,
          transferredSubjectIds: selected,
          studentNote
        });
        setMessage('Zahtev je podnet. Potpisite ugovor i donesite potvrdu uplate i dokumentaciju u studentsku sluzbu.');
      }
      setSelected([]); setStudentNote(''); setEditingId(null);
      await Promise.all([eligibility.reload(), requests.reload()]);
    } catch (error) {
      setActionError(apiErrorMessage(error, 'Slanje zahteva nije uspelo.'));
    } finally {
      setSubmitting(false);
    }
  }

  async function cancel(request: YearEnrollmentRequest) {
    if (!window.confirm('Da li zelite da otkazete ovaj zahtev?')) return;
    try {
      await yearEnrollmentApi.cancel(request.id);
      setMessage('Zahtev je otkazan.');
      await Promise.all([eligibility.reload(), requests.reload()]);
    } catch (error) {
      setActionError(apiErrorMessage(error, 'Otkazivanje zahteva nije uspelo.'));
    }
  }

  function edit(request: YearEnrollmentRequest) {
    setEditingId(request.id);
    setSelected(request.transferredSubjects.map((subject) => subject.id));
    setStudentNote(request.studentNote ?? '');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  if (eligibility.loading || requests.loading) return <Loading />;
  if (eligibility.error) return <ErrorMessage message={eligibility.error} />;
  const value = eligibility.data;
  if (!value) return <ErrorMessage message="Podaci o uslovu nisu dostupni." />;
  const renewalSelectionMissing = value.suggestedType === 'RENEW_YEAR'
    && value.transferableSubjects.length > 0 && selected.length === 0;

  const passedColumns = [
    { header: 'Sifra', render: (row: Record<string, unknown>) => String(row.sifra ?? '') },
    { header: 'Predmet', render: (row: Record<string, unknown>) => String(row.naziv ?? '') },
    { header: 'ESPB', render: (row: Record<string, unknown>) => String(row.espb ?? '') },
    { header: 'Ocena', render: (row: Record<string, unknown>) => String(row.ocena ?? '') }
  ];
  const requestColumns = [
    { header: 'Ciljna skolska godina', render: (row: YearEnrollmentRequest) => row.targetSchoolYear?.godina },
    { header: 'Tip', render: (row: YearEnrollmentRequest) => typeLabel(row.type) },
    { header: 'Godina studija', render: (row: YearEnrollmentRequest) => `${row.currentStudyYear}. -> ${row.requestedStudyYear}.` },
    { header: 'Status', render: (row: YearEnrollmentRequest) => <span className="statusBadge">{statusLabel(row.status)}</span> },
    { header: 'Dokumenti', render: (row: YearEnrollmentRequest) => <ChecklistSummary request={row} /> },
    { header: 'Napomena admina', render: (row: YearEnrollmentRequest) => row.adminNote || '-' },
    { header: 'Akcija', render: (row: YearEnrollmentRequest) => <div className="buttonGroup">
      {row.status === 'NEEDS_CHANGES' && <button type="button" onClick={() => edit(row)}>Dopuni</button>}
      {activeStatuses.includes(row.status) && <button type="button" className="secondaryButton" onClick={() => void cancel(row)}>Otkazi</button>}
    </div> }
  ];

  return <section>
    <header className="pageHeader"><div><h1>Upis i obnova godine</h1><p className="muted">Sistem racuna uslov iz polozenih predmeta i ostvarenih ESPB bodova.</p></div><span className="statusBadge">{typeLabel(value.suggestedType)}</span></header>
    {message && <p className="success">{message}</p>}{actionError && <ErrorMessage message={actionError} />}
    <div className="metricGrid">
      <article className="metricCard"><span>Trenutna godina studija</span><strong>{value.currentStudyYear ?? '-'}</strong><small>{value.currentSchoolYear?.godina ?? 'Nema evidentiranog upisa'}</small></article>
      <article className="metricCard"><span>Ciljna godina studija</span><strong>{value.requestedStudyYear ?? '-'}</strong><small>{value.targetSchoolYear?.godina ?? 'Sledeca godina nije konfigurisana'}</small></article>
      <article className="metricCard"><span>Ostvareni ESPB</span><strong>{value.earnedEcts}</strong><small>Redovan: {value.regularEnrollmentThreshold ?? '-'} / uslovni: {value.conditionalEnrollmentThreshold ?? '-'}</small></article>
      <article className="metricCard"><span>Predlog sistema</span><strong>{typeLabel(value.suggestedType)}</strong><small>{value.message}</small></article>
    </div>
    <section className="card instructionPanel"><h2>Sta student mora da uradi uzivo</h2><p>Posle slanja zahtev ceka dokumentaciju. Student mora u studentskoj sluzbi da potpise ugovor i donese potvrdu uplate i potrebnu dokumentaciju. Tek admin odobrenje stvarno evidentira upis ili obnovu.</p></section>
    <form className="card" onSubmit={submit}>
      <header className="pageHeader"><div><h2>{editingId ? 'Dopuna zahteva' : 'Novi zahtev'}</h2><p className="muted">Izaberite nepolozene predmete koje zelite da prenesete.</p></div>{editingId && <button type="button" className="secondaryButton" onClick={() => { setEditingId(null); setSelected([]); setStudentNote(''); }}>Odustani od dopune</button>}</header>
      <div className="subjectChecklist">
        {value.transferableSubjects.length === 0 && <p className="muted">Nema nepolozenih predmeta za prenos.</p>}
        {value.transferableSubjects.map((subject) => <label className="checkLabel" key={subject.id}><input type="checkbox" checked={selected.includes(subject.id)} onChange={() => toggleSubject(subject.id)} />{subjectLabel(subject)}</label>)}
      </div>
      <label>Napomena studentu sluzbi<textarea value={studentNote} maxLength={2000} onChange={(event) => setStudentNote(event.target.value)} /></label>
      <div className="toolbar"><button disabled={submitting || renewalSelectionMissing || (!editingId && !value.canSubmit)}>{editingId ? 'Posalji dopunu' : 'Podnesi zahtev'}</button><span className="muted">{selected.length} predmeta izabrano{renewalSelectionMissing ? ' - za obnovu izaberite najmanje jedan predmet' : ''}</span></div>
    </form>
    <section className="card"><h2>Polozeni predmeti</h2><DataTable rows={value.passedSubjects} columns={passedColumns} empty="Nema evidentiranih polozenih predmeta." /></section>
    <section className="card"><h2>Istorija zahteva</h2>{requests.error ? <ErrorMessage message={requests.error} /> : <DataTable rows={requests.data ?? []} columns={requestColumns} empty="Jos nema zahteva za upis ili obnovu godine." />}</section>
  </section>;
}

export function AdminYearEnrollmentsPage() {
  const [status, setStatus] = useState('');
  const [type, setType] = useState('');
  const [studentIndeksId, setStudentIndeksId] = useState('');
  const requests = useApi(() => yearEnrollmentApi.adminList({ status, type, studentIndeksId }), [status, type, studentIndeksId]);
  const [selected, setSelected] = useState<YearEnrollmentRequest | null>(null);
  const [checklist, setChecklist] = useState({ contractReceived: false, paymentConfirmed: false, documentationComplete: false, note: '' });
  const [message, setMessage] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);

  useEffect(() => {
    if (!selected) return;
    setChecklist({
      contractReceived: selected.contractReceived,
      paymentConfirmed: selected.paymentConfirmed,
      documentationComplete: selected.documentationComplete,
      note: selected.adminNote ?? ''
    });
  }, [selected]);

  async function open(request: YearEnrollmentRequest) {
    try { setSelected(await yearEnrollmentApi.adminDetail(request.id)); }
    catch (error) { setActionError(apiErrorMessage(error, 'Detalji zahteva nisu dostupni.')); }
  }

  async function saveChecklist(event: FormEvent) {
    event.preventDefault();
    if (!selected) return;
    try {
      const updated = await yearEnrollmentApi.checklist(selected.id, checklist);
      setSelected(updated); setMessage('Checklist dokumentacije je sacuvan.'); await requests.reload();
    } catch (error) { setActionError(apiErrorMessage(error, 'Cuvanje checkliste nije uspelo.')); }
  }

  async function approve() {
    if (!selected || !window.confirm('Odobriti zahtev i stvarno evidentirati upis godine?')) return;
    try {
      const updated = await yearEnrollmentApi.approve(selected.id);
      setSelected(updated); setMessage('Zahtev je odobren i upis je evidentiran.'); await requests.reload();
    } catch (error) { setActionError(apiErrorMessage(error, 'Odobrenje nije uspelo.')); }
  }

  async function decide(kind: 'reject' | 'needsChanges') {
    if (!selected) return;
    const reason = window.prompt(kind === 'reject' ? 'Razlog odbijanja' : 'Sta student treba da dopuni')?.trim();
    if (!reason) return;
    try {
      const updated = kind === 'reject'
        ? await yearEnrollmentApi.reject(selected.id, reason)
        : await yearEnrollmentApi.needsChanges(selected.id, reason);
      setSelected(updated); setMessage(kind === 'reject' ? 'Zahtev je odbijen.' : 'Zahtev je vracen na dopunu.'); await requests.reload();
    } catch (error) { setActionError(apiErrorMessage(error, 'Obrada zahteva nije uspela.')); }
  }

  if (requests.loading) return <Loading />;
  if (requests.error) return <ErrorMessage message={requests.error} />;
  const columns = [
    { header: 'Student', render: (row: YearEnrollmentRequest) => <><strong>{row.studentName}</strong><br /><span className="muted">{row.indexLabel} / ID {row.indeksId}</span></> },
    { header: 'Tip', render: (row: YearEnrollmentRequest) => typeLabel(row.type) },
    { header: 'Godina', render: (row: YearEnrollmentRequest) => `${row.currentStudyYear}. -> ${row.requestedStudyYear}. (${row.targetSchoolYear.godina})` },
    { header: 'ESPB', render: (row: YearEnrollmentRequest) => row.earnedEctsSnapshot },
    { header: 'Status', render: (row: YearEnrollmentRequest) => <span className="statusBadge">{statusLabel(row.status)}</span> },
    { header: 'Dokumenti', render: (row: YearEnrollmentRequest) => <ChecklistSummary request={row} /> },
    { header: 'Akcija', render: (row: YearEnrollmentRequest) => <button type="button" onClick={() => void open(row)}>Otvori</button> }
  ];

  return <section>
    <header className="pageHeader"><div><h1>Zahtevi za upis i obnovu godine</h1><p className="muted">Upis se menja tek eksplicitnim admin odobrenjem.</p></div></header>
    {message && <p className="success">{message}</p>}{actionError && <ErrorMessage message={actionError} />}
    <section className="card"><div className="formGrid">
      <label>Status<select value={status} onChange={(event) => setStatus(event.target.value)}><option value="">Svi statusi</option>{Object.keys(statusLabels).map((value) => <option key={value} value={value}>{statusLabel(value)}</option>)}</select></label>
      <label>Tip<select value={type} onChange={(event) => setType(event.target.value)}><option value="">Svi tipovi</option>{Object.keys(typeLabels).map((value) => <option key={value} value={value}>{typeLabel(value)}</option>)}</select></label>
      <label>ID indeksa<input value={studentIndeksId} onChange={(event) => setStudentIndeksId(event.target.value)} placeholder="Opcioni filter" /></label>
    </div></section>
    <section className="card"><DataTable rows={requests.data ?? []} columns={columns} empty="Nema zahteva za izabrane filtere." /></section>
    {selected && <Modal title={`Zahtev #${selected.id} - ${selected.studentName ?? selected.indexLabel}`} onClose={() => setSelected(null)}>
      <div className="metricGrid">
        <article className="metricCard"><span>Tip</span><strong>{typeLabel(selected.type)}</strong><small>{selected.currentStudyYear}.{' -> '}{selected.requestedStudyYear}. godina</small></article>
        <article className="metricCard"><span>Skolska godina</span><strong>{selected.targetSchoolYear.godina}</strong><small>Prethodna: {selected.currentSchoolYear.godina}</small></article>
        <article className="metricCard"><span>ESPB snapshot</span><strong>{selected.earnedEctsSnapshot}</strong><small>U trenutku podnosenja</small></article>
        <article className="metricCard"><span>Status</span><strong>{statusLabel(selected.status)}</strong><small>{selected.approvedEnrollmentId ? `Upis #${selected.approvedEnrollmentId}` : 'Upis jos nije promenjen'}</small></article>
      </div>
      <section><h3>Preneti predmeti</h3><ul>{selected.transferredSubjects.length ? selected.transferredSubjects.map((subject) => <li key={subject.id}>{subjectLabel(subject)}</li>) : <li>Nema izabranih predmeta za prenos.</li>}</ul></section>
      <form className="card" onSubmit={saveChecklist}><h3>Provera uzivo</h3><div className="subjectChecklist">
        <label className="checkLabel"><input type="checkbox" checked={checklist.contractReceived} onChange={(event) => setChecklist({ ...checklist, contractReceived: event.target.checked })} />Ugovor je potpisan</label>
        <label className="checkLabel"><input type="checkbox" checked={checklist.paymentConfirmed} onChange={(event) => setChecklist({ ...checklist, paymentConfirmed: event.target.checked })} />Uplata je potvrdjena</label>
        <label className="checkLabel"><input type="checkbox" checked={checklist.documentationComplete} onChange={(event) => setChecklist({ ...checklist, documentationComplete: event.target.checked })} />Dokumentacija je kompletna</label>
      </div><label>Napomena admina<textarea value={checklist.note} maxLength={2000} onChange={(event) => setChecklist({ ...checklist, note: event.target.value })} /></label><button disabled={!activeStatuses.includes(selected.status)}>Sacuvaj checklistu</button></form>
      <section><h3>Istorija statusa</h3><DataTable rows={selected.history} columns={[
        { header: 'Vreme', render: (row) => row.createdAt },
        { header: 'Prethodni status', render: (row) => statusLabel(row.oldStatus) },
        { header: 'Novi status', render: (row) => statusLabel(row.newStatus) },
        { header: 'Napomena', render: (row) => row.note || '-' }
      ]} /></section>
      <div className="buttonGroup modalActions">
        <button type="button" disabled={selected.status !== 'PENDING_ADMIN_APPROVAL'} onClick={() => void approve()}>Odobri i evidentiraj upis</button>
        <button type="button" className="secondaryButton" disabled={!activeStatuses.includes(selected.status)} onClick={() => void decide('needsChanges')}>Vrati na dopunu</button>
        <button type="button" className="dangerButton" disabled={!activeStatuses.includes(selected.status)} onClick={() => void decide('reject')}>Odbij</button>
      </div>
    </Modal>}
  </section>;
}
