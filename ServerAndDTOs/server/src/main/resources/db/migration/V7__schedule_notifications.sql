CREATE TABLE room (id BIGINT AUTO_INCREMENT PRIMARY KEY, code VARCHAR(255) NOT NULL UNIQUE, capacity INT NOT NULL, location VARCHAR(255));
CREATE TABLE student_group (id BIGINT AUTO_INCREMENT PRIMARY KEY, code VARCHAR(255) NOT NULL UNIQUE, name VARCHAR(255));
CREATE TABLE student_group_membership (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, student_group_id BIGINT NOT NULL, student_indeks_id BIGINT NOT NULL,
 CONSTRAINT uk_group_membership UNIQUE(student_group_id,student_indeks_id),
 CONSTRAINT fk_membership_group FOREIGN KEY(student_group_id) REFERENCES student_group(id),
 CONSTRAINT fk_membership_indeks FOREIGN KEY(student_indeks_id) REFERENCES student_indeks(id)
);
CREATE TABLE class_session (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, title VARCHAR(255) NOT NULL, room_id BIGINT NOT NULL, student_group_id BIGINT NOT NULL,
 professor_id BIGINT NOT NULL, starts_at TIMESTAMP NOT NULL, ends_at TIMESTAMP NOT NULL,
 CONSTRAINT fk_session_room FOREIGN KEY(room_id) REFERENCES room(id),
 CONSTRAINT fk_session_group FOREIGN KEY(student_group_id) REFERENCES student_group(id),
 CONSTRAINT fk_session_professor FOREIGN KEY(professor_id) REFERENCES nastavnik(id)
);
CREATE TABLE exam_room_assignment (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, ispit_id BIGINT NOT NULL, room_id BIGINT NOT NULL, expected_students INT NOT NULL,
 CONSTRAINT uk_exam_room UNIQUE(ispit_id,room_id),
 CONSTRAINT fk_exam_room_exam FOREIGN KEY(ispit_id) REFERENCES ispit(id),
 CONSTRAINT fk_exam_room_room FOREIGN KEY(room_id) REFERENCES room(id)
);
CREATE TABLE notification (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, recipient_id BIGINT NOT NULL, type VARCHAR(80) NOT NULL, title VARCHAR(255) NOT NULL,
 message VARCHAR(2000) NOT NULL, read_flag BOOLEAN NOT NULL DEFAULT FALSE, created_at TIMESTAMP NOT NULL,
 CONSTRAINT fk_notification_recipient FOREIGN KEY(recipient_id) REFERENCES user_account(id)
);
CREATE TABLE notification_template (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, template_key VARCHAR(255) NOT NULL UNIQUE, subject VARCHAR(255) NOT NULL, body VARCHAR(4000) NOT NULL
);
CREATE TABLE email_outbox (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, recipient_email VARCHAR(255) NOT NULL, subject VARCHAR(255) NOT NULL,
 body VARCHAR(4000) NOT NULL, status VARCHAR(32) NOT NULL, created_at TIMESTAMP NOT NULL
);
