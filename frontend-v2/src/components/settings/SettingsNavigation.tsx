import React from 'react';
import {
  User,
  Shield,
  BookOpen,
  Bell,
  Palette,
  Accessibility,
  Lock,
  Puzzle,
} from 'lucide-react';

export type SettingsTabId =
  | 'account'
  | 'security'
  | 'learning'
  | 'notifications'
  | 'appearance'
  | 'accessibility'
  | 'privacy'
  | 'integrations';

interface NavItem {
  id: SettingsTabId;
  label: string;
  icon: React.ElementType;
}

const SETTINGS_NAV: NavItem[] = [
  { id: 'account', label: 'Account', icon: User },
  { id: 'security', label: 'Security', icon: Shield },
  { id: 'learning', label: 'Learning', icon: BookOpen },
  { id: 'notifications', label: 'Notifications', icon: Bell },
  { id: 'appearance', label: 'Appearance', icon: Palette },
  { id: 'accessibility', label: 'Accessibility', icon: Accessibility },
  { id: 'privacy', label: 'Privacy', icon: Lock },
  { id: 'integrations', label: 'Integrations', icon: Puzzle },
];

interface SettingsNavigationProps {
  activeTab: SettingsTabId;
  onSelectTab: (id: SettingsTabId) => void;
}

export const SettingsNavigation: React.FC<SettingsNavigationProps> = ({
  activeTab,
  onSelectTab,
}) => {
  return (
    <nav
      aria-label="Settings Categories"
      className="w-full lg:w-64 rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-3 sm:p-4 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-1 sticky top-24 select-none"
    >
      {SETTINGS_NAV.map((item) => {
        const Icon = item.icon;
        const isActive = activeTab === item.id;

        return (
          <button
            key={item.id}
            type="button"
            onClick={() => onSelectTab(item.id)}
            className={`
              w-full flex items-center gap-3 px-4 py-2.5 rounded-2xl text-xs sm:text-sm font-semibold transition-all cursor-pointer text-left
              ${
                isActive
                  ? 'bg-[#e9edff]/80 text-[#0f1b32] font-bold shadow-2xs'
                  : 'text-[#53433c] hover:bg-black/[0.03] hover:text-[#0f1b32]'
              }
            `}
          >
            <Icon
              className={`w-4 h-4 shrink-0 ${
                isActive ? 'text-[#8e4d2b]' : 'text-gray-400'
              }`}
            />
            <span>{item.label}</span>
          </button>
        );
      })}
    </nav>
  );
};
