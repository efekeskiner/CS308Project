import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { authFetch, getCurrentUser } from "../services/auth";

const BASE_URL = "http://localhost:8080/api";

const PM_TABS = ["Comments", "Deliveries", "Products & Stock"];
const SM_TABS = ["Invoices", "Discounts", "Refunds", "Revenue Chart"];

export default function AdminPage() {
  const navigate = useNavigate();
  const user = getCurrentUser();
  const role = user?.role;
  const [tab, setTab] = useState(0);

  if (!role || (role !== "PRODUCT_MANAGER" && role !== "SALES_MANAGER")) {
    return (
      <div style={styles.center}>
        <h2>Access Denied</h2>
        <button style={styles.btn} onClick={() => navigate("/products")}>Go Back</button>
      </div>
    );
  }

  const tabs = role === "PRODUCT_MANAGER" ? PM_TABS : SM_TABS;

  return (
    <div style={styles.page}>
      <h1 style={styles.pageTitle}>
        {role === "PRODUCT_MANAGER" ? "Product Manager" : "Sales Manager"} Panel
      </h1>
      <div style={styles.tabBar}>
        {tabs.map((t, i) => (
          <button key={t} style={{ ...styles.tabBtn, ...(tab === i ? styles.tabActive : {}) }} onClick={() => setTab(i)}>
            {t}
          </button>
        ))}
      </div>
      <div style={styles.content}>
        {role === "PRODUCT_MANAGER" && tab === 0 && <CommentsPanel />}
        {role === "PRODUCT_MANAGER" && tab === 1 && <DeliveriesPanel />}
        {role === "PRODUCT_MANAGER" && tab === 2 && <ProductsStockPanel />}
        {role === "SALES_MANAGER" && tab === 0 && <InvoicesPanel />}
        {role === "SALES_MANAGER" && tab === 1 && <DiscountsPanel />}
        {role === "SALES_MANAGER" && tab === 2 && <RefundsPanel />}
        {role === "SALES_MANAGER" && tab === 3 && <RevenuePanel />}
      </div>
    </div>
  );
}

function CommentsPanel() {
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetch_ = () => {
    setLoading(true);
    authFetch(`${BASE_URL}/comments/pending`)
      .then((r) => r.json())
      .then((d) => setComments(Array.isArray(d) ? d : []))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetch_(); }, []);

  const action = async (id, approve) => {
    await authFetch(`${BASE_URL}/comments/${id}/${approve ? "approve" : "reject"}`, { method: "PUT" });
    fetch_();
  };

  if (loading) return <Spinner />;
  if (comments.length === 0) return <Empty text="No pending comments." />;

  return (
    <div style={styles.list}>
      {comments.map((c) => (
        <div key={c.id} style={styles.card}>
          <div style={styles.cardHeader}>
            <strong>{c.userName}</strong>
            <span style={{ fontSize: 12, color: "#888" }}>Product #{c.productId}</span>
          </div>
          <p style={{ margin: "8px 0", color: "#333" }}>{c.content}</p>
          <div style={{ display: "flex", gap: 8 }}>
            <button style={styles.approveBtn} onClick={() => action(c.id, true)}>✓ Approve</button>
            <button style={styles.rejectBtn} onClick={() => action(c.id, false)}>✗ Reject</button>
          </div>
        </div>
      ))}
    </div>
  );
}

