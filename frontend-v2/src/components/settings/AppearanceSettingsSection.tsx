import React, { useState } from 'react';
import { Sun, Moon, Monitor } from 'lucide-react';

interface AppearanceSettingsSectionProps {
  onThemeChanged?: (theme: string) => void;
}

export const AppearanceSettingsSection: React.FC<AppearanceSettingsSectionProps> = ({
  onThemeChanged,
}) => {
  const [theme, setTheme] = useState<'Light' | 'Dark' | 'System'>('Light');
  const [reduceEffects, setReduceEffects] = useState(false);

  const handleSelectTheme = (t: 'Light' | 'Dark' | 'System') => {
    setTheme(t);
    onThemeChanged?.(t);
  };

  return (
    <section id="appearance" className="space-y-4 text-left">
      <div className="space-y-1">
        <h2 className="text-xl sm:text-2xl font-extrabold text-[#0f1b32] tracking-tight">
          Appearance
        </h2>
        <p className="text-xs text-[#53433c] font-normal leading-relaxed">
          Customize your dashboard visual theme and display preferences.
        </p>
      </div>

      <div className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] space-y-6">
        {/* Theme Selectors */}
        <div className="space-y-2.5">
          <label className="text-xs font-bold text-gray-500 uppercase tracking-wider block">
            Theme
          </label>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            {/* Light */}
            <div
              onClick={() => handleSelectTheme('Light')}
              className={`
                flex items-center justify-center gap-2 p-3.5 rounded-2xl border transition-all cursor-pointer shadow-2xs
                ${
                  theme === 'Light'
                    ? 'bg-[#FAF4F0] border-[#8e4d2b] text-[#8e4d2b] font-bold'
                    : 'bg-white/80 border-gray-200/80 text-gray-600 hover:bg-white'
                }
              `}
            >
              <Sun className="w-4 h-4" />
              <span className="text-xs">Light</span>
            </div>

            {/* Dark */}
            <div
              onClick={() => handleSelectTheme('Dark')}
              className={`
                flex items-center justify-center gap-2 p-3.5 rounded-2xl border transition-all cursor-pointer shadow-2xs
                ${
                  theme === 'Dark'
                    ? 'bg-[#FAF4F0] border-[#8e4d2b] text-[#8e4d2b] font-bold'
                    : 'bg-white/80 border-gray-200/80 text-gray-600 hover:bg-white'
                }
              `}
            >
              <Moon className="w-4 h-4" />
              <span className="text-xs">Dark</span>
            </div>

            {/* System */}
            <div
              onClick={() => handleSelectTheme('System')}
              className={`
                flex items-center justify-center gap-2 p-3.5 rounded-2xl border transition-all cursor-pointer shadow-2xs
                ${
                  theme === 'System'
                    ? 'bg-[#FAF4F0] border-[#8e4d2b] text-[#8e4d2b] font-bold'
                    : 'bg-white/80 border-gray-200/80 text-gray-600 hover:bg-white'
                }
              `}
            >
              <Monitor className="w-4 h-4" />
              <span className="text-xs">System</span>
            </div>
          </div>
        </div>

        {/* Reduce Visual Effects */}
        <div className="flex items-center justify-between pt-2 border-t border-gray-100/80">
          <div>
            <span className="text-xs font-bold text-[#0f1b32] block">
              Reduce Visual Effects
            </span>
            <span className="text-[11px] text-gray-400 font-medium block">
              Minimize glassmorphism blur and subtle particle backgrounds
            </span>
          </div>

          <button
            type="button"
            role="switch"
            aria-checked={reduceEffects}
            onClick={() => setReduceEffects(!reduceEffects)}
            className={`
              relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none shadow-2xs
              ${reduceEffects ? 'bg-[#8e4d2b]' : 'bg-gray-200'}
            `}
          >
            <span
              className={`
                pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow-sm ring-0 transition duration-200 ease-in-out
                ${reduceEffects ? 'translate-x-5' : 'translate-x-0'}
              `}
            />
          </button>
        </div>
      </div>
    </section>
  );
};
