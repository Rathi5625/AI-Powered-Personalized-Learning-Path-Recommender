import React from 'react';
import { Link } from 'react-router-dom';
import { Clock, BarChart2 } from 'lucide-react';

interface NextBestStepCardProps {
  onContinueLearning?: () => void;
}

export const NextBestStepCard: React.FC<NextBestStepCardProps> = ({
  onContinueLearning,
}) => {
  return (
    <section
      aria-label="Next Recommended Step"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 border-l-4 border-l-[#8e4d2b] p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-4"
    >
      {/* Heading */}
      <h3 className="text-base sm:text-lg font-extrabold text-[#0f1b32] tracking-tight">
        Next Best Step
      </h3>

      {/* Description */}
      <p className="text-xs sm:text-sm text-[#53433c] leading-relaxed font-normal">
        Focus on your weak areas to improve your overall DSA score.
      </p>

      {/* Recommendation Card */}
      <div className="p-4 rounded-2xl bg-white/80 border border-gray-100/90 shadow-2xs space-y-3">
        <div className="flex items-start justify-between gap-2">
          <h4 className="text-sm font-extrabold text-[#0f1b32] leading-tight">
            Practice Binary Search Trees
          </h4>
          <span className="px-2.5 py-0.5 rounded-full bg-[#ffdbcb]/60 border border-[#d98b63]/30 text-[#8e4d2b] text-[11px] font-bold shrink-0">
            96% Match
          </span>
        </div>

        {/* Metadata */}
        <div className="flex items-center gap-4 text-xs text-gray-500 font-medium">
          <div className="flex items-center gap-1">
            <Clock className="w-3.5 h-3.5 text-gray-400" />
            <span>45m</span>
          </div>
          <div className="flex items-center gap-1">
            <BarChart2 className="w-3.5 h-3.5 text-gray-400" />
            <span>Intermediate</span>
          </div>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="space-y-2 pt-1">
        <button
          type="button"
          onClick={onContinueLearning}
          className="w-full py-3 px-4 rounded-2xl bg-[#FAF4F0] hover:bg-[#F2DACB]/70 text-[#0f1b32] font-bold text-xs sm:text-sm border border-[#F2DACB] transition-all cursor-pointer text-center shadow-2xs active:scale-[0.99]"
        >
          Continue Learning
        </button>

        <Link
          to="/learning-path"
          className="block w-full py-2 text-center text-xs font-bold text-[#0f1b32] hover:text-[#8e4d2b] transition-colors"
        >
          View Learning Path
        </Link>
      </div>
    </section>
  );
};
