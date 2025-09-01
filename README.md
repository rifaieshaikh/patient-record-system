# Patient Record Saga (Temporal) — Maps to 3-API Transaction with Compensations

This repo implements a **Temporal Saga** orchestrating **three external APIs** with:
- a **global transaction timeout**,
- **compensating actions** when any step fails or when the timeout is exceeded,
- a **REST endpoint** to trigger the workflow,
- **Docker Compose** to reproduce Temporal + dependencies locally,
- **tests** covering success, failure, and timeout branches.

Although the domain is **patient onboarding**, it directly maps to the **original generic requirement** of "3 external API calls with Saga + timeout + compensations." See the mapping table below.

---

## 1) Architecture (1-minute tour)

### Workflow
`CreatePatientWorkflow` orchestrates three steps:

1. **API-A** — *Create Patient Core Record*
  - External call (e.g., `POST /patients`).
  - **Compensation:** delete/rollback the created patient record (e.g., `DELETE /patients/{id}`).

2. **API-B** — *Create Medical Record*
  - External call (e.g., `POST /medical-records`).
  - **Compensation:** delete/rollback the medical record.

3. **API-C** — *Create Insurance Details*
  - External call (e.g., `POST /insurance-details`).
  - **Compensation:** delete/rollback the Insurance Details.

All three steps must complete **within `TRANSACTION_TIMEOUT_SECONDS`**; otherwise the workflow triggers **saga rollbacks** for completed steps.

### Activities
- `CreatePatientActivity` (and its `...Impl`)
- `TransactionRequestActivity` (and its `...Impl`)
- (If split in your code: `MedicalRecordActivity`)

> Each activity has a **do** and a **compensate** method. Compensations are applied in **reverse order** of completed steps, per the Saga pattern.

### REST Trigger
- `POST /api/transactions/create` launches the workflow with a `PatientDto` (and nested `MedicalRecordDto`, `InsuranceDetailDto`).

---

## 2) Mapping to the Original Requirement

| Requirement (original) | This repo (domain)                                                       | Notes                                                                       |
|---|--------------------------------------------------------------------------|-----------------------------------------------------------------------------|
| 3 external APIs | A: Create Patient, B: Create Medical Record, C: Create Insurance Details | Replace endpoints with your actual curdcrud.com URLs.                       |
| Compensations | Delete patient, Delete medical record, Delete insurance details          | Implemented per step; called in reverse on failure/timeout.                 |
| Global timeout | `TRANSACTION_TIMEOUT_SECONDS` (workflow run timeout)                     | Set via env/properties; exceed => saga rollback.                            |
| Trigger via REST | `POST /api/transactions/create`                                          | Accepts `PatientDto` (+ nested DTOs).                                       |
| Docker-compose reproducible | `docker-compose.yml` with Temporal + UI + MongoDB + http + worker        | Bring up infra locally; configure endpoints via env.                        |
| Tests for success/failure/timeout | JUnit/Mockito + `TestWorkflowExtension` (Temporal SDK)                   | Includes unit & E2E-style tests demonstrating compensation & timeout paths. |

---

## 📋 Prerequisites

### Required Software

- **Java 21** or higher
- **Maven 3.8+**
- **Docker** and **Docker Compose**
- **Git**
- **IDE** (IntelliJ IDEA or VS Code recommended)

### System Requirements

- **RAM**: Minimum 8GB (16GB recommended)
- **Disk Space**: At least 10GB free
- **OS**: Windows 10/11, macOS, or Linux

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/rifaieshaikh/patient-record-system.git
cd patient-record-system
```

### 2. Build the Project

```bash
mvn clean package
```

### 3. Start All Services with Docker Compose

Start all services including infrastructure and applications:

```bash
docker-compose up -d
```

This will start:
- **Temporal Server** on `localhost:7233`
- **Temporal Web UI** on `http://localhost:8088`
- **MongoDB** on `localhost:27017`
- **PostgreSQL** on `localhost:5432`
- **HTTP Service** on `http://localhost:8080`
- **Worker Service** on `http://localhost:8081`

Wait for all services to be healthy (approximately 1-2 minutes):
```bash
docker-compose ps
```

All services should show as "healthy" or "running".

### 4. Verify Services

- **HTTP API Health**: `http://localhost:8080/actuator/health`
- **Worker Health**: `http://localhost:8081/actuator/health`
- **Temporal UI**: `http://localhost:8088`

Check logs to ensure services are running correctly:
```bash
# View all logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f http
docker-compose logs -f worker
docker-compose logs -f temporal
```

### 5. Alternative: Local Development (Without Docker)

If you prefer to run services locally for development:

#### Start Infrastructure Only
```bash
# Start only infrastructure services
docker-compose up -d postgres mongo temporal temporal-ui
```

#### Run Applications Locally

Terminal 1 - Start Worker:
```bash
cd worker
mvn spring-boot:run
```

Terminal 2 - Start HTTP Service:
```bash
cd http
mvn spring-boot:run
```

