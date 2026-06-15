import React, { useEffect, useRef } from 'react';

export function Modal({ title, children, onClose }: { title: string; children: React.ReactNode; onClose: () => void }) {
  const modalRef = useRef<HTMLElement>(null);
  const onCloseRef = useRef(onClose);
  onCloseRef.current = onClose;

  useEffect(() => {
    const previousFocus = document.activeElement as HTMLElement | null;
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') onCloseRef.current();
    };
    document.addEventListener('keydown', onKeyDown);
    document.body.classList.add('modalOpen');
    modalRef.current?.focus();
    return () => {
      document.removeEventListener('keydown', onKeyDown);
      document.body.classList.remove('modalOpen');
      previousFocus?.focus();
    };
  }, []);

  return (
    <div className="modalBackdrop" role="presentation" onClick={onClose}>
      <section ref={modalRef} className="modal" role="dialog" aria-modal="true" aria-labelledby="modal-title" tabIndex={-1} onClick={(event) => event.stopPropagation()}>
        <header>
          <div><p className="eyebrow">Dialog</p><h2 id="modal-title">{title}</h2></div>
          <button type="button" className="modalClose" aria-label="Close dialog" onClick={onClose}>×</button>
        </header>
        <div className="modalBody">{children}</div>
      </section>
    </div>
  );
}
