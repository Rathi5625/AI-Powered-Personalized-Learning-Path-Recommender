import React from 'react';

export const AssessmentsPageHeader: React.FC = () => {
  return (
    <div className="text-left space-y-1.5 mb-6">
      <h1 className="text-3xl sm:text-4xl lg:text-[40px] font-extrabold text-[#0f1b32] tracking-tight leading-tight">
        Assessments
      </h1>
      <p className="text-xs sm:text-sm md:text-base text-[#53433c] font-normal max-w-3xl leading-relaxed">
        Measure your knowledge and help LearnAI understand what you should learn next.
      </p>
    </div>
  );
};
