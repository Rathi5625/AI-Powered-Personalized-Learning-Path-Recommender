import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { ChevronRight, Search, Bell, Menu, User, Settings, LogOut } from 'lucide-react';

interface ProjectDetailsTopBarProps {
  onToggleMobileMenu?: () => void;
  searchQuery?: string;
  onSearchChange?: (query: string) => void;
}

export const ProjectDetailsTopBar: React.FC<ProjectDetailsTopBarProps> = ({
  onToggleMobileMenu,
  searchQuery = '',
  onSearchChange,
}) => {
  const [profileOpen, setProfileOpen] = useState(false);
  const [notificationsOpen, setNotificationsOpen] = useState(false);

  return (
    <header className="w-full h-18 sm:h-20 px-4 sm:px-8 flex items-center justify-between gap-4 sticky top-0 z-20 bg-[#f9f9ff]/80 backdrop-blur-xl border-b border-gray-200/60 select-none">
      {/* Left: Mobile Menu & Breadcrumbs */}
      <div className="flex items-center gap-3">
        <button
          type="button"
          onClick={onToggleMobileMenu}
          aria-label="Open Navigation Menu"
          className="lg:hidden p-2 rounded-xl text-[#0f1b32] hover:bg-black/5 transition-colors cursor-pointer"
        >
          <Menu className="w-5 h-5" />
        </button>

        {/* Breadcrumbs */}
        <nav aria-label="Breadcrumb" className="flex items-center gap-1.5 sm:gap-2 text-xs sm:text-sm font-medium text-gray-500">
          <Link to="/projects" className="hover:text-[#8e4d2b] transition-colors cursor-pointer">
            Projects
          </Link>
          <ChevronRight className="w-3.5 h-3.5 text-gray-400 shrink-0" />
          <span className="font-bold text-[#8e4d2b]">
            Project Details
          </span>
        </nav>
      </div>

      {/* Right: Search, Notifications, Avatar */}
      <div className="flex items-center gap-2 sm:gap-4">
        {/* Search Input Field */}
        <div className="relative hidden md:block w-56 lg:w-64">
          <Search className="w-4 h-4 text-gray-400 absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => onSearchChange?.(e.target.value)}
            placeholder="Search projects..."
            className="w-full pl-9 pr-4 py-2 rounded-full bg-white/80 border border-gray-200/80 text-xs text-[#0f1b32] placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-[#8e4d2b]/20 focus:border-[#8e4d2b] transition-all shadow-2xs"
          />
        </div>

        {/* Notifications */}
        <div className="relative">
          <button
            type="button"
            onClick={() => {
              setNotificationsOpen(!notificationsOpen);
              setProfileOpen(false);
            }}
            aria-label="Notifications"
            className="relative p-2 sm:p-2.5 rounded-full bg-white/80 hover:bg-white border border-gray-200/80 text-gray-600 hover:text-[#0f1b32] transition-colors shadow-2xs cursor-pointer"
          >
            <Bell className="w-4 h-4" />
            <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-[#8e4d2b] ring-2 ring-white" />
          </button>

          {/* Notifications Menu */}
          {notificationsOpen && (
            <div className="absolute right-0 mt-2 w-72 bg-white rounded-2xl p-3 border border-gray-100 shadow-xl z-50 text-left">
              <div className="flex items-center justify-between pb-2 border-b border-gray-100 mb-2">
                <span className="text-xs font-bold text-[#0f1b32]">Notifications</span>
                <span className="text-[10px] text-[#8e4d2b] font-semibold cursor-pointer">Mark all as read</span>
              </div>
              <div className="space-y-2 text-xs">
                <div className="p-2.5 rounded-xl bg-[#FAF4F0] border border-[#F2DACB]/50">
                  <p className="font-bold text-[#0f1b32] text-xs">Build Plan Ready</p>
                  <p className="text-[11px] text-gray-600 mt-0.5">Day 3: Authentication &amp; JWT is active.</p>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Profile Avatar JD */}
        <div className="relative">
          <button
            type="button"
            onClick={() => {
              setProfileOpen(!profileOpen);
              setNotificationsOpen(false);
            }}
            aria-label="User Profile"
            className="w-8 sm:w-9 h-8 sm:h-9 rounded-full bg-[#ffdbcb] border border-[#d98b63]/40 text-[#8e4d2b] font-bold text-xs flex items-center justify-center shadow-2xs hover:scale-105 transition-transform cursor-pointer"
          >
            JD
          </button>

          {profileOpen && (
            <div className="absolute right-0 mt-2 w-48 bg-white rounded-2xl p-2 border border-gray-100 shadow-xl z-50 text-left">
              <div className="px-3 py-2 border-b border-gray-100 mb-1">
                <span className="text-xs font-bold text-[#0f1b32] block">John Doe</span>
                <span className="text-[11px] text-gray-400 block">john.doe@example.com</span>
              </div>
              <Link
                to="/profile"
                className="flex items-center gap-2 px-3 py-2 rounded-xl text-xs text-gray-700 hover:bg-gray-50 transition-colors"
              >
                <User className="w-3.5 h-3.5 text-gray-400" />
                <span>My Profile</span>
              </Link>
              <Link
                to="/settings"
                className="flex items-center gap-2 px-3 py-2 rounded-xl text-xs text-gray-700 hover:bg-gray-50 transition-colors"
              >
                <Settings className="w-3.5 h-3.5 text-gray-400" />
                <span>Settings</span>
              </Link>
              <div className="pt-1 border-t border-gray-100 mt-1">
                <Link
                  to="/login"
                  className="flex items-center gap-2 px-3 py-2 rounded-xl text-xs text-red-600 hover:bg-red-50 transition-colors"
                >
                  <LogOut className="w-3.5 h-3.5" />
                  <span>Log Out</span>
                </Link>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
