import React from 'react';
import { Route } from 'lucide-react';

export const PathImpactCard: React.FC = () => {
  return (
    <section
      aria-label="Learning Path Impact"
      className="rounded-3xl bg-[#f0f4ff]/80 backdrop-blur-2xl border border-white/90 p-5 sm:p-6 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-3.5"
    >
      {/* Header */}
      <div className="flex items-center gap-2">
        <div className="w-7 h-7 rounded-xl bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#8e4d2b]">
          <Route className="w-3.5 h-3.5 text-[#8e4d2b]" />
        </div>
        <h3 className="text-sm sm:text-base font-extrabold text-[#0f1b32] tracking-tight">
          Path Impact
        </h3>
      </div>

      {/* Description */}
      <p className="text-xs text-[#53433c] leading-relaxed font-normal">
        Your recent score in <strong className="font-bold text-[#0f1b32]">Git Flow</strong> unlocked
        new learning modules.
      </p>

      {/* Unlocked Module Box */}
      <div className="p-3.5 rounded-2xl bg-white/90 border border-gray-100 shadow-2xs space-y-1">
        <div className="flex items-center justify-between">
          <h4 className="text-xs font-bold text-[#0f1b32]">Advanced CI/CD</h4>
          <span className="px-2 py-0.5 rounded-full bg-[#ffdbcb]/60 border border-[#d98b63]/30 text-[#8e4d2b] text-[10px] font-bold">
            UNLOCKED
          </span>
        </div>
        <p className="text-[11px] text-gray-500 font-normal leading-relaxed">
          Added to your primary learning path based on demonstrated Git proficiency.
        </p>
      </div>
    </section>
  );
};
