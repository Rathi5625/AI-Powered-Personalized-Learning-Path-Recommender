import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { Bell, Settings, Menu, Bot } from 'lucide-react';

interface ProgressTopBarProps {
  onToggleMobileMenu?: () => void;
}

export const ProgressTopBar: React.FC<ProgressTopBarProps> = ({
  onToggleMobileMenu,
}) => {
  const [notificationsOpen, setNotificationsOpen] = useState(false);

  return (
    <header className="w-full h-16 sm:h-18 px-4 sm:px-8 flex items-center justify-between gap-4 sticky top-0 z-20 bg-[#f9f9ff]/80 backdrop-blur-xl border-b border-gray-200/60 select-none">
      {/* Left: Mobile Menu & Top Nav Tabs */}
      <div className="flex items-center gap-4 sm:gap-6">
        <button
          type="button"
          onClick={onToggleMobileMenu}
          aria-label="Open Navigation Menu"
          className="lg:hidden p-2 rounded-xl text-[#0f1b32] hover:bg-black/5 transition-colors cursor-pointer"
        >
          <Menu className="w-5 h-5" />
        </button>

        {/* Sub-Navigation Links */}
        <nav aria-label="Progress Navigation" className="flex items-center gap-4 sm:gap-6 text-xs sm:text-sm font-semibold">
          <Link
            to="/progress"
            className="text-[#0f1b32] font-extrabold border-b-2 border-[#8e4d2b] pb-0.5"
          >
            Progress
          </Link>
          <Link
            to="/learning-path"
            className="text-gray-500 hover:text-[#8e4d2b] transition-colors"
          >
            Curriculum
          </Link>
          <Link
            to="/explore-courses"
            className="text-gray-500 hover:text-[#8e4d2b] transition-colors"
          >
            Resources
          </Link>
        </nav>
      </div>

      {/* Right: Notifications, Settings, AI Mentor / Profile */}
      <div className="flex items-center gap-2 sm:gap-3">
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
                  <p className="font-bold text-[#0f1b32] text-xs">Career Readiness Up!</p>
                  <p className="text-[11px] text-gray-600 mt-0.5">Software Engineer readiness improved by +8% to 72%.</p>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Settings */}
        <Link
          to="/settings"
          aria-label="Settings"
          className="p-2 sm:p-2.5 rounded-full bg-white/80 hover:bg-white border border-gray-200/80 text-gray-600 hover:text-[#0f1b32] transition-colors shadow-2xs cursor-pointer hidden sm:flex items-center justify-center"
        >
          <Settings className="w-4 h-4" />
        </Link>

        {/* AI Mentor Quick Link */}
        <Link
          to="/ai-mentor"
          aria-label="AI Mentor"
          className="p-2 sm:p-2.5 rounded-full bg-[#FAF4F0] hover:bg-[#F2DACB]/60 border border-[#F2DACB] text-[#8e4d2b] transition-colors shadow-2xs flex items-center justify-center cursor-pointer"
        >
          <Bot className="w-4 h-4" />
        </Link>
      </div>
    </header>
  );
};
