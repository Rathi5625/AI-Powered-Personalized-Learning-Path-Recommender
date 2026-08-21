import React, { useState } from 'react';

interface NotificationSettingItem {
  id: string;
  title: string;
  description: string;
  enabled: boolean;
}

const DEFAULT_NOTIFICATIONS: NotificationSettingItem[] = [
  {
    id: 'reminders',
    title: 'Learning Reminders',
    description: 'Get daily reminders aligned with your target study schedule.',
    enabled: true,
  },
  {
    id: 'progress',
    title: 'Daily Progress Updates',
    description: 'Receive summary notifications on streak milestones and skill growth.',
    enabled: true,
  },
  {
    id: 'mentor',
    title: 'AI Mentor Suggestions',
    description: 'Proactive tips and code hints from your personalized AI tutor.',
    enabled: true,
  },
  {
    id: 'courses',
    title: 'Course Recommendations',
    description: 'Alerts when relevant modules match your career trajectory.',
    enabled: false,
  },
  {
    id: 'assessments',
    title: 'Assessment Reminders',
    description: 'Notifications when milestone diagnostic quizzes become available.',
    enabled: true,
  },
  {
    id: 'summary',
    title: 'Weekly Learning Summary',
    description: 'Comprehensive report on total hours and concept mastery.',
    enabled: true,
  },
];

interface NotificationSettingsSectionProps {
  onToggle?: (title: string, enabled: boolean) => void;
}

export const NotificationSettingsSection: React.FC<NotificationSettingsSectionProps> = ({
  onToggle,
}) => {
  const [items, setItems] = useState(DEFAULT_NOTIFICATIONS);

  const handleToggle = (id: string) => {
    setItems((prev) =>
      prev.map((item) => {
        if (item.id === id) {
          const next = !item.enabled;
          onToggle?.(item.title, next);
          return { ...item, enabled: next };
        }
        return item;
      })
    );
  };

  return (
    <section id="notifications" className="space-y-4 text-left">
      <div className="space-y-1">
        <h2 className="text-xl sm:text-2xl font-extrabold text-[#0f1b32] tracking-tight">
          Notifications
        </h2>
        <p className="text-xs text-[#53433c] font-normal leading-relaxed">
          Manage how and when LearnAI sends you updates and reminders.
        </p>
      </div>

      <div className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] space-y-5">
        {items.map((item, idx) => (
          <div
            key={item.id}
            className={`flex items-center justify-between gap-4 pb-4 ${
              idx !== items.length - 1 ? 'border-b border-gray-100/80' : ''
            }`}
          >
            <div className="space-y-0.5">
              <h3 className="text-xs sm:text-sm font-bold text-[#0f1b32]">
                {item.title}
              </h3>
              <p className="text-xs text-[#53433c] font-normal leading-relaxed">
                {item.description}
              </p>
            </div>

            {/* Toggle Switch */}
            <button
              type="button"
              role="switch"
              aria-checked={item.enabled}
              onClick={() => handleToggle(item.id)}
              className={`
                relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none shadow-2xs
                ${item.enabled ? 'bg-[#8e4d2b]' : 'bg-gray-200'}
              `}
            >
              <span
                className={`
                  pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow-sm ring-0 transition duration-200 ease-in-out
                  ${item.enabled ? 'translate-x-5' : 'translate-x-0'}
                `}
              />
            </button>
          </div>
        ))}
      </div>
    </section>
  );
};
