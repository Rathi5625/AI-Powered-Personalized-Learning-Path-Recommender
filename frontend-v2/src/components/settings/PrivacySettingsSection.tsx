import React, { useState } from 'react';
import { Download } from 'lucide-react';

interface PrivacySettingsSectionProps {
  onDownloadData?: () => void;
  onSettingChanged?: () => void;
}

export const PrivacySettingsSection: React.FC<PrivacySettingsSectionProps> = ({
  onDownloadData,
  onSettingChanged,
}) => {
  const [visibility, setVisibility] = useState<'Private' | 'Connections' | 'Public'>('Connections');
  const [personalizedRecs, setPersonalizedRecs] = useState(true);
  const [analyticsEnabled, setAnalyticsEnabled] = useState(true);

  return (
    <section id="privacy" className="space-y-4 text-left">
      <div className="space-y-1">
        <h2 className="text-xl sm:text-2xl font-extrabold text-[#0f1b32] tracking-tight">
          Privacy &amp; Data
        </h2>
        <p className="text-xs text-[#53433c] font-normal leading-relaxed">
          Control how your progress is shared and how AI uses your activity data.
        </p>
      </div>

      <div className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] space-y-6">
        {/* Profile Visibility */}
        <div className="space-y-2.5">
          <label className="text-xs font-bold text-gray-500 uppercase tracking-wider block">
            Profile Visibility
          </label>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            {(['Private', 'Connections', 'Public'] as const).map((v) => {
              const isSelected = visibility === v;

              return (
                <div
                  key={v}
                  onClick={() => {
                    setVisibility(v);
                    onSettingChanged?.();
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
                  <span className="text-xs">{v}</span>
                </div>
              );
            })}
          </div>
        </div>

        {/* Personalized Recommendations Toggle */}
        <div className="flex items-center justify-between gap-4 pb-4 border-b border-gray-100/80">
          <div>
            <span className="text-xs font-bold text-[#0f1b32] block">
              Personalized Recommendations
            </span>
            <span className="text-[11px] text-gray-400 font-medium block">
              Allow LearnAI to tailor modules and assessments based on quiz outcomes
            </span>
          </div>

          <button
            type="button"
            role="switch"
            aria-checked={personalizedRecs}
            onClick={() => {
              setPersonalizedRecs(!personalizedRecs);
              onSettingChanged?.();
            }}
            className={`
              relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none shadow-2xs
              ${personalizedRecs ? 'bg-[#8e4d2b]' : 'bg-gray-200'}
            `}
          >
            <span
              className={`
                pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow-sm ring-0 transition duration-200 ease-in-out
                ${personalizedRecs ? 'translate-x-5' : 'translate-x-0'}
              `}
            />
          </button>
        </div>

        {/* Learning Analytics Toggle */}
        <div className="flex items-center justify-between gap-4 pb-4 border-b border-gray-100/80">
          <div>
            <span className="text-xs font-bold text-[#0f1b32] block">
              Learning Analytics
            </span>
            <span className="text-[11px] text-gray-400 font-medium block">
              Track daily streaks, session times, and cognitive retention metrics
            </span>
          </div>

          <button
            type="button"
            role="switch"
            aria-checked={analyticsEnabled}
            onClick={() => {
              setAnalyticsEnabled(!analyticsEnabled);
              onSettingChanged?.();
            }}
            className={`
              relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none shadow-2xs
              ${analyticsEnabled ? 'bg-[#8e4d2b]' : 'bg-gray-200'}
            `}
          >
            <span
              className={`
                pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow-sm ring-0 transition duration-200 ease-in-out
                ${analyticsEnabled ? 'translate-x-5' : 'translate-x-0'}
              `}
            />
          </button>
        </div>

        {/* Download Data Button */}
        <div className="pt-1">
          <button
            type="button"
            onClick={onDownloadData}
            className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-white/90 hover:bg-[#FAF4F0] border border-gray-200/80 text-xs font-bold text-[#0f1b32] transition-colors cursor-pointer shadow-2xs"
          >
            <Download className="w-4 h-4 text-[#8e4d2b]" />
            <span>Download My Data</span>
          </button>
        </div>
      </div>
    </section>
  );
};
