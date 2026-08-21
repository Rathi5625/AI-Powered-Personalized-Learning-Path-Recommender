import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Sparkles, CheckCircle2 } from 'lucide-react';
import { AmbientBackground } from '../components/landing/AmbientBackground';
import { ProjectDetailsSidebar } from '../components/project-details/ProjectDetailsSidebar';
import { ProjectDetailsTopBar } from '../components/project-details/ProjectDetailsTopBar';
import { ProjectHero } from '../components/project-details/ProjectHero';
import { ProjectRecommendationCard } from '../components/project-details/ProjectRecommendationCard';
import { SkillsOutcomesCard } from '../components/project-details/SkillsOutcomesCard';
import { BuildPlan } from '../components/project-details/BuildPlan';
import { type BuildPlanStepData } from '../components/project-details/BuildPlanStep';
import { ProjectProgressCard } from '../components/project-details/ProjectProgressCard';
import { AIMentorTipCard } from '../components/project-details/AIMentorTipCard';
import { AIMentorModal } from '../components/project-details/AIMentorModal';

export const ProjectDetailsPage: React.FC = () => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [isSaved, setIsSaved] = useState(false);
  const [isStarted, setIsStarted] = useState(false);
  const [showMentorModal, setShowMentorModal] = useState(false);
  const [showUpgradeModal, setShowUpgradeModal] = useState(false);
  const [highlightedStepDay, setHighlightedStepDay] = useState<number | null>(null);
  const [toast, setToast] = useState<string | null>(null);

  const showToastNotice = (msg: string) => {
    setToast(msg);
    setTimeout(() => setToast(null), 3000);
  };

  const handleToggleSave = () => {
    const nextSaved = !isSaved;
    setIsSaved(nextSaved);
    showToastNotice(nextSaved ? 'Project saved to your list' : 'Project removed from saved');
  };

  const handleStartOrContinue = () => {
    if (!isStarted) {
      setIsStarted(true);
      showToastNotice('Project started successfully!');
    }
    scrollToStep(3);
  };

  const scrollToStep = (day: number) => {
    setHighlightedStepDay(day);
    const element = document.getElementById(`build-step-${day}`);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
    setTimeout(() => setHighlightedStepDay(null), 1800);
  };

  const handleStepAction = (step: BuildPlanStepData) => {
    if (step.status === 'current') {
      showToastNotice(`Resuming ${step.title}`);
    } else if (step.status === 'completed') {
      showToastNotice(`Reviewing completed ${step.title}`);
    } else {
      showToastNotice('This section unlocks after completing the previous step.');
    }
  };

  return (
    <div className="relative min-h-screen flex bg-[#f9f9ff] text-[#0f1b32] selection:bg-[#ffdbcb] selection:text-[#8e4d2b] overflow-x-hidden">
      {/* Atmosphere background lighting */}
      <AmbientBackground />

      {/* Desktop Fixed Left Sidebar */}
      <ProjectDetailsSidebar onUpgrade={() => setShowUpgradeModal(true)} />

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
                  href="/project-details"
                  className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-bold bg-[#ffdbcb]/60 text-[#8e4d2b]"
                >
                  Projects
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
        {/* Sticky Top App Bar */}
        <ProjectDetailsTopBar
          onToggleMobileMenu={() => setMobileMenuOpen(true)}
          searchQuery={searchQuery}
          onSearchChange={setSearchQuery}
        />

        {/* Page Content Body */}
        <main className="flex-1 px-4 sm:px-8 py-6 sm:py-8 max-w-7xl w-full mx-auto space-y-6 sm:space-y-8">
          {/* Main 2-Column Responsive Bento Layout */}
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 sm:gap-8 items-start">
            {/* Left 8 Columns: Main Project Content */}
            <div className="lg:col-span-8 space-y-6 sm:space-y-8">
              {/* Project Hero Card */}
              <ProjectHero
                isSaved={isSaved}
                isStarted={isStarted}
                onToggleSave={handleToggleSave}
                onStartOrContinue={handleStartOrContinue}
                onOpenAIMentor={() => setShowMentorModal(true)}
              />

              {/* 2-Card Middle Row */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6 items-stretch">
                <ProjectRecommendationCard />
                <SkillsOutcomesCard />
              </div>

              {/* AI Build Plan Section */}
              <BuildPlan
                onStepAction={handleStepAction}
                highlightedStepDay={highlightedStepDay}
              />
            </div>

            {/* Right 4 Columns: Sticky Project Progress & AI Mentor Tip */}
            <div className="lg:col-span-4 space-y-6 sticky top-24">
              {/* Project Progress Gauge */}
              <ProjectProgressCard
                progress={28}
                completedStepTitle="Database & Entities"
                currentStepTitle="Authentication & JWT"
                onContinueProject={() => {
                  showToastNotice('Continuing Authentication & JWT');
                  scrollToStep(3);
                }}
              />

              {/* AI Mentor Tip Card */}
              <AIMentorTipCard
                onReviewUserEntity={() => {
                  showToastNotice('Reviewing Day 2: Database & Entities');
                  scrollToStep(2);
                }}
                onAskSecurity={() => setShowMentorModal(true)}
              />
            </div>
          </div>
        </main>
      </div>

      {/* AI Mentor Conversation Modal */}
      <AIMentorModal
        isOpen={showMentorModal}
        onClose={() => setShowMentorModal(false)}
      />

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
                Unlock real-time AI code reviews, cloud development sandboxes, and personalized mentor sessions.
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
