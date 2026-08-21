import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Sparkles, CheckCircle2, AlertTriangle, LogOut } from 'lucide-react';
import { AmbientBackground } from '../components/landing/AmbientBackground';
import { ProfileSidebar } from '../components/profile/ProfileSidebar';
import { ProfileTopBar } from '../components/profile/ProfileTopBar';
import { ProfileHero } from '../components/profile/ProfileHero';
import { PersonalInformationCard } from '../components/profile/PersonalInformationCard';
import { CareerProfileCard } from '../components/profile/CareerProfileCard';
import { SkillsCard } from '../components/profile/SkillsCard';
import { PersonalObjectiveCard } from '../components/profile/PersonalObjectiveCard';
import { AIProfileStatusCard } from '../components/profile/AIProfileStatusCard';
import { LearningPreferencesCard } from '../components/profile/LearningPreferencesCard';
import { AccountSettingsCard } from '../components/profile/AccountSettingsCard';
import api from '../api/client';
import { UserProfile } from '../api/types';

export const ProfilePage: React.FC = () => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [showUpgradeModal, setShowUpgradeModal] = useState(false);
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [toast, setToast] = useState<string | null>(null);

  const [profile, setProfile] = useState<UserProfile | null>(null);

  const fetchProfile = async () => {
    try {
      const data = await api.getProfile();
      setProfile(data);
    } catch (err) {
      console.error('Failed to load profile:', err);
    }
  };

  useEffect(() => {
    fetchProfile();
  }, []);

  const showToastNotice = (msg: string) => {
    setToast(msg);
    setTimeout(() => setToast(null), 3000);
  };

  const handleSavePersonalInfo = async (data: { fullName: string; location: string; education: string; graduationYear: string }) => {
    try {
      const gradYear = data.graduationYear ? parseInt(data.graduationYear, 10) : undefined;
      const updated = await api.updateProfile({
        fullName: data.fullName,
        location: data.location,
        education: data.education,
        graduationYear: isNaN(gradYear as number) ? undefined : gradYear,
      });
      setProfile(updated);
      showToastNotice('Personal information updated successfully');
    } catch (err: any) {
      showToastNotice(err.message || 'Failed to update personal information');
    }
  };

  const handleSkillAdded = async (skillName: string) => {
    try {
      showToastNotice(`Skill added: ${skillName}`);
      fetchProfile();
    } catch (err: any) {
      showToastNotice(err.message || 'Failed to add skill');
    }
  };

  const handleSkillRemoved = async (skillName: string) => {
    try {
      const skillObj = profile?.skills?.find((s) => s.skillName.toLowerCase() === skillName.toLowerCase());
      if (skillObj) {
        await api.deleteProfileSkill(skillObj.skillId);
      }
      showToastNotice(`Removed skill: ${skillName}`);
      fetchProfile();
    } catch (err: any) {
      showToastNotice(err.message || 'Failed to remove skill');
    }
  };

  const handleSaveObjective = async () => {
    try {
      showToastNotice('Personal objective saved');
      fetchProfile();
    } catch (err: any) {
      showToastNotice(err.message || 'Failed to save objective');
    }
  };

  const skillNames = profile?.skills?.map((s) => s.skillName) || [
    'Java', 'DSA', 'React', 'SQL', 'Spring Boot', 'Git', 'REST APIs', 'MySQL'
  ];

  return (
    <div className="relative min-h-screen flex bg-[#f9f9ff] text-[#0f1b32] selection:bg-[#ffdbcb] selection:text-[#8e4d2b] overflow-x-hidden">
      {/* Soft atmospheric background lighting */}
      <AmbientBackground />

      {/* Desktop Fixed Left Sidebar */}
      <ProfileSidebar onUpgrade={() => setShowUpgradeModal(true)} />

      {/* Mobile Navigation Drawer */}
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
                  href="/profile"
                  className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-bold bg-[#ffdbcb]/60 text-[#8e4d2b]"
                >
                  Profile
                </a>
              </div>

              <div className="pt-4 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => {
                    setMobileMenuOpen(false);
                    setShowUpgradeModal(true);
                  }}
                  className="w-full py-2 rounded-xl bg-[#FAF4F0] border border-[#F2DACB] text-[#8e4d2b] font-bold text-xs"
                >
                  Upgrade to Pro
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Main Content Workspace */}
      <div className="flex-1 flex flex-col min-w-0 z-10">
        {/* Sticky Top Bar */}
        <ProfileTopBar
          onToggleMobileMenu={() => setMobileMenuOpen(true)}
          searchQuery={searchQuery}
          onSearchChange={setSearchQuery}
        />

        {/* Page Content */}
        <main className="flex-1 px-4 sm:px-8 py-6 sm:py-8 max-w-6xl w-full mx-auto space-y-6 sm:space-y-7">
          {/* Profile Hero Card */}
          <ProfileHero
            name={profile?.fullName || 'Parth Rathi'}
            role={profile?.targetCareer || 'Software Engineer'}
            completeness={profile?.profileCompletionPercentage ?? 92}
          />

          {/* Two-Column Layout Grid */}
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 sm:gap-7 items-start">
            {/* Left Column (8 Columns) */}
            <div className="lg:col-span-8 space-y-6 sm:space-y-7">
              <PersonalInformationCard
                initialData={{
                  fullName: profile?.fullName,
                  email: profile?.email,
                  location: profile?.location,
                  education: profile?.education,
                  graduationYear: profile?.graduationYear ? String(profile.graduationYear) : '2027',
                }}
                onSave={handleSavePersonalInfo}
              />

              <CareerProfileCard
                currentGoal={profile?.targetCareer || 'Software Engineer'}
                targetRole={profile?.targetCareer || 'Full Stack Developer'}
                experience={profile?.experienceLevel ? (profile.experienceLevel.charAt(0) + profile.experienceLevel.slice(1).toLowerCase()) : 'Intermediate'}
              />

              <SkillsCard
                skills={skillNames}
                onSkillAdded={handleSkillAdded}
                onSkillRemoved={handleSkillRemoved}
              />

              <PersonalObjectiveCard
                onSave={handleSaveObjective}
              />
            </div>

            {/* Right Column (4 Columns) */}
            <div className="lg:col-span-4 space-y-6 sm:space-y-7">
              <AIProfileStatusCard
                onRefresh={() => {
                  fetchProfile();
                  showToastNotice('Recommendations refreshed');
                }}
              />

              <LearningPreferencesCard />

              <AccountSettingsCard
                onLogOut={() => setShowLogoutModal(true)}
                onDeleteAccount={() => setShowDeleteModal(true)}
              />
            </div>
          </div>
        </main>
      </div>

      {/* Upgrade to Pro Modal */}
      <AnimatePresence>
        {showUpgradeModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setShowUpgradeModal(false)}
              className="fixed inset-0 bg-black/40 backdrop-blur-xs"
            />
            <motion.div
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              className="relative w-full max-w-md bg-white rounded-3xl p-6 sm:p-7 shadow-2xl border border-gray-100 z-10 text-left space-y-4"
            >
              <div className="flex items-center justify-between pb-3 border-b border-gray-100">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-xl bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#8e4d2b]">
                    <Sparkles className="w-4 h-4 text-[#8e4d2b]" />
                  </div>
                  <h3 className="text-base font-bold text-[#0f1b32]">Upgrade to LearnAI Pro</h3>
                </div>
                <button
                  type="button"
                  aria-label="Close modal"
                  onClick={() => setShowUpgradeModal(false)}
                  className="text-gray-400 hover:text-gray-600 p-1"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>

              <p className="text-xs sm:text-sm text-gray-600 leading-relaxed font-normal">
                Unlock exclusive full-stack interview prep, personalized AI mentorship, and verified profile certificates.
              </p>

              <button
                type="button"
                onClick={() => {
                  setShowUpgradeModal(false);
                  showToastNotice('Pro upgrade will be available soon!');
                }}
                className="w-full py-3 rounded-2xl bg-[#8e4d2b] hover:bg-[#783e20] text-white text-xs font-bold transition-colors cursor-pointer shadow-sm text-center"
              >
                Get Started with Pro
              </button>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Log Out Confirmation Modal */}
      <AnimatePresence>
        {showLogoutModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setShowLogoutModal(false)}
              className="fixed inset-0 bg-black/40 backdrop-blur-xs"
            />
            <motion.div
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              className="relative w-full max-w-sm bg-white rounded-3xl p-6 shadow-2xl border border-gray-100 z-10 text-left space-y-4"
            >
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 rounded-2xl bg-gray-100 flex items-center justify-center text-gray-700">
                  <LogOut className="w-4 h-4" />
                </div>
                <div>
                  <h3 className="text-sm font-bold text-[#0f1b32]">Log Out</h3>
                  <p className="text-xs text-gray-500">Are you sure you want to sign out?</p>
                </div>
              </div>

              <div className="flex items-center gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowLogoutModal(false)}
                  className="flex-1 py-2.5 rounded-xl border border-gray-200 text-xs font-bold text-gray-600 hover:bg-gray-50 transition-colors"
                >
                  Cancel
                </button>
                <a
                  href="/login"
                  onClick={() => api.logout()}
                  className="flex-1 py-2.5 rounded-xl bg-[#8e4d2b] hover:bg-[#783e20] text-white text-xs font-bold text-center transition-colors shadow-2xs"
                >
                  Log Out
                </a>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Delete Account Modal */}
      <AnimatePresence>
        {showDeleteModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setShowDeleteModal(false)}
              className="fixed inset-0 bg-black/40 backdrop-blur-xs"
            />
            <motion.div
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              className="relative w-full max-w-sm bg-white rounded-3xl p-6 shadow-2xl border border-red-100 z-10 text-left space-y-4"
            >
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 rounded-2xl bg-red-50 text-red-600 flex items-center justify-center">
                  <AlertTriangle className="w-4 h-4" />
                </div>
                <div>
                  <h3 className="text-sm font-bold text-[#ba1a1a]">Delete Account</h3>
                  <p className="text-xs text-gray-500">This action cannot be undone.</p>
                </div>
              </div>

              <p className="text-xs text-gray-600 leading-relaxed">
                All your learning history, skill gap profiles, and custom roadmaps will be permanently removed.
              </p>

              <div className="flex items-center gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowDeleteModal(false)}
                  className="flex-1 py-2.5 rounded-xl border border-gray-200 text-xs font-bold text-gray-600 hover:bg-gray-50 transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setShowDeleteModal(false);
                    showToastNotice('Account deletion requested.');
                  }}
                  className="flex-1 py-2.5 rounded-xl bg-[#ba1a1a] hover:bg-red-700 text-white text-xs font-bold text-center transition-colors shadow-2xs"
                >
                  Delete
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

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
