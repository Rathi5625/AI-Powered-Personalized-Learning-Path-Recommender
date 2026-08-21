import React from 'react';
import { BadgeCheck } from 'lucide-react';

interface ProfileDetailsCardProps {
  email?: string;
  status?: string;
  memberSince?: string;
  onEditProfile?: () => void;
}

export const ProfileDetailsCard: React.FC<ProfileDetailsCardProps> = ({
  email = 'parth@example.com',
  status = 'Active',
  memberSince = 'August 2026',
  onEditProfile,
}) => {
  return (
    <div className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-5 flex-1 flex flex-col justify-between select-none">
      <div className="space-y-5">
        {/* Header */}
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-xl bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#8e4d2b]">
            <BadgeCheck className="w-4 h-4 text-[#8e4d2b]" />
          </div>
          <h3 className="text-base font-extrabold text-[#0f1b32] tracking-tight">
            Profile Details
          </h3>
        </div>

        {/* Info Grid */}
        <div className="space-y-4">
          {/* Email Address */}
          <div>
            <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider block">
              EMAIL ADDRESS
            </span>
            <span className="text-xs sm:text-sm font-semibold text-[#0f1b32] mt-0.5 block">
              {email}
            </span>
          </div>

          {/* Status & Member Since */}
          <div className="flex items-center justify-between pt-1">
            <div>
              <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider block">
                STATUS
              </span>
              <div className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-emerald-50 border border-emerald-200 text-emerald-700 text-[11px] font-bold mt-1">
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-600" />
                <span>{status}</span>
              </div>
            </div>

            <div className="text-right">
              <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider block">
                MEMBER SINCE
              </span>
              <span className="text-xs font-semibold text-[#0f1b32] mt-1 block">
                {memberSince}
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Action Button */}
      <div className="pt-2">
        <button
          type="button"
          onClick={onEditProfile}
          className="w-full py-2.5 px-4 rounded-xl bg-white/90 hover:bg-[#FAF4F0] border border-gray-200/80 text-[#0f1b32] text-xs font-bold transition-all shadow-2xs cursor-pointer text-center active:scale-[0.99]"
        >
          Edit Profile
        </button>
      </div>
    </div>
  );
};
