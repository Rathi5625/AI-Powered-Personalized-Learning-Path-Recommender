import React from 'react';
import { Flame } from 'lucide-react';

interface LearningStreakCardProps {
  streakDays?: number;
}

export const LearningStreakCard: React.FC<LearningStreakCardProps> = ({
  streakDays = 7,
}) => {
  return (
    <div className="bg-white/80 backdrop-blur-xl border border-white/90 rounded-2xl sm:rounded-3xl p-4 sm:p-5 shadow-[0_16px_40px_rgba(15,27,50,0.04)] flex items-center gap-3.5 text-left">
      <div className="w-10 h-10 rounded-2xl bg-[#ffdbcb]/70 border border-[#d98b63]/30 flex items-center justify-center text-[#8e4d2b] shrink-0 shadow-2xs">
        <Flame className="w-5 h-5 fill-[#8e4d2b]" />
      </div>
      <div>
        <h4 className="text-xs sm:text-sm font-bold text-[#0f1b32] tracking-tight">
          {streakDays} day learning streak
        </h4>
        <p className="text-xs text-gray-500 font-normal">
          Keep going!
        </p>
      </div>
    </div>
  );
};
