import React from 'react';

export const SettingsHeader: React.FC = () => {
  return (
    <div className="text-left space-y-1.5 select-none">
      <h1 className="text-3xl sm:text-4xl font-extrabold text-[#0f1b32] tracking-tight">
        Settings
      </h1>
      <p className="text-xs sm:text-sm text-[#53433c] font-normal leading-relaxed max-w-3xl">
        Manage your account preferences, personalize your AI learning experience, and adjust your notifications.
      </p>
    </div>
  );
};
