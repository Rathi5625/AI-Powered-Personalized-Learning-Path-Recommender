import React from 'react';
import { motion } from 'framer-motion';
import { Check, type LucideIcon } from 'lucide-react';

export interface LearningFormatOption {
  id: string;
  title: string;
  description: string;
  icon: LucideIcon;
  shortLabel: string;
}

interface LearningFormatCardProps {
  option: LearningFormatOption;
  isSelected: boolean;
  onToggle: (id: string) => void;
}

export const LearningFormatCard: React.FC<LearningFormatCardProps> = ({
  option,
  isSelected,
  onToggle,
}) => {
  const Icon = option.icon;

  return (
    <motion.div
      whileHover={{ scale: 1.012 }}
      whileTap={{ scale: 0.988 }}
      onClick={() => onToggle(option.id)}
      className={`
        relative p-4 sm:p-5 rounded-2xl cursor-pointer transition-all duration-200 border text-left select-none flex items-start gap-3.5
        ${
          isSelected
            ? 'bg-[#FAF4F0] border-[#8B4D2B] shadow-[0_4px_16px_rgba(139,77,43,0.08)] ring-1 ring-[#8B4D2B]/15'
            : 'bg-white/60 hover:bg-white/90 border-transparent hover:border-gray-200/80 shadow-xs'
        }
      `}
    >
      {/* Icon container */}
      <div
        className={`
          w-10 h-10 rounded-xl flex items-center justify-center shrink-0 transition-colors
          ${
            isSelected
              ? 'bg-[#F2E8E1] text-[#8B4D2B]'
              : 'bg-[#F0EEFF] text-[#6B65E0]'
          }
        `}
      >
        <Icon className="w-5 h-5 stroke-[2]" />
      </div>

      {/* Content */}
      <div className="flex-1 min-w-0 pr-5">
        <h3
          className={`text-sm sm:text-base font-bold tracking-tight leading-tight transition-colors ${
            isSelected ? 'text-[#8B4D2B]' : 'text-[#1A1F36]'
          }`}
        >
          {option.title}
        </h3>
        <p className="text-xs sm:text-[13px] text-gray-500 mt-1 leading-snug font-normal">
          {option.description}
        </p>
      </div>

      {/* Selected Check Badge (top-right) */}
      {isSelected && (
        <motion.div
          initial={{ scale: 0.5, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          transition={{ type: 'spring', stiffness: 400, damping: 22 }}
          className="absolute top-4 right-4 w-5 h-5 rounded-full bg-[#8B4D2B] text-white flex items-center justify-center shadow-xs"
        >
          <Check className="w-3.5 h-3.5 stroke-[2.5]" />
        </motion.div>
      )}
    </motion.div>
  );
};
