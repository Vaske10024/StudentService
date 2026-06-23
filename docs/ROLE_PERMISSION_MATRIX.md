# Role and Permission Matrix

Only these user-facing roles exist in the migrated app:

- `STUDENT`
- `PROFESSOR`
- `ADMIN`
- `HEAD_ADMIN`

`HEAD_ADMIN` inherits admin access. The separate role matters for lead privacy: regular admins receive masked lead data, while head admins receive full lead data and full CSV export.

| API/workflow area | STUDENT | PROFESSOR | ADMIN | HEAD_ADMIN |
|---|---|---|---|---|
| Login/logout/me | Own account | Own account | Own account | Own account |
| `/api/me/student/**` | Own linked student/index only | No | Admin uses admin endpoints instead | Admin uses admin endpoints instead |
| `/api/me/professor/**` | No | Own linked professor only | Admin uses admin endpoints instead | Admin uses admin endpoints instead |
| Student add/save index | No | No | Yes | Yes |
| Student profile by id | Own linked index only | No | Yes | Yes |
| Student search/list | No | No by default | Yes | Yes |
| Public lead form | Submit only | Submit only | Submit only | Submit only |
| Lead admin list/export | No | No | Initials only | Full data |
| Study program selection | Authenticated read | Authenticated read | Read/write through admin endpoints | Read/write through admin endpoints |
| Program/school-year/study administration | No | No | Yes | Yes |
| Study-year enrollment requests | Submit/view/cancel own request | No | Review documents and approve with `ENROLLMENT_WRITE` | Review documents and approve with `ENROLLMENT_WRITE` |
| Teaching assignment students | No | Own `DrziPredmet` only | All | All |
| Exam registrations | Own index only | Assigned exams only where exposed | All | All |
| Exam results | Own result through safer route only | Own assigned exams only | All | All |
| Locked exam modification | No | No | Admin override where business policy permits | Admin override where business policy permits |
| Predispit definitions/results | Own read where exposed | Own assigned subjects only | All | All |
| Payments/saldo | Own linked index only | No | All | All |
| Reports | No | Assigned reports only if later exposed | Yes | Yes |
