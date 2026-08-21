import React from 'react';
import { Sparkles, ArrowRight, TrendingUp } from 'lucide-react';

interface SkillProfileUpdatedCardProps {
  beforeScore?: number;
  afterScore?: number;
  increase?: number;
}

export const SkillProfileUpdatedCard: React.FC<SkillProfileUpdatedCardProps> = ({
  beforeScore = 61,
  afterScore = 68,
  increase = 7,
}) => {
  return (
    <section
      aria-label="Skill Profile Update"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-4"
    >
      {/* Header */}
      <div className="flex items-center gap-2">
        <Sparkles className="w-4 h-4 text-[#8e4d2b]" />
        <h3 className="text-base sm:text-lg font-extrabold text-[#0f1b32] tracking-tight">
          Skill Profile Updated
        </h3>
      </div>

      {/* Description */}
      <p className="text-xs sm:text-sm text-[#53433c] leading-relaxed font-normal">
        Based on your performance, we&apos;ve updated your Data Structures &amp; Algorithms proficiency.
      </p>

      {/* Before / After Comparison Box */}
      <div className="p-4 rounded-2xl bg-white/80 border border-gray-100/90 shadow-2xs space-y-3">
        <div className="flex items-center justify-around text-center">
          <div>
            <span className="text-[11px] text-gray-400 font-medium block">Before</span>
            <span className="text-xl sm:text-2xl font-extrabold text-[#0f1b32]">{beforeScore}%</span>
          </div>

          <ArrowRight className="w-5 h-5 text-gray-400 shrink-0" />

          <div>
            <span className="text-[11px] text-gray-400 font-medium block">After</span>
            <span className="text-xl sm:text-2xl font-extrabold text-[#8e4d2b]">{afterScore}%</span>
          </div>
        </div>

        {/* Increase Badge */}
        <div className="flex items-center justify-center gap-1.5 text-xs font-bold text-emerald-600 pt-1 border-t border-gray-100/80">
          <TrendingUp className="w-3.5 h-3.5" />
          <span>+{increase}% Increase</span>
        </div>
      </div>
    </section>
  );
};
