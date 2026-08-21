import React, { useState } from 'react';

const FORMATS = ['Video', 'Reading', 'Hands-on Projects', 'Practice'];
const PACES = ['Relaxed', 'Balanced', 'Intensive'];

interface LearningSettingsSectionProps {
  onSettingsChanged?: () => void;
}

export const LearningSettingsSection: React.FC<LearningSettingsSectionProps> = ({
  onSettingsChanged,
}) => {
  const [selectedPace, setSelectedPace] = useState('Balanced');
  const [selectedFormats, setSelectedFormats] = useState([
    'Video',
    'Hands-on Projects',
    'Practice',
    'Reading',
  ]);
  const [hours, setHours] = useState(10);

  const toggleFormat = (fmt: string) => {
    if (selectedFormats.includes(fmt)) {
      setSelectedFormats(selectedFormats.filter((f) => f !== fmt));
    } else {
      setSelectedFormats([...selectedFormats, fmt]);
    }
    onSettingsChanged?.();
  };

  return (
    <section id="learning" className="space-y-4 text-left">
      <div className="space-y-1">
        <h2 className="text-xl sm:text-2xl font-extrabold text-[#0f1b32] tracking-tight">
          Learning Preferences
        </h2>
        <p className="text-xs text-[#53433c] font-normal leading-relaxed">
          Customize how AI generates and adapts your learning paths.
        </p>
      </div>

      <div className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] space-y-6">
        {/* Learning Pace */}
        <div className="space-y-2.5">
          <label className="text-xs font-bold text-gray-500 uppercase tracking-wider block">
            Learning Pace
          </label>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            {PACES.map((pace) => {
              const isSelected = selectedPace === pace;

              return (
                <div
                  key={pace}
                  onClick={() => {
                    setSelectedPace(pace);
                    onSettingsChanged?.();
                  }}
                  className={`
                    p-3.5 rounded-2xl border transition-all cursor-pointer shadow-2xs text-center
                    ${
                      isSelected
                        ? 'bg-[#FAF4F0] border-[#8e4d2b] text-[#8e4d2b] font-bold'
                        : 'bg-white/80 border-gray-200/80 text-gray-600 hover:bg-white'
                    }
                  `}
                >
                  <span className="text-xs">{pace}</span>
                </div>
              );
            })}
          </div>
        </div>

        {/* Preferred Formats */}
        <div className="space-y-2.5">
          <label className="text-xs font-bold text-gray-500 uppercase tracking-wider block">
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

        {/* Weekly Commitment Slider */}
        <div className="space-y-2.5 pt-2 border-t border-gray-100/80">
          <div className="flex items-center justify-between text-xs font-bold">
            <span className="text-gray-500 uppercase tracking-wider">Weekly Commitment</span>
            <span className="text-[#8e4d2b] text-sm">{hours} hrs/week</span>
          </div>

          <input
            type="range"
            min={2}
            max={40}
            step={2}
            value={hours}
            onChange={(e) => {
              setHours(Number(e.target.value));
              onSettingsChanged?.();
            }}
            className="w-full accent-[#8e4d2b] cursor-pointer"
          />
        </div>
      </div>
    </section>
  );
};
