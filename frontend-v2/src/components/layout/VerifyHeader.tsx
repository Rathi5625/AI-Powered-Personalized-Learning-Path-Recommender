import React from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';

export const VerifyHeader: React.FC = () => {
  return (
    <div className="w-full flex justify-center px-4 pt-5 sm:pt-6 z-10">
      <motion.div
        initial={{ y: -14, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ duration: 0.35, ease: [0.16, 1, 0.3, 1] }}
      >
        <Link
          to="/"
          className="inline-flex items-center px-6 py-2.5 bg-white/90 backdrop-blur-xl rounded-full border border-white/80 shadow-[0_6px_24px_rgba(0,0,0,0.05)] select-none hover:shadow-[0_8px_28px_rgba(0,0,0,0.08)] transition-shadow"
        >
          <span className="font-extrabold text-base sm:text-lg text-[#1A1F36] tracking-tight">
            LearnAI
          </span>
        </Link>
      </motion.div>
    </div>
  );
};
