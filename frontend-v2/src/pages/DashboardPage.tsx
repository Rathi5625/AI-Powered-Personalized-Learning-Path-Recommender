import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  TrendingUp,
  Clock,
  Award,
  Flame,
  X,
  Sparkles,
  Rocket,
} from 'lucide-react';
import { AmbientBackground } from '../components/landing/AmbientBackground';
import { DashboardSidebar } from '../components/dashboard/DashboardSidebar';
import { DashboardTopBar } from '../components/dashboard/DashboardTopBar';
import { DashboardStatCard } from '../components/dashboard/DashboardStatCard';
import { CareerGoalDashboardCard } from '../components/dashboard/CareerGoalDashboardCard';
import { TodaysLearningCard } from '../components/dashboard/TodaysLearningCard';
import { AIMentorDashboardCard } from '../components/dashboard/AIMentorDashboardCard';
import { SkillOverviewCard } from '../components/dashboard/SkillOverviewCard';
import { RecommendedProjectCard } from '../components/dashboard/RecommendedProjectCard';
import api from '../api/client';
import { DashboardAggregated } from '../api/types';

export const DashboardPage: React.FC = () => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [toast, setToast] = useState<string | null>(null);
  const [adjustGoalModal, setAdjustGoalModal] = useState(false);
  const [projectModal, setProjectModal] = useState(false);
  const [upgradeModal, setUpgradeModal] = useState(false);

  const [dashboardData, setDashboardData] = useState<DashboardAggregated | null>(null);

  const fetchDashboard = async () => {
    try {
      const data = await api.getDashboardData();
      setDashboardData(data);
    } catch (err) {
      console.error('Failed to load dashboard data:', err);
    }
  };

  useEffect(() => {
    fetchDashboard();
  }, []);

  const showToastNotice = (msg: string) => {
    setToast(msg);
    setTimeout(() => setToast(null), 3000);
  };

  const streak = dashboardData ? `${dashboardData.activeStreakDays} days` : '0 days';
  const hours = dashboardData ? `${dashboardData.totalLearningHours}h` : '0.0h';
  const skillsCount = dashboardData ? `${dashboardData.totalSkillsCount}` : '0';
  const readiness = dashboardData && dashboardData.profileCompletionPercentage > 0 ? `${dashboardData.profileCompletionPercentage}%` : 'Assessment needed';
  const career = dashboardData?.targetCareer || 'Software Engineer';

  return (
    <div className="relative min-h-screen flex bg-[#f9f9ff] text-[#0f1b32] selection:bg-[#ffdbcb] selection:text-[#8e4d2b] overflow-x-hidden">
      {/* Background Atmosphere */}
      <AmbientBackground />

      {/* Desktop Fixed Left Sidebar */}
      <DashboardSidebar onUpgrade={() => setUpgradeModal(true)} />

      {/* Mobile Drawer Navigation */}
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
                  className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-bold bg-[#ffdbcb]/60 text-[#8e4d2b]"
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
              </div>
              <div className="pt-4 border-t border-gray-100 text-xs">
                <a
                  href="/profile"
                  className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-medium text-gray-700 hover:bg-gray-50"
                >
                  Profile
                </a>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Main Workspace Frame */}
      <div className="flex-1 flex flex-col min-w-0 z-10">
        {/* Sticky Dashboard Top Bar */}
        <DashboardTopBar onToggleMobileMenu={() => setMobileMenuOpen(true)} />

        {/* Page Content Body */}
        <main className="flex-1 px-4 sm:px-8 py-6 sm:py-8 max-w-7xl w-full mx-auto space-y-6 sm:space-y-8">
          {/* Top 4 Stats Metric Cards Grid */}
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-5">
            <DashboardStatCard
              icon={TrendingUp}
              iconBg="bg-emerald-50"
              iconColor="text-emerald-600"
              label="Career Readiness"
              value={readiness}
            />
            <DashboardStatCard
              icon={Clock}
              iconBg="bg-[#FAF4F0]"
              iconColor="text-[#8e4d2b]"
              label="Total Hours"
              value={hours}
            />
            <DashboardStatCard
              icon={Award}
              iconBg="bg-blue-50"
              iconColor="text-blue-600"
              label="Skills Improved"
              value={skillsCount}
            />
            <DashboardStatCard
              icon={Flame}
              iconBg="bg-amber-50"
              iconColor="text-amber-600"
              label="Current Streak"
              value={streak}
            />
          </div>

          {/* Main Dashboard 2-Column Composition */}
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 sm:gap-8 items-start">
            {/* Left Column (8 cols on lg) */}
            <div className="lg:col-span-8 space-y-6">
              {/* Current Career Goal Card */}
              <CareerGoalDashboardCard
                role={career}
                estTime="Est. 6 months"
                weeklyHours="10 hrs/week"
                progress={dashboardData?.profileCompletionPercentage || 0}
                onAdjustGoal={() => setAdjustGoalModal(true)}
              />

              {/* 2-Column Sub-Grid: Today's Learning & AI Mentor */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-5 sm:gap-6 items-stretch">
                <TodaysLearningCard
                  enrolledCourses={dashboardData?.enrolledCourses}
                  onStartActivity={(act) => showToastNotice(`Launching ${act}...`)}
                />
                <AIMentorDashboardCard
                  onStartTopic={() => showToastNotice('Starting study session...')}
                />
              </div>
            </div>

            {/* Right Column: Skill Overview & Recommended Project (4 cols on lg) */}
            <div className="lg:col-span-4 space-y-6">
              {/* Skill Overview Card */}
              <SkillOverviewCard skills={dashboardData?.topSkills} />

              {/* Recommended Project Card */}
              <RecommendedProjectCard
                onViewDetails={() => setProjectModal(true)}
              />
            </div>
          </div>
        </main>
      </div>

      {/* Modal 1: Adjust Goal */}
      <AnimatePresence>
        {adjustGoalModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setAdjustGoalModal(false)}
              className="fixed inset-0 bg-black/40 backdrop-blur-xs"
            />
            <motion.div
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              className="relative w-full max-w-md bg-white rounded-3xl p-6 sm:p-7 shadow-2xl border border-gray-100 z-10 text-left"
            >
              <div className="flex items-center justify-between pb-3 border-b border-gray-100 mb-4">
                <h3 className="text-base font-bold text-[#0f1b32]">Adjust Career Goal</h3>
                <button
                  type="button"
                  aria-label="Close"
                  onClick={() => setAdjustGoalModal(false)}
                  className="text-gray-400 hover:text-gray-600 p-1"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
              <div className="space-y-4 text-xs text-gray-600">
                <div>
                  <label className="font-bold text-gray-700 block mb-1">Target Role</label>
                  <input
                    type="text"
                    defaultValue={career}
                    className="w-full h-10 px-3.5 rounded-xl border border-gray-200 text-xs text-[#0f1b32] focus:outline-none focus:border-[#8e4d2b]"
                  />
                </div>
                <div>
                  <label className="font-bold text-gray-700 block mb-1">Timeline</label>
                  <select className="w-full h-10 px-3.5 rounded-xl border border-gray-200 text-xs text-[#0f1b32] focus:outline-none focus:border-[#8e4d2b]">
                    <option>3 months (Fast track)</option>
                    <option defaultValue="6">6 months (Recommended)</option>
                    <option>12 months (Comprehensive)</option>
                  </select>
                </div>
                <button
                  type="button"
                  onClick={() => {
                    setAdjustGoalModal(false);
                    showToastNotice('Career roadmap updated');
                  }}
                  className="w-full h-11 rounded-xl bg-[#8e4d2b] hover:bg-[#783e20] text-white font-bold text-xs transition-colors cursor-pointer shadow-sm mt-2"
                >
                  Save & Re-generate Roadmap
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Modal 2: Project Details */}
      <AnimatePresence>
        {projectModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setProjectModal(false)}
              className="fixed inset-0 bg-black/40 backdrop-blur-xs"
            />
            <motion.div
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              className="relative w-full max-w-lg bg-white rounded-3xl p-6 sm:p-7 shadow-2xl border border-gray-100 z-10 text-left space-y-4"
            >
              <div className="flex items-center justify-between pb-3 border-b border-gray-100">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-xl bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#8e4d2b]">
                    <Rocket className="w-4 h-4 text-[#8e4d2b]" />
                  </div>
                  <h3 className="text-base font-bold text-[#0f1b32]">Full-Stack E-Commerce Platform</h3>
                </div>
                <button
                  type="button"
                  aria-label="Close"
                  onClick={() => setProjectModal(false)}
                  className="text-gray-400 hover:text-gray-600 p-1"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>

              <p className="text-xs sm:text-sm text-gray-600 leading-relaxed font-normal">
                Build a production-ready shopping portal with Spring Boot backend, React frontend, JWT auth, Stripe payments, and PostgreSQL persistence.
              </p>

              <div className="flex items-center gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => {
                    setProjectModal(false);
                    showToastNotice('Project started and added to your tracker!');
                  }}
                  className="flex-1 py-3 rounded-2xl bg-[#8e4d2b] hover:bg-[#783e20] text-white text-xs font-bold transition-colors cursor-pointer shadow-sm text-center"
                >
                  Start Project Now
                </button>
                <a
                  href="/projects"
                  className="px-4 py-3 rounded-2xl border border-gray-200 text-xs font-bold text-gray-700 hover:bg-gray-50 transition-colors text-center"
                >
                  View All Projects
                </a>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Modal 3: Upgrade to Pro */}
      <AnimatePresence>
        {upgradeModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setUpgradeModal(false)}
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
                  aria-label="Close"
                  onClick={() => setUpgradeModal(false)}
                  className="text-gray-400 hover:text-gray-600 p-1"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>

              <p className="text-xs sm:text-sm text-gray-600 leading-relaxed font-normal">
                Unlock unlimited AI mentor queries, real-time code reviews, adaptive mock assessments, and personalized career roadmaps.
              </p>

              <button
                type="button"
                onClick={() => {
                  setUpgradeModal(false);
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

      {/* Live Toast Notification */}
      <AnimatePresence>
        {toast && (
          <motion.div
            initial={{ opacity: 0, y: 18 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 18 }}
            className="fixed bottom-6 left-1/2 -translate-x-1/2 bg-[#0f1b32] text-white text-xs font-medium px-4 py-2.5 rounded-full shadow-lg z-50 flex items-center gap-2 whitespace-nowrap"
          >
            <Sparkles className="w-3.5 h-3.5 text-[#ffdbcb]" />
            <span>{toast}</span>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};
