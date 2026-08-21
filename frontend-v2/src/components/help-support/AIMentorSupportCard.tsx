import React from 'react';
import { Sparkles, Bot } from 'lucide-react';
import { Link } from 'react-router-dom';

export const AIMentorSupportCard: React.FC = () => {
  return (
    <div className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-4 select-none">
      {/* Header */}
      <div className="flex items-center gap-1.5 text-sm font-extrabold text-[#0f1b32]">
        <Sparkles className="w-4 h-4 text-[#8e4d2b]" />
        <span>Need a quick answer?</span>
      </div>

      {/* Description */}
      <p className="text-xs text-[#53433c] font-normal leading-relaxed">
        Ask LearnAI Mentor. It knows your profile, current progress, and learning history to provide
        personalized assistance.
      </p>

      {/* Action Button */}
      <div className="pt-1">
        <Link
          to="/ai-mentor"
          className="w-full py-3 px-4 rounded-2xl bg-[#e9edff] hover:bg-[#e1d8fe] border border-[#d8e2ff] text-[#0f1b32] text-xs font-bold transition-all shadow-2xs cursor-pointer flex items-center justify-center gap-2 active:scale-[0.99]"
        >
          <Bot className="w-4 h-4 text-[#615a7a]" />
          <span>Ask AI Mentor</span>
        </Link>
      </div>
    </div>
  );
};
