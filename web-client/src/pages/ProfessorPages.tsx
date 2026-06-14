import { FormEvent, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { apiErrorMessage } from '../api/client';
import { meApi } from '../api/me';
import { professorApi } from '../api/professor';
import { DataTable } from '../components/DataTable';
import { Modal } from '../components/Modal';
import { ErrorMessage, Loading } from '../components/Status';
import { useApi } from '../hooks/useApi';
import { asRows, JsonBlock, pick } from './dataHelpers';

const subjectColumns = [
  { header: 'Assignment', render: (row: Record<string, unknown>) => pick(row, ['id', 'drziPredmetId']) },
  { header: 'Subject', render: (row: Record<string, unknown>) => pick(row, ['predmetNaziv', 'naziv', 'name']) },
  { header: 'Year', render: (row: Record<string, unknown>) => pick(row, ['skolskaGodinaNaziv', 'skolskaGodinaId', 'godina']) },
  { header: 'Students', render: (row: Record<string, unknown>) => <Link to={`/professor/subjects/${String(row.id ?? row.drziPredmetId)}/students`}>Open</Link> }
];

const examColumns = [
  { header: 'Exam', render: (row: Record<string, unknown>) => pick(row, ['id', 'ispitId']) },
  { header: 'Subject', render: (row: Record<string, unknown>) => pick(row, ['predmetNaziv', 'nazivPredmeta', 'naziv']) },
  { header: 'Exam period', render: (row: Record<string, unknown>) => `${pick(row, ['rokDatumPocetka'])} - ${pick(row, ['rokDatumZavrsetka'])}` },
  { header: 'Date', render: (row: Record<string, unknown>) => pick(row, ['datumOdrzavanja', 'datum']) },
  { header: 'Time', render: (row: Record<string, unknown>) => pick(row, ['vremePocetka', 'vreme']) },
  { header: 'Locked', render: (row: Record<string, unknown>) => pick(row, ['zakljucen', 'locked']) },
  { header: 'Registered students', render: (row: Record<string, unknown>) => <Link to={`/professor/exams/${String(row.id ?? row.ispitId)}/registered`}>Open</Link> },
  { header: 'Results', render: (row: Record<string, unknown>) => <Link to={`/professor/exams/${String(row.id ?? row.ispitId)}/results`}>Open</Link> }
];

const studentColumns = [
  { header: 'Index', render: (row: Record<string, unknown>) => `${pick(row, ['studProgramOznaka', 'program'])}-${pick(row, ['broj'])}/${pick(row, ['godina'])}` },
  { header: 'Student', render: (row: Record<string, unknown>) => `${pick(row, ['ime', 'firstName'])} ${pick(row, ['prezime', 'lastName'])}` },
  { header: 'Student ID', render: (row: Record<string, unknown>) => pick(row, ['studentId', 'id']) }
];

const resultColumns = [
  {
    header: 'Student',
    render: (row: Record<string, unknown>) => {
      const student = row.student as Record<string, unknown> | undefined;
      return student ? `${pick(student, ['ime'])} ${pick(student, ['prezime'])}` : pick(row, ['studentImePrezime', 'imePrezime']);
    }
  },
  {
    header: 'Index',
    render: (row: Record<string, unknown>) => {
      const student = row.student as Record<string, unknown> | undefined;
      return student ? `${pick(student, ['studProgramOznaka'])} ${pick(student, ['broj'])}/${pick(student, ['godina'])}` : '-';
    }
  },
  { header: 'Total points', render: (row: Record<string, unknown>) => pick(row, ['ukupniPoeni']) },
  { header: 'Grade', render: (row: Record<string, unknown>) => pick(row, ['ocena']) },
  { header: 'Attended', render: (row: Record<string, unknown>) => pick(row, ['izasao']) },
  { header: 'Cancelled', render: (row: Record<string, unknown>) => pick(row, ['ponisteno']) }
];

export function ProfessorDashboardPage() {
  const { data, loading, error } = useApi(meApi.professorDashboard, []);
  if (loading) return <Loading />;
  if (error) return <ErrorMessage message={error} />;
  return (
    <section>
      <h1>Professor dashboard</h1>
      <div className="gridCards">
        <article className="card"><h2>Account</h2><JsonBlock value={data?.user ?? null} /></article>
        <article className="card"><h2>Subjects</h2><p className="metric">{asRows(data?.subjects).length}</p></article>
        <article className="card"><h2>Exams</h2><p className="metric">{asRows(data?.exams).length}</p></article>
      </div>
      <section className="card"><h2>My subjects</h2><DataTable rows={asRows(data?.subjects)} columns={subjectColumns} /></section>
      <section className="card"><h2>My exams</h2><DataTable rows={asRows(data?.exams)} columns={examColumns} /></section>
    </section>
  );
}

export function ProfessorSubjectsPage() {
  const { data, loading, error } = useApi(meApi.professorSubjects, []);
  if (loading) return <Loading />;
  if (error) return <ErrorMessage message={error} />;
  return <section className="card"><h1>My subjects</h1><DataTable rows={asRows(data)} columns={subjectColumns} /></section>;
}

export function ProfessorSubjectStudentsPage() {
  const { id } = useParams();
  const { data, loading, error } = useApi(() => professorApi.studentsForSubject(id ?? ''), [id]);
  if (loading) return <Loading />;
  if (error) return <ErrorMessage message={error} />;
  return <section className="card"><h1>Subject students</h1><DataTable rows={asRows(data)} columns={studentColumns} /></section>;
}

export function ProfessorExamsPage() {
  const exams = useApi(meApi.professorExams, []);
  const [editing, setEditing] = useState<Record<string, unknown> | null>(null);
  const [form, setForm] = useState({ datum: '', vreme: '' });
  const [message, setMessage] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);

  function edit(row: Record<string, unknown>) {
    setEditing(row);
    setForm({
      datum: String(row.datumOdrzavanja ?? ''),
      vreme: String(row.vremePocetka ?? '').slice(0, 5)
    });
    setMessage(null);
    setActionError(null);
  }

  async function save(event: FormEvent) {
    event.preventDefault();
    if (!editing?.id) return;
    setActionError(null);
    try {
      await professorApi.updateExamTime(String(editing.id), form);
      setEditing(null);
      setMessage('Termin ispita je izmenjen.');
      await exams.reload();
    } catch (error) {
      setActionError(apiErrorMessage(error, 'Izmena termina ispita nije uspela.'));
    }
  }

  if (exams.loading) return <Loading />;
  if (exams.error) return <ErrorMessage message={exams.error} />;
  const columns = [
    ...examColumns,
    { header: 'Action', render: (row: Record<string, unknown>) => <button type="button" className="secondaryButton" disabled={row.zakljucen === true} onClick={() => edit(row)}>Edit date and time</button> }
  ];
  return <section className="card"><h1>My exams</h1>
    {message && <p className="success">{message}</p>}{actionError && <ErrorMessage message={actionError} />}
    <DataTable rows={asRows(exams.data)} columns={columns} />
    {editing && <Modal title="Edit exam date and time" onClose={() => setEditing(null)}>
      <form className="formGrid" onSubmit={save}>
        <label>Exam date *<input required type="date" min={String(editing.rokDatumPocetka ?? '')} max={String(editing.rokDatumZavrsetka ?? '')} value={form.datum} onChange={(e) => setForm({ ...form, datum: e.target.value })} /></label>
        <label>Start time *<input required type="time" value={form.vreme} onChange={(e) => setForm({ ...form, vreme: e.target.value })} /></label>
        <button type="submit">Save changes</button>
      </form>
    </Modal>}
  </section>;
}

