import React from 'react';
import { CheckCircle2, Clock } from 'lucide-react';
import { ScoreRing } from './ScoreRing';

interface AssessmentScoreCardProps {
  title?: string;
  score?: number;
  label?: string;
  correctCount?: number;
  totalQuestions?: number;
  duration?: string;
}

export const AssessmentScoreCard: React.FC<AssessmentScoreCardProps> = ({
  title = 'DSA Fundamentals',
  score = 78,
  label = 'Good progress',
  correctCount = 15,
  totalQuestions = 20,
  duration = '16m 42s',
}) => {
  return (
    <section
      aria-label="Assessment Score Overview"
      className="relative overflow-hidden rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-8 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left"
    >
      {/* Decorative blurred terracotta glow in background */}
      <div className="pointer-events-none absolute -right-16 -top-16 w-64 h-64 rounded-full bg-radial from-[#ffdbcb]/40 to-transparent blur-2xl" />

      <div className="relative z-10 flex flex-col md:flex-row items-center justify-between gap-6 sm:gap-8">
        {/* Left: Animated Score Ring */}
        <div className="shrink-0 flex justify-center">
          <ScoreRing score={score} label={label} size={160} strokeWidth={14} />
        </div>

        {/* Right: Assessment Details & Stats */}
        <div className="flex-1 w-full space-y-4 text-left">
          <h2 className="text-xl sm:text-2xl font-extrabold text-[#0f1b32] tracking-tight">
            {title}
          </h2>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
            {/* Stat Card 1: Correct */}
            <div className="p-4 rounded-2xl bg-white/80 border border-gray-100/90 shadow-2xs space-y-1">
              <div className="flex items-center gap-1.5 text-xs text-gray-500 font-medium">
                <CheckCircle2 className="w-3.5 h-3.5 text-[#8e4d2b]" />
                <span>Correct</span>
              </div>
              <p className="text-lg sm:text-xl font-extrabold text-[#0f1b32] tracking-tight">
                {correctCount} <span className="text-xs text-gray-400 font-normal">/ {totalQuestions}</span>
              </p>
            </div>

            {/* Stat Card 2: Duration */}
            <div className="p-4 rounded-2xl bg-white/80 border border-gray-100/90 shadow-2xs space-y-1">
              <div className="flex items-center gap-1.5 text-xs text-gray-500 font-medium">
                <Clock className="w-3.5 h-3.5 text-[#8e4d2b]" />
                <span>Duration</span>
              </div>
              <p className="text-lg sm:text-xl font-extrabold text-[#0f1b32] tracking-tight">
                {duration}
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};
