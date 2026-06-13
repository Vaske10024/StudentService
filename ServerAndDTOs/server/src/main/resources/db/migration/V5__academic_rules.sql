CREATE TABLE subject_prerequisite (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, subject_id BIGINT NOT NULL, prerequisite_id BIGINT NOT NULL,
 CONSTRAINT uk_subject_prerequisite UNIQUE(subject_id, prerequisite_id),
 CONSTRAINT fk_prerequisite_subject FOREIGN KEY(subject_id) REFERENCES predmet(id),
 CONSTRAINT fk_prerequisite_required FOREIGN KEY(prerequisite_id) REFERENCES predmet(id)
);
CREATE TABLE academic_rule (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, rule_key VARCHAR(255) NOT NULL UNIQUE, rule_value VARCHAR(1000) NOT NULL, active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE ectsrule (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, program_id BIGINT NOT NULL, target_year INT NOT NULL, minimum_ects INT NOT NULL,
 CONSTRAINT uk_ects_program_year UNIQUE(program_id,target_year), CONSTRAINT fk_ects_program FOREIGN KEY(program_id) REFERENCES studijski_program(id)
);
CREATE TABLE graduation_record (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, student_indeks_id BIGINT NOT NULL UNIQUE, earned_ects INT NOT NULL,
 average_grade DOUBLE NOT NULL, note VARCHAR(255), approved_by_user_id BIGINT, graduated_at TIMESTAMP NOT NULL,
 CONSTRAINT fk_graduation_indeks FOREIGN KEY(student_indeks_id) REFERENCES student_indeks(id)
);
CREATE TABLE program_transfer (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, student_indeks_id BIGINT NOT NULL, from_program_id BIGINT NOT NULL, to_program_id BIGINT NOT NULL,
 status VARCHAR(32) NOT NULL, reason VARCHAR(1000) NOT NULL, decided_by_user_id BIGINT, decided_at TIMESTAMP, created_at TIMESTAMP NOT NULL,
 CONSTRAINT fk_transfer_indeks FOREIGN KEY(student_indeks_id) REFERENCES student_indeks(id),
 CONSTRAINT fk_transfer_from FOREIGN KEY(from_program_id) REFERENCES studijski_program(id),
 CONSTRAINT fk_transfer_to FOREIGN KEY(to_program_id) REFERENCES studijski_program(id)
);
CREATE TABLE recognized_subject (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, student_indeks_id BIGINT NOT NULL, subject_id BIGINT NOT NULL,
 grade INT, ects INT, source VARCHAR(255) NOT NULL, approved_by_user_id BIGINT, created_at TIMESTAMP NOT NULL,
 CONSTRAINT uk_recognized_subject UNIQUE(student_indeks_id,subject_id),
 CONSTRAINT fk_recognized_indeks FOREIGN KEY(student_indeks_id) REFERENCES student_indeks(id),
 CONSTRAINT fk_recognized_subject FOREIGN KEY(subject_id) REFERENCES predmet(id)
);
