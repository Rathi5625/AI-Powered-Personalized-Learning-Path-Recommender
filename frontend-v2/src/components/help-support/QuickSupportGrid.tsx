import React from 'react';
import { GraduationCap, Bot, UserCheck, Wrench, ArrowRight } from 'lucide-react';

interface QuickSupportGridProps {
  onSelectCategory: (category: string) => void;
}

export const QuickSupportGrid: React.FC<QuickSupportGridProps> = ({
  onSelectCategory,
}) => {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 gap-5 select-none text-left">
      {/* Card 1: Learning Help */}
      <div
        onClick={() => onSelectCategory('learning')}
        className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] hover:shadow-md transition-all cursor-pointer flex flex-col justify-between space-y-4 group"
      >
        <div className="space-y-3">
          <div className="w-10 h-10 rounded-2xl bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#8e4d2b] shadow-2xs">
            <GraduationCap className="w-5 h-5 text-[#8e4d2b]" />
          </div>
          <h3 className="text-base font-extrabold text-[#0f1b32] tracking-tight">
            Learning Help
          </h3>
          <p className="text-xs text-[#53433c] font-normal leading-relaxed">
            Assistance with courses, learning paths, and curriculum understanding.
          </p>
        </div>

        <div className="inline-flex items-center gap-1.5 text-xs font-bold text-[#8e4d2b] group-hover:gap-2 transition-all">
          <span>Explore Learning Help</span>
          <ArrowRight className="w-3.5 h-3.5" />
        </div>
      </div>

      {/* Card 2: AI Mentor */}
      <div
        onClick={() => onSelectCategory('ai-mentor')}
        className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] hover:shadow-md transition-all cursor-pointer flex flex-col justify-between space-y-4 group"
      >
        <div className="space-y-3">
          <div className="w-10 h-10 rounded-2xl bg-[#e9edff] border border-[#d8e2ff] flex items-center justify-center text-[#615a7a] shadow-2xs">
            <Bot className="w-5 h-5 text-[#615a7a]" />
          </div>
          <h3 className="text-base font-extrabold text-[#0f1b32] tracking-tight">
            AI Mentor
          </h3>
          <p className="text-xs text-[#53433c] font-normal leading-relaxed">
            Guidance from your personalized AI, ready to answer subject-matter questions.
          </p>
        </div>

        <div className="inline-flex items-center gap-1.5 text-xs font-bold text-[#615a7a] group-hover:gap-2 transition-all">
          <span>Ask AI Mentor</span>
          <ArrowRight className="w-3.5 h-3.5" />
        </div>
      </div>

      {/* Card 3: Account & Security */}
      <div
        onClick={() => onSelectCategory('account')}
        className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] hover:shadow-md transition-all cursor-pointer flex flex-col justify-between space-y-4 group"
      >
        <div className="space-y-3">
          <div className="w-10 h-10 rounded-2xl bg-[#e9edff] border border-[#d8e2ff] flex items-center justify-center text-[#0f1b32] shadow-2xs">
            <UserCheck className="w-5 h-5 text-[#0f1b32]" />
          </div>
          <h3 className="text-base font-extrabold text-[#0f1b32] tracking-tight">
            Account &amp; Security
          </h3>
          <p className="text-xs text-[#53433c] font-normal leading-relaxed">
            Manage your profile, billing, passwords, and data privacy settings.
          </p>
        </div>

        <div className="inline-flex items-center gap-1.5 text-xs font-bold text-[#0f1b32] group-hover:gap-2 transition-all">
          <span>Account Help</span>
          <ArrowRight className="w-3.5 h-3.5" />
        </div>
      </div>

      {/* Card 4: Technical Support */}
      <div
        onClick={() => onSelectCategory('technical')}
        className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] hover:shadow-md transition-all cursor-pointer flex flex-col justify-between space-y-4 group"
      >
        <div className="space-y-3">
          <div className="w-10 h-10 rounded-2xl bg-[#FAF4F0] border border-red-100 flex items-center justify-center text-[#ba1a1a] shadow-2xs">
            <Wrench className="w-5 h-5 text-[#ba1a1a]" />
          </div>
          <h3 className="text-base font-extrabold text-[#0f1b32] tracking-tight">
            Technical Support
          </h3>
          <p className="text-xs text-[#53433c] font-normal leading-relaxed">
            Report bugs, platform issues, or connectivity problems.
          </p>
        </div>

        <div className="inline-flex items-center gap-1.5 text-xs font-bold text-[#ba1a1a] group-hover:gap-2 transition-all">
          <span>Technical Support</span>
          <ArrowRight className="w-3.5 h-3.5" />
        </div>
      </div>
    </div>
  );
};
