import React from 'react';
import { motion } from 'framer-motion';
import { Target, Brain, Cpu, Route } from 'lucide-react';

const stages = [
  {
    icon: Target,
    title: 'Goal Setting',
    subtitle: 'Define objectives',
    bg: 'bg-white border-2 border-[#D4C4B8]',
    iconColor: 'text-[#A06A42]',
    titleClass: 'text-[#1A1F36] font-semibold',
    subtitleClass: 'text-gray-500',
    active: false,
  },
  {
    icon: Brain,
    title: 'Skill Mapping',
    subtitle: 'Current proficiency',
    bg: 'bg-[#EAE8FF]',
    iconColor: 'text-[#6B65E0]',
    titleClass: 'text-[#1A1F36] font-semibold',
    subtitleClass: 'text-gray-500',
    active: false,
  },
  {
    icon: Cpu,
    title: 'AI Analysis',
    subtitle: 'Processing path',
    bg: 'bg-[#EAE8FF]',
    iconColor: 'text-[#8E86FF]',
    titleClass: 'text-[#1A1F36] font-semibold',
    subtitleClass: 'text-gray-500',
    active: false,
  },
  {
    icon: Route,
    title: 'Learning Path',
    subtitle: 'Ready to start',
    bg: 'bg-[#CC7D52]',
    iconColor: 'text-white',
    titleClass: 'text-[#1A1F36] font-bold',
    subtitleClass: 'text-[#CC7D52] font-semibold',
    active: true,
  },
];

export const LearningJourney: React.FC = () => {
  return (
    <div className="flex flex-col gap-0 relative">
      {/* Connector line behind icons */}
      <div
        className="absolute left-[19px] top-[24px] bottom-[24px] w-px bg-gradient-to-b from-[#E8DCDC] via-[#D4C0FF] to-[#CC7D52]/40 hidden sm:block"
        style={{ zIndex: 0 }}
      />

      {stages.map((stage, i) => {
        const Icon = stage.icon;
        return (
          <motion.div
            key={stage.title}
            initial={{ opacity: 0, x: 12 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.15 + i * 0.1, duration: 0.4, ease: [0.16, 1, 0.3, 1] }}
            className="flex items-center gap-3 relative z-10 py-3"
          >
            {/* Icon Circle */}
            <div
              className={`w-10 h-10 rounded-full flex-shrink-0 flex items-center justify-center ${stage.bg} shadow-sm`}
            >
              <Icon className={`w-4.5 h-4.5 ${stage.iconColor}`} style={{ width: 18, height: 18 }} />
            </div>

            {/* Text */}
            <div>
              <p className={`text-sm leading-tight ${stage.titleClass}`}>{stage.title}</p>
              <p className={`text-xs mt-0.5 ${stage.subtitleClass}`}>{stage.subtitle}</p>
            </div>
          </motion.div>
        );
      })}
    </div>
  );
};
