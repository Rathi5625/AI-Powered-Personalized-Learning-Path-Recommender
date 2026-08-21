import React from 'react';
import { CheckCircle2, Clock, Award, Terminal } from 'lucide-react';
import { ProgressAnalytics } from '../../api/types';

interface ProgressStatCardsRowProps {
  analytics?: ProgressAnalytics | null;
}

export const ProgressStatCardsRow: React.FC<ProgressStatCardsRowProps> = ({ analytics }) => {
  const totalEnrolled = analytics?.totalEnrolledCourses ?? analytics?.recentCourses?.length ?? 0;
  const completedPct = analytics && totalEnrolled > 0
    ? `${Math.round((analytics.completedCoursesCount / totalEnrolled) * 100)}%`
    : '0%';

  const hoursText = analytics ? `${analytics.totalLearningHours} hrs` : '0.0 hrs';
  const skillsCount = analytics ? `${analytics.skillsMasteredCount}` : '0';
  const assessmentsCount = analytics ? `${analytics.totalAssessmentsTaken}` : '0';

  const stats = [
    {
      id: 'completed',
      label: 'Learning Completed',
      value: completedPct,
      icon: CheckCircle2,
    },
    {
      id: 'hours',
      label: 'Learning Hours',
      value: hoursText,
      icon: Clock,
    },
    {
      id: 'skills',
      label: 'Skills Mastered',
      value: skillsCount,
      icon: Award,
    },
    {
      id: 'assessments',
      label: 'Assessments Taken',
      value: assessmentsCount,
      icon: Terminal,
    },
  ];

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-5 select-none">
      {stats.map((stat) => {
        const Icon = stat.icon;

        return (
          <div
            key={stat.id}
            className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-5 sm:p-6 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-3"
          >
            {/* Header: Icon + Label */}
            <div className="flex items-center gap-2 text-xs text-gray-500 font-semibold">
              <Icon className="w-4 h-4 text-[#8e4d2b]" />
              <span>{stat.label}</span>
            </div>

            {/* Value */}
            <p className="text-2xl sm:text-3xl font-extrabold text-[#0f1b32] tracking-tight">
              {stat.value}
            </p>
          </div>
        );
      })}
    </div>
  );
};
