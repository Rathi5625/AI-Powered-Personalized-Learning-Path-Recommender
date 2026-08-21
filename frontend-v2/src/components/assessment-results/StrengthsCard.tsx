import React from 'react';
import { TrendingUp } from 'lucide-react';

interface StrengthItem {
  name: string;
  score: number;
}

const STRENGTHS: StrengthItem[] = [
  { name: 'Arrays', score: 86 },
  { name: 'Strings', score: 82 },
  { name: 'Basic Searching', score: 79 },
];

export const StrengthsCard: React.FC = () => {
  return (
    <div className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-5 sm:p-6 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left flex flex-col justify-between space-y-4">
      {/* Header */}
      <div className="flex items-center gap-2 text-[#88A98F]">
        <TrendingUp className="w-4 h-4 text-[#88A98F]" />
        <h3 className="text-sm sm:text-base font-extrabold text-[#88A98F] tracking-tight">
          Strengths
        </h3>
      </div>

      {/* Items List */}
      <div className="space-y-2.5">
        {STRENGTHS.map((item) => (
          <div
            key={item.name}
            className="flex items-center justify-between py-1 text-xs sm:text-sm font-semibold"
          >
            <span className="text-[#0f1b32]">{item.name}</span>
            <span className="px-2.5 py-0.5 rounded-full bg-[#88A98F]/15 border border-[#88A98F]/30 text-[#4e7456] text-xs font-bold">
              {item.score}%
            </span>
          </div>
        ))}
      </div>
    </div>
  );
};
