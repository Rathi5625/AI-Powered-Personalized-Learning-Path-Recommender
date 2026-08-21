import React from 'react';
import { Link } from 'react-router-dom';
import { CheckCircle2, Play, Sparkles, BookOpen } from 'lucide-react';
import { CourseEnrollment } from '../../api/types';

interface TodaysLearningCardProps {
  enrolledCourses?: CourseEnrollment[];
  onStartActivity?: (activityName: string) => void;
}

export const TodaysLearningCard: React.FC<TodaysLearningCardProps> = ({
  enrolledCourses = [],
  onStartActivity,
}) => {
  const hasCourses = enrolledCourses && enrolledCourses.length > 0;
  const completed = enrolledCourses.filter((c) => c.status === 'COMPLETED').length;
  const total = enrolledCourses.length;
  const pct = total > 0 ? Math.round((completed / total) * 100) : 0;

  return (
    <div className="bg-white/80 backdrop-blur-xl border border-white/90 rounded-2xl sm:rounded-[24px] p-5 sm:p-6 shadow-[0_16px_40px_rgba(15,27,50,0.04)] text-left flex flex-col justify-between h-full">
      <div>
        {/* Header */}
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-base sm:text-lg font-extrabold text-[#0f1b32] tracking-tight">
            Today&apos;s Learning
          </h3>
          {hasCourses && (
            <span className="px-2.5 py-0.5 rounded-full bg-[#FAF4F0] border border-[#F2DACB] text-[10px] sm:text-[11px] font-bold text-[#8e4d2b]">
              {pct}% Done
            </span>
          )}
        </div>

        {!hasCourses ? (
          <div className="py-6 px-3 text-center space-y-3 bg-[#FAF4F0]/60 rounded-2xl border border-[#F2DACB]/60">
            <BookOpen className="w-8 h-8 text-[#8e4d2b] mx-auto opacity-70" />
            <div>
              <p className="text-xs font-bold text-[#0f1b32]">Curriculum Ready to Generate</p>
              <p className="text-[11px] text-gray-500 mt-0.5">
                Take your diagnostic assessment to generate your personalized schedule.
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
          <div className="space-y-2.5">
            {enrolledCourses.slice(0, 3).map((course) => {
              const isComp = course.status === 'COMPLETED';
              return (
                <div
                  key={course.id}
                  onClick={() => onStartActivity?.(course.courseTitle)}
                  className={`flex items-center justify-between p-3 rounded-xl sm:rounded-2xl border transition-all cursor-pointer select-none group ${
                    isComp
                      ? 'bg-gray-50/70 border-gray-100/90'
                      : 'bg-white border-[#8e4d2b]/40 border-l-4 border-l-[#8e4d2b] shadow-xs hover:shadow-sm'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <div
                      className={`w-8 h-8 rounded-xl flex items-center justify-center shrink-0 ${
                        isComp ? 'bg-emerald-50 text-emerald-600' : 'bg-[#FAF4F0] text-[#8e4d2b]'
                      }`}
                    >
                      {isComp ? <CheckCircle2 className="w-4 h-4" /> : <Play className="w-4 h-4 fill-[#8e4d2b]" />}
                    </div>
                    <div>
                      <span className="text-xs sm:text-sm font-bold text-[#0f1b32] block">
                        {course.courseTitle}
                      </span>
                      <span
                        className={`text-[11px] font-medium block ${
                          isComp ? 'text-gray-400' : 'text-[#8e4d2b] font-semibold'
                        }`}
                      >
                        {isComp ? 'Completed' : `${course.progressPercentage}% • In Progress`}
                      </span>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Footer */}
      {hasCourses && (
        <div className="pt-3.5 mt-4 border-t border-gray-100 text-left">
          <span className="text-[11px] font-medium text-gray-400">
            {completed} of {total} activities completed
          </span>
        </div>
      )}
    </div>
  );
};
