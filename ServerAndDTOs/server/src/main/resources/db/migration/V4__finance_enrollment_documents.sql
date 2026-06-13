CREATE TABLE tuition_plan (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, student_indeks_id BIGINT NOT NULL,
  financing_type VARCHAR(32) NOT NULL, total_eur DECIMAL(19,2) NOT NULL,
  locked BOOLEAN NOT NULL DEFAULT FALSE, created_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_tuition_plan_indeks FOREIGN KEY (student_indeks_id) REFERENCES student_indeks(id)
);
CREATE TABLE tuition_installment (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, tuition_plan_id BIGINT NOT NULL,
  amount_eur DECIMAL(19,2) NOT NULL, due_date DATE NOT NULL, sequence_number INT NOT NULL,
  CONSTRAINT fk_installment_plan FOREIGN KEY (tuition_plan_id) REFERENCES tuition_plan(id)
);
CREATE TABLE financial_obligation (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, student_indeks_id BIGINT NOT NULL, tuition_plan_id BIGINT NULL,
  type VARCHAR(80) NOT NULL, amount_eur DECIMAL(19,2) NOT NULL, allocated_eur DECIMAL(19,2) NOT NULL DEFAULT 0,
  due_date DATE NOT NULL, created_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_obligation_indeks FOREIGN KEY (student_indeks_id) REFERENCES student_indeks(id),
  CONSTRAINT fk_obligation_plan FOREIGN KEY (tuition_plan_id) REFERENCES tuition_plan(id)
);
CREATE TABLE ledger_entry (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, student_indeks_id BIGINT NOT NULL, type VARCHAR(32) NOT NULL,
  amount_eur DECIMAL(19,2) NOT NULL, reverses_entry_id BIGINT NULL, reversed BOOLEAN NOT NULL DEFAULT FALSE,
  description VARCHAR(500), actor_user_id BIGINT, created_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_ledger_indeks FOREIGN KEY (student_indeks_id) REFERENCES student_indeks(id),
  CONSTRAINT fk_ledger_reverse FOREIGN KEY (reverses_entry_id) REFERENCES ledger_entry(id)
);
CREATE TABLE payment_allocation (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, payment_id BIGINT NOT NULL, obligation_id BIGINT NOT NULL,
  amount_eur DECIMAL(19,2) NOT NULL, created_at TIMESTAMP NOT NULL,
  CONSTRAINT uk_payment_obligation_allocation UNIQUE (payment_id, obligation_id),
  CONSTRAINT fk_allocation_payment FOREIGN KEY (payment_id) REFERENCES ledger_entry(id),
  CONSTRAINT fk_allocation_obligation FOREIGN KEY (obligation_id) REFERENCES financial_obligation(id)
);
CREATE TABLE enrollment_application (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, idempotency_key VARCHAR(80) NOT NULL, status VARCHAR(32) NOT NULL,
  ime VARCHAR(255) NOT NULL, prezime VARCHAR(255) NOT NULL, jmbg VARCHAR(255) NOT NULL, email VARCHAR(255) NOT NULL,
  username VARCHAR(255) NOT NULL, studijski_program_id BIGINT NOT NULL, godina INT NOT NULL, tuition_eur DECIMAL(19,2) NOT NULL,
  created_student_id BIGINT NULL, created_indeks_id BIGINT NULL, decision_reason VARCHAR(255), decided_by_user_id BIGINT,
  decided_at TIMESTAMP NULL, created_at TIMESTAMP NOT NULL, CONSTRAINT uk_enrollment_idempotency_key UNIQUE(idempotency_key)
);
CREATE TABLE enrollment_decision (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, application_id BIGINT NOT NULL, decision VARCHAR(32) NOT NULL,
  reason VARCHAR(255), actor_user_id BIGINT, created_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_enrollment_decision_app FOREIGN KEY(application_id) REFERENCES enrollment_application(id)
);
CREATE TABLE enrollment_document_checklist (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, application_id BIGINT NOT NULL, document_type VARCHAR(255) NOT NULL,
  verified BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_enrollment_checklist_app FOREIGN KEY(application_id) REFERENCES enrollment_application(id)
);
CREATE TABLE student_request (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, student_indeks_id BIGINT NOT NULL, type VARCHAR(64) NOT NULL, status VARCHAR(32) NOT NULL,
  reason VARCHAR(2000) NOT NULL, requested_from DATE, requested_to DATE, submitted_by_user_id BIGINT,
  decided_by_user_id BIGINT, decision_note VARCHAR(255), created_at TIMESTAMP NOT NULL, decided_at TIMESTAMP NULL,
  CONSTRAINT fk_student_request_indeks FOREIGN KEY(student_indeks_id) REFERENCES student_indeks(id)
);
CREATE TABLE student_request_status_history (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, student_request_id BIGINT NOT NULL, old_status VARCHAR(32) NOT NULL,
  new_status VARCHAR(32) NOT NULL, note VARCHAR(255), actor_user_id BIGINT, created_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_request_history_request FOREIGN KEY(student_request_id) REFERENCES student_request(id)
);
CREATE TABLE student_document (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, student_indeks_id BIGINT NOT NULL, student_request_id BIGINT NULL,
  type VARCHAR(32) NOT NULL, original_name VARCHAR(255) NOT NULL, content_type VARCHAR(255) NOT NULL,
  size_bytes BIGINT NOT NULL, storage_key VARCHAR(255) NOT NULL UNIQUE, created_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_document_indeks FOREIGN KEY(student_indeks_id) REFERENCES student_indeks(id),
  CONSTRAINT fk_document_request FOREIGN KEY(student_request_id) REFERENCES student_request(id)
);
CREATE TABLE generated_certificate (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, student_request_id BIGINT NOT NULL UNIQUE, storage_key VARCHAR(255) NOT NULL UNIQUE,
  verification_code VARCHAR(255) NOT NULL, created_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_certificate_request FOREIGN KEY(student_request_id) REFERENCES student_request(id)
);
