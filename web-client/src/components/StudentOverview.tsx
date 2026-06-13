import type { StudentDashboard } from '../api/types';
import { asRows, labelValue, pick } from '../pages/dataHelpers';
import { DataTable } from './DataTable';

const indexColumns = [
  { header: 'Index', render: (row: Record<string, unknown>) => `${pick(row, ['studProgramOznaka'])} ${pick(row, ['broj'])}/${pick(row, ['godina'])}` },
  { header: 'Study program', render: (row: Record<string, unknown>) => pick(record(row.studijskiProgram), ['naziv', 'oznaka']) },
  { header: 'Financing', render: (row: Record<string, unknown>) => pick(row, ['nacinFinansiranja']) },
  { header: 'ECTS', render: (row: Record<string, unknown>) => pick(row, ['ostvarenoEspb']) },
  { header: 'Active', render: (row: Record<string, unknown>) => pick(row, ['aktivan']) }
];

const subjectColumns = [
  { header: 'Code', render: (row: Record<string, unknown>) => pick(row, ['code']) },
  { header: 'Subject', render: (row: Record<string, unknown>) => pick(row, ['name']) },
  { header: 'ECTS', render: (row: Record<string, unknown>) => pick(row, ['ects']) },
  { header: 'Semester', render: (row: Record<string, unknown>) => pick(row, ['semester']) },
  { header: 'Instructors', render: (row: Record<string, unknown>) => asRows(row.instructors).map((item) => `${String(item.nastavnikImePrezime ?? '')} (${String(item.uloga ?? '').toLowerCase()})`).join(', ') || 'Not assigned' }
];

const registrationColumns = [
  { header: 'Registration', render: (row: Record<string, unknown>) => pick(row, ['id']) },
  { header: 'Exam', render: (row: Record<string, unknown>) => pick(row, ['ispitId']) },
  { header: 'Pre-exam points', render: (row: Record<string, unknown>) => pick(row, ['predispitniPoeni']) },
  { header: 'Exam points', render: (row: Record<string, unknown>) => pick(row, ['ispitniPoeni']) },
  { header: 'Total', render: (row: Record<string, unknown>) => pick(row, ['ukupnoPoena']) },
  { header: 'Grade', render: (row: Record<string, unknown>) => pick(row, ['ocena']) },
  { header: 'Cancelled', render: (row: Record<string, unknown>) => pick(row, ['ponisteno']) }
];

const studyColumns = [
  { header: 'Type', render: (row: Record<string, unknown>) => pick(row, ['tip']) },
  { header: 'Study year', render: (row: Record<string, unknown>) => pick(row, ['godina', 'upisujeGodinu']) },
  { header: 'Date', render: (row: Record<string, unknown>) => pick(row, ['datum']) },
  { header: 'Note', render: (row: Record<string, unknown>) => pick(row, ['napomena']) }
];

const paymentColumns = [
  { header: 'Date', render: (row: Record<string, unknown>) => pick(row, ['datum']) },
  { header: 'Amount RSD', render: (row: Record<string, unknown>) => pick(row, ['iznosRsd']) },
  { header: 'EUR rate', render: (row: Record<string, unknown>) => pick(row, ['srednjiKursEur']) },
  { header: 'Fallback rate', render: (row: Record<string, unknown>) => pick(row, ['fallbackKurs']) }
];

const statusHistoryColumns = [
  { header: 'From', render: (row: Record<string, unknown>) => pick(row, ['oldStatus']) },
  { header: 'To', render: (row: Record<string, unknown>) => pick(row, ['newStatus']) },
  { header: 'Valid from', render: (row: Record<string, unknown>) => pick(row, ['validFrom']) },
  { header: 'Valid to', render: (row: Record<string, unknown>) => pick(row, ['validTo']) },
  { header: 'Reason', render: (row: Record<string, unknown>) => pick(row, ['reason']) }
];

function record(value: unknown): Record<string, unknown> {
  return value && typeof value === 'object' ? value as Record<string, unknown> : {};
}

export function DetailList({ items }: { items: Array<[string, unknown]> }) {
  return (
    <dl className="details">
      {items.map(([label, value]) => (
        <div className="detailItem" key={label}>
          <dt>{label}</dt>
          <dd>{labelValue(value)}</dd>
        </div>
      ))}
    </dl>
  );
}

export function StudentPersonalDetails({ student }: { student: unknown }) {
  const value = record(student);
  return (
    <DetailList items={[
      ['First name', value.ime],
      ['Last name', value.prezime],
      ['Middle name', value.srednjeIme],
      ['Date of birth', value.datumRodjenja],
      ['Gender', value.pol],
      ['Personal ID', value.jmbg],
      ['Faculty email', value.emailFakultetski],
      ['Private email', value.emailPrivatni],
      ['Mobile phone', value.brojTelefonaMobilni],
      ['Landline phone', value.brojTelefonaFiksni],
      ['Citizenship', value.drzavljanstvo],
      ['Place of birth', value.mestoRodjenja],
      ['Address', value.adresaStanovanja ?? value.adresa],
      ['Residence', value.mestoStanovanja ?? value.mestoPrebivalista]
    ]} />
  );
}

