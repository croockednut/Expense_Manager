import { useEffect, useState } from "react";
import { api } from "./api";
import { AlertTriangle, TrendingUp, ShoppingBag, Activity } from "lucide-react";

interface DashboardData {
  monthlyTotalsByCategory?: Record<string, number>;
  topVendors?: Array<{ name: string; total: number }>;
  anomalies?: Array<{
    id?: number;
    expenseDate: string;
    amount: number;
    vendorName: string;
    categoryName: string;
    description: string;
  }>;
}

export default function Dashboard() {
  const [data, setData] = useState<DashboardData>({
    monthlyTotalsByCategory: {},
    topVendors: [],
    anomalies: []
  });
  const [loading, setLoading] = useState(true);
  const [anomalyCount, setAnomalyCount] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [anomaliesLoading, setAnomaliesLoading] = useState(false);

  useEffect(() => {
    api.get("/dashboard?page=0&size=5")
      .then(r => {
        const apiData = r.data;

        const monthlyTotalsByCategory = apiData.categories.reduce(
          (acc: Record<string, number>, [, , category, amount]: [number, number, string, number]) => {
            acc[category] = amount;
            return acc;
          },
          {}
        );

        const topVendors = apiData.vendors.map((v: [string, number]) => ({
          name: v[0],
          total: v[1]
        }));

        setData({
          monthlyTotalsByCategory,
          topVendors,
          anomalies: apiData.anomalies || []
        });

        setAnomalyCount(apiData.anomalyCount || 0);
        setTotalPages(apiData.totalPages || 0);
        setCurrentPage(apiData.currentPage || 0);
      })
      .catch(err => console.error("Dashboard API failed", err))
      .finally(() => setLoading(false));
  }, []);

  const loadAnomalies = (page: number) => {
    setAnomaliesLoading(true);

    api.get(`/dashboard?page=₹{page}&size=5`)
      .then(r => {
        const apiData = r.data;

        setData(prev => ({
          ...prev,
          anomalies: apiData.anomalies || []
        }));

        setCurrentPage(apiData.currentPage);
        setTotalPages(apiData.totalPages);
        setAnomalyCount(apiData.anomalyCount);
      })
      .finally(() => setAnomaliesLoading(false));
  };

  if (loading) {
    return (
      <div style={{ textAlign: "center", padding: "3rem", color: "var(--text-secondary)" }}>
        Loading dashboard...
      </div>
    );
  }

  const cats = data.monthlyTotalsByCategory || {};
  const vendors = data.topVendors || [];
  const anomalies = data.anomalies || [];

  return (
    <div>
      <div className="grid">
        <div className="card stat-card">
          <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", color: "var(--success-color)", marginBottom: "0.5rem" }}>
            <Activity size={20} />
            <span className="stat-label">Total Categories</span>
          </div>
          <div className="stat-value">{Object.keys(cats).length}</div>
        </div>

        <div className="card stat-card">
          <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", color: "var(--primary-color)", marginBottom: "0.5rem" }}>
            <ShoppingBag size={20} />
            <span className="stat-label">Top Vendor</span>
          </div>
          <div className="stat-value">{vendors.length > 0 ? vendors[0].name : "N/A"}</div>
        </div>

        <div className="card stat-card">
          <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", color: "var(--danger-color)", marginBottom: "0.5rem" }}>
            <AlertTriangle size={20} />
            <span className="stat-label">Detected Anomalies</span>
          </div>
          <div className="stat-value">{anomalyCount}</div>
        </div>
      </div>

      <div className="grid" style={{ gridTemplateColumns: "1fr 1fr" }}>
        <div className="card">
          <h3 style={{ marginBottom: "1.5rem", display: "flex", alignItems: "center", gap: "0.5rem" }}>
            <TrendingUp size={18} /> Monthly Totals by Category
          </h3>

          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Category</th>
                  <th style={{ textAlign: "right" }}>Total Amount</th>
                </tr>
              </thead>
              <tbody>
                {Object.entries(cats).length === 0 ? (
                  <tr><td colSpan={2} style={{ textAlign: "center", color: "var(--text-secondary)" }}>No data</td></tr>
                ) : Object.entries(cats).map(([cat, total]) => (
                  <tr key={cat}>
                    <td><span className="badge badge-primary">{cat}</span></td>
                    <td style={{ textAlign: "right", fontWeight: "600" }}>₹{Number(total).toFixed(2)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        <div className="card">
          <h3 style={{ marginBottom: "1.5rem", display: "flex", alignItems: "center", gap: "0.5rem" }}>
            <ShoppingBag size={18} /> Top 5 Vendors
          </h3>

          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Vendor</th>
                  <th style={{ textAlign: "right" }}>Total Spend</th>
                </tr>
              </thead>
              <tbody>
                {vendors.length === 0 ? (
                  <tr><td colSpan={2} style={{ textAlign: "center", color: "var(--text-secondary)" }}>No data</td></tr>
                ) : vendors.map((v, i) => (
                  <tr key={i}>
                    <td>{v.name}</td>
                    <td style={{ textAlign: "right", fontWeight: "600" }}>₹{Number(v.total).toFixed(2)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div className="card">
        <h3 style={{ marginBottom: "1.5rem", color: "var(--danger-color)", display: "flex", alignItems: "center", gap: "0.5rem" }}>
          <AlertTriangle size={18} /> Flagged Anomalies ({anomalyCount})
        </h3>

        <p style={{ color: "var(--text-secondary)", marginBottom: "1rem", fontSize: "0.9rem" }}>
          Expenses that are more than 3× the average amount for their category.
        </p>

        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Date</th>
                <th>Vendor</th>
                <th>Category</th>
                <th>Description</th>
                <th style={{ textAlign: "right" }}>Amount</th>
              </tr>
            </thead>
            <tbody>
              {anomaliesLoading ? (
                <tr><td colSpan={5} style={{ textAlign: "center", padding: "2rem" }}>Loading anomalies...</td></tr>
              ) : anomalies.length === 0 ? (
                <tr><td colSpan={5} style={{ textAlign: "center", color: "var(--text-secondary)", padding: "2rem" }}>No anomalies detected. Great job!</td></tr>
              ) : anomalies.map(a => (
                <tr key={a.id ?? `₹{a.vendorName}-₹{a.expenseDate}`} className="anomaly-row">
                  <td>{a.expenseDate}</td>
                  <td style={{ fontWeight: "500" }}>{a.vendorName}</td>
                  <td><span className="badge badge-danger">{a.categoryName}</span></td>
                  <td style={{ color: "var(--text-secondary)", fontSize: "0.9rem" }}>{a.description}</td>
                  <td style={{ textAlign: "right", fontWeight: "700", color: "#fca5a5" }}>₹{Number(a.amount).toFixed(2)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginTop: "1rem" }}>
        <p style={{ fontSize: "0.9rem", color: "var(--text-secondary)" }}>
          Page {currentPage + 1} of {totalPages}
        </p>

        <div style={{ display: "flex", gap: "0.5rem" }}>
          <button disabled={currentPage === 0} onClick={() => loadAnomalies(currentPage - 1)} className="btn-secondary">
            ⬅ Prev
          </button>
          <button disabled={currentPage + 1 >= totalPages} onClick={() => loadAnomalies(currentPage + 1)} className="btn-secondary">
            Next ➡
          </button>
        </div>
      </div>
    </div>
  );
}
