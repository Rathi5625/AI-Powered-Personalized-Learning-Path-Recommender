import React from 'react';
import { Sparkles } from 'lucide-react';
import { motion } from 'framer-motion';

export const ProjectRecommendationCard: React.FC = () => {
  return (
    <section
      aria-label="Why LearnAI Recommended This"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left flex flex-col justify-between space-y-5"
    >
      {/* Header */}
      <div className="space-y-2">
        <div className="flex items-center gap-2">
          <Sparkles className="w-4 h-4 text-[#8e4d2b]" />
          <h2 className="text-base sm:text-lg font-extrabold text-[#0f1b32] tracking-tight">
            Why LearnAI recommended this
          </h2>
        </div>

        {/* Text */}
        <p className="text-xs sm:text-sm text-[#53433c] leading-relaxed font-normal">
          This project is intricately designed around your current skill gaps and your broader{' '}
          <strong className="font-bold text-[#0f1b32]">Software Engineer</strong> career trajectory.
        </p>
      </div>

      {/* Spring Boot Mastery Progress Comparison */}
      <div className="p-3.5 rounded-2xl bg-white/80 border border-gray-100/90 shadow-2xs space-y-2">
        <div className="flex items-center justify-between text-xs">
          <span className="font-bold text-[#0f1b32]">Spring Boot Mastery</span>
          <span className="font-bold text-[#8e4d2b]">32% → 75%</span>
        </div>

        {/* Dual Progress Bar */}
        <div className="w-full h-2 bg-gray-100 rounded-full overflow-hidden relative">
          <div className="absolute left-0 top-0 h-full w-[32%] bg-[#615a7a] rounded-full z-10" />
          <motion.div
            initial={{ width: '32%' }}
            animate={{ width: '75%' }}
            transition={{ duration: 1.2, ease: 'easeOut' }}
            className="h-full bg-[#d98b63] rounded-full"
          />
        </div>
      </div>

      {/* 2 Metric Cards */}
      <div className="grid grid-cols-2 gap-3">
        {/* Metric 1 */}
        <div className="p-3 rounded-2xl bg-[#f9f9ff] border border-gray-100 text-left">
          <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider block">
            CAREER RELEVANCE
          </span>
          <span className="text-sm font-extrabold text-[#0f1b32] mt-0.5 block">
            High
          </span>
        </div>

        {/* Metric 2 */}
        <div className="p-3 rounded-2xl bg-[#f9f9ff] border border-gray-100 text-left">
          <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider block">
            ROADMAP POSITION
          </span>
          <span className="text-sm font-extrabold text-[#0f1b32] mt-0.5 block">
            Phase 4
          </span>
        </div>
      </div>
    </section>
  );
};
