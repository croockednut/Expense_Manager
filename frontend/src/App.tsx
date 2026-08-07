import { useState } from "react";
import AddExpense from "./AddExpense";
import Dashboard from "./Dashboard";
import { LayoutDashboard, Receipt } from "lucide-react";

export default function App() {
  const [activeTab, setActiveTab] = useState<"dashboard" | "add">("dashboard");

  return (
    <div className="container">
      <header className="header">
        <h1>Mini Expense Manager</h1>
      </header>

      <div className="tabs">
        <button
          className={`tab ${activeTab === "dashboard" ? "active" : ""}`}
          onClick={() => setActiveTab("dashboard")}
        >
          <LayoutDashboard size={18} style={{ display: "inline", verticalAlign: "middle", marginRight: "8px" }} />
          Dashboard
        </button>
        <button
          className={`tab ${activeTab === "add" ? "active" : ""}`}
          onClick={() => setActiveTab("add")}
        >
          <Receipt size={18} style={{ display: "inline", verticalAlign: "middle", marginRight: "8px" }} />
          Manage Expenses
        </button>
      </div>

      <main>
        {activeTab === "dashboard" ? <Dashboard /> : <AddExpense />}
      </main>
    </div>
  );
}
