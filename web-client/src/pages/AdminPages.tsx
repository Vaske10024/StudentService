import { FormEvent, useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { apiErrorMessage, apiRequest } from '../api/client';
import { adminApi } from '../api/admin';
import { DataTable } from '../components/DataTable';
import { ErrorMessage, Loading } from '../components/Status';
import { Modal } from '../components/Modal';
import { StudentFinance, StudentIndexesTable, StudentLifecycle, StudentPersonalDetails, StudentRegistrations, StudentStudyHistory } from '../components/StudentOverview';
import { useApi } from '../hooks/useApi';
import { asRows, JsonBlock, pick } from './dataHelpers';

const entityColumns = [
  { header: 'ID', render: (row: Record<string, unknown>) => pick(row, ['id']) },
  { header: 'Name', render: (row: Record<string, unknown>) => pick(row, ['naziv', 'imePrezime', 'ime', 'oznaka', 'name']) },
  { header: 'Code', render: (row: Record<string, unknown>) => pick(row, ['oznaka', 'sifra', 'code']) },
  { header: 'Details', render: (row: Record<string, unknown>) => <details><summary>View</summary><JsonBlock value={row} /></details> }
];

const studentColumns = [
  { header: 'ID', render: (row: Record<string, unknown>) => pick(row, ['id', 'studentId']) },
  { header: 'Name', render: (row: Record<string, unknown>) => `${pick(row, ['ime', 'firstName'])} ${pick(row, ['prezime', 'lastName'])}` },
  { header: 'Email', render: (row: Record<string, unknown>) => pick(row, ['emailFakultetski', 'emailPrivatni', 'email']) },
  { header: 'Open', render: (row: Record<string, unknown>) => <Link to={`/admin/students/${String(row.id ?? row.studentId)}`}>Open</Link> }
];

const examColumns = [
  { header: 'ID', render: (row: Record<string, unknown>) => pick(row, ['id']) },
  { header: 'Subject', render: (row: Record<string, unknown>) => pick(row, ['predmetNaziv', 'nazivPredmeta']) },
  { header: 'Professor', render: (row: Record<string, unknown>) => pick(row, ['nastavnikImePrezime']) },
  { header: 'Date', render: (row: Record<string, unknown>) => pick(row, ['datumOdrzavanja']) },
  { header: 'Time', render: (row: Record<string, unknown>) => pick(row, ['vremePocetka']) },
  { header: 'Locked', render: (row: Record<string, unknown>) => pick(row, ['zakljucen']) }
];

function pageRows(data: unknown): Record<string, unknown>[] {
  if (Array.isArray(data)) return asRows(data);
  if (data && typeof data === 'object' && Array.isArray((data as { content?: unknown[] }).content)) return asRows((data as { content: unknown[] }).content);
  return [];
}

export function AdminDashboardPage() {
  const programs = useApi(adminApi.programs, []);
  const students = useApi(() => adminApi.students(0, 5), []);
  return (
    <section>
      <h1>Admin dashboard</h1>
      <div className="gridCards">
        <article className="card"><h2>Students</h2>{students.loading ? <Loading /> : <p className="metric">{pageRows(students.data).length}</p>}</article>
        <article className="card"><h2>Study programs</h2>{programs.loading ? <Loading /> : <p className="metric">{pageRows(programs.data).length}</p>}</article>
        <article className="card"><h2>Security</h2><p className="muted">Admin routes call protected backend endpoints only.</p></article>
      </div>
    </section>
  );
}

export function AdminStudentsPage() {
  const [query, setQuery] = useState('');
  const [page, setPage] = useState(0);
  const loader = () => query.trim()
    ? apiRequest<unknown>(`/api/student/search?ime=${encodeURIComponent(query.trim())}&prezime=${encodeURIComponent(query.trim())}&page=${page}&size=20`)
    : adminApi.students(page, 20);
  const { data, loading, error, reload } = useApi(loader, [query, page]);
  const totalPages = typeof data === 'object' && data !== null && 'totalPages' in data ? Number((data as { totalPages?: number }).totalPages ?? 1) : 1;
  return (
    <section className="card">
      <header className="pageHeader"><h1>Students</h1><Link className="buttonLink" to="/admin/students/new">New student</Link></header>
      <form className="toolbar" onSubmit={(e) => { e.preventDefault(); setPage(0); void reload(); }}>
        <input placeholder="Search by name" value={query} onChange={(e) => setQuery(e.target.value)} />
        <button type="submit">Search</button>
      </form>
      {loading && <Loading />}
      {error && <ErrorMessage message={error} />}
      {!loading && !error && <DataTable rows={pageRows(data)} columns={studentColumns} />}
      <footer className="pager"><button disabled={page <= 0} onClick={() => setPage((p) => p - 1)}>Previous</button><span>Page {page + 1} / {Math.max(1, totalPages)}</span><button disabled={page + 1 >= totalPages} onClick={() => setPage((p) => p + 1)}>Next</button></footer>
    </section>
  );
}

export function AdminStudentNewPage() {
  const [form, setForm] = useState({
    ime: '',
    prezime: '',
    srednjeIme: '',
    jmbg: '',
    datumRodjenja: '',
    mestoRodjenja: '',
    mestoPrebivalista: '',
    drzavaRodjenja: '',
    drzavljanstvo: '',
    nacionalnost: '',
    pol: '',
    adresa: '',
    brojTelefonaMobilni: '',
    brojTelefonaFiksni: '',
    fakultetskiEmail: '',
    privatniEmail: '',
    brojLicneKarte: '',
    licnuKartuIzdao: '',
    mestoStanovanja: '',
    adresaStanovanja: ''
  });
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  function update(field: keyof typeof form, value: string) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setSuccess(null);
    try {
      const payload = Object.fromEntries(
        Object.entries(form).map(([key, value]) => [key, value.trim() || undefined])
      );
      const id = await apiRequest<number>('/api/student/add', { method: 'POST', body: JSON.stringify(payload) });
      setSuccess(`Student created with id ${id}.`);
    } catch (err) {
      setError(apiErrorMessage(err, 'Student creation failed.'));
    }
  }

  return (
    <section className="card">
      <h1>New student</h1>
      <form className="formGrid" onSubmit={submit}>
        <label>First name *<input required value={form.ime} onChange={(e) => update('ime', e.target.value)} /></label>
        <label>Last name *<input required value={form.prezime} onChange={(e) => update('prezime', e.target.value)} /></label>
        <label>Middle name *<input required value={form.srednjeIme} onChange={(e) => update('srednjeIme', e.target.value)} /></label>
        <label>Date of birth *<input required type="date" value={form.datumRodjenja} onChange={(e) => update('datumRodjenja', e.target.value)} /></label>
        <label>Gender *<select required value={form.pol} onChange={(e) => update('pol', e.target.value)}><option value="">Select</option><option value="M">Male</option><option value="Z">Female</option></select></label>
        <label>Permanent residence *<input required value={form.mestoPrebivalista} onChange={(e) => update('mestoPrebivalista', e.target.value)} /></label>
        <label>Citizenship *<input required value={form.drzavljanstvo} onChange={(e) => update('drzavljanstvo', e.target.value)} /></label>
        <label>Permanent address *<input required value={form.adresa} onChange={(e) => update('adresa', e.target.value)} /></label>
        <label>Faculty email *<input required type="email" value={form.fakultetskiEmail} onChange={(e) => update('fakultetskiEmail', e.target.value)} /></label>
        <label>Private email<input type="email" value={form.privatniEmail} onChange={(e) => update('privatniEmail', e.target.value)} /></label>
        <label>JMBG<input value={form.jmbg} onChange={(e) => update('jmbg', e.target.value)} /></label>
        <label>Place of birth<input value={form.mestoRodjenja} onChange={(e) => update('mestoRodjenja', e.target.value)} /></label>
        <label>Country of birth<input value={form.drzavaRodjenja} onChange={(e) => update('drzavaRodjenja', e.target.value)} /></label>
        <label>Nationality<input value={form.nacionalnost} onChange={(e) => update('nacionalnost', e.target.value)} /></label>
        <label>Mobile phone<input value={form.brojTelefonaMobilni} onChange={(e) => update('brojTelefonaMobilni', e.target.value)} /></label>
        <label>Landline phone<input value={form.brojTelefonaFiksni} onChange={(e) => update('brojTelefonaFiksni', e.target.value)} /></label>
        <label>ID card number<input value={form.brojLicneKarte} onChange={(e) => update('brojLicneKarte', e.target.value)} /></label>
        <label>ID card issued by<input value={form.licnuKartuIzdao} onChange={(e) => update('licnuKartuIzdao', e.target.value)} /></label>
        <label>Current residence<input value={form.mestoStanovanja} onChange={(e) => update('mestoStanovanja', e.target.value)} /></label>
        <label>Current address<input value={form.adresaStanovanja} onChange={(e) => update('adresaStanovanja', e.target.value)} /></label>
        {error && <p className="error">{error}</p>}
        {success && <p className="success">{success}</p>}
        <button type="submit">Create</button>
      </form>
    </section>
  );
}

