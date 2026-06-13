import React from 'react';

export interface Column<T> {
  header: string;
  render: (row: T) => React.ReactNode;
}

export function DataTable<T>({ rows, columns, empty = 'No data.' }: { rows: T[]; columns: Column<T>[]; empty?: string }) {
  if (!rows.length) return <p className="muted">{empty}</p>;
  return (
    <div className="tableWrap">
      <table>
        <thead>
          <tr>{columns.map((c) => <th key={c.header}>{c.header}</th>)}</tr>
        </thead>
        <tbody>
          {rows.map((row, idx) => (
            <tr key={idx}>{columns.map((c) => <td key={c.header}>{c.render(row)}</td>)}</tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
