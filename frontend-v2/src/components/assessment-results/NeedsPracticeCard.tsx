import React from 'react';
import { TrendingDown } from 'lucide-react';

interface NeedsPracticeItem {
  name: string;
  score: number;
}

const NEEDS_PRACTICE: NeedsPracticeItem[] = [
  { name: 'Graphs', score: 48 },
  { name: 'Trees', score: 54 },
  { name: 'Complexity Analysis', score: 61 },
];

export const NeedsPracticeCard: React.FC = () => {
  return (
    <div className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-5 sm:p-6 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left flex flex-col justify-between space-y-4">
      {/* Header */}
      <div className="flex items-center gap-2 text-[#8e4d2b]">
        <TrendingDown className="w-4 h-4 text-[#8e4d2b]" />
        <h3 className="text-sm sm:text-base font-extrabold text-[#8e4d2b] tracking-tight">
          Needs Practice
        </h3>
      </div>

      {/* Items List */}
      <div className="space-y-2.5">
        {NEEDS_PRACTICE.map((item) => (
          <div
            key={item.name}
            className="flex items-center justify-between py-1 text-xs sm:text-sm font-semibold"
          >
            <span className="text-[#0f1b32]">{item.name}</span>
            <span className="px-2.5 py-0.5 rounded-full bg-[#ffdbcb]/60 border border-[#d98b63]/30 text-[#8e4d2b] text-xs font-bold">
              {item.score}%
            </span>
          </div>
        ))}
      </div>
    </div>
  );
};
