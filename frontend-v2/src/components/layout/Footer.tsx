import React from 'react';
import { Sparkles } from 'lucide-react';

export const Footer: React.FC = () => {
  return (
    <footer className="w-full bg-[#FAF8F5]/90 border-t border-black/[0.05] py-8 sm:py-10 mt-auto relative z-10">
      <div className="max-w-7xl mx-auto px-6 sm:px-8 flex flex-col md:flex-row items-center justify-between gap-6 text-xs sm:text-sm text-gray-500">
        {/* Left: Brand */}
        <div className="flex items-center gap-2.5">
          <div className="w-6 h-6 rounded-lg bg-[#1A1F36] text-white flex items-center justify-center shadow-xs">
            <Sparkles className="w-3 h-3 text-[#FFB091]" />
          </div>
          <span className="font-extrabold text-base tracking-tight text-[#1A1F36]">
            LearnAI
          </span>
        </div>

        {/* Center: Links */}
        <div className="flex flex-wrap items-center justify-center gap-5 sm:gap-8 font-medium">
          <a
            href="#privacy"
            className="hover:text-[#1A1F36] transition-colors"
          >
            Privacy Policy
          </a>
          <a
            href="#terms"
            className="hover:text-[#1A1F36] transition-colors"
          >
            Terms of Service
          </a>
          <a
            href="#help"
            className="hover:text-[#1A1F36] transition-colors"
          >
            Help Center
          </a>
          <a
            href="#api"
            className="hover:text-[#1A1F36] transition-colors"
          >
            API Reference
          </a>
        </div>

        {/* Right: Copyright */}
        <div className="text-gray-400 text-center md:text-right font-normal">
          © 2024 LearnAI Platform. All rights reserved.
        </div>
      </div>
    </footer>
  );
};
