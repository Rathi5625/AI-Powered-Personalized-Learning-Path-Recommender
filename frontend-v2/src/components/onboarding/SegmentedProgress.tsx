import React from 'react';
import { motion } from 'framer-motion';

interface SegmentedProgressProps {
  currentStep: number;
  totalSteps: number;
}

export const SegmentedProgress: React.FC<SegmentedProgressProps> = ({
  currentStep,
  totalSteps,
}) => {
  return (
    <div className="w-full space-y-2.5">
      {/* Step label */}
      <div className="text-left">
        <span className="font-semibold text-gray-500 uppercase tracking-wider text-[11px] sm:text-xs">
          STEP {currentStep} OF {totalSteps}
        </span>
      </div>

      {/* 7 Segmented Track Bars */}
      <div className="grid grid-cols-7 gap-2 sm:gap-3 w-full">
        {Array.from({ length: totalSteps }).map((_, index) => {
          const isCompleted = index < currentStep;
          return (
            <div
              key={index}
              className="h-1.5 rounded-full overflow-hidden bg-[#EAE8FF]"
            >
              <motion.div
                initial={{ width: 0 }}
                animate={{ width: isCompleted ? '100%' : '0%' }}
                transition={{ duration: 0.4, delay: index * 0.05 }}
                className="h-full bg-[#CC7D52] rounded-full"
              />
            </div>
          );
        })}
      </div>
    </div>
  );
};
