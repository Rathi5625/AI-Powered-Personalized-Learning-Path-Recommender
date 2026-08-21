import React, { useState } from 'react';

const FORMATS = ['Video', 'Hands-on Projects', 'Practice', 'Reading'];
const PACES = ['Relaxed', 'Balanced', 'Intensive'];
const DAYS = [
  { label: 'M', id: 'mon', defaultSelected: true },
  { label: 'T', id: 'tue', defaultSelected: true },
  { label: 'W', id: 'wed', defaultSelected: true },
  { label: 'T', id: 'thu', defaultSelected: true },
  { label: 'F', id: 'fri', defaultSelected: false },
  { label: 'S', id: 'sat', defaultSelected: true },
  { label: 'S', id: 'sun', defaultSelected: false },
];

export const LearningPreferencesCard: React.FC = () => {
  const [selectedFormats, setSelectedFormats] = useState<string[]>([
    'Video',
    'Hands-on Projects',
    'Practice',
    'Reading',
  ]);
  const [selectedPace, setSelectedPace] = useState<string>('Balanced');
  const [selectedDays, setSelectedDays] = useState<string[]>([
    'mon',
    'tue',
    'wed',
    'thu',
    'sat',
  ]);

  const toggleFormat = (fmt: string) => {
    if (selectedFormats.includes(fmt)) {
      setSelectedFormats(selectedFormats.filter((f) => f !== fmt));
    } else {
      setSelectedFormats([...selectedFormats, fmt]);
    }
  };

  const toggleDay = (dayId: string) => {
    if (selectedDays.includes(dayId)) {
      setSelectedDays(selectedDays.filter((d) => d !== dayId));
    } else {
      setSelectedDays([...selectedDays, dayId]);
    }
  };

  return (
    <section
      aria-label="Learning Preferences"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-6 select-none"
    >
      <h3 className="text-base sm:text-lg font-extrabold text-[#0f1b32] tracking-tight">
        Learning Preferences
      </h3>

      {/* Preferred Formats */}
      <div className="space-y-2.5">
        <label className="text-[11px] font-bold text-gray-500 uppercase tracking-wider block">
          Preferred Formats
        </label>
        <div className="flex items-center gap-2 flex-wrap">
          {FORMATS.map((fmt) => {
            const isSelected = selectedFormats.includes(fmt);

            return (
              <button
                key={fmt}
                type="button"
                onClick={() => toggleFormat(fmt)}
                className={`
                  px-3.5 py-1.5 rounded-full text-xs font-bold transition-all cursor-pointer shadow-2xs
                  ${
                    isSelected
                      ? 'bg-[#FAF4F0] border border-[#F2DACB] text-[#8e4d2b]'
                      : 'bg-white/80 border border-gray-200 text-gray-500 hover:text-[#0f1b32]'
                  }
                `}
              >
                {fmt}
              </button>
            );
          })}
        </div>
      </div>

      {/* Pace Radio Choices */}
      <div className="space-y-2.5">
        <label className="text-[11px] font-bold text-gray-500 uppercase tracking-wider block">
          Pace
        </label>
        <div className="space-y-2">
          {PACES.map((pace) => {
            const isSelected = selectedPace === pace;

            return (
              <div
                key={pace}
                onClick={() => setSelectedPace(pace)}
                className={`
                  flex items-center gap-3 p-3 rounded-2xl border transition-all cursor-pointer shadow-2xs
                  ${
                    isSelected
                      ? 'bg-[#FAF4F0]/80 border-[#8e4d2b] text-[#0f1b32]'
                      : 'bg-white/80 border-gray-200/80 text-gray-600 hover:bg-white'
                  }
                `}
              >
                <div
                  className={`w-4 h-4 rounded-full border flex items-center justify-center ${
                    isSelected ? 'border-[#8e4d2b]' : 'border-gray-300'
                  }`}
                >
                  {isSelected && <div className="w-2 h-2 rounded-full bg-[#8e4d2b]" />}
                </div>
                <span className="text-xs font-bold">{pace}</span>
              </div>
            );
          })}
        </div>
      </div>

      {/* Weekly Commitment */}
      <div className="space-y-3 pt-1 border-t border-gray-100/80">
        <label className="text-[11px] font-bold text-gray-500 uppercase tracking-wider block">
          Weekly Commitment
        </label>

        {/* Target Hours Box */}
        <div className="flex items-center justify-between p-3 rounded-2xl bg-white/90 border border-gray-100/90 shadow-2xs text-xs font-bold">
          <span className="text-gray-500">Target Hours</span>
          <span className="text-[#0f1b32] text-sm">10 hrs</span>
        </div>

        {/* Available Days */}
        <div className="space-y-1.5">
          <span className="text-[10px] text-gray-400 font-semibold block">Available Days</span>
          <div className="flex items-center justify-between gap-1.5">
            {DAYS.map((day, idx) => {
              const isSelected = selectedDays.includes(day.id);

              return (
                <button
                  key={`${day.id}-${idx}`}
                  type="button"
                  onClick={() => toggleDay(day.id)}
                  className={`
                    w-8 h-8 rounded-full text-xs font-extrabold flex items-center justify-center transition-all cursor-pointer shadow-2xs
                    ${
                      isSelected
                        ? 'bg-[#8e4d2b] text-white'
                        : 'bg-[#d8e2ff]/60 text-gray-400 hover:bg-[#d8e2ff]'
                    }
                  `}
                >
                  {day.label}
                </button>
              );
            })}
          </div>
        </div>
      </div>
    </section>
  );
};
