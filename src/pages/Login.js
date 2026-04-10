import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { login } from "../services/auth";

function Login() {
    const [email, setEmail]       = useState("");
    const [password, setPassword] = useState("");
    const [error, setError]       = useState("");
    const [loading, setLoading]   = useState(false);
    const navigate = useNavigate();

  const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setLoading(true);
        try {
                await login(email, password);
                navigate("/products");
        } catch (err) {
                setError(err.message || "E-posta veya sifre hatali.");
        } finally {
                setLoading(false);
        }
  };

  return (
        <div style={styles.container}>
      <div style={styles.box}>
        <h2 style={styles.title}>Login</h2>
          <form onSubmit={handleSubmit} style={styles.form}>
          <input
              type="email"
              placeholder="Email"
              style={styles.input}
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
                        <input
            type="password"
            placeholder="Password"
            style={styles.input}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
            {error && (
                          <p style={{ color: "red", fontSize: "14px", marginBottom: "10px" }}>
{error}
</p>
          )}
          <button type="submit" style={styles.button} disabled={loading}>
          {loading ? "Logging in..." : "Login"}
</button>
          <p style={{ marginTop: "15px", textAlign: "center" }}>
            Don't have an account? <Link to="/register">Register</Link>
              </p>
              </form>
              </div>
              </div>
  );
}

const styles = {
    container: {
          height: "100vh",
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          backgroundColor: "#f5f5f5",
    },
    box: {
          backgroundColor: "white",
          padding: "40px",
          borderRadius: "10px",
          boxShadow: "0 0 10px rgba(0,0,0,0.1)",
          width: "300px",
          textAlign: "center",
    },
    title: {
          marginBottom: "20px",
          fontSize: "28px",
    },
    form: {
          display: "flex",
          flexDirection: "column",
    },
    input: {
          marginBottom: "15px",
          padding: "10px",
          fontSize: "16px",
    },
    button: {
          padding: "10px",
          fontSize: "16px",
          backgroundColor: "#007bff",
          color: "white",
          border: "none",
          borderRadius: "5px",
          cursor: "pointer",
    },
};

export default Login;
