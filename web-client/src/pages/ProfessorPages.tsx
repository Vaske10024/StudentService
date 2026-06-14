import { FormEvent, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { apiErrorMessage } from '../api/client';
import { meApi } from '../api/me';
import { professorApi } from '../api/professor';
import { DataTable } from '../components/DataTable';
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
  { header: 'Date', render: (row: Record<string, unknown>) => pick(row, ['datumOdrzavanja', 'datum']) },
  { header: 'Locked', render: (row: Record<string, unknown>) => pick(row, ['zakljucen', 'locked']) },
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
  const { data, loading, error } = useApi(meApi.professorExams, []);
  if (loading) return <Loading />;
  if (error) return <ErrorMessage message={error} />;
  return <section className="card"><h1>My exams</h1><DataTable rows={asRows(data)} columns={examColumns} /></section>;
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
  const students = useApi(() => subjectId ? professorApi.studentsForSubject(subjectId) : Promise.resolve([]), [subjectId]);
  const definitions = useApi(() => selected?.predmetId && selected?.skolskaGodinaId ? professorApi.preExamDefinitions(String(selected.predmetId), String(selected.skolskaGodinaId)) : Promise.resolve([]), [selected?.predmetId, selected?.skolskaGodinaId]);
  const [studentId, setStudentId] = useState('');
  const [definition, setDefinition] = useState({ vrsta: '', maxPoeni: '10' });
  const details = useApi(() => studentId && selected?.predmetId && selected?.skolskaGodinaId ? professorApi.preExamForStudent(studentId, String(selected.predmetId), String(selected.skolskaGodinaId)) : Promise.resolve([]), [studentId, selected?.predmetId, selected?.skolskaGodinaId]);
  const [error, setError] = useState<string | null>(null);
  async function save(predObId: unknown, points: string) {
    try { await professorApi.upsertPreExam({ studentIndeksId: Number(studentId), predispitnaObavezaId: Number(predObId), poeni: Number(points) }); await details.reload(); }
    catch (err) { setError(apiErrorMessage(err, 'Predispitni poeni nisu sačuvani.')); }
  }
  async function createDefinition(event: FormEvent) {
    event.preventDefault();
    if (!selected?.predmetId || !selected?.skolskaGodinaId) return;
    try {
      await professorApi.createPreExamDefinition({ predmetId: Number(selected.predmetId), skolskaGodinaId: Number(selected.skolskaGodinaId), vrsta: definition.vrsta, maxPoeni: Number(definition.maxPoeni) });
      setDefinition({ vrsta: '', maxPoeni: '10' }); await definitions.reload(); if (studentId) await details.reload();
    } catch (err) { setError(apiErrorMessage(err, 'Definicija predispitne obaveze nije sačuvana.')); }
  }
  return <section className="card"><h1>Predispitne obaveze</h1>{error && <ErrorMessage message={error} />}
    <div className="formGrid"><label>Predmet<select value={subjectId} onChange={(e) => { setSubjectId(e.target.value); setStudentId(''); }}><option value="">Select</option>{asRows(subjects.data).map((item) => <option key={String(item.id)} value={String(item.id)}>{pick(item, ['predmetNaziv'])}</option>)}</select></label>
    <label>Student<select value={studentId} onChange={(e) => setStudentId(e.target.value)}><option value="">Select</option>{asRows(students.data).map((item) => <option key={String(item.id)} value={String(item.id)}>{`${pick(item, ['studProgramOznaka'])} ${pick(item, ['broj'])}/${pick(item, ['godina'])} - ${pick(item, ['ime'])} ${pick(item, ['prezime'])}`}</option>)}</select></label></div>
    <form className="formGrid" onSubmit={createDefinition}><label>Nova obaveza<input required placeholder="Kolokvijum, projekat..." value={definition.vrsta} onChange={(e) => setDefinition({ ...definition, vrsta: e.target.value })} /></label><label>Maksimalni poeni<input required type="number" min="0" max="30" value={definition.maxPoeni} onChange={(e) => setDefinition({ ...definition, maxPoeni: e.target.value })} /></label><button disabled={!selected}>Dodaj obavezu</button></form>
    <p className="muted">{asRows(definitions.data).length ? `${asRows(definitions.data).length} definisanih obaveza.` : 'Nema definisanih predispitnih obaveza za predmet i školsku godinu.'}</p>
    {studentId && <DataTable rows={asRows(details.data)} columns={[
      { header: 'Obaveza', render: (row) => pick(row, ['vrsta']) },
      { header: 'Maksimum', render: (row) => pick(row, ['max']) },
      { header: 'Poeni', render: (row) => <PreExamEditor row={row} onSave={save} /> }
    ]} />}
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

function PreExamEditor({ row, onSave }: { row: Record<string, unknown>; onSave: (id: unknown, points: string) => Promise<void> }) {
  const [points, setPoints] = useState(String(row.osvojeni ?? 0));
  return <form className="inlineForm" onSubmit={(event) => { event.preventDefault(); void onSave(row.predObId, points); }}><input type="number" min="0" max={Number(row.max)} value={points} onChange={(e) => setPoints(e.target.value)} /><button>Sačuvaj</button></form>;
}
