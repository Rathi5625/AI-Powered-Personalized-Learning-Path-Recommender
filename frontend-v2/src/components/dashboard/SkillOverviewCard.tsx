import React from 'react';
import { Link } from 'react-router-dom';
import { Sparkles, Award } from 'lucide-react';
import { UserSkill } from '../../api/types';

interface SkillOverviewCardProps {
  skills?: UserSkill[];
}

export const SkillOverviewCard: React.FC<SkillOverviewCardProps> = ({ skills = [] }) => {
  const hasSkills = skills && skills.length > 0;

  return (
    <div className="bg-white/80 backdrop-blur-xl border border-white/90 rounded-2xl sm:rounded-[24px] p-5 sm:p-6 shadow-[0_16px_40px_rgba(15,27,50,0.04)] text-left">
      {/* Header */}
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-base sm:text-lg font-extrabold text-[#0f1b32] tracking-tight">
          Skill Overview
        </h3>
        <Link
          to="/skills"
          className="text-xs font-bold text-[#8e4d2b] hover:text-[#783e20] transition-colors"
        >
          View All
        </Link>
      </div>

      {!hasSkills ? (
        <div className="py-6 px-3 text-center space-y-3 bg-[#FAF4F0]/60 rounded-2xl border border-[#F2DACB]/60">
          <Award className="w-8 h-8 text-[#8e4d2b] mx-auto opacity-70" />
          <div>
            <p className="text-xs font-bold text-[#0f1b32]">Not Assessed Yet</p>
            <p className="text-[11px] text-gray-500 mt-0.5">
              Take a diagnostic assessment to benchmark your skills.
            </p>
          </div>
          <Link
            to="/assessments"
            className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-[#8e4d2b] text-white text-[11px] font-bold hover:bg-[#783e20] transition-colors shadow-2xs"
          >
            <Sparkles className="w-3 h-3 text-[#ffdbcb]" />
            <span>Start Assessment</span>
          </Link>
        </div>
      ) : (
        <div className="space-y-3.5">
          {skills.slice(0, 5).map((skill, idx) => {
            const colors = ['bg-[#8e4d2b]', 'bg-[#d98b63]', 'bg-[#615a7a]', 'bg-[#53433c]', 'bg-[#86736b]'];
            const color = colors[idx % colors.length];
            const pct = skill.confidence ? Math.round(skill.confidence * 100) : 0;
            return (
              <div key={skill.id || skill.skillName} className="space-y-1.5">
                <div className="flex items-center justify-between text-xs">
                  <span className="font-semibold text-[#0f1b32]">{skill.skillName}</span>
                  <span className="text-gray-500 font-medium">
                    {skill.isVerified ? `${pct}%` : `${skill.proficiencyLevel || 'Declared'}`}
                  </span>
                </div>
                <div className="w-full h-2 bg-[#EAE8FF] rounded-full overflow-hidden">
                  <div
                    className={`h-full ${color} rounded-full transition-all duration-500`}
                    style={{ width: `${Math.max(10, pct)}%` }}
                  />
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
