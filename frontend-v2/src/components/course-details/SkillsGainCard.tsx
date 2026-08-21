import React from 'react';
import { Lightbulb } from 'lucide-react';

const SKILLS = [
  { name: 'Arrays', isGap: false },
  { name: 'Strings', isGap: false },
  { name: 'Linked Lists', isGap: false },
  { name: 'Trees', isGap: false },
  { name: 'Graphs', isGap: false },
  { name: 'Algorithms', isGap: false },
  { name: 'Problem Solving', isGap: true },
];

export const SkillsGainCard: React.FC = () => {
  return (
    <div className="rounded-3xl bg-white/70 backdrop-blur-2xl border border-white/80 p-6 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left flex flex-col justify-between h-full">
      <div>
        <div className="flex items-center gap-2 mb-4">
          <Lightbulb className="w-4 h-4 text-[#8e4d2b]" />
          <h3 className="text-sm sm:text-base font-bold text-[#0f1b32] tracking-tight">
            Skills You&apos;ll Gain
          </h3>
        </div>

        {/* Skill Chips */}
        <div className="flex flex-wrap gap-2">
          {SKILLS.map((skill) => (
            <span
              key={skill.name}
              className={`
                px-3 py-1.5 rounded-full text-xs font-semibold transition-all duration-150
                ${
                  skill.isGap
                    ? 'bg-[#ffdbcb] text-[#8e4d2b] border border-[#d98b63]/40 shadow-2xs'
                    : 'bg-[#d4e5f4]/50 text-[#50606d] border border-[#d4e5f4]/60'
                }
              `}
            >
              {skill.name}
            </span>
          ))}
        </div>
      </div>
    </div>
  );
};
