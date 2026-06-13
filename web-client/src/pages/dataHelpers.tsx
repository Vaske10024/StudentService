import React from 'react';

export function labelValue(value: unknown): string {
  if (value === null || value === undefined || value === '') return '—';
  if (typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean') return String(value);
  if (Array.isArray(value)) return `${value.length} item${value.length === 1 ? '' : 's'}`;
  if (typeof value === 'object') {
    const obj = value as Record<string, unknown>;
    return String(obj.naziv ?? obj.imePrezime ?? obj.name ?? obj.id ?? JSON.stringify(obj));
  }
  return String(value);
}

export function pick(row: unknown, keys: string[]): React.ReactNode {
  const obj = row as Record<string, unknown> | null;
  if (!obj) return '—';
  for (const key of keys) {
    if (obj[key] !== undefined && obj[key] !== null && obj[key] !== '') return labelValue(obj[key]);
  }
  return '—';
}

export function asRows(value: unknown): Record<string, unknown>[] {
  return Array.isArray(value) ? value.map((item) => item as Record<string, unknown>) : [];
}

export function JsonBlock({ value }: { value: unknown }) {
  return <pre className="jsonBlock">{JSON.stringify(value, null, 2)}</pre>;
}
