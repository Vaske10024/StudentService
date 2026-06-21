import { FormEvent, useState } from 'react';
import { changePassword } from '../api/auth';
import { apiErrorMessage, apiRequest } from '../api/client';
import { meApi } from '../api/me';
import type { StudentDashboard } from '../api/types';
import { DataTable } from '../components/DataTable';
import { ErrorMessage, Loading } from '../components/Status';
import { StudentOverview } from '../components/StudentOverview';
import { useApi } from '../hooks/useApi';
import { asRows, pick } from './dataHelpers';

type ChartSlice = {
  label: string;
  value: number;
  color: string;
};

const subjectColumns = [
  { header: 'Code', render: (row: Record<string, unknown>) => pick(row, ['code', 'sifra']) },
  { header: 'Subject', render: (row: Record<string, unknown>) => pick(row, ['name', 'naziv']) },
  { header: 'ECTS', render: (row: Record<string, unknown>) => pick(row, ['ects', 'espb']) },
  { header: 'Year / semester', render: (row: Record<string, unknown>) => `${pick(row, ['studyYear', 'godina'])} / ${pick(row, ['semester', 'semestar'])}` },
  { header: 'School year', render: (row: Record<string, unknown>) => pick(row, ['schoolYear']) },
  { header: 'Instructors', render: (row: Record<string, unknown>) => instructorNames(row.instructors) }
];

const examColumns = [
  { header: 'Subject', render: (row: Record<string, unknown>) => `${pick(row, ['predmetSifra'])} ${pick(row, ['predmetNaziv'])}` },
  { header: 'Exam date', render: (row: Record<string, unknown>) => `${pick(row, ['datumIspita'])} ${pick(row, ['vremePocetka'])}` },
  { header: 'Instructor', render: (row: Record<string, unknown>) => pick(row, ['nastavnikImePrezime']) },
  { header: 'Grade', render: (row: Record<string, unknown>) => pick(row, ['ocena', 'grade']) },
  { header: 'Points', render: (row: Record<string, unknown>) => pick(row, ['ukupnoPoena']) },
  { header: 'Status', render: (row: Record<string, unknown>) => pick(row, ['status']) }
];

const paymentColumns = [
  { header: 'Date', render: (row: Record<string, unknown>) => pick(row, ['createdAt']) },
  { header: 'Type', render: (row: Record<string, unknown>) => pick(row, ['type']) },
  { header: 'Amount', render: (row: Record<string, unknown>) => money(row.amountEur, 'EUR') },
  { header: 'Description', render: (row: Record<string, unknown>) => pick(row, ['description']) }
];

const passedColumns = [
  { header: 'Code', render: (row: Record<string, unknown>) => pick(row, ['sifra']) },
  { header: 'Subject', render: (row: Record<string, unknown>) => pick(row, ['naziv']) },
  { header: 'ECTS', render: (row: Record<string, unknown>) => pick(row, ['espb']) },
  { header: 'Pre-exam points', render: (row: Record<string, unknown>) => pick(row, ['predispitniPoeni']) },
  { header: 'Exam points', render: (row: Record<string, unknown>) => pick(row, ['ispitniPoeni']) },
  { header: 'Total', render: (row: Record<string, unknown>) => pick(row, ['ukupnoPoena']) },
  { header: 'Grade', render: (row: Record<string, unknown>) => pick(row, ['ocena']) },
  { header: 'Method', render: (row: Record<string, unknown>) => pick(row, ['nacin']) },
  { header: 'Date', render: (row: Record<string, unknown>) => pick(row, ['datum']) }
];

const notPassedColumns = [
  { header: 'Code', render: (row: Record<string, unknown>) => pick(row, ['sifra']) },
  { header: 'Subject', render: (row: Record<string, unknown>) => pick(row, ['naziv']) },
  { header: 'ECTS', render: (row: Record<string, unknown>) => pick(row, ['espb']) },
  { header: 'Pre-exam points', render: (row: Record<string, unknown>) => pick(row, ['predispitniPoeni']) },
  { header: 'Exam points', render: (row: Record<string, unknown>) => pick(row, ['ispitniPoeni']) },
  { header: 'Total', render: (row: Record<string, unknown>) => pick(row, ['ukupnoPoena']) },
  { header: 'Description', render: (row: Record<string, unknown>) => pick(row, ['opis']) }
];

