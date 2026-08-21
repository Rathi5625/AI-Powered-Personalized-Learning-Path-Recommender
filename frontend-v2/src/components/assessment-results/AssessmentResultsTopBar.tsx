import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { ChevronRight, Bell, Settings, Menu, User, LogOut } from 'lucide-react';

interface AssessmentResultsTopBarProps {
  onToggleMobileMenu?: () => void;
}

export const AssessmentResultsTopBar: React.FC<AssessmentResultsTopBarProps> = ({
  onToggleMobileMenu,
}) => {
  const [profileOpen, setProfileOpen] = useState(false);
  const [notificationsOpen, setNotificationsOpen] = useState(false);

  return (
    <header className="w-full h-16 sm:h-18 px-4 sm:px-8 flex items-center justify-between gap-4 sticky top-0 z-20 bg-[#f9f9ff]/80 backdrop-blur-xl border-b border-gray-200/60 select-none">
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

        {/* Breadcrumb Navigation */}
        <nav aria-label="Breadcrumb" className="flex items-center gap-1.5 sm:gap-2 text-xs sm:text-sm font-medium text-gray-500">
          <Link to="/assessments" className="hover:text-[#8e4d2b] transition-colors cursor-pointer">
            Assessments
          </Link>
          <ChevronRight className="w-3.5 h-3.5 text-gray-400 shrink-0" />
          <Link to="/assessment" className="hover:text-[#8e4d2b] transition-colors cursor-pointer">
            DSA Assessment
          </Link>
          <ChevronRight className="w-3.5 h-3.5 text-gray-400 shrink-0" />
          <span className="font-bold text-[#8e4d2b]">
            Results
          </span>
        </nav>
      </div>

      {/* Right: Notifications, Settings, Profile Avatar */}
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
                <div className="p-2.5 rounded-xl bg-[#FAF4F0] border border-[#F2DACB]/50">
                  <p className="font-bold text-[#0f1b32] text-xs">Skill Proficiency Updated</p>
                  <p className="text-[11px] text-gray-600 mt-0.5">DSA score increased to 68% (+7%).</p>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Settings Button */}
        <Link
          to="/settings"
          aria-label="Settings"
          className="p-2 sm:p-2.5 rounded-full bg-white/80 hover:bg-white border border-gray-200/80 text-gray-600 hover:text-[#0f1b32] transition-colors shadow-2xs cursor-pointer hidden sm:flex items-center justify-center"
        >
          <Settings className="w-4 h-4" />
        </Link>

        {/* Profile Avatar Circle */}
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
            PS
          </button>

          {/* Profile Dropdown */}
          {profileOpen && (
            <div className="absolute right-0 mt-2 w-48 bg-white rounded-2xl p-2 border border-gray-100 shadow-xl z-50 text-left">
              <div className="px-3 py-2 border-b border-gray-100 mb-1">
                <span className="text-xs font-bold text-[#0f1b32] block">Parth S.</span>
                <span className="text-[11px] text-gray-400 block">parth@example.com</span>
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
