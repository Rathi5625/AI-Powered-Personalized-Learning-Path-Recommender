import React from 'react';
import { Menu } from 'lucide-react';

interface SkillsPageHeaderProps {
  onToggleMobileMenu?: () => void;
}

export const SkillsPageHeader: React.FC<SkillsPageHeaderProps> = ({
  onToggleMobileMenu,
}) => {
  return (
    <div className="text-left space-y-2 mb-6">
      <div className="flex items-center gap-3">
        <button
          type="button"
          onClick={onToggleMobileMenu}
          aria-label="Open Navigation Menu"
          className="lg:hidden p-2 rounded-xl text-[#0f1b32] hover:bg-black/5 transition-colors cursor-pointer"
        >
          <Menu className="w-5 h-5" />
        </button>
        <h1 className="text-3xl sm:text-4xl lg:text-[40px] font-extrabold text-[#0f1b32] tracking-tight leading-none">
          Your Skills
        </h1>
      </div>
      <p className="text-xs sm:text-sm md:text-base text-[#53433c] font-normal max-w-3xl leading-relaxed">
        Analyze your current skill proficiencies and identify critical gaps to reach your career
        target: <strong className="font-bold text-[#0f1b32]">Software Engineer</strong>.
      </p>
    </div>
  );
};
