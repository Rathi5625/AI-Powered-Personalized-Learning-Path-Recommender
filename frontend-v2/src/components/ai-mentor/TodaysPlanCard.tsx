import React, { useState, useEffect } from 'react';
import { Check, Sparkles } from 'lucide-react';
import api from '../../api/client';
import { DailyLearningPlan } from '../../api/types';

interface PlanItem {
  id: string;
  title: string;
  status: 'completed' | 'in-progress' | 'up-next';
  statusText: string;
}

const DEFAULT_PLAN: PlanItem[] = [
  {
    id: '1',
    title: 'Review Arrays & Traversal',
    status: 'completed',
    statusText: 'Completed',
  },
  {
    id: '2',
    title: 'Master Binary Search',
    status: 'in-progress',
    statusText: 'In Progress (Focus Topic)',
  },
  {
    id: '3',
    title: 'Adaptive Assessment Quiz',
    status: 'up-next',
    statusText: 'Up Next',
  },
];

interface TodaysPlanCardProps {
  onSelectPlanItem?: (item: PlanItem) => void;
}

export const TodaysPlanCard: React.FC<TodaysPlanCardProps> = ({
  onSelectPlanItem,
}) => {
  const [plan, setPlan] = useState<DailyLearningPlan | null>(null);

  useEffect(() => {
    api.getLearningPlan()
      .then((data) => setPlan(data))
      .catch((err) => console.error('Failed to fetch learning plan:', err));
  }, []);

  const items: PlanItem[] = plan && plan.items && plan.items.length > 0
    ? plan.items.map((item, idx) => ({
        id: item.id,
        title: item.title,
        status: idx === 0 ? 'in-progress' : 'up-next',
        statusText: `${item.durationMinutes} min • ${item.type}`,
      }))
    : DEFAULT_PLAN;

  return (
    <section
      aria-label="Today's Plan"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-5 sm:p-6 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-4"
    >
      <div className="flex items-center justify-between">
        <h3 className="text-sm sm:text-base font-extrabold text-[#0f1b32] tracking-tight">
          Today&apos;s Plan
        </h3>
        {plan && (
          <span className="text-[10px] font-bold text-[#8e4d2b] bg-[#FAF4F0] border border-[#F2DACB] px-2 py-0.5 rounded-full inline-flex items-center gap-1">
            <Sparkles className="w-2.5 h-2.5" />
            <span>{plan.estimatedTotalMinutes} min total</span>
          </span>
        )}
      </div>

      {/* Timeline Steps */}
      <div className="space-y-4 pt-1">
        {items.map((item, idx) => {
          const isCompleted = item.status === 'completed';
          const isInProgress = item.status === 'in-progress';
          const isLast = idx === items.length - 1;

          return (
            <div
              key={item.id}
              onClick={() => onSelectPlanItem?.(item)}
              className="relative flex items-start gap-3 text-left cursor-pointer group"
            >
              {/* Connecting vertical line */}
              {!isLast && (
                <div className="absolute left-3.5 top-6 bottom-0 w-0.5 bg-gray-100 -mb-4 z-0" />
              )}

              {/* Indicator Circle */}
              <div className="relative z-10 shrink-0">
                {isCompleted ? (
                  <div className="w-7 h-7 rounded-full bg-emerald-500 text-white flex items-center justify-center shadow-xs">
                    <Check className="w-4 h-4" />
                  </div>
                ) : isInProgress ? (
                  <div className="w-7 h-7 rounded-full bg-[#FAF4F0] border-2 border-[#8e4d2b] text-[#8e4d2b] flex items-center justify-center font-bold text-xs shadow-xs">
                    <span className="w-2 h-2 rounded-full bg-[#8e4d2b] animate-ping" />
                  </div>
                ) : (
                  <div className="w-7 h-7 rounded-full bg-white border border-gray-200 text-gray-400 flex items-center justify-center text-xs">
                    <span className="w-1.5 h-1.5 rounded-full bg-gray-300" />
                  </div>
                )}
              </div>

              {/* Content */}
              <div className="flex-1 pb-1">
                <h4
                  className={`text-xs sm:text-sm font-bold tracking-tight ${
                    isCompleted
                      ? 'text-gray-400 line-through'
                      : isInProgress
                      ? 'text-[#8e4d2b]'
                      : 'text-[#0f1b32]'
                  }`}
                >
                  {item.title}
                </h4>
                <span
                  className={`text-[11px] font-medium block ${
                    isInProgress ? 'text-[#8e4d2b]' : 'text-gray-400'
                  }`}
                >
                  {item.statusText}
                </span>
              </div>
            </div>
          );
        })}
      </div>
    </section>
  );
};
