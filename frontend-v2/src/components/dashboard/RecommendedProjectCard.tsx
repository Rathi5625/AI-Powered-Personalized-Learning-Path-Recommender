import React from 'react';
import { Rocket, Sparkles, Clock, ArrowRight } from 'lucide-react';

interface RecommendedProjectCardProps {
  onViewDetails?: () => void;
}

export const RecommendedProjectCard: React.FC<RecommendedProjectCardProps> = ({
  onViewDetails,
}) => {
  return (
    <div className="bg-white/80 backdrop-blur-xl border border-white/90 border-l-4 border-l-[#8e4d2b] rounded-2xl sm:rounded-[24px] p-5 sm:p-6 shadow-[0_16px_40px_rgba(15,27,50,0.04)] text-left flex flex-col justify-between">
      <div>
        {/* Header */}
        <div className="flex items-center gap-2 mb-2">
          <Rocket className="w-4 h-4 text-[#8e4d2b]" />
          <span className="text-[10px] sm:text-[11px] font-bold text-gray-400 uppercase tracking-wider">
            RECOMMENDED PROJECT
          </span>
        </div>

        {/* Title */}
        <h4 className="text-base sm:text-lg font-extrabold text-[#0f1b32] tracking-tight mb-3">
          Build a Spring Boot E-Commerce API
        </h4>

        {/* Badges */}
        <div className="flex items-center gap-2 mb-5">
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg bg-emerald-50 border border-emerald-200/60 text-xs font-bold text-emerald-700">
            <Sparkles className="w-3 h-3 text-emerald-600" />
            <span>94% Match</span>
          </span>

          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg bg-gray-100/80 border border-gray-200/60 text-xs font-semibold text-gray-600">
            <Clock className="w-3 h-3 text-gray-400" />
            <span>7 Days</span>
          </span>
        </div>
      </div>

      {/* Action Link */}
      <button
        type="button"
        onClick={onViewDetails}
        className="inline-flex items-center gap-1.5 text-xs sm:text-sm font-bold text-[#8e4d2b] hover:text-[#783e20] transition-colors cursor-pointer self-start group"
      >
        <span>View Details</span>
        <ArrowRight className="w-3.5 h-3.5 group-hover:translate-x-0.5 transition-transform" />
      </button>
    </div>
  );
};
