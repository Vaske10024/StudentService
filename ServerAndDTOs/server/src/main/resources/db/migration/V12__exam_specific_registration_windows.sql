ALTER TABLE ispit ADD COLUMN registration_start TIMESTAMP NULL;
ALTER TABLE ispit ADD COLUMN registration_end TIMESTAMP NULL;
ALTER TABLE ispit ADD COLUMN cancellation_end TIMESTAMP NULL;

UPDATE ispit i
SET registration_start = (SELECT r.registration_start FROM ispitni_rok r WHERE r.id = i.ispitni_rok_id),
    registration_end = (SELECT r.registration_end FROM ispitni_rok r WHERE r.id = i.ispitni_rok_id),
    cancellation_end = (SELECT r.cancellation_end FROM ispitni_rok r WHERE r.id = i.ispitni_rok_id)
WHERE i.registration_start IS NULL
  AND i.registration_end IS NULL
  AND i.cancellation_end IS NULL;
