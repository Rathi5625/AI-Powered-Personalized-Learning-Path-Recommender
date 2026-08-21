import React from 'react';
import { Sparkles, ArrowRight, HelpCircle } from 'lucide-react';

interface AIRecommendationCardProps {
  onContinueLearning?: () => void;
  onAskAIWhy?: () => void;
}

export const AIRecommendationCard: React.FC<AIRecommendationCardProps> = ({
  onContinueLearning,
  onAskAIWhy,
}) => {
  return (
    <div className="relative bg-white/80 backdrop-blur-xl border border-white/90 border-t-4 border-t-[#8e4d2b] rounded-2xl sm:rounded-3xl p-5 sm:p-6 shadow-[0_16px_40px_rgba(15,27,50,0.04)] text-left">
      {/* Header */}
      <div className="flex items-center gap-2 mb-3">
        <div className="w-7 h-7 rounded-lg bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#8e4d2b]">
          <Sparkles className="w-3.5 h-3.5" />
        </div>
        <h4 className="text-xs sm:text-sm font-bold text-[#0f1b32] tracking-tight">
          AI Recommendation
        </h4>
      </div>

      {/* Message */}
      <p className="text-xs sm:text-sm text-gray-700 leading-relaxed font-normal mb-5">
        &ldquo;You&apos;re making good progress in DSA. Complete Linked Lists before starting Trees.&rdquo;
      </p>

      {/* Action Buttons */}
      <div className="space-y-2">
        <button
          type="button"
          onClick={onContinueLearning}
          className="w-full py-2.5 px-4 rounded-xl bg-[#8e4d2b] hover:bg-[#783e20] text-white text-xs font-bold transition-all shadow-xs flex items-center justify-center gap-1.5 cursor-pointer"
        >
          <span>Continue Learning</span>
          <ArrowRight className="w-3.5 h-3.5" />
        </button>

        <button
          type="button"
          onClick={onAskAIWhy}
          className="w-full py-2.5 px-4 rounded-xl bg-white hover:bg-gray-50 border border-gray-200/80 text-gray-700 hover:text-[#0f1b32] text-xs font-bold transition-all shadow-2xs flex items-center justify-center gap-1.5 cursor-pointer"
        >
          <HelpCircle className="w-3.5 h-3.5 text-gray-400" />
          <span>Ask AI Why</span>
        </button>
      </div>
    </div>
  );
};
