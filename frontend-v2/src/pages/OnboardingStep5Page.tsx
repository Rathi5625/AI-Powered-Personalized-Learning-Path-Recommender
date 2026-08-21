import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Video,
  BookOpen,
  Laptop,
  Puzzle,
  MousePointerClick,
  ClipboardList,
  Sparkles,
  ArrowRight,
} from 'lucide-react';
import { AmbientBackground } from '../components/landing/AmbientBackground';
import { OnboardingProgress } from '../components/onboarding/OnboardingProgress';
import {
  LearningFormatCard,
  type LearningFormatOption,
} from '../components/onboarding/LearningFormatCard';

const learningFormats: LearningFormatOption[] = [
  {
    id: 'video',
    title: 'Video',
    description: 'Learn through visual explanations and lectures.',
    icon: Video,
    shortLabel: 'Video',
  },
  {
    id: 'reading',
    title: 'Reading',
    description: 'Learn through articles, documentation and written guides.',
    icon: BookOpen,
    shortLabel: 'Reading',
  },
  {
    id: 'projects',
    title: 'Hands-on Projects',
    description: 'Learn by building real things.',
    icon: Laptop,
    shortLabel: 'Projects',
  },
  {
    id: 'practice',
    title: 'Practice',
    description: 'Learn through coding exercises and problems.',
    icon: Puzzle,
    shortLabel: 'Practice',
  },
  {
    id: 'interactive',
    title: 'Interactive Learning',
    description: 'Learn through interactive examples and activities.',
    icon: MousePointerClick,
    shortLabel: 'Interactive',
  },
  {
    id: 'quizzes',
    title: 'Quizzes',
    description: 'Reinforce concepts through questions and assessments.',
    icon: ClipboardList,
    shortLabel: 'Quizzes',
  },
];

const GOAL_OPTIONS = [
  { id: 'career_growth', label: 'Career Growth' },
  { id: 'deep_understanding', label: 'Deep Understanding' },
  { id: 'practical_skills', label: 'Practical Skills' },
  { id: 'certifications', label: 'Certifications' },
  { id: 'interview_prep', label: 'Interview Preparation' },
  { id: 'personal_growth', label: 'Personal Growth' },
];

