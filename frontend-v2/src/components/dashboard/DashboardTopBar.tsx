import React, { useState } from 'react';
import { Search, Bot, Bell, ChevronDown, Menu, User, LogOut, Settings as SettingsIcon } from 'lucide-react';

interface DashboardTopBarProps {
  userName?: string;
  onToggleMobileMenu?: () => void;
  onOpenAIMentor?: () => void;
}

export const DashboardTopBar: React.FC<DashboardTopBarProps> = ({
  userName = 'Parth',
  onToggleMobileMenu,
  onOpenAIMentor,
}) => {
  const [profileOpen, setProfileOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  return (
    <div className="w-full flex flex-col md:flex-row md:items-center justify-between gap-4 pb-6 text-left">
      {/* Left: Heading and Subtitle */}
      <div>
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={onToggleMobileMenu}
            aria-label="Open Navigation Menu"
            className="lg:hidden p-2 rounded-xl text-[#0f1b32] hover:bg-black/5 transition-colors cursor-pointer"
          >
            <Menu className="w-5 h-5" />
          </button>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-[#0f1b32] tracking-tight">
            Dashboard
          </h1>
        </div>
        <p className="text-sm font-bold text-[#8e4d2b] mt-0.5">
          Good morning, {userName} 👋
        </p>
        <p className="text-xs text-gray-500 font-normal">
          Here&apos;s what your learning journey looks like today.
        </p>
      </div>

      {/* Right: Search, Actions & Profile */}
      <div className="flex items-center gap-2.5 sm:gap-3 flex-wrap">
        {/* Search Bar */}
        <div className="relative w-full sm:w-60 md:w-64">
          <Search className="w-4 h-4 text-gray-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search courses, skills..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full h-10 pl-9 pr-4 rounded-xl sm:rounded-2xl bg-white/80 backdrop-blur-md border border-gray-200/80 text-xs text-[#0f1b32] placeholder:text-gray-400 focus:outline-none focus:border-[#8e4d2b] focus:ring-2 focus:ring-[#8e4d2b]/10 transition-all shadow-2xs"
          />
        </div>

        {/* AI Mentor Quick Button */}
        <button
          type="button"
          onClick={onOpenAIMentor}
          aria-label="Open AI Mentor"
          className="p-2 sm:p-2.5 rounded-full bg-white/80 hover:bg-white border border-gray-200/80 text-[#8e4d2b] hover:text-[#783e20] transition-colors shadow-2xs cursor-pointer"
        >
          <Bot className="w-4 h-4" />
        </button>

        {/* Notifications Button */}
        <button
          type="button"
          aria-label="Notifications"
          className="relative p-2 sm:p-2.5 rounded-full bg-white/80 hover:bg-white border border-gray-200/80 text-gray-600 hover:text-[#0f1b32] transition-colors shadow-2xs cursor-pointer"
        >
          <Bell className="w-4 h-4" />
          <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-[#8e4d2b] ring-2 ring-white" />
        </button>

        {/* Profile Dropdown Control */}
        <div className="relative">
          <button
            type="button"
            onClick={() => setProfileOpen(!profileOpen)}
            className="flex items-center gap-2 pl-1 pr-2.5 py-1 rounded-full bg-white/80 hover:bg-white border border-gray-200/80 shadow-2xs transition-colors cursor-pointer select-none"
          >
            <div className="w-7 h-7 rounded-full bg-[#ffdbcb] border border-[#d98b63]/30 text-[#8e4d2b] font-bold text-xs flex items-center justify-center">
              PS
            </div>
            <span className="text-xs font-bold text-[#0f1b32] hidden sm:inline">
              Parth S.
            </span>
            <ChevronDown className="w-3.5 h-3.5 text-gray-400" />
          </button>

          {/* Dropdown Menu */}
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
                <SettingsIcon className="w-3.5 h-3.5 text-gray-400" />
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
    </div>
  );
};
