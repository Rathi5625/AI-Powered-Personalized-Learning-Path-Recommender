import React from 'react';
import { motion } from 'framer-motion';

export interface SkillGapItem {
  id: string;
  name: string;
  priority: 'Critical' | 'High';
  currentPercentage: number;
  requiredPercentage: number;
}

interface SkillGapAnalysisCardProps {
  skillGaps?: SkillGapItem[];
  onImproveSkill?: (skill: SkillGapItem) => void;
}

export const SkillGapAnalysisCard: React.FC<SkillGapAnalysisCardProps> = ({
  skillGaps = [],
  onImproveSkill,
}) => {
  const hasGaps = skillGaps && skillGaps.length > 0;
  return (
    <section
      aria-label="Skill Gap Analysis"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-8 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-6"
    >
      <h2 className="text-xl sm:text-2xl font-extrabold text-[#0f1b32] tracking-tight">
        Skill Gap Analysis
      </h2>

      {!hasGaps ? (
        <div className="py-8 px-4 text-center space-y-3 bg-[#FAF4F0]/60 rounded-2xl border border-[#F2DACB]/60">
          <p className="text-sm font-bold text-[#0f1b32]">No Skill Gaps Identified Yet</p>
          <p className="text-xs text-gray-500 max-w-md mx-auto">
            Take a diagnostic assessment to measure your competency levels against your target career requirements.
          </p>
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full min-w-[620px] text-left border-collapse">
            <thead>
              <tr className="border-b border-gray-100 text-[11px] font-bold text-gray-500 uppercase tracking-wider">
                <th scope="col" className="pb-3 font-semibold">
                  Skill Area
                </th>
                <th scope="col" className="pb-3 font-semibold text-center w-28">
                  Priority
                </th>
                <th scope="col" className="pb-3 font-semibold text-center w-80">
                  Current vs Required
                </th>
                <th scope="col" className="pb-3 font-semibold text-right w-24">
                  Action
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100/80">
              {skillGaps.map((gap) => (
                <tr key={gap.id} className="group hover:bg-black/[0.01] transition-colors">
                  <td className="py-4 pr-4">
                    <span className="text-xs sm:text-sm font-bold text-[#0f1b32]">
                      {gap.name}
                    </span>
                  </td>

                  <td className="py-4 px-2 text-center">
                    <span
                      className={`
                        inline-flex items-center px-3 py-1 rounded-full text-xs font-bold
                        ${
                          gap.priority === 'Critical'
                            ? 'bg-red-50 text-red-600 border border-red-200/60'
                            : 'bg-[#FAF4F0] text-[#8e4d2b] border border-[#F2DACB]'
                        }
                      `}
                    >
                      {gap.priority}
                    </span>
                  </td>

                  <td className="py-4 px-4">
                    <div className="flex items-center gap-3">
                      <span className="text-xs font-semibold text-gray-600 w-8 text-right shrink-0">
                        {gap.currentPercentage}%
                      </span>

                      <div className="relative flex-1 h-2.5 bg-[#d8e2ff]/80 rounded-full overflow-visible">
                        <motion.div
                          initial={{ width: 0 }}
                          animate={{ width: `${gap.currentPercentage}%` }}
                          transition={{ duration: 0.8, ease: 'easeOut' }}
                          className="h-full bg-[#8e4d2b] rounded-l-full"
                        />

                        <div
                          className="absolute top-1/2 -translate-y-1/2 w-0.5 h-4 bg-[#0f1b32] rounded-full z-10"
                          style={{ left: `${gap.requiredPercentage}%` }}
                          title={`Target: ${gap.requiredPercentage}%`}
                        />
                      </div>

                      <span className="text-xs font-bold text-[#0f1b32] w-8 shrink-0">
                        {gap.requiredPercentage}%
                      </span>
                    </div>
                  </td>

                  <td className="py-4 pl-4 text-right">
                    <button
                      type="button"
                      onClick={() => onImproveSkill?.(gap)}
                      className="text-xs sm:text-sm font-bold text-[#8e4d2b] hover:text-[#783e20] hover:underline cursor-pointer transition-colors"
                    >
                      Improve
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
};
