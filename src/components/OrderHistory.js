import React, { useEffect, useState } from "react";
import { getMyOrders } from "../services/orders";

const STATUS_COLORS = {
  PROCESSING: { bg: "#fff4e5", color: "#8a5a00", border: "#ffd7a8" },
  IN_TRANSIT: { bg: "#eff6ff", color: "#1e40af", border: "#bfdbfe" },
  DELIVERED: { bg: "#e7f6ec", color: "#1b7f3a", border: "#bfe6cb" },
  CANCELLED: { bg: "#fef2f2", color: "#991b1b", border: "#fecaca" },
};

function OrderHistory() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let mounted = true;

    const fetchOrders = async () => {
      try {
        setLoading(true);
        setError("");

        const data = await getMyOrders();

        if (!mounted) return;

        const realOrders = Array.isArray(data) ? data : data.content || [];
        setOrders(realOrders);
      } catch (err) {
        if (!mounted) return;
        setError("Could not load order history.");
      } finally {
        if (mounted) setLoading(false);
      }
    };

    fetchOrders();

    return () => {
      mounted = false;
    };
  }, []);

  const formatDate = (dateValue) => {
    if (!dateValue) return "Unknown date";

    return new Date(dateValue).toLocaleDateString("tr-TR", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
    });
  };

  const formatStatus = (status) => {
    if (!status) return "PROCESSING";
    return status.replace("_", " ");
  };

  const formatTotal = (totalPrice) => {
    const amount = Number(totalPrice || 0);
    return `₺${amount.toFixed(2)}`;
  };

  if (loading) {
    return <div style={styles.empty}>Loading order history...</div>;
  }

  if (error) {
    return <div style={styles.error}>{error}</div>;
  }

  if (!orders.length) {
    return <div style={styles.empty}>No orders yet.</div>;
  }

  return (
    <div style={styles.list}>
      {orders.map((order) => (
        <div key={order.id} style={styles.item}>
          <div style={styles.topRow}>
            <div style={styles.orderId}>Order #{order.id}</div>
            <div style={styles.badge(order.status)}>
              {formatStatus(order.status)}
            </div>
          </div>

          <div style={styles.metaRow}>
            <div style={styles.meta}>
              <span style={styles.metaLabel}>Date:</span>{" "}
              {formatDate(order.createdAt)}
            </div>

            <div style={styles.meta}>
              <span style={styles.metaLabel}>Total:</span>{" "}
              {formatTotal(order.totalPrice)}
            </div>
          </div>

          {order.items && order.items.length > 0 && (
            <div style={styles.products}>
              {order.items.slice(0, 2).map((item) => (
                <div key={item.id} style={styles.productLine}>
                  {item.productName} × {item.quantity}
                </div>
              ))}

              {order.items.length > 2 && (
                <div style={styles.productLine}>
                  +{order.items.length - 2} more item
                  {order.items.length - 2 > 1 ? "s" : ""}
                </div>
              )}
            </div>
          )}
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

  badge: (status) => {
    const s = STATUS_COLORS[status] || STATUS_COLORS.PROCESSING;

    return {
      padding: "4px 10px",
      borderRadius: "999px",
      fontSize: "12px",
      fontWeight: 600,
      textTransform: "capitalize",
      backgroundColor: s.bg,
      color: s.color,
      border: `1px solid ${s.border}`,
    };
  },

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

  products: {
    marginTop: "8px",
    paddingTop: "8px",
    borderTop: "1px solid #f3f3f3",
    display: "grid",
    gap: "4px",
  },

  productLine: {
    fontSize: "13px",
    color: "#555",
  },

  empty: {
    color: "#666",
  },

  error: {
    color: "#991b1b",
    backgroundColor: "#fee2e2",
    borderRadius: "10px",
    padding: "10px 12px",
    fontSize: "14px",
  },
};

export default OrderHistory;