function DeliveriesPanel() {
  const [deliveries, setDeliveries] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetch_ = () => {
    setLoading(true);
    authFetch(`${BASE_URL}/deliveries`)
      .then((r) => r.json())
      .then((d) => setDeliveries(Array.isArray(d) ? d : []))
      .finally(() => setLoading(false));
  };
  useEffect(() => { fetch_(); }, []);

  const updateStatus = async (id, newStatus) => {
    await authFetch(`${BASE_URL}/deliveries/${id}/status`, {
      method: "PUT",
      body: JSON.stringify({ status: newStatus }),
    });
    fetch_();
  };

  if (loading) return <Spinner />;
  if (deliveries.length === 0) return <Empty text="No deliveries found." />;

  return (
    <div style={styles.list}>
      {deliveries.map((d) => (
        <div key={d.id} style={styles.card}>
          <div style={styles.cardHeader}>
            <strong>Delivery #{d.id}</strong>
            <span style={{ fontSize: 13 }}>Order #{d.orderId}</span>
          </div>
          <p style={styles.metaText}>Customer: {d.customerName}</p>
          <p style={styles.metaText}>Product: {d.productName} × {d.quantity}</p>
          <p style={styles.metaText}>Address: {d.deliveryAddress}</p>
          <p style={styles.metaText}>Total: ₺{Number(d.totalPrice).toFixed(2)}</p>
          <p style={styles.metaText}>Status: <strong>{d.isCompleted ? "Completed" : "Pending"}</strong></p>
          {!d.isCompleted && (
            <div style={{ display: "flex", gap: 8, marginTop: 10, flexWrap: "wrap" }}>
              <button style={styles.actionBtn} onClick={() => updateStatus(d.id, "IN_TRANSIT")}>Mark In-Transit</button>
              <button style={styles.approveBtn} onClick={() => updateStatus(d.id, "DELIVERED")}>Mark Delivered</button>
            </div>
          )}
        </div>
      ))}
    </div>
  );
}

