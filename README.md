# Mini Expense Manager

A full-stack expense management application built as a coding assignment for iConcile Technologies.

Users can record expenses manually, upload bulk expenses via CSV, view a spending dashboard, and identify unusually high expenses automatically.

---

## Technologies Used

| Layer     | Technology                          |
|-----------|-------------------------------------|
| Frontend  | React 18, TypeScript, Vite          |
| Backend   | Java 21, Spring Boot 3, Spring Data JPA, Maven |
| Database  | PostgreSQL 14+                      |
| CSV       | OpenCSV 5.9                         |

---

## Prerequisites

- **Java 21** (JDK)
- **Node.js 18+** and **npm**
- **PostgreSQL** (running on port 5432)
- **Maven Wrapper** is included — no need to install Maven separately

---

## Database Setup

Create the database in PostgreSQL:

```sql
CREATE DATABASE expense_db;
```

The schema is created automatically by Hibernate (`ddl-auto: update`) when the backend starts.

---

## Configuration

Backend database credentials are in:

```
src/main/resources/application.yaml
```

Update the following fields to match your local PostgreSQL setup:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/expense_db
    username: postgres
    password: your_password
```

---

## Running the Application

### Backend (Spring Boot)

From the project root directory:

**Windows:**
```cmd
.\mvnw.cmd spring-boot:run
```

**macOS / Linux:**
```bash
./mvnw spring-boot:run
```

The backend starts on **http://localhost:8081**

---

### Frontend (Vite + React)

```bash
cd frontend
npm install
npm run dev
```

The frontend starts on **http://localhost:5173**

---

## Features

### 1. Manual Expense Entry

Add an expense by entering:
- Date
- Amount
- Vendor Name
- Description (optional)

The category is automatically assigned based on the vendor name (see categorization rules below).
The UI shows the detected category and warns if the expense appears anomalous.

### 2. CSV Expense Upload

Upload a CSV file to import multiple expenses at once.

**Expected CSV format:**

```csv
Date,Amount,Vendor,Description
2026-08-01,250,Swiggy,Lunch
2026-08-02,1200,Amazon,Headphones
2026-08-03,350,Uber,Cab
```

- The header row (`Date,Amount,Vendor,Description`) must be present.
- Date must be in `YYYY-MM-DD` format.
- Amount must be a positive number.
- Vendor is required.
- Description is optional.
- Invalid rows are skipped with a descriptive error; valid rows are saved.
- The response indicates how many rows succeeded and which rows failed with reasons.

### 3. Rule-Based Categorization

Vendors are automatically categorized:

| Vendor      | Category      |
|-------------|---------------|
| Swiggy      | Food          |
| Zomato      | Food          |
| Uber        | Transport     |
| Ola         | Transport     |
| Amazon      | Shopping      |
| Flipkart    | Shopping      |
| Netflix     | Entertainment |
| Electricity | Bills         |
| (all others)| Other         |

Matching is case-insensitive and ignores leading/trailing spaces.

### 4. Anomaly Detection

An expense is flagged as an anomaly if its amount is **strictly greater than 3× the average amount** for its category among existing expenses.

The baseline average is calculated from expenses already saved, so the new expense does not skew its own baseline.

The first expense in a category is never flagged.

### 5. Dashboard

- **Monthly Totals by Category** — shows spending per category grouped by month and year
- **Top 5 Vendors** — vendors ranked by total spend descending
- **Flagged Anomalies** — paginated list of anomalous expenses with count

---

## API Endpoints

| Method | Endpoint         | Description                        |
|--------|------------------|------------------------------------|
| POST   | /api/expenses    | Add a single expense manually      |
| POST   | /api/upload      | Upload CSV file                    |
| GET    | /api/dashboard   | Get dashboard data (page, size)    |

---

## Ports

| Service    | Port |
|------------|------|
| Backend    | 8081 |
| Frontend   | 5173 |
| PostgreSQL | 5432 |

---

## Design Note

Vendor-to-category mapping is hard-coded in `ExpenseService` as a `Map<String, String>`. This is a deliberate simplicity trade-off for a small assignment — it keeps the code easy to read and explain. A production system would store mappings in a database table.

Anomaly detection uses the rule: `amount > 3 × avg(category)`. The average is computed from already-persisted expenses before inserting the new one, so the new expense does not influence its own baseline. The first expense in any category is never flagged because no baseline exists yet.

The data model is a single `Expense` entity with fields: id, expenseDate, amount, vendorName, categoryName, description, anomaly (boolean). The schema is auto-managed by Hibernate.

CSV parsing uses OpenCSV to correctly handle quoted fields (e.g., descriptions containing commas). Invalid rows are reported with row numbers and reasons; valid rows are saved even if others fail.

Trade-offs: no authentication, no Docker, no caching layer — these would be added in a production system but are out of scope for this assignment.
