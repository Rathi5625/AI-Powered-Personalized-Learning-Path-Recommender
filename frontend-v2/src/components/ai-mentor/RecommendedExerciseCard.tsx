import React from 'react';
import { Lightbulb } from 'lucide-react';

interface RecommendedExerciseCardProps {
  onStartExercise?: () => void;
}

export const RecommendedExerciseCard: React.FC<RecommendedExerciseCardProps> = ({
  onStartExercise,
}) => {
  return (
    <section
      aria-label="Recommended Exercise"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 border-l-4 border-l-[#8e4d2b] p-5 sm:p-6 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-3.5"
    >
      {/* Header */}
      <div className="flex items-center gap-1.5 text-[11px] font-extrabold text-[#8e4d2b] uppercase tracking-wider">
        <Lightbulb className="w-3.5 h-3.5 text-[#8e4d2b]" />
        <span>RECOMMENDED</span>
      </div>

      {/* Title */}
      <h4 className="text-sm sm:text-base font-extrabold text-[#0f1b32] tracking-tight">
        Practice Binary Search
      </h4>

      {/* Description */}
      <p className="text-xs text-[#53433c] leading-relaxed font-normal">
        Solidify your understanding by solving a practical problem finding a target in a sorted array.
      </p>

      {/* Action Button */}
      <div className="pt-1">
        <button
          type="button"
          onClick={onStartExercise}
          className="w-full py-2.5 px-4 rounded-xl bg-white/90 hover:bg-[#FAF4F0] text-[#8e4d2b] text-xs font-bold border border-[#F2DACB] transition-colors cursor-pointer text-center shadow-2xs active:scale-[0.99]"
        >
          Start Exercise
        </button>
      </div>
    </section>
  );
};
