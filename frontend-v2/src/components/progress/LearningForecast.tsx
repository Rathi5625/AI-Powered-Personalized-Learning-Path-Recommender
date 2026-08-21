import React from 'react';
import { Activity } from 'lucide-react';
import { motion } from 'framer-motion';

interface LearningForecastProps {
  monthsRemaining?: number;
  progressPercent?: number;
}

export const LearningForecast: React.FC<LearningForecastProps> = ({
  monthsRemaining = 5.4,
  progressPercent = 65,
}) => {
  return (
    <section
      aria-label="Learning Forecast"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-5 sm:p-6 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-3"
    >
      {/* Header */}
      <div className="flex items-center gap-2">
        <Activity className="w-4 h-4 text-[#615a7a]" />
        <h3 className="text-sm sm:text-base font-extrabold text-[#0f1b32] tracking-tight">
          Learning Forecast
        </h3>
      </div>

      {/* Big Value */}
      <div className="flex items-baseline gap-1.5">
        <span className="text-2xl sm:text-3xl font-extrabold text-[#0f1b32]">
          {monthsRemaining}
        </span>
        <span className="text-xs text-gray-500 font-medium">months remaining</span>
      </div>

      {/* Progress Bar & Status */}
      <div className="space-y-1.5 pt-1">
        <div className="w-full h-2 bg-gray-100 rounded-full overflow-hidden">
          <motion.div
            initial={{ width: 0 }}
            animate={{ width: `${progressPercent}%` }}
            transition={{ duration: 1, ease: 'easeOut' }}
            className="h-full bg-[#615a7a] rounded-full"
          />
        </div>

        <div className="flex items-center justify-end">
          <span className="text-[10px] font-bold text-[#615a7a] uppercase tracking-wider">
            ON TRACK
          </span>
        </div>
      </div>
    </section>
  );
};
