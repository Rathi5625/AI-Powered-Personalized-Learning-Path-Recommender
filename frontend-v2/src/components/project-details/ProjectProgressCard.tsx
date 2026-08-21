import React from 'react';
import { motion } from 'framer-motion';

interface ProjectProgressCardProps {
  progress?: number;
  completedStepTitle?: string;
  currentStepTitle?: string;
  onContinueProject?: () => void;
}

export const ProjectProgressCard: React.FC<ProjectProgressCardProps> = ({
  progress = 0,
  completedStepTitle = '',
  currentStepTitle = '',
  onContinueProject,
}) => {
  // Arc calculation for semi-circle
  const radius = 50;
  const circumference = Math.PI * radius; // half circle
  const strokeDashoffset = circumference - (progress / 100) * circumference;

  return (
    <section
      aria-label="Project Progress"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-5"
    >
      <h3 className="text-base font-extrabold text-[#0f1b32] tracking-tight">
        Project Progress
      </h3>

      {/* Semi-Circular Progress Gauge */}
      <div className="flex flex-col items-center justify-center pt-2">
        <div className="relative w-36 h-20 flex items-center justify-center">
          <svg className="w-36 h-36 -rotate-90 transform" viewBox="0 0 120 120">
            {/* Background Track Arc */}
            <circle
              cx="60"
              cy="60"
              r={radius}
              fill="none"
              stroke="#FAF4F0"
              strokeWidth="10"
              strokeDasharray={circumference}
              strokeDashoffset="0"
              strokeLinecap="round"
            />
            {/* Filled Progress Arc */}
            <motion.circle
              cx="60"
              cy="60"
              r={radius}
              fill="none"
              stroke="#8e4d2b"
              strokeWidth="10"
              strokeDasharray={circumference}
              initial={{ strokeDashoffset: circumference }}
              animate={{ strokeDashoffset }}
              transition={{ duration: 1, ease: 'easeOut' }}
              strokeLinecap="round"
            />
          </svg>

          {/* Centered Score */}
          <div className="absolute top-10 flex flex-col items-center justify-center">
            <span className="text-xl font-extrabold text-[#0f1b32] leading-none">
              {progress}%
            </span>
            <span className="text-[10px] text-gray-400 font-bold tracking-wider uppercase mt-0.5">
              COMPLETED
            </span>
          </div>
        </div>
      </div>

      {/* Step Status Badges */}
      <div className="space-y-3 pt-2 text-xs">
        {/* Completed Indicator */}
        <div className="flex items-start gap-2.5">
          <span className="w-2 h-2 rounded-full bg-gray-400 mt-1 shrink-0" />
          <div>
            <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider block">
              COMPLETED
            </span>
            <span className="font-bold text-[#0f1b32]">{completedStepTitle}</span>
          </div>
        </div>

        {/* Current Indicator */}
        <div className="flex items-start gap-2.5">
          <span className="w-2 h-2 rounded-full bg-[#8e4d2b] mt-1 shrink-0 animate-pulse" />
          <div>
            <span className="text-[10px] text-[#8e4d2b] font-bold uppercase tracking-wider block">
              CURRENT
            </span>
            <span className="font-bold text-[#0f1b32]">{currentStepTitle}</span>
          </div>
        </div>
      </div>

      {/* Primary Action Button */}
      <div className="pt-2">
        <button
          type="button"
          onClick={onContinueProject}
          className="w-full py-3 rounded-2xl bg-[#8e4d2b] hover:bg-[#783e20] text-white font-bold text-xs sm:text-sm shadow-sm hover:shadow-md transition-all cursor-pointer text-center active:scale-[0.98]"
        >
          Continue Project
        </button>
      </div>
    </section>
  );
};
