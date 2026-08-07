# Mini Expense Manager

Users can record expenses manually, upload multiple expenses through CSV, automatically categorize expenses based on vendor names, detect unusually high expenses, and view spending summaries through a dashboard.

---

## Technologies Used

| Layer | Technology |
|---|---|
| Frontend | React 18, TypeScript, Vite |
| Backend | Java 21, Spring Boot 3, Spring Data JPA, Maven |
| Database | PostgreSQL 14+ |
| CSV Processing | OpenCSV 5.9 |

---
Demo
https://drive.google.com/file/d/1ZuiWQvMns0Ur1LOXaGmpULwbi76xWknw/view?usp=sharing
## Prerequisites

Make sure the following are installed before running the application:

- **Java 21** (JDK)
- **Node.js 18+** and **npm**
- **PostgreSQL** running on port `5432`
- Maven is not required separately if the Maven Wrapper files are present in the repository

---

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/croockednut/Expense_Manager
cd Expense_Manager
```

### 2. Database Setup

Create a PostgreSQL database named `expense_db`:

```sql
CREATE DATABASE expense_db;
```

The required database table is created automatically by Hibernate when the backend starts.

### 3. Configure the Backend

Open:

```text
src/main/resources/application.yaml
```

Update the PostgreSQL credentials according to your local setup:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/expense_db
    username: postgres
    password: your_password
```

Replace `your_password` with your local PostgreSQL password.

> Do not commit real database credentials to a public repository.

### 4. Run the Backend

From the project root directory:

#### Windows

```cmd
.\mvnw.cmd spring-boot:run
```

#### macOS / Linux

```bash
./mvnw spring-boot:run
```

The Spring Boot backend will start on:

```text
http://localhost:8081
```

### 5. Run the Frontend

Open a new terminal and navigate to the frontend directory:

```bash
cd frontend
```

Install the dependencies:

```bash
npm install
```

Start the Vite development server:

```bash
npm run dev
```

The frontend will be available at:

```text
http://localhost:5173
```

Open the above URL in a browser to use the application.

---

## Features

### 1. Manual Expense Entry

Users can add an expense using:

- Date
- Amount
- Vendor Name
- Description (optional)

The category is automatically assigned based on the vendor name.

The UI displays the detected category and indicates if the expense is flagged as an anomaly.

---

### 2. CSV Expense Upload

The application supports uploading multiple expenses through a CSV file.

#### Expected CSV Format

```csv
Date,Amount,Vendor,Description
2026-08-01,250,Swiggy,Lunch
2026-08-02,1200,Amazon,Headphones
2026-08-03,350,Uber,Cab
```

CSV requirements:

- The header row `Date,Amount,Vendor,Description` must be present.
- Date must be in `YYYY-MM-DD` format.
- Amount must be a positive number.
- Vendor is required.
- Description is optional.
- Quoted CSV fields containing commas are supported.
- Invalid rows are skipped and reported with their row number and reason.
- Valid rows are saved even when other rows in the same file are invalid.
- The upload response indicates the number of successfully processed rows and any failed rows.

---

### 3. Rule-Based Categorization

Expenses are automatically categorized using a vendor-to-category mapping.

| Vendor | Category |
|---|---|
| Swiggy | Food |
| Zomato | Food |
| Uber | Transport |
| Ola | Transport |
| Amazon | Shopping |
| Flipkart | Shopping |
| Netflix | Entertainment |
| Electricity | Bills |
| All other vendors | Other |

Vendor matching is **case-insensitive** and ignores leading/trailing spaces.

The same categorization logic is applied to both manually added expenses and CSV-uploaded expenses.

---

### 4. Anomaly Detection

An expense is flagged as an anomaly when:

```text
Amount > 3 × Average Amount for its Category
```

The average is calculated using expenses already stored in that category before the new expense is inserted.

Therefore, the new expense does not influence its own anomaly baseline.

The first expense in a category is not flagged because there is no previous baseline available.

Flagged expenses are displayed distinctly in the UI.

---

### 5. Dashboard

The dashboard provides:

- **Monthly Totals by Category** — spending grouped by month, year, and category
- **Top 5 Vendors** — vendors ranked by total spending
- **Flagged Anomalies** — count and paginated list of anomalous expenses

---

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/expenses` | Add a single expense manually |
| POST | `/api/upload` | Upload and process a CSV file |
| GET | `/api/dashboard` | Retrieve dashboard data with pagination parameters |

---

## Ports

| Service | Port |
|---|---:|
| Frontend | 5173 |
| Backend | 8081 |
| PostgreSQL | 5432 |

---

## Design Note

Vendor-to-category mapping is implemented as a `Map<String, String>` in `ExpenseService`, with rules such as `Swiggy → Food`. This keeps the categorization logic simple and easy to understand for the scope of the assignment. The same mapping is applied during both manual expense creation and CSV processing. Anomaly detection uses the rule `amount > 3 × average(category)`, where the average is calculated from already-persisted expenses before inserting the new expense. The first expense in a category is not flagged because no historical baseline exists. The data model uses a single `Expense` entity containing the ID, date, amount, vendor, category, description, and anomaly flag. Hibernate automatically manages the database schema. CSV processing uses OpenCSV to support quoted fields and reports invalid rows with their row numbers and reasons while allowing valid rows to be saved. A production implementation could move vendor mappings into a database table and add authentication, caching, and containerization, but these were intentionally kept out of scope.

---

## Assumptions & Trade-offs

- Unknown vendors are categorized as **Other**.
- Vendor matching is case-insensitive and ignores leading/trailing whitespace.
- Anomaly detection uses only previously stored expenses when calculating the category average.
- The first expense in a category is not considered an anomaly.
- CSV dates are expected in `YYYY-MM-DD` format.
- CSV processing supports quoted fields while following the expected four-column structure.
- Valid CSV rows are saved even if other rows in the same file are invalid.
- Vendor-category mappings are maintained in application code rather than a separate database table to keep the implementation lightweight.
- Authentication and multi-user functionality are outside the scope of this assignment.
- Docker and caching are not included because they are not required for the core assignment.

---

## Project Structure

```text
Mini-Expense-Manager/
│
├── frontend/
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── package-lock.json
│
├── src/
│   └── main/
│       ├── java/
│       └── resources/
│           └── application.yaml
│
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .mvn/
├── README.md
└── ...
```

---

## Running the Application

After completing the setup:

### Terminal 1 — Backend

```cmd
.\mvnw.cmd spring-boot:run
```

### Terminal 2 — Frontend

```bash
cd frontend
npm run dev
```

Then open:

```text
http://localhost:5173
```

The frontend communicates with the Spring Boot backend running on port `8081`.
