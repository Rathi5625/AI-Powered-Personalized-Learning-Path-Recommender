import React from 'react';
import { History, MessageSquare } from 'lucide-react';

interface ConversationHistoryProps {
  onSelectConversation: (topic: string) => void;
}

const PAST_CONVERSATIONS = [
  'DSA preparation',
  'React learning roadmap',
  'Spring Boot project help',
  'Placement preparation',
];

export const ConversationHistory: React.FC<ConversationHistoryProps> = ({
  onSelectConversation,
}) => {
  return (
    <div className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-5 sm:p-6 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-3.5 select-none">
      <div className="flex items-center gap-2 text-xs font-bold text-gray-500 uppercase tracking-wider">
        <History className="w-3.5 h-3.5 text-[#8e4d2b]" />
        <span>Recent Conversations</span>
      </div>

      <div className="space-y-1.5">
        {PAST_CONVERSATIONS.map((topic) => (
          <button
            key={topic}
            type="button"
            onClick={() => onSelectConversation(topic)}
            className="w-full flex items-center gap-2.5 px-3 py-2 rounded-xl bg-white/60 hover:bg-[#FAF4F0] border border-gray-100/80 text-xs font-semibold text-[#0f1b32] transition-colors text-left cursor-pointer"
          >
            <MessageSquare className="w-3.5 h-3.5 text-[#8e4d2b] shrink-0" />
            <span className="truncate">{topic}</span>
          </button>
        ))}
      </div>
    </div>
  );
};
