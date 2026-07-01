# Smart Clinic Management System — Backend

A Spring Boot backend for a clinic management system, built with a clean
layered architecture (Controller → Service → Repository → Entity), backed
by PostgreSQL, with JWT-based authentication and role-based access control
(Phase 2), and a full appointment + medical record business workflow
(Phase 3).

## Tech Stack

- Java 17
- Spring Boot 3.3.4
- Spring Data JPA (Hibernate)
- Spring Security (stateless, JWT-based)
- JJWT 0.12.6 (JWT generation/parsing)
- PostgreSQL
- Lombok (reduces entity/service boilerplate)
- Maven

## Project Structure

```
src/main/java/com/smartclinic/smartclinic/
├── SmartclinicApplication.java      # main entry point
├── entity/
│   ├── User.java                    # implements Spring Security's UserDetails
│   ├── Patient.java
│   ├── Doctor.java
│   ├── Appointment.java
│   ├── MedicalRecord.java           # now has diagnosis, treatment, patient, doctor, createdAt
│   ├── Role.java                    # enum: PATIENT, DOCTOR, ADMIN
│   └── AppointmentStatus.java        # enum: PENDING, APPROVED, REJECTED, COMPLETED
├── dto/
│   ├── RegisterRequest.java
│   ├── LoginRequest.java
│   ├── AuthResponse.java
│   ├── BookAppointmentRequest.java   # Phase 3
│   └── CreateMedicalRecordRequest.java # Phase 3
├── security/
│   ├── JwtService.java               # generate/parse/validate JWTs
│   ├── JwtAuthFilter.java            # runs once per request, populates security context
│   ├── CustomUserDetailsService.java # loads User by email for Spring Security
│   ├── CurrentUserResolver.java      # Phase 3: JWT -> User -> Patient/Doctor profile
│   └── SecurityConfig.java           # filter chain, URL rules, password encoder
├── repository/
│   ├── UserRepository.java
│   ├── PatientRepository.java
│   ├── DoctorRepository.java
│   ├── AppointmentRepository.java
│   └── MedicalRecordRepository.java
├── service/
│   ├── AuthService.java              # register / login business logic
│   ├── UserService.java
│   ├── PatientService.java
│   ├── DoctorService.java
│   ├── AppointmentService.java       # Phase 3: booking, status lifecycle, ownership
│   └── MedicalRecordService.java     # Phase 3: creation rules, ownership-filtered reads
├── controller/
│   ├── AuthController.java           # POST /auth/register, /auth/login
│   ├── AdminController.java          # demo: GET /admin/dashboard
│   ├── DoctorHomeController.java     # demo: GET /doctor/dashboard
│   ├── PatientHomeController.java    # demo: GET /patient/dashboard
│   ├── UserController.java           # /api/users (ADMIN only)
│   ├── PatientController.java
│   ├── DoctorController.java
│   ├── AppointmentController.java    # Phase 3 workflow + Phase 1 generic CRUD
│   └── MedicalRecordController.java  # Phase 3 workflow + Phase 1 generic CRUD
└── exception/
    ├── ResourceNotFoundException.java
    ├── ForbiddenOperationException.java     # Phase 3: ownership violations (403)
    ├── InvalidStatusTransitionException.java # Phase 3: bad appointment status change (409)
    ├── IllegalStateConflictException.java    # Phase 3: other state conflicts (409)
    └── GlobalExceptionHandler.java   # maps exceptions -> HTTP status codes
```

## Entity Relationships

- **User ↔ Patient**: one-to-one (`patients.user_id` is the FK, unique)
- **User ↔ Doctor**: one-to-one (`doctors.user_id` is the FK, unique)
- **Patient → Appointment**: one-to-many (`appointments.patient_id` FK)
- **Doctor → Appointment**: one-to-many (`appointments.doctor_id` FK)
- **Appointment ↔ MedicalRecord**: one-to-one (`medical_records.appointment_id` FK, unique)
- **Patient/Doctor → MedicalRecord**: many-to-one each (`medical_records.patient_id`, `medical_records.doctor_id`) — denormalized copies of the appointment's patient/doctor, kept directly on the record so it can be queried by patient or doctor id without joining through `appointment` first. Always set from the appointment at creation time, never trusted from client input.

