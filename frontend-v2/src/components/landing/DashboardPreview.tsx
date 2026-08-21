import React from 'react';
import { motion } from 'framer-motion';
import { Sparkles, Clock, Target, CheckCircle2 } from 'lucide-react';
import { Badge } from '../ui/Badge';

export const DashboardPreview: React.FC = () => {
  return (
    <div className="relative w-full max-w-xl mx-auto lg:max-w-none">
      {/* Floating Badge 1: Top-Left (Career Goal) */}
      <motion.div
        initial={{ opacity: 0, y: 15, x: -10 }}
        animate={{ opacity: 1, y: 0, x: 0 }}
        transition={{ duration: 0.6, delay: 0.2 }}
        whileHover={{ scale: 1.03 }}
        className="absolute -top-5 -left-3 sm:-left-8 z-20 bg-white/95 backdrop-blur-2xl border border-white/90 rounded-2xl p-3 sm:p-3.5 px-4 shadow-[0_12px_32px_rgba(26,31,54,0.08)] flex items-center gap-3"
      >
        <div className="w-8 h-8 rounded-xl bg-[#F2EFFF] text-[#8E86FF] flex items-center justify-center shrink-0">
          <Target className="w-4 h-4" />
        </div>
        <div>
          <div className="text-[10px] tracking-wider uppercase font-semibold text-gray-400">
            Career Goal
          </div>
          <div className="text-xs sm:text-sm font-bold text-[#1A1F36]">
            Software Engineer
          </div>
        </div>
      </motion.div>

      {/* Main Glassmorphism Dashboard Container */}
      <motion.div
        initial={{ opacity: 0, scale: 0.96, y: 20 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        transition={{ duration: 0.7, ease: [0.16, 1, 0.3, 1] }}
        className="relative bg-white/80 backdrop-blur-2xl rounded-[32px] sm:rounded-[36px] p-6 sm:p-8 border border-white/90 shadow-[0_24px_64px_rgba(26,31,54,0.07)] z-10 overflow-visible"
      >
        {/* Learner Header Row */}
        <div className="flex items-center justify-between pb-5 border-b border-gray-100/80">
          <div className="flex items-center gap-3.5">
            {/* Avatar with subtle gradient border */}
            <div className="relative w-11 h-11 rounded-full p-[2px] bg-gradient-to-tr from-[#FFB091] via-[#8E86FF] to-[#A06A42]">
              <div className="w-full h-full rounded-full bg-white flex items-center justify-center text-xs font-bold text-[#1A1F36] overflow-hidden">
                <span className="bg-gradient-to-br from-slate-100 to-slate-200 w-full h-full flex items-center justify-center">
                  AM
                </span>
              </div>
            </div>
            <div>
              <div className="font-bold text-[#1A1F36] text-sm sm:text-base leading-tight">
                Alex Mercer
              </div>
              <div className="text-xs text-gray-400 font-medium">
                Aspiring Software Engineer
              </div>
            </div>
          </div>

          <Badge variant="pro">Pro Plan</Badge>
        </div>

        {/* Inner Cards Grid: Progress & Focus */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mt-5">
          {/* Card 1: Learning Progress */}
          <div className="bg-white/95 rounded-2xl p-5 border border-gray-100 shadow-xs flex flex-col justify-between hover:shadow-md transition-shadow">
            <div>
              <span className="text-xs font-medium text-gray-400">
                Learning Progress
              </span>
              <div className="text-3xl sm:text-4xl font-extrabold text-[#1A1F36] mt-1 mb-3 tracking-tight">
                42%
              </div>

              {/* Two-tone progress bar */}
              <div className="h-2 w-full bg-[#EDEAFB] rounded-full overflow-hidden flex">
                <div
                  className="h-full bg-[#A06A42] rounded-full transition-all duration-1000"
                  style={{ width: '42%' }}
                />
              </div>
            </div>

            <div className="flex items-center gap-1.5 text-xs text-gray-400 font-medium mt-5">
              <Clock className="w-3.5 h-3.5 text-gray-400" />
              <span>Next milestone in 3 days</span>
            </div>
          </div>

          {/* Card 2: Current Focus */}
          <div className="bg-white/95 rounded-2xl p-5 border border-gray-100 shadow-xs flex flex-col justify-between hover:shadow-md transition-shadow">
            <div>
              <span className="text-xs font-medium text-gray-400">
                Current Focus
              </span>
              <div className="text-sm font-bold text-[#1A1F36] mt-1 mb-2.5">
                Advanced Data Structures
              </div>

              {/* Tags */}
              <div className="flex flex-wrap gap-1.5">
                <span className="bg-slate-100 text-slate-600 text-[11px] font-medium px-2.5 py-0.5 rounded-md">
                  Trees
                </span>
                <span className="bg-slate-100 text-slate-600 text-[11px] font-medium px-2.5 py-0.5 rounded-md">
                  Graphs
                </span>
              </div>
            </div>

            <button
              type="button"
              className="text-xs font-semibold text-[#1A1F36] hover:text-[#A06A42] mt-5 text-center cursor-pointer transition-colors"
            >
              Continue Lesson
            </button>
          </div>
        </div>

        {/* AI Recommendation Card */}
        <div className="mt-4 bg-white/95 rounded-2xl p-4 sm:p-4.5 border border-purple-100/70 shadow-xs flex items-start gap-3.5 hover:shadow-md transition-shadow">
          <div className="w-7 h-7 rounded-lg bg-[#F5F3FF] text-[#8E86FF] flex items-center justify-center shrink-0 mt-0.5">
            <Sparkles className="w-4 h-4" />
          </div>
          <div>
            <div className="text-xs font-bold text-[#1A1F36] flex items-center gap-1.5">
              <span>✨ AI Recommendation</span>
            </div>
            <p className="text-xs text-gray-600 leading-relaxed mt-1 font-normal">
              Based on your recent quiz, reviewing Hash Maps will improve your algorithm efficiency score.
            </p>
          </div>
        </div>
      </motion.div>

      {/* Floating Badge 2: Bottom-Right (Skill Gap Found) */}
      <motion.div
        initial={{ opacity: 0, y: 15, x: 10 }}
        animate={{ opacity: 1, y: 0, x: 0 }}
        transition={{ duration: 0.6, delay: 0.35 }}
        whileHover={{ scale: 1.03 }}
        className="absolute -bottom-4 -right-2 sm:-right-6 z-20 bg-white/95 backdrop-blur-2xl border border-white/90 rounded-2xl p-3 sm:p-3.5 px-4 shadow-[0_12px_32px_rgba(26,31,54,0.08)] flex items-center gap-3"
      >
        <div className="w-8 h-8 rounded-xl bg-[#F2EFFF] text-[#8E86FF] flex items-center justify-center shrink-0">
          <CheckCircle2 className="w-4 h-4" />
        </div>
        <div>
          <div className="text-[10px] tracking-wider uppercase font-semibold text-gray-400">
            Skill Gap Found
          </div>
          <div className="text-xs sm:text-sm font-bold text-[#1A1F36]">
            React Hooks (Resolved)
          </div>
        </div>
      </motion.div>
    </div>
  );
};
