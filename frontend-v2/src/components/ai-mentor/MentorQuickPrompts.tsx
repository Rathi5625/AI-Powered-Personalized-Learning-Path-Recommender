import React from 'react';
import { Code2, Sparkles, BookOpen } from 'lucide-react';

interface MentorQuickPromptsProps {
  onSelectPrompt: (prompt: string) => void;
}

export const MentorQuickPrompts: React.FC<MentorQuickPromptsProps> = ({
  onSelectPrompt,
}) => {
  return (
    <div className="flex items-center gap-2 sm:gap-2.5 flex-wrap pt-2 select-none">
      {/* Prompt 1 */}
      <button
        type="button"
        onClick={() => onSelectPrompt('Practice Binary Search')}
        className="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-full bg-white/80 hover:bg-white border border-gray-200/80 text-xs font-semibold text-[#0f1b32] hover:text-[#8e4d2b] hover:border-[#F2DACB] transition-all shadow-2xs cursor-pointer active:scale-95"
      >
        <Code2 className="w-3.5 h-3.5 text-[#8e4d2b]" />
        <span>Practice Binary Search</span>
      </button>

      {/* Prompt 2 */}
      <button
        type="button"
        onClick={() => onSelectPrompt('Explain my next topic')}
        className="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-full bg-white/80 hover:bg-white border border-gray-200/80 text-xs font-semibold text-[#0f1b32] hover:text-[#8e4d2b] hover:border-[#F2DACB] transition-all shadow-2xs cursor-pointer active:scale-95"
      >
        <Sparkles className="w-3.5 h-3.5 text-[#8e4d2b]" />
        <span>Explain my next topic</span>
      </button>

      {/* Prompt 3 */}
      <button
        type="button"
        onClick={() => onSelectPrompt('Review my path')}
        className="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-full bg-white/80 hover:bg-white border border-gray-200/80 text-xs font-semibold text-[#0f1b32] hover:text-[#8e4d2b] hover:border-[#F2DACB] transition-all shadow-2xs cursor-pointer active:scale-95"
      >
        <BookOpen className="w-3.5 h-3.5 text-[#8e4d2b]" />
        <span>Review my path</span>
      </button>
    </div>
  );
};
