import React from 'react';
import { Sparkles } from 'lucide-react';

interface CareerProfileCardProps {
  currentGoal?: string;
  targetRole?: string;
  experience?: string;
}

export const CareerProfileCard: React.FC<CareerProfileCardProps> = ({
  currentGoal = 'Software Engineer',
  targetRole = 'Full Stack Developer',
  experience = 'Intermediate',
}) => {
  return (
    <section
      aria-label="Career Goals and Path"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-6"
    >
      {/* Header */}
      <h3 className="text-base sm:text-lg font-extrabold text-[#0f1b32] tracking-tight">
        What you&apos;re working toward
      </h3>

      {/* 3 Metrics Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        {/* Current Goal */}
        <div className="p-4 rounded-2xl bg-white/90 border border-gray-100/90 shadow-2xs space-y-1">
          <span className="text-[11px] font-bold text-gray-400 uppercase tracking-wider block">
            Current Goal
          </span>
          <span className="text-sm font-extrabold text-[#0f1b32] block">
            {currentGoal}
          </span>
        </div>

        {/* Target Role */}
        <div className="p-4 rounded-2xl bg-white/90 border border-gray-100/90 shadow-2xs space-y-1">
          <span className="text-[11px] font-bold text-gray-400 uppercase tracking-wider block">
            Target Role
          </span>
          <span className="text-sm font-extrabold text-[#0f1b32] block">
            {targetRole}
          </span>
        </div>

        {/* Experience */}
        <div className="p-4 rounded-2xl bg-white/90 border border-gray-100/90 shadow-2xs space-y-1">
          <span className="text-[11px] font-bold text-gray-400 uppercase tracking-wider block">
            Experience
          </span>
          <span className="text-sm font-extrabold text-[#0f1b32] block">
            {experience}
          </span>
        </div>
      </div>

      {/* AI Insight Highlight Panel */}
      <div className="p-5 rounded-2xl bg-[#FAF4F0]/90 border border-[#F2DACB]/80 shadow-2xs space-y-2">
        <div className="flex items-center gap-1.5 text-xs font-bold text-[#8e4d2b]">
          <Sparkles className="w-3.5 h-3.5 text-[#8e4d2b]" />
          <span>How LearnAI sees your profile</span>
        </div>

        <p className="text-xs text-[#53433c] leading-relaxed font-normal">
          Based on your current skills and target role, you have a solid foundation in backend concepts.
          To transition smoothly into Full Stack Development, we&apos;ll focus on bridging the gap in
          frontend frameworks (React) while deepening your understanding of scalable API design.
        </p>
      </div>
    </section>
  );
};
