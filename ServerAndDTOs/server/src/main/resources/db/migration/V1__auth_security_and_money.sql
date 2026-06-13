-- Additive production migration for the web migration. Existing domain tables are kept.
CREATE TABLE IF NOT EXISTS user_account (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(180) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(32) NOT NULL,
    enabled BIT NOT NULL DEFAULT 1,
    linked_student_podaci_id BIGINT NULL,
    linked_student_indeks_id BIGINT NULL,
    linked_nastavnik_id BIGINT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_account_username UNIQUE (username),
    CONSTRAINT fk_user_account_student_podaci FOREIGN KEY (linked_student_podaci_id) REFERENCES student_podaci(id),
    CONSTRAINT fk_user_account_student_indeks FOREIGN KEY (linked_student_indeks_id) REFERENCES student_indeks(id),
    CONSTRAINT fk_user_account_nastavnik FOREIGN KEY (linked_nastavnik_id) REFERENCES nastavnik(id)
);

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    actor_user_id BIGINT NULL,
    action VARCHAR(80) NOT NULL,
    details VARCHAR(2000) NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_audit_log_actor (actor_user_id),
    INDEX idx_audit_log_action (action)
);

ALTER TABLE uplata
    MODIFY COLUMN iznos_rsd DECIMAL(19,2) NOT NULL,
    MODIFY COLUMN srednji_kurs_eur DECIMAL(19,6) NOT NULL;

ALTER TABLE uplata
    ADD COLUMN fallback_kurs BIT NOT NULL DEFAULT 0;

-- Database-level backstop for the active-school-year invariant: only one row can have aktivna=1.
-- MySQL allows multiple NULL values in a unique key, so inactive years do not conflict.
ALTER TABLE skolska_godina
    ADD COLUMN active_unique_key TINYINT GENERATED ALWAYS AS (CASE WHEN aktivna = 1 THEN 1 ELSE NULL END) STORED,
    ADD UNIQUE KEY uk_skolska_godina_only_one_active (active_unique_key);
