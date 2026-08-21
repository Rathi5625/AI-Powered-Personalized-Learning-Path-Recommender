import React from 'react';
import { motion } from 'framer-motion';
import { CheckCircle2, Sparkles, Circle } from 'lucide-react';

interface AnalysisItem {
  label: string;
  status: 'completed' | 'processing' | 'pending';
}

const ANALYSIS_ITEMS: AnalysisItem[] = [
  { label: 'Career goals', status: 'completed' },
  { label: 'Current skills', status: 'completed' },
  { label: 'Experience level', status: 'processing' },
  { label: 'Learning preferences', status: 'pending' },
  { label: 'Weekly availability', status: 'pending' },
];

export const AnalysisProgressCard: React.FC = () => {
  return (
    <motion.div
      initial={{ opacity: 0, x: -20 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.5, delay: 0.15 }}
      className="bg-white/75 backdrop-blur-xl border border-white/90 rounded-2xl sm:rounded-3xl p-5 sm:p-6 shadow-[0_16px_40px_rgba(26,31,54,0.05)] text-left w-full"
    >
      {/* Title */}
      <h3 className="text-sm sm:text-base font-bold text-[#1A1F36] tracking-tight mb-4 pb-2.5 border-b border-gray-100/80">
        Analysis Progress
      </h3>

      {/* Items List */}
      <ul className="space-y-3">
        {ANALYSIS_ITEMS.map((item, idx) => (
          <li key={idx} className="flex items-center gap-2.5 text-xs sm:text-sm">
            {item.status === 'completed' && (
              <CheckCircle2 className="w-4 h-4 text-[#CC7D52] shrink-0" />
            )}

            {item.status === 'processing' && (
              <motion.div
                animate={{ rotate: [0, 15, -15, 0], scale: [1, 1.1, 1] }}
                transition={{ repeat: Infinity, duration: 2, ease: 'easeInOut' }}
                className="shrink-0"
              >
                <Sparkles className="w-4 h-4 text-[#CC7D52]" />
              </motion.div>
            )}

            {item.status === 'pending' && (
              <Circle className="w-4 h-4 text-gray-300 shrink-0" />
            )}

            <span
              className={`
                transition-colors
                ${
                  item.status === 'completed'
                    ? 'font-medium text-[#1A1F36]'
                    : item.status === 'processing'
                    ? 'font-bold text-[#CC7D52]'
                    : 'text-gray-400 font-normal'
                }
              `}
            >
              {item.label}
            </span>
          </li>
        ))}
      </ul>
    </motion.div>
  );
};