export function ProfessorExamRegisteredStudentsPage() {
  const { id } = useParams();
  const { data, loading, error } = useApi(() => professorApi.registeredStudentsForExam(id ?? ''), [id]);
  if (loading) return <Loading />;
  if (error) return <ErrorMessage message={error} />;
  return <section className="card">
    <header className="pageHeader"><h1>Registered students</h1><Link className="buttonLink secondaryButton" to={`/professor/exams/${id ?? ''}/results`}>Open results</Link></header>
    <DataTable rows={asRows(data)} columns={studentColumns} empty="No students are registered for this exam." />
  </section>;
}

export function ProfessorExamResultsPage() {
  const { id } = useParams();
  const result = useApi(() => professorApi.registrationsForExam(id ?? ''), [id]);
  const [saving, setSaving] = useState<number | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  async function lock() {
    if (!id || !window.confirm('Zaključati rezultate ispita? Posle toga profesor više ne može da ih menja.')) return;
    try { await professorApi.lockExam(id); setMessage('Rezultati su zaključani.'); }
    catch (error) { setActionError(apiErrorMessage(error, 'Zaključavanje nije uspelo.')); }
  }
  if (result.loading) return <Loading />;
  if (result.error) return <ErrorMessage message={result.error} />;
  const columns = [
    ...resultColumns.slice(0, 2),
    { header: 'Predispitni', render: (row: Record<string, unknown>) => pick(row, ['predispitniPoeni']) },
    { header: 'Rezultat', render: (row: Record<string, unknown>) => <ResultEditor row={row} disabled={saving !== null} onSave={async (payload) => {
      setSaving(Number(row.id)); setActionError(null); setMessage(null);
      try { await professorApi.updateResult({ prijavaId: Number(row.id), ...payload }); setMessage('Rezultat je sačuvan, a ocena obračunata.'); await result.reload(); }
      catch (error) { setActionError(apiErrorMessage(error, 'Čuvanje rezultata nije uspelo.')); } finally { setSaving(null); }
    }} /> }
  ];
  return <section className="card"><header className="pageHeader"><h1>Exam results</h1><button type="button" onClick={() => void lock()}>Zaključaj rezultate</button></header>{message && <p className="success">{message}</p>}{actionError && <ErrorMessage message={actionError} />}<DataTable rows={asRows(result.data)} columns={columns} /></section>;
}

