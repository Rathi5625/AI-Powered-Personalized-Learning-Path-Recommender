import React from 'react';
import { Sparkles, ArrowRight } from 'lucide-react';
import { Link } from 'react-router-dom';

interface MentorRecommendationCardProps {
  onStartTopic?: () => void;
}

export const MentorRecommendationCard: React.FC<MentorRecommendationCardProps> = ({
  onStartTopic,
}) => {
  return (
    <div className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-5 sm:p-6 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-4 select-none">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="w-7 h-7 rounded-xl bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#8e4d2b]">
            <Sparkles className="w-3.5 h-3.5 text-[#8e4d2b]" />
          </div>
          <span className="text-xs font-bold text-[#0f1b32] uppercase tracking-wider">
            Mentor Recommendation
          </span>
        </div>
      </div>

      <div className="p-3.5 rounded-2xl bg-[#FAF4F0]/60 border border-[#F2DACB]/60 space-y-1.5">
        <span className="text-xs font-extrabold text-[#8e4d2b] block">
          Binary Search
        </span>
        <p className="text-[11px] text-[#53433c] font-normal leading-relaxed">
          You&apos;re ready for this based on your recent assessment and Arrays progress.
        </p>
      </div>

      <div className="flex items-center gap-2 pt-1">
        <button
          type="button"
          onClick={onStartTopic}
          className="flex-1 py-2 px-3 rounded-xl bg-[#8e4d2b] hover:bg-[#783e20] text-white text-xs font-bold transition-all shadow-2xs cursor-pointer flex items-center justify-center gap-1.5"
        >
          <span>Start Topic</span>
          <ArrowRight className="w-3 h-3" />
        </button>

        <Link
          to="/learning-path"
          className="py-2 px-3 rounded-xl bg-white hover:bg-gray-50 border border-gray-200 text-xs font-bold text-[#0f1b32] transition-colors text-center"
        >
          View Details
        </Link>
      </div>
    </div>
  );
};
