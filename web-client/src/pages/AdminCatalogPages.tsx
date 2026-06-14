import { FormEvent, useState } from 'react';
import { adminApi } from '../api/admin';
import { apiErrorMessage } from '../api/client';
import { DataTable } from '../components/DataTable';
import { Modal } from '../components/Modal';
import { ErrorMessage, Loading } from '../components/Status';
import { useApi } from '../hooks/useApi';
import { asRows, labelValue, pick } from './dataHelpers';

function ProfileDetails({ value }: { value: Record<string, unknown> }) {
  const fields = [
    ['Email', 'email'], ['JMBG', 'jmbg'], ['Middle name', 'srednjeIme'],
    ['Date of birth', 'datumRodjenja'], ['Gender', 'pol'], ['Phone', 'brojTelefona'],
    ['Address', 'adresa']
  ];
  return (
    <div className="profilePanel">
      <div className="profileAvatar">{`${String(value.ime ?? '?').charAt(0)}${String(value.prezime ?? '').charAt(0)}`}</div>
      <div>
        <h2>{`${labelValue(value.ime)} ${labelValue(value.prezime)}`}</h2>
        <p className="muted">{labelValue(value.email)}</p>
      </div>
      <dl className="details profileDetails">
        {fields.map(([label, key]) => <div className="detailItem" key={key}><dt>{label}</dt><dd>{labelValue(value[key])}</dd></div>)}
      </dl>
    </div>
  );
}

export function AdminProfessorsPage() {
  const professors = useApi(adminApi.professors, []);
  const [createOpen, setCreateOpen] = useState(false);
  const [selected, setSelected] = useState<Record<string, unknown> | null>(null);
  const [form, setForm] = useState({ ime: '', prezime: '', srednjeIme: '', email: '', jmbg: '', datumRodjenja: '', pol: '', brojTelefona: '', adresa: '' });
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  function update(field: keyof typeof form, value: string) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function create(event: FormEvent) {
    event.preventDefault();
    setError(null);
    try {
      const payload = Object.fromEntries(Object.entries(form).map(([key, value]) => [key, value.trim() || undefined]));
      const id = await adminApi.createProfessor(payload);
      setMessage(`Professor created with id ${id}.`);
      setCreateOpen(false);
      await professors.reload();
    } catch (err) {
      setError(apiErrorMessage(err, 'Professor creation failed.'));
    }
  }

  async function openProfile(id: unknown) {
    setError(null);
    try {
      setSelected(await adminApi.professor(String(id)));
    } catch (err) {
      setError(apiErrorMessage(err, 'Professor profile could not be loaded.'));
    }
  }

  async function provision(id: unknown) {
    setError(null);
    try {
      const result = await adminApi.provisionProfessor(String(id));
      setMessage(result.accountCreated
        ? `Professor account ${result.username} created. One-time temporary password: ${result.temporaryPassword}`
        : `Professor already has account ${result.username}.`);
    } catch (err) {
      setError(apiErrorMessage(err, 'Professor account provisioning failed.'));
    }
  }

  const columns = [
    { header: 'Professor', render: (row: Record<string, unknown>) => <strong>{`${pick(row, ['ime'])} ${pick(row, ['prezime'])}`}</strong> },
    { header: 'Email', render: (row: Record<string, unknown>) => pick(row, ['email']) },
    { header: 'Actions', render: (row: Record<string, unknown>) => <div className="buttonGroup"><button type="button" className="secondaryButton" onClick={() => void openProfile(row.id)}>Open profile</button><button type="button" onClick={() => void provision(row.id)}>Provision account</button></div> }
  ];

  return (
    <section className="card">
      <header className="pageHeader"><div><h1>Professors</h1><p className="muted">Manage professor records and profiles.</p></div><button type="button" onClick={() => { setError(null); setCreateOpen(true); }}>New professor</button></header>
      {message && <p className="success">{message}</p>}
      {error && <ErrorMessage message={error} />}
      {professors.loading ? <Loading /> : professors.error ? <ErrorMessage message={professors.error} /> : <DataTable rows={asRows(professors.data)} columns={columns} />}
      {createOpen && <Modal title="New professor" onClose={() => setCreateOpen(false)}>
        <form className="formGrid" onSubmit={create}>
          <label>First name *<input required value={form.ime} onChange={(e) => update('ime', e.target.value)} /></label>
          <label>Last name *<input required value={form.prezime} onChange={(e) => update('prezime', e.target.value)} /></label>
          <label>Middle name *<input required value={form.srednjeIme} onChange={(e) => update('srednjeIme', e.target.value)} /></label>
          <label>Email *<input required type="email" value={form.email} onChange={(e) => update('email', e.target.value)} /></label>
          <label>JMBG<input value={form.jmbg} onChange={(e) => update('jmbg', e.target.value)} /></label>
          <label>Date of birth<input type="date" value={form.datumRodjenja} onChange={(e) => update('datumRodjenja', e.target.value)} /></label>
          <label>Gender<select value={form.pol} onChange={(e) => update('pol', e.target.value)}><option value="">Select</option><option value="M">Male</option><option value="Z">Female</option></select></label>
          <label>Phone<input value={form.brojTelefona} onChange={(e) => update('brojTelefona', e.target.value)} /></label>
          <label>Address<input value={form.adresa} onChange={(e) => update('adresa', e.target.value)} /></label>
          {error && <p className="error">{error}</p>}
          <button type="submit">Create professor</button>
        </form>
      </Modal>}
      {selected && <Modal title="Professor profile" onClose={() => setSelected(null)}><ProfileDetails value={selected} /></Modal>}
    </section>
  );
}

