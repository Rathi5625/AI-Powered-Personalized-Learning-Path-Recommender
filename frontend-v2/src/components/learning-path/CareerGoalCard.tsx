import React from 'react';
import { Sparkles, ArrowUpRight } from 'lucide-react';

interface CareerGoalCardProps {
  targetRole?: string;
  progress?: number;
  estJourney?: string;
  weeklyHours?: string;
  onViewAnalysis?: () => void;
}

export const CareerGoalCard: React.FC<CareerGoalCardProps> = ({
  targetRole = 'Software Engineer',
  progress = 42,
  estJourney = '6 mo',
  weeklyHours = '10 hrs',
  onViewAnalysis,
}) => {
  return (
    <div className="bg-white/80 backdrop-blur-xl border border-white/90 rounded-2xl sm:rounded-3xl p-5 sm:p-7 shadow-[0_16px_40px_rgba(15,27,50,0.04)] text-left">
      {/* Top Target Section */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <span className="text-[10px] sm:text-[11px] font-bold text-gray-400 uppercase tracking-widest block mb-1">
            YOUR TARGET
          </span>
          <h2 className="text-xl sm:text-2xl font-extrabold text-[#0f1b32] tracking-tight">
            {targetRole}
          </h2>
        </div>

        {/* 3 Compact Stats Cards */}
        <div className="grid grid-cols-3 gap-2 sm:gap-3">
          {/* Stat 1 */}
          <div className="bg-[#FAF4F0] border border-[#F2DACB]/80 rounded-xl sm:rounded-2xl px-3 sm:px-4 py-2.5 text-center">
            <span className="text-sm sm:text-base font-extrabold text-[#8e4d2b] block leading-tight">
              {progress}%
            </span>
            <span className="text-[10px] sm:text-[11px] font-semibold text-gray-500 block">
              Progress
            </span>
          </div>

          {/* Stat 2 */}
          <div className="bg-[#F2EFFE] border border-[#E6E1FF] rounded-xl sm:rounded-2xl px-3 sm:px-4 py-2.5 text-center">
            <span className="text-sm sm:text-base font-extrabold text-[#615a7a] block leading-tight">
              {estJourney}
            </span>
            <span className="text-[10px] sm:text-[11px] font-semibold text-gray-500 block">
              Est. Journey
            </span>
          </div>

          {/* Stat 3 */}
          <div className="bg-white/90 border border-gray-200/80 rounded-xl sm:rounded-2xl px-3 sm:px-4 py-2.5 text-center">
            <span className="text-sm sm:text-base font-extrabold text-[#0f1b32] block leading-tight">
              {weeklyHours}
            </span>
            <span className="text-[10px] sm:text-[11px] font-semibold text-gray-500 block">
              Weekly
            </span>
          </div>
        </div>
      </div>

      {/* Why This Path Banner */}
      <div className="bg-[#F9F9FF] border border-[#E6DEFF] rounded-2xl p-4 sm:p-5 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3.5">
        <div className="flex items-start gap-3">
          <div className="w-8 h-8 rounded-xl bg-white border border-[#DDD7FF] flex items-center justify-center shrink-0 text-[#8e4d2b] shadow-2xs">
            <Sparkles className="w-4 h-4" />
          </div>
          <div>
            <span className="text-xs font-bold text-[#0f1b32] block mb-0.5">
              Why this path?
            </span>
            <p className="text-xs text-gray-600 leading-relaxed font-normal">
              LearnAI created this roadmap based on your current skills, career goal, experience level and available learning time.
            </p>
          </div>
        </div>

        {/* View AI Analysis Button */}
        <button
          type="button"
          onClick={onViewAnalysis}
          className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-white hover:bg-white/90 border border-gray-200/90 text-xs font-bold text-[#0f1b32] hover:text-[#8e4d2b] transition-colors shadow-2xs cursor-pointer shrink-0"
        >
          <span>View AI Analysis</span>
          <ArrowUpRight className="w-3.5 h-3.5 text-gray-400" />
        </button>
      </div>
    </div>
  );
};
