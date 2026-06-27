import React, { useState } from "react";
import { categoriesService } from "../services/api";
import { FolderHeart, Plus, HelpCircle, Key, RefreshCw, Layers } from "lucide-react";

export default function CategoryTable({ categories, onSaveSuccess }) {
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [iconName, setIconName] = useState("auto_awesome");
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");

    if (!name.trim() || !description.trim()) {
      setError("Please complete all required fields.");
      setLoading(false);
      return;
    }

    try {
      const payload = {
        name: name.trim(),
        description: description.trim(),
        iconName: iconName,
      };
      await onSaveSuccess(payload);
      setSuccess(`Category '${name}' deployed successfully!`);
      setName("");
      setDescription("");
    } catch (err) {
      setError(err.message || "Failed to create category classification.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 animate-fadeIn">
      {/* List Panel */}
      <div className="bg-darkSurface border border-white/10 rounded-2xl p-6 shadow-xl lg:col-span-2 space-y-6">
        <div>
          <h3 className="text-lg font-bold text-white flex items-center gap-2">
            <Layers className="w-5 h-5 text-primaryGreen" />
            Content Classification Genres
          </h3>
          <p className="text-xs text-accentSlate/60">Classify movies inside active categories shown on the mobile homepage</p>
        </div>

        <div className="overflow-x-auto border border-white/5 rounded-xl">
          <table className="w-full border-collapse text-left text-sm">
            <thead>
              <tr className="bg-[#11141a] text-accentSlate/70 border-b border-white/5 uppercase font-mono text-[10px] tracking-wider">
                <th className="px-5 py-4 font-bold">Vector Symbol</th>
                <th className="px-5 py-4 font-bold">Category Name</th>
                <th className="px-5 py-4 font-bold">Description Synopses</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5 text-white/90">
              {categories.length === 0 ? (
                <tr>
                  <td colSpan="3" className="px-5 py-8 text-center text-accentSlate/40 font-mono text-xs">
                    No categories created yet. Create one on the sidebar!
                  </td>
                </tr>
              ) : (
                categories.map((cat) => (
                  <tr key={cat.id} className="hover:bg-white/[0.02] transition">
                    <td className="px-5 py-4 whitespace-nowrap">
                      <div className="flex items-center gap-2 font-mono text-xs text-primaryGreen bg-primaryGreen/5 border border-primaryGreen/15 px-2.5 py-1 rounded-lg w-max">
                        <span className="text-xs">🏷️</span>
                        <span>{cat.iconName || cat.icon_name}</span>
                      </div>
                    </td>
                    <td className="px-5 py-4 whitespace-nowrap font-bold text-white">
                      {cat.name}
                    </td>
                    <td className="px-5 py-4 text-xs text-accentSlate/65 leading-normal">
                      {cat.description}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Deploy Form Sidebar */}
      <div className="bg-darkSurface border border-white/10 rounded-2xl p-6 shadow-xl h-max space-y-6">
        <div>
          <h3 className="text-base font-bold text-white">Deploy New Category</h3>
          <p className="text-xs text-accentSlate/60">Add a custom genre channel option</p>
        </div>

        {error && (
          <div className="bg-red-500/10 border border-red-500/30 text-red-200 p-3 rounded-lg text-xs leading-normal">
            {error}
          </div>
        )}

        {success && (
          <div className="bg-emerald-500/10 border border-emerald-500/30 text-emerald-200 p-3 rounded-lg text-xs leading-normal font-semibold">
            {success}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-accentSlate/80 mb-2 font-mono">
              Genre Name <span className="text-primaryGreen">*</span>
            </label>
            <input
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g. Documentaries"
              className="w-full bg-[#11141a] text-white px-4 py-2 border border-white/10 rounded-xl focus:outline-none focus:border-primaryGreen transition text-sm"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-accentSlate/80 mb-2 font-mono">
              Classification Description <span className="text-primaryGreen">*</span>
            </label>
            <textarea
              required
              rows={3}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Describe what kind of cinematic experience these movies offer..."
              className="w-full bg-[#11141a] text-white px-4 py-2 border border-white/10 rounded-xl focus:outline-none focus:border-primaryGreen transition text-sm"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-accentSlate/80 mb-2 font-mono">
              Android Vector Icon Key
            </label>
            <select
              value={iconName}
              onChange={(e) => setIconName(e.target.value)}
              className="w-full bg-[#11141a] text-white px-4 py-2 border border-white/10 rounded-xl focus:outline-none focus:border-primaryGreen transition text-sm font-mono"
            >
              <option value="auto_awesome">auto_awesome (Magic Sparks)</option>
              <option value="rocket_launch">rocket_launch (Space Sci-Fi)</option>
              <option value="local_fire_department">local_fire_department (Action Thrills)</option>
              <option value="theater_comedy">theater_comedy (Drama Stage)</option>
              <option value="visibility_off">visibility_off (Mystery Puzzles)</option>
            </select>
            <p className="mt-1.5 text-[10px] text-accentSlate/50 leading-relaxed font-mono">
              Maps directly to Material Symbols identifier constants embedded inside the Android compose screens.
            </p>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-[#11141a] hover:bg-white/5 border border-white/10 hover:border-primaryGreen text-white hover:text-primaryGreen font-bold py-3 px-4 rounded-xl transition-all flex items-center justify-center gap-2 text-xs"
          >
            {loading ? (
              <RefreshCw className="w-4 h-4 animate-spin text-primaryGreen" />
            ) : (
              <Plus className="w-4 h-4 text-primaryGreen" />
            )}
            Deploy Category Key
          </button>
        </form>
      </div>
    </div>
  );
}
