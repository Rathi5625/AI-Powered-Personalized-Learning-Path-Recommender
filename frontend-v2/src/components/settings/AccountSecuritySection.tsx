import React from 'react';
import { ProfileDetailsCard } from './ProfileDetailsCard';
import { SecurityCard } from './SecurityCard';

interface AccountSecuritySectionProps {
  onEditProfile?: () => void;
  onChangePassword?: () => void;
  onToggle2FA?: (enabled: boolean) => void;
  onViewSessions?: () => void;
}

export const AccountSecuritySection: React.FC<AccountSecuritySectionProps> = ({
  onEditProfile,
  onChangePassword,
  onToggle2FA,
  onViewSessions,
}) => {
  return (
    <section id="account" className="space-y-4 text-left">
      {/* Title & Description */}
      <div className="space-y-1">
        <h2 className="text-xl sm:text-2xl font-extrabold text-[#0f1b32] tracking-tight">
          Account &amp; Security
        </h2>
        <p className="text-xs text-[#53433c] font-normal leading-relaxed">
          Manage your basic information and security credentials.
        </p>
      </div>

      {/* Two Side-by-Side Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-5 sm:gap-6 items-stretch">
        <ProfileDetailsCard onEditProfile={onEditProfile} />
        <SecurityCard
          onChangePassword={onChangePassword}
          onToggle2FA={onToggle2FA}
          onViewSessions={onViewSessions}
        />
      </div>
    </section>
  );
};