function record(value: unknown): Record<string, unknown> {
  return value && typeof value === 'object' ? value as Record<string, unknown> : {};
}

function instructorNames(value: unknown): string {
  return asRows(value).map((item) => {
    const name = String(item.nastavnikImePrezime ?? '');
    const role = String(item.uloga ?? '');
    return role ? `${name} (${role.toLowerCase()})` : name;
  }).filter(Boolean).join(', ') || 'Not assigned';
}

function money(value: unknown, currency: 'EUR' | 'RSD'): string {
  const number = Number(value);
  if (!Number.isFinite(number)) return '—';
  return new Intl.NumberFormat('sr-RS', { style: 'currency', currency }).format(number);
}

function numeric(value: unknown): number {
  const number = Number(value);
  return Number.isFinite(number) ? number : 0;
}

function sumByKeys(rows: Record<string, unknown>[], keys: string[]): number {
  return rows.reduce((sum, row) => {
    for (const key of keys) {
      if (row[key] !== undefined && row[key] !== null && row[key] !== '') return sum + numeric(row[key]);
    }
    return sum;
  }, 0);
}

function percent(value: number, total: number): string {
  if (!total) return '0%';
  return `${Math.round((value / total) * 100)}%`;
}

function DonutChart({ title, subtitle, slices, emptyLabel = 'No data' }: { title: string; subtitle: string; slices: ChartSlice[]; emptyLabel?: string }) {
  const cleanSlices = slices.filter((slice) => slice.value > 0);
  const total = cleanSlices.reduce((sum, slice) => sum + slice.value, 0);
  const radius = 43;
  const circumference = 2 * Math.PI * radius;
  let offset = 0;

  return (
    <article className="chartCard">
      <div className="chartHeader">
        <span>{title}</span>
        <strong>{subtitle}</strong>
      </div>
      <div className="donutWrap">
        <svg className="donutChart" viewBox="0 0 120 120" role="img" aria-label={`${title}: ${subtitle}`}>
          <circle className="donutTrack" cx="60" cy="60" r={radius} />
          {total === 0 ? (
            <circle className="donutEmpty" cx="60" cy="60" r={radius} />
          ) : cleanSlices.map((slice) => {
            const arc = (slice.value / total) * circumference;
            const dashOffset = -offset;
            offset += arc;
            return (
              <circle
                key={slice.label}
                className="donutSegment"
                cx="60"
                cy="60"
                r={radius}
                stroke={slice.color}
                strokeDasharray={`${arc} ${circumference - arc}`}
                strokeDashoffset={dashOffset}
              />
            );
          })}
          <text className="donutTotal" x="60" y="57" textAnchor="middle">{total}</text>
          <text className="donutLabel" x="60" y="73" textAnchor="middle">{total ? 'total' : emptyLabel}</text>
        </svg>
      </div>
      <div className="chartLegend">
        {slices.map((slice) => (
          <div className="legendItem" key={slice.label}>
            <span className="legendSwatch" style={{ background: slice.color }} aria-hidden="true" />
            <span>{slice.label}</span>
            <strong>{slice.value} <small>{percent(slice.value, total)}</small></strong>
          </div>
        ))}
      </div>
    </article>
  );
}

