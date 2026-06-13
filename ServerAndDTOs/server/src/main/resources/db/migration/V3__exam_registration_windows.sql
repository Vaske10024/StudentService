ALTER TABLE ispitni_rok ADD COLUMN registration_start TIMESTAMP NULL;
ALTER TABLE ispitni_rok ADD COLUMN registration_end TIMESTAMP NULL;
ALTER TABLE ispitni_rok ADD COLUMN cancellation_end TIMESTAMP NULL;
ALTER TABLE ispitni_rok ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE prijava_ispita ADD COLUMN status VARCHAR(32) NOT NULL DEFAULT 'PRIJAVLJEN';
ALTER TABLE prijava_ispita ADD COLUMN cancelled_at TIMESTAMP NULL;
ALTER TABLE prijava_ispita ADD COLUMN cancelled_by_user_id BIGINT NULL;
ALTER TABLE prijava_ispita ADD COLUMN cancellation_reason VARCHAR(1000) NULL;

UPDATE prijava_ispita SET status = 'PONISTEN' WHERE ponisteno = TRUE;
