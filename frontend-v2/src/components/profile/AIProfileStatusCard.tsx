import React, { useState } from 'react';
import { Sparkles, Check, Bot, Loader2 } from 'lucide-react';

interface AIProfileStatusCardProps {
  onRefresh?: () => void;
}

const CHECKLIST_ITEMS = [
  'Career Goal Set',
  'Skills Mapped',
  'Pace Adjusted',
  'Schedule Synced',
];

export const AIProfileStatusCard: React.FC<AIProfileStatusCardProps> = ({ onRefresh }) => {
  const [loading, setLoading] = useState(false);

  const handleRefresh = () => {
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
      onRefresh?.();
    }, 600);
  };

  return (
    <section
      aria-label="AI Profile Optimization Status"
      className="relative overflow-hidden rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-5"
    >
      {/* Subtle Robot Watermark in Background */}
      <Bot className="absolute right-3 top-3 w-28 h-28 text-[#d98b63]/[0.08] pointer-events-none -rotate-12" />

      {/* Header */}
      <div className="space-y-1 relative z-10">
        <div className="flex items-center gap-1.5 text-xs font-bold text-[#8e4d2b]">
          <Sparkles className="w-3.5 h-3.5 text-[#8e4d2b]" />
          <span>Your LearnAI Profile</span>
        </div>
        <p className="text-xs text-gray-500 font-medium">
          Your learning path is fully optimized.
        </p>
      </div>

      {/* Checklist */}
      <div className="space-y-2.5 relative z-10 text-xs font-bold text-[#0f1b32]">
        {CHECKLIST_ITEMS.map((item) => (
          <div key={item} className="flex items-center gap-2.5">
            <div className="w-5 h-5 rounded-full bg-[#FAF4F0] border border-[#F2DACB] text-[#8e4d2b] flex items-center justify-center shrink-0">
              <Check className="w-3 h-3 stroke-[2.5]" />
            </div>
            <span>{item}</span>
          </div>
        ))}
      </div>

      {/* Action Button */}
      <div className="pt-1 relative z-10">
        <button
          type="button"
          disabled={loading}
          onClick={handleRefresh}
          className="w-full py-3 px-4 rounded-2xl bg-[#8e4d2b] hover:bg-[#783e20] text-white text-xs font-bold transition-all shadow-2xs hover:shadow-xs cursor-pointer flex items-center justify-center gap-2 active:scale-[0.99] disabled:opacity-75"
        >
          {loading ? (
            <>
              <Loader2 className="w-3.5 h-3.5 animate-spin" />
              <span>Optimizing...</span>
            </>
          ) : (
            <span>Refresh Recommendations</span>
          )}
        </button>
      </div>
    </section>
  );
};
