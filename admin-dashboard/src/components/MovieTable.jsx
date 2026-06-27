import React, { useState } from "react";
import { Edit2, Trash2, Search, Plus, Film, ExternalLink, Star } from "lucide-react";

export default function MovieTable({ movies, onAddClick, onEditClick, onDeleteClick, onBulkDelete }) {
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedMovieIds, setSelectedMovieIds] = useState([]);

  const filteredMovies = movies.filter((movie) => {
    const term = searchTerm.toLowerCase();
    return (
      movie.title.toLowerCase().includes(term) ||
      movie.genre.toLowerCase().includes(term)
    );
  });

  const handleSelectMovie = (id) => {
    setSelectedMovieIds((prev) =>
      prev.includes(id) ? prev.filter((mId) => mId !== id) : [...prev, id]
    );
  };

  const handleSelectAll = (e) => {
    if (e.target.checked) {
      setSelectedMovieIds(filteredMovies.map((m) => m.id));
    } else {
      setSelectedMovieIds([]);
    }
  };

  return (
    <div className="bg-darkSurface border border-white/10 rounded-2xl p-6 shadow-xl space-y-6 animate-fadeIn">
      
      {/* Top Controls Row */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h3 className="text-lg font-bold text-white flex items-center gap-2">
            <Film className="w-5 h-5 text-primaryGreen" />
            Active Video Catalog
          </h3>
          <p className="text-xs text-accentSlate/60">Configure, modify or deploy movie streams dynamically</p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          {selectedMovieIds.length > 0 && onBulkDelete && (
            <button
              onClick={() => {
                if (window.confirm(`Are you sure you want to permanently delete ${selectedMovieIds.length} selected movie(s) from the database?`)) {
                  onBulkDelete(selectedMovieIds);
                  setSelectedMovieIds([]);
                }
              }}
              className="bg-red-500/15 hover:bg-red-500/25 border border-red-500/30 text-red-400 font-bold px-4 py-2.5 rounded-xl transition text-xs flex items-center gap-1.5 shadow-[0_2px_8px_rgba(239,68,68,0.15)]"
            >
              <Trash2 className="w-4 h-4" />
              Delete Selected ({selectedMovieIds.length})
            </button>
          )}

          <button
            onClick={onAddClick}
            className="bg-primaryGreen text-darkBg hover:bg-primaryGreen/90 font-bold px-4 py-2.5 rounded-xl transition text-xs flex items-center gap-1.5 shadow-accent-glow"
          >
            <Plus className="w-4 h-4" />
            Deploy New Movie
          </button>
        </div>
      </div>

      {/* Search Input Bar */}
      <div className="relative">
        <span className="absolute inset-y-0 left-0 flex items-center pl-3.5 text-accentSlate/40">
          <Search className="w-4 h-4" />
        </span>
        <input
          type="text"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          placeholder="Search by movie title or classification genres..."
          className="w-full bg-[#11141a] text-white pl-10 pr-4 py-2.5 border border-white/10 rounded-xl focus:outline-none focus:border-primaryGreen transition text-sm"
        />
      </div>

      {/* Movies Table */}
      <div className="overflow-x-auto border border-white/5 rounded-xl">
        <table className="w-full border-collapse text-left text-sm">
          <thead>
            <tr className="bg-[#11141a] text-accentSlate/70 border-b border-white/5 uppercase font-mono text-[10px] tracking-wider select-none">
              <th className="px-5 py-4 font-bold w-12">
                <input
                  type="checkbox"
                  checked={filteredMovies.length > 0 && selectedMovieIds.length === filteredMovies.length}
                  onChange={handleSelectAll}
                  className="rounded bg-[#11141a] border-white/10 text-primaryGreen focus:ring-primaryGreen cursor-pointer w-4 h-4"
                />
              </th>
              <th className="px-5 py-4 font-bold">Key Poster</th>
              <th className="px-5 py-4 font-bold">Title & Description</th>
              <th className="px-5 py-4 font-bold">Release Year</th>
              <th className="px-5 py-4 font-bold">Metrics</th>
              <th className="px-5 py-4 font-bold text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-white/5 text-white/90">
            {filteredMovies.length === 0 ? (
              <tr>
                <td colSpan="6" className="px-5 py-8 text-center text-accentSlate/40 font-mono text-xs">
                  No video assets found matching search parameters.
                </td>
              </tr>
            ) : (
              filteredMovies.map((movie) => (
                <tr key={movie.id} className="hover:bg-white/[0.02] transition">
                  {/* Checkbox Column */}
                  <td className="px-5 py-4 w-12">
                    <input
                      type="checkbox"
                      checked={selectedMovieIds.includes(movie.id)}
                      onChange={() => handleSelectMovie(movie.id)}
                      className="rounded bg-[#11141a] border-white/10 text-primaryGreen focus:ring-primaryGreen cursor-pointer w-4 h-4"
                    />
                  </td>

                  {/* Poster Indicator */}
                  <td className="px-5 py-4 whitespace-nowrap">
                    <div className="flex items-center gap-3">
                      <div className="w-11 h-15 rounded bg-white/5 border border-white/10 flex flex-col justify-center items-center text-[8px] font-mono shrink-0 select-none uppercase font-bold text-center text-accentSlate/50 px-1 py-1.5 leading-tight">
                        {movie.posterDrawableName === "img_poster_cyberpunk" ? "🌌 Cyber" :
                         movie.posterDrawableName === "img_poster_fantasy" ? "🔮 Magic" : "🏜️ Dune"}
                      </div>
                      <div className="text-xs">
                        <span className="block font-semibold text-white font-mono text-[10px] bg-white/5 px-1.5 py-0.5 border border-white/5 rounded w-max mb-1">
                          {movie.posterDrawableName}
                        </span>
                        <span className="block text-accentSlate/55">{movie.duration}</span>
                      </div>
                    </div>
                  </td>

                  {/* Title & Description */}
                  <td className="px-5 py-4">
                    <div className="max-w-md">
                      <div className="flex items-center gap-2">
                        <span className="font-bold text-white text-sm hover:text-primaryGreen transition">
                          {movie.title}
                        </span>
                        {movie.isFeatured && (
                          <span className="text-[9px] bg-amber-500/10 border border-amber-500/20 text-amber-400 px-1.5 py-0.5 rounded-full font-bold">
                            Carousel
                          </span>
                        )}
                        {movie.isTrending && (
                          <span className="text-[9px] bg-purple-500/10 border border-purple-500/20 text-purple-400 px-1.5 py-0.5 rounded-full font-bold">
                            Trending
                          </span>
                        )}
                      </div>
                      <p className="text-xs text-accentSlate/60 line-clamp-2 mt-1 leading-normal">
                        {movie.description}
                      </p>
                      <div className="flex flex-wrap gap-1.5 mt-2">
                        {movie.genre.split(",").map((g, idx) => (
                          <span key={idx} className="text-[10px] bg-white/5 text-accentSlate border border-white/5 px-2 py-0.5 rounded-full">
                            {g.trim()}
                          </span>
                        ))}
                      </div>
                    </div>
                  </td>

                  {/* Year */}
                  <td className="px-5 py-4 whitespace-nowrap font-mono text-xs text-accentSlate">
                    {movie.year}
                  </td>

                  {/* Metrics */}
                  <td className="px-5 py-4 whitespace-nowrap">
                    <div className="flex flex-col gap-1 text-xs">
                      <div className="flex items-center gap-1 text-amber-400 font-bold">
                        <Star className="w-3.5 h-3.5 fill-amber-400" />
                        <span>{movie.rating}</span>
                      </div>
                      <div className="text-accentSlate/50 text-[11px] font-mono">
                        {movie.viewsCount !== undefined ? movie.viewsCount : movie.views_count} views
                      </div>
                    </div>
                  </td>

                  {/* Action Buttons */}
                  <td className="px-5 py-4 whitespace-nowrap text-right">
                    <div className="inline-flex items-center gap-2">
                      <a
                        href={movie.videoUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        title="Test Stream URL link"
                        className="p-1.5 bg-[#11141a] hover:bg-white/5 border border-white/5 rounded-lg text-accentSlate hover:text-primaryGreen transition"
                      >
                        <ExternalLink className="w-4 h-4" />
                      </a>
                      <button
                        onClick={() => onEditClick(movie)}
                        title="Edit metadata values"
                        className="p-1.5 bg-[#11141a] hover:bg-white/5 border border-white/5 rounded-lg text-accentSlate hover:text-white transition"
                      >
                        <Edit2 className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => {
                          if (window.confirm(`Are you sure you want to permanently delete '${movie.title}' from the database?`)) {
                            onDeleteClick(movie.id);
                          }
                        }}
                        title="Delete video record"
                        className="p-1.5 bg-[#11141a] hover:bg-red-500/10 border border-white/5 rounded-lg text-accentSlate hover:text-red-400 transition"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
