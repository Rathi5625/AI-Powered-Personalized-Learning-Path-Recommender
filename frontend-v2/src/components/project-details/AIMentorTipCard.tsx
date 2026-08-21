import React from 'react';
import { Bot, ArrowRight, MessageSquare } from 'lucide-react';

interface AIMentorTipCardProps {
  onReviewUserEntity?: () => void;
  onAskSecurity?: () => void;
}

export const AIMentorTipCard: React.FC<AIMentorTipCardProps> = ({
  onReviewUserEntity,
  onAskSecurity,
}) => {
  return (
    <section
      aria-label="AI Mentor Tip"
      className="relative overflow-hidden rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-4"
    >
      {/* Decorative Oversized Bot Watermark */}
      <Bot className="pointer-events-none absolute -right-4 -top-4 w-28 h-28 text-gray-100/60 -rotate-12 select-none" />

      {/* Header */}
      <div className="relative z-10 flex items-center gap-2">
        <div className="w-7 h-7 rounded-xl bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#8e4d2b]">
          <Bot className="w-3.5 h-3.5 text-[#8e4d2b]" />
        </div>
        <h3 className="text-sm sm:text-base font-extrabold text-[#0f1b32] tracking-tight">
          AI Mentor Tip
        </h3>
      </div>

      {/* Tip Quote */}
      <p className="relative z-10 text-xs sm:text-sm text-[#53433c] leading-relaxed italic font-normal">
        &ldquo;Before diving into JWT implementation today, review the User entity we created yesterday.
        Ensure your roles and permissions align with the Spring Security UserDetails interface
        requirements.&rdquo;
      </p>

      {/* Quick Interactive Actions */}
      <div className="relative z-10 space-y-2 pt-2 border-t border-gray-100/80 text-xs font-bold">
        <button
          type="button"
          onClick={onReviewUserEntity}
          className="w-full flex items-center justify-between py-2 text-[#0f1b32] hover:text-[#8e4d2b] transition-colors cursor-pointer"
        >
          <span>Review User Entity</span>
          <ArrowRight className="w-3.5 h-3.5 text-gray-400" />
        </button>

        <button
          type="button"
          onClick={onAskSecurity}
          className="w-full flex items-center justify-between py-2 text-[#0f1b32] hover:text-[#8e4d2b] transition-colors cursor-pointer"
        >
          <span>Ask about Spring Security</span>
          <MessageSquare className="w-3.5 h-3.5 text-gray-400" />
        </button>
      </div>
    </section>
  );
};
