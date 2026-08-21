import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Sparkles, CheckCircle2 } from 'lucide-react';
import { AmbientBackground } from '../components/landing/AmbientBackground';
import { ProgressSidebar } from '../components/progress/ProgressSidebar';
import { ProgressTopBar } from '../components/progress/ProgressTopBar';
import { CareerReadinessCard } from '../components/progress/CareerReadinessCard';
import { AIProgressInsight } from '../components/progress/AIProgressInsight';
import { LearningForecast } from '../components/progress/LearningForecast';
import { ProgressStatCardsRow } from '../components/progress/ProgressStatCard';
import { LearningActivityChart } from '../components/progress/LearningActivityChart';
import { SkillGrowthCard } from '../components/progress/SkillGrowthCard';
import { LearningConsistencyCard } from '../components/progress/LearningConsistencyCard';
import { useAuth } from '../context/AuthContext';
import api from '../api/client';
import { ProgressAnalytics } from '../api/types';

export const ProgressPage: React.FC = () => {
  const { user } = useAuth();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [showUpgradeModal, setShowUpgradeModal] = useState(false);
  const [toast, setToast] = useState<string | null>(null);

  const [analytics, setAnalytics] = useState<ProgressAnalytics | null>(null);

  const fetchAnalytics = async () => {
    try {
      const data = await api.getProgressAnalytics();
      setAnalytics(data);
    } catch (err) {
      console.error('Failed to load progress analytics:', err);
    }
  };

  useEffect(() => {
    fetchAnalytics();
  }, []);

  const showToastNotice = (msg: string) => {
    setToast(msg);
    setTimeout(() => setToast(null), 3000);
  };

  const readinessScore = analytics && (analytics.skillsMasteredCount > 0 || analytics.completedCoursesCount > 0)
    ? Math.min(100, (analytics.skillsMasteredCount * 20) + (analytics.completedCoursesCount * 15))
    : 0;

  return (
    <div className="relative min-h-screen flex bg-[#f9f9ff] text-[#0f1b32] selection:bg-[#ffdbcb] selection:text-[#8e4d2b] overflow-x-hidden">
      {/* Soft atmospheric background lighting */}
      <AmbientBackground />

      {/* Desktop Fixed Left Sidebar */}
      <ProgressSidebar onUpgrade={() => setShowUpgradeModal(true)} />

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
                  href="/progress"
                  className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-bold bg-[#ffdbcb]/60 text-[#8e4d2b]"
                >
                  Progress
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

      {/* Main Container */}
      <div className="flex-1 flex flex-col min-w-0 z-10">
        <ProgressTopBar onToggleMobileMenu={() => setMobileMenuOpen(true)} />

        <main className="flex-1 px-4 sm:px-8 py-6 sm:py-8 max-w-7xl w-full mx-auto space-y-6 sm:space-y-8">
          {/* Row 1: 2-Column Responsive Card Composition */}
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 sm:gap-8 items-stretch">
            {/* Left 8 Cols: Career Readiness Master Gauge */}
            <div className="lg:col-span-8">
              <CareerReadinessCard
                score={readinessScore}
                improvement={0}
                role={user?.targetCareer || 'Software Engineer'}
                onViewAnalysis={() => showToastNotice('Opening detailed readiness breakdown...')}
              />
            </div>

            {/* Right 4 Cols: AI Insight + Forecast */}
            <div className="lg:col-span-4 space-y-4 sm:space-y-5 flex flex-col justify-between">
              <AIProgressInsight
                onAction={() => showToastNotice('Navigating to next recommended course...')}
              />
              <LearningForecast
                monthsRemaining={readinessScore > 0 ? 5.4 : 6.0}
                progressPercent={readinessScore}
              />
            </div>
          </div>

          {/* Row 2: 4 Summary Statistic Cards */}
          <ProgressStatCardsRow analytics={analytics} />

          {/* Row 3: Learning Activity Chart & Skill Growth / Consistency */}
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 sm:gap-8 items-start">
            {/* Left 8 Cols: Learning Activity Chart */}
            <div className="lg:col-span-8">
              <LearningActivityChart />
            </div>

            {/* Right 4 Cols: Skill Growth & Consistency */}
            <div className="lg:col-span-4 space-y-6">
              <SkillGrowthCard />
              <LearningConsistencyCard />
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
                Unlock advanced progress projections, velocity diagnostics, in-depth benchmarking against industry peers, and automated milestone badges.
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
