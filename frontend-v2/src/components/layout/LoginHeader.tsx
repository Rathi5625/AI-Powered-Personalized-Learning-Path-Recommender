import React from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowLeft } from 'lucide-react';

export const LoginHeader: React.FC = () => {
  return (
    <div className="w-full flex justify-center px-4 sm:px-6 pt-5 sm:pt-6">
      <motion.header
        initial={{ y: -15, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ duration: 0.35, ease: [0.16, 1, 0.3, 1] }}
        className="w-full max-w-5xl bg-white/80 backdrop-blur-xl border border-white/80 rounded-full px-5 sm:px-7 py-3 sm:py-3.5 shadow-[0_8px_30px_rgba(0,0,0,0.04)] flex items-center justify-between transition-colors"
      >
        {/* Left: Brand Mark */}
        <Link
          to="/"
          className="flex items-center gap-2.5 group select-none cursor-pointer"
        >
          <div className="w-8 h-8 rounded-xl bg-[#A06A42] text-white flex items-center justify-center font-bold text-sm shadow-xs group-hover:scale-105 transition-transform">
            <span>L</span>
          </div>
          <span className="font-extrabold text-base sm:text-lg tracking-tight text-[#1A1F36]">
            LearnAI
          </span>
        </Link>

        {/* Right: Back to Website Link */}
        <Link
          to="/"
          className="inline-flex items-center gap-2 text-xs sm:text-sm font-medium text-gray-600 hover:text-[#1A1F36] px-3 py-1.5 rounded-full hover:bg-black/[0.03] transition-colors"
        >
          <ArrowLeft className="w-4 h-4 text-gray-500" />
          <span>Back to website</span>
        </Link>
      </motion.header>
    </div>
  );
};
