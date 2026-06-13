import { Link, useParams } from 'react-router-dom';
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
  const { data, loading, error } = useApi(() => professorApi.resultsForExam(id ?? ''), [id]);
  if (loading) return <Loading />;
  if (error) return <ErrorMessage message={error} />;
  return <section className="card"><h1>Exam results</h1><DataTable rows={asRows(data)} columns={resultColumns} /></section>;
}

export function ProfessorPredispitPage() {
  return (
    <section className="card">
      <h1>Predispit</h1>
      <p className="muted">Use the subject and exam screens to open assigned students and maintain pre-exam obligations. Backend ownership checks prevent updates outside your teaching assignments.</p>
    </section>
  );
}