export function AdminStudentDetailPage() {
  const { id = '' } = useParams();
  const [tab, setTab] = useState<'profile' | 'status' | 'indexes' | 'study' | 'registrations' | 'payments'>('profile');
  const [indexId, setIndexId] = useState('');
  const [indexOpen, setIndexOpen] = useState(false);
  const [enrollOpen, setEnrollOpen] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);
  const [actionMessage, setActionMessage] = useState<string | null>(null);
  const [indexForm, setIndexForm] = useState({ godina: String(new Date().getFullYear()), studProgramOznaka: '', nacinFinansiranja: 'BUDZET', vaziOd: new Date().toISOString().slice(0, 10) });
  const [studyYear, setStudyYear] = useState('1');
  const profile = useApi(() => adminApi.studentDetails(id), [id]);
  const indexes = useApi(() => adminApi.studentIndexes(id), [id]);
  const programs = useApi(adminApi.programs, []);

  useEffect(() => {
    const rows = asRows(indexes.data);
    if (!indexId && rows[0]?.id !== undefined) {
      const active = rows.find((row) => row.aktivan === true) ?? rows[0];
      setIndexId(String(active.id));
    }
  }, [indexId, indexes.data]);

  const dashboard = useApi(
    () => indexId ? adminApi.studentDashboard(indexId) : Promise.resolve(null),
    [indexId]
  );

  async function createIndex(event: FormEvent) {
    event.preventDefault();
    setActionError(null);
    try {
      const createdId = await adminApi.createIndex({
        godina: Number(indexForm.godina),
        studProgramOznaka: indexForm.studProgramOznaka,
        nacinFinansiranja: indexForm.nacinFinansiranja,
        aktivan: true,
        vaziOd: indexForm.vaziOd,
        student: { id: Number(id) }
      });
      setActionMessage(`Index created with id ${createdId}. The student can now log in using their faculty email as both username and temporary password.`);
      setIndexId(String(createdId));
      setIndexOpen(false);
      await indexes.reload();
    } catch (err) {
      setActionError(apiErrorMessage(err, 'Index creation failed.'));
    }
  }

  async function enrollStudyYear(event: FormEvent) {
    event.preventDefault();
    setActionError(null);
    try {
      await adminApi.enrollStudyYear(indexId, Number(studyYear));
      setActionMessage(`Study year ${studyYear} enrolled. All program subjects for that year were assigned.`);
      setEnrollOpen(false);
      await dashboard.reload();
    } catch (err) {
      setActionError(apiErrorMessage(err, 'Study year enrollment failed.'));
    }
  }

  async function syncSubjects() {
    setActionError(null);
    try {
      const count = await adminApi.syncStudentSubjects(indexId);
      setActionMessage(`Current subjects synchronized. The student now has ${count} subjects in the active school year.`);
      await dashboard.reload();
    } catch (err) {
      setActionError(apiErrorMessage(err, 'Subject synchronization failed.'));
    }
  }

  if (profile.loading || indexes.loading) return <Loading />;
  if (profile.error) return <ErrorMessage message={profile.error} />;
  if (indexes.error) return <ErrorMessage message={indexes.error} />;

  const needsDashboard = tab === 'status' || tab === 'study' || tab === 'registrations' || tab === 'payments';

  return (
    <section className="card">
      <header className="pageHeader">
        <div>
          <h1>Student details</h1>
          <p className="muted">{`${pick(profile.data, ['ime'])} ${pick(profile.data, ['prezime'])}`}</p>
        </div>
        <div className="headerActions">
          <label>
            Selected index
            <select value={indexId} onChange={(event) => setIndexId(event.target.value)}>
              {!asRows(indexes.data).length && <option value="">No indexes available</option>}
              {asRows(indexes.data).map((index) => (
                <option key={String(index.id)} value={String(index.id)}>
                  {`${pick(index, ['studProgramOznaka'])} ${pick(index, ['broj'])}/${pick(index, ['godina'])}${index.aktivan ? ' (active)' : ''}`}
                </option>
              ))}
            </select>
          </label>
          <div className="buttonGroup"><button type="button" onClick={() => { setActionError(null); setIndexOpen(true); }}>Create index</button><button type="button" className="secondaryButton" disabled={!indexId} onClick={() => { setActionError(null); setEnrollOpen(true); }}>Enroll study year</button><button type="button" className="secondaryButton" disabled={!indexId} onClick={() => void syncSubjects()}>Sync current subjects</button></div>
        </div>
      </header>
      {actionMessage && <p className="success">{actionMessage}</p>}
      {actionError && <ErrorMessage message={actionError} />}

      <nav className="tabs" aria-label="Student detail sections">
        <button className={tab === 'profile' ? 'active' : ''} type="button" onClick={() => setTab('profile')}>Profile</button>
        <button className={tab === 'status' ? 'active' : ''} type="button" disabled={!indexId} onClick={() => setTab('status')}>Status</button>
        <button className={tab === 'indexes' ? 'active' : ''} type="button" onClick={() => setTab('indexes')}>Indexes</button>
        <button className={tab === 'study' ? 'active' : ''} type="button" disabled={!indexId} onClick={() => setTab('study')}>Enrollment / renewal</button>
        <button className={tab === 'registrations' ? 'active' : ''} type="button" disabled={!indexId} onClick={() => setTab('registrations')}>Registrations</button>
        <button className={tab === 'payments' ? 'active' : ''} type="button" disabled={!indexId} onClick={() => setTab('payments')}>Payments</button>
      </nav>

      {tab === 'profile' && <StudentPersonalDetails student={profile.data} />}
      {tab === 'indexes' && <StudentIndexesTable indexes={indexes.data} />}
      {needsDashboard && dashboard.loading && <Loading />}
      {needsDashboard && dashboard.error && <ErrorMessage message={dashboard.error} />}
      {tab === 'status' && !dashboard.loading && !dashboard.error && <StudentLifecycle dashboard={dashboard.data} />}
      {tab === 'study' && !dashboard.loading && !dashboard.error && <StudentStudyHistory dashboard={dashboard.data} />}
      {tab === 'registrations' && !dashboard.loading && !dashboard.error && <StudentRegistrations dashboard={dashboard.data} />}
      {tab === 'payments' && !dashboard.loading && !dashboard.error && <StudentFinance dashboard={dashboard.data} />}
      {indexOpen && <Modal title="Create student index" onClose={() => setIndexOpen(false)}>
        <form className="formGrid" onSubmit={createIndex}>
          <label>Study program *<select required value={indexForm.studProgramOznaka} onChange={(e) => setIndexForm({ ...indexForm, studProgramOznaka: e.target.value })}><option value="">Select</option>{asRows(programs.data).map((program) => <option key={String(program.id)} value={String(program.oznaka)}>{`${pick(program, ['oznaka'])} - ${pick(program, ['naziv'])}`}</option>)}</select></label>
          <label>Enrollment year *<input required type="number" min="2000" max="2100" value={indexForm.godina} onChange={(e) => setIndexForm({ ...indexForm, godina: e.target.value })} /></label>
          <label>Financing *<select value={indexForm.nacinFinansiranja} onChange={(e) => setIndexForm({ ...indexForm, nacinFinansiranja: e.target.value })}><option value="BUDZET">Budget</option><option value="SAMOFINANSIRANJE">Self-financed</option></select></label>
          <label>Valid from *<input required type="date" value={indexForm.vaziOd} onChange={(e) => setIndexForm({ ...indexForm, vaziOd: e.target.value })} /></label>
          {actionError && <p className="error">{actionError}</p>}<button type="submit">Create index</button>
        </form>
      </Modal>}
      {enrollOpen && <Modal title="Enroll study year" onClose={() => setEnrollOpen(false)}>
        <form className="formGrid" onSubmit={enrollStudyYear}>
          <label>Study year *<select required value={studyYear} onChange={(e) => setStudyYear(e.target.value)}>{[1,2,3,4].map((year) => <option key={year} value={year}>{year}</option>)}</select></label>
          {actionError && <p className="error">{actionError}</p>}<button type="submit">Enroll and assign subjects</button>
        </form>
      </Modal>}
    </section>
  );
}

