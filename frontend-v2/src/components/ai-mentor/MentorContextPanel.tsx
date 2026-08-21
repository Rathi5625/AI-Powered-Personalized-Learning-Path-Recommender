import React, { useState, useEffect } from 'react';
import { BrainCircuit } from 'lucide-react';
import { motion } from 'framer-motion';

import api from '../../api/client';
import { LearnerMasterySummary, DashboardAggregated } from '../../api/types';

interface MentorContextPanelProps {
  careerPath?: string;
  currentTopic?: string;
  skillLevel?: number;
}

export const MentorContextPanel: React.FC<MentorContextPanelProps> = ({
  careerPath: propCareer,
  currentTopic: propTopic,
  skillLevel: propSkill,
}) => {
  const [mastery, setMastery] = useState<LearnerMasterySummary | null>(null);
  const [dash, setDash] = useState<DashboardAggregated | null>(null);

  useEffect(() => {
    Promise.all([
      api.getMasterySummary().catch(() => null),
      api.getDashboardData().catch(() => null),
    ]).then(([masteryData, dashData]) => {
      if (masteryData) setMastery(masteryData);
      if (dashData) setDash(dashData);
    });
  }, []);

  const career = propCareer || dash?.targetCareer || 'Software Engineer';
  const topic = propTopic || (mastery?.weakSkills && mastery.weakSkills.length > 0 ? mastery.weakSkills[0] : 'Binary Search');
  const level = propSkill !== undefined ? propSkill : (mastery ? Math.round(mastery.overallMasteryPercentage) : 61);

  return (
    <section
      aria-label="Learner Context"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-5 sm:p-6 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-4"
    >
      {/* Header */}
      <div className="flex items-center gap-2">
        <div className="w-7 h-7 rounded-xl bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#8e4d2b]">
          <BrainCircuit className="w-3.5 h-3.5 text-[#8e4d2b]" />
        </div>
        <h3 className="text-sm sm:text-base font-extrabold text-[#0f1b32] tracking-tight">
          Your Knowledge Context
        </h3>
      </div>

      <div className="space-y-3.5 text-xs">
        {/* Career Path */}
        <div>
          <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider block">
            TARGET CAREER
          </span>
          <span className="font-extrabold text-[#0f1b32] text-sm mt-0.5 block">
            {career}
          </span>
        </div>

        {/* Current Topic */}
        <div>
          <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider block">
            CURRENT FOCUS TOPIC
          </span>
          <div className="flex items-center justify-between mt-0.5">
            <span className="font-extrabold text-[#0f1b32] text-sm">{topic}</span>
            <span className="px-2.5 py-0.5 rounded-full bg-[#e1d8fe]/60 border border-[#c7b8fe] text-[#615a7a] text-[10px] font-bold">
              BKT Active
            </span>
          </div>
        </div>

        {/* BKT Overall Mastery */}
        <div className="space-y-1.5 pt-1">
          <div className="flex items-center justify-between text-[11px] font-bold">
            <span className="text-gray-400 uppercase tracking-wider">OVERALL MASTERY PROBABILITY</span>
            <span className="text-[#8e4d2b]">{level}%</span>
          </div>
          <div className="w-full h-2 rounded-full bg-gray-100 overflow-hidden">
            <motion.div
              initial={{ width: 0 }}
              animate={{ width: `${Math.min(100, Math.max(5, level))}%` }}
              transition={{ duration: 0.8, ease: 'easeOut' }}
              className="h-full rounded-full bg-linear-to-r from-[#e7bba4] to-[#8e4d2b]"
            />
          </div>
        </div>
      </div>
    </section>
  );
};
