import React, { useState } from 'react';
import { Github, Calendar, Linkedin, Check } from 'lucide-react';

interface IntegrationItem {
  id: string;
  name: string;
  description: string;
  icon: React.ElementType;
  connected: boolean;
}

const DEFAULT_INTEGRATIONS: IntegrationItem[] = [
  {
    id: 'github',
    name: 'GitHub',
    description: 'Sync project repositories and showcase code milestones directly on your profile.',
    icon: Github,
    connected: true,
  },
  {
    id: 'calendar',
    name: 'Google Calendar',
    description: 'Schedule daily study blocks and sync assessment deadlines to your calendar.',
    icon: Calendar,
    connected: false,
  },
  {
    id: 'linkedin',
    name: 'LinkedIn',
    description: 'Auto-post completed course certificates and verified skill badges to your feed.',
    icon: Linkedin,
    connected: false,
  },
];

interface IntegrationsSettingsSectionProps {
  onToggleIntegration?: (name: string, connected: boolean) => void;
}

export const IntegrationsSettingsSection: React.FC<IntegrationsSettingsSectionProps> = ({
  onToggleIntegration,
}) => {
  const [integrations, setIntegrations] = useState(DEFAULT_INTEGRATIONS);

  const toggleConnection = (id: string) => {
    setIntegrations((prev) =>
      prev.map((item) => {
        if (item.id === id) {
          const next = !item.connected;
          onToggleIntegration?.(item.name, next);
          return { ...item, connected: next };
        }
        return item;
      })
    );
  };

  return (
    <section id="integrations" className="space-y-4 text-left">
      <div className="space-y-1">
        <h2 className="text-xl sm:text-2xl font-extrabold text-[#0f1b32] tracking-tight">
          Integrations
        </h2>
        <p className="text-xs text-[#53433c] font-normal leading-relaxed">
          Connect your favorite developer platforms and productivity tools.
        </p>
      </div>

      <div className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] space-y-4">
        {integrations.map((item) => {
          const Icon = item.icon;

          return (
            <div
              key={item.id}
              className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-4 rounded-2xl bg-white/90 border border-gray-100/90 shadow-2xs"
            >
              <div className="flex items-start gap-3.5">
                <div className="w-10 h-10 rounded-2xl bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#8e4d2b] shrink-0 mt-0.5">
                  <Icon className="w-5 h-5" />
                </div>
                <div className="space-y-0.5">
                  <span className="text-sm font-bold text-[#0f1b32] block">
                    {item.name}
                  </span>
                  <p className="text-xs text-[#53433c] font-normal leading-relaxed max-w-md">
                    {item.description}
                  </p>
                </div>
              </div>

              {/* Action Button */}
              <button
                type="button"
                onClick={() => toggleConnection(item.id)}
                className={`
                  px-4 py-2 rounded-xl text-xs font-bold transition-all cursor-pointer shadow-2xs shrink-0 self-start sm:self-center
                  ${
                    item.connected
                      ? 'bg-emerald-50 border border-emerald-200 text-emerald-700 flex items-center gap-1.5'
                      : 'bg-[#8e4d2b] hover:bg-[#783e20] text-white'
                  }
                `}
              >
                {item.connected ? (
                  <>
                    <Check className="w-3.5 h-3.5" />
                    <span>Connected</span>
                  </>
                ) : (
                  <span>Connect</span>
                )}
              </button>
            </div>
          );
        })}
      </div>
    </section>
  );
};
