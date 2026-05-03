import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  logout,
  getCurrentUser,
  isLoggedIn,
  authFetch,
  updateCurrentUser,
} from "../services/auth";
import UserInfo from "../components/UserInfo";
import OrderHistory from "../components/OrderHistory";

const BASE_URL = "http://localhost:8080/api";

function ProfilePage() {
  const navigate = useNavigate();
  const loggedIn = isLoggedIn();

  const [user, setUser] = useState(getCurrentUser());
  const [loading, setLoading] = useState(false);

  const [editing, setEditing] = useState(false);
  const [editName, setEditName] = useState("");
  const [editHomeAddress, setEditHomeAddress] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!loggedIn) return;

    setLoading(true);

    authFetch(`${BASE_URL}/auth/me`)
      .then((r) => {
        if (!r.ok) throw new Error();
        return r.json();
      })
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

  const startEditing = () => {
    setEditName(user?.name || "");
    setEditHomeAddress(user?.homeAddress || "");
    setError("");
    setEditing(true);
  };

  const cancelEditing = () => {
    setEditing(false);
    setError("");
  };

  const saveProfile = async () => {
    if (!editName.trim() || !editHomeAddress.trim()) {
      setError("Name and address cannot be empty.");
      return;
    }

    try {
      setSaving(true);
      setError("");

      const updatedUser = await updateCurrentUser(
        editName.trim(),
        editHomeAddress.trim()
      );

      setUser(updatedUser);
      setEditing(false);
    } catch (err) {
      setError(err.message || "Profile update failed.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div style={styles.page}>
      <div style={styles.headerRow}>
        <div>
          <h2 style={styles.title}>Profile Dashboard</h2>
          <div style={styles.subtitle}>
            {loggedIn
              ? "Manage your account at a glance."
              : "You are not logged in."}
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
          <div style={styles.cardHeader}>
            <h3 style={styles.cardTitle}>User Information</h3>

            {loggedIn && !editing && !loading && (
              <button style={styles.editButton} onClick={startEditing}>
                Edit Account Info
              </button>
            )}
          </div>

          {error && <div style={styles.error}>{error}</div>}

          {loading ? (
            <p style={{ color: "#888" }}>Loading profile...</p>
          ) : editing ? (
            <div style={styles.editForm}>
              <label style={styles.inputLabel}>Name</label>
              <input
                style={styles.input}
                value={editName}
                onChange={(e) => setEditName(e.target.value)}
                placeholder="Enter your name"
              />

              <label style={styles.inputLabel}>Address</label>
              <textarea
                style={styles.textarea}
                value={editHomeAddress}
                onChange={(e) => setEditHomeAddress(e.target.value)}
                placeholder="Enter your address"
              />

              <div style={styles.editActions}>
                <button
                  style={styles.saveButton}
                  onClick={saveProfile}
                  disabled={saving}
                >
                  {saving ? "Saving..." : "Save Changes"}
                </button>

                <button
                  style={styles.cancelButton}
                  onClick={cancelEditing}
                  disabled={saving}
                >
                  Cancel
                </button>
              </div>
            </div>
          ) : (
            <UserInfo user={user} />
          )}
        </div>

        {loggedIn && (
          <div style={styles.card}>
            <h3 style={{ ...styles.cardTitle, marginBottom: "12px" }}>
              Order History
            </h3>
            <OrderHistory />
          </div>
        )}

        <div style={styles.card}>
          <h3 style={{ ...styles.cardTitle, marginBottom: "12px" }}>
            Quick Actions
          </h3>

          <div style={styles.actions}>
            {!loggedIn && (
              <button
                style={styles.actionButton}
                onClick={() => navigate("/login")}
              >
                Go to Login
              </button>
            )}

            <button
              style={styles.actionButton}
              onClick={() => navigate(loggedIn ? "/wishlist" : "/login")}
            >
              Go to Wishlist
            </button>

            <button style={styles.actionButton} onClick={() => navigate("/cart")}>
              Go to Cart
            </button>

            <button
              style={styles.actionButton}
              onClick={() => navigate("/products")}
            >
              Continue Shopping
            </button>

            {loggedIn && (
              <button
                style={styles.actionButton}
                onClick={() => navigate("/orders")}
              >
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

  cardHeader: {
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    gap: "12px",
    marginBottom: "12px",
  },

  cardTitle: {
    margin: 0,
  },

  editButton: {
    backgroundColor: "#6b4f3b",
    color: "white",
    border: "none",
    borderRadius: "10px",
    padding: "8px 14px",
    fontSize: "14px",
    cursor: "pointer",
  },

  editForm: {
    display: "grid",
    gap: "8px",
    maxWidth: "520px",
  },

  inputLabel: {
    fontWeight: 600,
    color: "#333",
    marginTop: "6px",
  },

  input: {
    border: "1px solid #ddd",
    borderRadius: "10px",
    padding: "10px 12px",
    fontSize: "15px",
  },

  textarea: {
    border: "1px solid #ddd",
    borderRadius: "10px",
    padding: "10px 12px",
    fontSize: "15px",
    minHeight: "80px",
    resize: "vertical",
    fontFamily: "inherit",
  },

  editActions: {
    display: "flex",
    gap: "10px",
    marginTop: "8px",
  },

  saveButton: {
    backgroundColor: "#6b4f3b",
    color: "white",
    border: "none",
    borderRadius: "10px",
    padding: "10px 16px",
    fontSize: "15px",
    cursor: "pointer",
  },

  cancelButton: {
    backgroundColor: "#f3f4f6",
    color: "#333",
    border: "1px solid #ddd",
    borderRadius: "10px",
    padding: "10px 16px",
    fontSize: "15px",
    cursor: "pointer",
  },

  error: {
    backgroundColor: "#fee2e2",
    color: "#991b1b",
    borderRadius: "10px",
    padding: "10px 12px",
    marginBottom: "12px",
    fontSize: "14px",
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