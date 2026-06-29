UPDATE potential_student_lead
SET status = 'NEW'
WHERE status IS NULL OR TRIM(status) = '';

UPDATE potential_student_lead
SET status = UPPER(TRIM(status))
WHERE status IS NOT NULL AND TRIM(status) <> '';

UPDATE potential_student_lead
SET status = 'NEW'
WHERE status NOT IN ('NEW', 'CONTACTED', 'INTERESTED', 'NOT_INTERESTED', 'ENROLLED', 'INVALID');
