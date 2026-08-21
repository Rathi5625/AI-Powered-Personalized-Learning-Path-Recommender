import React, { useState } from 'react';
import { Search, Bell, Settings, ChevronDown, Menu, User, LogOut } from 'lucide-react';

interface ExploreCoursesTopBarProps {
  onToggleMobileMenu?: () => void;
  searchQuery?: string;
  onSearchChange?: (query: string) => void;
}

export const ExploreCoursesTopBar: React.FC<ExploreCoursesTopBarProps> = ({
  onToggleMobileMenu,
  searchQuery = '',
  onSearchChange,
}) => {
  const [profileOpen, setProfileOpen] = useState(false);
  const [notificationsOpen, setNotificationsOpen] = useState(false);

  return (
    <header className="w-full h-20 px-4 sm:px-8 flex items-center justify-between gap-4 sticky top-0 z-20 bg-[#f9f9ff]/80 backdrop-blur-xl border-b border-gray-200/60 select-none">
      {/* Left: Mobile Menu & Search */}
      <div className="flex items-center gap-3 flex-1 max-w-xl">
        <button
          type="button"
          onClick={onToggleMobileMenu}
          aria-label="Open Navigation Menu"
          className="lg:hidden p-2 rounded-xl text-[#0f1b32] hover:bg-black/5 transition-colors cursor-pointer"
        >
          <Menu className="w-5 h-5" />
        </button>

        {/* Search Bar */}
        <div className="relative w-full max-w-md">
          <Search className="w-4 h-4 text-gray-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search courses, skills, topics..."
            value={searchQuery}
            onChange={(e) => onSearchChange?.(e.target.value)}
            className="w-full h-10 pl-9 pr-4 rounded-xl sm:rounded-2xl bg-white/80 backdrop-blur-md border border-gray-200/80 text-xs sm:text-sm text-[#0f1b32] placeholder:text-gray-400 focus:outline-none focus:border-[#8e4d2b] focus:ring-2 focus:ring-[#8e4d2b]/10 transition-all shadow-2xs"
          />
        </div>
      </div>

      {/* Right: Notifications, Settings, and Profile Avatar */}
      <div className="flex items-center gap-2 sm:gap-3">
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

          {/* Notifications Dropdown */}
          {notificationsOpen && (
            <div className="absolute right-0 mt-2 w-72 bg-white rounded-2xl p-3 border border-gray-100 shadow-xl z-50 text-left">
              <div className="flex items-center justify-between pb-2 border-b border-gray-100 mb-2">
                <span className="text-xs font-bold text-[#0f1b32]">Notifications</span>
                <span className="text-[10px] text-[#8e4d2b] font-semibold cursor-pointer">Mark all as read</span>
              </div>
              <div className="space-y-2 text-xs">
                <a
                  href="/course-details"
                  className="block p-2 rounded-xl bg-[#FAF4F0] hover:bg-[#F2DACB]/40 border border-[#F2DACB]/50 transition-colors"
                >
                  <p className="font-bold text-[#0f1b32] text-xs">New Course Recommendation</p>
                  <p className="text-[11px] text-gray-600 mt-0.5">Data Structures &amp; Algorithms Specialization (96% Match)</p>
                </a>
              </div>
            </div>
          )}
        </div>

        {/* Settings Button */}
        <a
          href="/settings"
          aria-label="Settings"
          className="p-2 sm:p-2.5 rounded-full bg-white/80 hover:bg-white border border-gray-200/80 text-gray-600 hover:text-[#0f1b32] transition-colors shadow-2xs cursor-pointer hidden sm:flex items-center justify-center"
        >
          <Settings className="w-4 h-4" />
        </a>

        {/* Profile Avatar Pill */}
        <div className="relative">
          <button
            type="button"
            onClick={() => {
              setProfileOpen(!profileOpen);
              setNotificationsOpen(false);
            }}
            className="flex items-center gap-2 pl-1 pr-2 sm:pr-2.5 py-1 rounded-full bg-white/80 hover:bg-white border border-gray-200/80 shadow-2xs transition-colors cursor-pointer select-none"
          >
            <div className="w-7 h-7 rounded-full bg-[#ffdbcb] border border-[#d98b63]/30 text-[#8e4d2b] font-bold text-xs flex items-center justify-center">
              PS
            </div>
            <span className="text-xs font-bold text-[#0f1b32] hidden md:inline">
              Parth S.
            </span>
            <ChevronDown className="w-3.5 h-3.5 text-gray-400 hidden sm:inline" />
          </button>

          {/* Profile Dropdown */}
          {profileOpen && (
            <div className="absolute right-0 mt-2 w-48 bg-white rounded-2xl p-2 border border-gray-100 shadow-xl z-50 text-left">
              <div className="px-3 py-2 border-b border-gray-100 mb-1">
                <span className="text-xs font-bold text-[#0f1b32] block">Parth S.</span>
                <span className="text-[11px] text-gray-400 block">parth@example.com</span>
              </div>
              <a
                href="/profile"
                className="flex items-center gap-2 px-3 py-2 rounded-xl text-xs text-gray-700 hover:bg-gray-50 transition-colors"
              >
                <User className="w-3.5 h-3.5 text-gray-400" />
                <span>My Profile</span>
              </a>
              <a
                href="/settings"
                className="flex items-center gap-2 px-3 py-2 rounded-xl text-xs text-gray-700 hover:bg-gray-50 transition-colors"
              >
                <Settings className="w-3.5 h-3.5 text-gray-400" />
                <span>Account Settings</span>
              </a>
              <div className="pt-1 border-t border-gray-100 mt-1">
                <a
                  href="/login"
                  className="flex items-center gap-2 px-3 py-2 rounded-xl text-xs text-red-600 hover:bg-red-50 transition-colors"
                >
                  <LogOut className="w-3.5 h-3.5" />
                  <span>Log Out</span>
                </a>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
