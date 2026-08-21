import React from 'react';
import { motion } from 'framer-motion';

interface OnboardingProgressProps {
  currentStep: number;
  totalSteps: number;
  stepTitle?: string;
  stepLabel?: string;
  rightLabel?: string;
  percentage?: number;
}

export const OnboardingProgress: React.FC<OnboardingProgressProps> = ({
  currentStep,
  totalSteps,
  stepTitle,
  stepLabel,
  rightLabel,
  percentage,
}) => {
  const calcPercentage = percentage ?? Math.round((currentStep / totalSteps) * 100);
  const leftText = stepLabel || `Step ${currentStep} of ${totalSteps}`;
  const rightText = rightLabel || stepTitle || `${calcPercentage}%`;

  return (
    <div className="w-full space-y-2.5">
      {/* Step counter and title/percentage row */}
      <div className="flex items-center justify-between text-xs sm:text-sm">
        <span className="font-semibold text-gray-500 uppercase tracking-wider text-[11px] sm:text-xs">
          {leftText}
        </span>
        <span className="font-bold text-[#8B4D2B] text-xs sm:text-sm">
          {rightText}
        </span>
      </div>

      {/* Progress track */}
      <div className="w-full h-1.5 bg-[#EAE8FF] rounded-full overflow-hidden">
        <motion.div
          initial={{ width: 0 }}
          animate={{ width: `${calcPercentage}%` }}
          transition={{ duration: 0.6, ease: [0.16, 1, 0.3, 1] }}
          className="h-full bg-[#8B4D2B] rounded-full"
        />
      </div>
    </div>
  );
};