export function AdminStudentIndexesPage() {
  const parts = window.location.pathname.split('/');
  const id = parts[parts.length - 2];
  const { data, loading, error } = useApi(() => adminApi.studentIndexes(id), [id]);
  if (loading) return <Loading />;
  if (error) return <ErrorMessage message={error} />;
  return <section className="card"><h1>Student indexes</h1><DataTable rows={pageRows(data)} columns={entityColumns} /></section>;
}

export function AdminExamsPage() {
  const periods = useApi(adminApi.examPeriods, []);
  const [periodId, setPeriodId] = useState('');

  useEffect(() => {
    const firstPeriod = asRows(periods.data)[0];
    if (!periodId && firstPeriod?.id !== undefined) setPeriodId(String(firstPeriod.id));
  }, [periodId, periods.data]);

  const exams = useApi(
    () => periodId ? adminApi.examsForPeriod(periodId) : Promise.resolve([]),
    [periodId]
  );

  return (
    <section className="card">
      <h1>Exams</h1>
      {periods.loading && <Loading />}
      {periods.error && <ErrorMessage message={periods.error} />}
      {!periods.loading && !periods.error && (
        <>
          <label>
            Exam period
            <select value={periodId} onChange={(event) => setPeriodId(event.target.value)}>
              {!asRows(periods.data).length && <option value="">No exam periods available</option>}
              {asRows(periods.data).map((period) => (
                <option key={String(period.id)} value={String(period.id)}>
                  {`${pick(period, ['datumPocetka'])} - ${pick(period, ['datumZavrsetka'])}`}
                </option>
              ))}
            </select>
          </label>
          {exams.loading && <Loading />}
          {exams.error && <ErrorMessage message={exams.error} />}
          {!exams.loading && !exams.error && <DataTable rows={asRows(exams.data)} columns={examColumns} empty="No exams in the selected period." />}
        </>
      )}
    </section>
  );
}

export function AdminEntityPage({ title, loader }: { title: string; loader: () => Promise<unknown> }) {
  const [showJson, setShowJson] = useState(false);
  const memoLoader = useMemo(() => loader, [loader]);
  const { data, loading, error } = useApi(memoLoader, [memoLoader]);
  if (loading) return <Loading />;
  if (error) return <ErrorMessage message={error} />;
  return (
    <section className="card">
      <header className="pageHeader"><h1>{title}</h1><button type="button" onClick={() => setShowJson(true)}>Raw data</button></header>
      <DataTable rows={pageRows(data)} columns={entityColumns} />
      {showJson && <Modal title={title} onClose={() => setShowJson(false)}><JsonBlock value={data} /></Modal>}
    </section>
  );
}
