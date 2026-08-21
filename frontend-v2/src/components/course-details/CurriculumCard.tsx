import React, { useState } from 'react';
import { Check, Play, ChevronDown, ChevronUp, ArrowRight } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

interface CurriculumModule {
  id: number;
  title: string;
  subtitle: string;
  status: 'completed' | 'current' | 'upcoming';
  progress?: number;
}

const MODULES: CurriculumModule[] = [
  {
    id: 1,
    title: '1. Foundations & Complexity',
    subtitle: 'Big O Notation, Memory, Arrays.',
    status: 'completed',
  },
  {
    id: 2,
    title: '2. Linked Lists',
    subtitle: 'Singly, Doubly, Circular, Pointers.',
    status: 'current',
    progress: 68,
  },
  {
    id: 3,
    title: '3. Trees & Heaps',
    subtitle: 'Binary Search Trees, AVL, Trie.',
    status: 'upcoming',
  },
  {
    id: 4,
    title: '4. Graphs & Traversal',
    subtitle: 'BFS, DFS, Dijkstra’s Algorithm.',
    status: 'upcoming',
  },
  {
    id: 5,
    title: '5. Advanced Algorithms',
    subtitle: 'Dynamic Programming, Greedy.',
    status: 'upcoming',
  },
];

interface CurriculumCardProps {
  onContinueModule?: (moduleId: number) => void;
}

export const CurriculumCard: React.FC<CurriculumCardProps> = ({ onContinueModule }) => {
  // Module 2 is expanded by default as shown in reference design
  const [expandedModules, setExpandedModules] = useState<Record<number, boolean>>({
    2: true,
  });

  const toggleModule = (id: number) => {
    setExpandedModules((prev) => ({
      ...prev,
      [id]: !prev[id],
    }));
  };

  return (
    <section
      id="curriculum-section"
      aria-label="Course Curriculum"
      className="rounded-3xl bg-white/80 backdrop-blur-2xl border border-white/90 p-5 sm:p-6 shadow-[0_8px_32px_rgba(23,35,58,0.06)] text-left space-y-4"
    >
      <h2 className="text-lg sm:text-xl font-extrabold text-[#0f1b32] tracking-tight">
        Curriculum
      </h2>

      {/* Module List Accordion */}
      <div className="space-y-2.5">
        {MODULES.map((module) => {
          const isExpanded = !!expandedModules[module.id];
          const isCurrent = module.status === 'current';
          const isCompleted = module.status === 'completed';

          return (
            <div
              key={module.id}
              className={`
                rounded-2xl transition-all duration-200 overflow-hidden
                ${
                  isCurrent
                    ? 'bg-[#FAF4F0]/90 border border-[#F2DACB]'
                    : 'bg-white/60 hover:bg-white/90 border border-gray-100/90'
                }
              `}
            >
              {/* Accordion Header */}
              <button
                type="button"
                onClick={() => toggleModule(module.id)}
                aria-expanded={isExpanded}
                className="w-full flex items-center justify-between p-3.5 sm:p-4 text-left cursor-pointer transition-colors"
              >
                <div className="flex items-center gap-3 min-w-0 pr-2">
                  {/* Status Indicator Icon */}
                  {isCompleted && (
                    <div className="w-6 h-6 rounded-full bg-emerald-100 text-emerald-700 flex items-center justify-center shrink-0">
                      <Check className="w-3.5 h-3.5 stroke-[3]" />
                    </div>
                  )}

                  {isCurrent && (
                    <div className="w-6 h-6 rounded-full bg-[#ffdbcb] text-[#8e4d2b] flex items-center justify-center shrink-0">
                      <Play className="w-3 h-3 fill-[#8e4d2b]" />
                    </div>
                  )}

                  {!isCompleted && !isCurrent && (
                    <div className="w-6 h-6 rounded-full bg-gray-100 text-gray-500 flex items-center justify-center text-xs font-bold shrink-0">
                      {module.id}
                    </div>
                  )}

                  {/* Title & Subtitle */}
                  <div className="min-w-0">
                    <h3
                      className={`text-xs sm:text-sm font-bold truncate ${
                        isCurrent ? 'text-[#8e4d2b]' : 'text-[#0f1b32]'
                      }`}
                    >
                      {module.title}
                    </h3>
                    <p className="text-[11px] text-gray-500 font-normal truncate mt-0.5">
                      {module.subtitle}
                    </p>
                  </div>
                </div>

                {/* Chevron */}
                <div className="text-gray-400 shrink-0">
                  {isExpanded ? (
                    <ChevronUp className="w-4 h-4" />
                  ) : (
                    <ChevronDown className="w-4 h-4" />
                  )}
                </div>
              </button>

              {/* Accordion Body */}
              <AnimatePresence>
                {isExpanded && (
                  <motion.div
                    initial={{ height: 0, opacity: 0 }}
                    animate={{ height: 'auto', opacity: 1 }}
                    exit={{ height: 0, opacity: 0 }}
                    transition={{ duration: 0.2 }}
                    className="px-4 pb-4 pt-1"
                  >
                    {isCurrent && (
                      <div className="space-y-2.5 pt-2 border-t border-[#F2DACB]/60">
                        {/* Progress label & value */}
                        <div className="flex items-center justify-between text-[11px] font-bold">
                          <span className="text-gray-500 tracking-wider uppercase">
                            Module Progress
                          </span>
                          <span className="text-[#8e4d2b]">{module.progress}%</span>
                        </div>

                        {/* Progress bar */}
                        <div className="w-full h-2 bg-[#ffdbcb]/60 rounded-full overflow-hidden">
                          <div
                            className="h-full bg-[#8e4d2b] rounded-full transition-all duration-500"
                            style={{ width: `${module.progress}%` }}
                          />
                        </div>

                        {/* Action link */}
                        <div className="pt-1">
                          <button
                            type="button"
                            onClick={() => onContinueModule?.(module.id)}
                            className="inline-flex items-center gap-1.5 text-xs font-bold text-[#8e4d2b] hover:text-[#783e20] transition-colors cursor-pointer"
                          >
                            <span>Continue</span>
                            <ArrowRight className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      </div>
                    )}

                    {!isCurrent && (
                      <div className="pt-2 border-t border-gray-100 text-xs text-gray-500 leading-relaxed font-normal">
                        {isCompleted ? (
                          <span className="text-emerald-700 font-semibold">
                            ✓ Completed on your learning journey.
                          </span>
                        ) : (
                          <span>
                            Upcoming topic. Completes core fundamentals required for advanced algorithm mastery.
                          </span>
                        )}
                      </div>
                    )}
                  </motion.div>
                )}
              </AnimatePresence>
            </div>
          );
        })}
      </div>
    </section>
  );
};
