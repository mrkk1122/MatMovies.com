import React, { useState } from "react";
import { Shield, Trash2, Calendar, Mail, UserCheck, ShieldAlert, Award, CreditCard, ArrowUpDown, ArrowUp, ArrowDown } from "lucide-react";

export default function UserTable({ users, currentAdmin, onRoleChange, onSubscriptionChange, onDeleteClick }) {
  const [searchTerm, setSearchTerm] = useState("");
  const [sortField, setSortField] = useState(null); // 'createdAt' or 'subscription'
  const [sortDirection, setSortDirection] = useState("asc"); // 'asc' or 'desc'

  const filteredUsers = users.filter((user) => {
    const term = searchTerm.toLowerCase();
    return (
      user.username.toLowerCase().includes(term) ||
      user.email.toLowerCase().includes(term)
    );
  });

  const handleSort = (field) => {
    if (sortField === field) {
      setSortDirection(sortDirection === "asc" ? "desc" : "asc");
    } else {
      setSortField(field);
      setSortDirection("asc");
    }
  };

  const getSubRank = (user) => {
    const status = user.subscriptionStatus || user.subscription_status || "Free";
    const s = status.toUpperCase();
    if (s === "VIP") return 3;
    if (s === "PREMIUM") return 2;
    return 1;
  };

  const sortedUsers = [...filteredUsers].sort((a, b) => {
    if (!sortField) return 0;

    let valA, valB;
    if (sortField === "createdAt") {
      valA = new Date(a.createdAt || a.created_at || 0).getTime();
      valB = new Date(b.createdAt || b.created_at || 0).getTime();
    } else if (sortField === "subscription") {
      valA = getSubRank(a);
      valB = getSubRank(b);
    }

    if (valA < valB) return sortDirection === "asc" ? -1 : 1;
    if (valA > valB) return sortDirection === "asc" ? 1 : -1;
    return 0;
  });

  const renderSortIcon = (field) => {
    if (sortField !== field) {
      return <ArrowUpDown className="w-3.5 h-3.5 opacity-40 hover:opacity-100 transition inline-block ml-1 shrink-0" />;
    }
    return sortDirection === "asc" ? (
      <ArrowUp className="w-3.5 h-3.5 text-primaryGreen inline-block ml-1 shrink-0" />
    ) : (
      <ArrowDown className="w-3.5 h-3.5 text-primaryGreen inline-block ml-1 shrink-0" />
    );
  };

  const formatDate = (timestamp) => {
    if (!timestamp) return "N/A";
    const d = new Date(timestamp);
    return d.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
  };

  return (
    <div className="bg-darkSurface border border-white/10 rounded-2xl p-6 shadow-xl space-y-6 animate-fadeIn">
      
      {/* Top Header Controls */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h3 className="text-lg font-bold text-white flex items-center gap-2">
            <Shield className="w-5 h-5 text-primaryGreen" />
            Registered Users & Accounts
          </h3>
          <p className="text-xs text-accentSlate/60">Promote administrative team members or adjust customer subscriptions</p>
        </div>
      </div>

      {/* Search Input Bar */}
      <div className="relative">
        <span className="absolute inset-y-0 left-0 flex items-center pl-3.5 text-accentSlate/40">
          <Mail className="w-4 h-4" />
        </span>
        <input
          type="text"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          placeholder="Search by registered username or email account..."
          className="w-full bg-[#11141a] text-white pl-10 pr-4 py-2.5 border border-white/10 rounded-xl focus:outline-none focus:border-primaryGreen transition text-sm"
        />
      </div>

      {/* Users Table */}
      <div className="overflow-x-auto border border-white/5 rounded-xl">
        <table className="w-full border-collapse text-left text-sm">
          <thead>
            <tr className="bg-[#11141a] text-accentSlate/70 border-b border-white/5 uppercase font-mono text-[10px] tracking-wider select-none">
              <th className="px-5 py-4 font-bold">User Identity</th>
              <th className="px-5 py-4 font-bold">System Role</th>
              <th 
                className="px-5 py-4 font-bold cursor-pointer hover:text-white transition flex items-center gap-1"
                onClick={() => handleSort("subscription")}
              >
                <span>Subscription Plan</span>
                {renderSortIcon("subscription")}
              </th>
              <th 
                className="px-5 py-4 font-bold cursor-pointer hover:text-white transition"
                onClick={() => handleSort("createdAt")}
              >
                <span className="inline-flex items-center gap-1">
                  <span>Created Date</span>
                  {renderSortIcon("createdAt")}
                </span>
              </th>
              <th className="px-5 py-4 font-bold text-right">Delete Account</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-white/5 text-white/90">
            {sortedUsers.length === 0 ? (
              <tr>
                <td colSpan="5" className="px-5 py-8 text-center text-accentSlate/40 font-mono text-xs">
                  No user accounts registered matching search criteria.
                </td>
              </tr>
            ) : (
              sortedUsers.map((user) => {
                const isSelf = currentAdmin && currentAdmin.id === user.id;
                
                return (
                  <tr key={user.id} className="hover:bg-white/[0.02] transition">
                    {/* User Identity */}
                    <td className="px-5 py-4">
                      <div className="flex items-center gap-3">
                        <div className="w-9 h-9 rounded-full bg-gradient-to-br from-primaryGreen/20 to-neonPurple/20 border border-white/15 flex items-center justify-center font-bold text-sm text-primaryGreen font-mono uppercase shrink-0">
                          {user.username.slice(0, 2)}
                        </div>
                        <div>
                          <div className="font-bold text-white flex items-center gap-1.5 text-sm">
                            <span>{user.username}</span>
                            {isSelf && (
                              <span className="text-[9px] font-mono uppercase bg-primaryGreen/15 border border-primaryGreen/35 text-primaryGreen px-1.5 py-0.5 rounded">
                                You
                              </span>
                            )}
                          </div>
                          <span className="text-xs text-accentSlate/50 font-mono block mt-0.5">{user.email}</span>
                        </div>
                      </div>
                    </td>

                    {/* System Role */}
                    <td className="px-5 py-4 whitespace-nowrap">
                      <div className="flex items-center gap-2">
                        {isSelf ? (
                          <span className="text-xs font-bold text-primaryGreen bg-primaryGreen/5 border border-primaryGreen/15 px-2.5 py-1 rounded-lg flex items-center gap-1 font-mono">
                            <UserCheck className="w-3.5 h-3.5 shrink-0" />
                            Admin Account
                          </span>
                        ) : (
                          <select
                            value={user.role}
                            onChange={(e) => {
                              if (window.confirm(`Are you sure you want to change role of ${user.username} to ${e.target.value}?`)) {
                                onRoleChange(user.id, e.target.value);
                              }
                            }}
                            className="bg-[#11141a] text-white text-xs border border-white/10 rounded-lg px-2 py-1.5 focus:outline-none focus:border-primaryGreen font-semibold transition cursor-pointer"
                          >
                            <option value="User">User Access</option>
                            <option value="Admin">Admin Portal Access</option>
                          </select>
                        )}
                      </div>
                    </td>

                    {/* Subscription Tier */}
                    <td className="px-5 py-4 whitespace-nowrap">
                      <div className="flex items-center gap-2">
                        <select
                          value={user.subscriptionStatus || user.subscription_status}
                          onChange={(e) => {
                            if (window.confirm(`Update subscription plan of ${user.username} to ${e.target.value}?`)) {
                              onSubscriptionChange(user.id, e.target.value);
                            }
                          }}
                          className={`text-xs border rounded-lg px-2 py-1.5 focus:outline-none font-bold transition cursor-pointer bg-[#11141a] ${
                            (user.subscriptionStatus || user.subscription_status) === "VIP" ? "text-amber-400 border-amber-400/20 hover:border-amber-400" :
                            (user.subscriptionStatus || user.subscription_status) === "Premium" ? "text-purple-400 border-purple-400/20 hover:border-purple-400" :
                            "text-accentSlate/60 border-white/10 hover:border-accentSlate"
                          }`}
                        >
                          <option value="Free">Free Basic Plan</option>
                          <option value="Premium">Premium HD Plan</option>
                          <option value="VIP">VIP Gold 4K Plan</option>
                        </select>
                      </div>
                    </td>

                    {/* Created Date */}
                    <td className="px-5 py-4 whitespace-nowrap font-mono text-xs text-accentSlate/75">
                      <div className="flex items-center gap-1.5">
                        <Calendar className="w-3.5 h-3.5 text-accentSlate/40" />
                        <span>{formatDate(user.createdAt || user.created_at)}</span>
                      </div>
                    </td>

                    {/* Delete Action */}
                    <td className="px-5 py-4 whitespace-nowrap text-right">
                      {isSelf ? (
                        <span className="text-[10px] font-mono text-accentSlate/30">Protected</span>
                      ) : (
                        <button
                          onClick={() => {
                            if (window.confirm(`WARNING: Deleting this account will permanently clear this user and all associated playlists, comments, and metrics. Proceed with deletion of '${user.username}'?`)) {
                              onDeleteClick(user.id);
                            }
                          }}
                          title="Delete user account"
                          className="p-1.5 bg-[#11141a] hover:bg-red-500/10 border border-white/5 rounded-lg text-accentSlate hover:text-red-400 transition ml-auto block"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      )}
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
