import React from 'react';
import { Sparkles } from 'lucide-react';

interface AIProgressInsightProps {
  onAction?: () => void;
}

export const AIProgressInsight: React.FC<AIProgressInsightProps> = ({ onAction }) => {
  return (
    <section
      aria-label="AI Progress Insight"
      onClick={onAction}
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 border-l-4 border-l-[#8e4d2b] p-5 sm:p-6 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-2.5 cursor-pointer hover:shadow-md transition-all"
    >
      {/* Header */}
      <div className="flex items-center gap-2">
        <Sparkles className="w-4 h-4 text-[#8e4d2b]" />
        <h3 className="text-sm sm:text-base font-extrabold text-[#0f1b32] tracking-tight">
          AI Progress Insight
        </h3>
      </div>

      {/* Description */}
      <p className="text-xs text-[#53433c] leading-relaxed font-normal">
        Your learning consistency has improved by <strong className="font-bold text-[#0f1b32]">18%</strong>... Recommended next step: Continue Spring Boot fundamentals.
      </p>
    </section>
  );
};
