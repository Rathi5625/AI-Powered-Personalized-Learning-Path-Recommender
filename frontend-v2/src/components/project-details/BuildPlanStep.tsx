import React from 'react';
import { Check, Clock } from 'lucide-react';
import { motion } from 'framer-motion';

export interface BuildPlanStepData {
  day: number;
  title: string;
  duration: string;
  description: string;
  status: 'completed' | 'current' | 'upcoming';
}

interface BuildPlanStepProps {
  step: BuildPlanStepData;
  isLast?: boolean;
  onAction?: (step: BuildPlanStepData) => void;
  isHighlighted?: boolean;
}

export const BuildPlanStep: React.FC<BuildPlanStepProps> = ({
  step,
  isLast = false,
  onAction,
  isHighlighted = false,
}) => {
  const isCompleted = step.status === 'completed';
  const isCurrent = step.status === 'current';

  return (
    <div className="relative flex items-start gap-4 sm:gap-6 text-left group">
      {/* Connecting Vertical Line */}
      {!isLast && (
        <div
          className={`absolute left-4 top-8 -bottom-4 w-0.5 ${
            isCompleted ? 'bg-[#8e4d2b]/40' : 'bg-gray-200/80'
          }`}
        />
      )}

      {/* Circle Status Indicator */}
      <div className="relative z-10 shrink-0">
        {isCompleted ? (
          <div className="w-8 h-8 rounded-full bg-[#f1f3ff] border border-[#ffdbcb] text-[#8e4d2b] flex items-center justify-center shadow-2xs">
            <Check className="w-4 h-4 text-[#8e4d2b]" />
          </div>
        ) : isCurrent ? (
          <div className="w-8 h-8 rounded-full bg-[#8e4d2b] text-white flex items-center justify-center font-extrabold text-xs shadow-md ring-4 ring-[#ffdbcb]/60">
            {step.day}
          </div>
        ) : (
          <div className="w-8 h-8 rounded-full bg-white/80 border border-gray-200 text-gray-400 flex items-center justify-center font-bold text-xs">
            {step.day}
          </div>
        )}
      </div>

      {/* Step Content Card */}
      <motion.div
        animate={
          isHighlighted
            ? {
                scale: [1, 1.02, 1],
                boxShadow: [
                  '0 0 0 rgba(142,77,43,0)',
                  '0 0 20px rgba(142,77,43,0.3)',
                  '0 0 0 rgba(142,77,43,0)',
                ],
              }
            : {}
        }
        transition={{ duration: 0.8 }}
        id={`build-step-${step.day}`}
        className={`
          flex-1 p-4 sm:p-5 rounded-2xl transition-all duration-200
          ${
            isCurrent
              ? 'bg-white/95 border-2 border-[#d98b63] shadow-md'
              : 'bg-white/60 hover:bg-white/90 border border-gray-100/90 shadow-2xs'
          }
        `}
      >
        {/* Header: Day & Duration */}
        <div className="flex items-center justify-between gap-2 mb-1.5 flex-wrap">
          <div className="flex items-center gap-2">
            {isCurrent && (
              <span className="inline-flex items-center gap-1 text-[10px] font-extrabold text-[#8e4d2b] uppercase tracking-wider">
                <span className="w-1.5 h-1.5 rounded-full bg-[#8e4d2b] animate-pulse" />
                CURRENT FOCUS
              </span>
            )}
            <span className="text-[11px] font-bold text-gray-400 uppercase tracking-wider">
              DAY {step.day}
            </span>
          </div>

          <div className="flex items-center gap-1 text-xs text-gray-400 font-medium">
            <Clock className="w-3.5 h-3.5 text-gray-400" />
            <span>{step.duration}</span>
          </div>
        </div>

        {/* Title */}
        <h4 className="text-sm sm:text-base font-extrabold text-[#0f1b32] tracking-tight">
          {step.title}
        </h4>

        {/* Description */}
        <p className="text-xs text-[#53433c] leading-relaxed font-normal mt-1">
          {step.description}
        </p>

        {/* Resume Button for Current Step */}
        {isCurrent && (
          <div className="pt-3">
            <button
              type="button"
              onClick={() => onAction?.(step)}
              className="px-4 py-2 rounded-xl bg-[#FAF4F0] hover:bg-[#F2DACB]/60 text-[#8e4d2b] font-bold text-xs border border-[#F2DACB] transition-colors cursor-pointer"
            >
              Resume Section
            </button>
          </div>
        )}
      </motion.div>
    </div>
  );
};
