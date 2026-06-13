CREATE TABLE IF NOT EXISTS realizacija_predmeta (
    id BIGINT NOT NULL AUTO_INCREMENT,
    program_predmet_id BIGINT NOT NULL,
    skolska_godina_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT uk_realizacija_program_predmet_sg UNIQUE (program_predmet_id, skolska_godina_id),
    CONSTRAINT fk_realizacija_program_predmet FOREIGN KEY (program_predmet_id) REFERENCES program_predmet(id),
    CONSTRAINT fk_realizacija_skolska_godina FOREIGN KEY (skolska_godina_id) REFERENCES skolska_godina(id)
);

ALTER TABLE drzi_predmet ADD COLUMN realizacija_predmeta_id BIGINT NULL;
ALTER TABLE drzi_predmet ADD COLUMN uloga VARCHAR(20) NULL DEFAULT 'NOSILAC';
ALTER TABLE drzi_predmet ADD CONSTRAINT fk_drzi_realizacija
    FOREIGN KEY (realizacija_predmeta_id) REFERENCES realizacija_predmeta(id);
ALTER TABLE drzi_predmet DROP INDEX uk_drzi_nastavnik_predmet_sg;
CREATE UNIQUE INDEX uk_drzi_realizacija_nastavnik_uloga
    ON drzi_predmet(realizacija_predmeta_id, nastavnik_id, uloga);

ALTER TABLE slusa_predmet ADD COLUMN realizacija_predmeta_id BIGINT NULL;
ALTER TABLE slusa_predmet ADD COLUMN upis_godine_id BIGINT NULL;
ALTER TABLE slusa_predmet ADD CONSTRAINT fk_slusa_realizacija
    FOREIGN KEY (realizacija_predmeta_id) REFERENCES realizacija_predmeta(id);
ALTER TABLE slusa_predmet ADD CONSTRAINT fk_slusa_upis
    FOREIGN KEY (upis_godine_id) REFERENCES upis_godine(id);

INSERT IGNORE INTO realizacija_predmeta (program_predmet_id, skolska_godina_id, status)
SELECT DISTINCT pp.id, dp.skolska_godina_id, 'ACTIVE'
FROM drzi_predmet dp
JOIN program_predmet pp ON pp.predmet_id = dp.predmet_id
WHERE dp.skolska_godina_id IS NOT NULL;

UPDATE drzi_predmet dp
SET dp.realizacija_predmeta_id = (
    SELECT MIN(rp.id)
    FROM realizacija_predmeta rp
    JOIN program_predmet pp ON pp.id = rp.program_predmet_id
    WHERE pp.predmet_id = dp.predmet_id AND rp.skolska_godina_id = dp.skolska_godina_id
)
WHERE dp.realizacija_predmeta_id IS NULL;

UPDATE slusa_predmet sp
JOIN drzi_predmet dp ON dp.id = sp.drzi_predmet_id
SET sp.realizacija_predmeta_id = dp.realizacija_predmeta_id
WHERE sp.realizacija_predmeta_id IS NULL;

DELETE duplicate_slusa
FROM slusa_predmet duplicate_slusa
JOIN slusa_predmet kept_slusa
  ON duplicate_slusa.student_indeks_id = kept_slusa.student_indeks_id
 AND duplicate_slusa.realizacija_predmeta_id = kept_slusa.realizacija_predmeta_id
 AND duplicate_slusa.id > kept_slusa.id
WHERE duplicate_slusa.realizacija_predmeta_id IS NOT NULL;

CREATE UNIQUE INDEX uk_slusa_student_realizacija
    ON slusa_predmet(student_indeks_id, realizacija_predmeta_id);

ALTER TABLE upis_godine ADD COLUMN skolska_godina_id BIGINT NULL;
UPDATE upis_godine u
SET u.skolska_godina_id = (SELECT sg.id FROM skolska_godina sg WHERE sg.aktivna = 1 LIMIT 1)
WHERE u.skolska_godina_id IS NULL;
ALTER TABLE upis_godine ADD CONSTRAINT fk_upis_skolska_godina
    FOREIGN KEY (skolska_godina_id) REFERENCES skolska_godina(id);
CREATE UNIQUE INDEX uk_upis_indeks_godina_sg
    ON upis_godine(indeks_id, upisuje_godinu, skolska_godina_id);
