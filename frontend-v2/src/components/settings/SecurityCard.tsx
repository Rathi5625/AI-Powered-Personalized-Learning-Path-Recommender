import React, { useState } from 'react';
import { Shield, ChevronRight, ArrowRight } from 'lucide-react';

interface SecurityCardProps {
  onChangePassword?: () => void;
  onToggle2FA?: (enabled: boolean) => void;
  onViewSessions?: () => void;
}

export const SecurityCard: React.FC<SecurityCardProps> = ({
  onChangePassword,
  onToggle2FA,
  onViewSessions,
}) => {
  const [twoFactorEnabled, setTwoFactorEnabled] = useState(false);

  const handleToggle = () => {
    const nextState = !twoFactorEnabled;
    setTwoFactorEnabled(nextState);
    onToggle2FA?.(nextState);
  };

  return (
    <div className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-5 flex-1 flex flex-col justify-between select-none">
      <div className="space-y-4">
        {/* Header */}
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-xl bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#8e4d2b]">
            <Shield className="w-4 h-4 text-[#8e4d2b]" />
          </div>
          <h3 className="text-base font-extrabold text-[#0f1b32] tracking-tight">
            Security
          </h3>
        </div>

        {/* Password Row */}
        <div
          onClick={onChangePassword}
          className="flex items-center justify-between p-3.5 rounded-2xl bg-white/90 border border-gray-100/90 shadow-2xs hover:bg-[#FAF4F0]/60 transition-colors cursor-pointer"
        >
          <div>
            <span className="text-xs font-bold text-[#0f1b32] block">Password</span>
            <span className="text-[11px] text-gray-400 font-medium block">
              Last changed 3 months ago
            </span>
          </div>
          <ChevronRight className="w-4 h-4 text-gray-400" />
        </div>

        {/* Two-Factor Auth Toggle */}
        <div className="flex items-center justify-between pt-1">
          <div>
            <span className="text-xs font-bold text-[#0f1b32] block">
              Two-Factor Auth
            </span>
            <span className="text-[11px] text-gray-400 font-medium block">
              Adds extra security
            </span>
          </div>

          {/* Toggle Switch */}
          <button
            type="button"
            role="switch"
            aria-checked={twoFactorEnabled}
            onClick={handleToggle}
            className={`
              relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none shadow-2xs
              ${twoFactorEnabled ? 'bg-[#8e4d2b]' : 'bg-gray-200'}
            `}
          >
            <span
              className={`
                pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow-sm ring-0 transition duration-200 ease-in-out
                ${twoFactorEnabled ? 'translate-x-5' : 'translate-x-0'}
              `}
            />
          </button>
        </div>
      </div>

      {/* Bottom Link: View Active Sessions */}
      <div className="pt-3 border-t border-gray-100/80">
        <button
          type="button"
          onClick={onViewSessions}
          className="inline-flex items-center gap-1 text-xs font-bold text-[#8e4d2b] hover:text-[#783e20] transition-colors cursor-pointer"
        >
          <span>View active sessions</span>
          <ArrowRight className="w-3.5 h-3.5" />
        </button>
      </div>
    </div>
  );
};
