import React from 'react';
import { Clock, BarChart2 } from 'lucide-react';
import { motion } from 'framer-motion';

export interface AssessmentItem {
  id: string;
  title: string;
  estimatedKnowledge: number | null;
  duration: string;
  difficulty: 'Advanced' | 'Hard' | 'Interm.' | 'Intermediate';
  actionType: 'Retake' | 'Assess';
  isRecommended?: boolean;
  needsImprovement?: boolean;
}

interface AssessmentCardProps {
  assessment: AssessmentItem;
  onAction: (assessment: AssessmentItem) => void;
}

export const AssessmentCard: React.FC<AssessmentCardProps> = ({
  assessment,
  onAction,
}) => {
  const hasProgress = assessment.estimatedKnowledge !== null;
  const progressValue = assessment.estimatedKnowledge ?? 0;

  return (
    <div className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-5 sm:p-6 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left flex flex-col justify-between space-y-4 hover:shadow-md transition-all">
      <div className="space-y-3">
        {/* Title & Est. Knowledge */}
        <div className="flex items-start justify-between gap-3">
          <h4 className="text-sm sm:text-base font-extrabold text-[#0f1b32] tracking-tight">
            {assessment.title}
          </h4>
        </div>

        <div className="flex items-center justify-between text-xs font-semibold">
          <span className="text-gray-500 font-medium">Est. Knowledge</span>
          <span
            className={`font-bold ${
              hasProgress ? (progressValue < 50 ? 'text-[#615a7a]' : 'text-[#8e4d2b]') : 'text-gray-400'
            }`}
          >
            {hasProgress ? `${progressValue}%` : '--'}
          </span>
        </div>

        {/* Progress Bar */}
        <div className="w-full h-2 bg-gray-100/90 rounded-full overflow-hidden">
          {hasProgress ? (
            <motion.div
              initial={{ width: 0 }}
              animate={{ width: `${progressValue}%` }}
              transition={{ duration: 0.8, ease: 'easeOut' }}
              className={`h-full rounded-full ${
                progressValue < 50 ? 'bg-[#615a7a]' : 'bg-[#8e4d2b]'
              }`}
            />
          ) : (
            <div className="w-0 h-full" />
          )}
        </div>
      </div>

      {/* Footer: Metadata & Action Link */}
      <div className="flex items-center justify-between pt-2 border-t border-gray-100/70 text-xs">
        <div className="flex items-center gap-3 text-gray-500 font-medium">
          <div className="flex items-center gap-1">
            <Clock className="w-3.5 h-3.5 text-gray-400" />
            <span>{assessment.duration}</span>
          </div>
          <div className="flex items-center gap-1">
            <BarChart2 className="w-3.5 h-3.5 text-gray-400" />
            <span>{assessment.difficulty}</span>
          </div>
        </div>

        <button
          type="button"
          onClick={() => onAction(assessment)}
          className="font-bold text-[#8e4d2b] hover:text-[#783e20] hover:underline transition-colors cursor-pointer"
        >
          {assessment.actionType}
        </button>
      </div>
    </div>
  );
};
