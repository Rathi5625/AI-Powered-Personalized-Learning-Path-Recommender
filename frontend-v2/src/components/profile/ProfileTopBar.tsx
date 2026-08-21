import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { Search, Bell, Menu, Bot } from 'lucide-react';

interface ProfileTopBarProps {
  onToggleMobileMenu?: () => void;
  searchQuery?: string;
  onSearchChange?: (query: string) => void;
}

export const ProfileTopBar: React.FC<ProfileTopBarProps> = ({
  onToggleMobileMenu,
  searchQuery = '',
  onSearchChange,
}) => {
  const [notificationsOpen, setNotificationsOpen] = useState(false);

  return (
    <header className="w-full h-16 sm:h-20 px-4 sm:px-8 flex items-center justify-between gap-4 sticky top-0 z-20 bg-[#f9f9ff]/80 backdrop-blur-xl border-b border-gray-200/60 select-none">
      {/* Left: Mobile Menu & Large Search Input */}
      <div className="flex items-center gap-3 flex-1 max-w-md">
        <button
          type="button"
          onClick={onToggleMobileMenu}
          aria-label="Open Navigation Menu"
          className="lg:hidden p-2 rounded-xl text-[#0f1b32] hover:bg-black/5 transition-colors cursor-pointer"
        >
          <Menu className="w-5 h-5" />
        </button>

        {/* Search Field */}
        <div className="relative w-full">
          <Search className="w-4 h-4 text-gray-400 absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => onSearchChange?.(e.target.value)}
            placeholder="Search courses, skills, or mentors..."
            className="w-full pl-9 pr-4 py-2 sm:py-2.5 rounded-full bg-white/80 border border-gray-200/80 text-xs sm:text-sm text-[#0f1b32] placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-[#8e4d2b]/20 focus:border-[#8e4d2b] transition-all shadow-2xs"
          />
        </div>
      </div>

      {/* Right: AI Mentor link, Notifications, PR Avatar */}
      <div className="flex items-center gap-2 sm:gap-3">
        {/* AI Mentor Link */}
        <Link
          to="/ai-mentor"
          aria-label="AI Mentor"
          className="p-2 sm:p-2.5 rounded-full bg-white/80 hover:bg-white border border-gray-200/80 text-gray-600 hover:text-[#8e4d2b] transition-colors shadow-2xs flex items-center justify-center cursor-pointer"
        >
          <Bot className="w-4 h-4" />
        </Link>

        {/* Notifications */}
        <div className="relative">
          <button
            type="button"
            onClick={() => setNotificationsOpen(!notificationsOpen)}
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
                <div className="p-2.5 rounded-xl bg-[#FAF4F0] border border-[#F2DACB]/50">
                  <p className="font-bold text-[#0f1b32] text-xs">Profile 92% Complete</p>
                  <p className="text-[11px] text-gray-600 mt-0.5">Add your GitHub link to reach 100% profile completeness.</p>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* User Avatar Circle */}
        <div className="w-8 sm:w-9 h-8 sm:h-9 rounded-full bg-[#d98b63] text-white font-extrabold text-xs flex items-center justify-center shadow-2xs select-none">
          PR
        </div>
      </div>
    </header>
  );
};
