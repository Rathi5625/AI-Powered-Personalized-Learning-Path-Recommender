import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, CheckCircle2, Menu } from 'lucide-react';
import { AmbientBackground } from '../components/landing/AmbientBackground';
import { SettingsSidebar } from '../components/settings/SettingsSidebar';
import { SettingsHeader } from '../components/settings/SettingsHeader';
import { SettingsNavigation, type SettingsTabId } from '../components/settings/SettingsNavigation';
import { AccountSecuritySection } from '../components/settings/AccountSecuritySection';
import { LearningSettingsSection } from '../components/settings/LearningSettingsSection';
import { NotificationSettingsSection } from '../components/settings/NotificationSettingsSection';
import { AppearanceSettingsSection } from '../components/settings/AppearanceSettingsSection';
import { AccessibilitySettingsSection } from '../components/settings/AccessibilitySettingsSection';
import { PrivacySettingsSection } from '../components/settings/PrivacySettingsSection';
import { IntegrationsSettingsSection } from '../components/settings/IntegrationsSettingsSection';
import { EditProfileModal } from '../components/settings/EditProfileModal';
import { ChangePasswordModal } from '../components/settings/ChangePasswordModal';
import { ActiveSessionsModal } from '../components/settings/ActiveSessionsModal';
import api from '../api/client';
import { Settings } from '../api/types';

