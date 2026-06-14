CREATE TABLE study_year_enrollment_request (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_indeks_id BIGINT NOT NULL,
    current_school_year_id BIGINT NOT NULL,
    target_school_year_id BIGINT NOT NULL,
    type VARCHAR(40) NOT NULL,
    status VARCHAR(40) NOT NULL,
    current_study_year INT NOT NULL,
    requested_study_year INT NOT NULL,
    earned_ects_snapshot INT NOT NULL,
    contract_received BOOLEAN NOT NULL DEFAULT FALSE,
    payment_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    documentation_complete BOOLEAN NOT NULL DEFAULT FALSE,
    student_note VARCHAR(2000) NULL,
    admin_note VARCHAR(2000) NULL,
    submitted_by_user_id BIGINT NULL,
    decided_by_user_id BIGINT NULL,
    approved_upis_godine_id BIGINT NULL,
    approved_obnova_godine_id BIGINT NULL,
    submitted_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    decided_at DATETIME NULL,
    PRIMARY KEY (id),
    INDEX idx_year_request_student (student_indeks_id),
    INDEX idx_year_request_status (status),
    INDEX idx_year_request_target_year (target_school_year_id),
    CONSTRAINT fk_year_request_indeks FOREIGN KEY (student_indeks_id) REFERENCES student_indeks(id),
    CONSTRAINT fk_year_request_current_sg FOREIGN KEY (current_school_year_id) REFERENCES skolska_godina(id),
    CONSTRAINT fk_year_request_target_sg FOREIGN KEY (target_school_year_id) REFERENCES skolska_godina(id),
    CONSTRAINT fk_year_request_approved_upis FOREIGN KEY (approved_upis_godine_id) REFERENCES upis_godine(id),
    CONSTRAINT fk_year_request_approved_obnova FOREIGN KEY (approved_obnova_godine_id) REFERENCES obnova_godine(id)
);

CREATE TABLE study_year_enrollment_transferred_subject (
    id BIGINT NOT NULL AUTO_INCREMENT,
    request_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    ects_snapshot INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_year_request_subject UNIQUE (request_id, subject_id),
    CONSTRAINT fk_year_request_subject_request FOREIGN KEY (request_id)
        REFERENCES study_year_enrollment_request(id),
    CONSTRAINT fk_year_request_subject_subject FOREIGN KEY (subject_id) REFERENCES predmet(id)
);

CREATE TABLE study_year_enrollment_request_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    request_id BIGINT NOT NULL,
    old_status VARCHAR(40) NULL,
    new_status VARCHAR(40) NOT NULL,
    note VARCHAR(2000) NULL,
    actor_user_id BIGINT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_year_request_history_request (request_id),
    CONSTRAINT fk_year_request_history_request FOREIGN KEY (request_id)
        REFERENCES study_year_enrollment_request(id)
);

ALTER TABLE obnova_godine ADD COLUMN skolska_godina_id BIGINT NULL;
ALTER TABLE obnova_godine ADD COLUMN upis_godine_id BIGINT NULL;
ALTER TABLE obnova_godine ADD CONSTRAINT fk_obnova_skolska_godina
    FOREIGN KEY (skolska_godina_id) REFERENCES skolska_godina(id);
ALTER TABLE obnova_godine ADD CONSTRAINT fk_obnova_upis_godine
    FOREIGN KEY (upis_godine_id) REFERENCES upis_godine(id);
