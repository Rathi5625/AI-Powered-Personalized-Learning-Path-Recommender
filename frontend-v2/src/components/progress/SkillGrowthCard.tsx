import React from 'react';
import { motion } from 'framer-motion';

interface SkillGrowthItem {
  skill: string;
  current: number;
  target: number;
}

const SKILLS_DATA: SkillGrowthItem[] = [
  { skill: 'DSA', current: 61, target: 68 },
  { skill: 'Java', current: 72, target: 78 },
  { skill: 'SQL', current: 62, target: 71 },
];

export const SkillGrowthCard: React.FC = () => {
  return (
    <section
      aria-label="Skill Growth"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-5 sm:p-6 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-4"
    >
      <h3 className="text-base font-extrabold text-[#0f1b32] tracking-tight">
        Skill Growth
      </h3>

      <div className="space-y-3.5">
        {SKILLS_DATA.map((item) => (
          <div key={item.skill} className="space-y-1.5">
            <div className="flex items-center justify-between text-xs font-semibold">
              <span className="text-[#0f1b32]">{item.skill}</span>
              <span className="text-gray-400">
                {item.current}% → <strong className="text-[#8e4d2b] font-bold">{item.target}%</strong>
              </span>
            </div>

            {/* Progress Bar */}
            <div className="w-full h-2 bg-[#FAF4F0] rounded-full overflow-hidden">
              <motion.div
                initial={{ width: 0 }}
                animate={{ width: `${item.target}%` }}
                transition={{ duration: 1, ease: 'easeOut' }}
                className="h-full bg-[#d98b63] rounded-full"
              />
            </div>
          </div>
        ))}
      </div>
    </section>
  );
};