function StudentProfileCharts({ dashboard }: { dashboard: StudentDashboard }) {
  const passed = asRows(dashboard.passedSubjects);
  const notPassed = asRows(dashboard.failedOrNotPassedSubjects);
  const current = asRows(dashboard.currentSubjects);
  const earnedEcts = sumByKeys(passed, ['espb', 'ects']);
  const remainingEcts = sumByKeys(notPassed, ['espb', 'ects']);
  const activeEcts = sumByKeys(current, ['ects', 'espb']);
  const gradeCounts = passed.reduce<Record<string, number>>((counts, row) => {
    const grade = numeric(row.ocena ?? row.grade);
    if (grade >= 6) counts[String(grade)] = (counts[String(grade)] ?? 0) + 1;
    return counts;
  }, {});
  const gradeColors: Record<string, string> = {
    '6': '#6b7d91',
    '7': '#2f80c9',
    '8': '#1c8c78',
    '9': '#d19624',
    '10': '#b34865'
  };
  const gradeSlices = Object.entries(gradeCounts)
    .sort(([a], [b]) => Number(a) - Number(b))
    .map(([grade, value]) => ({ label: `Grade ${grade}`, value, color: gradeColors[grade] ?? '#7c6eb0' }));

  return (
    <section className="card">
      <header className="sectionHeader">
        <div>
          <h2>Academic overview</h2>
          <p className="muted">Quick visual snapshot of passed exams, ECTS progress and grades.</p>
        </div>
      </header>
      <div className="chartGrid">
        <DonutChart
          title="Exam status"
          subtitle={`${passed.length} passed / ${notPassed.length} pending`}
          slices={[
            { label: 'Passed', value: passed.length, color: '#1c8c78' },
            { label: 'Not passed', value: notPassed.length, color: '#d19624' }
          ]}
        />
        <DonutChart
          title="ECTS balance"
          subtitle={`${earnedEcts} earned ECTS`}
          slices={[
            { label: 'Earned', value: earnedEcts, color: '#2f80c9' },
            { label: 'Remaining', value: remainingEcts, color: '#d45b5b' },
            { label: 'Currently listening', value: activeEcts, color: '#7c6eb0' }
          ]}
        />
        <DonutChart
          title="Grade spread"
          subtitle={gradeSlices.length ? `${gradeSlices.length} grade groups` : 'No passed grades yet'}
          slices={gradeSlices.length ? gradeSlices : [{ label: 'No grades', value: 0, color: '#a7b4c4' }]}
        />
      </div>
    </section>
  );
}

export function StudentDashboardPage() {
  const { data, loading, error } = useApi(meApi.studentDashboard, []);
  if (loading) return <Loading />;
  if (error) return <ErrorMessage message={error} />;
  const student = record(data?.student);
  const index = record(data?.activeIndex);
  const status = record(data?.status);
  const balance = record(data?.balance);
  const passed = asRows(data?.passedSubjects);
  const latestEnrollment = asRows(data?.studyEnrollments)[0] ?? {};
  const earnedEcts = passed.reduce((sum, item) => sum + Number(item.espb ?? 0), 0);
  const grades = passed.map((item) => Number(item.ocena)).filter(Number.isFinite);
  const average = grades.length ? (grades.reduce((sum, grade) => sum + grade, 0) / grades.length).toFixed(2) : '—';
  return (
    <section>
      <header className="studentHero card">
        <div className="profileAvatar">{String(student.ime ?? '?').slice(0, 1)}{String(student.prezime ?? '?').slice(0, 1)}</div>
        <div><p className="eyebrow">Welcome back</p><h1>{String(student.ime ?? '')} {String(student.prezime ?? '')}</h1><p className="muted">{String(student.emailFakultetski ?? '')}</p></div>
        <span className="statusBadge">{String(status.status ?? 'UNKNOWN')}</span>
      </header>
      <div className="metricGrid">
        <article className="metricCard"><span>Active index</span><strong>{String(index.studProgramOznaka ?? '—')} {String(index.broj ?? '—')}/{String(index.godina ?? '—')}</strong><small>{String(record(index.studijskiProgram).naziv ?? '')}</small></article>
        <article className="metricCard"><span>School year</span><strong>{String(record(data?.schoolYear).godina ?? '—')}</strong><small>{String(data?.currentSubjects?.length ?? 0)} current subjects</small></article>
        <article className="metricCard"><span>Current study year</span><strong>{String(latestEnrollment.godina ?? '—')}</strong><small>Enrollment: {String(latestEnrollment.skolskaGodina ?? '—')}</small></article>
        <article className="metricCard"><span>Earned ECTS</span><strong>{earnedEcts}</strong><small>{passed.length} passed subjects</small></article>
        <article className="metricCard"><span>Average grade</span><strong>{average}</strong><small>Across passed subjects</small></article>
        <article className="metricCard"><span>Remaining balance</span><strong>{money(balance.debtEur, 'EUR')}</strong><small>Ledger source of truth</small></article>
      </div>
      <section className="card"><h2>Current subjects</h2><DataTable rows={asRows(data?.currentSubjects)} columns={subjectColumns} /></section>
      <section className="card"><h2>Active exam registrations</h2><DataTable rows={asRows(data?.activeExamRegistrations)} columns={examColumns} /></section>
    </section>
  );
}

