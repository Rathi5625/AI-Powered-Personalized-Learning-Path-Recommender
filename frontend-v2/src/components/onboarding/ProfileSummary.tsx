import React from 'react';
import { FileText } from 'lucide-react';

interface ProfileSummaryProps {
  targetRole: string;
  experience?: string;
  skills?: string[];
  commitment?: string;
  learningStyle?: string;
}

export const ProfileSummary: React.FC<ProfileSummaryProps> = ({
  targetRole,
  experience = 'Intermediate',
  skills = ['Java', 'DSA', 'React', 'SQL'],
  commitment = '10 hours/week',
  learningStyle = 'Projects · Practice · Video',
}) => {
  return (
    <div className="bg-white/85 backdrop-blur-xl border border-white/90 rounded-2xl sm:rounded-3xl p-5 sm:p-6 shadow-[0_16px_40px_rgba(26,31,54,0.05)] text-left">
      {/* Header */}
      <div className="flex items-center gap-2.5 mb-5 pb-3.5 border-b border-gray-100/90">
        <div className="w-8 h-8 rounded-xl bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#CC7D52]">
          <FileText className="w-4 h-4" />
        </div>
        <h3 className="text-sm sm:text-base font-bold text-[#1A1F36] tracking-tight">
          Your Profile So Far
        </h3>
      </div>

      {/* Sections */}
      <div className="space-y-4">
        {/* Target Role */}
        <div>
          <span className="text-[10px] font-bold text-gray-400 uppercase tracking-widest block mb-1">
            TARGET ROLE
          </span>
          <p className="text-xs sm:text-sm font-bold text-[#1A1F36]">
            {targetRole || 'Software Engineer'}
          </p>
        </div>

        {/* Experience */}
        <div className="pt-3 border-t border-gray-100/80">
          <span className="text-[10px] font-bold text-gray-400 uppercase tracking-widest block mb-1">
            EXPERIENCE
          </span>
          <p className="text-xs sm:text-sm font-semibold text-[#1A1F36]">
            {experience}
          </p>
        </div>

        {/* Current Skills */}
        <div className="pt-3 border-t border-gray-100/80">
          <span className="text-[10px] font-bold text-gray-400 uppercase tracking-widest block mb-2">
            CURRENT SKILLS
          </span>
          <div className="flex flex-wrap gap-1.5">
            {skills.map((skill) => (
              <span
                key={skill}
                className="px-2.5 py-0.5 rounded-full bg-[#EAE8FF] text-[#1A1F36] text-[11px] font-semibold"
              >
                {skill}
              </span>
            ))}
          </div>
        </div>

        {/* Commitment */}
        <div className="pt-3 border-t border-gray-100/80">
          <span className="text-[10px] font-bold text-gray-400 uppercase tracking-widest block mb-1">
            COMMITMENT
          </span>
          <p className="text-xs sm:text-sm font-semibold text-[#1A1F36]">
            {commitment}
          </p>
        </div>

        {/* Learning Style */}
        <div className="pt-3 border-t border-gray-100/80">
          <span className="text-[10px] font-bold text-gray-400 uppercase tracking-widest block mb-1">
            LEARNING STYLE
          </span>
          <p className="text-xs sm:text-sm font-semibold text-[#1A1F36]">
            {learningStyle}
          </p>
        </div>
      </div>
    </div>
  );
};
