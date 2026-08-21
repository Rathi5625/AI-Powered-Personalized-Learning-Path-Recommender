import React from 'react';
import { motion } from 'framer-motion';
import { Check, type LucideIcon } from 'lucide-react';

export interface ProfileOption {
  id: string;
  title: string;
  description: string;
  icon: LucideIcon;
  insight: string;
}

interface ProfileOptionCardProps {
  option: ProfileOption;
  isSelected: boolean;
  onSelect: (id: string) => void;
}

export const ProfileOptionCard: React.FC<ProfileOptionCardProps> = ({
  option,
  isSelected,
  onSelect,
}) => {
  const Icon = option.icon;

  return (
    <motion.div
      whileHover={{ scale: 1.012 }}
      whileTap={{ scale: 0.988 }}
      onClick={() => onSelect(option.id)}
      className={`
        relative p-5 sm:p-6 rounded-2xl sm:rounded-3xl cursor-pointer transition-all duration-200 border text-left select-none
        ${
          isSelected
            ? 'bg-[#FAF4F0] border-[#8B4D2B] shadow-[0_8px_24px_rgba(139,77,43,0.08)] ring-1 ring-[#8B4D2B]/20'
            : 'bg-white/60 hover:bg-white/90 border-gray-100 hover:border-gray-200/80 shadow-xs'
        }
      `}
    >
      {/* Top row: Icon and checkmark */}
      <div className="flex items-start justify-between mb-3">
        <div
          className={`
            w-10 h-10 rounded-xl flex items-center justify-center transition-colors
            ${
              isSelected
                ? 'text-[#8B4D2B]'
                : 'text-[#6B65E0]'
            }
          `}
        >
          <Icon className="w-6 h-6 stroke-[2]" />
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

      {/* Title & Description */}
      <div>
        <h3 className="text-base sm:text-lg font-bold text-[#1A1F36] tracking-tight">
          {option.title}
        </h3>
        <p className="text-xs sm:text-sm text-gray-500 mt-1 leading-relaxed font-normal">
          {option.description}
        </p>
      </div>
    </motion.div>
  );
};
