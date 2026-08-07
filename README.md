# Mini Expense Manager

A full-stack expense management application that allows users to record daily expenses, upload expenses through CSV, automatically categorize expenses based on vendors, detect unusually high expenses, and view spending summaries through a dashboard.

## Tech Stack

### Frontend
- React
- TypeScript
- Vite

### Backend
- Java
- Spring Boot
- Spring Data JPA
- Maven

### Database
- PostgreSQL

---

## Features

### 1. Manual Expense Entry

Users can add an expense using:

- Date
- Amount
- Vendor Name
- Description

The expense category is automatically determined based on the vendor name.

For example:

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

Vendors that do not match a predefined rule are categorized as `Other`.

Vendor matching is case-insensitive.

---

### 2. CSV Expense Upload

The application supports uploading multiple expenses through a CSV file.

Expected CSV format:

```csv
Date,Amount,Vendor,Description
2026-08-01,250,Swiggy,Lunch
2026-08-02,1200,Amazon,Headphones
2026-08-03,350,Uber,Cab