export function AdminSubjectsPage() {
  const subjects = useApi(adminApi.subjects, []);
  const assignments = useApi(adminApi.assignments, []);
  const realizations = useApi(() => adminApi.realizations(), []);
  const programs = useApi(adminApi.programs, []);
  const professors = useApi(adminApi.professors, []);
  const schoolYears = useApi(adminApi.schoolYears, []);
  const [subjectOpen, setSubjectOpen] = useState(false);
  const [assignmentOpen, setAssignmentOpen] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [subject, setSubject] = useState({ sifra: '', naziv: '', opis: '', espb: '6', programId: '', godinaStudija: '1', semestarUGodini: '1', obavezan: true });
  const [assignment, setAssignment] = useState({ realizacijaPredmetaId: '', nastavnikId: '', uloga: 'NOSILAC' });
  const [generation, setGeneration] = useState({ programId: '', skolskaGodinaId: '' });

  async function createSubject(event: FormEvent) {
    event.preventDefault();
    setError(null);
    try {
      const id = await adminApi.createSubject({ ...subject, espb: Number(subject.espb), programId: subject.programId ? Number(subject.programId) : undefined, godinaStudija: subject.programId ? Number(subject.godinaStudija) : undefined, semestarUGodini: subject.programId ? Number(subject.semestarUGodini) : undefined });
      setMessage(`Subject created with id ${id}.`);
      setSubjectOpen(false);
      await subjects.reload();
    } catch (err) { setError(apiErrorMessage(err, 'Subject creation failed.')); }
  }

  async function createAssignment(event: FormEvent) {
    event.preventDefault();
    setError(null);
    try {
      const id = await adminApi.createAssignment({
        realizacijaPredmetaId: Number(assignment.realizacijaPredmetaId),
        nastavnikId: Number(assignment.nastavnikId),
        uloga: assignment.uloga
      });
      setMessage(`Teaching assignment created with id ${id}.`);
      setAssignmentOpen(false);
      await assignments.reload();
    } catch (err) { setError(apiErrorMessage(err, 'Teaching assignment creation failed.')); }
  }

  async function generateRealizations(event: FormEvent) {
    event.preventDefault();
    setError(null);
    try {
      const generated = await adminApi.generateRealizations(generation.programId, generation.skolskaGodinaId || undefined);
      setMessage(`${generated.length} subject realizations are ready.`);
      await realizations.reload();
    } catch (err) { setError(apiErrorMessage(err, 'Realization generation failed.')); }
  }

  const subjectColumns = [
    { header: 'Code', render: (row: Record<string, unknown>) => pick(row, ['sifra']) },
    { header: 'Subject', render: (row: Record<string, unknown>) => pick(row, ['naziv']) },
    { header: 'ECTS', render: (row: Record<string, unknown>) => pick(row, ['espb']) },
    { header: 'Program', render: (row: Record<string, unknown>) => pick(row, ['studProgramOznaka', 'programOznaka']) }
  ];
  const assignmentColumns = [
    { header: 'Subject', render: (row: Record<string, unknown>) => pick(row, ['predmetNaziv']) },
    { header: 'Professor', render: (row: Record<string, unknown>) => pick(row, ['nastavnikImePrezime']) },
    { header: 'ID', render: (row: Record<string, unknown>) => pick(row, ['id']) }
  ];

  return (
    <section className="stack">
      <article className="card">
        <header className="pageHeader"><div><h1>Subjects</h1><p className="muted">Manage the curriculum, annual realizations, and professor assignments.</p></div><div className="buttonGroup"><button type="button" onClick={() => { setError(null); setSubjectOpen(true); }}>New program subject</button><button type="button" className="secondaryButton" onClick={() => { setError(null); setAssignmentOpen(true); }}>Assign professor</button></div></header>
        {message && <p className="success">{message}</p>}{error && <ErrorMessage message={error} />}
        {subjects.loading ? <Loading /> : <DataTable rows={asRows(subjects.data)} columns={subjectColumns} />}
      </article>
      <article className="card"><h2>Generate annual realizations</h2><form className="formGrid" onSubmit={generateRealizations}><label>Study program *<select required value={generation.programId} onChange={(e) => setGeneration({ ...generation, programId: e.target.value })}><option value="">Select</option>{asRows(programs.data).map((p) => <option key={String(p.id)} value={String(p.id)}>{`${pick(p, ['oznaka'])} - ${pick(p, ['naziv'])}`}</option>)}</select></label><label>School year<select value={generation.skolskaGodinaId} onChange={(e) => setGeneration({ ...generation, skolskaGodinaId: e.target.value })}><option value="">Active school year</option>{asRows(schoolYears.data).map((sg) => <option key={String(sg.id)} value={String(sg.id)}>{`${pick(sg, ['godina'])}${sg.aktivna ? ' (active)' : ''}`}</option>)}</select></label><button type="submit">Generate realizations</button></form></article>
      <article className="card"><h2>Active teaching assignments</h2>{assignments.loading ? <Loading /> : <DataTable rows={asRows(assignments.data)} columns={assignmentColumns} />}</article>
      {subjectOpen && <Modal title="New subject" onClose={() => setSubjectOpen(false)}><form className="formGrid" onSubmit={createSubject}>
        <label>Code *<input required value={subject.sifra} onChange={(e) => setSubject({ ...subject, sifra: e.target.value })} /></label>
        <label>Name *<input required value={subject.naziv} onChange={(e) => setSubject({ ...subject, naziv: e.target.value })} /></label>
        <label>ECTS<input required type="number" min="0" value={subject.espb} onChange={(e) => setSubject({ ...subject, espb: e.target.value })} /></label>
        <label>Study program<select value={subject.programId} onChange={(e) => setSubject({ ...subject, programId: e.target.value })}><option value="">No program</option>{asRows(programs.data).map((p) => <option key={String(p.id)} value={String(p.id)}>{`${pick(p, ['oznaka'])} - ${pick(p, ['naziv'])}`}</option>)}</select></label>
        <label>Study year<select disabled={!subject.programId} value={subject.godinaStudija} onChange={(e) => setSubject({ ...subject, godinaStudija: e.target.value })}>{[1,2,3,4].map((year) => <option key={year} value={year}>{year}</option>)}</select></label>
        <label>Semester<select disabled={!subject.programId} value={subject.semestarUGodini} onChange={(e) => setSubject({ ...subject, semestarUGodini: e.target.value })}><option value="1">1</option><option value="2">2</option></select></label>
        <label>Description<textarea value={subject.opis} onChange={(e) => setSubject({ ...subject, opis: e.target.value })} /></label>
        <label className="checkLabel"><input type="checkbox" checked={subject.obavezan} onChange={(e) => setSubject({ ...subject, obavezan: e.target.checked })} /> Mandatory</label>
        {error && <p className="error">{error}</p>}<button type="submit">Create subject</button>
      </form></Modal>}
      {assignmentOpen && <Modal title="New teaching assignment" onClose={() => setAssignmentOpen(false)}><form className="formGrid" onSubmit={createAssignment}>
        <label>Subject realization *<select required value={assignment.realizacijaPredmetaId} onChange={(e) => setAssignment({ ...assignment, realizacijaPredmetaId: e.target.value })}><option value="">Select</option>{asRows(realizations.data).map((r) => <option key={String(r.id)} value={String(r.id)}>{`${pick(r, ['programOznaka'])} - ${pick(r, ['predmetNaziv'])} (${pick(r, ['skolskaGodina'])})`}</option>)}</select></label>
        <label>Professor *<select required value={assignment.nastavnikId} onChange={(e) => setAssignment({ ...assignment, nastavnikId: e.target.value })}><option value="">Select</option>{asRows(professors.data).map((p) => <option key={String(p.id)} value={String(p.id)}>{`${pick(p, ['ime'])} ${pick(p, ['prezime'])}`}</option>)}</select></label>
        <label>Role<select value={assignment.uloga} onChange={(e) => setAssignment({ ...assignment, uloga: e.target.value })}><option value="NOSILAC">Course lead</option><option value="PREDAVANJA">Lectures</option><option value="VEZBE">Exercises</option><option value="PRAKTIKUM">Practicum</option></select></label>
        {error && <p className="error">{error}</p>}<button type="submit">Create assignment</button>
      </form></Modal>}
    </section>
  );
}

