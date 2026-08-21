import React, { useState } from 'react';

interface AccessibilitySettingsSectionProps {
  onToggle?: (setting: string, val: boolean) => void;
}

export const AccessibilitySettingsSection: React.FC<AccessibilitySettingsSectionProps> = ({
  onToggle,
}) => {
  const [reduceMotion, setReduceMotion] = useState(false);
  const [largerText, setLargerText] = useState(false);
  const [highContrast, setHighContrast] = useState(false);

  return (
    <section id="accessibility" className="space-y-4 text-left">
      <div className="space-y-1">
        <h2 className="text-xl sm:text-2xl font-extrabold text-[#0f1b32] tracking-tight">
          Accessibility
        </h2>
        <p className="text-xs text-[#53433c] font-normal leading-relaxed">
          Adjust text contrast, typography scaling, and animation behaviors.
        </p>
      </div>

      <div className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] space-y-5">
        {/* Reduce Motion */}
        <div className="flex items-center justify-between gap-4 pb-4 border-b border-gray-100/80">
          <div>
            <span className="text-xs font-bold text-[#0f1b32] block">
              Reduce Motion
            </span>
            <span className="text-[11px] text-gray-400 font-medium block">
              Disable decorative animations and page slide transitions
            </span>
          </div>

          <button
            type="button"
            role="switch"
            aria-checked={reduceMotion}
            onClick={() => {
              const val = !reduceMotion;
              setReduceMotion(val);
              onToggle?.('Reduce Motion', val);
            }}
            className={`
              relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none shadow-2xs
              ${reduceMotion ? 'bg-[#8e4d2b]' : 'bg-gray-200'}
            `}
          >
            <span
              className={`
                pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow-sm ring-0 transition duration-200 ease-in-out
                ${reduceMotion ? 'translate-x-5' : 'translate-x-0'}
              `}
            />
          </button>
        </div>

        {/* Larger Text */}
        <div className="flex items-center justify-between gap-4 pb-4 border-b border-gray-100/80">
          <div>
            <span className="text-xs font-bold text-[#0f1b32] block">
              Larger Text
            </span>
            <span className="text-[11px] text-gray-400 font-medium block">
              Increase base font sizing across all learning dashboards
            </span>
          </div>

          <button
            type="button"
            role="switch"
            aria-checked={largerText}
            onClick={() => {
              const val = !largerText;
              setLargerText(val);
              onToggle?.('Larger Text', val);
            }}
            className={`
              relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none shadow-2xs
              ${largerText ? 'bg-[#8e4d2b]' : 'bg-gray-200'}
            `}
          >
            <span
              className={`
                pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow-sm ring-0 transition duration-200 ease-in-out
                ${largerText ? 'translate-x-5' : 'translate-x-0'}
              `}
            />
          </button>
        </div>

        {/* High Contrast */}
        <div className="flex items-center justify-between gap-4">
          <div>
            <span className="text-xs font-bold text-[#0f1b32] block">
              High Contrast
            </span>
            <span className="text-[11px] text-gray-400 font-medium block">
              Enhance border definition and text sharpness
            </span>
          </div>

          <button
            type="button"
            role="switch"
            aria-checked={highContrast}
            onClick={() => {
              const val = !highContrast;
              setHighContrast(val);
              onToggle?.('High Contrast', val);
            }}
            className={`
              relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none shadow-2xs
              ${highContrast ? 'bg-[#8e4d2b]' : 'bg-gray-200'}
            `}
          >
            <span
              className={`
                pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow-sm ring-0 transition duration-200 ease-in-out
                ${highContrast ? 'translate-x-5' : 'translate-x-0'}
              `}
            />
          </button>
        </div>
      </div>
    </section>
  );
};
