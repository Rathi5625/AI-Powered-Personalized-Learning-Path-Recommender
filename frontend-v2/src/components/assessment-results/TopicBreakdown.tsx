import React from 'react';
import { motion } from 'framer-motion';

interface TopicItem {
  name: string;
  score: number;
  color: string;
}

const TOPIC_BREAKDOWN_DATA: TopicItem[] = [
  { name: 'Arrays', score: 86, color: '#88A98F' },
  { name: 'Strings', score: 82, color: '#88A98F' },
  { name: 'Searching', score: 79, color: '#88A98F' },
  { name: 'Linked Lists', score: 65, color: '#8fa0ae' },
  { name: 'Complexity', score: 61, color: '#d98b63' },
];

export const TopicBreakdown: React.FC = () => {
  return (
    <section
      aria-label="Topic Breakdown"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-5"
    >
      <h3 className="text-base sm:text-lg font-extrabold text-[#0f1b32] tracking-tight">
        Topic Breakdown
      </h3>

      <div className="space-y-4">
        {TOPIC_BREAKDOWN_DATA.map((topic) => (
          <div key={topic.name} className="space-y-1.5">
            {/* Label and Percentage */}
            <div className="flex items-center justify-between text-xs sm:text-sm font-semibold">
              <span className="text-[#0f1b32]">{topic.name}</span>
              <span className="text-xs text-gray-500 font-bold">{topic.score}%</span>
            </div>

            {/* Accessible Animated Progress Bar */}
            <div
              role="progressbar"
              aria-label={`${topic.name} proficiency`}
              aria-valuenow={topic.score}
              aria-valuemin={0}
              aria-valuemax={100}
              className="w-full h-2 bg-gray-100/90 rounded-full overflow-hidden"
            >
              <motion.div
                initial={{ width: 0 }}
                animate={{ width: `${topic.score}%` }}
                transition={{ duration: 0.9, ease: 'easeOut' }}
                style={{ backgroundColor: topic.color }}
                className="h-full rounded-full"
              />
            </div>
          </div>
        ))}
      </div>
    </section>
  );
};
