import React from 'react';
import { ShieldCheck, Sparkles } from 'lucide-react';

export const MentorPersonalizationCard: React.FC = () => {
  return (
    <div className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-4 sm:p-5 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-2 select-none">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-1.5 text-xs font-extrabold text-[#0f1b32]">
          <Sparkles className="w-3.5 h-3.5 text-[#8e4d2b]" />
          <span>AI Mentor is personalized to you</span>
        </div>
        <span className="px-2 py-0.5 rounded-full bg-emerald-50 border border-emerald-200 text-emerald-700 text-[10px] font-bold inline-flex items-center gap-1">
          <ShieldCheck className="w-3 h-3" />
          <span>Active</span>
        </span>
      </div>

      <p className="text-[11px] text-gray-500 font-normal leading-relaxed">
        Recommendations use your goals, assessments, skills, learning progress, and preferences.
      </p>
    </div>
  );
};
