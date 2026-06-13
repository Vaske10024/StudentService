CREATE TABLE role_permission (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, role VARCHAR(32) NOT NULL, permission VARCHAR(64) NOT NULL,
 CONSTRAINT uk_role_permission UNIQUE(role,permission)
);
CREATE TABLE user_permission_override (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, user_account_id BIGINT NOT NULL, permission VARCHAR(64) NOT NULL, allowed BOOLEAN NOT NULL,
 CONSTRAINT uk_user_permission_override UNIQUE(user_account_id,permission),
 CONSTRAINT fk_permission_override_user FOREIGN KEY(user_account_id) REFERENCES user_account(id)
);
CREATE TABLE system_setting (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, setting_key VARCHAR(255) NOT NULL UNIQUE, setting_value VARCHAR(2000) NOT NULL,
 description VARCHAR(255), updated_by_user_id BIGINT, updated_at TIMESTAMP NOT NULL
);