export function AdminProgramsPage() {
  const programs = useApi(adminApi.programs, []);
  const studyTypes = useApi(adminApi.studyTypes, []);
  const [typeEditing, setTypeEditing] = useState<Record<string, unknown> | null>(null);
  const [typeForm, setTypeForm] = useState({ skracenica: '', puniNaziv: '' });
  const [form, setForm] = useState({
    oznaka: '', naziv: '', godinaAkreditacije: String(new Date().getFullYear()), zvanje: '',
    trajanjeGodina: '4', ukupnoEspb: '240', vrstaStudijaId: ''
  });
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function create(event: FormEvent) {
    event.preventDefault();
    setError(null);
    try {
      const id = await adminApi.createProgram({
        ...form,
        godinaAkreditacije: Number(form.godinaAkreditacije),
        trajanjeGodina: Number(form.trajanjeGodina),
        ukupnoEspb: Number(form.ukupnoEspb),
        vrstaStudijaId: Number(form.vrstaStudijaId)
      });
      setMessage(`Study program created with id ${id}.`);
      setForm({ ...form, oznaka: '', naziv: '', zvanje: '' });
      await programs.reload();
    } catch (err) { setError(apiErrorMessage(err, 'Study program creation failed.')); }
  }

  function editType(type?: Record<string, unknown>) {
    setTypeEditing(type ?? {});
    setTypeForm(type ? { skracenica: String(type.skracenica ?? ''), puniNaziv: String(type.puniNaziv ?? '') } : { skracenica: '', puniNaziv: '' });
    setError(null);
  }

  async function saveType(event: FormEvent) {
    event.preventDefault();
    setError(null);
    try {
      if (typeEditing?.id) await adminApi.updateStudyType(String(typeEditing.id), typeForm);
      else await adminApi.createStudyType(typeForm);
      setTypeEditing(null);
      setMessage('Study type saved.');
      await studyTypes.reload();
    } catch (err) { setError(apiErrorMessage(err, 'Study type could not be saved.')); }
  }

  async function deleteType(type: Record<string, unknown>) {
    if (!window.confirm(`Delete study type ${String(type.skracenica ?? '')}?`)) return;
    setError(null);
    try {
      await adminApi.deleteStudyType(String(type.id));
      setMessage('Study type deleted.');
      await studyTypes.reload();
    } catch (err) { setError(apiErrorMessage(err, 'Study type could not be deleted.')); }
  }

  const columns = [
    { header: 'Code', render: (row: Record<string, unknown>) => pick(row, ['oznaka']) },
    { header: 'Program', render: (row: Record<string, unknown>) => pick(row, ['naziv']) },
    { header: 'Accreditation', render: (row: Record<string, unknown>) => pick(row, ['godinaAkreditacije']) },
    { header: 'Duration', render: (row: Record<string, unknown>) => `${pick(row, ['trajanjeGodina'])} years / ${pick(row, ['trajanjeSemestara'])} semesters` },
    { header: 'ECTS', render: (row: Record<string, unknown>) => pick(row, ['ukupnoEspb']) }
  ];
  return <section className="stack">
    <section className="card"><header className="pageHeader"><div><h1>Study types</h1><p className="muted">Manage types before creating study programs.</p></div><button type="button" onClick={() => editType()}>New study type</button></header>
      <DataTable rows={asRows(studyTypes.data)} columns={[
        { header: 'Code', render: (row) => pick(row, ['skracenica']) },
        { header: 'Name', render: (row) => pick(row, ['puniNaziv']) },
        { header: 'Actions', render: (row) => <div className="buttonGroup"><button type="button" className="secondaryButton" onClick={() => editType(row)}>Edit</button><button type="button" className="secondaryButton" onClick={() => void deleteType(row)}>Delete</button></div> }
      ]} empty="No study types. Create one before adding a program." />
    </section>
    <section className="card"><h1>Study programs</h1>{message && <p className="success">{message}</p>}{error && <ErrorMessage message={error} />}
      <DataTable rows={asRows(programs.data)} columns={columns} />
    </section>
    <section className="card"><h2>Create study program</h2><form className="formGrid" onSubmit={create}>
      <label>Code *<input required value={form.oznaka} onChange={(e) => setForm({ ...form, oznaka: e.target.value })} /></label>
      <label>Name *<input required value={form.naziv} onChange={(e) => setForm({ ...form, naziv: e.target.value })} /></label>
      <label>Accreditation year *<input required type="number" value={form.godinaAkreditacije} onChange={(e) => setForm({ ...form, godinaAkreditacije: e.target.value })} /></label>
      <label>Qualification *<input required value={form.zvanje} onChange={(e) => setForm({ ...form, zvanje: e.target.value })} /></label>
      <label>Duration in years *<input required type="number" min="1" max="8" value={form.trajanjeGodina} onChange={(e) => setForm({ ...form, trajanjeGodina: e.target.value })} /></label>
      <label>Total ECTS *<input required type="number" min="1" value={form.ukupnoEspb} onChange={(e) => setForm({ ...form, ukupnoEspb: e.target.value })} /></label>
      <label>Study type *<select required value={form.vrstaStudijaId} onChange={(e) => setForm({ ...form, vrstaStudijaId: e.target.value })}><option value="">Select</option>{asRows(studyTypes.data).map((type) => <option key={String(type.id)} value={String(type.id)}>{`${pick(type, ['skracenica'])} - ${pick(type, ['puniNaziv'])}`}</option>)}</select></label>
      <button type="submit">Create program</button>
    </form></section>
    {typeEditing && <Modal title={typeEditing.id ? 'Edit study type' : 'New study type'} onClose={() => setTypeEditing(null)}><form className="formGrid" onSubmit={saveType}>
      <label>Code *<input required value={typeForm.skracenica} onChange={(e) => setTypeForm({ ...typeForm, skracenica: e.target.value })} /></label>
      <label>Full name *<input required value={typeForm.puniNaziv} onChange={(e) => setTypeForm({ ...typeForm, puniNaziv: e.target.value })} /></label>
      <button type="submit">Save study type</button>
    </form></Modal>}
  </section>;
}

