import { useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";

export default function Login() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [sending, setSending] = useState(false);

  if (user) return <Navigate to="/" replace />;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSending(true);
    try {
      await login(email, password);
      navigate("/", { replace: true });
    } catch (err) {
      setError(err.response?.data?.message || "Login failed. Try again.");
    } finally {
      setSending(false);
    }
  };

  return (
    <div className="center">
      <form className="auth-card" onSubmit={handleSubmit}>
        <h1>DevGuide AI</h1>
        <p className="muted">Log in to your study desk</p>

        <label>
          Email
          <input type="email" value={email} required
                 onChange={(e) => setEmail(e.target.value)} />
        </label>

        <label>
          Password
          <input type="password" value={password} required
                 onChange={(e) => setPassword(e.target.value)} />
        </label>

        {error && <p className="error">{error}</p>}

        <button type="submit" disabled={sending}>
          {sending ? "Logging in…" : "Log in"}
        </button>
      </form>
    </div>
  );
}
