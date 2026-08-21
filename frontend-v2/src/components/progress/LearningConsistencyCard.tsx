import React from 'react';

// 7 columns x 2 rows of heatmap tiles representing activity levels
const HEATMAP_CELLS = [
  'bg-[#ffdbcb]/60',
  'bg-[#d98b63]/50',
  'bg-[#d98b63]',
  'bg-[#d98b63]',
  'bg-[#e9edff]',
  'bg-[#d98b63]/50',
  'bg-[#8e4d2b]',
  'bg-[#e9edff]',
  'bg-[#ffdbcb]/60',
  'bg-[#d98b63]',
  'bg-[#d98b63]',
  'bg-[#d98b63]/70',
  'bg-[#d98b63]',
  'bg-[#8e4d2b]',
];

export const LearningConsistencyCard: React.FC = () => {
  return (
    <section
      aria-label="Learning Consistency and Streaks"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-5 sm:p-6 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-4"
    >
      <h3 className="text-base font-extrabold text-[#0f1b32] tracking-tight">
        Learning Consistency
      </h3>

      {/* Streak Counters */}
      <div className="flex items-center justify-between">
        <div>
          <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider block">
            CURRENT STREAK
          </span>
          <span className="text-sm sm:text-base font-extrabold text-[#0f1b32] flex items-center gap-1 mt-0.5">
            🔥 7 Days
          </span>
        </div>

        <div className="text-right">
          <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider block">
            LONGEST STREAK
          </span>
          <span className="text-sm sm:text-base font-extrabold text-[#0f1b32] mt-0.5 block">
            21 Days
          </span>
        </div>
      </div>

      {/* 7-Column Heatmap Grid */}
      <div className="grid grid-cols-7 gap-2 pt-2 border-t border-gray-100/80">
        {HEATMAP_CELLS.map((cellColor, index) => (
          <div
            key={index}
            className={`w-full aspect-square rounded-lg ${cellColor} shadow-2xs transition-transform hover:scale-110 cursor-pointer`}
          />
        ))}
      </div>
    </section>
  );
};
