import React from 'react';
import { motion } from 'framer-motion';

interface ProfileHeroProps {
  name?: string;
  role?: string;
  completeness?: number;
}

export const ProfileHero: React.FC<ProfileHeroProps> = ({
  name = 'Parth Rathi',
  role = 'Software Engineer',
  completeness = 92,
}) => {
  return (
    <section
      aria-label="Profile Summary Header"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-8 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left flex flex-col md:flex-row items-center justify-between gap-6"
    >
      {/* Left User Identity */}
      <div className="flex items-center gap-4 sm:gap-5 w-full md:w-auto">
        {/* Large PR Avatar */}
        <div className="w-16 sm:w-20 h-16 sm:h-20 rounded-full bg-[#d98b63] text-white font-extrabold text-2xl sm:text-3xl flex items-center justify-center shrink-0 shadow-md">
          PR
        </div>

        {/* Name, Role & Status */}
        <div className="space-y-1.5">
          <h1 className="text-2xl sm:text-3xl font-extrabold text-[#0f1b32] tracking-tight">
            {name}
          </h1>
          <span className="text-sm font-semibold text-gray-500 block">
            {role}
          </span>
          <div className="inline-flex items-center gap-1.5 px-3 py-0.5 rounded-full bg-[#FAF4F0] border border-[#F2DACB] text-[#8e4d2b] text-[11px] font-bold">
            <span className="w-1.5 h-1.5 rounded-full bg-[#8e4d2b] animate-pulse" />
            <span>Learning with LearnAI</span>
          </div>
        </div>
      </div>

      {/* Right Profile Completeness */}
      <div className="w-full md:w-72 space-y-2 text-left md:text-right">
        <div className="flex items-center justify-between md:justify-end gap-3 text-xs font-bold">
          <span className="text-gray-500">Profile Completeness</span>
          <span className="text-[#8e4d2b] text-sm font-extrabold">{completeness}%</span>
        </div>

        {/* Progress Bar */}
        <div className="w-full h-2 bg-[#FAF4F0] rounded-full overflow-hidden">
          <motion.div
            initial={{ width: 0 }}
            animate={{ width: `${completeness}%` }}
            transition={{ duration: 1, ease: 'easeOut' }}
            className="h-full bg-[#d98b63] rounded-full"
          />
        </div>

        <p className="text-[11px] text-gray-400 font-medium">
          Add your GitHub link to reach 100%
        </p>
      </div>
    </section>
  );
};
