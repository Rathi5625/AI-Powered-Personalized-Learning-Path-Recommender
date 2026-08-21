import React, { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Sparkles, CheckCircle2, Play, RefreshCw, History } from 'lucide-react';
import { AmbientBackground } from '../components/landing/AmbientBackground';
import { AssessmentsSidebar } from '../components/assessments/AssessmentsSidebar';
import { AssessmentsTopBar } from '../components/assessments/AssessmentsTopBar';
import { AssessmentsPageHeader } from '../components/assessments/AssessmentsPageHeader';
import { AssessmentOverviewCard } from '../components/assessments/AssessmentOverviewCard';
import { AISkillInsightsCard } from '../components/assessments/AISkillInsightsCard';
import { RecommendedAssessmentCard } from '../components/assessments/RecommendedAssessmentCard';
import { AssessmentFilters, type AssessmentFilterType } from '../components/assessments/AssessmentFilters';
import { AssessmentCard, type AssessmentItem } from '../components/assessments/AssessmentCard';
import { RecentResultsCard } from '../components/assessments/RecentResultsCard';
import { PathImpactCard } from '../components/assessments/PathImpactCard';

const INITIAL_ASSESSMENTS: AssessmentItem[] = [
  {
    id: 'java',
    title: 'Java Fundamentals',
    estimatedKnowledge: 82,
    duration: '15m',
    difficulty: 'Advanced',
    actionType: 'Retake',
    isRecommended: false,
    needsImprovement: false,
  },
  {
    id: 'system-design',
    title: 'System Design',
    estimatedKnowledge: 41,
    duration: '30m',
    difficulty: 'Hard',
    actionType: 'Assess',
    isRecommended: true,
    needsImprovement: true,
  },
  {
    id: 'spring-boot',
    title: 'Spring Boot',
    estimatedKnowledge: null,
    duration: '20m',
    difficulty: 'Interm.',
    actionType: 'Assess',
    isRecommended: true,
    needsImprovement: false,
  },
  {
    id: 'sql',
    title: 'SQL Databases',
    estimatedKnowledge: 75,
    duration: '15m',
    difficulty: 'Interm.',
    actionType: 'Retake',
    isRecommended: false,
    needsImprovement: false,
  },
];

