import React from 'react';
import { LogOut, Trash2 } from 'lucide-react';

interface AccountSettingsCardProps {
  onLogOut?: () => void;
  onDeleteAccount?: () => void;
}

export const AccountSettingsCard: React.FC<AccountSettingsCardProps> = ({
  onLogOut,
  onDeleteAccount,
}) => {
  return (
    <section
      aria-label="Account Settings"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-3.5 select-none"
    >
      <h3 className="text-base sm:text-lg font-extrabold text-[#0f1b32] tracking-tight">
        Account Settings
      </h3>

      <div className="space-y-2.5">
        {/* Log Out Button */}
        <button
          type="button"
          onClick={onLogOut}
          className="w-full py-3 px-4 rounded-2xl bg-white/90 hover:bg-gray-50 border border-gray-200/80 text-[#0f1b32] text-xs font-bold transition-all shadow-2xs cursor-pointer flex items-center justify-center gap-2 active:scale-[0.99]"
        >
          <LogOut className="w-3.5 h-3.5" />
          <span>Log Out</span>
        </button>

        {/* Delete Account Button */}
        <button
          type="button"
          onClick={onDeleteAccount}
          className="w-full py-3 px-4 rounded-2xl bg-[#FAF4F0]/60 hover:bg-red-50 border border-red-200 text-[#ba1a1a] text-xs font-bold transition-all shadow-2xs cursor-pointer flex items-center justify-center gap-2 active:scale-[0.99]"
        >
          <Trash2 className="w-3.5 h-3.5" />
          <span>Delete Account</span>
        </button>
      </div>
    </section>
  );
};
