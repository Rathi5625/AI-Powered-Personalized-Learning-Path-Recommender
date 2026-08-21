import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { Bell, History, Menu } from 'lucide-react';

interface HelpTopBarProps {
  onToggleMobileMenu?: () => void;
  onViewHistory?: () => void;
}

export const HelpTopBar: React.FC<HelpTopBarProps> = ({
  onToggleMobileMenu,
  onViewHistory,
}) => {
  const [notificationsOpen, setNotificationsOpen] = useState(false);

  return (
    <header className="w-full h-16 sm:h-20 px-4 sm:px-8 flex items-center justify-between gap-4 sticky top-0 z-20 bg-[#f9f9ff]/80 backdrop-blur-xl border-b border-gray-200/60 select-none">
      {/* Left: Mobile Menu & Page Title */}
      <div className="flex items-center gap-3.5">
        <button
          type="button"
          onClick={onToggleMobileMenu}
          aria-label="Open Navigation Menu"
          className="lg:hidden p-2 rounded-xl text-[#0f1b32] hover:bg-black/5 transition-colors cursor-pointer"
        >
          <Menu className="w-5 h-5" />
        </button>

        <div className="text-left space-y-0.5">
          <h1 className="text-base sm:text-lg font-extrabold text-[#0f1b32] tracking-tight leading-tight">
            Help &amp; Support
          </h1>
          <p className="text-[11px] text-gray-400 font-medium hidden sm:block">
            Find answers, get help, or talk to the LearnAI team.
          </p>
        </div>
      </div>

      {/* Right: Notifications, History, Profile Avatar */}
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
                  <p className="font-bold text-[#0f1b32] text-xs">Support Ticket #1042 Updated</p>
                  <p className="text-[11px] text-gray-600 mt-0.5">Your inquiry regarding profile photo uploads is in progress.</p>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Support History */}
        <button
          type="button"
          onClick={onViewHistory}
          aria-label="Support History"
          className="p-2 sm:p-2.5 rounded-full bg-white/80 hover:bg-white border border-gray-200/80 text-gray-600 hover:text-[#0f1b32] transition-colors shadow-2xs cursor-pointer flex items-center justify-center"
        >
          <History className="w-4 h-4" />
        </button>

        {/* User Profile Avatar */}
        <Link
          to="/profile"
          aria-label="User Profile"
          className="w-8 sm:w-9 h-8 sm:h-9 rounded-full bg-[#d98b63] text-white font-extrabold text-xs flex items-center justify-center shadow-2xs cursor-pointer hover:scale-105 transition-transform select-none"
        >
          PR
        </Link>
      </div>
    </header>
  );
};
