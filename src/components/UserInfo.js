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

  const name = user.name || "Not provided";
  const email = user.email || "Not provided";
  const taxId = user.taxId || "Not provided";
  const homeAddress = user.homeAddress || "Not provided";
  const role = user.role || "CUSTOMER";

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
        <div style={styles.label}>Tax ID</div>
        <div style={styles.value}>{taxId}</div>
      </div>
      <div style={styles.row}>
        <div style={styles.label}>Address</div>
        <div style={styles.value}>{homeAddress}</div>
      </div>
      <div style={styles.row}>
        <div style={styles.label}>Role</div>
        <div style={styles.value}>
          <span style={{
            ...styles.badge,
            backgroundColor: role === "CUSTOMER" ? "#e0f2fe" : "#fef3c7",
            color: role === "CUSTOMER" ? "#0369a1" : "#92400e",
          }}>
            {role}
          </span>
        </div>
      </div>
    </div>
  );
}

const styles = {
  container: { display: "grid", gap: "10px" },
  row: { display: "grid", gridTemplateColumns: "120px 1fr", gap: "10px", alignItems: "center" },
  label: { fontWeight: 600, color: "#333" },
  value: { color: "#444", wordBreak: "break-word" },
  badge: { padding: "2px 10px", borderRadius: "999px", fontSize: "13px", fontWeight: 600 },
  empty: { border: "1px dashed #ddd", borderRadius: "12px", padding: "12px", backgroundColor: "#fafafa" },
  emptyTitle: { fontWeight: 600, marginBottom: "4px" },
  emptyText: { color: "#666", fontSize: "14px" },
};

export default UserInfo;