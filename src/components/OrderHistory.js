import React, { useMemo } from "react";

function OrderHistory() {
  const orders = useMemo(
    () => [
      { id: "ORD-1001", date: "2026-04-10", total: 129.99, status: "delivered" },
      { id: "ORD-1002", date: "2026-04-18", total: 49.5, status: "processing" },
      { id: "ORD-1003", date: "2026-04-20", total: 18.0, status: "processing" },
    ],
    []
  );

  if (!orders.length) {
    return <div style={styles.empty}>No orders yet.</div>;
  }

  return (
    <div style={styles.list}>
      {orders.map((o) => (
        <div key={o.id} style={styles.item}>
          <div style={styles.topRow}>
            <div style={styles.orderId}>{o.id}</div>
            <div style={styles.badge(o.status)}>{o.status}</div>
          </div>
          <div style={styles.metaRow}>
            <div style={styles.meta}>
              <span style={styles.metaLabel}>Date:</span> {o.date}
            </div>
            <div style={styles.meta}>
              <span style={styles.metaLabel}>Total:</span> ${o.total.toFixed(2)}
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}

const styles = {
  list: {
    display: "grid",
    gap: "10px",
  },
  item: {
    border: "1px solid #eee",
    borderRadius: "12px",
    padding: "12px",
    backgroundColor: "#fff",
  },
  topRow: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    gap: "10px",
    marginBottom: "8px",
  },
  orderId: {
    fontWeight: 700,
  },
  badge: (status) => ({
    padding: "4px 10px",
    borderRadius: "999px",
    fontSize: "12px",
    fontWeight: 600,
    textTransform: "capitalize",
    backgroundColor: status === "delivered" ? "#e7f6ec" : "#fff4e5",
    color: status === "delivered" ? "#1b7f3a" : "#8a5a00",
    border: `1px solid ${status === "delivered" ? "#bfe6cb" : "#ffd7a8"}`,
  }),
  metaRow: {
    display: "flex",
    flexWrap: "wrap",
    gap: "12px",
    color: "#444",
    fontSize: "14px",
  },
  meta: {},
  metaLabel: {
    fontWeight: 600,
    color: "#333",
  },
  empty: {
    color: "#666",
  },
};

export default OrderHistory;
