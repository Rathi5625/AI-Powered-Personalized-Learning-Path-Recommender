import React from 'react';
import { Bot, Sparkles, User } from 'lucide-react';
import { MentorQuickPrompts } from './MentorQuickPrompts';
import { MentorRichResponse } from './MentorRichResponse';

export interface ChatMessageItem {
  id: string;
  role: 'user' | 'mentor';
  type?: 'text' | 'welcome' | 'rich';
  content?: string;
  timestamp?: string;
}

interface MentorMessageProps {
  message: ChatMessageItem;
  onSelectPrompt?: (prompt: string) => void;
  onVisualExample?: () => void;
  onExplainFurther?: () => void;
}

export const MentorMessage: React.FC<MentorMessageProps> = ({
  message,
  onSelectPrompt,
  onVisualExample,
  onExplainFurther,
}) => {
  const isUser = message.role === 'user';

  if (isUser) {
    return (
      <div className="flex flex-col items-end space-y-1 select-none">
        {/* Header Label: YOU */}
        <div className="flex items-center gap-1.5 text-[11px] font-bold text-gray-400 uppercase tracking-wider pr-1">
          <span>YOU</span>
          <div className="w-5 h-5 rounded-full bg-[#ffdbcb] text-[#8e4d2b] flex items-center justify-center font-bold text-[9px]">
            <User className="w-3 h-3 text-[#8e4d2b]" />
          </div>
        </div>

        {/* User Bubble */}
        <div className="max-w-[85%] sm:max-w-[75%] p-4 rounded-2xl bg-[#d8e2ff]/75 border border-[#c7d8fe]/80 text-[#0f1b32] text-xs sm:text-sm font-medium leading-relaxed text-left shadow-2xs">
          {message.content}
        </div>
      </div>
    );
  }

  // Mentor Message
  return (
    <div className="flex items-start gap-3 select-none text-left">
      {/* Bot Avatar Icon */}
      <div className="w-8 h-8 rounded-full bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#8e4d2b] shrink-0 mt-1 shadow-2xs">
        <Bot className="w-4 h-4 text-[#8e4d2b]" />
      </div>

      {/* Message Content Bubble Container */}
      <div className="flex-1 space-y-2">
        {/* Mentor Label Header */}
        <div className="flex items-center gap-1 text-[11px] font-bold text-[#8e4d2b] uppercase tracking-wider">
          <Sparkles className="w-3 h-3 text-[#8e4d2b]" />
          <span>LEARNAI MENTOR</span>
        </div>

        {/* Message Body */}
        <div className="p-4 sm:p-5 rounded-2xl rounded-tl-xs bg-white/80 backdrop-blur-md border border-white/90 shadow-2xs space-y-3">
          {message.type === 'welcome' ? (
            <>
              <p className="text-xs sm:text-sm text-[#0f1b32] leading-relaxed">
                Welcome back! I noticed you were recently working on{' '}
                <strong className="font-bold text-[#8e4d2b]">Binary Search</strong>. You&apos;re making
                good progress, currently at <strong className="font-bold text-[#0f1b32]">61%</strong> mastery
                in Data Structures.
              </p>
              <p className="text-xs sm:text-sm text-[#53433c] leading-relaxed">
                Would you like to continue with practice problems, or should we explore a new topic from your
                learning path?
              </p>
              {onSelectPrompt && <MentorQuickPrompts onSelectPrompt={onSelectPrompt} />}
            </>
          ) : message.type === 'rich' ? (
            <MentorRichResponse
              onVisualExample={onVisualExample}
              onExplainFurther={onExplainFurther}
            />
          ) : (
            <p className="text-xs sm:text-sm text-[#0f1b32] leading-relaxed">
              {message.content}
            </p>
          )}
        </div>
      </div>
    </div>
  );
};
