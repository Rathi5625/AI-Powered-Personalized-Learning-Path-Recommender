import React from 'react';
import { CheckCircle2 } from 'lucide-react';

interface SkillChip {
  name: string;
  dotColor: string;
}

const SKILL_CHIPS: SkillChip[] = [
  { name: 'Spring Boot', dotColor: '#8e4d2b' },
  { name: 'REST APIs', dotColor: '#615a7a' },
  { name: 'JPA / Hibernate', dotColor: '#50606d' },
  { name: 'MySQL', dotColor: '#88A98F' },
  { name: 'JWT Security', dotColor: '#d98b63' },
];

const OUTCOMES: string[] = [
  'Secure user registration and authentication endpoints',
  'Robust product catalog management APIs',
  'Shopping cart logic with stateful session handling',
  'Order processing and database transaction management',
];

export const SkillsOutcomesCard: React.FC = () => {
  return (
    <section
      aria-label="Skills and Outcomes"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left flex flex-col justify-between space-y-5"
    >
      {/* Top: Skills You'll Practice */}
      <div className="space-y-3">
        <h3 className="text-sm sm:text-base font-extrabold text-[#0f1b32] tracking-tight">
          Skills you&apos;ll practice
        </h3>

        {/* Skill Chips */}
        <div className="flex items-center gap-2 flex-wrap">
          {SKILL_CHIPS.map((chip) => (
            <span
              key={chip.name}
              className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#f1f3ff] border border-gray-200/80 text-xs font-semibold text-[#0f1b32]"
            >
              <span
                className="w-2 h-2 rounded-full shrink-0"
                style={{ backgroundColor: chip.dotColor }}
              />
              <span>{chip.name}</span>
            </span>
          ))}
        </div>
      </div>

      {/* Bottom: What You'll Build */}
      <div className="space-y-2.5 pt-1 border-t border-gray-100/80">
        <h4 className="text-xs font-bold text-gray-500 uppercase tracking-wider">
          What you&apos;ll build
        </h4>

        <div className="space-y-2">
          {OUTCOMES.map((item, idx) => (
            <div key={idx} className="flex items-start gap-2 text-xs text-[#53433c]">
              <CheckCircle2 className="w-3.5 h-3.5 text-[#88A98F] shrink-0 mt-0.5" />
              <span className="leading-relaxed">{item}</span>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};
