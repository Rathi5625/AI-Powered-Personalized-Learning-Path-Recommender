import React from 'react';
import { motion } from 'framer-motion';

interface DashboardStatCardProps {
  icon: React.ElementType;
  iconBg: string;
  iconColor: string;
  label: string;
  value: string;
  growthBadge?: string;
}

export const DashboardStatCard: React.FC<DashboardStatCardProps> = ({
  icon: Icon,
  iconBg,
  iconColor,
  label,
  value,
  growthBadge,
}) => {
  return (
    <motion.div
      whileHover={{ y: -2 }}
      className="bg-white/80 backdrop-blur-xl border border-white/90 rounded-2xl sm:rounded-[22px] p-4 sm:p-5 shadow-[0_12px_32px_rgba(15,27,50,0.03)] flex items-center gap-3.5 sm:gap-4 text-left select-none"
    >
      {/* Icon Container */}
      <div
        className={`w-11 h-11 sm:w-12 sm:h-12 rounded-2xl flex items-center justify-center shrink-0 shadow-2xs ${iconBg} ${iconColor}`}
      >
        <Icon className="w-5 h-5 sm:w-5.5 sm:h-5.5" />
      </div>

      {/* Content */}
      <div className="min-w-0 flex-1">
        <span className="text-[11px] sm:text-xs text-gray-500 font-normal block truncate">
          {label}
        </span>
        <div className="flex items-center gap-2 mt-0.5">
          <span className="text-lg sm:text-2xl font-extrabold text-[#0f1b32] tracking-tight">
            {value}
          </span>
          {growthBadge && (
            <span className="inline-flex items-center px-1.5 py-0.5 rounded-md bg-emerald-50 border border-emerald-200/60 text-[10px] sm:text-[11px] font-bold text-emerald-700">
              {growthBadge}
            </span>
          )}
        </div>
      </div>
    </motion.div>
  );
};
