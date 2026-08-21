import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Lightbulb } from 'lucide-react';

interface PersonalizationInsightProps {
  insightText: string;
}

export const PersonalizationInsight: React.FC<PersonalizationInsightProps> = ({
  insightText,
}) => {
  return (
    <div className="w-full bg-[#F2EFFE]/90 border border-[#E6E1FF] rounded-2xl sm:rounded-3xl p-4 sm:p-5 flex items-center gap-3.5 shadow-xs">
      {/* Left Icon */}
      <div className="w-9 h-9 rounded-xl bg-white/80 border border-[#DDD7FF] flex items-center justify-center shrink-0 text-[#CC7D52] shadow-xs">
        <Lightbulb className="w-4.5 h-4.5" />
      </div>

      {/* Dynamic Insight Content */}
      <div className="flex-1 overflow-hidden">
        <AnimatePresence mode="wait">
          <motion.p
            key={insightText}
            initial={{ opacity: 0, y: 4 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -4 }}
            transition={{ duration: 0.25 }}
            className="text-xs sm:text-sm text-gray-700 leading-relaxed font-normal"
          >
            {insightText}
          </motion.p>
        </AnimatePresence>
      </div>
    </div>
  );
};
