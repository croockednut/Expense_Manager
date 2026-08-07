Mini Expense Manager
Tech Stack
React + TypeScript, Spring Boot, PostgreSQL

Setup

Frontend:
Create React app (Vite)
Place files from frontend folder

npm install 
npm start

Backend:
Create DB: expense_db

Update credentials in application.properties
Run Spring Boot app (port 8081)



Design Note

I implemented rule-based categorization using a simple vendor-to-category mapping in the service layer (e.g., Swiggy → Food).  

During expense creation and CSV upload, the vendor name is matched and the category is assigned automatically.  

For anomaly detection, I calculate the average expense amount per category from the database and flag any expense that is more than 3× this average.  

The data model is kept simple with a single Expense table storing date, amount, vendor, category, and anomaly flag.  This keeps queries and logic easy to understand for a small application.  

As a trade-off, CSV parsing is basic (comma-separated without advanced validation), and the categorization rules are hard-coded instead of stored in a separate table.  

The UI and validations are minimal since the focus is on backend logic and overall approach.