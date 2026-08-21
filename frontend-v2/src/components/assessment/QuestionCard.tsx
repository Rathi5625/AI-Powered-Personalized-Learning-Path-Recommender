import React from 'react';
import { type AssessmentQuestion } from './dsaQuestions';
import { AnswerOption } from './AnswerOption';

interface QuestionCardProps {
  question: AssessmentQuestion;
  selectedAnswer?: 'A' | 'B' | 'C' | 'D';
  onSelectAnswer: (answerId: 'A' | 'B' | 'C' | 'D') => void;
}

export const QuestionCard: React.FC<QuestionCardProps> = ({
  question,
  selectedAnswer,
  onSelectAnswer,
}) => {
  return (
    <div className="rounded-3xl bg-white/80 backdrop-blur-2xl border border-white/90 p-6 sm:p-10 shadow-[0_12px_40px_rgba(23,35,58,0.06)] text-left space-y-6 sm:space-y-8">
      {/* Question Number Badge */}
      <div>
        <span className="text-xs font-extrabold text-[#8e4d2b] uppercase tracking-wider block mb-2">
          QUESTION {question.questionNumber}
        </span>

        {/* Main Question Text */}
        <h2 className="text-xl sm:text-2xl lg:text-[26px] font-extrabold text-[#0f1b32] tracking-tight leading-snug">
          {question.question}
        </h2>
      </div>

      {/* Answer Options List */}
      <div className="space-y-3">
        {question.options.map((option) => (
          <AnswerOption
            key={option.id}
            id={option.id}
            text={option.text}
            isSelected={selectedAnswer === option.id}
            onSelect={onSelectAnswer}
          />
        ))}
      </div>
    </div>
  );
};
