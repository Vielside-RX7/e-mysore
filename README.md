# E-Mysore: Urban Complaint Management System

A modern, full-stack platform for citizens to file complaints about municipal issues (potholes, water leaks, garbage, etc.) and for administrators to manage, track, and resolve them efficiently. Built with **React** (frontend), **Spring Boot** (backend), and **FastAPI** (ML enrichment).

## Table of Contents

1. [Overview](#overview)
2. [Features](#features)
3. [Architecture](#architecture)
4. [Tech Stack](#tech-stack)
5. [Prerequisites](#prerequisites)
6. [Quick Start](#quick-start)
7. [Project Structure](#project-structure)
8. [Configuration](#configuration)
9. [API Documentation](#api-documentation)
10. [ML Integration](#ml-integration)
11. [Database Migrations](#database-migrations)
12. [Deployment](#deployment)
13. [Troubleshooting](#troubleshooting)

---

## Overview

E-Mysore (eMysore) is a civic complaint management platform that enables:
- **Citizens**: File complaints with photos, track status, receive updates.
- **Officers**: Manage complaints, update status, escalate issues.
- **Admins**: Dashboard analytics, department assignment, manual escalation.
- **ML Engine**: Auto-assign complaints to departments based on content analysis.

The system notifies both citizens and department contacts via email/SMS when complaints are created, updated, or escalated.

---

## Features

### Citizen Features
- 🏠 **Dashboard**: View all filed complaints and their status in real-time.
- 📋 **Complaint Form**: File a new complaint with title, description, location, and photo upload.
- 📱 **Mobile Responsive**: iOS-styled UI for seamless mobile and desktop experience.
- 🔔 **Notifications**: Receive updates when status changes or escalates.
- 🔐 **Secure Auth**: JWT-based registration and login.

### Officer Features
- 👁️ **Complaint Listing**: Browse all complaints with filtering and pagination.
- ✏️ **Status Updates**: Update complaint status with remarks.
- 🚀 **Manual Escalation**: Escalate complaints to higher authorities.

### Admin Features
- 📊 **Analytics Dashboard**: View total complaints, open, in-progress, and resolved counts.
- 🎯 **Interactive Filters**: Click metric cards to filter by status or category.
- 📄 **Complaint Details**: View full complaint history, escalation chain, and department hierarchy.
- 🔧 **Department Management**: Assign departments and set contact info.
- 🤖 **ML-Powered Assignment**: Auto-assign complaints based on department rules.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Frontend (React)                          │
│                    (http://localhost:3000)                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  Dashboard  │ ComplaintForm  │ AdminDashboard  │ AdminDetail     │
└────────────────────┬─────────────────────────────────────────────┘
                     │ (Fetch / JWT)
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│                  Backend (Spring Boot 3.5.7)                     │
│                    (http://localhost:8080)                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  Auth Controller   │ Complaint Controller  │ Admin Controller    │
│         │                    │                      │              │
│         ▼                    ▼                      ▼              │
│  AuthService       │ ComplaintService    │ DepartmentService     │
│         │                    │                      │              │
│         └────────────────────┴──────────────────────┘              │
│                              │                                      │
│                    ┌─────────┴──────────┐                          │
│                    ▼                    ▼                          │
│              MLService            NotificationService             │
│                    │                    │                          │
│                    │           ┌────────┼────────┐                │
│                    ▼           ▼                  ▼                │
│            Hibernate ORM    EmailService     SMSService            │
└────────────────────┬──────────────┬───────────────┬────────────────┘
                     │              │               │
                     ▼              ▼               ▼
        ┌─────────────────────┐  SMTP  │       SMS Gateway
        │  PostgreSQL DB      │        │
        │  (localhost:5432)   │        │
        └─────────────────────┘        │
                     │
                     ▼
        ┌──────────────────────────────────┐
        │   Flyway Migrations              │
        │  (Schema versioning)             │
        └──────────────────────────────────┘

                     │
                     ▼
        ┌──────────────────────────────────┐
        │  ML Stub (FastAPI)               │
        │  (http://localhost:8000)         │
        │  - Predict department            │
        │  - Sentiment analysis            │
        │  - Confidence scoring            │
        └──────────────────────────────────┘
```

---

## Tech Stack

### Frontend
- **React** 18+ (Create React App)
- **React Router** — Navigation and routing
- **Fetch API** — HTTP requests
- **CSS3** — iOS-styled responsive design

### Backend
- **Spring Boot 3.5.7**
- **Spring Security** — JWT authentication
- **Spring Data JPA** — ORM with Hibernate
- **PostgreSQL 15+** — Primary database
- **Flyway** — Database migrations
- **Spring Mail** — Email notifications
- **Lombok** — Code generation

### ML / ML Stub
- **FastAPI** — Python web framework
- **Pydantic** — Data validation
- **Rule-based classifier** — Department prediction with confidence scoring

### DevOps / Tools
- **Maven** — Build management
- **Docker** — Containerization (optional)
- **Git** — Version control

---

## Prerequisites

Ensure you have installed:

- **Node.js 16+** and **npm 8+** (for frontend)
- **Java 21+** (for backend)
- **Maven 3.9+** (for backend build)
- **Python 3.9+** (for ML stub)
- **PostgreSQL 15+** (database)

### Verify Installations

```bash
node --version
npm --version
java -version
mvn --version
python3 --version
psql --version
```

---

## Quick Start

### 1. Database Setup

```bash
# Create PostgreSQL database
createdb -U postgres emysore

# Verify connection
psql -U postgres -d emysore -c "SELECT version();"
```

### 2. Start the ML Stub (FastAPI)

```bash
cd e-mysore
python3 -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

pip install fastapi uvicorn pydantic
python3 ml_stub.py
```

**Output:**
```
INFO:     Uvicorn running on http://0.0.0.0:8000 (Press CTRL+C to quit)
```

The ML stub will:
- Listen on `http://localhost:8000`
- Provide `/predict` endpoint for department assignment
- Return JSON with `assigned_dept`, `department_hierarchy`, `urgency`, `sentiment`, `confidence`

### 3. Build and Start the Backend

```bash
cd ecom-mysore-backend

# Build
./mvnw clean package -DskipTests

# Run
java -jar target/ecom-mysore-backend-0.0.1-SNAPSHOT.jar
```

**Output:**
```
2025-11-13 16:09:08.123  INFO  Started EcomMysoreBackendApplication in 5.234 seconds
```

The backend will:
- Connect to PostgreSQL (`localhost:5432/emysore`)
- Run Flyway migrations (`V1`, `V2`)
- Listen on `http://localhost:8080`
- Logs written to console and `/tmp/backend.log`

### 4. Start the Frontend

```bash
cd e-mysore-frontend

npm install
npm start
```

**Output:**
```
webpack compiled successfully
On Your Network: http://192.168.x.x:3000
```

Open **http://localhost:3000** in your browser.

---

## Project Structure

```
e-mysore/
├── README.md                          # This file
├── ml_stub.py                         # FastAPI ML prediction stub
├── uploads/                           # Local complaint photo storage
│
├── e-mysore-frontend/                 # React frontend (Create React App)
│   ├── public/
│   │   ├── index.html
│   │   └── manifest.json
│   ├── src/
│   │   ├── App.js                     # Main app component with routing
│   │   ├── App.css                    # Global styles
│   │   ├── index.js                   # React root
│   │   ├── components/
│   │   │   ├── Login.jsx              # Citizen login
│   │   │   ├── Signup.jsx             # Citizen registration
│   │   │   ├── Dashboard.jsx          # Citizen dashboard
│   │   │   ├── ComplaintForm.jsx      # File new complaint
│   │   │   ├── AdminLogin.jsx         # Admin login
│   │   │   ├── AdminDashboard.jsx     # Admin analytics & complaint list
│   │   │   ├── AdminComplaintDetail.jsx # Admin complaint detail view
│   │   │   └── Navbar.jsx             # Top navigation bar
│   │   └── [CSS files for components]
│   └── package.json
│
├── ecom-mysore-backend/               # Spring Boot backend
│   ├── pom.xml                        # Maven dependencies
│   ├── mvnw                           # Maven wrapper
│   ├── DB_MIGRATIONS.md               # Flyway migration guide
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/emysore/ecom_mysore_backend/
│   │   │   │   ├── EcomMysoreBackendApplication.java
│   │   │   │   ├── config/
│   │   │   │   │   ├── SecurityConfig.java
│   │   │   │   │   ├── WebConfig.java
│   │   │   │   │   └── GlobalExceptionHandler.java
│   │   │   │   ├── controller/
│   │   │   │   │   ├── AuthController.java
│   │   │   │   │   ├── ComplaintController.java
│   │   │   │   │   └── AdminController.java
│   │   │   │   ├── model/
│   │   │   │   │   ├── User.java
│   │   │   │   │   ├── Complaint.java
│   │   │   │   │   ├── Department.java
│   │   │   │   │   ├── Notification.java
│   │   │   │   │   └── ComplaintAuditLog.java
│   │   │   │   ├── repository/
│   │   │   │   │   ├── UserRepository.java
│   │   │   │   │   ├── ComplaintRepository.java
│   │   │   │   │   ├── DepartmentRepository.java
│   │   │   │   │   ├── NotificationRepository.java
│   │   │   │   │   └── ComplaintAuditLogRepository.java
│   │   │   │   ├── service/
│   │   │   │   │   ├── AuthService.java
│   │   │   │   │   ├── ComplaintService.java
│   │   │   │   │   ├── MLService.java
│   │   │   │   │   ├── NotificationService.java
│   │   │   │   │   ├── EmailService.java
│   │   │   │   │   ├── SMSService.java
│   │   │   │   │   └── FileStorageService.java
│   │   │   │   └── security/
│   │   │   │       ├── JwtAuthenticationFilter.java
│   │   │   │       └── JwtTokenProvider.java
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── db/migration/
│   │   │           ├── V1__Initial_Schema.sql
│   │   │           └── V2__Add_Department_Hierarchy.sql
│   │   └── test/
│   │       └── java/...
│   └── target/
│       └── ecom-mysore-backend-0.0.1-SNAPSHOT.jar
```

---

## Configuration

### Backend Configuration (`application.properties`)

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/emysore
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Flyway Migrations
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baselineOnMigrate=true

# Server
server.port=8080

# ML Service
ml.service.url=http://localhost:8000
ml.service.timeout=5000

# Email (update with your SMTP credentials)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-specific-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# SMS (update with your SMS gateway)
sms.api.key=your-sms-gateway-api-key
sms.api.url=https://your-sms-gateway-url.com/api/v1/send
```

### Frontend Environment Variables (`.env` in `e-mysore-frontend/`)

```bash
REACT_APP_API_URL=http://localhost:8080
REACT_APP_ML_URL=http://localhost:8000
```

### ML Stub Configuration

The ML stub is configured in `ml_stub.py`:
- **Host**: `0.0.0.0`
- **Port**: `8000`
- **Endpoint**: `/predict`

To use a different port:
```python
if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=9000)  # Change port here
```

---

## API Documentation

### Authentication

#### Register a Citizen
```bash
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "password": "secure_password",
  "email": "john@example.com",
  "phone": "9876543210"
}

Response: 200 OK
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "role": "CITIZEN"
}
```

#### Login
```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "secure_password"
}

Response: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "username": "john_doe",
  "role": "CITIZEN"
}
```

#### Register Admin
```bash
POST /api/auth/register-admin
Content-Type: application/json

{
  "username": "admin",
  "password": "1234",
  "email": "admin@emysore.local",
  "phone": "9000000000"
}

Response: 200 OK
{
  "id": 2,
  "username": "admin",
  "role": "ADMIN"
}
```

### Complaints

#### Create Complaint (with file upload)
```bash
POST /api/complaints
Authorization: Bearer <token>
Content-Type: multipart/form-data

Fields:
- title (string, required)
- description (string)
- category (string)
- location (string)
- image (file, optional)

Response: 200 OK
{
  "id": 5,
  "title": "Pothole on Main St",
  "description": "Large pothole near bus stop",
  "category": "ROAD",
  "status": "PENDING",
  "assigned_dept": "MCC – Engineering (Roads) Section",
  "department_hierarchy": "AE > JE > EE > Commissioner",
  "urgency": "MEDIUM",
  "sentiment": "NEUTRAL",
  "confidence": 0.95,
  "createdAt": "2025-11-13T20:30:00"
}
```

#### List Complaints
```bash
GET /api/complaints?page=0&size=20
Authorization: Bearer <token>

Response: 200 OK
{
  "content": [
    {
      "id": 5,
      "title": "Pothole on Main St",
      "status": "PENDING",
      "createdAt": "2025-11-13T20:30:00",
      "user": {"id": 1, "username": "john_doe"}
    }
  ],
  "pageable": {...},
  "totalElements": 10,
  "totalPages": 1
}
```

#### Get Complaint by ID
```bash
GET /api/complaints/{id}
Authorization: Bearer <token>

Response: 200 OK
{
  "id": 5,
  "title": "Pothole on Main St",
  "description": "...",
  "status": "PENDING",
  "assigned_dept": "MCC – Engineering (Roads) Section",
  "department_hierarchy": "AE > JE > EE > Commissioner",
  "escalated": false,
  "createdAt": "2025-11-13T20:30:00"
}
```

#### Update Complaint Status
```bash
PATCH /api/complaints/{id}/status
Authorization: Bearer <token>
Content-Type: application/json

{
  "status": "IN_PROGRESS",
  "remarks": "Forwarded to MCC Engineering"
}

Response: 200 OK
{
  "id": 5,
  "status": "IN_PROGRESS",
  "remarks": "Forwarded to MCC Engineering"
}
```

#### Escalate Complaint
```bash
POST /api/complaints/{id}/escalate
Authorization: Bearer <token>
Content-Type: application/json

{
  "escalate": true
}

Response: 200 OK
{
  "id": 5,
  "escalated": true
}
```

#### Get Complaint Audit Log
```bash
GET /api/complaints/{id}/audit
Authorization: Bearer <token>

Response: 200 OK
[
  {
    "action": "CREATED",
    "oldValue": "",
    "newValue": "PENDING",
    "comment": "Complaint created",
    "timestamp": "2025-11-13T20:30:00",
    "user": {"username": "john_doe"}
  },
  {
    "action": "STATUS_UPDATED",
    "oldValue": "PENDING",
    "newValue": "IN_PROGRESS",
    "comment": "Forwarded to MCC Engineering",
    "timestamp": "2025-11-13T21:00:00",
    "user": {"username": "officer1"}
  }
]
```

### Admin

#### Get Dashboard Analytics
```bash
GET /api/admin/analytics
Authorization: Bearer <admin-token>

Response: 200 OK
{
  "total_complaints": 42,
  "open": 15,
  "in_progress": 18,
  "resolved": 9,
  "by_category": {
    "ROAD": 12,
    "WATER": 8,
    "WASTE": 10,
    "OTHER": 12
  }
}
```

---

## ML Integration

### Overview

The ML stub is a **FastAPI** service that enriches complaints with:
- **Department Assignment**: Predicts which department should handle the complaint.
- **Urgency Level**: HIGH / MEDIUM / LOW based on keywords.
- **Sentiment Analysis**: POSITIVE / NEGATIVE / NEUTRAL.
- **Confidence Score**: 0.0 to 1.0 confidence in the prediction.

### ML Stub Endpoint

**POST /predict**

Request:
```json
{
  "text": "There is a large pothole on Main Street causing accidents",
  "category": "ROAD"
}
```

Response:
```json
{
  "category": "ROAD",
  "priority": 1,
  "estimated_resolution_time": 24,
  "assigned_dept": "MCC – Engineering (Roads) Section",
  "department_hierarchy": ["Assistant Engineer (AE)", "Junior Engineer (JE)", "MCC Executive Engineer (EE)", "MCC Commissioner", "PWD"],
  "urgency": "HIGH",
  "sentiment": "NEGATIVE",
  "confidence": 0.95
}
```

### How it Works

1. **Complaint Creation**: User files a complaint.
2. **ML Enrichment**: Backend calls `MLService.enrichComplaint(complaint)`.
3. **ML Stub Called**: `MLService` makes HTTP POST to `http://localhost:8000/predict`.
4. **Prediction**: Stub returns department, urgency, sentiment, confidence.
5. **Persistence**: Backend stores these values in the `complaints` table:
   - `assigned_dept` (VARCHAR 255)
   - `department_hierarchy` (TEXT, joined with " > ")
   - `urgency` (VARCHAR 50)
   - `sentiment` (VARCHAR 50)
   - `confidence_score` (DOUBLE PRECISION)
6. **Notification**: If `assigned_dept` is set, `NotificationService` notifies the department contact.

### Improving the ML Stub

The current stub uses rule-based classification with keywords. To improve:

1. **Add More Rules**: Update `ml_stub.py` with additional keywords and departments.
2. **Train a Real Model**: Collect labeled complaints and train a classification model (scikit-learn, transformers).
3. **Deploy as Service**: Use Docker to containerize and scale the ML service.

### Testing the ML Stub

```bash
# Start the stub
python3 ml_stub.py

# In another terminal, test the endpoint
curl -s -X POST http://localhost:8000/predict \
  -H 'Content-Type: application/json' \
  -d '{
    "text": "Pothole on Main Street causing accidents",
    "category": "ROAD"
  }' | jq

# Example response:
{
  "category": "ROAD",
  "priority": 1,
  "estimated_resolution_time": 24,
  "assigned_dept": "MCC – Engineering (Roads) Section",
  "department_hierarchy": ["AE", "JE", "EE", "Commissioner"],
  "urgency": "HIGH",
  "sentiment": "NEGATIVE",
  "confidence": 0.95
}
```

---

## Database Migrations

E-Mysore uses **Flyway** for version-controlled database migrations. See [DB_MIGRATIONS.md](ecom-mysore-backend/DB_MIGRATIONS.md) for detailed documentation.

### Current Migrations

- **V1__Initial_Schema.sql** — Creates users, complaints, audit logs, notifications, departments tables.
- **V2__Add_Department_Hierarchy.sql** — Adds `department_hierarchy` column to complaints.

### Adding a New Migration

1. Create a new file in `ecom-mysore-backend/src/main/resources/db/migration/`:
   ```bash
   V3__Add_User_Status_Field.sql
   ```

2. Write the SQL:
   ```sql
   ALTER TABLE users ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'ACTIVE';
   ```

3. Rebuild and restart the backend. Flyway will apply the migration automatically.

---

## Deployment

### Docker (Optional)

#### Build Backend Docker Image

```bash
cd ecom-mysore-backend

# Create Dockerfile
cat > Dockerfile << EOF
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/ecom-mysore-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
EOF

# Build image
docker build -t emysore-backend:latest .

# Run container
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/emysore \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  emysore-backend:latest
```

#### Build Frontend Docker Image

```bash
cd e-mysore-frontend

# Create Dockerfile
cat > Dockerfile << EOF
FROM node:18-alpine
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm install
COPY . .
RUN npm run build
EXPOSE 3000
CMD ["npm", "start"]
EOF

# Build image
docker build -t emysore-frontend:latest .

# Run container
docker run -p 3000:3000 emysore-frontend:latest
```

### Kubernetes (Advanced)

Create `k8s/` directory with manifests for deployment, service, configmap, secrets. Consult your Kubernetes documentation for specifics.

---

## Troubleshooting

### Frontend Issues

#### Port 3000 already in use
```bash
# Kill the process using port 3000
lsof -ti:3000 | xargs kill -9

# Or use a different port
PORT=3001 npm start
```

#### Dependencies not installing
```bash
# Clear npm cache
npm cache clean --force

# Remove node_modules and reinstall
rm -rf node_modules package-lock.json
npm install
```

### Backend Issues

#### Port 8080 already in use
```bash
# Find and kill the process
lsof -ti:8080 | xargs kill -9

# Or use a different port in application.properties
server.port=8081
```

#### Database connection error
```
ERROR: could not translate host name "localhost" to address
```

**Solution**: Ensure PostgreSQL is running and the database exists.
```bash
# Start PostgreSQL (macOS)
brew services start postgresql

# Create database
createdb -U postgres emysore
```

#### Flyway migration fails
```
ERROR FlywayException: Unable to execute migration
```

**Solution**: Check the migration file for SQL syntax errors and review logs.
```sql
-- Check if table exists before creating
CREATE TABLE IF NOT EXISTS users (...);

-- Check if column exists before altering
ALTER TABLE complaints ADD COLUMN IF NOT EXISTS new_column VARCHAR(255);
```

### ML Stub Issues

#### ML service not responding
```
ConnectionRefusedException: Connection refused
```

**Solution**: Start the ML stub before the backend.
```bash
# Terminal 1
python3 ml_stub.py

# Terminal 2
cd ecom-mysore-backend && java -jar target/...jar
```

#### ML predictions not applied to complaints
```
MLService: Failed to enrich complaint with ML analysis
```

**Solution**: Check that the ML stub is running and accessible from the backend machine.
```bash
# From backend machine, test ML endpoint
curl http://localhost:8000/predict
```

---

## Security Considerations

⚠️ **Important for Production**

1. **Update SMTP Credentials**: Replace placeholder email/password in `application.properties` with real credentials.
2. **Update SMS Credentials**: Set real SMS gateway API key and URL.
3. **JWT Secret**: Update JWT secret in `JwtTokenProvider.java` to a strong, random value.
4. **Database Password**: Change default PostgreSQL password.
5. **Enable HTTPS**: Configure SSL/TLS for frontend and backend.
6. **Rate Limiting**: Implement rate limiting on API endpoints.
7. **CORS**: Configure CORS for production domains in `WebConfig.java`.
8. **Security Headers**: Add security headers (CSP, X-Frame-Options, etc.).

---

## Contributing

1. Clone the repository.
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit changes: `git commit -am 'Add your feature'`
4. Push to branch: `git push origin feature/your-feature`
5. Open a pull request.

---

## License

This project is licensed under the MIT License. See LICENSE file for details.

---

## Support

For issues, questions, or feature requests, please:
- Open an issue on GitHub.
- Check the [Troubleshooting](#troubleshooting) section.
- Review logs at `/tmp/backend.log` for backend issues.

---

## Changelog

### Version 1.0.0 (November 13, 2025)

**Features:**
- ✅ Citizen complaint filing with photo upload.
- ✅ Real-time dashboard and complaint tracking.
- ✅ Admin analytics and complaint management.
- ✅ ML-powered department auto-assignment.
- ✅ Email and SMS notifications.
- ✅ Complaint escalation workflow.
- ✅ Audit logs and complaint history.
- ✅ Database versioning with Flyway.

**Known Limitations:**
- SMS service currently stubbed (logs only).
- ML classifier is rule-based (not ML model trained).
- S3 file storage is optional (uses local uploads).

---

**Built with ❤️ for urban civic engagement.**
