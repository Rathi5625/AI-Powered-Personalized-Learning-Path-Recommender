import React from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';

interface ResetHeaderProps {
  className?: string;
}

export const ResetHeader: React.FC<ResetHeaderProps> = () => {
  return (
    <div className="w-full px-4 sm:px-6 pt-5 sm:pt-6">
      <motion.header
        initial={{ y: -15, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ duration: 0.35, ease: [0.16, 1, 0.3, 1] }}
        className="w-full max-w-5xl mx-auto bg-white/80 backdrop-blur-xl border border-white/80 rounded-full px-6 sm:px-8 py-3 sm:py-3.5 shadow-[0_8px_30px_rgba(0,0,0,0.04)] flex items-center justify-between"
      >
        {/* Left: Brand */}
        <Link
          to="/"
          className="font-extrabold text-base sm:text-lg tracking-tight text-[#1A1F36] hover:text-[#A06A42] transition-colors select-none"
        >
          LearnAI
        </Link>

        {/* Right: Sign In */}
        <Link
          to="/login"
          className="text-xs sm:text-sm font-semibold text-[#A06A42] hover:text-[#8D5832] transition-colors"
        >
          Sign In
        </Link>
      </motion.header>
    </div>
  );
};
