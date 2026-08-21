import React from 'react';
import { Sparkles } from 'lucide-react';
import { StrengthsCard } from './StrengthsCard';
import { NeedsPracticeCard } from './NeedsPracticeCard';

export const ResultsInsights: React.FC = () => {
  return (
    <section aria-label="AI Performance Insights" className="space-y-3.5 text-left">
      {/* Heading with Sparkles Icon */}
      <div className="flex items-center gap-2">
        <Sparkles className="w-4 h-4 text-[#8e4d2b]" />
        <h2 className="text-base sm:text-lg font-extrabold text-[#0f1b32] tracking-tight">
          What your results tell us
        </h2>
      </div>

      {/* 2-Column Grid of Strengths & Needs Practice */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 sm:gap-5 items-stretch">
        <StrengthsCard />
        <NeedsPracticeCard />
      </div>
    </section>
  );
};
