import React from 'react';
import { HelpCircle, Clock, BarChart2, Sparkles } from 'lucide-react';

interface RecommendedAssessmentCardProps {
  onStartAssessment?: () => void;
}

export const RecommendedAssessmentCard: React.FC<RecommendedAssessmentCardProps> = ({
  onStartAssessment,
}) => {
  return (
    <div className="space-y-3 text-left">
      <h2 className="text-xl sm:text-2xl font-extrabold text-[#0f1b32] tracking-tight">
        Recommended Next Step
      </h2>

      <section
        aria-label="Recommended Assessment"
        className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] flex flex-col md:flex-row md:items-center justify-between gap-6"
      >
        {/* Left Info */}
        <div className="space-y-2.5">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#e1d8fe]/60 border border-[#c7b8fe] text-[#615a7a] text-[11px] font-bold tracking-wide uppercase shadow-2xs">
            <Sparkles className="w-3.5 h-3.5 text-[#615a7a]" />
            <span>96% AI RELEVANCE</span>
          </div>

          <h3 className="text-xl sm:text-2xl font-extrabold text-[#0f1b32] tracking-tight">
            Data Structures &amp; Algorithms
          </h3>

          <div className="flex items-center gap-4 sm:gap-6 text-xs text-[#53433c] flex-wrap">
            <div className="flex items-center gap-1.5">
              <HelpCircle className="w-3.5 h-3.5 text-gray-400" />
              <span>20 questions</span>
            </div>
            <div className="flex items-center gap-1.5">
              <Clock className="w-3.5 h-3.5 text-gray-400" />
              <span>20 mins</span>
            </div>
            <div className="flex items-center gap-1.5">
              <BarChart2 className="w-3.5 h-3.5 text-gray-400" />
              <span>Intermediate</span>
            </div>
          </div>
        </div>

        {/* Right CTA Button */}
        <div className="shrink-0">
          <button
            type="button"
            onClick={onStartAssessment}
            className="w-full md:w-auto px-6 py-3 rounded-2xl bg-[#FAF4F0] hover:bg-[#F2DACB]/70 text-[#8e4d2b] font-bold text-xs sm:text-sm border border-[#F2DACB] shadow-2xs hover:shadow-sm transition-all cursor-pointer text-center active:scale-[0.98]"
          >
            Start Assessment
          </button>
        </div>
      </section>
    </div>
  );
};
