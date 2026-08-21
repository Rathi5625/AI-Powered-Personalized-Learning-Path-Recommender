import React from 'react';

interface QuestionMapProps {
  totalQuestions: number;
  currentQuestion: number;
  answers: Record<number, 'A' | 'B' | 'C' | 'D'>;
  onSelectQuestion: (questionNumber: number) => void;
}

export const QuestionMap: React.FC<QuestionMapProps> = ({
  totalQuestions,
  currentQuestion,
  answers,
  onSelectQuestion,
}) => {
  const questionNumbers = Array.from({ length: totalQuestions }, (_, i) => i + 1);

  return (
    <>
      {/* Desktop Floating Right Map */}
      <aside
        aria-label="Question Navigation Map"
        className="hidden xl:flex flex-col items-center fixed right-8 top-36 w-16 bg-white/75 backdrop-blur-2xl border border-white/90 rounded-3xl p-3 py-4 shadow-[0_8px_32px_rgba(23,35,58,0.05)] z-30 select-none max-h-[calc(100vh-180px)] overflow-y-auto scrollbar-none"
      >
        {/* Vertical MAP Label */}
        <span className="text-[10px] font-bold text-gray-400 tracking-widest uppercase mb-3 block">
          MAP
        </span>

        {/* Question Circles List */}
        <div className="flex flex-col items-center gap-2.5 w-full">
          {questionNumbers.map((num) => {
            const isCurrent = currentQuestion === num;
            const isAnswered = answers[num] !== undefined;

            return (
              <button
                key={num}
                type="button"
                onClick={() => onSelectQuestion(num)}
                aria-label={`Go to question ${num}`}
                className={`
                  w-8 h-8 rounded-full flex items-center justify-center transition-all duration-200 cursor-pointer text-xs
                  ${
                    isCurrent
                      ? 'w-9 h-9 bg-white text-[#8e4d2b] border-2 border-[#d98b63] shadow-md ring-4 ring-[#ffdbcb]/50 font-extrabold scale-110'
                      : isAnswered
                      ? 'bg-[#ffdbcb]/60 hover:bg-[#ffdbcb] text-[#8e4d2b] font-bold shadow-2xs'
                      : 'bg-white/60 hover:bg-white text-gray-500 font-medium border border-gray-100'
                  }
                `}
              >
                {num}
              </button>
            );
          })}
        </div>
      </aside>

      {/* Mobile / Tablet Horizontal Question Selector */}
      <div className="xl:hidden w-full overflow-x-auto py-2 scrollbar-none select-none">
        <div className="flex items-center gap-2 px-1 min-w-max">
          {questionNumbers.map((num) => {
            const isCurrent = currentQuestion === num;
            const isAnswered = answers[num] !== undefined;

            return (
              <button
                key={num}
                type="button"
                onClick={() => onSelectQuestion(num)}
                className={`
                  w-8 h-8 rounded-full flex items-center justify-center transition-all duration-150 cursor-pointer text-xs shrink-0
                  ${
                    isCurrent
                      ? 'bg-[#8e4d2b] text-white font-bold shadow-sm'
                      : isAnswered
                      ? 'bg-[#ffdbcb] text-[#8e4d2b] font-bold'
                      : 'bg-white/70 text-gray-500 font-medium border border-gray-200/70'
                  }
                `}
              >
                {num}
              </button>
            );
          })}
        </div>
      </div>
    </>
  );
};
