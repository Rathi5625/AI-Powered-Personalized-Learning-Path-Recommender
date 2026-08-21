import React from 'react';
import { ArrowLeft, ArrowRight, CheckCircle2 } from 'lucide-react';

interface AssessmentNavigationProps {
  currentQuestion: number;
  totalQuestions: number;
  onPrevious: () => void;
  onNext: () => void;
  onSubmit: () => void;
}

export const AssessmentNavigation: React.FC<AssessmentNavigationProps> = ({
  currentQuestion,
  totalQuestions,
  onPrevious,
  onNext,
  onSubmit,
}) => {
  const isFirst = currentQuestion <= 1;
  const isLast = currentQuestion >= totalQuestions;

  return (
    <div className="w-full flex items-center justify-between gap-4 pt-2 select-none">
      {/* Previous Button */}
      <button
        type="button"
        disabled={isFirst}
        onClick={onPrevious}
        className="inline-flex items-center gap-2 text-xs sm:text-sm font-bold text-[#0f1b32] hover:text-[#8e4d2b] transition-colors cursor-pointer disabled:opacity-35 disabled:cursor-not-allowed py-2 px-1"
      >
        <ArrowLeft className="w-4 h-4" />
        <span>Previous</span>
      </button>

      {/* Next or Submit Button */}
      {isLast ? (
        <button
          type="button"
          onClick={onSubmit}
          className="inline-flex items-center gap-2 px-7 py-3 rounded-2xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs sm:text-sm shadow-md hover:shadow-lg transition-all duration-200 cursor-pointer active:scale-[0.98]"
        >
          <span>Submit Assessment</span>
          <CheckCircle2 className="w-4 h-4" />
        </button>
      ) : (
        <button
          type="button"
          onClick={onNext}
          className="inline-flex items-center gap-2 px-7 py-3 rounded-2xl bg-[#8e4d2b] hover:bg-[#783e20] text-white font-bold text-xs sm:text-sm shadow-md hover:shadow-lg transition-all duration-200 cursor-pointer active:scale-[0.98]"
        >
          <span>Next Question</span>
          <ArrowRight className="w-4 h-4" />
        </button>
      )}
    </div>
  );
};
