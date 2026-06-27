import React, { useState, useEffect } from "react";
import { X, Save, Film, HelpCircle } from "lucide-react";

export default function MovieFormModal({ movie, onClose, onSaveSuccess }) {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [rating, setRating] = useState("4.5");
  const [year, setYear] = useState("2026");
  const [duration, setDuration] = useState("");
  const [genre, setGenre] = useState("");
  const [videoUrl, setVideoUrl] = useState("");
  const [posterDrawableName, setPosterDrawableName] = useState("img_poster_cyberpunk");
  const [language, setLanguage] = useState("English");
  const [subtitlesUrl, setSubtitlesUrl] = useState("");
  const [isFeatured, setIsFeatured] = useState(false);
  const [isTrending, setIsTrending] = useState(false);
  const [isLatest, setIsLatest] = useState(true);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (movie) {
      setTitle(movie.title || "");
      setDescription(movie.description || "");
      setRating(movie.rating ? String(movie.rating) : "4.5");
      setYear(movie.year ? String(movie.year) : "2026");
      setDuration(movie.duration || "");
      setGenre(movie.genre || "");
      setVideoUrl(movie.videoUrl || movie.video_url || "");
      setPosterDrawableName(movie.posterDrawableName || movie.poster_drawable_name || "img_poster_cyberpunk");
      setLanguage(movie.language || "English");
      setSubtitlesUrl(movie.subtitlesUrl || movie.subtitles_url || "");
      setIsFeatured(movie.isFeatured || movie.is_featured || false);
      setIsTrending(movie.isTrending || movie.is_trending || false);
      setIsLatest(movie.isLatest !== undefined ? (movie.isLatest || movie.is_latest) : true);
    }
  }, [movie]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    if (!title.trim() || !description.trim() || !duration.trim() || !genre.trim() || !videoUrl.trim()) {
      setError("Please complete all required fields.");
      setLoading(false);
      return;
    }

    const moviePayload = {
      title: title.trim(),
      description: description.trim(),
      rating: parseFloat(rating) || 4.5,
      year: parseInt(year) || 2026,
      duration: duration.trim(),
      genre: genre.trim(),
      videoUrl: videoUrl.trim(),
      posterDrawableName: posterDrawableName,
      language: language.trim(),
      subtitlesUrl: subtitlesUrl.trim(),
      isFeatured: isFeatured,
      isTrending: isTrending,
      isLatest: isLatest,
    };

    try {
      await onSaveSuccess(movie ? movie.id : null, moviePayload);
      onClose();
    } catch (err) {
      setError(err.message || "An error occurred compiling metadata.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex justify-center items-center p-4 overflow-y-auto">
      <div className="bg-darkSurface border border-white/10 rounded-2xl w-full max-w-2xl max-h-[90vh] flex flex-col shadow-2xl animate-scaleUp">
        
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-white/10">
          <div className="flex items-center gap-2">
            <Film className="w-5 h-5 text-primaryGreen" />
            <h3 className="text-lg font-bold text-white">
              {movie ? "Edit Movie Metadata" : "Deploy New Movie Content"}
            </h3>
          </div>
          <button
            onClick={onClose}
            className="text-accentSlate hover:text-white p-1 rounded-lg hover:bg-white/5 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content Form */}
        <form onSubmit={handleSubmit} className="flex-1 overflow-y-auto p-6 space-y-6">
          {error && (
            <div className="bg-red-500/15 border border-red-500/30 text-red-200 p-3 rounded-lg text-xs leading-normal">
              {error}
            </div>
          )}

          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            {/* Title */}
            <div className="md:col-span-2">
              <label className="block text-xs font-semibold uppercase tracking-wider text-accentSlate/80 mb-2 font-mono">
                Movie Title <span className="text-primaryGreen">*</span>
              </label>
              <input
                type="text"
                required
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="e.g. Dune Chronicles"
                className="w-full bg-[#11141a] text-white px-4 py-2.5 border border-white/10 rounded-xl focus:outline-none focus:border-primaryGreen transition text-sm"
              />
            </div>

            {/* Description */}
            <div className="md:col-span-2">
              <label className="block text-xs font-semibold uppercase tracking-wider text-accentSlate/80 mb-2 font-mono">
                Synopsis / Description <span className="text-primaryGreen">*</span>
              </label>
              <textarea
                required
                rows={3}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Describe the storyline, primary conflict, and visual style..."
                className="w-full bg-[#11141a] text-white px-4 py-2.5 border border-white/10 rounded-xl focus:outline-none focus:border-primaryGreen transition text-sm"
              />
            </div>

            {/* Rating & Year */}
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-accentSlate/80 mb-2 font-mono">
                Average Rating (0.0 - 5.0)
              </label>
              <input
                type="number"
                step="0.1"
                min="0"
                max="5"
                required
                value={rating}
                onChange={(e) => setRating(e.target.value)}
                className="w-full bg-[#11141a] text-white px-4 py-2.5 border border-white/10 rounded-xl focus:outline-none focus:border-primaryGreen transition text-sm font-mono"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-accentSlate/80 mb-2 font-mono">
                Release Year
              </label>
              <input
                type="number"
                min="1888"
                max="2100"
                required
                value={year}
                onChange={(e) => setYear(e.target.value)}
                className="w-full bg-[#11141a] text-white px-4 py-2.5 border border-white/10 rounded-xl focus:outline-none focus:border-primaryGreen transition text-sm font-mono"
              />
            </div>

            {/* Duration & Genre */}
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-accentSlate/80 mb-2 font-mono">
                Duration <span className="text-primaryGreen">*</span>
              </label>
              <input
                type="text"
                required
                value={duration}
                onChange={(e) => setDuration(e.target.value)}
                placeholder="e.g. 2h 12m"
                className="w-full bg-[#11141a] text-white px-4 py-2.5 border border-white/10 rounded-xl focus:outline-none focus:border-primaryGreen transition text-sm font-mono"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-accentSlate/80 mb-2 font-mono">
                Genre <span className="text-primaryGreen">*</span>
              </label>
              <input
                type="text"
                required
                value={genre}
                onChange={(e) => setGenre(e.target.value)}
                placeholder="e.g. Sci-Fi, Action, Fantasy"
                className="w-full bg-[#11141a] text-white px-4 py-2.5 border border-white/10 rounded-xl focus:outline-none focus:border-primaryGreen transition text-sm"
              />
            </div>

            {/* Video URL */}
            <div className="md:col-span-2">
              <label className="block text-xs font-semibold uppercase tracking-wider text-accentSlate/80 mb-2 font-mono">
                Video Stream HTTP URL (MP4 / HLS) <span className="text-primaryGreen">*</span>
              </label>
              <input
                type="url"
                required
                value={videoUrl}
                onChange={(e) => setVideoUrl(e.target.value)}
                placeholder="https://commondatastorage.googleapis.com/...mp4"
                className="w-full bg-[#11141a] text-white px-4 py-2.5 border border-white/10 rounded-xl focus:outline-none focus:border-primaryGreen transition text-sm font-mono"
              />
            </div>

            {/* Key Visual Poster Drawable Name */}
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-accentSlate/80 mb-2 font-mono">
                Android Key Visual Asset
              </label>
              <select
                value={posterDrawableName}
                onChange={(e) => setPosterDrawableName(e.target.value)}
                className="w-full bg-[#11141a] text-white px-4 py-2.5 border border-white/10 rounded-xl focus:outline-none focus:border-primaryGreen transition text-sm font-mono"
              >
                <option value="img_poster_cyberpunk">img_poster_cyberpunk (Sci-Fi)</option>
                <option value="img_poster_fantasy">img_poster_fantasy (Fantasy Magic)</option>
                <option value="img_hero_dune">img_hero_dune (Desert Sci-Fi)</option>
              </select>
            </div>

            {/* Language */}
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-accentSlate/80 mb-2 font-mono">
                Audio Language
              </label>
              <input
                type="text"
                required
                value={language}
                onChange={(e) => setLanguage(e.target.value)}
                className="w-full bg-[#11141a] text-white px-4 py-2.5 border border-white/10 rounded-xl focus:outline-none focus:border-primaryGreen transition text-sm"
              />
            </div>

            {/* Subtitles Description */}
            <div className="md:col-span-2">
              <label className="block text-xs font-semibold uppercase tracking-wider text-accentSlate/80 mb-2 font-mono">
                Subtitle Channels List
              </label>
              <input
                type="text"
                value={subtitlesUrl}
                onChange={(e) => setSubtitlesUrl(e.target.value)}
                placeholder="e.g. English (SRT), Spanish (VTT)"
                className="w-full bg-[#11141a] text-white px-4 py-2.5 border border-white/10 rounded-xl focus:outline-none focus:border-primaryGreen transition text-sm"
              />
            </div>

            {/* Flag Toggles */}
            <div className="md:col-span-2 border-t border-white/5 pt-4 flex flex-wrap gap-x-8 gap-y-3">
              <label className="flex items-center gap-2.5 cursor-pointer select-none">
                <input
                  type="checkbox"
                  checked={isFeatured}
                  onChange={(e) => setIsFeatured(e.target.checked)}
                  className="w-4.5 h-4.5 rounded text-primaryGreen bg-[#11141a] border-white/10 focus:ring-0 focus:ring-offset-0 accent-primaryGreen"
                />
                <span className="text-xs text-white font-semibold">Feature on Home Banner</span>
              </label>

              <label className="flex items-center gap-2.5 cursor-pointer select-none">
                <input
                  type="checkbox"
                  checked={isTrending}
                  onChange={(e) => setIsTrending(e.target.checked)}
                  className="w-4.5 h-4.5 rounded text-primaryGreen bg-[#11141a] border-white/10 focus:ring-0 focus:ring-offset-0 accent-primaryGreen"
                />
                <span className="text-xs text-white font-semibold">Add to Trending Row</span>
              </label>

              <label className="flex items-center gap-2.5 cursor-pointer select-none">
                <input
                  type="checkbox"
                  checked={isLatest}
                  onChange={(e) => setIsLatest(e.target.checked)}
                  className="w-4.5 h-4.5 rounded text-primaryGreen bg-[#11141a] border-white/10 focus:ring-0 focus:ring-offset-0 accent-primaryGreen"
                />
                <span className="text-xs text-white font-semibold">Latest Release Listing</span>
              </label>
            </div>
          </div>
        </form>

        {/* Footer */}
        <div className="px-6 py-4 border-t border-white/10 flex items-center justify-end gap-3 bg-[#11141a] rounded-b-2xl">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 border border-white/10 rounded-xl hover:bg-white/5 transition text-xs font-semibold text-accentSlate"
          >
            Close
          </button>
          <button
            type="button"
            onClick={handleSubmit}
            disabled={loading}
            className="bg-primaryGreen text-darkBg hover:bg-primaryGreen/90 font-bold px-5 py-2 rounded-xl transition text-xs flex items-center gap-1.5 shadow-accent-glow disabled:opacity-50"
          >
            <Save className="w-4 h-4" />
            {loading ? "Saving Metadata..." : "Save Content Details"}
          </button>
        </div>
      </div>
    </div>
  );
}
