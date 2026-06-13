UPDATE student_indeks
SET status = CASE WHEN aktivan = 1 THEN 'AKTIVAN' ELSE 'NEAKTIVAN' END
WHERE status IS NULL OR TRIM(status) = '';
