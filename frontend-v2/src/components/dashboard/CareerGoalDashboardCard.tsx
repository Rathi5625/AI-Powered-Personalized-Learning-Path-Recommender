import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Calendar, Clock, Code2, ArrowRight } from 'lucide-react';

interface CareerGoalDashboardCardProps {
  role?: string;
  estTime?: string;
  weeklyHours?: string;
  progress?: number;
  onAdjustGoal?: () => void;
}

export const CareerGoalDashboardCard: React.FC<CareerGoalDashboardCardProps> = ({
  role = 'Software Engineer',
  estTime = 'Est. 6 months',
  weeklyHours = '10 hrs/week',
  progress = 42,
  onAdjustGoal,
}) => {
  const navigate = useNavigate();

  return (
    <div className="relative overflow-hidden bg-white/80 backdrop-blur-xl border border-white/90 rounded-2xl sm:rounded-[24px] p-6 sm:p-7 shadow-[0_16px_40px_rgba(15,27,50,0.04)] text-left">
      {/* Subtle Decorative Ambient Gradient on Right */}
      <div className="absolute top-0 right-0 w-64 h-64 bg-gradient-to-br from-[#ffdbcb]/30 to-[#e1d8fe]/20 rounded-full blur-3xl -mr-16 -mt-16 pointer-events-none" />

      {/* Top Badge & Code Icon */}
      <div className="flex items-start justify-between gap-4 mb-3">
        <span className="inline-flex items-center px-3 py-1 rounded-full bg-[#FAF4F0] border border-[#F2DACB] text-[10px] sm:text-[11px] font-bold text-[#8e4d2b] uppercase tracking-wider">
          CURRENT CAREER GOAL
        </span>

        <div className="w-9 h-9 rounded-full bg-white/90 border border-gray-200/80 shadow-2xs flex items-center justify-center text-gray-700">
          <Code2 className="w-4 h-4 text-[#8e4d2b]" />
        </div>
      </div>

      {/* Career Title */}
      <h2 className="text-2xl sm:text-3xl font-extrabold text-[#0f1b32] tracking-tight mb-2">
        {role}
      </h2>

      {/* Metadata (Est Time, Weekly Hours) */}
      <div className="flex flex-wrap items-center gap-4 text-xs text-gray-500 font-medium mb-6">
        <div className="flex items-center gap-1.5">
          <Calendar className="w-3.5 h-3.5 text-gray-400" />
          <span>{estTime}</span>
        </div>
        <div className="flex items-center gap-1.5">
          <Clock className="w-3.5 h-3.5 text-gray-400" />
          <span>{weeklyHours}</span>
        </div>
      </div>

      {/* Progress Section */}
      <div className="space-y-1.5 mb-6">
        <div className="flex items-center justify-between text-xs">
          <span className="text-gray-500 font-medium">Overall Progress</span>
          <span className="font-bold text-[#0f1b32]">{progress}%</span>
        </div>
        <div className="w-full h-2.5 bg-[#EAE8FF] rounded-full overflow-hidden">
          <div
            className="h-full bg-gradient-to-r from-[#d98b63] to-[#8e4d2b] rounded-full transition-all duration-500"
            style={{ width: `${progress}%` }}
          />
        </div>
      </div>

      {/* Action Buttons */}
      <div className="flex flex-wrap items-center gap-3">
        <button
          type="button"
          onClick={() => navigate('/learning-path')}
          className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-[#8e4d2b] hover:bg-[#783e20] active:bg-[#623219] text-white text-xs sm:text-sm font-bold transition-all shadow-xs cursor-pointer"
        >
          <span>View My Learning Path</span>
          <ArrowRight className="w-3.5 h-3.5" />
        </button>

        <button
          type="button"
          onClick={onAdjustGoal}
          className="inline-flex items-center px-4 py-2.5 rounded-xl bg-white hover:bg-gray-50 border border-gray-200/90 text-xs sm:text-sm font-bold text-gray-700 hover:text-[#0f1b32] transition-colors shadow-2xs cursor-pointer"
        >
          <span>Adjust Goal</span>
        </button>
      </div>
    </div>
  );
};
