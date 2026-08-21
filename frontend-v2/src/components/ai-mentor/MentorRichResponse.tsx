import React from 'react';

interface MentorRichResponseProps {
  onVisualExample?: () => void;
  onExplainFurther?: () => void;
}

export const MentorRichResponse: React.FC<MentorRichResponseProps> = ({
  onVisualExample,
  onExplainFurther,
}) => {
  return (
    <div className="space-y-3.5 text-xs sm:text-sm text-[#0f1b32] leading-relaxed text-left">
      <p>
        That&apos;s a great question. The core difference comes down to whether the data is{' '}
        <strong className="font-bold text-[#8e4d2b]">sorted</strong> and the{' '}
        <strong className="font-bold text-[#8e4d2b]">scale</strong> of the data.
      </p>

      {/* Inner Highlighted Golden Rule Card */}
      <div className="p-4 rounded-2xl bg-white/90 border border-[#F2DACB]/80 shadow-2xs space-y-1">
        <h4 className="text-xs font-extrabold text-[#8e4d2b] tracking-tight">
          The Golden Rule:
        </h4>
        <p className="text-xs text-[#53433c] leading-relaxed">
          Use Binary Search when your array is already sorted, and you need to look up items
          frequently. It runs in <strong className="font-bold text-[#0f1b32]">O(log n)</strong> time,
          whereas a linear scan runs in <strong className="font-bold text-[#0f1b32]">O(n)</strong> time.
        </p>
      </div>

      {/* Dictionary Analogy */}
      <p className="text-xs sm:text-sm text-[#53433c] leading-relaxed">
        Imagine looking for a word in a dictionary. A linear scan means checking every single word
        starting from page 1. Binary search means opening the dictionary to the middle, deciding if
        your word is in the first or second half, and repeating.
      </p>

      {/* Action Buttons */}
      <div className="flex items-center gap-2 pt-1 flex-wrap">
        <button
          type="button"
          onClick={onVisualExample}
          className="px-3.5 py-1.5 rounded-xl bg-[#FAF4F0] hover:bg-[#F2DACB]/60 text-[#8e4d2b] font-bold text-xs border border-[#F2DACB] transition-colors cursor-pointer"
        >
          Try a visual example
        </button>

        <button
          type="button"
          onClick={onExplainFurther}
          className="px-3.5 py-1.5 rounded-xl bg-white/80 hover:bg-white text-gray-700 font-bold text-xs border border-gray-200 transition-colors cursor-pointer"
        >
          Explain Further
        </button>
      </div>
    </div>
  );
};
