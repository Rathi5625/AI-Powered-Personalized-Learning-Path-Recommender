import React from 'react';
import { Sparkles } from 'lucide-react';
import { BuildPlanStep, type BuildPlanStepData } from './BuildPlanStep';

const BUILD_PLAN_STEPS: BuildPlanStepData[] = [
  {
    day: 1,
    title: 'Project Setup & Env',
    duration: '1 hr',
    description: 'Initialize Spring Boot app, configure application.yml, and set up Dockerized MySQL.',
    status: 'completed',
  },
  {
    day: 2,
    title: 'Database & Entities',
    duration: '2 hrs',
    description: 'Create JPA entities (User, Product, Order) and define relational mappings.',
    status: 'completed',
  },
  {
    day: 3,
    title: 'Authentication & JWT',
    duration: '2.5 hrs',
    description: 'Implement Spring Security configuration, create JWT utility classes, and build login/register endpoints.',
    status: 'current',
  },
  {
    day: 4,
    title: 'Product Catalog APIs',
    duration: '1.5 hrs',
    description: 'Build REST endpoints for products, implement pagination, search filtering, and DTO validations.',
    status: 'upcoming',
  },
  {
    day: 5,
    title: 'Cart & Order Processing',
    duration: '2 hrs',
    description: 'Implement shopping cart logic, order placement with transactional guarantees, and error handling.',
    status: 'upcoming',
  },
];

interface BuildPlanProps {
  onStepAction?: (step: BuildPlanStepData) => void;
  highlightedStepDay?: number | null;
}

export const BuildPlan: React.FC<BuildPlanProps> = ({
  onStepAction,
  highlightedStepDay,
}) => {
  return (
    <section
      aria-label="AI Build Plan"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-8 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-6"
    >
      {/* Header */}
      <div className="space-y-1.5">
        <div className="flex items-center gap-2">
          <Sparkles className="w-4 h-4 text-[#8e4d2b]" />
          <h2 className="text-lg sm:text-xl font-extrabold text-[#0f1b32] tracking-tight">
            Your AI Build Plan
          </h2>
        </div>
        <p className="text-xs sm:text-sm text-[#53433c] leading-relaxed font-normal">
          LearnAI created this structured 7-day timeline tailored to your current backend experience
          and your stated availability of 10 hours per week.
        </p>
      </div>

      {/* Vertical Timeline */}
      <div className="space-y-4 pt-2">
        {BUILD_PLAN_STEPS.map((step, idx) => (
          <BuildPlanStep
            key={step.day}
            step={step}
            isLast={idx === BUILD_PLAN_STEPS.length - 1}
            onAction={onStepAction}
            isHighlighted={highlightedStepDay === step.day}
          />
        ))}
      </div>
    </section>
  );
};