```
User (1) ── (1) Patient (1) ── (*) Appointment (1) ── (1) MedicalRecord
User (1) ── (1) Doctor  (1) ── (*) Appointment
```

A `User` only ever has a `Patient` *or* a `Doctor` profile in practice
(governed by its `role` field), but both associations exist on `User`
since either is possible at the schema level.

## Authentication & Role-Based Access Control

### How it works

1. **Register** — `POST /auth/register` creates a `User` row with a
   BCrypt-hashed password and returns a JWT immediately.
2. **Login** — `POST /auth/login` validates credentials via Spring
   Security's `AuthenticationManager` (BCrypt comparison happens here)
   and returns a fresh JWT.
3. **Protected requests** — send `Authorization: Bearer <token>` on every
   subsequent request. `JwtAuthFilter` runs once per request, validates
   the token, and (if valid) populates Spring Security's context with the
   user's identity and role — this is what makes the role checks below
   actually work.
4. Server is fully **stateless** — no session is stored anywhere; every
   request re-proves identity via its own token.

### JWT contents

| Claim | Meaning |
|---|---|
| `sub` (subject) | the user's email |
| `role` | `ADMIN`, `DOCTOR`, or `PATIENT` |
| `iat` / `exp` | issued-at / expiration timestamps (24h lifetime by default) |

> Note: the spec's User model describes a `username` field. This project
> reuses the existing `email` field as the login identifier instead of
> adding a redundant column — it's already unique and is exactly what a
> JWT subject should hold. If you genuinely need a separate, changeable
> display username distinct from email, add a `username` column to
> `User` and swap the lookups in `CustomUserDetailsService` /
> `AuthService` accordingly.

### Access rules (`SecurityConfig`)

