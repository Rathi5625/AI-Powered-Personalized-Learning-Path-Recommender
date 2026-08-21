import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Sparkles, CheckCircle2, Play, Users, MessageSquare } from 'lucide-react';
import { AmbientBackground } from '../components/landing/AmbientBackground';
import { CourseDetailsSidebar } from '../components/course-details/CourseDetailsSidebar';
import { CourseDetailsTopBar } from '../components/course-details/CourseDetailsTopBar';
import { CourseBreadcrumbs } from '../components/course-details/CourseBreadcrumbs';
import { CourseHeroCard } from '../components/course-details/CourseHeroCard';
import { AIRecommendationCard } from '../components/course-details/AIRecommendationCard';
import { RoadmapConnection } from '../components/course-details/RoadmapConnection';
import { SkillsGainCard } from '../components/course-details/SkillsGainCard';
import { ProjectedImpactCard } from '../components/course-details/ProjectedImpactCard';
import { CurriculumCard } from '../components/course-details/CurriculumCard';
import { LearningPathStatusCard } from '../components/course-details/LearningPathStatusCard';

export const CourseDetailsPage: React.FC = () => {
  const navigate = useNavigate();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [isAdded, setIsAdded] = useState(false);
  const [isFavorite, setIsFavorite] = useState(false);
  const [toast, setToast] = useState<string | null>(null);
  const [showUpgradeModal, setShowUpgradeModal] = useState(false);
  const [showStartModal, setShowStartModal] = useState(false);
  const [showMentorsModal, setShowMentorsModal] = useState(false);
  const [showReviewsModal, setShowReviewsModal] = useState(false);

  const showToastNotice = (msg: string) => {
    setToast(msg);
    setTimeout(() => setToast(null), 3000);
  };

  const handleToggleAdd = () => {
    if (!isAdded) {
      setIsAdded(true);
      showToastNotice('Course added to your learning path!');
    } else {
      setIsAdded(false);
      showToastNotice('Course removed from your learning path');
    }
  };

  const handleToggleFavorite = () => {
    const nextState = !isFavorite;
    setIsFavorite(nextState);
    showToastNotice(nextState ? 'Added to favorites' : 'Removed from favorites');
  };

  const handleStartCourse = () => {
    setShowStartModal(true);
  };

  const handleContinueModule = (moduleId: number) => {
    showToastNotice(`Launching Module ${moduleId}: Interactive Practice Sandbox...`);
  };

  const scrollToCurriculum = () => {
    const el = document.getElementById('curriculum-section');
    if (el) {
      el.scrollIntoView({ behavior: 'smooth' });
    }
  };

  return (
    <div className="relative min-h-screen flex bg-[#f9f9ff] text-[#0f1b32] selection:bg-[#ffdbcb] selection:text-[#8e4d2b] overflow-x-hidden">
      {/* Ambient background lighting */}
      <AmbientBackground />

      {/* Desktop Fixed Left Sidebar */}
      <CourseDetailsSidebar onUpgrade={() => setShowUpgradeModal(true)} />

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
                  <div className="w-8 h-8 rounded-xl bg-[#8e4d2b] text-white flex items-center justify-center font-bold text-sm">
                    L
                  </div>
                  <span className="font-extrabold text-base text-[#0f1b32]">LearnAI</span>
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

              <div className="space-y-1 py-4">
                <a
                  href="/dashboard"
                  className="flex items-center gap-3 px-3 py-2 rounded-xl text-xs font-medium text-gray-700 hover:bg-gray-50"
                >
                  Dashboard
                </a>
                <a
                  href="/learning-path"
                  className="flex items-center gap-3 px-3 py-2 rounded-xl text-xs font-medium text-gray-700 hover:bg-gray-50"
                >
                  My Learning Path
                </a>
                <a
                  href="/explore-courses"
                  className="flex items-center gap-3 px-3 py-2 rounded-xl text-xs font-bold bg-[#ffdbcb]/60 text-[#8e4d2b]"
                >
                  Explore Courses
                </a>
              </div>

              <div className="pt-4 border-t border-gray-100 text-xs text-gray-400">
                © 2024 LearnAI Platform
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0 z-10">
        {/* Top Navigation Bar */}
        <CourseDetailsTopBar
          onToggleMobileMenu={() => setMobileMenuOpen(true)}
          searchQuery={searchQuery}
          onSearchChange={(q) => {
            setSearchQuery(q);
            if (q.trim()) {
              showToastNotice(`Searching for "${q}"...`);
            }
          }}
          onScrollToCurriculum={scrollToCurriculum}
          onShowMentors={() => setShowMentorsModal(true)}
          onShowReviews={() => setShowReviewsModal(true)}
        />

        {/* Page Content Body */}
        <main className="flex-1 px-4 sm:px-8 py-6 sm:py-8 max-w-7xl w-full mx-auto space-y-6">
          {/* Breadcrumb Navigation */}
          <CourseBreadcrumbs />

          {/* 12-Column Layout */}
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 sm:gap-8 items-start">
            {/* Primary Content (8 Columns on lg) */}
            <div className="lg:col-span-8 space-y-6">
              {/* Course Hero Card */}
              <CourseHeroCard
                isAdded={isAdded}
                isFavorite={isFavorite}
                onToggleAdd={handleToggleAdd}
                onStartCourse={handleStartCourse}
                onToggleFavorite={handleToggleFavorite}
              />

              {/* AI Recommendation Explanation Card */}
              <AIRecommendationCard />

              {/* Roadmap Connection Timeline */}
              <RoadmapConnection />

              {/* Bottom 2-Card Row: Skills You'll Gain & Projected Impact */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-5 sm:gap-6 items-stretch">
                <SkillsGainCard />
                <ProjectedImpactCard />
              </div>
            </div>

            {/* Right Sidebar: Curriculum & Learning Path Status (4 Columns on lg) */}
            <div className="lg:col-span-4 space-y-6 lg:sticky lg:top-24">
              {/* Interactive Curriculum Card */}
              <CurriculumCard onContinueModule={handleContinueModule} />

              {/* Learning Path Status Card */}
              <LearningPathStatusCard onContinuePath={() => navigate('/learning-path')} />
            </div>
          </div>
        </main>
      </div>

      {/* Modal: Start Course */}
      <AnimatePresence>
        {showStartModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setShowStartModal(false)}
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
                    <Play className="w-4 h-4 fill-[#8e4d2b]" />
                  </div>
                  <h3 className="text-base font-bold text-[#0f1b32]">
                    Starting Course
                  </h3>
                </div>
                <button
                  type="button"
                  aria-label="Close modal"
                  onClick={() => setShowStartModal(false)}
                  className="text-gray-400 hover:text-gray-600 p-1"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>

              <div className="p-3.5 rounded-2xl bg-[#FAF4F0] border border-[#F2DACB]/60 space-y-1">
                <span className="text-[11px] font-bold uppercase tracking-wider text-[#8e4d2b]">
                  Next Active Module
                </span>
                <p className="text-sm font-bold text-[#0f1b32]">
                  2. Linked Lists — Singly, Doubly, Circular
                </p>
                <p className="text-xs text-gray-500">
                  Ready to resume with your interactive code sandbox.
                </p>
              </div>

              <div className="flex items-center gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => {
                    setShowStartModal(false);
                    showToastNotice('Launching module sandbox...');
                  }}
                  className="flex-1 py-3 rounded-2xl bg-[#8e4d2b] hover:bg-[#783e20] text-white text-xs font-bold transition-colors cursor-pointer shadow-sm text-center"
                >
                  Launch Workspace
                </button>
                <button
                  type="button"
                  onClick={() => setShowStartModal(false)}
                  className="px-4 py-3 rounded-2xl bg-gray-100 hover:bg-gray-200 text-gray-700 text-xs font-bold transition-colors cursor-pointer text-center"
                >
                  Cancel
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Modal: Mentors */}
      <AnimatePresence>
        {showMentorsModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setShowMentorsModal(false)}
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
                    <Users className="w-4 h-4 text-[#8e4d2b]" />
                  </div>
                  <h3 className="text-base font-bold text-[#0f1b32]">Course Mentors</h3>
                </div>
                <button
                  type="button"
                  aria-label="Close modal"
                  onClick={() => setShowMentorsModal(false)}
                  className="text-gray-400 hover:text-gray-600 p-1"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>

              <div className="space-y-2.5 text-xs">
                <div className="p-3 rounded-2xl bg-gray-50 border border-gray-100 flex items-center gap-3">
                  <div className="w-9 h-9 rounded-full bg-[#ffdbcb] text-[#8e4d2b] font-bold flex items-center justify-center text-xs">
                    DR
                  </div>
                  <div>
                    <p className="font-bold text-[#0f1b32]">Dr. Alex Rivers</p>
                    <p className="text-gray-500 text-[11px]">Staff Software Engineer • Ex-Google</p>
                  </div>
                </div>
                <div className="p-3 rounded-2xl bg-gray-50 border border-gray-100 flex items-center gap-3">
                  <div className="w-9 h-9 rounded-full bg-[#e1d8fe] text-[#615a7a] font-bold flex items-center justify-center text-xs">
                    EM
                  </div>
                  <div>
                    <p className="font-bold text-[#0f1b32]">Elena Morales</p>
                    <p className="text-gray-500 text-[11px]">Algorithms Research Lead • Stanford</p>
                  </div>
                </div>
              </div>

              <button
                type="button"
                onClick={() => setShowMentorsModal(false)}
                className="w-full py-2.5 rounded-xl bg-[#8e4d2b] text-white text-xs font-bold hover:bg-[#783e20] transition-colors"
              >
                Done
              </button>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Modal: Reviews */}
      <AnimatePresence>
        {showReviewsModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setShowReviewsModal(false)}
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
                    <MessageSquare className="w-4 h-4 text-[#8e4d2b]" />
                  </div>
                  <h3 className="text-base font-bold text-[#0f1b32]">Learner Reviews (4.8 / 5)</h3>
                </div>
                <button
                  type="button"
                  aria-label="Close modal"
                  onClick={() => setShowReviewsModal(false)}
                  className="text-gray-400 hover:text-gray-600 p-1"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>

              <div className="space-y-2.5 text-xs text-gray-600">
                <div className="p-3 rounded-2xl bg-gray-50 border border-gray-100">
                  <p className="font-bold text-[#0f1b32]">“The step-by-step pointers module is gold.”</p>
                  <p className="text-[11px] text-gray-500 mt-1">— Priya K., SWE at Uber</p>
                </div>
                <div className="p-3 rounded-2xl bg-gray-50 border border-gray-100">
                  <p className="font-bold text-[#0f1b32]">“Helped me bridge my DSA gap in just 3 weeks.”</p>
                  <p className="text-[11px] text-gray-500 mt-1">— Marcus T., Junior Developer</p>
                </div>
              </div>

              <button
                type="button"
                onClick={() => setShowReviewsModal(false)}
                className="w-full py-2.5 rounded-xl bg-[#8e4d2b] text-white text-xs font-bold hover:bg-[#783e20] transition-colors"
              >
                Close
              </button>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Modal: Upgrade to Pro */}
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
                Unlock 1-on-1 AI Code Reviews, algorithmic mock interviews, and priority roadmap synchronization.
              </p>

              <button
                type="button"
                onClick={() => {
                  setShowUpgradeModal(false);
                  showToastNotice('Upgrade feature will be connected with billing soon!');
                }}
                className="w-full py-3 rounded-2xl bg-[#8e4d2b] hover:bg-[#783e20] text-white text-xs font-bold transition-colors cursor-pointer shadow-sm"
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
