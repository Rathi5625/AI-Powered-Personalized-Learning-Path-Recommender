import React, { useState, useRef } from 'react';
import { Paperclip, Mic, Send, X, Plus } from 'lucide-react';

interface MentorComposerProps {
  onSendMessage: (text: string) => void;
  onSelectContextChip: (chip: string) => void;
  onMicClick: () => void;
  isThinking?: boolean;
}

export const MentorComposer: React.FC<MentorComposerProps> = ({
  onSendMessage,
  onSelectContextChip,
  onMicClick,
  isThinking = false,
}) => {
  const [text, setText] = useState('');
  const [attachment, setAttachment] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleSubmit = (e?: React.FormEvent) => {
    e?.preventDefault();
    if (!text.trim() || isThinking) return;

    onSendMessage(text.trim());
    setText('');
    setAttachment(null);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit();
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setAttachment(e.target.files[0].name);
    }
  };

  return (
    <div className="space-y-3 pt-2 select-none">
      {/* Context Chips Row */}
      <div className="flex items-center gap-2 flex-wrap">
        <button
          type="button"
          onClick={() => onSelectContextChip('Current Topic: Binary Search')}
          className="inline-flex items-center gap-1 px-3 py-1 rounded-full bg-white/70 hover:bg-white border border-gray-200/80 text-[11px] font-semibold text-[#53433c] hover:text-[#8e4d2b] transition-colors cursor-pointer shadow-2xs"
        >
          <Plus className="w-3 h-3 text-[#8e4d2b]" />
          <span>Current Topic</span>
        </button>

        <button
          type="button"
          onClick={() => onSelectContextChip('My Skills: DSA 61%')}
          className="inline-flex items-center gap-1 px-3 py-1 rounded-full bg-white/70 hover:bg-white border border-gray-200/80 text-[11px] font-semibold text-[#53433c] hover:text-[#8e4d2b] transition-colors cursor-pointer shadow-2xs"
        >
          <Plus className="w-3 h-3 text-[#8e4d2b]" />
          <span>My Skills</span>
        </button>

        <button
          type="button"
          onClick={() => onSelectContextChip('Code Snippet Context')}
          className="inline-flex items-center gap-1 px-3 py-1 rounded-full bg-white/70 hover:bg-white border border-gray-200/80 text-[11px] font-semibold text-[#53433c] hover:text-[#8e4d2b] transition-colors cursor-pointer shadow-2xs"
        >
          <Plus className="w-3 h-3 text-[#8e4d2b]" />
          <span>Code Snippet</span>
        </button>
      </div>

      {/* Attachment Pill if Selected */}
      {attachment && (
        <div className="flex items-center gap-1.5 px-3 py-1 rounded-lg bg-[#FAF4F0] border border-[#F2DACB] text-xs font-semibold text-[#8e4d2b] w-max">
          <Paperclip className="w-3 h-3" />
          <span>{attachment}</span>
          <button
            type="button"
            onClick={() => setAttachment(null)}
            className="p-0.5 hover:text-red-600 transition-colors ml-1"
          >
            <X className="w-3 h-3" />
          </button>
        </div>
      )}

      {/* Composer Input Box */}
      <div className="flex items-center gap-2 p-2 rounded-2xl bg-white/90 border border-gray-200/80 shadow-sm focus-within:ring-2 focus-within:ring-[#8e4d2b]/20 focus-within:border-[#8e4d2b] transition-all">
        {/* Hidden File Input */}
        <input
          type="file"
          ref={fileInputRef}
          onChange={handleFileChange}
          className="hidden"
        />

        {/* Paperclip Button */}
        <button
          type="button"
          aria-label="Add attachment"
          onClick={() => fileInputRef.current?.click()}
          className="p-2 text-gray-400 hover:text-[#8e4d2b] rounded-xl hover:bg-gray-50 transition-colors cursor-pointer"
        >
          <Paperclip className="w-4 h-4" />
        </button>

        {/* Textarea Input */}
        <textarea
          value={text}
          onChange={(e) => setText(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Ask your mentor a question..."
          rows={1}
          className="flex-1 bg-transparent text-xs sm:text-sm text-[#0f1b32] placeholder-gray-400 resize-none focus:outline-none py-1.5 px-1 max-h-24 overflow-y-auto scrollbar-none"
        />

        {/* Microphone Button */}
        <button
          type="button"
          aria-label="Voice input"
          onClick={onMicClick}
          className="p-2 text-gray-400 hover:text-[#8e4d2b] rounded-xl hover:bg-gray-50 transition-colors cursor-pointer"
        >
          <Mic className="w-4 h-4" />
        </button>

        {/* Send Button */}
        <button
          type="button"
          disabled={!text.trim() || isThinking}
          onClick={() => handleSubmit()}
          aria-label="Send message"
          className="p-2.5 rounded-xl bg-[#8e4d2b] hover:bg-[#783e20] text-white disabled:opacity-40 disabled:pointer-events-none shadow-2xs transition-all cursor-pointer active:scale-95"
        >
          <Send className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
};
