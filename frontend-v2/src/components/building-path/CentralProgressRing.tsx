import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Sparkles, Target, Brain, Clock } from 'lucide-react';

interface CentralProgressRingProps {
  targetRole?: string;
  experience?: string;
  learningTime?: string;
}

export const CentralProgressRing: React.FC<CentralProgressRingProps> = ({
  targetRole = 'Software Engineer',
  experience = 'Intermediate',
  learningTime = '10 hrs/week',
}) => {
  const [percent, setPercent] = useState(0);

  useEffect(() => {
    const timer = setTimeout(() => {
      setPercent(73);
    }, 100);
    return () => clearTimeout(timer);
  }, []);

  const radius = 80;
  const strokeWidth = 12;
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = circumference - (percent / 100) * circumference;

  return (
    <div className="relative flex flex-col items-center justify-center py-4 w-full">
      {/* Top Status Pill */}
      <motion.div
        initial={{ opacity: 0, y: -10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
        className="mb-6 inline-flex items-center gap-1.5 px-4 py-1.5 rounded-full bg-white/80 backdrop-blur-md border border-white/90 shadow-sm text-xs font-semibold text-[#CC7D52]"
      >
        <motion.div
          animate={{ opacity: [0.6, 1, 0.6], scale: [0.95, 1.05, 0.95] }}
          transition={{ repeat: Infinity, duration: 1.8, ease: 'easeInOut' }}
        >
          <Sparkles className="w-3.5 h-3.5" />
        </motion.div>
        <span>Identifying skill gaps...</span>
      </motion.div>

      {/* Center SVG Ring Container */}
      <div className="relative flex items-center justify-center">
        <svg
          className="w-52 h-52 sm:w-60 sm:h-60 transform -rotate-90"
          viewBox="0 0 200 200"
        >
          {/* Background Track */}
          <circle
            cx="100"
            cy="100"
            r={radius}
            fill="none"
            stroke="rgba(234, 232, 255, 0.7)"
            strokeWidth={strokeWidth}
          />

          {/* Animated Progress Stroke */}
          <circle
            cx="100"
            cy="100"
            r={radius}
            fill="none"
            stroke="#CC7D52"
            strokeWidth={strokeWidth}
            strokeDasharray={circumference}
            strokeDashoffset={strokeDashoffset}
            strokeLinecap="round"
            style={{
              transition: 'stroke-dashoffset 1.4s cubic-bezier(0.16, 1, 0.3, 1)',
            }}
          />
        </svg>

        {/* Center Percentage Display */}
        <div className="absolute flex flex-col items-center justify-center text-center">
          <motion.span
            initial={{ opacity: 0, scale: 0.8 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ delay: 0.3, duration: 0.5 }}
            className="text-3xl sm:text-4xl font-extrabold text-[#1A1F36] tracking-tight"
          >
            {percent}%
          </motion.span>
          <span className="text-[10px] sm:text-[11px] font-bold text-gray-400 uppercase tracking-widest mt-0.5">
            COMPLETE
          </span>
        </div>

        {/* Floating Card 1: Upper-left (Target Role) */}
        <motion.div
          animate={{ y: [-3, 3, -3] }}
          transition={{ repeat: Infinity, duration: 4, ease: 'easeInOut' }}
          className="hidden lg:flex absolute -top-2 -left-20 bg-white/90 backdrop-blur-md border border-white/90 rounded-2xl px-3.5 py-2 shadow-[0_8px_24px_rgba(26,31,54,0.06)] items-center gap-2 -rotate-3 z-10 select-none"
        >
          <div className="w-7 h-7 rounded-lg bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#CC7D52]">
            <Target className="w-3.5 h-3.5" />
          </div>
          <div className="text-left">
            <span className="text-[9px] font-bold text-gray-400 uppercase tracking-wider block">
              TARGET
            </span>
            <span className="text-xs font-bold text-[#1A1F36] block">
              {targetRole}
            </span>
          </div>
        </motion.div>

        {/* Floating Card 2: Lower-left (Experience) */}
        <motion.div
          animate={{ y: [3, -3, 3] }}
          transition={{ repeat: Infinity, duration: 4.5, ease: 'easeInOut', delay: 0.4 }}
          className="hidden lg:flex absolute -bottom-4 -left-16 bg-white/90 backdrop-blur-md border border-white/90 rounded-2xl px-3.5 py-2 shadow-[0_8px_24px_rgba(26,31,54,0.06)] items-center gap-2 rotate-2 z-10 select-none"
        >
          <div className="w-7 h-7 rounded-lg bg-[#F2EFFE] border border-[#E6E1FF] flex items-center justify-center text-[#6B65E0]">
            <Brain className="w-3.5 h-3.5" />
          </div>
          <div className="text-left">
            <span className="text-[9px] font-bold text-gray-400 uppercase tracking-wider block">
              EXPERIENCE
            </span>
            <span className="text-xs font-bold text-[#1A1F36] block">
              {experience}
            </span>
          </div>
        </motion.div>

        {/* Floating Card 3: Upper-right (Learning Time) */}
        <motion.div
          animate={{ y: [-4, 3, -4] }}
          transition={{ repeat: Infinity, duration: 4.2, ease: 'easeInOut', delay: 0.8 }}
          className="hidden lg:flex absolute -top-4 -right-20 bg-white/90 backdrop-blur-md border border-white/90 rounded-2xl px-3.5 py-2 shadow-[0_8px_24px_rgba(26,31,54,0.06)] items-center gap-2 rotate-2 z-10 select-none"
        >
          <div className="w-7 h-7 rounded-lg bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#CC7D52]">
            <Clock className="w-3.5 h-3.5" />
          </div>
          <div className="text-left">
            <span className="text-[9px] font-bold text-gray-400 uppercase tracking-wider block">
              LEARNING TIME
            </span>
            <span className="text-xs font-bold text-[#1A1F36] block">
              {learningTime}
            </span>
          </div>
        </motion.div>
      </div>

      {/* Mobile/Tablet Stacked Insight Badges */}
      <div className="flex lg:hidden flex-wrap items-center justify-center gap-2 mt-6">
        <div className="inline-flex items-center gap-1.5 px-3 py-1 bg-white/80 rounded-xl border border-gray-100 text-xs shadow-2xs">
          <Target className="w-3.5 h-3.5 text-[#CC7D52]" />
          <span className="font-medium text-gray-500">Target:</span>
          <span className="font-bold text-[#1A1F36]">{targetRole}</span>
        </div>
        <div className="inline-flex items-center gap-1.5 px-3 py-1 bg-white/80 rounded-xl border border-gray-100 text-xs shadow-2xs">
          <Brain className="w-3.5 h-3.5 text-[#6B65E0]" />
          <span className="font-medium text-gray-500">Exp:</span>
          <span className="font-bold text-[#1A1F36]">{experience}</span>
        </div>
        <div className="inline-flex items-center gap-1.5 px-3 py-1 bg-white/80 rounded-xl border border-gray-100 text-xs shadow-2xs">
          <Clock className="w-3.5 h-3.5 text-[#CC7D52]" />
          <span className="font-medium text-gray-500">Time:</span>
          <span className="font-bold text-[#1A1F36]">{learningTime}</span>
        </div>
      </div>
    </div>
  );
};