export const SettingsPage: React.FC = () => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [activeTab, setActiveTab] = useState<SettingsTabId>('account');
  const [showEditProfile, setShowEditProfile] = useState(false);
  const [showChangePassword, setShowChangePassword] = useState(false);
  const [showActiveSessions, setShowActiveSessions] = useState(false);
  const [toast, setToast] = useState<string | null>(null);

  const [settings, setSettings] = useState<Settings | null>(null);

  const fetchSettings = async () => {
    try {
      const data = await api.getSettings();
      setSettings(data);
    } catch (err) {
      console.error('Failed to load settings:', err);
    }
  };

  useEffect(() => {
    fetchSettings();
  }, []);

  const showToastNotice = (msg: string) => {
    setToast(msg);
    setTimeout(() => setToast(null), 3000);
  };

  const handleSelectTab = (tabId: SettingsTabId) => {
    setActiveTab(tabId);
    const element = document.getElementById(tabId);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  };

  const handleSaveSettings = async (updates: Partial<Settings>) => {
    try {
      const updated = await api.updateSettings(updates);
      setSettings(updated);
      showToastNotice('Settings updated successfully');
    } catch (err: any) {
      showToastNotice(err.message || 'Failed to update settings');
    }
  };

  return (
    <div className="relative min-h-screen flex bg-[#f9f9ff] text-[#0f1b32] selection:bg-[#ffdbcb] selection:text-[#8e4d2b] overflow-x-hidden">
      {/* Ambient background lighting */}
      <AmbientBackground />

      {/* Desktop Fixed Left Sidebar */}
      <SettingsSidebar />

      {/* Mobile Drawer */}
      <AnimatePresence>
        {mobileMenuOpen && (
          <div className="fixed inset-0 z-50 lg:hidden flex">
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setMobileMenuOpen(false)}
              className="fixed inset-0 bg-black/30 backdrop-blur-xs"
            />
            <motion.div
              initial={{ x: -260 }}
              animate={{ x: 0 }}
              exit={{ x: -260 }}
              transition={{ type: 'spring', damping: 25, stiffness: 280 }}
              className="relative w-[260px] bg-white h-full z-10 p-5 flex flex-col justify-between shadow-2xl"
            >
              <div className="flex items-center justify-between pb-4 border-b border-gray-100">
                <div className="flex items-center gap-2">
                  <div className="w-7 h-7 rounded-lg bg-[#8e4d2b] flex items-center justify-center text-white font-bold text-sm">
                    L
                  </div>
                  <span className="font-extrabold text-lg text-[#8e4d2b]">LearnAI</span>
                </div>
                <button
                  type="button"
                  aria-label="Close menu"
                  onClick={() => setMobileMenuOpen(false)}
                  className="p-1 text-gray-400 hover:text-gray-600"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              <div className="space-y-1 py-4 text-xs">
                <a
                  href="/dashboard"
                  className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-medium text-gray-700 hover:bg-gray-50"
                >
                  Dashboard
                </a>
                <a
                  href="/learning-path"
                  className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-medium text-gray-700 hover:bg-gray-50"
                >
                  My Learning Path
                </a>
                <a
                  href="/explore-courses"
                  className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-medium text-gray-700 hover:bg-gray-50"
                >
                  Explore Courses
                </a>
                <a
                  href="/skills"
                  className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-medium text-gray-700 hover:bg-gray-50"
                >
                  Skills
                </a>
                <a
                  href="/assessments"
                  className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-medium text-gray-700 hover:bg-gray-50"
                >
                  Assessments
                </a>
                <a
                  href="/projects"
                  className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-medium text-gray-700 hover:bg-gray-50"
                >
                  Projects
                </a>
                <a
                  href="/ai-mentor"
                  className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-medium text-gray-700 hover:bg-gray-50"
                >
                  AI Mentor
                </a>
                <a
                  href="/progress"
                  className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-medium text-gray-700 hover:bg-gray-50"
                >
                  Progress
                </a>
                <a
                  href="/settings"
                  className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-bold bg-[#ffdbcb]/60 text-[#8e4d2b]"
                >
                  Settings
                </a>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Main Content Workspace */}
      <div className="flex-1 flex flex-col min-w-0 z-10">
        {/* Mobile Header Bar */}
        <div className="lg:hidden h-16 px-4 flex items-center justify-between border-b border-gray-200/60 bg-[#f9f9ff]/80 backdrop-blur-xl sticky top-0 z-20">
          <button
            type="button"
            onClick={() => setMobileMenuOpen(true)}
            aria-label="Open Navigation Menu"
            className="p-2 rounded-xl text-[#0f1b32] hover:bg-black/5"
          >
            <Menu className="w-5 h-5" />
          </button>
          <span className="font-extrabold text-base text-[#8e4d2b]">Settings</span>
          <div className="w-8 h-8 rounded-full bg-[#d98b63] text-white font-extrabold text-xs flex items-center justify-center">
            {settings?.fullName?.substring(0, 2).toUpperCase() || 'PR'}
          </div>
        </div>

        {/* Page Content Body */}
        <main className="flex-1 px-4 sm:px-8 py-6 sm:py-8 max-w-7xl w-full mx-auto space-y-6 sm:space-y-8">
          {/* Header */}
          <SettingsHeader />

          {/* Settings Two-Column Layout */}
          <div className="flex flex-col lg:flex-row gap-8 items-start">
            {/* Left Sticky Settings Navigation */}
            <SettingsNavigation
              activeTab={activeTab}
              onSelectTab={handleSelectTab}
            />

            {/* Right Settings Content Sections */}
            <div className="flex-1 space-y-8 min-w-0 w-full">
              <AccountSecuritySection
                onEditProfile={() => setShowEditProfile(true)}
                onChangePassword={() => setShowChangePassword(true)}
                onToggle2FA={(enabled) =>
                  showToastNotice(
                    enabled
                      ? 'Two-factor authentication enabled'
                      : 'Two-factor authentication disabled'
                  )
                }
                onViewSessions={() => setShowActiveSessions(true)}
              />

              <LearningSettingsSection
                onSettingsChanged={() => {
                  fetchSettings();
                  showToastNotice('Learning preferences updated');
                }}
              />

              <NotificationSettingsSection
                onToggle={(title, enabled) => {
                  if (title.toLowerCase().includes('email')) {
                    handleSaveSettings({ emailNotifications: enabled });
                  } else if (title.toLowerCase().includes('push')) {
                    handleSaveSettings({ pushNotifications: enabled });
                  } else {
                    showToastNotice(`${title} ${enabled ? 'enabled' : 'disabled'}`);
                  }
                }}
              />

              <AppearanceSettingsSection
                onThemeChanged={(theme) => {
                  handleSaveSettings({ themePreference: theme.toLowerCase() });
                  showToastNotice(`Theme changed to ${theme}`);
                }}
              />

              <AccessibilitySettingsSection
                onToggle={(setting, val) =>
                  showToastNotice(`${setting} ${val ? 'enabled' : 'disabled'}`)
                }
              />

              <PrivacySettingsSection
                onDownloadData={() => showToastNotice('Preparing data download...')}
                onSettingChanged={() => showToastNotice('Privacy preferences saved')}
              />

              <IntegrationsSettingsSection
                onToggleIntegration={(name, connected) =>
                  showToastNotice(
                    `${name} ${connected ? 'connected' : 'disconnected'}`
                  )
                }
              />
            </div>
          </div>
        </main>
      </div>

      {/* Edit Profile Modal */}
      <EditProfileModal
        isOpen={showEditProfile}
        onClose={() => setShowEditProfile(false)}
        onSuccess={(data: { fullName: string; email: string; location: string }) => {
          handleSaveSettings({ fullName: data.fullName, location: data.location });
          setShowEditProfile(false);
        }}
      />

      {/* Change Password Modal */}
      <ChangePasswordModal
        isOpen={showChangePassword}
        onClose={() => setShowChangePassword(false)}
        onSuccess={() => {
          setShowChangePassword(false);
          showToastNotice('Password updated successfully');
        }}
      />

      {/* Active Sessions Modal */}
      <ActiveSessionsModal
        isOpen={showActiveSessions}
        onClose={() => setShowActiveSessions(false)}
        onSignOutOthers={() => {
          setShowActiveSessions(false);
          showToastNotice('All other sessions terminated');
        }}
      />

      {/* Live Toast Notification */}
      <AnimatePresence>
        {toast && (
          <motion.div
            initial={{ opacity: 0, y: 18 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 18 }}
            className="fixed bottom-6 left-1/2 -translate-x-1/2 bg-[#0f1b32] text-white text-xs font-medium px-4 py-2.5 rounded-full shadow-lg z-50 flex items-center gap-2 whitespace-nowrap"
          >
            <CheckCircle2 className="w-3.5 h-3.5 text-[#ffdbcb]" />
            <span>{toast}</span>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};