| Path pattern | Who can access |
|---|---|
| `/auth/**` | Public — no token required |
| `/admin/**` | `ADMIN` only |
| `/doctor/**` | `DOCTOR` only |
| `/patient/**` | `PATIENT` only |
| `/api/users/**` | `ADMIN` only (enforced via `@PreAuthorize`, since this path doesn't match the prefixes above) |
| Everything else (e.g. `/api/patients`, `/api/appointments`) | Any authenticated user, regardless of role |

`/admin/dashboard`, `/doctor/dashboard`, and `/patient/dashboard` are
included as minimal working examples of each role-restricted prefix —
replace/extend them with real business endpoints as needed.

### Trying it end-to-end

Register a patient:
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Jane Doe","email":"jane@example.com","password":"changeme123","role":"PATIENT"}'
```
Response:
```json
{
  "token": "eyJhbGciOi...",
  "tokenType": "Bearer",
  "userId": 1,
  "name": "Jane Doe",
  "email": "jane@example.com",
  "role": "PATIENT"
}
```

Log in again later:
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"jane@example.com","password":"changeme123"}'
```

Call a protected endpoint with the token:
```bash
curl http://localhost:8080/patient/dashboard \
  -H "Authorization: Bearer eyJhbGciOi..."
```

Calling `/admin/dashboard` or `/doctor/dashboard` with that same PATIENT
token correctly returns `403 Forbidden` — try it to see the RBAC rule
in action. Calling any protected endpoint with no token, or an expired
one, returns `401 Unauthorized`.

### Security notes

- Passwords are hashed with `BCryptPasswordEncoder` — raw passwords are
  never stored or logged.
- `jwt.secret` in `application.properties` is a randomly generated key
  included **for local development only**. Override it via an
  environment variable (`jwt.secret=${JWT_SECRET}`) before deploying
  anywhere real, and never commit a production secret to source control.
- `User.getPassword()` is excluded from all JSON responses via
  `@JsonIgnore`, so the hash is never returned to a client even by
  accident.

## Appointment & Medical Record Workflow (Phase 3)

### Appointment lifecycle

```
PENDING ──approve──> APPROVED ──complete──> COMPLETED
   │
   └──reject──> REJECTED
```

- New appointments always start at `PENDING` — set server-side, never by the client.
- `APPROVED` and `REJECTED` are only reachable from `PENDING`.
- `COMPLETED` is only reachable from `APPROVED`.
- `REJECTED` and `COMPLETED` are terminal — no further transitions are allowed from either.
- All of this is validated in `AppointmentService` (`assertTransition`), not the controller, per the spec. An invalid transition returns `409 Conflict`.

### Who can do what

| Action | Role | Ownership rule (enforced in the service layer) |
|---|---|---|
| Book an appointment | `PATIENT` | Booked for themselves only — `patient` is resolved from the JWT, never from the request body |
| View "my" appointments | `PATIENT` | Only their own |
| View "all" appointments | `ADMIN`, `DOCTOR` | `ADMIN` sees everything; `DOCTOR` sees only appointments assigned to them |
| Approve / reject | `DOCTOR`, `ADMIN` | A `DOCTOR` may only approve/reject appointments assigned to them; `ADMIN` may act on any |
| Complete | `DOCTOR` only | Only the assigned doctor — not opened up to `ADMIN`, since marking a consultation as actually completed isn't something an admin should do on a doctor's behalf |
| Create a medical record | `DOCTOR` only | Only for one of their own appointments, and only once that appointment is `COMPLETED` |
| View "my" medical records | `PATIENT` | Only their own |
| View records for a given patient | `DOCTOR`, `ADMIN` | `ADMIN` sees all of that patient's records; `DOCTOR` sees only the subset that came from their own appointments with that patient |
| View "all" medical records | `ADMIN` only | — |

Two layers of access control work together here:
- **`SecurityConfig`** / **`@PreAuthorize`** decide whether a given *role* may call an endpoint at all (e.g. only `PATIENT` can hit `POST /api/appointments/book`).
- **The service layer** (`AppointmentService`, `MedicalRecordService`, via `CurrentUserResolver`) decides whether *this specific* doctor/patient is allowed to touch *this specific* resource — a `DOCTOR` token is accepted by `PUT /api/appointments/{id}/approve`, but the request still fails with `403 Forbidden` if that appointment belongs to a different doctor.

### Endpoints

Per the spec's suggested paths, but nested under the project's existing `/api/appointments` and `/api/medical-records` prefixes rather than introducing a second, parallel URL namespace for the same two resources.

**Appointments — `/api/appointments`**

| Method | Path | Role | Description |
|--------|------|------|-------------|
| POST | `/api/appointments/book` | PATIENT | Book a new appointment (status starts at `PENDING`). Returns `{ appointment, notificationMessage }` (Phase 5). |
| GET | `/api/appointments/my` | PATIENT | List the caller's own appointments |
| GET | `/api/appointments/all` | ADMIN, DOCTOR | All appointments (ADMIN) or assigned appointments (DOCTOR) |
| PUT | `/api/appointments/{id}/approve` | DOCTOR, ADMIN | `PENDING` → `APPROVED`. Returns `{ appointment, notificationMessage }` (Phase 5). |
| PUT | `/api/appointments/{id}/reject` | DOCTOR, ADMIN | `PENDING` → `REJECTED`. Returns `{ appointment, notificationMessage }` (Phase 5). |
| PUT | `/api/appointments/{id}/complete` | DOCTOR | `APPROVED` → `COMPLETED`. Returns a bare appointment — no notification step on this transition. |

**Statistics — `/api/statistics`** (ADMIN only, Phase 5)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/statistics/dashboard` | Top-line totals for the admin dashboard's stat cards |
| GET | `/api/statistics/appointments/monthly` | Appointments grouped by calendar month |
| GET | `/api/statistics/appointments/by-doctor` | Appointments grouped by doctor, busiest first |

**Medical Records — `/api/medical-records`**

| Method | Path | Role | Description |
|--------|------|------|-------------|
| POST | `/api/medical-records/create` | DOCTOR | Create a record for one of the caller's own `COMPLETED` appointments |
| GET | `/api/medical-records/my` | PATIENT | List the caller's own records |
| GET | `/api/medical-records/all` | ADMIN | List every record in the system |
| GET | `/api/medical-records/patient/{id}` | DOCTOR, ADMIN | Records for a given patient (filtered to the caller's own appointments if DOCTOR) |

The original Phase 1 generic CRUD endpoints (`GET/POST/PUT/DELETE /api/appointments`, `/api/medical-records`, etc.) are retained alongside these and are now restricted to `ADMIN` only, for direct/manual record management outside the normal workflow.

### Trying the workflow end-to-end

```bash
# 1. Patient books an appointment with doctor id 1
curl -X POST http://localhost:8080/api/appointments/book \
  -H "Authorization: Bearer <PATIENT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"doctorId": 1, "date": "2026-07-10T10:00:00"}'
# -> 201 Created, status: PENDING

# 2. Doctor approves it (assuming appointment id 1)
curl -X PUT http://localhost:8080/api/appointments/1/approve \
  -H "Authorization: Bearer <DOCTOR_TOKEN>"
# -> 200 OK, status: APPROVED

# 3. Doctor marks it complete after the consultation
curl -X PUT http://localhost:8080/api/appointments/1/complete \
  -H "Authorization: Bearer <DOCTOR_TOKEN>"
# -> 200 OK, status: COMPLETED

# 4. Doctor writes up the medical record
curl -X POST http://localhost:8080/api/medical-records/create \
  -H "Authorization: Bearer <DOCTOR_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"appointmentId": 1, "diagnosis": "Seasonal allergy", "treatment": "Antihistamine, 10mg daily", "notes": "Follow up in 2 weeks if symptoms persist."}'
# -> 201 Created

# 5. Patient reads their own records
curl http://localhost:8080/api/medical-records/my \
  -H "Authorization: Bearer <PATIENT_TOKEN>"
```

Things worth deliberately trying to see the guardrails in action:
- Calling `/complete` on a `PENDING` (not yet `APPROVED`) appointment → `409 Conflict`.
- A different doctor trying to approve/complete this appointment → `403 Forbidden`.
- Creating a medical record before the appointment is `COMPLETED` → `409 Conflict`.
- Creating a second medical record for the same appointment → `409 Conflict`.
- A patient requesting `GET /api/medical-records/patient/{id}` → `403 Forbidden` (role-level rejection, since that endpoint is DOCTOR/ADMIN only).

### ⚠️ Schema change note

`AppointmentStatus` previously had values `SCHEDULED, COMPLETED, CANCELLED` and now has `PENDING, APPROVED, REJECTED, COMPLETED`. `MedicalRecord` gained new `NOT NULL` columns (`diagnosis`, `treatment`, `patient_id`, `doctor_id`, `created_at`). If you have an existing local database from before Phase 3 with rows in either table, `ddl-auto=update` will **not** safely migrate that data — old status values and rows missing the new required columns will not match the new schema. For local development, the simplest fix is to drop and recreate the database:
```sql
DROP DATABASE smart_clinic_db;
CREATE DATABASE smart_clinic_db;
```
For anything beyond local development, this is exactly the kind of change a real migration tool (Flyway/Liquibase) is meant for — see the existing note about `ddl-auto` in Database Setup below.

## Dashboard Statistics, Charts & Email Simulation (Phase 5)

### Statistics endpoints

`StatisticsController` (`/api/statistics`, ADMIN only) exposes three
read-only aggregation endpoints, backed by `StatisticsService`:

| Method | Path | Returns |
|---|---|---|
| GET | `/api/statistics/dashboard` | `{ totalPatients, totalDoctors, totalAppointments, pendingAppointments, approvedAppointments, completedAppointments, rejectedAppointments }` |
| GET | `/api/statistics/appointments/monthly` | `[{ monthLabel, count }]` — one entry per calendar month that has at least one appointment |
| GET | `/api/statistics/appointments/by-doctor` | `[{ doctorId, doctorName, appointmentCount }]` — busiest doctor first |

**Naming note:** the field is `rejectedAppointments`, not
`cancelledAppointments` as in the original Phase 5 brief. This system's
`AppointmentStatus` enum has no `CANCELLED` value (see the Phase 3
schema-change note above) — the equivalent terminal "did not happen"
status is `REJECTED`. Nothing was renamed or faked to manufacture a
`cancelled` count that doesn't exist in the actual data; the field name
reflects what's really being counted.

### Email simulation

Two new services, with no real SMTP integration — every "send" is a
structured log line plus a `System.out.println`:

- **`EmailService`** — the low-level "deliver this email" simulation.
  Knows nothing about appointments; just logs a recipient/subject/body.
  This is the one class you'd swap out for a real `JavaMailSender`
  integration later.
- **`NotificationService`** — the domain layer. Knows the three
  appointment-related messages and composes them with real appointment
  data (doctor name, date, patient email) before asking `EmailService`
  to "send" them. Returns the exact confirmation string the frontend
  should display.

`AppointmentService.bookAppointment` / `approveAppointment` /
`rejectAppointment` each gained a `...WithNotification` sibling method
(e.g. `bookAppointmentWithNotification`) that calls the original method,
triggers the matching `NotificationService` call, and returns both the
appointment and the resulting message wrapped in `AppointmentActionResponse`.
The controller endpoints for book/approve/reject now return this wrapper
instead of a bare `Appointment` — **this is a response-shape change** from
Phase 3: `POST /api/appointments/book`, `PUT /api/appointments/{id}/approve`,
and `PUT /api/appointments/{id}/reject` now return
`{ appointment, notificationMessage }` rather than the appointment
directly. `PUT /api/appointments/{id}/complete` is unchanged (no
notification is specified for that transition).

The three exact messages (matching the spec):
- Booking → `"Email sent successfully to patient."`
- Approval → `"Appointment approval email sent."`
- Rejection (the spec calls this "cancelled") → `"Appointment cancellation email sent."`

## Prerequisites

- JDK 17+
- Maven 3.8+ (or use an IDE with bundled Maven)
- PostgreSQL 13+ running locally (or update the connection URL to point
  elsewhere)

## Database Setup

Create the database before first run:

```sql
CREATE DATABASE smart_clinic_db;
```

Update `src/main/resources/application.properties` with your own
credentials if they differ from the defaults:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/smart_clinic_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

`spring.jpa.hibernate.ddl-auto=update` is set, so Hibernate will create/update
the `users`, `patients`, `doctors`, `appointments`, and `medical_records`
tables automatically on startup — no manual schema scripting needed for
local development.

## Running the Application

```bash
mvn spring-boot:run
```

or build a jar and run it directly:

```bash
mvn clean package
java -jar target/smartclinic-0.0.1-SNAPSHOT.jar
```

The API will be available at `http://localhost:8080`.

> **Note on this delivery:** the sandbox this project was built in has no
> network access and no Maven installation, so the build could not be
> compiled or run here. The code has been carefully hand-reviewed for
> consistency (package paths, imports, field/getter/setter names, JPA
> annotations), but please run `mvn clean compile` as your first step
> after downloading to confirm a clean build in your own environment.

## API Endpoints

All endpoints return/accept JSON. Base URL: `http://localhost:8080`

### Users — `/api/users` (ADMIN only)
| Method | Path | Description |
|--------|------|-------------|
| GET    | `/api/users` | List all users |
| GET    | `/api/users/{id}` | Get a user by id |
| POST   | `/api/users` | Create a user |
| PUT    | `/api/users/{id}` | Update a user |
| DELETE | `/api/users/{id}` | Delete a user |

### Patients — `/api/patients`
| Method | Path | Description |
|--------|------|-------------|
| GET    | `/api/patients` | List all patients |
| GET    | `/api/patients/{id}` | Get a patient by id |
| POST   | `/api/patients` | Create a patient |
| PUT    | `/api/patients/{id}` | Update a patient |
| DELETE | `/api/patients/{id}` | Delete a patient |

### Doctors — `/api/doctors`
| Method | Path | Description |
|--------|------|-------------|
| GET    | `/api/doctors` | List all doctors |
| GET    | `/api/doctors/{id}` | Get a doctor by id |
| POST   | `/api/doctors` | Create a doctor |
| PUT    | `/api/doctors/{id}` | Update a doctor |
| DELETE | `/api/doctors/{id}` | Delete a doctor |

### Appointments — `/api/appointments` (generic CRUD, ADMIN only)
See [Appointment & Medical Record Workflow](#appointment--medical-record-workflow-phase-3) above for the patient/doctor-facing workflow endpoints (`/book`, `/my`, `/all`, `/approve`, `/reject`, `/complete`).

| Method | Path | Description |
|--------|------|-------------|
| GET    | `/api/appointments` | List all appointments |
| GET    | `/api/appointments/{id}` | Get an appointment by id |
| GET    | `/api/appointments/patient/{patientId}` | List appointments for a patient |
| GET    | `/api/appointments/doctor/{doctorId}` | List appointments for a doctor |
| POST   | `/api/appointments` | Create an appointment directly |
| PUT    | `/api/appointments/{id}` | Update an appointment directly |
| DELETE | `/api/appointments/{id}` | Delete an appointment |

### Medical Records — `/api/medical-records` (generic CRUD, ADMIN only)
See [Appointment & Medical Record Workflow](#appointment--medical-record-workflow-phase-3) above for the patient/doctor-facing workflow endpoints (`/create`, `/my`, `/all`, `/patient/{id}`).

| Method | Path | Description |
|--------|------|-------------|
| GET    | `/api/medical-records` | List all medical records |
| GET    | `/api/medical-records/{id}` | Get a medical record by id |
| PUT    | `/api/medical-records/{id}` | Update a medical record directly |
| DELETE | `/api/medical-records/{id}` | Delete a medical record |

## Example Requests

Register a patient and a doctor (see [Authentication](#authentication--role-based-access-control) above for the full request/response):
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Jane Doe","email":"jane@example.com","password":"changeme123","role":"PATIENT"}'

curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Dr. Smith","email":"dr.smith@example.com","password":"changeme123","role":"DOCTOR"}'
```

Each response includes a JWT. Use it to create the corresponding profile (assuming the patient got user id `1` and the doctor got user id `2` — an ADMIN token is needed for these two, since `/api/patients` and `/api/doctors` allow any authenticated user to read, but profile creation here is just plain CRUD with no ownership wiring yet beyond the User link):
```bash
curl -X POST http://localhost:8080/api/patients \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"age":29,"gender":"female","user":{"id":1}}'

curl -X POST http://localhost:8080/api/doctors \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"specialization":"Cardiology","user":{"id":2}}'
```

For the full booking → approve → complete → medical record flow, see
[Trying the workflow end-to-end](#trying-the-workflow-end-to-end) above.

## What's Intentionally Not Included Yet

- Refresh tokens (the JWT simply expires after 24h; no rotation/refresh
  endpoint exists yet — the client must log in again)
- Token blacklisting/logout-everywhere (stateless JWTs can't be
  individually revoked without an extra store, e.g. Redis, tracking
  invalidated token IDs)
- Rate limiting on `/auth/login` (brute-force protection)
- Email verification / password reset flows
- Appointment conflict checking (e.g. nothing currently stops a doctor
  from being double-booked at the same date/time — `bookAppointment`
  only checks that the doctor exists, not their availability)
- Updating/cancelling a `PENDING` appointment from the patient's side
  (only doctor/admin-initiated transitions exist right now — a patient
  can't withdraw their own request)
- Pagination on list endpoints (`/my`, `/all`, `/patient/{id}`, etc. -
  all currently return every matching row in one response)
- DTOs for the Phase 1 generic CRUD endpoints (`/api/patients`,
  `/api/doctors`, the admin-only `/api/appointments`,
  `/api/medical-records`) — those controllers still bind directly to
  entities; consider introducing request/response DTOs before exposing
  this publicly, to control exactly what nested data gets serialized in
  responses (the new Phase 3 workflow endpoints already use DTOs for
  their inputs)
- Real email delivery — `EmailService` only logs; swapping in
  `JavaMailSender` (or a transactional email provider) means changing
  that one class, not any of its callers
- A notification on `completeAppointment` — the Phase 5 spec only
  specifies messages for booking, approval, and rejection/cancellation
- Caching on the statistics endpoints — every call to
  `/api/statistics/**` re-runs the aggregation queries live; fine at
  this scale, but worth caching (e.g. with a short TTL) if the dashboard
  is polled frequently against a much larger dataset

These are natural next steps once the foundation is reviewed.