export function StudentProfilePage() {
  const { data, loading, error } = useApi(meApi.studentDashboard, []);
  const [passwords, setPasswords] = useState({ current: '', next: '', confirm: '' });
  const [passwordError, setPasswordError] = useState<string | null>(null);
  const [passwordMessage, setPasswordMessage] = useState<string | null>(null);

  async function submitPassword(event: FormEvent) {
    event.preventDefault();
    setPasswordError(null);
    setPasswordMessage(null);
    if (passwords.next !== passwords.confirm) {
      setPasswordError('New password and confirmation do not match.');
      return;
    }
    try {
      await changePassword(passwords.current, passwords.next);
      setPasswords({ current: '', next: '', confirm: '' });
      setPasswordMessage('Password changed successfully.');
    } catch (err) {
      setPasswordError(apiErrorMessage(err, 'Password change failed.'));
    }
  }

  if (loading) return <Loading />;
  if (error) return <ErrorMessage message={error} />;
  if (!data) return <section className="card"><h1>Student profile</h1><p className="muted">Profile data is not available.</p></section>;
  return (
    <section>
      <h1>Student profile</h1>
      <StudentProfileCharts dashboard={data} />
      <StudentOverview dashboard={data} />
      <section className="card">
        <h2>Change password</h2>
        <form className="formGrid" onSubmit={submitPassword}>
          <label>Current password<input required type="password" autoComplete="current-password" value={passwords.current} onChange={(e) => setPasswords({ ...passwords, current: e.target.value })} /></label>
          <label>New password<input required minLength={8} type="password" autoComplete="new-password" value={passwords.next} onChange={(e) => setPasswords({ ...passwords, next: e.target.value })} /></label>
          <label>Confirm new password<input required minLength={8} type="password" autoComplete="new-password" value={passwords.confirm} onChange={(e) => setPasswords({ ...passwords, confirm: e.target.value })} /></label>
          {passwordError && <p className="error">{passwordError}</p>}
          {passwordMessage && <p className="success">{passwordMessage}</p>}
          <button type="submit">Change password</button>
        </form>
      </section>
    </section>
  );
}

export function StudentSubjectsPage() {
  const { data, loading, error } = useApi(meApi.studentSubjects, []);
  if (loading) return <Loading />;
  if (error) return <ErrorMessage message={error} />;
  return <section className="card"><header className="pageHeader"><div><h1>My subjects</h1><p className="muted">Subjects assigned through your active study-year enrollment.</p></div><span className="statusBadge">{asRows(data).length} subjects</span></header><DataTable rows={asRows(data)} columns={subjectColumns} empty="No subjects are assigned for the active school year. Contact student services if your study year is already enrolled." /></section>;
}

