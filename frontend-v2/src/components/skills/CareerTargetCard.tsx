import React from 'react';
import { motion } from 'framer-motion';

interface CareerTargetCardProps {
  careerTarget?: string;
  readinessScore?: number;
  requiredSkillsCount?: number;
  strongSkillsCount?: number;
  criticalGapsCount?: number;
}

export const CareerTargetCard: React.FC<CareerTargetCardProps> = ({
  careerTarget = 'Software Engineer',
  readinessScore = 0,
  requiredSkillsCount = 0,
  strongSkillsCount = 0,
  criticalGapsCount = 0,
}) => {
  const isAssessed = readinessScore > 0;

  return (
    <section
      aria-label="Career Target & Overall Readiness"
      className="relative overflow-hidden rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-8 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left flex flex-col justify-between"
    >
      <div className="space-y-6">
        {/* Top Header: Career Target & Readiness */}
        <div className="flex items-start justify-between gap-4 flex-wrap">
          <div>
            <span className="text-[11px] font-bold uppercase tracking-wider text-[#8e4d2b] block mb-1">
              CAREER TARGET
            </span>
            <h2 className="text-2xl sm:text-3xl font-extrabold text-[#0f1b32] tracking-tight">
              {careerTarget}
            </h2>
          </div>

          <div className="text-right">
            <span className="text-3xl sm:text-4xl font-extrabold text-[#8e4d2b] tracking-tight block leading-none">
              {isAssessed ? `${readinessScore}%` : 'Assessment needed'}
            </span>
            <span className="text-xs text-gray-500 font-medium block mt-1">
              Overall Readiness
            </span>
          </div>
        </div>

        {/* 3 Metric Cards */}
        <div className="grid grid-cols-3 gap-3 sm:gap-6 pt-2">
          {/* Required Skills */}
          <div>
            <span className="text-2xl sm:text-3xl font-extrabold text-[#0f1b32] block leading-none mb-1">
              {requiredSkillsCount}
            </span>
            <span className="text-[11px] sm:text-xs text-gray-500 font-medium">
              Required Skills
            </span>
          </div>

          {/* Strong Skills */}
          <div>
            <span className="text-2xl sm:text-3xl font-extrabold text-[#8e4d2b] block leading-none mb-1">
              {strongSkillsCount}
            </span>
            <span className="text-[11px] sm:text-xs text-gray-500 font-medium">
              Strong Skills
            </span>
          </div>

          {/* Critical Gaps */}
          <div>
            <span className="text-2xl sm:text-3xl font-extrabold text-[#ba1a1a] block leading-none mb-1">
              {criticalGapsCount}
            </span>
            <span className="text-[11px] sm:text-xs text-gray-500 font-medium">
              Critical Gaps
            </span>
          </div>
        </div>
      </div>

      {/* Bottom Horizontal Readiness Progress Bar */}
      <div className="pt-6">
        <div className="w-full h-2.5 bg-[#d8e2ff]/70 rounded-full overflow-hidden">
          <motion.div
            initial={{ width: 0 }}
            animate={{ width: `${Math.max(0, Math.min(100, readinessScore))}%` }}
            transition={{ duration: 0.9, ease: 'easeOut' }}
            className="h-full bg-[#8e4d2b] rounded-full"
          />
        </div>
      </div>
    </section>
  );
};
