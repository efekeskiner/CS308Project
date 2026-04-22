import React from "react";

function UserInfo({ user }) {
  const isValidUserObject =
    user &&
    typeof user === "object" &&
    !Array.isArray(user) &&
    Object.keys(user).length > 0;

  if (!isValidUserObject) {
    return (
      <div style={styles.empty}>
        <div style={styles.emptyTitle}>No user data found</div>
        <div style={styles.emptyText}>
          Please log in again to see your profile information.
        </div>
      </div>
    );
  }

  const name = user.name || user.fullName || "Not provided";
  const email = user.email || "Not provided";
  const phone = user.phone || user.phoneNumber || "Not provided";

  return (
    <div style={styles.container}>
      <div style={styles.row}>
        <div style={styles.label}>Name</div>
        <div style={styles.value}>{name}</div>
      </div>
      <div style={styles.row}>
        <div style={styles.label}>Email</div>
        <div style={styles.value}>{email}</div>
      </div>
      <div style={styles.row}>
        <div style={styles.label}>Phone</div>
        <div style={styles.value}>{phone}</div>
      </div>
    </div>
  );
}

const styles = {
  container: {
    display: "grid",
    gap: "10px",
  },
  row: {
    display: "grid",
    gridTemplateColumns: "120px 1fr",
    gap: "10px",
    alignItems: "center",
  },
  label: {
    fontWeight: 600,
    color: "#333",
  },
  value: {
    color: "#444",
    wordBreak: "break-word",
  },
  empty: {
    border: "1px dashed #ddd",
    borderRadius: "12px",
    padding: "12px",
    backgroundColor: "#fafafa",
  },
  emptyTitle: {
    fontWeight: 600,
    marginBottom: "4px",
  },
  emptyText: {
    color: "#666",
    fontSize: "14px",
  },
};

export default UserInfo;