export function StudentExamsPage() {
  const registrations = useApi(meApi.studentExams, []);
  const available = useApi(meApi.availableStudentExams, []);
  const [submitting, setSubmitting] = useState<number | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  async function register(examId: number) {
    const indexId = registrations.data?.activeIndex?.id;
    if (!indexId || submitting !== null) return;
    setSubmitting(examId); setActionError(null); setMessage(null);
    try {
      await apiRequest('/api/ispit/prijava', { method: 'POST', body: JSON.stringify({ ispitId: examId, studentIndeksId: indexId }) });
      setMessage('Ispit je uspešno prijavljen.');
      await Promise.all([registrations.reload(), available.reload()]);
    } catch (error) { setActionError(apiErrorMessage(error, 'Prijava ispita nije uspela.')); }
    finally { setSubmitting(null); }
  }

  async function cancel(registrationId: number) {
    if (submitting !== null || !window.confirm('Da li želite da odjavite ispit?')) return;
    setSubmitting(registrationId); setActionError(null); setMessage(null);
    try {
      await apiRequest(`/api/ispit/prijava/${registrationId}/odjavi`, { method: 'PATCH', body: JSON.stringify({ reason: 'Odjava studenta kroz portal' }) });
      setMessage('Ispit je uspešno odjavljen.');
      await Promise.all([registrations.reload(), available.reload()]);
    } catch (error) { setActionError(apiErrorMessage(error, 'Odjava ispita nije uspela.')); }
    finally { setSubmitting(null); }
  }

  if (registrations.loading || available.loading) return <Loading />;
  if (registrations.error) return <ErrorMessage message={registrations.error} />;
  const availableColumns = [
    { header: 'Predmet', render: (row: Record<string, unknown>) => `${pick(row, ['subjectCode'])} ${pick(row, ['subjectName'])}` },
    { header: 'Termin', render: (row: Record<string, unknown>) => `${pick(row, ['examDate'])} ${pick(row, ['examTime'])}` },
    { header: 'Prijava', render: (row: Record<string, unknown>) => `${pick(row, ['registrationStart'])} - ${pick(row, ['registrationEnd'])}` },
    { header: 'Odjava do', render: (row: Record<string, unknown>) => pick(row, ['cancellationEnd']) },
    { header: 'Status', render: (row: Record<string, unknown>) => <span className="statusBadge">{String(row.eligibilityMessage ?? '')}</span> },
    { header: 'Akcija', render: (row: Record<string, unknown>) => row.activeRegistrationId
      ? <button type="button" className="secondaryButton" disabled={!row.cancellationAllowed || submitting !== null} onClick={() => void cancel(Number(row.activeRegistrationId))}>Odjavi</button>
      : <button type="button" disabled={!row.eligible || submitting !== null} onClick={() => void register(Number(row.examId))}>Prijavi</button> }
  ];
  return (
    <section>
      {message && <p className="success">{message}</p>}{actionError && <ErrorMessage message={actionError} />}{available.error && <ErrorMessage message={available.error} />}
      <section className="card"><h1>Dostupni ispiti</h1><p className="muted">Prikazani su rokovi, prozori prijave i razlog kada prijava nije moguća.</p><DataTable rows={asRows(available.data)} columns={availableColumns} empty="Trenutno nema ispita za predmete koje slušate." /></section>
      <section className="card"><h1>Aktivne prijave</h1><DataTable rows={asRows(registrations.data?.activeExamRegistrations)} columns={examColumns} /></section>
      <section className="card"><h1>Prethodni izlasci</h1><DataTable rows={asRows(registrations.data?.previousExamAttempts)} columns={examColumns} /></section>
    </section>
  );
}

export function StudentPaymentsPage() {
  const { data, loading, error } = useApi(meApi.studentPayments, []);
  if (loading) return <Loading />;
  if (error) return <ErrorMessage message={error} />;
  const balance = record(data?.balance);
  return (
    <section>
      <section className="card"><h1>Payments</h1><DataTable rows={asRows(data?.payments)} columns={paymentColumns} /></section>
      <section className="card"><h2>Balance</h2><div className="metricGrid">
        <article className="metricCard"><span>Dugovanje</span><strong>{money(balance.debtEur, 'EUR')}</strong><small>Isti saldo se koristi za prijavu ispita</small></article>
        <article className="metricCard"><span>Pretplata</span><strong>{money(balance.creditEur, 'EUR')}</strong><small>Raspoloživ kredit</small></article>
        <article className="metricCard"><span>Saldo</span><strong>{money(balance.balanceEur, 'EUR')}</strong><small>Pozitivno znači dugovanje</small></article>
      </div></section>
    </section>
  );
}

export function StudentGradesPage() {
  const { data, loading, error } = useApi(meApi.studentDashboard, []);
  if (loading) return <Loading />;
  if (error) return <ErrorMessage message={error} />;
  return (
    <section>
      <section className="card"><h1>Passed subjects</h1><DataTable rows={asRows(data?.passedSubjects)} columns={passedColumns} /></section>
      <section className="card"><h1>Failed or not-passed subjects</h1><DataTable rows={asRows(data?.failedOrNotPassedSubjects)} columns={notPassedColumns} /></section>
    </section>
  );
}