Note: When running locally, update application.yml files to use localhost instead of container names.

## 📊 Database Configuration

### MongoDB
- **Host**: localhost (or `mongo` within Docker network)
- **Port**: 27017
- **Database**: prs
- **Collections**:
  - transaction_requests

### PostgreSQL
- **Host**: localhost (or `postgres` within Docker network)
- **Port**: 5432
- **Database**: temporal
- **Username**: temporal
- **Password**: temporal

Tables:
- `patient_records`
- `medical_records`
- `insurance_details`

## 🔧 Configuration Files

### Docker Environment Variables

The docker-compose.yml configures services with these environment variables:

**HTTP Service**:
- `MONGO_URI`: mongodb://mongo:27017
- `MONGO_DB`: prs
- `TEMPORAL_TARGET`: temporal:7233
- `TEMPORAL_CREATE_PATIENT_TASK_QUEUE`: prs-create-patient-task-queue
- `TRANSACTION_TIMEOUT_SECONDS`: 60

**Worker Service**:
- Same as HTTP Service

### Application Properties (Local Development)

**HTTP Service** (`http/src/main/resources/application.yml`):
```yaml
server:
  port: 8080

spring:
  data:
    mongodb:
      uri: ${MONGO_URI:mongodb://localhost:27017}
      database: ${MONGO_DB:prs}

temporal:
  namespace: default
  service-address: ${TEMPORAL_TARGET:localhost:7233}
  workflow:
    task-queue: ${TEMPORAL_CREATE_PATIENT_TASK_QUEUE:prs-create-patient-task-queue}
```

**Worker Service** (`worker/src/main/resources/application.yml`):
```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/temporal
    username: temporal
    password: temporal
  
  data:
    mongodb:
      uri: ${MONGO_URI:mongodb://localhost:27017}
      database: ${MONGO_DB:prs}

temporal:
  namespace: default
  service-address: ${TEMPORAL_TARGET:localhost:7233}
  workflow:
    task-queue: ${TEMPORAL_CREATE_PATIENT_TASK_QUEUE:prs-create-patient-task-queue}
```

## 🐳 Docker Commands

### Starting Services

```bash
# Start all services
docker-compose up -d

# Start with build (if code changed)
docker-compose up -d --build

# Start specific services
docker-compose up -d temporal mongo postgres

# View logs
docker-compose logs -f

# Stop all services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

### Container Management

```bash
# List running containers
docker-compose ps

# Execute commands in container
docker exec -it prs-http sh
docker exec -it prs-worker sh

# View container logs
docker logs prs-http -f
docker logs prs-worker -f

# Restart specific service
docker-compose restart http
docker-compose restart worker
```

## 📡 API Endpoints

### Base URL
- **Docker**: `http://localhost:8080`
- **Local Development**: `http://localhost:8080`

### Create Patient Transaction

**POST** `/api/v1/transaction-requests`

Request Body:
```json
{
  "patient": {
    "firstName": "Ashaaaa",
    "lastName": "Varma",
    "dateOfBirth": "1995-05-10",
    "gender": "F",
    "phoneNumber": "9876543210",
    "email": "asha.varma@example.com",
    "address": "Tirur, Malappuram, Kerala, India",
    "bloodGroup": "O+",
    "medicalRecord": {
      "allergies": "Penicillin",
      "chronicConditions": "Hypertension",
      "currentMedications": "Amlodipine",
      "pastSurgeries": "Appendectomy (2015)",
      "emergencyContact": "Rahul Varma - +91-99999-88888"
    },
    "insuranceDetail": {
      "providerName": "ABC Health Insurance",
      "policyNumber": "POL12345",
      "coverageType": "Comprehensive",
      "coverageLimit": 500000,
      "validFrom": "2025-01-01",
      "validTo": "2026-01-01",
      "claimContact": "+91-1800-111-222"
    }
  },
  "transactionType": "CREATE"
}
```

Response:
```json
{
  "requestId": "507f1f77bcf86cd799439011",
  "workflowId": "create-793e3282-9ec8-4a9b-8951-5f2d67fccfde",
  "status": "INITIATED"
}
```

## 🧪 Running Tests

### Unit Tests
```bash
mvn test
```

#### In-Memory Tests (Fast)
```bash
mvn test -pl worker -Dtest=CreatePatientWorkflowImplTest
```
## 🔍 Monitoring & Debugging

### Temporal Web UI

Access at `http://localhost:8080` to:
- View workflow executions
- Monitor workflow status
- Debug failed workflows
- Replay workflow history

### Application Logs

- **Worker logs**: Check for activity execution and workflow status
- **HTTP logs**: Monitor API requests and responses
- **Temporal logs**: Available in Docker container logs

View Docker logs:
```bash
docker-compose logs -f temporal
docker-compose logs -f mongodb
docker-compose logs -f postgres
```

### Health Checks

- **HTTP Service**: `http://localhost:8080/actuator/health`
- **Worker Service**: `http://localhost:8081/actuator/health`