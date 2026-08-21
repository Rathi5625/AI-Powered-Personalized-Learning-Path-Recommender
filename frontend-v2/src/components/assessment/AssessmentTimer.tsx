import React, { useState, useEffect } from 'react';
import { Clock } from 'lucide-react';

interface AssessmentTimerProps {
  initialSeconds?: number;
  onTimeUp?: () => void;
}

export const AssessmentTimer: React.FC<AssessmentTimerProps> = ({
  initialSeconds = 18 * 60 + 42, // 18:42 default
  onTimeUp,
}) => {
  const [secondsLeft, setSecondsLeft] = useState(initialSeconds);

  useEffect(() => {
    if (secondsLeft <= 0) {
      onTimeUp?.();
      return;
    }

    const interval = setInterval(() => {
      setSecondsLeft((prev) => {
        if (prev <= 1) {
          clearInterval(interval);
          onTimeUp?.();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(interval);
  }, [onTimeUp]);

  const formatTime = (totalSec: number) => {
    const mins = Math.floor(totalSec / 60);
    const secs = totalSec % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  return (
    <div
      aria-label="Time Remaining"
      className="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-full bg-white/80 backdrop-blur-md border border-[#F2DACB]/80 text-[#8e4d2b] font-bold text-xs sm:text-sm shadow-2xs select-none"
    >
      <Clock className="w-3.5 h-3.5 text-[#8e4d2b]" />
      <span>{formatTime(secondsLeft)} remaining</span>
    </div>
  );
};
