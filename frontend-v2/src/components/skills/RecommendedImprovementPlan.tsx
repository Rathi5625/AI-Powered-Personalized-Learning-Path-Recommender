import React from 'react';
import { ArrowRight, ArrowDown } from 'lucide-react';

interface RecommendedImprovementPlanProps {
  onSelectTopic?: (topic: string, stage: string) => void;
}

export const RecommendedImprovementPlan: React.FC<RecommendedImprovementPlanProps> = ({
  onSelectTopic,
}) => {
  return (
    <section
      aria-label="Recommended Improvement Plan"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-8 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-6"
    >
      <h2 className="text-xl sm:text-2xl font-extrabold text-[#0f1b32] tracking-tight">
        Recommended Improvement Plan
      </h2>

      {/* 3-Stage Cards Roadmap */}
      <div className="flex flex-col md:flex-row items-center justify-between gap-4 md:gap-6">
        {/* Stage 1: NOW - Binary Search */}
        <button
          type="button"
          onClick={() => onSelectTopic?.('Binary Search', 'NOW')}
          className="w-full flex-1 rounded-2xl bg-[#FAF4F0]/90 border border-[#F2DACB] p-5 sm:p-6 text-center shadow-2xs hover:shadow-sm hover:scale-[1.01] transition-all cursor-pointer select-none group"
        >
          <span className="text-[11px] font-bold text-[#8e4d2b] tracking-wider uppercase block mb-1.5">
            NOW
          </span>
          <h3 className="text-base sm:text-lg font-extrabold text-[#0f1b32] group-hover:text-[#8e4d2b] transition-colors">
            Binary Search
          </h3>
        </button>

        {/* Arrow Connector 1 */}
        <div className="text-gray-400 shrink-0">
          <ArrowRight className="w-5 h-5 hidden md:block" />
          <ArrowDown className="w-5 h-5 md:hidden" />
        </div>

        {/* Stage 2: NEXT - Trees */}
        <button
          type="button"
          onClick={() => onSelectTopic?.('Trees', 'NEXT')}
          className="w-full flex-1 rounded-2xl bg-white/80 border border-gray-100 p-5 sm:p-6 text-center shadow-2xs hover:shadow-sm hover:scale-[1.01] transition-all cursor-pointer select-none group"
        >
          <span className="text-[11px] font-bold text-gray-500 tracking-wider uppercase block mb-1.5">
            NEXT
          </span>
          <h3 className="text-base sm:text-lg font-bold text-[#0f1b32] group-hover:text-[#8e4d2b] transition-colors">
            Trees
          </h3>
        </button>

        {/* Arrow Connector 2 */}
        <div className="text-gray-400 shrink-0">
          <ArrowRight className="w-5 h-5 hidden md:block" />
          <ArrowDown className="w-5 h-5 md:hidden" />
        </div>

        {/* Stage 3: AFTER - Graphs */}
        <button
          type="button"
          onClick={() => onSelectTopic?.('Graphs', 'AFTER')}
          className="w-full flex-1 rounded-2xl bg-gray-50/70 border border-gray-100 p-5 sm:p-6 text-center shadow-2xs hover:shadow-sm hover:scale-[1.01] transition-all cursor-pointer select-none group"
        >
          <span className="text-[11px] font-bold text-gray-400 tracking-wider uppercase block mb-1.5">
            AFTER
          </span>
          <h3 className="text-base sm:text-lg font-medium text-gray-500 group-hover:text-[#0f1b32] transition-colors">
            Graphs
          </h3>
        </button>
      </div>
    </section>
  );
};
