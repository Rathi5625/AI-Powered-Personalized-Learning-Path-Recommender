import React from 'react';
import { Sparkles } from 'lucide-react';
import { motion } from 'framer-motion';

export interface AISkillMetric {
  name: string;
  percentage: number;
}

interface AISkillAnalysisCardProps {
  skills?: AISkillMetric[];
}

export const AISkillAnalysisCard: React.FC<AISkillAnalysisCardProps> = ({ skills = [] }) => {
  const hasSkills = skills && skills.length > 0;

  return (
    <section
      aria-label="AI Skill Analysis"
      className="relative overflow-hidden rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-8 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left flex flex-col justify-between"
    >
      <div className="space-y-4">
        {/* Card Header */}
        <div className="flex items-center gap-2">
          <Sparkles className="w-4 h-4 text-[#8e4d2b]" />
          <h3 className="text-base sm:text-lg font-extrabold text-[#0f1b32] tracking-tight">
            AI Skill Analysis
          </h3>
        </div>

        {/* Analysis Body */}
        {!hasSkills ? (
          <div className="py-4 text-left space-y-2">
            <p className="text-xs sm:text-[13px] text-[#53433c] leading-relaxed font-normal">
              No assessed skill data available yet. Complete a diagnostic assessment to calibrate your Bayesian Knowledge Tracing model.
            </p>
          </div>
        ) : (
          <>
            <p className="text-xs sm:text-[13px] text-[#53433c] leading-relaxed font-normal">
              Calibrated competency estimates based on your verified learning and assessment performance.
            </p>

            {/* Skill Progress List */}
            <div className="space-y-3 pt-2">
              {skills.map((skill, idx) => {
                const colors = [
                  { fill: 'bg-[#8e4d2b]', track: 'bg-[#ffdbcb]/80' },
                  { fill: 'bg-[#615a7a]', track: 'bg-[#e1d8fe]/80' },
                  { fill: 'bg-[#d98b63]', track: 'bg-[#ffdbcb]/80' },
                ];
                const color = colors[idx % colors.length];

                return (
                  <div key={skill.name} className="space-y-1.5">
                    <div className="flex items-center justify-between text-xs font-semibold">
                      <span className="text-[#0f1b32]">{skill.name}</span>
                      <span className="text-gray-500">{skill.percentage}%</span>
                    </div>
                    <div className={`w-full h-1.5 ${color.track} rounded-full overflow-hidden`}>
                      <motion.div
                        initial={{ width: 0 }}
                        animate={{ width: `${skill.percentage}%` }}
                        transition={{ duration: 0.8, ease: 'easeOut' }}
                        className={`h-full ${color.fill} rounded-full`}
                      />
                    </div>
                  </div>
                );
              })}
            </div>
          </>
        )}
      </div>
    </section>
  );
};