export const OnboardingStep5Page: React.FC = () => {
  const navigate = useNavigate();
  const [selectedFormatIds, setSelectedFormatIds] = useState<string[]>(['video', 'projects']);
  const [selectedGoalIds, setSelectedGoalIds] = useState<string[]>([
    'career_growth',
    'practical_skills',
  ]);

  const toggleFormat = (id: string) => {
    setSelectedFormatIds((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    );
  };

  const toggleGoal = (id: string) => {
    setSelectedGoalIds((prev) => {
      if (prev.includes(id)) {
        return prev.filter((item) => item !== id);
      }
      if (prev.length >= 3) {
        return [...prev.slice(1), id]; // Keep maximum 3
      }
      return [...prev, id];
    });
  };

  const handleContinue = () => {
    try {
      localStorage.setItem('learnai_onboarding_formats', JSON.stringify(selectedFormatIds));
      localStorage.setItem('learnai_onboarding_goals', JSON.stringify(selectedGoalIds));
    } catch {
      // Ignore storage errors
    }
    navigate('/onboarding/step-6');
  };

  const handleBack = () => {
    navigate('/onboarding/step-4');
  };

  const selectedShortLabels = learningFormats
    .filter((f) => selectedFormatIds.includes(f.id))
    .map((f) => f.shortLabel);

  return (
    <div className="relative min-h-screen flex flex-col justify-between overflow-x-hidden selection:bg-[#FFB091]/30 selection:text-[#9C5B33]">
      {/* Soft Ambient Background Glows */}
      <AmbientBackground />

      {/* Main Onboarding Card */}
      <main className="flex-1 flex flex-col items-center justify-center px-4 sm:px-6 py-8 sm:py-12 z-10">
        <motion.div
          initial={{ opacity: 0, scale: 0.97, y: 16 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          transition={{ duration: 0.45, ease: [0.16, 1, 0.3, 1] }}
          className="w-full max-w-[880px] bg-white/85 backdrop-blur-2xl rounded-[28px] sm:rounded-[36px] p-6 sm:p-10 border border-white/90 shadow-[0_24px_64px_rgba(26,31,54,0.06)]"
        >
          {/* Top Brand & Skip Navigation Inside Card */}
          <div className="flex items-center justify-between mb-5">
            <Link
              to="/"
              className="font-extrabold text-base sm:text-lg tracking-tight text-[#1A1F36] hover:text-[#8B4D2B] transition-colors"
            >
              LearnAI
            </Link>
            <Link
              to="/"
              className="text-[11px] sm:text-xs font-semibold text-gray-500 hover:text-[#1A1F36] transition-colors"
            >
              Skip for now
            </Link>
          </div>

          {/* Progress Indicator */}
          <OnboardingProgress
            currentStep={5}
            totalSteps={7}
            stepLabel="STEP 5 OF 7"
            rightLabel="LEARNING PREFERENCES"
            percentage={71}
          />

          {/* Main Heading and Subtitle */}
          <div className="mt-7 mb-6 text-left">
            <h1 className="text-2xl sm:text-3xl font-extrabold text-[#1A1F36] tracking-tight leading-snug">
              How do you like to learn?
            </h1>
            <p className="text-xs sm:text-sm text-gray-500 mt-1.5 leading-relaxed font-normal max-w-2xl">
              Tell us what helps you learn best. We&apos;ll use this to personalize the resources and activities in your learning path.
            </p>
          </div>

          {/* 6 Learning Formats Grid (2-Column) */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5 sm:gap-4 mb-7">
            {learningFormats.map((format) => (
              <LearningFormatCard
                key={format.id}
                option={format}
                isSelected={selectedFormatIds.includes(format.id)}
                onToggle={toggleFormat}
              />
            ))}
          </div>

          {/* "WHAT MATTERS MOST TO YOU?" Section */}
          <div className="mb-6 pt-2">
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-xs sm:text-sm font-bold text-[#1A1F36] tracking-tight">
                What matters most to you?
              </h3>
              <span className="text-[10px] sm:text-[11px] font-bold text-gray-400 uppercase tracking-wider">
                SELECT UP TO 3
              </span>
            </div>

            {/* Goal Chips (Multi-Select) */}
            <div className="flex flex-wrap items-center gap-2 sm:gap-2.5">
              {GOAL_OPTIONS.map((goal) => {
                const isSelected = selectedGoalIds.includes(goal.id);
                return (
                  <motion.button
                    key={goal.id}
                    type="button"
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                    onClick={() => toggleGoal(goal.id)}
                    className={`
                      px-3.5 sm:px-4 py-2 rounded-xl text-xs sm:text-[13px] font-medium transition-all duration-150 cursor-pointer select-none border text-left
                      ${
                        isSelected
                          ? 'bg-[#607274] text-white border-transparent shadow-xs font-semibold'
                          : 'bg-white/80 hover:bg-white text-gray-700 border-gray-200/80'
                      }
                    `}
                  >
                    {goal.label}
                  </motion.button>
                );
              })}
            </div>
          </div>

          {/* Personalization Insight Panel */}
          <div className="w-full bg-[#F2EFFE]/90 border border-[#E6E1FF] rounded-2xl sm:rounded-3xl p-4 sm:p-5 flex items-start sm:items-center gap-3.5 shadow-xs mb-6 sm:mb-8">
            <div className="w-8 h-8 rounded-xl bg-white/80 border border-[#DDD7FF] flex items-center justify-center shrink-0 text-[#CC7D52] shadow-xs">
              <Sparkles className="w-4 h-4 text-[#CC7D52]" />
            </div>
            <p className="text-xs sm:text-sm text-gray-700 leading-relaxed font-normal">
              <strong className="font-semibold text-[#1A1F36]">LearnAI will adapt your recommendations:</strong> Your learning preferences will influence the courses, projects, practice exercises and resources we recommend.
            </p>
          </div>

          {/* Bottom Action Controls & Summary */}
          <div className="flex flex-col sm:flex-row items-center justify-between gap-4 pt-2">
            {/* Back Button */}
            <button
              type="button"
              onClick={handleBack}
              className="text-xs sm:text-sm font-semibold text-[#1A1F36] hover:text-[#8B4D2B] px-3 py-2 transition-colors cursor-pointer self-start sm:self-auto"
            >
              Back
            </button>

            {/* Right Group: Summary Chips + Continue Button */}
            <div className="flex items-center gap-3 w-full sm:w-auto justify-end">
              {/* Selected Summary Chips */}
              <div className="hidden sm:flex items-center gap-1.5">
                <AnimatePresence>
                  {selectedShortLabels.map((label) => (
                    <motion.span
                      layout
                      initial={{ scale: 0.8, opacity: 0 }}
                      animate={{ scale: 1, opacity: 1 }}
                      exit={{ scale: 0.8, opacity: 0 }}
                      key={label}
                      className="px-3 py-1 rounded-full bg-[#FAF4F0] border border-[#8B4D2B]/30 text-xs font-semibold text-[#8B4D2B] shadow-2xs"
                    >
                      {label}
                    </motion.span>
                  ))}
                </AnimatePresence>
              </div>

              {/* Continue Button */}
              <motion.button
                whileHover={{ scale: 1.015 }}
                whileTap={{ scale: 0.985 }}
                type="button"
                onClick={handleContinue}
                className="w-full sm:w-[165px] h-12 rounded-xl sm:rounded-2xl bg-[#CC7D52] hover:bg-[#B86D44] active:bg-[#A05D36] text-white font-semibold text-xs sm:text-sm flex items-center justify-center gap-2 shadow-sm shadow-[#CC7D52]/20 transition-all cursor-pointer"
              >
                <span>Continue</span>
                <ArrowRight className="w-4 h-4 text-white" />
              </motion.button>
            </div>
          </div>
        </motion.div>
      </main>
    </div>
  );
};
