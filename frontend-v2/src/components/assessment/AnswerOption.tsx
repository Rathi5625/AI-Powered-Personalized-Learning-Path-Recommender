import React from 'react';
import { CheckCircle2 } from 'lucide-react';

interface AnswerOptionProps {
  id: 'A' | 'B' | 'C' | 'D';
  text: string;
  isSelected: boolean;
  onSelect: (id: 'A' | 'B' | 'C' | 'D') => void;
}

export const AnswerOption: React.FC<AnswerOptionProps> = ({
  id,
  text,
  isSelected,
  onSelect,
}) => {
  return (
    <button
      type="button"
      role="button"
      aria-pressed={isSelected}
      onClick={() => onSelect(id)}
      className={`
        w-full p-4 sm:p-5 rounded-2xl text-left flex items-center justify-between gap-3 transition-all duration-200 cursor-pointer select-none
        ${
          isSelected
            ? 'bg-[#FAF4F0] border border-[#d98b63]/70 shadow-sm'
            : 'bg-white/60 hover:bg-white/95 border border-gray-100/90 shadow-2xs hover:shadow-sm hover:-translate-y-0.5'
        }
      `}
    >
      {/* Left: Letter Circle + Text */}
      <div className="flex items-center gap-3.5 min-w-0">
        <div
          className={`
            w-8 h-8 rounded-full font-bold text-xs flex items-center justify-center shrink-0 transition-colors
            ${
              isSelected
                ? 'bg-[#d98b63] text-white border border-[#d98b63]'
                : 'bg-white text-gray-700 border border-gray-200'
            }
          `}
        >
          {id}
        </div>
        <span className="text-xs sm:text-sm font-semibold text-[#0f1b32] truncate">
          {text}
        </span>
      </div>

      {/* Right Checkmark if Selected */}
      {isSelected && (
        <CheckCircle2 className="w-5 h-5 text-[#8e4d2b] shrink-0 fill-[#FAF4F0]" />
      )}
    </button>
  );
};