function ProductsStockPanel() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [stockInputs, setStockInputs] = useState({});

  const fetch_ = () => {
    setLoading(true);
    fetch(`${BASE_URL}/products?size=100`)
      .then((r) => r.json())
      .then((d) => setProducts(d.content ?? d))
      .finally(() => setLoading(false));
  };
  useEffect(() => { fetch_(); }, []);

  const updateStock = async (id) => {
    const qty = parseInt(stockInputs[id]);
    if (isNaN(qty) || qty < 0) { alert("Invalid quantity"); return; }
    await authFetch(`${BASE_URL}/products/${id}/stock`, {
      method: "PUT",
      body: JSON.stringify({ quantity: qty }),
    });
    fetch_();
  };

  const deleteProduct = async (id) => {
    if (!window.confirm("Delete this product?")) return;
    await authFetch(`${BASE_URL}/products/${id}`, { method: "DELETE" });
    fetch_();
  };

  if (loading) return <Spinner />;

  return (
    <div>
      <table style={styles.table}>
        <thead>
          <tr>{["ID","Name","Category","Price","Stock","Update Stock",""].map((h) => <th key={h} style={styles.th}>{h}</th>)}</tr>
        </thead>
        <tbody>
          {products.map((p) => (
            <tr key={p.id} style={{ borderBottom: "1px solid #f0e8e0" }}>
              <td style={styles.td_}>{p.id}</td>
              <td style={styles.td_}>{p.name}</td>
              <td style={styles.td_}>{p.categoryName}</td>
              <td style={styles.td_}>₺{Number(p.price).toFixed(2)}</td>
              <td style={{ ...styles.td_, color: p.quantityInStock === 0 ? "#dc2626" : "#16a34a", fontWeight: 600 }}>{p.quantityInStock}</td>
              <td style={styles.td_}>
                <div style={{ display: "flex", gap: 4 }}>
                  <input type="number" min={0} placeholder="qty" style={styles.stockInput}
                    value={stockInputs[p.id] ?? ""}
                    onChange={(e) => setStockInputs({ ...stockInputs, [p.id]: e.target.value })} />
                  <button style={styles.actionBtn} onClick={() => updateStock(p.id)}>Set</button>
                </div>
              </td>
              <td style={styles.td_}>
                <button style={styles.rejectBtn} onClick={() => deleteProduct(p.id)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function InvoicesPanel() {
  const [invoices, setInvoices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");

  const fetch_ = () => {
    setLoading(true);
    const params = new URLSearchParams();
    if (startDate) params.set("startDate", startDate);
    if (endDate) params.set("endDate", endDate);
    authFetch(`${BASE_URL}/invoices?${params}`)
      .then((r) => r.json())
      .then((d) => setInvoices(Array.isArray(d) ? d : []))
      .finally(() => setLoading(false));
  };
  useEffect(() => { fetch_(); }, []);

  return (
    <div>
      <div style={{ display: "flex", gap: 12, marginBottom: 20, flexWrap: "wrap", alignItems: "flex-end" }}>
        <div><label style={styles.label}>Start Date</label><input type="date" style={styles.input} value={startDate} onChange={(e) => setStartDate(e.target.value)} /></div>
        <div><label style={styles.label}>End Date</label><input type="date" style={styles.input} value={endDate} onChange={(e) => setEndDate(e.target.value)} /></div>
        <button style={styles.btn} onClick={fetch_}>Filter</button>
      </div>
      {loading ? <Spinner /> : invoices.length === 0 ? <Empty text="No invoices found." /> : (
        <table style={styles.table}>
          <thead><tr>{["Invoice ID","Order ID","Customer","Total","Date","PDF"].map((h) => <th key={h} style={styles.th}>{h}</th>)}</tr></thead>
          <tbody>
            {invoices.map((inv) => (
              <tr key={inv.id} style={{ borderBottom: "1px solid #f0e8e0" }}>
                <td style={styles.td_}>#{inv.id}</td>
                <td style={styles.td_}>#{inv.orderId}</td>
                <td style={styles.td_}>{inv.customerName}</td>
                <td style={styles.td_}>₺{Number(inv.totalPrice).toFixed(2)}</td>
                <td style={styles.td_}>{new Date(inv.createdAt).toLocaleDateString("tr-TR")}</td>
                <td style={styles.td_}><a href={`${BASE_URL}/invoices/${inv.id}/pdf`} target="_blank" rel="noreferrer" style={styles.linkBtn}>📄 PDF</a></td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

function DiscountsPanel() {
  const [products, setProducts] = useState([]);
  const [selected, setSelected] = useState([]);
  const [rate, setRate] = useState("");
  const [loading, setLoading] = useState(true);
  const [msg, setMsg] = useState("");

  useEffect(() => {
    fetch(`${BASE_URL}/products?size=100`).then((r) => r.json()).then((d) => setProducts(d.content ?? d)).finally(() => setLoading(false));
  }, []);

  const toggleSelect = (id) => setSelected((prev) => prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]);

  const applyDiscount = async () => {
    if (selected.length === 0 || !rate) { alert("Select products and enter a rate."); return; }
    await authFetch(`${BASE_URL}/discounts`, { method: "POST", body: JSON.stringify({ productIds: selected, discountRate: parseFloat(rate) }) });
    setMsg(`Discount of ${rate}% applied to ${selected.length} product(s).`);
    setSelected([]); setRate("");
  };

  const removeDiscount = async (productId) => {
    await authFetch(`${BASE_URL}/discounts/${productId}`, { method: "DELETE" });
    window.location.reload();
  };

  if (loading) return <Spinner />;

  return (
    <div>
      {msg && <p style={{ color: "#16a34a", marginBottom: 16 }}>{msg}</p>}
      <div style={{ display: "flex", gap: 12, alignItems: "center", marginBottom: 20, flexWrap: "wrap" }}>
        <input type="number" min={1} max={100} placeholder="Discount % (e.g. 20)" style={styles.input} value={rate} onChange={(e) => setRate(e.target.value)} />
        <button style={styles.approveBtn} onClick={applyDiscount}>Apply to Selected</button>
        <span style={{ color: "#777", fontSize: 13 }}>{selected.length} selected</span>
      </div>
      <table style={styles.table}>
        <thead><tr>{["","ID","Name","Price","Discount",""].map((h) => <th key={h} style={styles.th}>{h}</th>)}</tr></thead>
        <tbody>
          {products.map((p) => (
            <tr key={p.id} style={{ borderBottom: "1px solid #f0e8e0", backgroundColor: selected.includes(p.id) ? "#fef3c7" : "white" }}>
              <td style={styles.td_}><input type="checkbox" checked={selected.includes(p.id)} onChange={() => toggleSelect(p.id)} /></td>
              <td style={styles.td_}>{p.id}</td>
              <td style={styles.td_}>{p.name}</td>
              <td style={styles.td_}>₺{Number(p.price).toFixed(2)}</td>
              <td style={styles.td_}>{p.discountRate > 0 ? <span style={{ color: "#dc2626", fontWeight: 600 }}>-{p.discountRate}%</span> : <span style={{ color: "#9ca3af" }}>—</span>}</td>
              <td style={styles.td_}>{p.discountRate > 0 && <button style={styles.rejectBtn} onClick={() => removeDiscount(p.id)}>Remove</button>}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function RefundsPanel() {
  const [refunds, setRefunds] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetch_ = () => {
    setLoading(true);
    authFetch(`${BASE_URL}/refunds`).then((r) => r.json()).then((d) => setRefunds(Array.isArray(d) ? d : [])).finally(() => setLoading(false));
  };
  useEffect(() => { fetch_(); }, []);

  const action = async (id, approve) => {
    await authFetch(`${BASE_URL}/refunds/${id}/${approve ? "approve" : "reject"}`, { method: "PUT" });
    fetch_();
  };

  if (loading) return <Spinner />;
  if (refunds.length === 0) return <Empty text="No pending refund requests." />;

  return (
    <div style={styles.list}>
      {refunds.map((r) => (
        <div key={r.id} style={styles.card}>
          <div style={styles.cardHeader}>
            <strong>Refund #{r.id}</strong>
            <span style={{ fontSize: 12, color: "#888" }}>Status: {r.status}</span>
          </div>
          <p style={styles.metaText}>Product: {r.productName}</p>
          <p style={styles.metaText}>Amount: ₺{Number(r.refundAmount).toFixed(2)}</p>
          <p style={styles.metaText}>Requested: {new Date(r.requestedAt).toLocaleDateString("tr-TR")}</p>
          {r.status === "PENDING" && (
            <div style={{ display: "flex", gap: 8, marginTop: 10 }}>
              <button style={styles.approveBtn} onClick={() => action(r.id, true)}>✓ Approve</button>
              <button style={styles.rejectBtn} onClick={() => action(r.id, false)}>✗ Reject</button>
            </div>
          )}
        </div>
      ))}
    </div>
  );
}

function RevenuePanel() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");

  const fetch_ = () => {
    if (!startDate || !endDate) { alert("Select both dates."); return; }
    setLoading(true);
    authFetch(`${BASE_URL}/analytics/revenue?startDate=${startDate}&endDate=${endDate}`).then((r) => r.json()).then(setData).finally(() => setLoading(false));
  };

  return (
    <div>
      <div style={{ display: "flex", gap: 12, marginBottom: 24, flexWrap: "wrap", alignItems: "flex-end" }}>
        <div><label style={styles.label}>Start Date</label><input type="date" style={styles.input} value={startDate} onChange={(e) => setStartDate(e.target.value)} /></div>
        <div><label style={styles.label}>End Date</label><input type="date" style={styles.input} value={endDate} onChange={(e) => setEndDate(e.target.value)} /></div>
        <button style={styles.btn} onClick={fetch_}>Generate Report</button>
      </div>
      {loading && <Spinner />}
      {data && (
        <div>
          <div style={{ display: "flex", gap: 16, marginBottom: 24, flexWrap: "wrap" }}>
            {[
              { label: "Total Revenue", value: `₺${Number(data.totalRevenue).toFixed(2)}`, color: "#16a34a" },
              { label: "Total Cost", value: `₺${Number(data.totalCost).toFixed(2)}`, color: "#dc2626" },
              { label: "Net Profit", value: `₺${Number(data.profit).toFixed(2)}`, color: data.profit >= 0 ? "#2563eb" : "#dc2626" },
            ].map((s) => (
              <div key={s.label} style={{ ...styles.statCard, color: s.color }}>
                <p style={{ margin: 0, fontSize: 13, color: "#777" }}>{s.label}</p>
                <p style={{ margin: "4px 0 0", fontSize: 28, fontWeight: 700 }}>{s.value}</p>
              </div>
            ))}
          </div>
          {data.dataPoints?.length > 0 && (
            <div style={styles.chartWrap}>
              <h3 style={{ color: "#4b2e2e", marginTop: 0 }}>Daily Revenue</h3>
              <div style={{ display: "flex", alignItems: "flex-end", gap: 4, height: 160, overflowX: "auto" }}>
                {data.dataPoints.map((dp) => {
                  const maxRevenue = Math.max(...data.dataPoints.map((d) => d.revenue));
                  const height = maxRevenue > 0 ? (dp.revenue / maxRevenue) * 140 : 0;
                  return (
                    <div key={dp.date} style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 4 }}>
                      <span style={{ fontSize: 10, color: "#555" }}>₺{Math.round(dp.revenue)}</span>
                      <div style={{ width: 28, height, backgroundColor: "#6b4f3b", borderRadius: "4px 4px 0 0", minHeight: 2 }} />
                      <span style={{ fontSize: 9, color: "#888", transform: "rotate(-45deg)", transformOrigin: "top left", whiteSpace: "nowrap" }}>{dp.date?.slice(5)}</span>
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

function Spinner() { return <div style={{ textAlign: "center", padding: 40, color: "#6b4f3b" }}>Loading...</div>; }
function Empty({ text }) { return <div style={{ textAlign: "center", padding: 40, color: "#9ca3af" }}>{text}</div>; }

const styles = {
  page: { minHeight: "100vh", padding: "32px", background: "linear-gradient(180deg, #f8f4ee 0%, #f3ece3 100%)", fontFamily: "Arial, sans-serif" },
  pageTitle: { color: "#4b2e2e", fontSize: 32, marginBottom: 24 },
  tabBar: { display: "flex", gap: 8, marginBottom: 28, flexWrap: "wrap" },
  tabBtn: { padding: "10px 20px", borderRadius: 10, border: "1px solid #d1c7bc", backgroundColor: "white", cursor: "pointer", fontSize: 14, color: "#4b2e2e" },
  tabActive: { backgroundColor: "#4b2e2e", color: "white", borderColor: "#4b2e2e" },
  content: { backgroundColor: "white", borderRadius: 16, padding: "24px 28px", boxShadow: "0 2px 8px rgba(0,0,0,0.06)" },
  list: { display: "flex", flexDirection: "column", gap: 14 },
  card: { border: "1px solid #f0e8e0", borderRadius: 12, padding: "16px 20px", backgroundColor: "#fdfaf7" },
  cardHeader: { display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 6 },
  metaText: { margin: "4px 0", color: "#555", fontSize: 14 },
  approveBtn: { backgroundColor: "#16a34a", color: "white", border: "none", borderRadius: 8, padding: "8px 16px", cursor: "pointer", fontSize: 13 },
  rejectBtn: { backgroundColor: "#dc2626", color: "white", border: "none", borderRadius: 8, padding: "8px 16px", cursor: "pointer", fontSize: 13 },
  actionBtn: { backgroundColor: "#6b4f3b", color: "white", border: "none", borderRadius: 8, padding: "8px 14px", cursor: "pointer", fontSize: 13 },
  btn: { backgroundColor: "#6b4f3b", color: "white", border: "none", borderRadius: 10, padding: "10px 18px", fontSize: 14, cursor: "pointer" },
  label: { display: "block", fontSize: 13, color: "#555", marginBottom: 4 },
  input: { padding: "10px 12px", borderRadius: 8, border: "1px solid #d1c7bc", fontSize: 14, outline: "none" },
  stockInput: { width: 60, padding: "6px 8px", borderRadius: 6, border: "1px solid #d1c7bc", fontSize: 13 },
  table: { width: "100%", borderCollapse: "collapse" },
  th: { textAlign: "left", padding: "10px 12px", fontSize: 13, backgroundColor: "#f8f4ee", color: "#4b2e2e", fontWeight: 600, borderBottom: "2px solid #e5d9d0" },
  td_: { padding: "10px 12px", fontSize: 14, verticalAlign: "middle" },
  linkBtn: { backgroundColor: "#4b2e2e", color: "white", textDecoration: "none", borderRadius: 6, padding: "5px 10px", fontSize: 12, display: "inline-block" },
  center: { textAlign: "center", paddingTop: 80, color: "#555" },
  statCard: { flex: 1, minWidth: 160, backgroundColor: "white", borderRadius: 12, padding: "16px 20px", boxShadow: "0 2px 8px rgba(0,0,0,0.06)", border: "1px solid #f0e8e0" },
  chartWrap: { backgroundColor: "#fdfaf7", borderRadius: 12, padding: "20px 24px", border: "1px solid #f0e8e0" },
};