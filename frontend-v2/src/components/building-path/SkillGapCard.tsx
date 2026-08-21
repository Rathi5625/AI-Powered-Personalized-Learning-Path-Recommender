import React from 'react';
import { motion } from 'framer-motion';
import { Lightbulb } from 'lucide-react';

interface SkillGapCardProps {
  category?: string;
  description?: string;
}

export const SkillGapCard: React.FC<SkillGapCardProps> = ({
  category = 'SYSTEM DESIGN',
  description = 'Your target role requires stronger system design knowledge. We are adding foundational modules to your path.',
}) => {
  return (
    <motion.div
      initial={{ opacity: 0, x: 20 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.5, delay: 0.25 }}
      className="bg-white/75 backdrop-blur-xl border border-white/90 border-l-4 border-l-[#CC7D52] rounded-2xl sm:rounded-3xl p-5 sm:p-6 shadow-[0_16px_40px_rgba(26,31,54,0.05)] text-left w-full"
    >
      {/* Header */}
      <div className="flex items-center gap-2 mb-2">
        <Lightbulb className="w-4 h-4 text-[#CC7D52]" />
        <h3 className="text-sm sm:text-base font-bold text-[#1A1F36] tracking-tight">
          Skill Gap Identified
        </h3>
      </div>

      {/* Category */}
      <span className="text-[10px] font-bold text-[#CC7D52] uppercase tracking-wider block mb-2">
        {category}
      </span>

      {/* Description */}
      <p className="text-xs sm:text-sm text-gray-600 leading-relaxed font-normal">
        {description}
      </p>
    </motion.div>
  );
};
