import React from 'react';
import { motion } from 'framer-motion';
import { Check, type LucideIcon } from 'lucide-react';

export interface ExperienceLevelOption {
  id: 'beginner' | 'intermediate' | 'advanced';
  title: string;
  subtitle: string;
  description: string;
  icon: LucideIcon;
}

interface ExperienceLevelCardProps {
  option: ExperienceLevelOption;
  isSelected: boolean;
  onSelect: (id: 'beginner' | 'intermediate' | 'advanced') => void;
}

export const ExperienceLevelCard: React.FC<ExperienceLevelCardProps> = ({
  option,
  isSelected,
  onSelect,
}) => {
  const Icon = option.icon;

  return (
    <motion.div
      whileHover={{ scale: 1.015 }}
      whileTap={{ scale: 0.985 }}
      onClick={() => onSelect(option.id)}
      className={`
        relative p-5 sm:p-6 rounded-2xl sm:rounded-3xl cursor-pointer transition-all duration-200 border text-left select-none flex flex-col justify-between
        ${
          isSelected
            ? 'bg-[#FAF4F0] border-[#8B4D2B] shadow-[0_8px_24px_rgba(139,77,43,0.08)] ring-1 ring-[#8B4D2B]/15'
            : 'bg-white/60 hover:bg-white/90 border-transparent hover:border-gray-200/80 shadow-xs'
        }
      `}
    >
      {/* Top row: Icon and check badge */}
      <div className="flex items-start justify-between mb-4">
        <div
          className={`
            w-11 h-11 rounded-2xl flex items-center justify-center transition-colors
            ${
              isSelected
                ? 'bg-[#F2E8E1] text-[#8B4D2B]'
                : 'bg-[#F0EEFF] text-[#6B65E0]'
            }
          `}
        >
          <Icon className="w-5 h-5 stroke-[2]" />
        </div>

        {/* Selected Checkmark Badge */}
        {isSelected && (
          <motion.div
            initial={{ scale: 0.5, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ type: 'spring', stiffness: 400, damping: 22 }}
            className="w-5 h-5 rounded-full bg-[#8B4D2B] text-white flex items-center justify-center shadow-xs"
          >
            <Check className="w-3.5 h-3.5 stroke-[2.5]" />
          </motion.div>
        )}
      </div>

      {/* Content */}
      <div className="space-y-1">
        <h3 className="text-base sm:text-lg font-bold text-[#1A1F36] tracking-tight">
          {option.title}
        </h3>
        <p
          className={`text-xs sm:text-sm font-semibold transition-colors ${
            isSelected ? 'text-[#8B4D2B]' : 'text-gray-600'
          }`}
        >
          {option.subtitle}
        </p>
        <p className="text-xs sm:text-[13px] text-gray-500 pt-1 leading-relaxed font-normal">
          {option.description}
        </p>
      </div>
    </motion.div>
  );
};
