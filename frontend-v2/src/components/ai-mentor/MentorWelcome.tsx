import React from 'react';
import { Sparkles, ArrowRight, Target, Compass, Code2, Award, Clock } from 'lucide-react';

interface MentorWelcomeProps {
  onStartTopic: (topic: string) => void;
  onWhyRecommendation: () => void;
  onSelectPrompt: (prompt: string) => void;
}

const CONTEXT_PILLS = [
  { label: 'CURRENT GOAL', value: 'Software Engineer', icon: Target },
  { label: 'TARGET ROLE', value: 'Full Stack Developer', icon: Compass },
  { label: 'CURRENT FOCUS', value: 'DSA', icon: Code2 },
  { label: 'SKILL LEVEL', value: 'Intermediate', icon: Award },
  { label: 'WEEKLY COMMITMENT', value: '10 hrs/week', icon: Clock },
];

const SUGGESTED_PROMPTS = [
  "What's the best thing to learn today?",
  'Explain my learning path',
  'Why was Binary Search recommended?',
  'Help me prepare for placements',
  'Create a DSA practice plan',
  'Review my current skills',
  'How am I progressing?',
];

export const MentorWelcome: React.FC<MentorWelcomeProps> = ({
  onStartTopic,
  onWhyRecommendation,
  onSelectPrompt,
}) => {
  return (
    <div className="space-y-6 select-none text-left">
      {/* Welcome Greeting */}
      <div className="space-y-1.5">
        <h2 className="text-xl sm:text-2xl font-extrabold text-[#0f1b32] tracking-tight">
          Hey Parth 👋
        </h2>
        <p className="text-xs sm:text-sm text-[#53433c] font-normal leading-relaxed">
          I’m your LearnAI Mentor. I know your goals, skills, learning path, and recent progress — so
          let’s figure out what to learn next.
        </p>
      </div>

      {/* 5 Compact Context Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-2.5">
        {CONTEXT_PILLS.map((pill) => {
          const Icon = pill.icon;
          return (
            <div
              key={pill.label}
              className="p-3 rounded-2xl bg-white/80 border border-white/90 shadow-2xs space-y-1"
            >
              <div className="flex items-center gap-1.5 text-gray-400">
                <Icon className="w-3 h-3 text-[#8e4d2b]" />
                <span className="text-[9px] font-extrabold tracking-wider">{pill.label}</span>
              </div>
              <span className="text-xs font-bold text-[#0f1b32] block truncate">
                {pill.value}
              </span>
            </div>
          );
        })}
      </div>

      {/* Personalized AI Insight Card */}
      <div className="rounded-3xl bg-gradient-to-br from-[#FAF4F0] via-white to-[#FAF4F0] border border-[#F2DACB] p-5 sm:p-6 shadow-2xs space-y-4">
        <div className="flex items-center gap-2">
          <div className="w-7 h-7 rounded-xl bg-[#ffdbcb]/60 border border-[#d98b63]/30 flex items-center justify-center text-[#8e4d2b]">
            <Sparkles className="w-3.5 h-3.5 text-[#8e4d2b]" />
          </div>
          <span className="text-xs font-extrabold text-[#8e4d2b] uppercase tracking-wider">
            Based on your recent progress...
          </span>
        </div>

        <p className="text-xs sm:text-sm text-[#0f1b32] font-semibold leading-relaxed">
          You&apos;ve improved your Java skills recently, and your latest DSA assessment (78%) suggests
          you&apos;re ready to move from <span className="font-bold text-[#8e4d2b]">Arrays</span> into{' '}
          <span className="font-bold text-[#8e4d2b]">Binary Search</span>.
        </p>

        <div className="flex items-center gap-3 pt-1 flex-wrap">
          <button
            type="button"
            onClick={() => onStartTopic('Binary Search')}
            className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-[#8e4d2b] hover:bg-[#783e20] text-white text-xs font-bold transition-all shadow-2xs cursor-pointer active:scale-95"
          >
            <span>Start Binary Search</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </button>

          <button
            type="button"
            onClick={onWhyRecommendation}
            className="px-3.5 py-2.5 rounded-xl bg-white/80 hover:bg-white border border-gray-200 text-xs font-bold text-[#53433c] hover:text-[#0f1b32] transition-colors cursor-pointer"
          >
            Why this recommendation?
          </button>
        </div>
      </div>

      {/* Suggested Prompts List */}
      <div className="space-y-2.5 pt-2">
        <span className="text-[11px] font-bold text-gray-400 uppercase tracking-wider block">
          Suggested questions
        </span>
        <div className="flex items-center gap-2 flex-wrap">
          {SUGGESTED_PROMPTS.map((prompt) => (
            <button
              key={prompt}
              type="button"
              onClick={() => onSelectPrompt(prompt)}
              className="px-3.5 py-2 rounded-xl bg-white/80 hover:bg-[#FAF4F0] border border-gray-200/80 text-xs font-semibold text-[#0f1b32] hover:border-[#F2DACB] transition-all shadow-2xs cursor-pointer text-left active:scale-[0.98]"
            >
              {prompt}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};
