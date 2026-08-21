import React from 'react';
import { Brain } from 'lucide-react';

export const AIRecommendationCard: React.FC = () => {
  return (
    <section
      aria-label="AI Recommendation Analysis"
      className="relative overflow-hidden rounded-3xl bg-white/70 backdrop-blur-2xl border border-white/80 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left"
    >
      <div className="flex items-start gap-4">
        {/* Lavender AI Brain Icon */}
        <div className="w-10 h-10 rounded-2xl bg-[#e1d8fe]/80 border border-[#c7b8fe]/60 flex items-center justify-center text-[#615a7a] shrink-0 shadow-2xs">
          <Brain className="w-5 h-5" />
        </div>

        {/* Content */}
        <div className="space-y-2.5 flex-1">
          <h2 className="text-base sm:text-lg font-bold text-[#0f1b32] tracking-tight">
            Why LearnAI recommends this
          </h2>

          <p className="text-xs sm:text-sm text-[#53433c] leading-relaxed font-normal">
            This specialization perfectly fits your{' '}
            <strong className="font-bold text-[#0f1b32]">Software Engineer</strong> goal. It
            directly addresses your identified skill gap in algorithmic problem-solving and matches
            your current Intermediate proficiency level.
          </p>

          {/* Tags */}
          <div className="flex items-center gap-2 pt-1 flex-wrap">
            <span className="px-3 py-1 rounded-full bg-[#e1d8fe]/60 border border-[#c7b8fe]/50 text-[#615a7a] text-[11px] font-semibold">
              Goal Alignment
            </span>
            <span className="px-3 py-1 rounded-full bg-[#e1d8fe]/60 border border-[#c7b8fe]/50 text-[#615a7a] text-[11px] font-semibold">
              Skill Gap: Problem Solving
            </span>
          </div>
        </div>
      </div>
    </section>
  );
};
