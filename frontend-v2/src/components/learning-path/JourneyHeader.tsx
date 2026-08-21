import React from 'react';

export type JourneyFilter = 'all' | 'in_progress' | 'completed';

interface JourneyHeaderProps {
  currentFilter: JourneyFilter;
  onFilterChange: (filter: JourneyFilter) => void;
  summaryText?: string;
}

const FILTER_OPTIONS: { id: JourneyFilter; label: string }[] = [
  { id: 'all', label: 'All' },
  { id: 'in_progress', label: 'In Progress' },
  { id: 'completed', label: 'Completed' },
];

export const JourneyHeader: React.FC<JourneyHeaderProps> = ({
  currentFilter,
  onFilterChange,
  summaryText = '5 phases · 24 skills · 8 projects · 6 months',
}) => {
  return (
    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-left">
      <div>
        <h3 className="text-lg sm:text-xl font-extrabold text-[#0f1b32] tracking-tight">
          Your Journey
        </h3>
        <p className="text-xs text-gray-500 mt-0.5 font-normal">
          {summaryText}
        </p>
      </div>

      {/* Filter Tabs */}
      <div className="flex items-center gap-1 bg-white/70 p-1 rounded-xl border border-gray-200/80 shadow-2xs self-start sm:self-auto">
        {FILTER_OPTIONS.map((tab) => {
          const isActive = currentFilter === tab.id;
          return (
            <button
              key={tab.id}
              type="button"
              onClick={() => onFilterChange(tab.id)}
              className={`
                px-3 py-1.5 rounded-lg text-xs font-semibold transition-all duration-150 cursor-pointer select-none
                ${
                  isActive
                    ? 'bg-[#ffdbcb]/70 text-[#8e4d2b] shadow-2xs'
                    : 'text-gray-600 hover:text-[#0f1b32] hover:bg-black/[0.02]'
                }
              `}
            >
              {tab.label}
            </button>
          );
        })}
      </div>
    </div>
  );
};
