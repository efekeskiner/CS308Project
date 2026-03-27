import { useState } from "react";
import "./Registration.css";

function Registration() {
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [passwordError, setPasswordError] = useState("");
  const [formError, setFormError] = useState("");

  const formatPhone = (value) => {
    let digits = value.replace(/\D/g, "");
    digits = digits.substring(0, 12);

    let formatted = "+";

    if (digits.length > 0) {
      formatted += digits.substring(0, 2);
    }
    if (digits.length > 2) {
      formatted += " " + digits.substring(2, 5);
    }
    if (digits.length > 5) {
      formatted += " " + digits.substring(5, 8);
    }
    if (digits.length > 8) {
      formatted += " " + digits.substring(8, 10);
    }
    if (digits.length > 10) {
      formatted += " " + digits.substring(10, 12);
    }

    return formatted;
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    if (
      !name.trim() ||
      !email.trim() ||
      !phone.trim() ||
      !password.trim() ||
      !confirmPassword.trim()
    ) {
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
    alert("Registration form submitted");
  };

  return (
    <div className="page">
      <div className="register-card">
        <div className="brand">Online Store</div>

        <h1>Create Account</h1>
        <p className="subtitle">
          Sign up to start shopping and manage your account
        </p>

        <form className="register-form" onSubmit={handleSubmit}>
          <label htmlFor="name">Full Name</label>
          <input
            id="name"
            type="text"
            placeholder="Enter your full name"
            value={name}
            onChange={(e) => {
              setName(e.target.value);
              setFormError("");
            }}
          />

          <label htmlFor="email">Email</label>
          <input
            id="email"
            type="email"
            placeholder="Enter your email"
            value={email}
            onChange={(e) => {
              setEmail(e.target.value);
              setFormError("");
            }}
          />

          <label htmlFor="phone">Phone Number</label>
          <input
            id="phone"
            type="tel"
            value={phone}
            onChange={(e) => {
              const input = e.target.value;

              if (input === "") {
                setPhone("");
              } else {
                setPhone(formatPhone(input));
              }

              setFormError("");
            }}
            placeholder="+(90) 5XX XXX XX XX"
          />

          <label htmlFor="password">Password</label>
          <div className="password-wrapper">
            <input
              id="password"
              type={showPassword ? "text" : "password"}
              placeholder="Create a password"
              value={password}
              onChange={(e) => {
                const newPassword = e.target.value;
                setPassword(newPassword);
                setFormError("");

                if (confirmPassword && newPassword !== confirmPassword) {
                  setPasswordError("Passwords do not match.");
                } else {
                  setPasswordError("");
                }
              }}
            />
            <button
              type="button"
              className="toggle-password"
              onClick={() => setShowPassword(!showPassword)}
            >
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
                const newConfirmPassword = e.target.value;
                setConfirmPassword(newConfirmPassword);
                setFormError("");

                if (password !== newConfirmPassword) {
                  setPasswordError("Passwords do not match.");
                } else {
                  setPasswordError("");
                }
              }}
            />
            <button
              type="button"
              className="toggle-password"
              onClick={() => setShowConfirmPassword(!showConfirmPassword)}
            >
              {showConfirmPassword ? "Hide" : "Show"}
            </button>
          </div>

          {formError && <p className="error-text">{formError}</p>}
          {passwordError && <p className="error-text">{passwordError}</p>}

          <button type="submit" className="submit-btn">
            Create Account
          </button>
        </form>

        <p className="bottom-text">
          Already have an account? <a href="/">Log in</a>
        </p>
      </div>
    </div>
  );
}

export default Registration;