import React from "react";
import { Film, FolderHeart, Users, Eye, Play, Sparkles, CheckCircle2 } from "lucide-react";

export default function DashboardOverview({ stats, moviesCount, categoriesCount, usersCount }) {
  const cards = [
    {
      title: "Total Movies & Shows",
      value: moviesCount,
      description: "Active video streaming catalog assets",
      icon: Film,
      color: "from-blue-500/20 to-indigo-500/5 border-blue-500/30 text-blue-400",
    },
    {
      title: "Active Genres / Categories",
      value: categoriesCount,
      description: "System categorization tags",
      icon: FolderHeart,
      color: "from-purple-500/20 to-pink-500/5 border-purple-500/30 text-purple-400",
    },
    {
      title: "Registered Users",
      value: usersCount,
      description: "Subscribed customer accounts database",
      icon: Users,
      color: "from-emerald-500/20 to-teal-500/5 border-emerald-500/30 text-emerald-400",
    },
    {
      title: "Total Video Plays",
      value: stats.totalViews,
      description: "Dynamic streaming play count tally",
      icon: Eye,
      color: "from-amber-500/20 to-orange-500/5 border-amber-500/30 text-amber-400",
    },
  ];

  return (
    <div className="space-y-8 animate-fadeIn">
      {/* Welcome Banner */}
      <div className="relative overflow-hidden bg-gradient-to-r from-darkSurface to-[#171c24] border border-white/10 rounded-2xl p-8 flex flex-col md:flex-row md:items-center justify-between gap-6 shadow-xl">
        <div className="absolute top-0 right-0 w-80 h-80 rounded-full bg-primaryGreen/5 blur-[80px] pointer-events-none"></div>
        <div className="space-y-2 relative z-10">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 bg-primaryGreen/15 border border-primaryGreen/30 rounded-full text-xs font-semibold text-primaryGreen mb-2">
            <Sparkles className="w-3.5 h-3.5" />
            System Live Status
          </div>
          <h2 className="text-2xl md:text-3xl font-extrabold text-white">
            Mat<span className="text-primaryGreen">Movies</span> Admin Command
          </h2>
          <p className="text-accentSlate/70 text-sm max-w-xl">
            Welcome to the centralized content orchestration cockpit. Manage media stream records, classify content tags, review user permissions, and deploy files securely.
          </p>
        </div>
        <div className="bg-[#11141a] px-6 py-4 rounded-xl border border-white/5 font-mono text-xs flex flex-col gap-2 shrink-0 self-start md:self-auto">
          <div className="flex items-center gap-2 text-primaryGreen">
            <CheckCircle2 className="w-4 h-4 shrink-0" />
            <span>FastAPI Server: Online</span>
          </div>
          <div className="text-accentSlate/50">DB Location: matmovies.db</div>
          <div className="text-accentSlate/50">Algorithm: HS256 JWT</div>
        </div>
      </div>

      {/* Stats Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {cards.map((card, idx) => {
          const Icon = card.icon;
          return (
            <div
              key={idx}
              className={`bg-gradient-to-br ${card.color} border rounded-2xl p-6 flex flex-col justify-between hover:scale-[1.02] transition-all shadow-lg`}
            >
              <div className="flex items-start justify-between">
                <div>
                  <p className="text-xs uppercase tracking-wider text-accentSlate/60 font-mono mb-1">
                    {card.title}
                  </p>
                  <h3 className="text-3xl font-black text-white tracking-tight">
                    {card.value}
                  </h3>
                </div>
                <div className="p-3 bg-white/5 border border-white/10 rounded-xl">
                  <Icon className="w-6 h-6" />
                </div>
              </div>
              <p className="text-xs text-accentSlate/50 mt-4 leading-normal">
                {card.description}
              </p>
            </div>
          );
        })}
      </div>

      {/* Platform Summary Analytics */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Core Capabilities */}
        <div className="bg-darkSurface border border-white/10 rounded-2xl p-6 lg:col-span-2 shadow-xl flex flex-col justify-between">
          <div className="mb-4">
            <h4 className="text-base font-bold text-white mb-1">Platform Integration Overview</h4>
            <p className="text-xs text-accentSlate/60">How the React portal synchronizes changes in real-time</p>
          </div>
          
          <div className="space-y-4 my-2">
            <div className="flex items-start gap-3">
              <div className="w-8 h-8 rounded-lg bg-blue-500/10 border border-blue-500/20 flex items-center justify-center shrink-0 mt-0.5 font-mono text-xs text-blue-400 font-bold">1</div>
              <div>
                <p className="text-xs font-bold text-white">Direct SQLite Synchronization</p>
                <p className="text-xs text-accentSlate/60">Every content upload, rating update, review deletion, and subscription modification maps instantly to SQLite rows.</p>
              </div>
            </div>
            <div className="flex items-start gap-3">
              <div className="w-8 h-8 rounded-lg bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center shrink-0 mt-0.5 font-mono text-xs text-emerald-400 font-bold">2</div>
              <div>
                <p className="text-xs font-bold text-white">Cryptographic Role Validation</p>
                <p className="text-xs text-accentSlate/60">All database mutations require a valid JSON Web Token containing administrator permission tags.</p>
              </div>
            </div>
            <div className="flex items-start gap-3">
              <div className="w-8 h-8 rounded-lg bg-amber-500/10 border border-amber-500/20 flex items-center justify-center shrink-0 mt-0.5 font-mono text-xs text-amber-400 font-bold">3</div>
              <div>
                <p className="text-xs font-bold text-white">Unified App & Web Catalog</p>
                <p className="text-xs text-accentSlate/60">Changes made here are delivered immediately to the MatMovies Jetpack Compose Android client via Retrofit calls.</p>
              </div>
            </div>
          </div>
        </div>

        {/* Poster Key Visuals Help */}
        <div className="bg-darkSurface border border-white/10 rounded-2xl p-6 shadow-xl flex flex-col justify-between">
          <div>
            <h4 className="text-base font-bold text-white mb-1">Visual Asset Map</h4>
            <p className="text-xs text-accentSlate/60">Android resources linked to key posters</p>
          </div>

          <div className="space-y-3 font-mono text-[11px] my-4 bg-[#11141a] p-4 rounded-xl border border-white/5">
            <div className="flex justify-between border-b border-white/5 pb-2 text-accentSlate/70">
              <span>Drawable Name</span>
              <span className="text-white">Style Category</span>
            </div>
            <div className="flex justify-between">
              <span className="text-accentSlate/55">img_poster_cyberpunk</span>
              <span className="text-primaryGreen font-semibold">Neon Corporate Sci-Fi</span>
            </div>
            <div className="flex justify-between">
              <span className="text-accentSlate/55">img_poster_fantasy</span>
              <span className="text-purple-400 font-semibold">Ancient Crystals Magic</span>
            </div>
            <div className="flex justify-between">
              <span className="text-accentSlate/55">img_hero_dune</span>
              <span className="text-amber-400 font-semibold">Arid Desert Planet Space</span>
            </div>
          </div>
          <p className="text-[11px] text-accentSlate/50 leading-relaxed">
            Choose these drawable names to load standard, high-definition posters bundled inside the MatMovies Android app!
          </p>
        </div>
      </div>
    </div>
  );
}
