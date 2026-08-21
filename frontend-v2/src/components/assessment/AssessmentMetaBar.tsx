import React from 'react';
import { AssessmentTimer } from './AssessmentTimer';

interface AssessmentMetaBarProps {
  topic?: string;
  level?: string;
  totalQuestions?: number;
  onTimeUp?: () => void;
}

export const AssessmentMetaBar: React.FC<AssessmentMetaBarProps> = ({
  topic = 'Data Structures & Algorithms',
  level = 'Intermediate',
  totalQuestions = 20,
  onTimeUp,
}) => {
  return (
    <div className="w-full flex flex-col sm:flex-row sm:items-center justify-between gap-3 select-none">
      {/* Left Metadata Badges */}
      <div className="flex items-center gap-2 flex-wrap">
        <span className="px-3.5 py-1.5 rounded-full bg-white/75 backdrop-blur-md border border-gray-200/80 text-xs font-semibold text-[#0f1b32] shadow-2xs">
          {topic}
        </span>
        <span className="px-3.5 py-1.5 rounded-full bg-white/75 backdrop-blur-md border border-gray-200/80 text-xs font-semibold text-[#0f1b32] shadow-2xs">
          {level}
        </span>
        <span className="px-3.5 py-1.5 rounded-full bg-white/75 backdrop-blur-md border border-gray-200/80 text-xs font-semibold text-[#0f1b32] shadow-2xs">
          {totalQuestions} Questions
        </span>
      </div>

      {/* Right Countdown Timer */}
      <div className="self-end sm:self-auto">
        <AssessmentTimer onTimeUp={onTimeUp} />
      </div>
    </div>
  );
};
