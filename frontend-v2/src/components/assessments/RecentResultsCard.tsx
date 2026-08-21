import React from 'react';

interface RecentResult {
  id: string;
  topic: string;
  timeAgo: string;
  score: number;
  improvement: string;
}

const RECENT_RESULTS: RecentResult[] = [
  {
    id: '1',
    topic: 'React Basics',
    timeAgo: '2 days ago',
    score: 65,
    improvement: '+5%',
  },
  {
    id: '2',
    topic: 'Git Flow',
    timeAgo: '1 week ago',
    score: 88,
    improvement: '+12%',
  },
];

interface RecentResultsCardProps {
  onViewHistory?: () => void;
}

export const RecentResultsCard: React.FC<RecentResultsCardProps> = ({
  onViewHistory,
}) => {
  return (
    <section
      aria-label="Recent Assessment Results"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-5 sm:p-6 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-4"
    >
      <h3 className="text-base sm:text-lg font-extrabold text-[#0f1b32] tracking-tight">
        Recent Results
      </h3>

      <div className="space-y-3">
        {RECENT_RESULTS.map((res) => (
          <div
            key={res.id}
            className="flex items-center justify-between p-3 rounded-2xl bg-white/60 border border-gray-100/80"
          >
            <div>
              <h4 className="text-xs sm:text-sm font-bold text-[#0f1b32]">
                {res.topic}
              </h4>
              <span className="text-[11px] text-gray-400 font-normal">
                {res.timeAgo}
              </span>
            </div>

            <div className="text-right">
              <span className="text-sm sm:text-base font-extrabold text-[#8e4d2b] block leading-none">
                {res.score}%
              </span>
              <span className="text-[10px] font-bold text-emerald-600">
                {res.improvement}
              </span>
            </div>
          </div>
        ))}
      </div>

      <div className="pt-1 text-center">
        <button
          type="button"
          onClick={onViewHistory}
          className="text-xs font-bold text-[#8e4d2b] hover:text-[#783e20] hover:underline transition-colors cursor-pointer"
        >
          View History
        </button>
      </div>
    </section>
  );
};
