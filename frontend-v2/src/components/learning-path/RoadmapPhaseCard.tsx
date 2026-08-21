import React from 'react';
import { motion } from 'framer-motion';
import { Check, ArrowRight } from 'lucide-react';

export type PhaseStatus = 'completed' | 'in_progress' | 'upcoming';

export interface SkillItem {
  name: string;
  status: 'completed' | 'current' | 'upcoming';
}

export interface RoadmapPhase {
  id: string;
  phaseNumber: string;
  title: string;
  status: PhaseStatus;
  progressPercent?: number;
  skills: SkillItem[];
}

interface RoadmapPhaseCardProps {
  phase: RoadmapPhase;
  onContinue?: () => void;
}

export const RoadmapPhaseCard: React.FC<RoadmapPhaseCardProps> = ({
  phase,
  onContinue,
}) => {
  const isCompleted = phase.status === 'completed';
  const isInProgress = phase.status === 'in_progress';

  return (
    <div className="relative flex items-start gap-4 sm:gap-6 text-left">
      {/* Left Timeline Marker */}
      <div className="flex flex-col items-center shrink-0 z-10">
        <div
          className={`
            w-8 h-8 rounded-full flex items-center justify-center font-bold text-xs shadow-xs transition-colors
            ${
              isCompleted
                ? 'bg-[#8e4d2b] text-white'
                : isInProgress
                ? 'bg-[#8e4d2b] text-white ring-4 ring-[#ffdbcb]'
                : 'bg-white border-2 border-gray-300 text-gray-400'
            }
          `}
        >
          {isCompleted ? (
            <Check className="w-4 h-4 stroke-[2.5]" />
          ) : isInProgress ? (
            <span className="w-2 h-2 rounded-full bg-white" />
          ) : (
            <span className="text-[10px]">{phase.phaseNumber}</span>
          )}
        </div>
      </div>

      {/* Main Phase Card */}
      <motion.div
        whileHover={{ y: -2 }}
        className={`
          flex-1 bg-white/80 backdrop-blur-xl border rounded-2xl sm:rounded-3xl p-5 sm:p-6 shadow-[0_12px_32px_rgba(15,27,50,0.03)] transition-all
          ${
            isInProgress
              ? 'border-[#d98b63]/60 ring-1 ring-[#d98b63]/20 shadow-[0_16px_40px_rgba(142,77,43,0.06)]'
              : isCompleted
              ? 'border-gray-100/90 opacity-90'
              : 'border-gray-100/80 opacity-75'
          }
        `}
      >
        {/* Phase Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-3">
          <div>
            <div className="flex items-center gap-2">
              <span className="text-[10px] font-bold text-gray-400 uppercase tracking-widest">
                PHASE {phase.phaseNumber}
              </span>
              <span
                className={`
                  text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full
                  ${
                    isCompleted
                      ? 'bg-emerald-50 text-emerald-700 border border-emerald-200/60'
                      : isInProgress
                      ? 'bg-[#FAF4F0] text-[#8e4d2b] border border-[#F2DACB]'
                      : 'bg-gray-100 text-gray-500'
                  }
                `}
              >
                {isCompleted
                  ? 'Completed'
                  : isInProgress
                  ? `In Progress (${phase.progressPercent}%)`
                  : 'Upcoming'}
              </span>
            </div>
            <h4 className="text-base sm:text-lg font-bold text-[#0f1b32] tracking-tight mt-1">
              {phase.title}
            </h4>
          </div>

          {/* Action button if in progress */}
          {isInProgress && (
            <button
              type="button"
              onClick={onContinue}
              className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-[#8e4d2b] hover:bg-[#783e20] text-white text-xs font-bold transition-all shadow-sm cursor-pointer self-start sm:self-auto"
            >
              <span>Continue Learning</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </button>
          )}
        </div>

        {/* Progress Bar (In Progress only) */}
        {isInProgress && phase.progressPercent !== undefined && (
          <div className="mb-4">
            <div className="w-full h-1.5 bg-[#EAE8FF] rounded-full overflow-hidden">
              <div
                className="h-full bg-[#8e4d2b] rounded-full transition-all duration-500"
                style={{ width: `${phase.progressPercent}%` }}
              />
            </div>
          </div>
        )}

        {/* Skills Chips / Sub-list */}
        <div className="pt-2 border-t border-gray-100/80">
          <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider block mb-2">
            SKILLS COVERED
          </span>
          <div className="flex flex-wrap gap-2">
            {phase.skills.map((skill, idx) => {
              const isSkillDone = skill.status === 'completed';
              const isSkillCurrent = skill.status === 'current';

              return (
                <span
                  key={idx}
                  className={`
                    inline-flex items-center gap-1.5 px-3 py-1 rounded-xl text-xs font-medium transition-all
                    ${
                      isSkillDone
                        ? 'bg-emerald-50/80 text-emerald-800 border border-emerald-200/50 line-through opacity-80'
                        : isSkillCurrent
                        ? 'bg-[#FAF4F0] text-[#8e4d2b] border border-[#8e4d2b] font-bold shadow-2xs'
                        : 'bg-gray-50 text-gray-500 border border-gray-200/60'
                    }
                  `}
                >
                  {isSkillCurrent && (
                    <span className="w-1.5 h-1.5 rounded-full bg-[#8e4d2b] animate-ping inline-block" />
                  )}
                  {isSkillDone && (
                    <Check className="w-3 h-3 text-emerald-600 stroke-[2.5]" />
                  )}
                  <span>{skill.name}</span>
                </span>
              );
            })}
          </div>
        </div>
      </motion.div>
    </div>
  );
};
