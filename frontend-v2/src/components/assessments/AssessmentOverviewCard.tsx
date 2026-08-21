import React from 'react';
import { BarChart3 } from 'lucide-react';

export const AssessmentOverviewCard: React.FC = () => {
  return (
    <section
      aria-label="Assessment Overview"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left flex flex-col justify-between h-full"
    >
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-xl bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#8e4d2b]">
            <BarChart3 className="w-4 h-4 text-[#8e4d2b]" />
          </div>
          <h2 className="text-lg sm:text-xl font-extrabold text-[#0f1b32] tracking-tight">
            Overview
          </h2>
        </div>

        {/* 4 Stats Grid */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 sm:gap-6 pt-1">
          <div>
            <span className="text-xs text-gray-500 font-medium block mb-1">
              Overall Knowledge
            </span>
            <span className="text-2xl sm:text-3xl font-extrabold text-[#0f1b32] tracking-tight">
              68%
            </span>
          </div>

          <div>
            <span className="text-xs text-gray-500 font-medium block mb-1">
              Assessments Completed
            </span>
            <span className="text-2xl sm:text-3xl font-extrabold text-[#0f1b32] tracking-tight">
              8
            </span>
          </div>

          <div>
            <span className="text-xs text-gray-500 font-medium block mb-1">
              Skills Assessed
            </span>
            <span className="text-2xl sm:text-3xl font-extrabold text-[#0f1b32] tracking-tight">
              12
            </span>
          </div>

          <div>
            <span className="text-xs text-gray-500 font-medium block mb-1">
              Last Assessment
            </span>
            <span className="text-xl sm:text-2xl font-extrabold text-[#0f1b32] tracking-tight">
              2 days ago
            </span>
          </div>
        </div>
      </div>
    </section>
  );
};
