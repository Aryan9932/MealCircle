import { useState } from "react";

function AuthModal({ isOpen, onClose, onAuthSuccess }) {
  const [mode, setMode] = useState("login");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState("CUSTOMER");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  if (!isOpen) {
    return null;
  }

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");
    setLoading(true);

    try {
      await onAuthSuccess({
        mode,
        payload:
          mode === "register" ? { email, password, role } : { email, password },
      });

      setEmail("");
      setPassword("");
      setRole("CUSTOMER");
      onClose();
    } catch (submitError) {
      setError(submitError.message || "Request failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className="auth-overlay"
      role="dialog"
      aria-modal="true"
      aria-label="Authentication"
    >
      <div className="auth-modal">
        <div className="auth-head">
          <h3>{mode === "login" ? "Welcome Back" : "Create Account"}</h3>
          <button
            className="close-btn"
            onClick={onClose}
            aria-label="Close auth modal"
          >
            ×
          </button>
        </div>

        <div className="auth-tabs">
          <button
            className={`tab-btn ${mode === "login" ? "tab-active" : ""}`}
            onClick={() => setMode("login")}
          >
            Login
          </button>
          <button
            className={`tab-btn ${mode === "register" ? "tab-active" : ""}`}
            onClick={() => setMode("register")}
          >
            Register
          </button>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            Email
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="you@example.com"
              required
            />
          </label>

          <label>
            Password
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="Enter password"
              required
            />
          </label>

          {mode === "register" && (
            <label>
              Role
              <select
                value={role}
                onChange={(event) => setRole(event.target.value)}
              >
                <option value="CUSTOMER">CUSTOMER</option>
                <option value="OWNER">OWNER</option>
              </select>
            </label>
          )}

          {error && <p className="auth-error">{error}</p>}

          <button
            className="btn btn-solid auth-submit"
            type="submit"
            disabled={loading}
          >
            {loading
              ? "Please wait..."
              : mode === "login"
                ? "Login"
                : "Register"}
          </button>
        </form>
      </div>
    </div>
  );
}

export default AuthModal;
