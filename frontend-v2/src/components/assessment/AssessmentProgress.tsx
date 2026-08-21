import React from 'react';
import { Check } from 'lucide-react';
import { motion } from 'framer-motion';

interface AssessmentProgressProps {
  currentQuestion: number;
  totalQuestions: number;
}

export const AssessmentProgress: React.FC<AssessmentProgressProps> = ({
  currentQuestion,
  totalQuestions,
}) => {
  const progressPercent = Math.round((currentQuestion / totalQuestions) * 100);

  return (
    <div className="rounded-2xl bg-white/75 backdrop-blur-xl border border-white/90 p-4 sm:p-5 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-2.5 select-none">
      {/* Top Text Row */}
      <div className="flex items-center justify-between">
        <h3 className="text-sm sm:text-base font-extrabold text-[#0f1b32] tracking-tight">
          Question {currentQuestion} of {totalQuestions}
        </h3>
        <span className="text-xs text-gray-500 font-semibold">
          {progressPercent}% complete
        </span>
      </div>

      {/* Progress Bar */}
      <div className="w-full h-2.5 bg-[#d8e2ff]/60 rounded-full overflow-hidden">
        <motion.div
          key={currentQuestion}
          initial={{ width: `${Math.max(0, progressPercent - 5)}%` }}
          animate={{ width: `${progressPercent}%` }}
          transition={{ duration: 0.5, ease: 'easeOut' }}
          className="h-full bg-[#8e4d2b] rounded-full"
        />
      </div>

      {/* Bottom Status */}
      <div className="flex items-center justify-end gap-1 text-[#88A98F] text-[11px] font-semibold">
        <Check className="w-3 h-3 stroke-[3]" />
        <span>Progress saved</span>
      </div>
    </div>
  );
};