export function ProfessorPredispitPage() {
  const subjects = useApi(meApi.professorSubjects, []);
  const [subjectId, setSubjectId] = useState('');
  useEffect(() => { const first = asRows(subjects.data)[0]; if (!subjectId && first?.id) setSubjectId(String(first.id)); }, [subjectId, subjects.data]);
  const selected = asRows(subjects.data).find((item) => String(item.id) === subjectId);
  const gradebooks = useApi(() => subjectId ? professorApi.preExamGradebook(subjectId) : Promise.resolve([]), [subjectId]);
  const [definitionId, setDefinitionId] = useState('');
  const [definition, setDefinition] = useState({ vrsta: '', maxPoeni: '10' });
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const books = asRows(gradebooks.data);
  useEffect(() => {
    if (!books.length) setDefinitionId('');
    else if (!books.some((item) => String(item.predispitnaObavezaId) === definitionId)) setDefinitionId(String(books[0].predispitnaObavezaId));
  }, [definitionId, gradebooks.data]);
  const activeBook = books.find((item) => String(item.predispitnaObavezaId) === definitionId);

  async function save(studentIndeksId: unknown, points: string) {
    if (!activeBook) return;
    setError(null); setMessage(null);
    try {
      await professorApi.upsertPreExam({ studentIndeksId: Number(studentIndeksId), predispitnaObavezaId: Number(activeBook.predispitnaObavezaId), poeni: Number(points) });
      setMessage('Poeni su sačuvani.');
      await gradebooks.reload();
    } catch (err) { setError(apiErrorMessage(err, 'Predispitni poeni nisu sačuvani.')); }
  }
  async function createDefinition(event: FormEvent) {
    event.preventDefault();
    if (!selected?.predmetId || !selected?.skolskaGodinaId) return;
    setError(null); setMessage(null);
    try {
      const id = await professorApi.createPreExamDefinition({ predmetId: Number(selected.predmetId), skolskaGodinaId: Number(selected.skolskaGodinaId), vrsta: definition.vrsta, maxPoeni: Number(definition.maxPoeni) });
      setDefinition({ vrsta: '', maxPoeni: '10' }); setDefinitionId(String(id));
      setMessage('Predispitna obaveza je dodata. Sada možete uneti poene studentima.');
      await gradebooks.reload();
    } catch (err) { setError(apiErrorMessage(err, 'Definicija predispitne obaveze nije sačuvana.')); }
  }
  if (subjects.loading) return <Loading />;
  return <section className="stack">
    <section className="card"><h1>Predispitne obaveze</h1><p className="muted">Prvo definišite obavezu, zatim unesite poene studentima koji slušaju predmet.</p>
      {error && <ErrorMessage message={error} />}{message && <p className="success">{message}</p>}
      <label>Predmet i školska godina<select value={subjectId} onChange={(e) => { setSubjectId(e.target.value); setDefinitionId(''); setError(null); setMessage(null); }}><option value="">Izaberite predmet</option>{asRows(subjects.data).map((item) => <option key={String(item.id)} value={String(item.id)}>{`${pick(item, ['predmetNaziv'])} · ${pick(item, ['skolskaGodinaNaziv'])} · ${pick(item, ['uloga'])}`}</option>)}</select></label>
    </section>
    {selected && <section className="card"><h2>1. Dodaj predispitnu obavezu</h2><p className="muted">Ukupan maksimum svih predispitnih obaveza za predmet može biti najviše 30 poena.</p>
      <form className="formGrid" onSubmit={createDefinition}><label>Naziv obaveze<input required placeholder="Kolokvijum, projekat, aktivnost..." value={definition.vrsta} onChange={(e) => setDefinition({ ...definition, vrsta: e.target.value })} /></label><label>Maksimalni poeni<input required type="number" min="1" max="30" value={definition.maxPoeni} onChange={(e) => setDefinition({ ...definition, maxPoeni: e.target.value })} /></label><button type="submit">Dodaj obavezu</button></form>
    </section>}
    {selected && <section className="card"><h2>2. Unesi poene studentima</h2>
      {gradebooks.loading ? <Loading /> : gradebooks.error ? <ErrorMessage message={gradebooks.error} /> : !books.length ? <p className="muted">Još nema predispitnih obaveza. Dodajte prvu obavezu iznad.</p> : <>
        <div className="preExamTabs">{books.map((book) => <button type="button" className={String(book.predispitnaObavezaId) === definitionId ? 'active' : 'secondaryButton'} key={String(book.predispitnaObavezaId)} onClick={() => setDefinitionId(String(book.predispitnaObavezaId))}>{`${pick(book, ['vrsta'])} (${pick(book, ['maxPoeni'])} poena)`}</button>)}</div>
        {activeBook && <DataTable rows={asRows(activeBook.studenti)} columns={[
          { header: 'Indeks', render: (row) => `${pick(row, ['studProgramOznaka'])}-${pick(row, ['broj'])}/${pick(row, ['godina'])}` },
          { header: 'Student', render: (row) => `${pick(row, ['ime'])} ${pick(row, ['prezime'])}` },
          { header: `Poeni / ${String(activeBook.maxPoeni)}`, render: (row) => <StudentPreExamScoreEditor key={`${String(activeBook.predispitnaObavezaId)}-${String(row.studentIndeksId)}-${String(row.poeni)}`} row={row} max={Number(activeBook.maxPoeni)} onSave={save} /> }
        ]} empty="Nema studenata koji slušaju ovaj predmet." />}
      </>}
    </section>}
  </section>;
}

