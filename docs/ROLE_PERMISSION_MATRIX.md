# Role and Permission Matrix

Only these user-facing roles exist in the migrated app:

- `STUDENT`
- `PROFESSOR`
- `ADMIN`

| API/workflow area | STUDENT | PROFESSOR | ADMIN |
|---|---|---|---|
| Login/logout/me | Own account | Own account | Own account |
| `/api/me/student/**` | Own linked student/index only | No | Admin uses admin endpoints instead |
| `/api/me/professor/**` | No | Own linked professor only | Admin uses admin endpoints instead |
| Student add/save index | No | No | Yes |
| Student profile by id | Own linked index only | No | Yes |
| Student search/list | No | No by default | Yes |
| Study program selection | Authenticated read | Authenticated read | Read/write through admin endpoints |
| Program/school-year/study administration | No | No | Yes |
| Study-year enrollment requests | Submit/view/cancel own request | No | Review documents and approve with `ENROLLMENT_WRITE` |
| Teaching assignment students | No | Own `DrziPredmet` only | All |
| Exam registrations | Own index only | Assigned exams only where exposed | All |
| Exam results | Own result through safer route only | Own assigned exams only | All |
| Locked exam modification | No | No | Admin override where business policy permits |
| Predispit definitions/results | Own read where exposed | Own assigned subjects only | All |
| Payments/saldo | Own linked index only | No | All |
| Reports | No | Assigned reports only if later exposed | Yes |
