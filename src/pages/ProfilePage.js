import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { logout, getCurrentUser, isLoggedIn, authFetch } from "../services/auth";
import UserInfo from "../components/UserInfo";
import OrderHistory from "../components/OrderHistory";

const BASE_URL = "http://localhost:8080/api";

function ProfilePage() {
  const navigate = useNavigate();
  const loggedIn = isLoggedIn();
  const [user, setUser] = useState(getCurrentUser());
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!loggedIn) return;
    setLoading(true);
    authFetch(`${BASE_URL}/auth/me`)
      .then((r) => { if (!r.ok) throw new Error(); return r.json(); })
      .then((data) => {
        setUser(data);
        localStorage.setItem("user", JSON.stringify(data));
      })
      .catch(() => setUser(getCurrentUser()))
      .finally(() => setLoading(false));
  }, [loggedIn]);

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <div style={styles.page}>
      <div style={styles.headerRow}>
        <div>
          <h2 style={styles.title}>Profile Dashboard</h2>
          <div style={styles.subtitle}>
            {loggedIn ? "Manage your account at a glance." : "You are not logged in."}
          </div>
        </div>
        {loggedIn && (
          <button style={styles.logoutButton} onClick={handleLogout}>
            Logout
          </button>
        )}
      </div>

      <div style={styles.grid}>
        <div style={styles.card}>
          <h3 style={styles.cardTitle}>User Information</h3>
          {loading ? <p style={{ color: "#888" }}>Loading profile...</p> : <UserInfo user={user} />}
        </div>

        {loggedIn && (
          <div style={styles.card}>
            <h3 style={styles.cardTitle}>Order History</h3>
            <OrderHistory />
          </div>
        )}

        <div style={styles.card}>
          <h3 style={styles.cardTitle}>Quick Actions</h3>
          <div style={styles.actions}>
            {!loggedIn && (
              <button style={styles.actionButton} onClick={() => navigate("/login")}>
                Go to Login
              </button>
            )}
            <button style={styles.actionButton} onClick={() => navigate(loggedIn ? "/wishlist" : "/login")}>
              Go to Wishlist
            </button>
            <button style={styles.actionButton} onClick={() => navigate("/cart")}>
              Go to Cart
            </button>
            <button style={styles.actionButton} onClick={() => navigate("/products")}>
              Continue Shopping
            </button>
            {loggedIn && (
              <button style={styles.actionButton} onClick={() => navigate("/orders")}>
                My Orders
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

const styles = {
  page: { padding: "24px 32px", maxWidth: "1100px", margin: "0 auto" },
  headerRow: { display: "flex", alignItems: "flex-start", justifyContent: "space-between", gap: "16px", marginBottom: "18px" },
  title: { margin: 0 },
  subtitle: { marginTop: "6px", color: "#555", fontSize: "14px" },
  logoutButton: { backgroundColor: "#6b4f3b", color: "white", border: "none", borderRadius: "10px", padding: "10px 16px", fontSize: "15px", cursor: "pointer" },
  grid: { display: "grid", gridTemplateColumns: "1fr", gap: "16px" },
  card: { border: "1px solid #eee", borderRadius: "14px", padding: "16px", backgroundColor: "white" },
  cardTitle: { marginTop: 0, marginBottom: "12px" },
  actions: { display: "flex", flexWrap: "wrap", gap: "10px" },
  actionButton: { backgroundColor: "#6b4f3b", color: "white", border: "none", borderRadius: "10px", padding: "10px 16px", fontSize: "15px", cursor: "pointer" },
};

export default ProfilePage;