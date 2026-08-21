import React from 'react';
import { Bot } from 'lucide-react';

interface AISkillInsightsCardProps {
  onOptimizePath?: () => void;
}

export const AISkillInsightsCard: React.FC<AISkillInsightsCardProps> = ({
  onOptimizePath,
}) => {
  return (
    <section
      aria-label="AI Skill Insights"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left flex flex-col justify-between h-full space-y-4"
    >
      <div className="space-y-3">
        {/* Header */}
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-xl bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#8e4d2b]">
            <Bot className="w-4 h-4 text-[#8e4d2b]" />
          </div>
          <h2 className="text-lg sm:text-xl font-extrabold text-[#0f1b32] tracking-tight">
            AI Insights
          </h2>
        </div>

        {/* Text Body */}
        <p className="text-xs sm:text-sm text-[#53433c] leading-relaxed font-normal">
          Your Data Structures knowledge is solid, but System Design needs attention to reach
          your target role.
        </p>

        {/* Strongest & Focus */}
        <div className="space-y-1.5 pt-1 text-xs sm:text-sm">
          <div className="flex items-center justify-between">
            <span className="text-[#53433c]">Strongest: <strong className="text-[#0f1b32] font-semibold">Java</strong></span>
            <span className="font-bold text-[#8e4d2b]">82%</span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-[#53433c]">Focus: <strong className="text-[#0f1b32] font-semibold">System Design</strong></span>
            <span className="font-semibold text-gray-500">41%</span>
          </div>
        </div>
      </div>

      {/* CTA Button */}
      <div className="pt-2">
        <button
          type="button"
          onClick={onOptimizePath}
          className="w-full py-2.5 px-4 rounded-xl bg-[#FAF4F0] hover:bg-[#F2DACB]/60 text-[#8e4d2b] text-xs sm:text-sm font-bold border border-[#F2DACB] transition-colors cursor-pointer text-center shadow-2xs active:scale-[0.99]"
        >
          Optimize My Path
        </button>
      </div>
    </section>
  );
};
