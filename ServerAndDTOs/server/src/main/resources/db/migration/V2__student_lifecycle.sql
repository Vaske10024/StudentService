ALTER TABLE student_indeks ADD COLUMN status VARCHAR(32) NULL;
UPDATE student_indeks SET status = CASE WHEN aktivan = 1 THEN 'AKTIVAN' ELSE 'NEAKTIVAN' END WHERE status IS NULL;
ALTER TABLE student_indeks MODIFY COLUMN status VARCHAR(32) NOT NULL;
ALTER TABLE student_indeks ADD COLUMN status_reason VARCHAR(1000) NULL;
ALTER TABLE student_indeks ADD COLUMN activated_at DATETIME NULL;
ALTER TABLE student_indeks ADD COLUMN deactivated_at DATETIME NULL;

CREATE TABLE student_status_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_indeks_id BIGINT NOT NULL,
    old_status VARCHAR(32) NULL,
    new_status VARCHAR(32) NOT NULL,
    reason VARCHAR(1000) NULL,
    valid_from DATE NOT NULL,
    valid_to DATE NULL,
    changed_by_user_id BIGINT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_status_history_indeks (student_indeks_id),
    CONSTRAINT fk_status_history_indeks FOREIGN KEY (student_indeks_id) REFERENCES student_indeks(id)
);

CREATE TABLE student_status_request (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_indeks_id BIGINT NOT NULL,
    type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    reason VARCHAR(2000) NOT NULL,
    requested_from DATE NULL,
    requested_to DATE NULL,
    submitted_by_user_id BIGINT NULL,
    decided_by_user_id BIGINT NULL,
    decision_note VARCHAR(2000) NULL,
    created_at DATETIME NOT NULL,
    decided_at DATETIME NULL,
    PRIMARY KEY (id),
    INDEX idx_status_request_indeks (student_indeks_id),
    INDEX idx_status_request_status (status),
    CONSTRAINT fk_status_request_indeks FOREIGN KEY (student_indeks_id) REFERENCES student_indeks(id)
);
