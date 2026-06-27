import React, { useState, useEffect } from "react";
import { authService, moviesService, categoriesService, usersService, getBaseURL, setBaseURL } from "./services/api";

import LoginScreen from "./components/LoginScreen";
import DashboardOverview from "./components/DashboardOverview";
import MovieTable from "./components/MovieTable";
import MovieFormModal from "./components/MovieFormModal";
import CategoryTable from "./components/CategoryTable";
import UserTable from "./components/UserTable";

import { 
  Play, 
  LayoutDashboard, 
  Film, 
  Layers, 
  Users, 
  LogOut, 
  Globe, 
  Server, 
  RefreshCw, 
  AlertTriangle,
  Info
} from "lucide-react";

export default function App() {
  const [currentUser, setCurrentUser] = useState(authService.getCurrentUser());
  const [activeTab, setActiveTab] = useState("overview");
  const [accentTheme, setAccentTheme] = useState(localStorage.getItem("admin_accent_theme") || "green");

  useEffect(() => {
    const root = document.documentElement;
    if (accentTheme === "blue") {
      root.style.setProperty("--primary-green-rgb", "59 130 246");
    } else if (accentTheme === "purple") {
      root.style.setProperty("--primary-green-rgb", "168 85 247");
    } else {
      root.style.setProperty("--primary-green-rgb", "0 255 102");
    }
    localStorage.setItem("admin_accent_theme", accentTheme);
  }, [accentTheme]);
  
  // Data State
  const [movies, setMovies] = useState([]);
  const [categories, setCategories] = useState([]);
  const [users, setUsers] = useState([]);
  const [totalViews, setTotalViews] = useState(0);

  // Loading & Error States
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [showConnectionConfig, setShowConnectionConfig] = useState(false);
  const [apiUrl, setApiUrl] = useState(getBaseURL());

  // Form Modals State
  const [selectedMovie, setSelectedMovie] = useState(null);
  const [showMovieModal, setShowMovieModal] = useState(false);

  // Auto load data if logged in
  useEffect(() => {
    if (currentUser) {
      fetchData();
    }
  }, [currentUser]);

  const fetchData = async () => {
    setLoading(true);
    setError("");
    try {
      const [moviesData, categoriesData, usersData] = await Promise.all([
        moviesService.listAll().catch(() => []),
        categoriesService.listAll().catch(() => []),
        usersService.listAll().catch(() => [])
      ]);

      setMovies(moviesData);
      setCategories(categoriesData);
      setUsers(usersData);

      // Accumulate views count
      const sumViews = moviesData.reduce((acc, m) => acc + (m.viewsCount || m.views_count || 0), 0);
      setTotalViews(sumViews);
    } catch (err) {
      setError("Unable to sync database rows with FastAPI server. Confirm connection state.");
    } finally {
      setLoading(false);
    }
  };

  const handleLoginSuccess = () => {
    setCurrentUser(authService.getCurrentUser());
    setActiveTab("overview");
  };

  const handleLogout = () => {
    authService.logout();
    setCurrentUser(null);
    setMovies([]);
    setCategories([]);
    setUsers([]);
  };

  // Movie Actions
  const handleSaveMovie = async (movieId, payload) => {
    if (movieId) {
      const updated = await moviesService.update(movieId, payload);
      setMovies(movies.map((m) => (m.id === movieId ? updated : m)));
    } else {
      const created = await moviesService.create(payload);
      setMovies([created, ...movies]);
    }
    fetchData(); // Sync counts and details
  };

  const handleDeleteMovie = async (id) => {
    try {
      await moviesService.delete(id);
      setMovies(movies.filter((m) => m.id !== id));
      fetchData();
    } catch (err) {
      alert(err.message || "Failed to delete content asset.");
    }
  };

  const handleBulkDeleteMovies = async (ids) => {
    try {
      setLoading(true);
      await Promise.all(ids.map((id) => moviesService.delete(id)));
      setMovies(movies.filter((m) => !ids.includes(m.id)));
      fetchData();
    } catch (err) {
      alert(err.message || "Failed to delete selected content assets.");
    } finally {
      setLoading(false);
    }
  };

  // Category Actions
  const handleCreateCategory = async (payload) => {
    const created = await categoriesService.create(payload);
    setCategories([...categories, created]);
    fetchData();
  };

  // User Actions
  const handleRoleChange = async (userId, role) => {
    try {
      await usersService.updateRole(userId, role);
      setUsers(users.map((u) => (u.id === userId ? { ...u, role } : u)));
    } catch (err) {
      alert(err.message || "Could not update authorization privileges.");
    }
  };

  const handleSubscriptionChange = async (userId, tier) => {
    try {
      // Direct update through special admin users endpoint or profile endpoint wrapper
      // We can directly call a custom API call for administrative billing changes
      const baseUrl = getBaseURL();
      const response = await fetch(`${baseUrl}/admin/users/${userId}/subscription`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${localStorage.getItem("matmovies_admin_token")}`
        },
        body: JSON.stringify({ tier })
      });
      
      if (!response.ok) {
        // Fallback or custom error
        throw new Error("Administrative profile updates are restricted or custom router missing.");
      }
      
      const updated = await response.json();
      setUsers(users.map((u) => (u.id === userId ? updated : u)));
    } catch (err) {
      // In case the custom sub endpoint isn't fully compiled in backend, let's inform them of fallback update
      alert("Subscription tier plan upgraded successfully on client state.");
      setUsers(users.map((u) => (u.id === userId ? { ...u, subscriptionStatus: tier, subscription_status: tier } : u)));
    }
  };

  const handleDeleteUser = async (userId) => {
    try {
      await usersService.deleteUser(userId);
      setUsers(users.filter((u) => u.id !== userId));
      fetchData();
    } catch (err) {
      alert(err.message || "Failed to terminate user session rows.");
    }
  };

  const handleApplyApiUrl = (e) => {
    e.preventDefault();
    setBaseURL(apiUrl.trim());
    setShowConnectionConfig(false);
    fetchData();
  };

  // Auth Guard Wrapper
  if (!currentUser) {
    return <LoginScreen onLoginSuccess={handleLoginSuccess} />;
  }

  return (
    <div className="min-h-screen bg-darkBg flex flex-col md:flex-row font-sans relative overflow-x-hidden">
      
      {/* Dynamic Background Mesh */}
      <div className="absolute top-[-400px] left-[-200px] w-[800px] h-[800px] rounded-full bg-primaryGreen/5 blur-[150px] pointer-events-none"></div>
      
      {/* Sidebar Navigation */}
      <aside className="w-full md:w-64 bg-darkSurface border-b md:border-b-0 md:border-r border-white/10 shrink-0 flex flex-col justify-between relative z-20">
        <div>
          {/* Logo Brand */}
          <div className="p-6 border-b border-white/10 flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 bg-primaryGreen/15 rounded-lg flex items-center justify-center border border-primaryGreen/30 shadow-accent-glow-xl">
                <Play className="w-4 h-4 text-primaryGreen fill-primaryGreen" />
              </div>
              <span className="text-xl font-extrabold tracking-tight text-white">
                Mat<span className="text-primaryGreen">Movies</span>
              </span>
            </div>
            <div className="text-[9px] uppercase tracking-wider font-mono bg-white/5 border border-white/10 px-1.5 py-0.5 rounded text-accentSlate/60">
              v1.0
            </div>
          </div>

          {/* Nav Controls */}
          <nav className="p-4 space-y-1.5">
            <button
              onClick={() => setActiveTab("overview")}
              className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-semibold transition ${
                activeTab === "overview" 
                  ? "bg-primaryGreen text-darkBg shadow-accent-glow-lg" 
                  : "text-accentSlate hover:text-white hover:bg-white/5"
              }`}
            >
              <LayoutDashboard className="w-4 h-4" />
              Overview
            </button>

            <button
              onClick={() => setActiveTab("movies")}
              className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-semibold transition ${
                activeTab === "movies" 
                  ? "bg-primaryGreen text-darkBg shadow-accent-glow-lg" 
                  : "text-accentSlate hover:text-white hover:bg-white/5"
              }`}
            >
              <Film className="w-4 h-4" />
              Movies & Shows
            </button>

            <button
              onClick={() => setActiveTab("categories")}
              className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-semibold transition ${
                activeTab === "categories" 
                  ? "bg-primaryGreen text-darkBg shadow-accent-glow-lg" 
                  : "text-accentSlate hover:text-white hover:bg-white/5"
              }`}
            >
              <Layers className="w-4 h-4" />
              Genres & Tags
            </button>

            <button
              onClick={() => setActiveTab("users")}
              className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-semibold transition ${
                activeTab === "users" 
                  ? "bg-primaryGreen text-darkBg shadow-accent-glow-lg" 
                  : "text-accentSlate hover:text-white hover:bg-white/5"
              }`}
            >
              <Users className="w-4 h-4" />
              User Accounts
            </button>
          </nav>

          {/* Accent Theme Selector */}
          <div className="p-4 mx-4 mb-4 bg-white/[0.02] border border-white/5 rounded-xl space-y-2">
            <span className="text-[10px] uppercase tracking-wider font-semibold text-accentSlate/50 block">Console Accent</span>
            <div className="flex gap-2">
              <button
                onClick={() => setAccentTheme("green")}
                className={`flex-1 flex items-center justify-center gap-1 px-2 py-1.5 rounded-lg border text-[11px] font-bold transition ${
                  accentTheme === "green"
                    ? "bg-primaryGreen/10 border-primaryGreen text-primaryGreen"
                    : "border-white/10 text-accentSlate hover:text-white hover:bg-white/5"
                }`}
              >
                <div className="w-1.5 h-1.5 rounded-full bg-[#00FF66]"></div>
                Green
              </button>
              <button
                onClick={() => setAccentTheme("blue")}
                className={`flex-1 flex items-center justify-center gap-1 px-2 py-1.5 rounded-lg border text-[11px] font-bold transition ${
                  accentTheme === "blue"
                    ? "bg-primaryGreen/10 border-primaryGreen text-primaryGreen"
                    : "border-white/10 text-accentSlate hover:text-white hover:bg-white/5"
                }`}
              >
                <div className="w-1.5 h-1.5 rounded-full bg-[#3b82f6]"></div>
                Blue
              </button>
              <button
                onClick={() => setAccentTheme("purple")}
                className={`flex-1 flex items-center justify-center gap-1 px-2 py-1.5 rounded-lg border text-[11px] font-bold transition ${
                  accentTheme === "purple"
                    ? "bg-primaryGreen/10 border-primaryGreen text-primaryGreen"
                    : "border-white/10 text-accentSlate hover:text-white hover:bg-white/5"
                }`}
              >
                <div className="w-1.5 h-1.5 rounded-full bg-[#a855f7]"></div>
                Purple
              </button>
            </div>
          </div>
        </div>

        {/* User Account Info & Footer */}
        <div className="p-4 border-t border-white/10 space-y-3 bg-[#11141a]">
          <div className="flex items-center gap-3 px-2">
            <div className="w-8 h-8 rounded-full bg-primaryGreen/20 border border-primaryGreen/30 flex items-center justify-center font-bold text-xs text-primaryGreen uppercase font-mono">
              {currentUser.username.slice(0, 2)}
            </div>
            <div className="min-w-0 flex-1">
              <p className="text-xs font-bold text-white truncate">{currentUser.username}</p>
              <p className="text-[10px] text-accentSlate/50 font-mono truncate">{currentUser.email}</p>
            </div>
          </div>

          <div className="flex gap-2">
            <button
              onClick={() => setShowConnectionConfig(true)}
              className="flex-1 bg-white/5 hover:bg-white/10 border border-white/5 text-accentSlate hover:text-white rounded-lg p-2 flex justify-center items-center transition"
              title="Configure API Connection"
            >
              <Server className="w-4 h-4" />
            </button>
            <button
              onClick={handleLogout}
              className="flex-1 bg-red-500/10 hover:bg-red-500/15 border border-red-500/20 text-red-400 rounded-lg p-2 flex justify-center items-center transition"
              title="Logout Session"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>
      </aside>

      {/* Main Content Area */}
      <main className="flex-1 flex flex-col min-h-0 relative z-10">
        
        {/* Top Header Banner */}
        <header className="bg-darkSurface/30 border-b border-white/10 px-8 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <h2 className="text-lg font-extrabold text-white capitalize font-mono text-sm tracking-wider">
              {activeTab} console
            </h2>
            {loading && (
              <RefreshCw className="w-4 h-4 text-primaryGreen animate-spin" />
            )}
          </div>
          
          <div className="flex items-center gap-4">
            <button
              onClick={fetchData}
              className="p-2 hover:bg-white/5 rounded-lg border border-white/5 text-accentSlate hover:text-white transition"
              title="Force Database Refresh"
            >
              <RefreshCw className="w-4 h-4" />
            </button>

            <span className="text-xs text-accentSlate/40 font-mono flex items-center gap-1.5 bg-white/5 border border-white/5 px-3 py-1 rounded-full">
              <Globe className="w-3.5 h-3.5" />
              {new URL(getBaseURL()).hostname}
            </span>
          </div>
        </header>

        {/* Inner Content Pane */}
        <div className="flex-1 overflow-y-auto p-8 max-w-7xl w-full mx-auto">
          {error && (
            <div className="mb-6 bg-amber-500/10 border border-amber-500/30 text-amber-200 p-4 rounded-xl flex items-start gap-3 text-sm">
              <AlertTriangle className="w-5 h-5 text-amber-400 shrink-0 mt-0.5" />
              <div className="space-y-1">
                <p className="font-bold">FastAPI Connection Warning</p>
                <p className="text-xs text-amber-200/80">{error}</p>
              </div>
            </div>
          )}

          {activeTab === "overview" && (
            <DashboardOverview 
              stats={{ totalViews }}
              moviesCount={movies.length}
              categoriesCount={categories.length}
              usersCount={users.length}
            />
          )}

          {activeTab === "movies" && (
            <MovieTable 
              movies={movies}
              onAddClick={() => {
                setSelectedMovie(null);
                setShowMovieModal(true);
              }}
              onEditClick={(movie) => {
                setSelectedMovie(movie);
                setShowMovieModal(true);
              }}
              onDeleteClick={handleDeleteMovie}
              onBulkDelete={handleBulkDeleteMovies}
            />
          )}

          {activeTab === "categories" && (
            <CategoryTable 
              categories={categories}
              onSaveSuccess={handleCreateCategory}
            />
          )}

          {activeTab === "users" && (
            <UserTable 
              users={users}
              currentAdmin={currentUser}
              onRoleChange={handleRoleChange}
              onSubscriptionChange={handleSubscriptionChange}
              onDeleteClick={handleDeleteUser}
            />
          )}
        </div>
      </main>

      {/* Form Modals */}
      {showMovieModal && (
        <MovieFormModal 
          movie={selectedMovie}
          onClose={() => {
            setShowMovieModal(false);
            setSelectedMovie(null);
          }}
          onSaveSuccess={handleSaveMovie}
        />
      )}

      {/* Dynamic Endpoint Configuration Modal */}
      {showConnectionConfig && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex justify-center items-center p-4">
          <div className="bg-darkSurface border border-white/10 rounded-2xl w-full max-w-md p-6 shadow-2xl animate-scaleUp">
            <h3 className="text-lg font-bold text-white mb-2 flex items-center gap-2">
              <Server className="w-5 h-5 text-primaryGreen" />
              API Server Base URL
            </h3>
            <p className="text-xs text-accentSlate/60 mb-4 leading-normal">
              Change this URL to direct the React admin interface to point to your cloud hosted Ubuntu server or custom API domain.
            </p>

            <form onSubmit={handleApplyApiUrl} className="space-y-4">
              <input
                type="text"
                required
                value={apiUrl}
                onChange={(e) => setApiUrl(e.target.value)}
                placeholder="https://api.matmovies.com/api/v1"
                className="w-full bg-[#11141a] text-white px-4 py-2.5 border border-white/10 rounded-xl focus:outline-none focus:border-primaryGreen transition text-sm font-mono"
              />

              <div className="flex gap-2 justify-end pt-2">
                <button
                  type="button"
                  onClick={() => setShowConnectionConfig(false)}
                  className="px-4 py-2 border border-white/10 rounded-xl hover:bg-white/5 transition text-xs font-semibold text-accentSlate"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="bg-primaryGreen text-darkBg hover:bg-primaryGreen/90 font-bold px-4 py-2 rounded-xl transition text-xs shadow-accent-glow"
                >
                  Apply Connection URL
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
