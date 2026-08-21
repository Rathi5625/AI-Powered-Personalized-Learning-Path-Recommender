import React from 'react';

export const AssessmentResultsHeader: React.FC = () => {
  return (
    <div className="text-left space-y-1.5 mb-6">
      <h1 className="text-3xl sm:text-4xl lg:text-[40px] font-extrabold text-[#0f1b32] tracking-tight leading-tight">
        Assessment Results
      </h1>
      <p className="text-xs sm:text-sm md:text-base text-[#53433c] font-normal max-w-3xl leading-relaxed">
        Here&apos;s what LearnAI learned from your latest assessment.
      </p>
    </div>
  );
};
