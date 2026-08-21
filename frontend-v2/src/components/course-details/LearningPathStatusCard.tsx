import React from 'react';
import { Link } from 'react-router-dom';
import { Route } from 'lucide-react';

interface LearningPathStatusCardProps {
  onContinuePath?: () => void;
}

export const LearningPathStatusCard: React.FC<LearningPathStatusCardProps> = ({
  onContinuePath,
}) => {
  return (
    <section
      aria-label="Learning Path Status"
      className="rounded-3xl bg-white/80 backdrop-blur-2xl border border-white/90 border-l-4 border-l-[#8e4d2b] p-5 sm:p-6 shadow-[0_8px_32px_rgba(23,35,58,0.06)] text-left space-y-3.5"
    >
      <div className="flex items-center gap-2.5">
        <div className="w-8 h-8 rounded-xl bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#8e4d2b]">
          <Route className="w-4 h-4 text-[#8e4d2b]" />
        </div>
        <h3 className="text-sm sm:text-base font-bold text-[#0f1b32] tracking-tight">
          Your Learning Path
        </h3>
      </div>

      <p className="text-xs text-[#53433c] leading-relaxed font-normal">
        You are currently 61% through your overall DSA competency journey.
      </p>

      <div className="pt-1">
        {onContinuePath ? (
          <button
            type="button"
            onClick={onContinuePath}
            className="w-full py-3 rounded-2xl bg-[#0f1b32] hover:bg-[#1b2c4e] text-white text-xs sm:text-sm font-bold transition-colors cursor-pointer shadow-sm active:scale-[0.99] text-center"
          >
            Continue Path
          </button>
        ) : (
          <Link
            to="/learning-path"
            className="block w-full py-3 rounded-2xl bg-[#0f1b32] hover:bg-[#1b2c4e] text-white text-xs sm:text-sm font-bold transition-colors cursor-pointer shadow-sm active:scale-[0.99] text-center"
          >
            Continue Path
          </Link>
        )}
      </div>
    </section>
  );
};
