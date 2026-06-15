import React from 'react';

export interface Column<T> {
  header: string;
  render: (row: T) => React.ReactNode;
}

export function DataTable<T>({ rows, columns, empty = 'No data.' }: { rows: T[]; columns: Column<T>[]; empty?: string }) {
  if (!rows.length) return <div className="emptyState" role="status"><span aria-hidden="true">i</span><p>{empty}</p></div>;
  return (
    <div className="tableWrap" role="region" aria-label="Data table" tabIndex={0}>
      <table>
        <thead>
          <tr>{columns.map((column) => <th key={column.header}>{column.header}</th>)}</tr>
        </thead>
        <tbody>
          {rows.map((row, rowIndex) => (
            <tr key={rowIndex}>{columns.map((column) => <td key={column.header}>{column.render(row)}</td>)}</tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
