import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Sparkles, LogOut, Compass, CheckCircle2, ArrowRight } from 'lucide-react';
import { AmbientBackground } from '../components/landing/AmbientBackground';
import { SkillsSidebar } from '../components/skills/SkillsSidebar';
import { SkillsPageHeader } from '../components/skills/SkillsPageHeader';
import { CareerTargetCard } from '../components/skills/CareerTargetCard';
import { AISkillAnalysisCard } from '../components/skills/AISkillAnalysisCard';
import { SkillGapAnalysisCard, type SkillGapItem } from '../components/skills/SkillGapAnalysisCard';
import { RecommendedImprovementPlan } from '../components/skills/RecommendedImprovementPlan';
import { api } from '../api/client';
import { DashboardAggregated } from '../api/types';

export const SkillsPage: React.FC = () => {
  const navigate = useNavigate();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [showNewCourseModal, setShowNewCourseModal] = useState(false);
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [activeImproveSkill, setActiveImproveSkill] = useState<SkillGapItem | null>(null);
  const [toast, setToast] = useState<string | null>(null);
  const [dashboardData, setDashboardData] = useState<DashboardAggregated | null>(null);

  useEffect(() => {
    const loadData = async () => {
      try {
        const data = await api.getDashboardData();
        setDashboardData(data);
      } catch (err) {
        console.error('Failed to load skills dashboard context:', err);
      }
    };
    loadData();
  }, []);

  const showToastNotice = (msg: string) => {
    setToast(msg);
    setTimeout(() => setToast(null), 3000);
  };

  const handleImproveSkill = (skill: SkillGapItem) => {
    setActiveImproveSkill(skill);
  };

  const handleSelectTopic = (topic: string, stage: string) => {
    if (stage === 'NOW') {
      showToastNotice(`Starting ${topic} learning module.`);
    } else if (stage === 'NEXT') {
      showToastNotice(`${topic} is your next recommended topic.`);
    } else {
      showToastNotice(`${topic} is planned next in your curriculum.`);
    }
  };

  return (
    <div className="relative min-h-screen flex bg-[#f9f9ff] text-[#0f1b32] selection:bg-[#ffdbcb] selection:text-[#8e4d2b] overflow-x-hidden">
      {/* Atmosphere Background */}
      <AmbientBackground />

      {/* Desktop Fixed Left Sidebar */}
      <SkillsSidebar
        onNewCourse={() => setShowNewCourseModal(true)}
        onLogout={() => setShowLogoutModal(true)}
      />

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
                  <div className="w-8 h-8 rounded-full bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#8e4d2b]">
                    <Sparkles className="w-4 h-4 text-[#8e4d2b]" />
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

              <div className="space-y-1.5 py-4">
                <Link
                  to="/dashboard"
                  className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-medium text-gray-700 hover:bg-gray-50"
                >
                  Dashboard
                </Link>
                <Link
                  to="/skills"
                  className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-bold bg-[#FAF4F0] text-[#8e4d2b] border-l-4 border-[#8e4d2b]"
                >
                  Skills
                </Link>
                <Link
                  to="/calendar"
                  className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-medium text-gray-700 hover:bg-gray-50"
                >
                  Calendar
                </Link>
                <Link
                  to="/settings"
                  className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-medium text-gray-700 hover:bg-gray-50"
                >
                  Settings
                </Link>
                <Link
                  to="/help"
                  className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-medium text-gray-700 hover:bg-gray-50"
                >
                  Support
                </Link>
              </div>

              <div className="space-y-2 pt-4 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => {
                    setMobileMenuOpen(false);
                    setShowNewCourseModal(true);
                  }}
                  className="w-full py-2.5 rounded-xl bg-[#8e4d2b] text-white text-xs font-bold text-center"
                >
                  New Course
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Main Content Workspace */}
      <div className="flex-1 flex flex-col min-w-0 z-10">
        <main className="flex-1 px-4 sm:px-8 py-6 sm:py-8 max-w-7xl w-full mx-auto space-y-6 sm:space-y-8">
          {/* Header */}
          <SkillsPageHeader onToggleMobileMenu={() => setMobileMenuOpen(true)} />

          {/* Top Section: Career Target (8 cols) + AI Analysis (4 cols) */}
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 sm:gap-8 items-stretch">
            <div className="lg:col-span-8 flex flex-col">
              <CareerTargetCard
                careerTarget={dashboardData?.targetCareer || 'Software Engineer'}
                readinessScore={dashboardData?.profileCompletionPercentage || 0}
                requiredSkillsCount={dashboardData?.topSkills ? dashboardData.topSkills.length : 0}
                strongSkillsCount={dashboardData?.topSkills ? dashboardData.topSkills.filter(s => (s.confidence || 0) >= 0.75).length : 0}
                criticalGapsCount={dashboardData?.topSkills ? dashboardData.topSkills.filter(s => (s.confidence || 0) < 0.5).length : 0}
              />
            </div>
            <div className="lg:col-span-4 flex flex-col">
              <AISkillAnalysisCard
                skills={dashboardData?.topSkills?.map(s => ({ name: s.skillName, percentage: Math.round((s.confidence || 0) * 100) })) || []}
              />
            </div>
          </div>

          {/* Full-width: Skill Gap Analysis */}
          <SkillGapAnalysisCard onImproveSkill={handleImproveSkill} />

          {/* Full-width: Recommended Improvement Plan */}
          <RecommendedImprovementPlan onSelectTopic={handleSelectTopic} />
        </main>
      </div>

      {/* Modal 1: New Course Dialog */}
      <AnimatePresence>
        {showNewCourseModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setShowNewCourseModal(false)}
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
                    <Compass className="w-4 h-4 text-[#8e4d2b]" />
                  </div>
                  <h3 className="text-base font-bold text-[#0f1b32]">Explore &amp; Add Courses</h3>
                </div>
                <button
                  type="button"
                  aria-label="Close modal"
                  onClick={() => setShowNewCourseModal(false)}
                  className="text-gray-400 hover:text-gray-600 p-1"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>

              <p className="text-xs sm:text-sm text-gray-600 leading-relaxed font-normal">
                Discover courses curated by AI to close your skill gaps faster and boost your Software Engineer readiness score.
              </p>

              <div className="space-y-2 pt-1">
                <button
                  type="button"
                  onClick={() => {
                    setShowNewCourseModal(false);
                    navigate('/course-details');
                  }}
                  className="w-full p-3 rounded-2xl bg-[#FAF4F0] hover:bg-[#F2DACB]/60 border border-[#F2DACB] flex items-center justify-between transition-colors text-left group"
                >
                  <div>
                    <span className="text-[10px] font-bold uppercase tracking-wider text-[#8e4d2b] block">
                      Recommended Spec
                    </span>
                    <p className="text-xs font-bold text-[#0f1b32] group-hover:text-[#8e4d2b]">
                      Data Structures &amp; Algorithms
                    </p>
                  </div>
                  <ArrowRight className="w-4 h-4 text-[#8e4d2b]" />
                </button>

                <button
                  type="button"
                  onClick={() => {
                    setShowNewCourseModal(false);
                    navigate('/explore-courses');
                  }}
                  className="w-full py-3 rounded-2xl bg-[#8e4d2b] hover:bg-[#783e20] text-white text-xs font-bold transition-colors cursor-pointer shadow-sm text-center"
                >
                  Explore All Recommended Courses
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Modal 2: Improve Skill Plan */}
      <AnimatePresence>
        {activeImproveSkill && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setActiveImproveSkill(null)}
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
                  <h3 className="text-base font-bold text-[#0f1b32]">
                    Improve {activeImproveSkill.name}
                  </h3>
                </div>
                <button
                  type="button"
                  aria-label="Close modal"
                  onClick={() => setActiveImproveSkill(null)}
                  className="text-gray-400 hover:text-gray-600 p-1"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>

              <div className="p-3.5 rounded-2xl bg-gray-50 border border-gray-100 space-y-2 text-xs">
                <div className="flex items-center justify-between">
                  <span className="text-gray-500 font-medium">Target Proficiency</span>
                  <span className="font-bold text-[#8e4d2b]">
                    {activeImproveSkill.currentPercentage}% → {activeImproveSkill.requiredPercentage}%
                  </span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-gray-500 font-medium">Priority</span>
                  <span
                    className={`font-bold ${
                      activeImproveSkill.priority === 'Critical' ? 'text-red-600' : 'text-[#8e4d2b]'
                    }`}
                  >
                    {activeImproveSkill.priority}
                  </span>
                </div>
              </div>

              <p className="text-xs sm:text-sm text-gray-600 leading-relaxed font-normal">
                AI has generated targeted practice sets and selected curated course modules to bring your proficiency to{' '}
                <strong className="text-[#0f1b32] font-bold">{activeImproveSkill.requiredPercentage}%</strong>.
              </p>

              <div className="flex items-center gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => {
                    const name = activeImproveSkill.name;
                    setActiveImproveSkill(null);
                    showToastNotice(`Improvement plan generated for ${name}.`);
                    if (name.includes('Data Structures')) {
                      navigate('/course-details');
                    } else {
                      navigate('/learning-path');
                    }
                  }}
                  className="flex-1 py-3 rounded-2xl bg-[#8e4d2b] hover:bg-[#783e20] text-white text-xs font-bold transition-colors cursor-pointer shadow-sm text-center"
                >
                  View Learning Path Module
                </button>
                <button
                  type="button"
                  onClick={() => setActiveImproveSkill(null)}
                  className="px-4 py-3 rounded-2xl bg-gray-100 hover:bg-gray-200 text-gray-700 text-xs font-bold transition-colors cursor-pointer text-center"
                >
                  Close
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Modal 3: Logout Confirmation */}
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
              className="relative w-full max-w-sm bg-white rounded-3xl p-6 sm:p-7 shadow-2xl border border-gray-100 z-10 text-left space-y-4"
            >
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-2xl bg-red-50 text-red-600 flex items-center justify-center">
                  <LogOut className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-base font-bold text-[#0f1b32]">Log Out</h3>
                  <p className="text-xs text-gray-500">Are you sure you want to sign out?</p>
                </div>
              </div>

              <div className="flex items-center gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => {
                    setShowLogoutModal(false);
                    navigate('/login');
                  }}
                  className="flex-1 py-2.5 rounded-xl bg-red-600 hover:bg-red-700 text-white text-xs font-bold transition-colors cursor-pointer shadow-sm text-center"
                >
                  Log Out
                </button>
                <button
                  type="button"
                  onClick={() => setShowLogoutModal(false)}
                  className="flex-1 py-2.5 rounded-xl bg-gray-100 hover:bg-gray-200 text-gray-700 text-xs font-bold transition-colors cursor-pointer text-center"
                >
                  Cancel
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Live Action Toast Notification */}
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
