import React from 'react';

interface ProgressMetric {
  label: string;
  percentage: number;
  barColor: string;
}

const METRICS: ProgressMetric[] = [
  { label: 'Overall Track', percentage: 42, barColor: 'bg-[#8e4d2b]' },
  { label: 'Skills Acquired', percentage: 61, barColor: 'bg-[#615a7a]' },
  { label: 'Course Completion', percentage: 38, barColor: 'bg-[#615a7a]' },
];

export const ProgressOverviewCard: React.FC = () => {
  return (
    <div className="bg-white/80 backdrop-blur-xl border border-white/90 rounded-2xl sm:rounded-3xl p-5 sm:p-6 shadow-[0_16px_40px_rgba(15,27,50,0.04)] text-left">
      <h4 className="text-xs sm:text-sm font-bold text-[#0f1b32] tracking-tight mb-4">
        Your Progress
      </h4>

      <div className="space-y-4">
        {METRICS.map((metric) => (
          <div key={metric.label} className="space-y-1.5">
            <div className="flex items-center justify-between text-xs">
              <span className="text-gray-600 font-medium">{metric.label}</span>
              <span className="font-bold text-[#0f1b32]">{metric.percentage}%</span>
            </div>
            <div className="w-full h-2 bg-[#EAE8FF] rounded-full overflow-hidden">
              <div
                className={`h-full ${metric.barColor} rounded-full transition-all duration-500`}
                style={{ width: `${metric.percentage}%` }}
              />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
