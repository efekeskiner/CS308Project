import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { authFetch } from "../services/auth";

const BASE_URL = "http://localhost:8080/api";

const STATUS_COLORS = {
  PROCESSING: { bg: "#fff4e5", color: "#8a5a00", border: "#ffd7a8" },
  IN_TRANSIT: { bg: "#eff6ff", color: "#1e40af", border: "#bfdbfe" },
  DELIVERED:  { bg: "#e7f6ec", color: "#1b7f3a", border: "#bfe6cb" },
  CANCELLED:  { bg: "#fef2f2", color: "#991b1b", border: "#fecaca" },
};

function Badge({ status }) {
  const s = STATUS_COLORS[status] || STATUS_COLORS.PROCESSING;
  return (
    <span style={{ padding: "4px 12px", borderRadius: "999px", fontSize: "12px", fontWeight: 600, backgroundColor: s.bg, color: s.color, border: `1px solid ${s.border}` }}>
      {status?.replace("_", " ")}
    </span>
  );
}

export default function OrdersPage() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [expanded, setExpanded] = useState(null);
  const [cancelling, setCancelling] = useState(null);
  const navigate = useNavigate();

  const fetchOrders = () => {
    setLoading(true);
    authFetch(`${BASE_URL}/orders`)
      .then((r) => { if (!r.ok) throw new Error("Failed"); return r.json(); })
      .then((data) => setOrders(Array.isArray(data) ? data : (data.content ?? [])))
      .catch(() => setError("Could not load orders."))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchOrders(); }, []);

  const handleCancel = async (orderId) => {
    if (!window.confirm("Cancel this order?")) return;
    setCancelling(orderId);
    try {
      const res = await authFetch(`${BASE_URL}/orders/${orderId}/cancel`, { method: "PUT" });
      if (!res.ok) throw new Error("Cancel failed");
      fetchOrders();
    } catch { alert("Could not cancel order."); }
    finally { setCancelling(null); }
  };

  if (loading) return <div style={styles.center}>Loading orders...</div>;
  if (error) return <div style={{ ...styles.center, color: "#dc2626" }}>{error}</div>;

  return (
    <div style={styles.page}>
      <div style={styles.headerRow}>
        <div>
          <h1 style={styles.title}>My Orders</h1>
          <p style={styles.subtitle}>Track your orders and their delivery status.</p>
        </div>
        <button style={styles.btn} onClick={() => navigate("/products")}>Continue Shopping</button>
      </div>

      {orders.length === 0 ? (
        <div style={styles.empty}>
          <h2>No orders yet</h2>
          <button style={styles.btn} onClick={() => navigate("/products")}>Browse Products</button>
        </div>
      ) : (
        <div style={styles.list}>
          {orders.map((order) => (
            <div key={order.id} style={styles.card}>
              <div style={styles.cardHeader}>
                <div>
                  <span style={styles.orderId}>Order #{order.id}</span>
                  <span style={{ marginLeft: 12, color: "#777", fontSize: 13 }}>
                    {new Date(order.createdAt).toLocaleDateString("tr-TR")}
                  </span>
                </div>
                <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                  <Badge status={order.status} />
                  <span style={styles.total}>₺{Number(order.totalPrice).toFixed(2)}</span>
                  <button style={styles.toggleBtn} onClick={() => setExpanded(expanded === order.id ? null : order.id)}>
                    {expanded === order.id ? "▲ Hide" : "▼ Details"}
                  </button>
                </div>
              </div>

              <div style={styles.progressRow}>
                {["PROCESSING", "IN_TRANSIT", "DELIVERED"].map((s, i) => {
                  const steps = ["PROCESSING", "IN_TRANSIT", "DELIVERED"];
                  const currentIdx = steps.indexOf(order.status);
                  const active = i <= currentIdx;
                  return (
                    <div key={s} style={styles.stepWrap}>
                      <div style={{ ...styles.stepDot, backgroundColor: active ? "#4b2e2e" : "#e5e7eb" }} />
                      <span style={{ ...styles.stepLabel, color: active ? "#4b2e2e" : "#9ca3af", fontWeight: active ? 600 : 400 }}>
                        {s.replace("_", " ")}
                      </span>
                      {i < 2 && <div style={{ ...styles.stepLine, backgroundColor: i < currentIdx ? "#4b2e2e" : "#e5e7eb" }} />}
                    </div>
                  );
                })}
              </div>

              {expanded === order.id && (
                <div style={styles.itemsList}>
                  {order.items?.map((item) => (
                    <div key={item.id} style={styles.itemRow}>
                      <span>{item.productName}</span>
                      <span style={{ color: "#555" }}>× {item.quantity}</span>
                      <span style={{ fontWeight: 600 }}>₺{(item.unitPrice * item.quantity).toFixed(2)}</span>
                    </div>
                  ))}
                  <div style={{ display: "flex", gap: 10, marginTop: 12, flexWrap: "wrap" }}>
                    {order.invoiceId && (
                      <a href={`${BASE_URL}/invoices/${order.invoiceId}/pdf`} target="_blank" rel="noreferrer" style={styles.linkBtn}>
                        📄 Invoice PDF
                      </a>
                    )}
                    {order.status === "PROCESSING" && (
                      <button style={styles.cancelBtn} disabled={cancelling === order.id} onClick={() => handleCancel(order.id)}>
                        {cancelling === order.id ? "Cancelling..." : "Cancel Order"}
                      </button>
                    )}
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

const styles = {
  page: { minHeight: "100vh", padding: "32px", background: "linear-gradient(180deg, #f8f4ee 0%, #f3ece3 100%)", fontFamily: "Arial, sans-serif" },
  headerRow: { display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: 24, flexWrap: "wrap", gap: 12 },
  title: { fontSize: 36, color: "#4b2e2e", margin: 0 },
  subtitle: { color: "#6b5b53", marginTop: 6 },
  btn: { backgroundColor: "#6b4f3b", color: "white", border: "none", borderRadius: 10, padding: "10px 18px", fontSize: 15, cursor: "pointer" },
  empty: { textAlign: "center", paddingTop: 60, color: "#555" },
  list: { display: "flex", flexDirection: "column", gap: 16 },
  card: { backgroundColor: "white", borderRadius: 16, padding: "20px 24px", boxShadow: "0 2px 8px rgba(0,0,0,0.07)" },
  cardHeader: { display: "flex", justifyContent: "space-between", alignItems: "center", flexWrap: "wrap", gap: 8, marginBottom: 14 },
  orderId: { fontWeight: 700, fontSize: 16, color: "#4b2e2e" },
  total: { fontWeight: 700, fontSize: 16, color: "#4b2e2e" },
  toggleBtn: { background: "none", border: "1px solid #d1c7bc", borderRadius: 8, padding: "4px 10px", cursor: "pointer", fontSize: 13, color: "#6b4f3b" },
  progressRow: { display: "flex", alignItems: "center", gap: 0, marginBottom: 8 },
  stepWrap: { display: "flex", alignItems: "center", gap: 4 },
  stepDot: { width: 12, height: 12, borderRadius: "50%", flexShrink: 0 },
  stepLabel: { fontSize: 12, whiteSpace: "nowrap" },
  stepLine: { width: 40, height: 2, margin: "0 4px" },
  itemsList: { borderTop: "1px solid #f0e8e0", paddingTop: 14, marginTop: 8 },
  itemRow: { display: "flex", justifyContent: "space-between", alignItems: "center", padding: "6px 0", borderBottom: "1px solid #f9f4f0", fontSize: 14, gap: 8 },
  linkBtn: { backgroundColor: "#4b2e2e", color: "white", border: "none", borderRadius: 8, padding: "8px 14px", fontSize: 13, cursor: "pointer", textDecoration: "none", display: "inline-block" },
  cancelBtn: { backgroundColor: "#dc2626", color: "white", border: "none", borderRadius: 8, padding: "8px 14px", fontSize: 13, cursor: "pointer" },
  center: { textAlign: "center", padding: 60, fontSize: 18, color: "#6b5b53" },
};