export const AssessmentsPage: React.FC = () => {
  const navigate = useNavigate();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [currentFilter, setCurrentFilter] = useState<AssessmentFilterType>('All');
  const [toast, setToast] = useState<string | null>(null);

  // Modals state
  const [showUpgradeModal, setShowUpgradeModal] = useState(false);
  const [showStartModal, setShowStartModal] = useState(false);
  const [showHistoryModal, setShowHistoryModal] = useState(false);
  const [activeActionAssessment, setActiveActionAssessment] = useState<AssessmentItem | null>(null);

  const showToastNotice = (msg: string) => {
    setToast(msg);
    setTimeout(() => setToast(null), 3000);
  };

  // Filter and search computation
  const filteredAssessments = useMemo(() => {
    return INITIAL_ASSESSMENTS.filter((item) => {
      // Search matching
      const matchesSearch = item.title.toLowerCase().includes(searchQuery.toLowerCase());
      if (!matchesSearch) return false;

      // Filter matching
      if (currentFilter === 'Recommended') {
        return !!item.isRecommended;
      }
      if (currentFilter === 'Not Assessed') {
        return item.estimatedKnowledge === null;
      }
      if (currentFilter === 'Needs Improvement') {
        return !!item.needsImprovement || (item.estimatedKnowledge !== null && item.estimatedKnowledge < 50);
      }
      return true;
    });
  }, [searchQuery, currentFilter]);

  const handleAssessmentAction = (assessment: AssessmentItem) => {
    setActiveActionAssessment(assessment);
  };

  const handleOptimizePath = () => {
    showToastNotice('Learning path optimized based on latest assessment data!');
  };

  return (
    <div className="relative min-h-screen flex bg-[#f9f9ff] text-[#0f1b32] selection:bg-[#ffdbcb] selection:text-[#8e4d2b] overflow-x-hidden">
      {/* Atmosphere background lighting */}
      <AmbientBackground />

      {/* Desktop Fixed Left Sidebar */}
      <AssessmentsSidebar onUpgrade={() => setShowUpgradeModal(true)} />

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
                  className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-bold bg-[#ffdbcb]/60 text-[#8e4d2b]"
                >
                  Assessments
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
        <AssessmentsTopBar
          onToggleMobileMenu={() => setMobileMenuOpen(true)}
          searchQuery={searchQuery}
          onSearchChange={(q) => setSearchQuery(q)}
          onOpenAIMentor={() => showToastNotice('AI Mentor conversation is opening...')}
        />

        {/* Page Content Body */}
        <main className="flex-1 px-4 sm:px-8 py-6 sm:py-8 max-w-7xl w-full mx-auto space-y-6 sm:space-y-8">
          {/* Header */}
          <AssessmentsPageHeader />

          {/* Top Section: Overview (8 cols) + AI Insights (4 cols) */}
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 sm:gap-8 items-stretch">
            <div className="lg:col-span-8 flex flex-col">
              <AssessmentOverviewCard />
            </div>
            <div className="lg:col-span-4 flex flex-col">
              <AISkillInsightsCard onOptimizePath={handleOptimizePath} />
            </div>
          </div>

          {/* Middle Section: Recommended Next Step (DSA) */}
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 sm:gap-8 items-start">
            <div className="lg:col-span-8">
              <RecommendedAssessmentCard
                onStartAssessment={() => setShowStartModal(true)}
              />
            </div>
          </div>

          {/* Lower Section: Filterable Assessments Grid (8 cols) & Side Widgets (4 cols) */}
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 sm:gap-8 items-start">
            {/* Left 8 Cols: Filters & Cards Grid */}
            <div className="lg:col-span-8 space-y-5">
              {/* Filter Pills */}
              <AssessmentFilters
                currentFilter={currentFilter}
                onFilterChange={setCurrentFilter}
              />

              {/* 2-Column Cards Grid */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 sm:gap-5">
                {filteredAssessments.length > 0 ? (
                  filteredAssessments.map((assessment) => (
                    <AssessmentCard
                      key={assessment.id}
                      assessment={assessment}
                      onAction={handleAssessmentAction}
                    />
                  ))
                ) : (
                  <div className="col-span-2 p-8 text-center bg-white/50 rounded-3xl border border-gray-100 text-xs text-gray-400">
                    No assessments match your current filter.
                  </div>
                )}
              </div>
            </div>

            {/* Right 4 Cols: Recent Results & Path Impact */}
            <div className="lg:col-span-4 space-y-6">
              <RecentResultsCard onViewHistory={() => setShowHistoryModal(true)} />
              <PathImpactCard />
            </div>
          </div>
        </main>
      </div>

      {/* Modal 1: Start Recommended Assessment (DSA) */}
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
                    Data Structures &amp; Algorithms
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

              <div className="grid grid-cols-3 gap-2 p-3.5 rounded-2xl bg-[#FAF4F0] border border-[#F2DACB]/60 text-center text-xs">
                <div>
                  <span className="text-[10px] text-gray-500 block uppercase tracking-wider">
                    Questions
                  </span>
                  <span className="font-bold text-[#0f1b32]">20 Qs</span>
                </div>
                <div>
                  <span className="text-[10px] text-gray-500 block uppercase tracking-wider">
                    Duration
                  </span>
                  <span className="font-bold text-[#0f1b32]">20 Mins</span>
                </div>
                <div>
                  <span className="text-[10px] text-gray-500 block uppercase tracking-wider">
                    Level
                  </span>
                  <span className="font-bold text-[#8e4d2b]">Intermediate</span>
                </div>
              </div>

              <p className="text-xs text-gray-600 leading-relaxed font-normal">
                This assessment evaluates pointer manipulation, tree traversals, and dynamic programming heuristics to calibrate your roadmap.
              </p>

              <div className="flex items-center gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => {
                    setShowStartModal(false);
                    navigate('/assessment');
                  }}
                  className="flex-1 py-3 rounded-2xl bg-[#8e4d2b] hover:bg-[#783e20] text-white text-xs font-bold transition-colors cursor-pointer shadow-sm text-center"
                >
                  Start Assessment
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

      {/* Modal 2: Assess / Retake Specific Topic */}
      <AnimatePresence>
        {activeActionAssessment && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setActiveActionAssessment(null)}
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
                    {activeActionAssessment.actionType === 'Retake' ? (
                      <RefreshCw className="w-4 h-4 text-[#8e4d2b]" />
                    ) : (
                      <Play className="w-4 h-4 fill-[#8e4d2b]" />
                    )}
                  </div>
                  <h3 className="text-base font-bold text-[#0f1b32]">
                    {activeActionAssessment.title}
                  </h3>
                </div>
                <button
                  type="button"
                  aria-label="Close modal"
                  onClick={() => setActiveActionAssessment(null)}
                  className="text-gray-400 hover:text-gray-600 p-1"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>

              <div className="p-3.5 rounded-2xl bg-gray-50 border border-gray-100 space-y-2 text-xs">
                <div className="flex items-center justify-between">
                  <span className="text-gray-500 font-medium">Est. Knowledge</span>
                  <span className="font-bold text-[#8e4d2b]">
                    {activeActionAssessment.estimatedKnowledge !== null
                      ? `${activeActionAssessment.estimatedKnowledge}%`
                      : 'Unassessed'}
                  </span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-gray-500 font-medium">Duration &amp; Difficulty</span>
                  <span className="font-semibold text-[#0f1b32]">
                    {activeActionAssessment.duration} • {activeActionAssessment.difficulty}
                  </span>
                </div>
              </div>

              <div className="flex items-center gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => {
                    const title = activeActionAssessment.title;
                    setActiveActionAssessment(null);
                    showToastNotice(`Launching test for ${title}...`);
                  }}
                  className="flex-1 py-3 rounded-2xl bg-[#8e4d2b] hover:bg-[#783e20] text-white text-xs font-bold transition-colors cursor-pointer shadow-sm text-center"
                >
                  {activeActionAssessment.actionType === 'Retake' ? 'Confirm Retake' : 'Begin Assessment'}
                </button>
                <button
                  type="button"
                  onClick={() => setActiveActionAssessment(null)}
                  className="px-4 py-3 rounded-2xl bg-gray-100 hover:bg-gray-200 text-gray-700 text-xs font-bold transition-colors cursor-pointer text-center"
                >
                  Cancel
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Modal 3: View History Modal */}
      <AnimatePresence>
        {showHistoryModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setShowHistoryModal(false)}
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
                    <History className="w-4 h-4 text-[#8e4d2b]" />
                  </div>
                  <h3 className="text-base font-bold text-[#0f1b32]">
                    Assessment History
                  </h3>
                </div>
                <button
                  type="button"
                  aria-label="Close modal"
                  onClick={() => setShowHistoryModal(false)}
                  className="text-gray-400 hover:text-gray-600 p-1"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>

              <div className="space-y-2 text-xs">
                <div className="p-3 rounded-2xl bg-gray-50 border border-gray-100 flex items-center justify-between">
                  <div>
                    <p className="font-bold text-[#0f1b32]">React Basics</p>
                    <p className="text-[11px] text-gray-400">Completed 2 days ago</p>
                  </div>
                  <span className="font-extrabold text-[#8e4d2b] text-sm">65% (+5%)</span>
                </div>
                <div className="p-3 rounded-2xl bg-gray-50 border border-gray-100 flex items-center justify-between">
                  <div>
                    <p className="font-bold text-[#0f1b32]">Git Flow &amp; Branching</p>
                    <p className="text-[11px] text-gray-400">Completed 1 week ago</p>
                  </div>
                  <span className="font-extrabold text-[#8e4d2b] text-sm">88% (+12%)</span>
                </div>
                <div className="p-3 rounded-2xl bg-gray-50 border border-gray-100 flex items-center justify-between">
                  <div>
                    <p className="font-bold text-[#0f1b32]">SQL Joins &amp; Indexing</p>
                    <p className="text-[11px] text-gray-400">Completed 2 weeks ago</p>
                  </div>
                  <span className="font-extrabold text-[#8e4d2b] text-sm">75% (+8%)</span>
                </div>
              </div>

              <button
                type="button"
                onClick={() => setShowHistoryModal(false)}
                className="w-full py-2.5 rounded-xl bg-[#8e4d2b] hover:bg-[#783e20] text-white text-xs font-bold transition-colors"
              >
                Close History
              </button>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Modal 4: Upgrade to Pro Modal */}
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
                Unlock unlimited AI diagnostic assessments, detailed question-by-question breakdown, and verified skill badges.
              </p>

              <button
                type="button"
                onClick={() => {
                  setShowUpgradeModal(false);
                  showToastNotice('Pro upgrade will be available soon!');
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
