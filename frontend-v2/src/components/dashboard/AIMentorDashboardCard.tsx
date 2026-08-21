import React from 'react';
import { Bot, ArrowRight } from 'lucide-react';

interface AIMentorDashboardCardProps {
  onStartTopic?: () => void;
}

export const AIMentorDashboardCard: React.FC<AIMentorDashboardCardProps> = ({
  onStartTopic,
}) => {
  return (
    <div className="bg-[#F2EFFE]/90 backdrop-blur-xl border border-[#E6E1FF] rounded-2xl sm:rounded-[24px] p-5 sm:p-6 shadow-[0_16px_40px_rgba(15,27,50,0.04)] text-left flex flex-col justify-between h-full">
      <div>
        {/* Header */}
        <div className="flex items-center gap-2 mb-3">
          <div className="w-7 h-7 rounded-lg bg-white border border-[#DDD7FF] flex items-center justify-center text-[#615a7a] shadow-2xs">
            <Bot className="w-4 h-4" />
          </div>
          <h3 className="text-xs sm:text-sm font-bold text-[#615a7a] uppercase tracking-wider">
            AI Mentor Suggests
          </h3>
        </div>

        {/* Main Heading */}
        <h4 className="text-base sm:text-lg font-extrabold text-[#0f1b32] tracking-tight mb-2">
          Focus on Binary Search today
        </h4>

        {/* Description */}
        <p className="text-xs sm:text-[13px] text-gray-700 leading-relaxed font-normal mb-4">
          Based on your recent assessment, I recommend strengthening{' '}
          <strong className="font-semibold text-[#0f1b32]">Binary Search</strong>{' '}
          before moving into advanced tree and graph problems. It&apos;s a foundational pattern you&apos;ll need.
        </p>

        {/* Optimal Path Box */}
        <div className="bg-white/80 border border-[#DDD7FF] rounded-xl sm:rounded-2xl p-3 mb-5">
          <span className="text-[9px] sm:text-[10px] font-bold text-gray-400 uppercase tracking-widest block mb-1.5">
            OPTIMAL PATH
          </span>
          <div className="flex items-center gap-2 text-xs flex-wrap">
            <span className="px-2.5 py-1 rounded-lg bg-[#FAF4F0] border border-[#F2DACB] text-[#8e4d2b] font-bold">
              Binary Search
            </span>
            <ArrowRight className="w-3.5 h-3.5 text-gray-400 shrink-0" />
            <span className="text-gray-600 font-medium">Trees</span>
            <ArrowRight className="w-3.5 h-3.5 text-gray-400 shrink-0" />
            <span className="text-gray-600 font-medium">Graphs</span>
          </div>
        </div>
      </div>

      {/* Action Button */}
      <button
        type="button"
        onClick={onStartTopic}
        className="w-full py-2.5 px-4 rounded-xl bg-[#615a7a] hover:bg-[#524b69] active:bg-[#433d57] text-white text-xs sm:text-sm font-bold transition-all shadow-xs flex items-center justify-center gap-1.5 cursor-pointer"
      >
        <span>Start Topic</span>
        <ArrowRight className="w-3.5 h-3.5" />
      </button>
    </div>
  );
};
