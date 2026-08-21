import React from 'react';
import { Brain, LogOut } from 'lucide-react';

interface AssessmentHeaderProps {
  title?: string;
  onExitAssessment: () => void;
}

export const AssessmentHeader: React.FC<AssessmentHeaderProps> = ({
  title = 'DSA Skill Assessment',
  onExitAssessment,
}) => {
  return (
    <header className="fixed top-4 sm:top-6 left-4 right-4 sm:left-8 sm:right-8 max-w-5xl mx-auto z-40 select-none">
      <div className="h-14 sm:h-16 px-5 sm:px-8 rounded-full bg-white/80 backdrop-blur-2xl border border-white/90 shadow-[0_8px_32px_rgba(23,35,58,0.06)] flex items-center justify-between gap-4">
        {/* Left: LearnAI Logo */}
        <div className="flex items-center gap-2 shrink-0">
          <div className="w-8 h-8 rounded-full bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#8e4d2b]">
            <Brain className="w-4 h-4 text-[#8e4d2b]" />
          </div>
          <span className="font-extrabold text-base sm:text-lg text-[#8e4d2b] tracking-tight">
            LearnAI
          </span>
        </div>

        {/* Center: Title (hidden on tiny screens, prominent on desktop) */}
        <div className="hidden sm:block text-center truncate px-2">
          <span className="text-xs sm:text-sm font-extrabold text-[#0f1b32] tracking-tight">
            {title}
          </span>
        </div>

        {/* Right: Exit Assessment */}
        <button
          type="button"
          onClick={onExitAssessment}
          className="flex items-center gap-1.5 text-xs font-semibold text-[#53433c] hover:text-[#ba1a1a] transition-colors cursor-pointer shrink-0"
        >
          <span>Exit Assessment</span>
          <LogOut className="w-3.5 h-3.5" />
        </button>
      </div>
    </header>
  );
};
