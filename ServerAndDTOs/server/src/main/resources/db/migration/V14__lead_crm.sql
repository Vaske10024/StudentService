ALTER TABLE potential_student_lead
    ADD COLUMN status VARCHAR(32) NOT NULL DEFAULT 'NEW';

CREATE INDEX idx_potential_student_lead_status ON potential_student_lead(status);

CREATE TABLE lead_email_template (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(180) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    body VARCHAR(10000) NOT NULL,
    active BIT NOT NULL DEFAULT 1,
    created_by_user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_lead_template_creator FOREIGN KEY (created_by_user_id) REFERENCES user_account(id),
    INDEX idx_lead_email_template_active (active)
);

CREATE TABLE lead_email_message (
    id BIGINT NOT NULL AUTO_INCREMENT,
    lead_id BIGINT NOT NULL,
    sent_by_user_id BIGINT NOT NULL,
    template_id BIGINT NULL,
    subject_snapshot VARCHAR(255) NOT NULL,
    body_snapshot VARCHAR(10000) NOT NULL,
    status VARCHAR(32) NOT NULL,
    provider_message_id VARCHAR(255) NULL,
    error_message VARCHAR(2000) NULL,
    sent_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_lead_email_message_lead FOREIGN KEY (lead_id) REFERENCES potential_student_lead(id),
    CONSTRAINT fk_lead_email_message_sender FOREIGN KEY (sent_by_user_id) REFERENCES user_account(id),
    CONSTRAINT fk_lead_email_message_template FOREIGN KEY (template_id) REFERENCES lead_email_template(id),
    INDEX idx_lead_email_message_lead_created (lead_id, created_at),
    INDEX idx_lead_email_message_status (status)
);

CREATE TABLE lead_export_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    exported_by_user_id BIGINT NOT NULL,
    exporter_role VARCHAR(32) NOT NULL,
    export_type VARCHAR(80) NOT NULL,
    masked BIT NOT NULL,
    record_count INT NOT NULL,
    filters VARCHAR(1000) NULL,
    ip_address VARCHAR(64) NULL,
    user_agent VARCHAR(255) NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_lead_export_user FOREIGN KEY (exported_by_user_id) REFERENCES user_account(id),
    INDEX idx_lead_export_created (created_at),
    INDEX idx_lead_export_user (exported_by_user_id)
);

ALTER TABLE audit_log
    ADD COLUMN actor_username VARCHAR(180) NULL,
    ADD COLUMN actor_role VARCHAR(32) NULL,
    ADD COLUMN lead_id BIGINT NULL,
    ADD COLUMN old_value VARCHAR(1000) NULL,
    ADD COLUMN new_value VARCHAR(1000) NULL,
    ADD COLUMN ip_address VARCHAR(64) NULL,
    ADD COLUMN user_agent VARCHAR(255) NULL,
    ADD CONSTRAINT fk_audit_log_lead FOREIGN KEY (lead_id) REFERENCES potential_student_lead(id);

CREATE INDEX idx_audit_log_lead ON audit_log(lead_id);
CREATE INDEX idx_audit_log_created_at ON audit_log(created_at);
