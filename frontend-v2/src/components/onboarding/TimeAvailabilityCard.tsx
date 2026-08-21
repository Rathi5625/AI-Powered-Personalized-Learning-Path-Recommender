import React from 'react';
import { motion } from 'framer-motion';
import { Check } from 'lucide-react';

export interface TimeOption {
  id: string;
  hours: string;
  description: string;
}

interface TimeAvailabilityCardProps {
  option: TimeOption;
  isSelected: boolean;
  onSelect: (id: string) => void;
}

export const TimeAvailabilityCard: React.FC<TimeAvailabilityCardProps> = ({
  option,
  isSelected,
  onSelect,
}) => {
  return (
    <motion.div
      whileHover={{ scale: 1.012 }}
      whileTap={{ scale: 0.988 }}
      onClick={() => onSelect(option.id)}
      className={`
        relative p-4 sm:p-5 rounded-2xl cursor-pointer transition-all duration-200 border text-left select-none flex flex-col justify-between
        ${
          isSelected
            ? 'bg-[#FAF4F0] border-[#CC7D52] shadow-[0_4px_16px_rgba(204,125,82,0.08)] ring-1 ring-[#CC7D52]/15'
            : 'bg-white/60 hover:bg-white/90 border-gray-100 hover:border-gray-200/80 shadow-xs'
        }
      `}
    >
      {/* Top row: Hours label and radio/check indicator */}
      <div className="flex items-center justify-between mb-1.5">
        <h3 className="text-sm sm:text-base font-bold text-[#1A1F36] tracking-tight">
          {option.hours}
        </h3>

        {isSelected ? (
          <motion.div
            initial={{ scale: 0.5, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ type: 'spring', stiffness: 400, damping: 22 }}
            className="w-5 h-5 rounded-full bg-[#CC7D52] text-white flex items-center justify-center shadow-xs"
          >
            <Check className="w-3.5 h-3.5 stroke-[2.5]" />
          </motion.div>
        ) : (
          <div className="w-5 h-5 rounded-full border-2 border-gray-300 bg-white/40" />
        )}
      </div>

      {/* Description */}
      <p className="text-xs sm:text-[13px] text-gray-500 leading-snug font-normal">
        {option.description}
      </p>
    </motion.div>
  );
};
