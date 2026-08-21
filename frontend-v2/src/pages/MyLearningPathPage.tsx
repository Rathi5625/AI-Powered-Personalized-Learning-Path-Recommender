import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Sparkles, RefreshCw, Target, Calendar } from 'lucide-react';
import { AmbientBackground } from '../components/landing/AmbientBackground';

import { LearningPathSidebar } from '../components/learning-path/LearningPathSidebar';
import { LearningPathTopBar } from '../components/learning-path/LearningPathTopBar';
import { CareerGoalCard } from '../components/learning-path/CareerGoalCard';
import { JourneyHeader, type JourneyFilter } from '../components/learning-path/JourneyHeader';
import { RoadmapTimeline } from '../components/learning-path/RoadmapTimeline';
import { type RoadmapPhase } from '../components/learning-path/RoadmapPhaseCard';
import { AIRecommendationCard } from '../components/learning-path/AIRecommendationCard';
import { ProgressOverviewCard } from '../components/learning-path/ProgressOverviewCard';
import { LearningStreakCard } from '../components/learning-path/LearningStreakCard';
import api from '../api/client';
import { LearningPathFullResponse, WeeklyLearningPlanDto, SkillGapDetailDto } from '../api/types';

export const MyLearningPathPage: React.FC = () => {
  const [filter, setFilter] = useState<JourneyFilter>('all');
  const [toast, setToast] = useState<string | null>(null);
  const [showAnalysisModal, setShowAnalysisModal] = useState(false);
  const [showWhyModal, setShowWhyModal] = useState(false);
  const [showWeeklyModal, setShowWeeklyModal] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [isRecalculating, setIsRecalculating] = useState(false);

  const [pathData, setPathData] = useState<LearningPathFullResponse | null>(null);
  const [weeklyPlan, setWeeklyPlan] = useState<WeeklyLearningPlanDto | null>(null);
  const [skillGaps, setSkillGaps] = useState<SkillGapDetailDto[]>([]);
  const [loading, setLoading] = useState(true);

  const loadLearningPathData = async () => {
    try {
      setLoading(true);
      const [path, plan, gaps] = await Promise.all([
        api.getLearningPath().catch(() => null),
        api.getLearningPathWeeklyPlan().catch(() => null),
        api.getLearningPathSkillGaps().catch(() => []),
      ]);

      if (path) setPathData(path);
      if (plan) setWeeklyPlan(plan);
      if (gaps) setSkillGaps(gaps);
    } catch (err) {
      console.error('Failed to load learning path data:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadLearningPathData();
  }, []);

  const showToastNotice = (msg: string) => {
    setToast(msg);
    setTimeout(() => setToast(null), 3000);
  };

  const handleRecalculate = async () => {
    try {
      setIsRecalculating(true);
      const updated = await api.recalculateLearningPath('Learner initiated curriculum recalculation');
      setPathData(updated);
      showToastNotice(`Path calibrated! Updated to Version ${updated.version}`);
    } catch (err) {
      console.error('Recalculation error:', err);
      showToastNotice('Failed to recalculate path. Please try again.');
    } finally {
      setIsRecalculating(false);
    }
  };

  const handleContinueLearning = (phase?: RoadmapPhase) => {
    const title = phase ? phase.title : 'Data Structures & Algorithms';
    showToastNotice(`Continuing ${title}`);
  };

  // Convert real nodes into structured Roadmap Phases
  const phases: RoadmapPhase[] = React.useMemo(() => {
    if (!pathData || !pathData.nodes || pathData.nodes.length === 0) {
      return [];
    }

    const phaseMap: Record<number, RoadmapPhase> = {
      1: {
        id: 'phase-1',
        phaseNumber: '01',
        title: 'Phase 1: Foundations & Core Concepts',
        status: 'in_progress',
        progressPercent: 0,
        skills: [],
      },
      2: {
        id: 'phase-2',
        phaseNumber: '02',
        title: 'Phase 2: Algorithmic & Architectural Depth',
        status: 'upcoming',
        progressPercent: 0,
        skills: [],
      },
      3: {
        id: 'phase-3',
        phaseNumber: '03',
        title: 'Phase 3: Advanced Systems & Capstone',
        status: 'upcoming',
        progressPercent: 0,
        skills: [],
      },
    };

    pathData.nodes.forEach((node) => {
      const pNum = node.phaseNumber || 1;
      if (!phaseMap[pNum]) {
        phaseMap[pNum] = {
          id: `phase-${pNum}`,
          phaseNumber: `0${pNum}`,
          title: node.phaseTitle || `Phase ${pNum}`,
          status: 'upcoming',
          progressPercent: 0,
          skills: [],
        };
      }

      let skillStatus: 'completed' | 'current' | 'upcoming' = 'upcoming';
      if (node.completed || node.status === 'COMPLETED') {
        skillStatus = 'completed';
      } else if (node.status === 'IN_PROGRESS' || node.status === 'REVISION_REQUIRED') {
        skillStatus = 'current';
      }

      const name = node.skillName || node.title || 'Concept';
      if (!phaseMap[pNum].skills.some((s) => s.name === name)) {
        phaseMap[pNum].skills.push({ name, status: skillStatus });
      }
    });

    return Object.values(phaseMap);
  }, [pathData]);

  const targetRole = pathData?.targetRole || pathData?.targetCareer || 'Software Engineer';
  const overallProgress = pathData ? Math.round(pathData.overallProgress) : 0;
  const estJourney = pathData ? `${Math.ceil(pathData.estimatedTotalHours / 40)} mo` : '6 mo';
  const weeklyHoursText = pathData ? `${pathData.weeklyHours} hrs/wk` : '10 hrs';

  return (
    <div className="relative min-h-screen flex bg-[#f9f9ff] text-[#0f1b32] selection:bg-[#ffdbcb] selection:text-[#8e4d2b] overflow-x-hidden">
      {/* Background Atmosphere */}
      <AmbientBackground />

      {/* Desktop Fixed Left Sidebar */}
      <LearningPathSidebar />

      {/* Toast Notice */}
      <AnimatePresence>
        {toast && (
          <motion.div
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            className="fixed top-5 right-5 z-50 bg-[#8e4d2b] text-white px-4 py-2.5 rounded-2xl shadow-xl text-xs font-bold flex items-center gap-2"
          >
            <Sparkles className="w-4 h-4 text-[#ffdbcb]" />
            <span>{toast}</span>
          </motion.div>
        )}
      </AnimatePresence>

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
                  className="flex items-center gap-3 px-3 py-2 rounded-xl text-xs font-bold bg-[#ffdbcb]/60 text-[#8e4d2b]"
                >
                  My Learning Path
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
        <LearningPathTopBar
          onToggleMobileMenu={() => setMobileMenuOpen(true)}
          onOpenAIMentor={() => showToastNotice('AI Mentor is active with full curriculum context.')}
        />

        <main className="flex-1 px-4 sm:px-8 py-6 sm:py-8 max-w-7xl w-full mx-auto space-y-6 sm:space-y-8">
          {/* Main Title & Action Bar */}
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 text-left">
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-2xl sm:text-3xl font-extrabold text-[#0f1b32] tracking-tight">
                  Personalized Learning Path
                </h1>
                {pathData && (
                  <span className="px-2.5 py-0.5 rounded-full text-[10px] font-extrabold bg-[#FAF4F0] border border-[#F2DACB] text-[#8e4d2b]">
                    v{pathData.version}
                  </span>
                )}
              </div>
              <p className="text-xs sm:text-sm text-gray-500 mt-1 font-normal">
                Continuously calibrated with Bayesian Knowledge Tracing and prerequisite dependency graphs.
              </p>
            </div>

            <div className="flex items-center gap-2.5">
              <button
                type="button"
                onClick={() => setShowWeeklyModal(true)}
                className="px-3.5 py-2 rounded-xl bg-white border border-gray-200 text-[#0f1b32] text-xs font-bold hover:bg-gray-50 shadow-xs flex items-center gap-1.5 transition-colors cursor-pointer"
              >
                <Calendar className="w-3.5 h-3.5 text-[#8e4d2b]" />
                <span>Weekly Plan</span>
              </button>
              <button
                type="button"
                disabled={isRecalculating}
                onClick={handleRecalculate}
                className="px-4 py-2 rounded-xl bg-[#8e4d2b] text-white text-xs font-bold hover:bg-[#783e21] shadow-xs flex items-center gap-1.5 transition-colors cursor-pointer disabled:opacity-50"
              >
                <RefreshCw className={`w-3.5 h-3.5 ${isRecalculating ? 'animate-spin' : ''}`} />
                <span>{isRecalculating ? 'Calibrating...' : 'Recalculate Path'}</span>
              </button>
            </div>
          </div>

          {/* Target Role & Career Stats Card */}
          <CareerGoalCard
            targetRole={targetRole}
            progress={overallProgress}
            estJourney={estJourney}
            weeklyHours={weeklyHoursText}
            onViewAnalysis={() => setShowAnalysisModal(true)}
          />

          {/* 2-Column Dashboard Composition */}
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 sm:gap-8 items-start">
            {/* Left Column: Vertical Roadmap */}
            <div className="lg:col-span-8 space-y-6">
              <JourneyHeader
                currentFilter={filter}
                onFilterChange={setFilter}
              />

              {loading ? (
                <div className="bg-white/75 backdrop-blur-2xl rounded-3xl p-12 text-center text-gray-400 border border-white/90 shadow-sm">
                  <Sparkles className="w-6 h-6 animate-pulse mx-auto mb-2 text-[#8e4d2b]" />
                  <p className="text-sm font-semibold">Generating optimized curriculum graph...</p>
                </div>
              ) : (
                <RoadmapTimeline
                  phases={phases}
                  filter={filter}
                  onContinuePhase={handleContinueLearning}
                />
              )}
            </div>

            {/* Right Column: AI Insights & Quality Breakdown */}
            <div className="lg:col-span-4 space-y-5">
              <AIRecommendationCard
                onContinueLearning={() => handleContinueLearning()}
                onAskAIWhy={() => setShowWhyModal(true)}
              />

              {/* Real Quality Score Card */}
              {pathData && (
                <section className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-5 sm:p-6 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-3">
                  <div className="flex items-center justify-between">
                    <h3 className="text-sm font-extrabold text-[#0f1b32]">Path Quality Score</h3>
                    <span className="text-sm font-black text-[#8e4d2b]">{Math.round(pathData.qualityScore)}%</span>
                  </div>
                  <div className="space-y-2 text-xs">
                    {Object.entries(pathData.qualityBreakdown || {}).map(([key, val]) => (
                      <div key={key} className="flex items-center justify-between text-gray-500">
                        <span className="capitalize">{key.replace(/([A-Z])/g, ' $1')}</span>
                        <span className="font-bold text-[#0f1b32]">{Math.round(val)}%</span>
                      </div>
                    ))}
                  </div>
                </section>
              )}

              <ProgressOverviewCard />
              <LearningStreakCard streakDays={7} />
            </div>
          </div>
        </main>
      </div>

      {/* Modal 1: AI Path Analysis & Real Skill Gaps */}
      <AnimatePresence>
        {showAnalysisModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setShowAnalysisModal(false)}
              className="fixed inset-0 bg-black/40 backdrop-blur-xs"
            />
            <motion.div
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              className="relative w-full max-w-lg bg-white rounded-3xl p-6 sm:p-7 shadow-2xl border border-gray-100 z-10 text-left space-y-4 max-h-[85vh] overflow-y-auto"
            >
              <div className="flex items-center justify-between pb-3 border-b border-gray-100">
                <div className="flex items-center gap-2">
                  <div className="w-7 h-7 rounded-lg bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#8e4d2b]">
                    <Target className="w-3.5 h-3.5" />
                  </div>
                  <h3 className="text-base font-bold text-[#0f1b32]">Real Skill Gap Analysis</h3>
                </div>
                <button
                  type="button"
                  onClick={() => setShowAnalysisModal(false)}
                  className="text-gray-400 hover:text-gray-600 p-1"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>

              <div className="space-y-3">
                {skillGaps.map((gap) => (
                  <div key={gap.skill} className="p-3 rounded-2xl bg-gray-50 border border-gray-100 flex items-center justify-between">
                    <div>
                      <span className="font-bold text-xs text-[#0f1b32] block">{gap.skill}</span>
                      <span className="text-[10px] text-gray-400">
                        Current: {Math.round(gap.currentMastery * 100)}% • Target: {Math.round(gap.requiredLevel * 100)}%
                      </span>
                    </div>
                    <span
                      className={`text-[10px] font-extrabold px-2 py-0.5 rounded-full ${
                        gap.status === 'MASTERED'
                          ? 'bg-emerald-50 text-emerald-600 border border-emerald-200'
                          : gap.status === 'PROFICIENT'
                          ? 'bg-blue-50 text-blue-600 border border-blue-200'
                          : gap.status === 'REVISION_REQUIRED'
                          ? 'bg-rose-50 text-rose-600 border border-rose-200'
                          : 'bg-amber-50 text-amber-600 border border-amber-200'
                      }`}
                    >
                      {gap.status}
                    </span>
                  </div>
                ))}
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Modal 2: Weekly Schedule Plan */}
      <AnimatePresence>
        {showWeeklyModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setShowWeeklyModal(false)}
              className="fixed inset-0 bg-black/40 backdrop-blur-xs"
            />
            <motion.div
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              className="relative w-full max-w-xl bg-white rounded-3xl p-6 sm:p-7 shadow-2xl border border-gray-100 z-10 text-left space-y-4 max-h-[85vh] overflow-y-auto"
            >
              <div className="flex items-center justify-between pb-3 border-b border-gray-100">
                <div className="flex items-center gap-2">
                  <div className="w-7 h-7 rounded-lg bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#8e4d2b]">
                    <Calendar className="w-3.5 h-3.5" />
                  </div>
                  <h3 className="text-base font-bold text-[#0f1b32]">Weekly Learning Plan</h3>
                </div>
                <button
                  type="button"
                  onClick={() => setShowWeeklyModal(false)}
                  className="text-gray-400 hover:text-gray-600 p-1"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>

              {weeklyPlan ? (
                <div className="space-y-3.5">
                  <div className="p-3 rounded-2xl bg-[#FAF4F0] border border-[#F2DACB] text-xs text-[#8e4d2b] flex items-center justify-between">
                    <span className="font-bold">Focus: {weeklyPlan.focusTopic}</span>
                    <span>{weeklyPlan.scheduledMinutes} / {weeklyPlan.weeklyTargetMinutes} min planned</span>
                  </div>

                  <div className="space-y-3">
                    {weeklyPlan.days.map((day) => (
                      <div key={day.dayName} className="p-3.5 rounded-2xl bg-gray-50 border border-gray-100">
                        <div className="flex items-center justify-between mb-2">
                          <span className="font-extrabold text-xs text-[#0f1b32]">{day.dayName}</span>
                          <span className="text-[10px] text-gray-400 font-bold">{day.allocatedMinutes} min</span>
                        </div>
                        <div className="space-y-1.5">
                          {day.activities.map((act) => (
                            <div key={act.id} className="text-xs text-gray-600 flex items-center gap-2">
                              <span className="w-1.5 h-1.5 rounded-full bg-[#8e4d2b]" />
                              <span>{act.title}</span>
                              <span className="text-[10px] text-gray-400 font-mono">({act.estimatedMinutes}m)</span>
                            </div>
                          ))}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              ) : (
                <p className="text-xs text-gray-400">Loading weekly plan...</p>
              )}
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Modal 3: Why Was This Recommended? */}
      <AnimatePresence>
        {showWhyModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setShowWhyModal(false)}
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
                  <div className="w-7 h-7 rounded-lg bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#8e4d2b]">
                    <Sparkles className="w-3.5 h-3.5" />
                  </div>
                  <h3 className="text-base font-bold text-[#0f1b32]">Why Was This Selected?</h3>
                </div>
                <button
                  type="button"
                  onClick={() => setShowWhyModal(false)}
                  className="text-gray-400 hover:text-gray-600 p-1"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>

              <div className="space-y-3 text-xs text-gray-600 leading-relaxed">
                <p>
                  LearnAI evaluates <strong>10 candidate signals</strong> via a trained <strong>GradientBoosting model</strong>, cross-referenced with your <strong>Bayesian Knowledge Tracing</strong> probability states.
                </p>
                <div className="p-3 rounded-2xl bg-gray-50 border border-gray-100 space-y-1">
                  <span className="font-bold text-[#0f1b32] block">Current Focus Recommendation:</span>
                  <p className="text-gray-500">
                    {pathData?.nodes?.find((n) => n.status === 'IN_PROGRESS')?.reason ||
                      'Bridges highest priority skill gap while respecting prerequisite mastery gates.'}
                  </p>
                </div>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
};
