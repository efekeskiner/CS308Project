import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { register } from "../services/auth";
import "./Registration.css";

function Registration() {
  const [showPassword, setShowPassword]               = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [name, setName]                               = useState("");
  const [email, setEmail]                             = useState("");
  const [taxId, setTaxId]                             = useState("");
  const [homeAddress, setHomeAddress]                 = useState("");
  const [password, setPassword]                       = useState("");
  const [confirmPassword, setConfirmPassword]         = useState("");
  const [passwordError, setPasswordError]             = useState("");
  const [formError, setFormError]                     = useState("");
  const [loading, setLoading]                         = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!name.trim() || !email.trim() || !taxId.trim() || !homeAddress.trim() || !password.trim() || !confirmPassword.trim()) {
      setFormError("Please fill in all fields.");
      return;
    }
    if (password !== confirmPassword) {
      setFormError("");
      setPasswordError("Passwords do not match.");
      return;
    }
    setFormError("");
    setPasswordError("");
    setLoading(true);
    try {
      await register(name, email, taxId, homeAddress, password);
      navigate("/login");
    } catch (err) {
      setFormError(err.message || "Registration failed. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <div className="register-card">
        <div className="brand">Online Bookstore</div>
        <h1>Create Account</h1>
        <p className="subtitle">Sign up to start shopping and manage your account</p>
        <form className="register-form" onSubmit={handleSubmit}>

          <label htmlFor="name">Full Name</label>
          <input
            id="name" type="text" placeholder="Enter your full name" value={name}
            onChange={(e) => { setName(e.target.value); setFormError(""); }}
          />

          <label htmlFor="email">Email</label>
          <input
            id="email" type="email" placeholder="Enter your email" value={email}
            onChange={(e) => { setEmail(e.target.value); setFormError(""); }}
          />

          <label htmlFor="taxId">Tax ID</label>
          <input
            id="taxId"
            type="text"
            placeholder="Enter your tax ID"
            value={taxId}
            onChange={(e) => { setTaxId(e.target.value); setFormError(""); }}
          />

          <label htmlFor="homeAddress">Home Address</label>
          <textarea
            id="homeAddress" rows="3" placeholder="Enter your home address" value={homeAddress}
            onChange={(e) => { setHomeAddress(e.target.value); setFormError(""); }}
          />

          <label htmlFor="password">Password</label>
          <div className="password-wrapper">
            <input
              id="password"
              type={showPassword ? "text" : "password"}
              placeholder="Create a password"
              value={password}
              onChange={(e) => {
                const val = e.target.value;
                setPassword(val);
                setFormError("");
                setPasswordError(confirmPassword && val !== confirmPassword ? "Passwords do not match." : "");
              }}
            />
            <button type="button" className="toggle-password" onClick={() => setShowPassword(!showPassword)}>
              {showPassword ? "Hide" : "Show"}
            </button>
          </div>

          <label htmlFor="confirmPassword">Confirm Password</label>
          <div className="password-wrapper">
            <input
              id="confirmPassword"
              type={showConfirmPassword ? "text" : "password"}
              placeholder="Confirm your password"
              value={confirmPassword}
              onChange={(e) => {
                const val = e.target.value;
                setConfirmPassword(val);
                setFormError("");
                setPasswordError(password !== val ? "Passwords do not match." : "");
              }}
            />
            <button type="button" className="toggle-password" onClick={() => setShowConfirmPassword(!showConfirmPassword)}>
              {showConfirmPassword ? "Hide" : "Show"}
            </button>
          </div>

          {formError     && <p className="error-text">{formError}</p>}
          {passwordError && <p className="error-text">{passwordError}</p>}

          <button type="submit" className="submit-btn" disabled={loading}>
            {loading ? "Creating account..." : "Create Account"}
          </button>
        </form>
        <p className="bottom-text">
          Already have an account? <Link to="/login">Log in</Link>
        </p>
      </div>
    </div>
  );
}

export default Registration;