import { useState, useRef } from "react";
import { api } from "./api";
import { UploadCloud, Plus, AlertTriangle } from "lucide-react";

export default function AddExpense() {
  const [form, setForm] = useState({ date: "", amount: "", vendorName: "", description: "" });
  const [loading, setLoading] = useState(false);
  const [msg, setMsg] = useState<{ text: string; type: "success" | "error" | "warn" | "" }>({ text: "", type: "" });
  const [detectedCategory, setDetectedCategory] = useState<string | null>(null);
  const [anomalyWarning, setAnomalyWarning] = useState<string | null>(null);

  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setMsg({ text: "", type: "" });
    setDetectedCategory(null);
    setAnomalyWarning(null);

    try {
      const res = await api.post("/expenses", {
        expenseDate: form.date,
        amount: Number(form.amount),
        vendorName: form.vendorName,
        description: form.description
      });

      const saved = res.data;

      setDetectedCategory(saved.categoryName || null);

      if (saved.anomaly) {
        setAnomalyWarning("⚠ This expense looks unusually high for this category.");
      }

      setMsg({ text: "Expense added successfully!", type: "success" });
      setForm({ date: "", amount: "", vendorName: "", description: "" });
    } catch (error: any) {
      console.log(error);
      const backendMsg = error?.response?.data;
      console.log("Message", error?.response?.data);

      if (backendMsg?.toLowerCase()?.includes("duplicate")) {
        setMsg({ text: "🚫 Duplicate expense detected. This looks like an existing entry.", type: "error" });
      } else {
        setMsg({ text: "Failed to add expense", type: "error" });
      }
    } finally {
      setLoading(false);
    }
  };

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const formData = new FormData();
    formData.append("file", file);

    setLoading(true);
    setMsg({ text: "", type: "" });

    try {
      const res = await api.post("/upload", formData, {
        headers: { "Content-Type": "multipart/form-data" }
      });
      const result = res.data;
      if (result.failureCount > 0) {
        const errorSummary = result.errors
          .map((e: { rowNumber: number; reason: string }) => `Row ${e.rowNumber}: ${e.reason}`)
          .join("\n");
        setMsg({
          text: `Uploaded: ${result.successCount} succeeded, ${result.failureCount} failed.\n${errorSummary}`,
          type: result.successCount > 0 ? "warn" : "error"
        });
      } else {
        setMsg({ text: `CSV uploaded successfully! ${result.successCount} expenses imported.`, type: "success" });
      }
    } catch (error: any) {
      const msg = error?.response?.data;
      setMsg({ text: typeof msg === "string" ? msg : "Failed to upload CSV", type: "error" });
    } finally {
      setLoading(false);
      if (fileInputRef.current) fileInputRef.current.value = "";
    }
  };

  return (
    <div className="grid">

      <div className="card">
        <h2 style={{ marginBottom: "1.5rem" }}>Add Expense Manually</h2>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Date</label>
            <input
              type="date"
              className="input"
              required
              value={form.date}
              onChange={e => setForm({ ...form, date: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label>Amount</label>
            <input
              type="number"
              step="0.01"
              className="input"
              required
              placeholder="e.g. 15.50"
              value={form.amount}
              onChange={e => setForm({ ...form, amount: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label>Vendor Name</label>
            <input
              type="text"
              className="input"
              required
              placeholder="e.g. Swiggy"
              value={form.vendorName}
              onChange={e => setForm({ ...form, vendorName: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label>Description</label>
            <input
              type="text"
              className="input"
              placeholder="e.g. Lunch"
              value={form.description}
              onChange={e => setForm({ ...form, description: e.target.value })}
            />
          </div>

          <button type="submit" className="btn" style={{ width: "100%" }} disabled={loading}>
            <Plus size={18} /> {loading ? "Adding..." : "Add Expense"}
          </button>
        </form>

        {detectedCategory && (
          <div style={{ marginTop: "1rem", padding: "0.75rem", borderRadius: "8px", background: "var(--primary-surface)" }}>
            🧠 Auto-detected category: <b>{detectedCategory}</b>
          </div>
        )}

        {anomalyWarning && (
          <div style={{ marginTop: "0.75rem", padding: "0.75rem", borderRadius: "8px", background: "var(--danger-surface)", color: "#fca5a5", display: "flex", alignItems: "center", gap: "0.5rem" }}>
            <AlertTriangle size={16} /> {anomalyWarning}
          </div>
        )}
      </div>

      <div className="card" style={{ display: "flex", flexDirection: "column" }}>
        <h2 style={{ marginBottom: "1.5rem" }}>Upload Bulk Expenses</h2>
        <p style={{ color: "var(--text-secondary)", marginBottom: "1.5rem" }}>
          Upload a CSV file with columns: Date, Amount, Vendor Name, Description.
        </p>

        <input
          type="file"
          accept=".csv"
          ref={fileInputRef}
          style={{ display: "none" }}
          onChange={handleFileUpload}
        />

        <div
          className="upload-area"
          onClick={() => fileInputRef.current?.click()}
          style={{ flexGrow: 1, display: "flex", flexDirection: "column", justifyContent: "center", alignItems: "center" }}
        >
          <UploadCloud size={48} className="upload-icon" />
          <h3 style={{ marginBottom: "0.5rem" }}>Click to Upload CSV</h3>
          <p style={{ color: "var(--text-secondary)", fontSize: "0.9rem" }}>or drag and drop here</p>
        </div>

        {msg.text && (
          <div
            style={{
              marginTop: "1rem",
              padding: "1rem",
              borderRadius: "8px",
              backgroundColor: msg.type === "error" ? "var(--danger-surface)" : msg.type === "warn" ? "var(--warning-surface)" : "var(--success-surface)",
              color: msg.type === "error" ? "#fca5a5" : msg.type === "warn" ? "#fde68a" : "#6ee7b7"
            }}
          >
            {msg.text}
          </div>
        )}
      </div>
    </div>
  );
}