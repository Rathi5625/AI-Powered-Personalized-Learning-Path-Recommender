import React from 'react';

export type AssessmentFilterType = 'All' | 'Recommended' | 'Not Assessed' | 'Needs Improvement';

interface AssessmentFiltersProps {
  currentFilter: AssessmentFilterType;
  onFilterChange: (filter: AssessmentFilterType) => void;
}

const FILTERS: AssessmentFilterType[] = [
  'All',
  'Recommended',
  'Not Assessed',
  'Needs Improvement',
];

export const AssessmentFilters: React.FC<AssessmentFiltersProps> = ({
  currentFilter,
  onFilterChange,
}) => {
  return (
    <div className="flex items-center gap-2 sm:gap-2.5 overflow-x-auto pb-1 select-none scrollbar-none">
      {FILTERS.map((filter) => {
        const isActive = currentFilter === filter;

        return (
          <button
            key={filter}
            type="button"
            onClick={() => onFilterChange(filter)}
            className={`
              px-4 py-2 rounded-full text-xs font-semibold whitespace-nowrap transition-all duration-150 cursor-pointer
              ${
                isActive
                  ? 'bg-[#FAF4F0] text-[#8e4d2b] border border-[#F2DACB] font-bold shadow-2xs'
                  : 'bg-white/75 hover:bg-white text-[#53433c] border border-gray-200/80'
              }
            `}
          >
            {filter}
          </button>
        );
      })}
    </div>
  );
};