export function AdminSchoolYearsPage() {
  const years = useApi(adminApi.schoolYears, []);
  const [oznaka, setOznaka] = useState('');
  const [aktivna, setAktivna] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function create(event: FormEvent) {
    event.preventDefault();
    setError(null);
    try {
      const id = await adminApi.createSchoolYear({ oznaka, aktivna });
      setMessage(`School year created with id ${id}.`);
      setOznaka('');
      await years.reload();
    } catch (err) { setError(apiErrorMessage(err, 'School year creation failed.')); }
  }

  async function activate(id: unknown) {
    setError(null);
    try {
      await adminApi.activateSchoolYear(String(id));
      setMessage('Active school year changed.');
      await years.reload();
    } catch (err) { setError(apiErrorMessage(err, 'School year activation failed.')); }
  }

  const columns = [
    { header: 'School year', render: (row: Record<string, unknown>) => pick(row, ['godina']) },
    { header: 'Status', render: (row: Record<string, unknown>) => row.aktivna ? 'Active' : 'Inactive' },
    { header: 'Action', render: (row: Record<string, unknown>) => <button type="button" disabled={row.aktivna === true} onClick={() => void activate(row.id)}>Activate</button> }
  ];
  return <section className="stack">
    <section className="card"><h1>School years</h1>{message && <p className="success">{message}</p>}{error && <ErrorMessage message={error} />}<DataTable rows={asRows(years.data)} columns={columns} /></section>
    <section className="card"><h2>Create school year</h2><form className="formGrid" onSubmit={create}><label>Label *<input required placeholder="2026/2027" value={oznaka} onChange={(e) => setOznaka(e.target.value)} /></label><label className="checkLabel"><input type="checkbox" checked={aktivna} onChange={(e) => setAktivna(e.target.checked)} /> Activate immediately</label><button type="submit">Create school year</button></form></section>
  </section>;
}
