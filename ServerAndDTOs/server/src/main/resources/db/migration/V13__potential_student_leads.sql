CREATE TABLE potential_student_lead (
    id BIGINT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(180) NOT NULL,
    phone VARCHAR(60) NULL,
    interested_program VARCHAR(180) NULL,
    source VARCHAR(180) NULL,
    note VARCHAR(1000) NULL,
    privacy_consent BIT NOT NULL DEFAULT 1,
    consent_at DATETIME NOT NULL,
    remote_address VARCHAR(64) NULL,
    user_agent VARCHAR(255) NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_potential_student_lead_created_at (created_at),
    INDEX idx_potential_student_lead_email (email)
);
