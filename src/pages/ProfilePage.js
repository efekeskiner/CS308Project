import React from "react";
import { useNavigate } from "react-router-dom";
import { logout, getCurrentUser, isLoggedIn } from "../services/auth";
import UserInfo from "../components/UserInfo";
import OrderHistory from "../components/OrderHistory";

function ProfilePage() {
  const navigate = useNavigate();
  const loggedIn = isLoggedIn();
  const user = React.useMemo(() => {
    if (!loggedIn) return null;
    try {
      const u = getCurrentUser();
      return u && typeof u === "object" ? u : null;
    } catch {
      return null;
    }
  }, [loggedIn]);

  const handleLogout = () => {
    if (!loggedIn) {
      navigate("/login");
      return;
    }
    logout();
    navigate("/login");
  };

  const handleGo = (path) => navigate(path);

  const handleWishlistClick = () => {
    navigate(loggedIn ? "/wishlist" : "/login");
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
          <UserInfo user={user} />
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
              <button style={styles.actionButton} onClick={() => handleGo("/login")}>
                Go to Login
              </button>
            )}
            <button style={styles.actionButton} onClick={handleWishlistClick}>
              Go to Wishlist
            </button>
            <button style={styles.actionButton} onClick={() => handleGo("/cart")}>
              Go to Cart
            </button>
            <button style={styles.actionButton} onClick={() => handleGo("/products")}>
              Continue Shopping
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

const styles = {
  page: {
    padding: "24px 32px",
    maxWidth: "1100px",
    margin: "0 auto",
  },
  headerRow: {
    display: "flex",
    alignItems: "flex-start",
    justifyContent: "space-between",
    gap: "16px",
    marginBottom: "18px",
  },
  title: {
    margin: 0,
  },
  subtitle: {
    marginTop: "6px",
    color: "#555",
    fontSize: "14px",
  },
  logoutButton: {
    backgroundColor: "#6b4f3b",
    color: "white",
    border: "none",
    borderRadius: "10px",
    padding: "10px 16px",
    fontSize: "15px",
    cursor: "pointer",
    height: "fit-content",
  },
  grid: {
    display: "grid",
    gridTemplateColumns: "1fr",
    gap: "16px",
  },
  card: {
    border: "1px solid #eee",
    borderRadius: "14px",
    padding: "16px",
    backgroundColor: "white",
  },
  cardTitle: {
    marginTop: 0,
    marginBottom: "12px",
  },
  actions: {
    display: "flex",
    flexWrap: "wrap",
    gap: "10px",
  },
  actionButton: {
    backgroundColor: "#6b4f3b",
    color: "white",
    border: "none",
    borderRadius: "10px",
    padding: "10px 16px",
    fontSize: "15px",
    cursor: "pointer",
  },
};

export default ProfilePage;
