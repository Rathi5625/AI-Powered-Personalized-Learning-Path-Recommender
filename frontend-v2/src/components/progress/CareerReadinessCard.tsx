import React from 'react';
import { ArrowRight } from 'lucide-react';
import { motion } from 'framer-motion';

interface CareerReadinessCardProps {
  score?: number;
  improvement?: number;
  role?: string;
  onViewAnalysis?: () => void;
}

export const CareerReadinessCard: React.FC<CareerReadinessCardProps> = ({
  score = 0,
  improvement = 0,
  role = 'Software Engineer',
  onViewAnalysis,
}) => {
  const size = 150;
  const strokeWidth = 12;
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = circumference - (score / 100) * circumference;

  return (
    <section
      aria-label="Career Readiness Overview"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-8 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left flex flex-col md:flex-row items-center justify-between gap-6"
    >
      {/* Left Info */}
      <div className="space-y-3 flex-1">
        <span className="inline-block px-3 py-1 rounded-full bg-[#FAF4F0] border border-[#F2DACB] text-[#8e4d2b] text-[10px] font-bold uppercase tracking-wider shadow-2xs">
          TARGET ROLE
        </span>

        <div>
          <h2 className="text-xl sm:text-2xl font-extrabold text-[#0f1b32] tracking-tight">
            Career Readiness
          </h2>
          <span className="text-base sm:text-lg font-bold text-[#8e4d2b] block mt-0.5">
            {role}
          </span>
        </div>

        <p className="text-xs sm:text-sm text-[#53433c] leading-relaxed font-normal">
          You&apos;re making steady progress toward your target role.
        </p>

        <div className="pt-1">
          <button
            type="button"
            onClick={onViewAnalysis}
            className="inline-flex items-center gap-1.5 text-xs font-bold text-[#0f1b32] hover:text-[#8e4d2b] transition-colors cursor-pointer"
          >
            <span>View Skill Analysis</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>

      {/* Right Circular Gauge */}
      <div className="shrink-0 relative flex flex-col items-center justify-center">
        <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} className="-rotate-90 transform">
          {/* Background Track */}
          <circle
            cx={size / 2}
            cy={size / 2}
            r={radius}
            fill="none"
            stroke="#e9edff"
            strokeWidth={strokeWidth}
          />
          {/* Animated Arc */}
          <motion.circle
            cx={size / 2}
            cy={size / 2}
            r={radius}
            fill="none"
            stroke="#d98b63"
            strokeWidth={strokeWidth}
            strokeDasharray={circumference}
            initial={{ strokeDashoffset: circumference }}
            animate={{ strokeDashoffset }}
            transition={{ duration: 1.2, ease: 'easeOut' }}
            strokeLinecap="round"
          />
        </svg>

        {/* Centered Score */}
        <div className="absolute inset-0 flex flex-col items-center justify-center text-center">
          <span className="text-3xl font-extrabold text-[#0f1b32] leading-none">
            {score}%
          </span>
          <span className="text-xs font-bold text-emerald-600 mt-1 flex items-center gap-0.5">
            ↑ {improvement}%
          </span>
        </div>
      </div>
    </section>
  );
};
