import React from 'react';

export function Modal({ title, children, onClose }: { title: string; children: React.ReactNode; onClose: () => void }) {
  return (
    <div className="modalBackdrop" role="presentation" onClick={onClose}>
      <section className="modal" role="dialog" aria-modal="true" aria-label={title} onClick={(e) => e.stopPropagation()}>
        <header><h2>{title}</h2><button type="button" onClick={onClose}>×</button></header>
        {children}
      </section>
    </div>
  );
}
