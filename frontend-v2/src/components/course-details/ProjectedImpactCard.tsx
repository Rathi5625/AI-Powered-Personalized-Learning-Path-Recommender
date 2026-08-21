import React from 'react';
import { TrendingUp, ArrowRight } from 'lucide-react';
import { motion } from 'framer-motion';

export const ProjectedImpactCard: React.FC = () => {
  return (
    <div className="rounded-3xl bg-white/70 backdrop-blur-2xl border border-white/80 p-6 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left flex flex-col justify-between h-full">
      <div>
        <div className="flex items-center gap-2 mb-4">
          <TrendingUp className="w-4 h-4 text-[#8e4d2b]" />
          <h3 className="text-sm sm:text-base font-bold text-[#0f1b32] tracking-tight">
            Projected Impact
          </h3>
        </div>

        {/* Metric Row */}
        <div className="flex items-center justify-between text-xs sm:text-sm mb-2">
          <span className="font-semibold text-[#0f1b32]">DSA Proficiency</span>
          <div className="flex items-center gap-1.5 font-bold text-[#0f1b32]">
            <span className="text-gray-500 font-semibold">61%</span>
            <ArrowRight className="w-3.5 h-3.5 text-[#8e4d2b]" />
            <span className="text-[#8e4d2b]">75%</span>
          </div>
        </div>

        {/* Double Segment Progress Bar */}
        <div className="w-full h-3 bg-gray-100/90 rounded-full overflow-hidden flex relative mb-3">
          {/* Current 61% */}
          <motion.div
            initial={{ width: 0 }}
            animate={{ width: '61%' }}
            transition={{ duration: 0.8, ease: 'easeOut' }}
            className="h-full bg-[#8e4d2b] rounded-l-full"
          />

          {/* Projected +14% */}
          <motion.div
            initial={{ width: 0 }}
            animate={{ width: '14%' }}
            transition={{ duration: 0.8, delay: 0.4, ease: 'easeOut' }}
            className="h-full bg-repeating-linear-gradient bg-[#e1d8fe] border-l border-white/60 relative overflow-hidden"
            style={{
              backgroundImage:
                'repeating-linear-gradient(45deg, rgba(97,90,122,0.15) 0, rgba(97,90,122,0.15) 6px, transparent 6px, transparent 12px)',
            }}
          />
        </div>

        {/* Impact Subtext */}
        <p className="text-[11px] sm:text-xs text-gray-500 font-normal leading-relaxed">
          Completion adds <strong className="font-bold text-[#8e4d2b]">+14%</strong> to overall
          competency.
        </p>
      </div>
    </div>
  );
};