function ResultEditor({ row, disabled, onSave }: { row: Record<string, unknown>; disabled: boolean; onSave: (payload: Record<string, unknown>) => Promise<void> }) {
  const [points, setPoints] = useState(String(row.ispitniPoeni ?? 0));
  const [attended, setAttended] = useState(row.izasao === true);
  const [note, setNote] = useState(String(row.napomena ?? ''));
  return <form className="inlineForm" onSubmit={(event: FormEvent) => { event.preventDefault(); void onSave({ brojOsvojenihPoena: Number(points), izasao: attended, napomena: note }); }}>
    <input aria-label="Exam points" type="number" min="0" max="100" value={points} onChange={(e) => setPoints(e.target.value)} />
    <label className="checkLabel"><input type="checkbox" checked={attended} onChange={(e) => setAttended(e.target.checked)} /> Izašao</label>
    <input aria-label="Note" placeholder="Napomena" value={note} onChange={(e) => setNote(e.target.value)} /><button disabled={disabled}>Sačuvaj</button>
  </form>;
}

function StudentPreExamScoreEditor({ row, max, onSave }: { row: Record<string, unknown>; max: number; onSave: (studentIndeksId: unknown, points: string) => Promise<void> }) {
  const [points, setPoints] = useState(String(row.poeni ?? 0));
  return <form className="scoreEditor" onSubmit={(event) => { event.preventDefault(); void onSave(row.studentIndeksId, points); }}><input aria-label={`Poeni za ${String(row.ime)} ${String(row.prezime)}`} type="number" min="0" max={max} required value={points} onChange={(e) => setPoints(e.target.value)} /><button type="submit">Sačuvaj</button></form>;
}
