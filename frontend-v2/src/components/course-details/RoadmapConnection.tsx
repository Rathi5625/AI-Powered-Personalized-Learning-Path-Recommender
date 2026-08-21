import React from 'react';
import { Check, Network, Code, Rocket, ShieldCheck } from 'lucide-react';

interface Stage {
  id: string;
  name: string;
  sublabel?: string;
  status: 'completed' | 'current' | 'upcoming';
  icon: React.ElementType;
}

const STAGES: Stage[] = [
  {
    id: '1',
    name: 'Foundations',
    status: 'completed',
    icon: Check,
  },
  {
    id: '2',
    name: 'DSA (This Course)',
    status: 'current',
    icon: Network,
  },
  {
    id: '3',
    name: 'Backend Dev',
    status: 'upcoming',
    icon: Code,
  },
  {
    id: '4',
    name: 'Projects',
    status: 'upcoming',
    icon: Rocket,
  },
  {
    id: '5',
    name: 'Interview Prep',
    status: 'upcoming',
    icon: ShieldCheck,
  },
];

export const RoadmapConnection: React.FC = () => {
  return (
    <section
      aria-label="Roadmap Connection Timeline"
      className="relative overflow-hidden rounded-3xl bg-white/70 backdrop-blur-2xl border border-white/80 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left"
    >
      <h2 className="text-base sm:text-lg font-bold text-[#0f1b32] tracking-tight mb-6">
        Roadmap Connection
      </h2>

      {/* Horizontal Timeline Container */}
      <div className="relative overflow-x-auto pb-2 scrollbar-none">
        <div className="min-w-[520px] flex items-center justify-between relative px-4">
          {/* Background Connecting Line */}
          <div className="absolute left-8 right-8 top-5 h-[2px] bg-gray-200 z-0" />

          {/* Render Timeline Nodes */}
          {STAGES.map((stage) => {
            const Icon = stage.icon;

            if (stage.status === 'completed') {
              return (
                <div key={stage.id} className="relative z-10 flex flex-col items-center group">
                  <div className="w-10 h-10 rounded-full bg-emerald-600 text-white flex items-center justify-center shadow-md ring-4 ring-white">
                    <Check className="w-5 h-5 stroke-[2.5]" />
                  </div>
                  <span className="text-xs font-semibold text-[#0f1b32] mt-3 whitespace-nowrap">
                    {stage.name}
                  </span>
                </div>
              );
            }

            if (stage.status === 'current') {
              return (
                <div key={stage.id} className="relative z-10 flex flex-col items-center">
                  <div className="w-12 h-12 rounded-full bg-[#d98b63] text-white flex items-center justify-center shadow-lg shadow-[#d98b63]/30 ring-4 ring-white animate-pulse">
                    <Icon className="w-5 h-5" />
                  </div>
                  <span className="text-xs font-bold text-[#8e4d2b] mt-2 whitespace-nowrap">
                    {stage.name}
                  </span>
                </div>
              );
            }

            return (
              <div key={stage.id} className="relative z-10 flex flex-col items-center">
                <div className="w-10 h-10 rounded-full bg-[#f0f4f9] text-[#718096] border border-gray-200 flex items-center justify-center ring-4 ring-white">
                  <Icon className="w-4 h-4" />
                </div>
                <span className="text-xs font-medium text-gray-500 mt-3 whitespace-nowrap">
                  {stage.name}
                </span>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
};
