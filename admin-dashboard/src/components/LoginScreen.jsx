import React, { useState } from "react";
import { authService, getBaseURL, setBaseURL } from "../services/api";
import { ShieldAlert, Play, Key, Mail, Settings, RefreshCw, Server } from "lucide-react";

export default function LoginScreen({ onLoginSuccess }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [showSettings, setShowSettings] = useState(false);
  const [tempApiUrl, setTempApiUrl] = useState(getBaseURL());

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      await authService.login(email.trim(), password);
      onLoginSuccess();
    } catch (err) {
      setError(err.message || "Failed to log in. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const saveSettings = (e) => {
    e.preventDefault();
    setBaseURL(tempApiUrl.trim());
    setShowSettings(false);
  };

  return (
    <div className="min-h-screen bg-darkBg flex flex-col justify-center items-center px-4 relative overflow-hidden font-sans">
      {/* Abstract Background Glows */}
      <div className="absolute top-[-20%] left-[-20%] w-[60%] h-[60%] rounded-full bg-primaryGreen/10 blur-[120px] pointer-events-none"></div>
      <div className="absolute bottom-[-20%] right-[-20%] w-[60%] h-[60%] rounded-full bg-neonPurple/10 blur-[120px] pointer-events-none"></div>

      <div className="w-full max-w-md bg-darkSurface border border-white/10 rounded-2xl p-8 shadow-2xl relative z-10">
        <div className="flex flex-col items-center mb-8">
          <div className="w-16 h-16 bg-primaryGreen/15 rounded-2xl flex items-center justify-center border border-primaryGreen/30 mb-4 shadow-accent-glow-xl">
            <Play className="w-8 h-8 text-primaryGreen fill-primaryGreen" />
          </div>
          <h1 className="text-3xl font-extrabold tracking-tight text-white mb-1">
            Mat<span className="text-primaryGreen">Movies</span>
          </h1>
          <p className="text-xs uppercase tracking-wider text-accentSlate/60 font-mono">
            Admin Portal Control Center
          </p>
        </div>

        {error && (
          <div className="mb-6 bg-red-500/10 border border-red-500/30 text-red-200 p-4 rounded-xl flex items-start gap-3 text-sm animate-shake">
            <ShieldAlert className="w-5 h-5 text-red-400 shrink-0 mt-0.5" />
            <span>{error}</span>
          </div>
        )}

        {!showSettings ? (
          <form onSubmit={handleLogin} className="space-y-5">
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-accentSlate/80 mb-2 font-mono">
                Administrator Email
              </label>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 flex items-center pl-3 text-accentSlate/40">
                  <Mail className="w-5 h-5" />
                </span>
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="admin@matmovies.com"
                  className="w-full bg-[#11141a] text-white pl-10 pr-4 py-3 border border-white/10 rounded-xl focus:outline-none focus:border-primaryGreen focus:ring-1 focus:ring-primaryGreen transition text-sm"
                />
              </div>
            </div>

            <div>
              <div className="flex justify-between items-center mb-2">
                <label className="block text-xs font-semibold uppercase tracking-wider text-accentSlate/80 font-mono">
                  Secret Key Phrase
                </label>
              </div>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 flex items-center pl-3 text-accentSlate/40">
                  <Key className="w-5 h-5" />
                </span>
                <input
                  type="password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••••••"
                  className="w-full bg-[#11141a] text-white pl-10 pr-4 py-3 border border-white/10 rounded-xl focus:outline-none focus:border-primaryGreen focus:ring-1 focus:ring-primaryGreen transition text-sm"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-primaryGreen hover:bg-primaryGreen/90 text-darkBg font-bold py-3.5 px-4 rounded-xl transition-all shadow-accent-glow-button hover:shadow-accent-glow-button-hover disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 mt-4 text-sm"
            >
              {loading ? (
                <>
                  <RefreshCw className="w-4 h-4 animate-spin" />
                  Authorizing Cryptography Keys...
                </>
              ) : (
                "Unlock Dashboard Console"
              )}
            </button>
          </form>
        ) : (
          <form onSubmit={saveSettings} className="space-y-5">
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-accentSlate/80 mb-2 font-mono">
                API Base Connection URL
              </label>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 flex items-center pl-3 text-accentSlate/40">
                  <Server className="w-5 h-5" />
                </span>
                <input
                  type="text"
                  required
                  value={tempApiUrl}
                  onChange={(e) => setTempApiUrl(e.target.value)}
                  placeholder="https://api.matmovies.com/api/v1"
                  className="w-full bg-[#11141a] text-white pl-10 pr-4 py-3 border border-white/10 rounded-xl focus:outline-none focus:border-primaryGreen focus:ring-1 focus:ring-primaryGreen transition text-sm font-mono"
                />
              </div>
              <p className="mt-2 text-xs text-accentSlate/50">
                Points your local or cloud hosted REST API service container.
              </p>
            </div>

            <div className="flex gap-3 pt-2">
              <button
                type="button"
                onClick={() => setShowSettings(false)}
                className="flex-1 bg-white/5 hover:bg-white/10 border border-white/10 text-white font-semibold py-3 px-4 rounded-xl transition text-sm"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="flex-1 bg-primaryGreen text-darkBg font-bold py-3 px-4 rounded-xl transition-all text-sm"
              >
                Apply Link
              </button>
            </div>
          </form>
        )}

        <div className="mt-8 border-t border-white/5 pt-6 flex justify-between items-center text-xs">
          <span className="text-accentSlate/40 font-mono">Connection URL:</span>
          <button
            type="button"
            onClick={() => {
              setTempApiUrl(getBaseURL());
              setShowSettings(!showSettings);
            }}
            className="text-primaryGreen hover:underline flex items-center gap-1 font-mono transition"
          >
            <Settings className="w-3.5 h-3.5" />
            {showSettings ? "Back to Login" : "Configure API"}
          </button>
        </div>
      </div>
    </div>
  );
}