export function StudentIndexesTable({ indexes }: { indexes: unknown }) {
  return <DataTable rows={asRows(indexes)} columns={indexColumns} empty="No indexes available." />;
}

export function StudentSubjectsTable({ subjects }: { subjects: unknown }) {
  return <DataTable rows={asRows(subjects)} columns={subjectColumns} empty="No current subjects." />;
}

export function StudentStudyHistory({ dashboard }: { dashboard: StudentDashboard | null }) {
  return (
    <div className="stack">
      <section>
        <h3>Enrollments</h3>
        <DataTable rows={asRows(dashboard?.studyEnrollments)} columns={studyColumns} empty="No enrollments." />
      </section>
      <section>
        <h3>Renewals</h3>
        <DataTable rows={asRows(dashboard?.renewals)} columns={studyColumns} empty="No renewals." />
      </section>
    </div>
  );
}

export function StudentRegistrations({ dashboard }: { dashboard: StudentDashboard | null }) {
  return (
    <div className="stack">
      <section>
        <h3>Active registrations</h3>
        <DataTable rows={asRows(dashboard?.activeExamRegistrations)} columns={registrationColumns} empty="No active exam registrations." />
      </section>
      <section>
        <h3>Previous attempts</h3>
        <DataTable rows={asRows(dashboard?.previousExamAttempts)} columns={registrationColumns} empty="No previous exam attempts." />
      </section>
    </div>
  );
}

export function StudentFinance({ dashboard }: { dashboard: StudentDashboard | null }) {
  const balance = record(dashboard?.balance);
  return (
    <div className="stack">
      <section>
        <h3>Balance</h3>
        <DetailList items={[
          ['Total RSD', balance.ukupnoRsd],
          ['Total EUR', balance.ukupnoEur],
          ['Remaining RSD', balance.preostaloRsd],
          ['Remaining EUR', balance.preostaloEur],
          ['Current EUR rate', balance.currentEurRate]
        ]} />
      </section>
      <section>
        <h3>Payments</h3>
        <DataTable rows={asRows(dashboard?.payments)} columns={paymentColumns} empty="No payments." />
      </section>
    </div>
  );
}

export function StudentLifecycle({ dashboard }: { dashboard: StudentDashboard | null }) {
  const status = record(dashboard?.status);
  return (
    <div className="stack">
      <DetailList items={[
        ['Current status', status.status],
        ['Reason', status.reason],
        ['Activated at', status.activatedAt],
        ['Deactivated at', status.deactivatedAt]
      ]} />
      <section>
        <h3>Status history</h3>
        <DataTable rows={asRows(dashboard?.statusHistory)} columns={statusHistoryColumns} empty="No status changes." />
      </section>
    </div>
  );
}

export function StudentOverview({ dashboard }: { dashboard: StudentDashboard }) {
  return (
    <div className="stack">
      <section className="card">
        <h2>Active study record</h2>
        <DetailList items={[
          ['Index', `${pick(record(dashboard.activeIndex), ['studProgramOznaka'])} ${pick(record(dashboard.activeIndex), ['broj'])}/${pick(record(dashboard.activeIndex), ['godina'])}`],
          ['Study program', pick(record(record(dashboard.activeIndex).studijskiProgram), ['naziv'])],
          ['Financing', pick(record(dashboard.activeIndex), ['nacinFinansiranja'])],
          ['Valid from', pick(record(dashboard.activeIndex), ['vaziOd'])],
          ['School year', pick(record(dashboard.schoolYear), ['godina'])]
        ]} />
      </section>
      <section className="card">
        <h2>Personal details</h2>
        <StudentPersonalDetails student={dashboard.student} />
      </section>
      <section className="card">
        <h2>Academic status</h2>
        <StudentLifecycle dashboard={dashboard} />
      </section>
      <section className="card">
        <h2>Indexes</h2>
        <StudentIndexesTable indexes={dashboard.allIndexes} />
      </section>
      <section className="card">
        <h2>Current subjects</h2>
        <StudentSubjectsTable subjects={dashboard.currentSubjects} />
      </section>
      <section className="card">
        <h2>Exam registrations</h2>
        <StudentRegistrations dashboard={dashboard} />
      </section>
      <section className="card">
        <h2>Finances</h2>
        <StudentFinance dashboard={dashboard} />
      </section>
    </div>
  );
}
