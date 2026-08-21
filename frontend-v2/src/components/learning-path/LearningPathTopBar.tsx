import React from 'react';
import { Sparkles, Bell, Menu } from 'lucide-react';

interface LearningPathTopBarProps {
  onToggleMobileMenu?: () => void;
  onOpenAIMentor?: () => void;
}

export const LearningPathTopBar: React.FC<LearningPathTopBarProps> = ({
  onToggleMobileMenu,
  onOpenAIMentor,
}) => {
  return (
    <header className="sticky top-0 z-20 w-full bg-white/70 backdrop-blur-xl border-b border-gray-200/80 px-4 sm:px-8 py-3 flex items-center justify-between">
      {/* Left: Mobile Menu Toggle & Brand (Visible on mobile only) */}
      <div className="flex items-center gap-3 lg:hidden">
        <button
          type="button"
          onClick={onToggleMobileMenu}
          aria-label="Toggle Navigation Menu"
          className="p-2 rounded-xl text-[#0f1b32] hover:bg-black/5 transition-colors cursor-pointer"
        >
          <Menu className="w-5 h-5" />
        </button>
        <div className="flex items-center gap-2">
          <div className="w-7 h-7 rounded-lg bg-[#8e4d2b] text-white flex items-center justify-center font-bold text-xs shadow-xs">
            L
          </div>
          <span className="font-extrabold text-sm text-[#0f1b32]">LearnAI</span>
        </div>
      </div>

      {/* Desktop Spacer */}
      <div className="hidden lg:block"></div>

      {/* Right Controls */}
      <div className="flex items-center gap-2.5 sm:gap-3.5">
        {/* AI Mentor Button */}
        <button
          type="button"
          onClick={onOpenAIMentor}
          className="inline-flex items-center gap-1.5 px-3 sm:px-4 py-2 rounded-full bg-[#FAF4F0] hover:bg-[#F2E8E1] border border-[#F2DACB] text-xs sm:text-sm font-semibold text-[#8e4d2b] transition-colors shadow-2xs cursor-pointer"
        >
          <Sparkles className="w-3.5 h-3.5 text-[#8e4d2b]" />
          <span>AI Mentor</span>
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

        {/* User Initials Avatar */}
        <div className="w-8 h-8 sm:w-9 sm:h-9 rounded-full bg-[#ffdbcb] border border-[#d98b63]/30 text-[#8e4d2b] font-bold text-xs sm:text-sm flex items-center justify-center shadow-2xs select-none">
          JD
        </div>
      </div>
    </header>
  );
};
