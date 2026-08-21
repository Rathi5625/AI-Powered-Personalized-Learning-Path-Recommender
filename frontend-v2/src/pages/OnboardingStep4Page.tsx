import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Compass,
  Route,
  Mountain,
  Sparkles,
  ArrowLeft,
  ArrowRight,
} from 'lucide-react';
import { AmbientBackground } from '../components/landing/AmbientBackground';
import { OnboardingProgress } from '../components/onboarding/OnboardingProgress';
import {
  ExperienceLevelCard,
  type ExperienceLevelOption,
} from '../components/onboarding/ExperienceLevelCard';

const experienceOptions: ExperienceLevelOption[] = [
  {
    id: 'beginner',
    title: 'Beginner',
    subtitle: "I'm building my foundations.",
    description: "I'm new to this area and want to understand the fundamentals.",
    icon: Compass,
  },
  {
    id: 'intermediate',
    title: 'Intermediate',
    subtitle: 'I know the fundamentals.',
    description: 'I understand the basics and have some practical experience.',
    icon: Route,
  },
  {
    id: 'advanced',
    title: 'Advanced',
    subtitle: "I'm comfortable building.",
    description: 'I have strong practical experience and want to go deeper.',
    icon: Mountain,
  },
];

const DEFAULT_PROFILE_SKILLS = ['Java', 'DSA', 'React', 'SQL'];

export const OnboardingStep4Page: React.FC = () => {
  const navigate = useNavigate();
  const [selectedLevel, setSelectedLevel] = useState<'beginner' | 'intermediate' | 'advanced'>('intermediate');
  const [toast, setToast] = useState<string | null>(null);

  const showToast = (msg: string) => {
    setToast(msg);
    setTimeout(() => setToast(null), 3000);
  };

  const handleContinue = () => {
    try {
      localStorage.setItem('learnai_onboarding_experience', selectedLevel);
    } catch {
      // Ignore storage errors
    }
    navigate('/onboarding/step-5');
  };

  const handleBack = () => {
    navigate('/onboarding/step-3');
  };

  const selectedTitle =
    experienceOptions.find((opt) => opt.id === selectedLevel)?.title || 'Intermediate';

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
          <div className="flex items-center justify-between mb-4">
            <Link
              to="/onboarding/step-3"
              className="inline-flex items-center gap-1.5 font-extrabold text-base sm:text-lg tracking-tight text-[#1A1F36] hover:text-[#8B4D2B] transition-colors"
            >
              <ArrowLeft className="w-4 h-4 text-gray-400" />
              <span>LearnAI</span>
            </Link>
            <Link
              to="/"
              className="text-[11px] sm:text-xs font-semibold text-gray-500 hover:text-[#1A1F36] transition-colors"
            >
              Skip for now
            </Link>
          </div>

          {/* Centered Progress Indicator */}
          <div className="max-w-[420px] mx-auto mb-7">
            <OnboardingProgress
              currentStep={4}
              totalSteps={7}
              stepLabel="Step 4 of 7"
              rightLabel=""
              percentage={57}
            />
          </div>

          {/* Main Heading and Subtitle (Centered with clean editorial breaks) */}
          <div className="text-center mb-7 sm:mb-8">
            <h1 className="text-2xl sm:text-3xl font-extrabold text-[#1A1F36] tracking-tight leading-snug">
              How would you describe your current
              <br className="hidden sm:inline" /> experience?
            </h1>
            <p className="text-xs sm:text-sm text-gray-500 mt-2 max-w-lg mx-auto leading-relaxed font-normal">
              This helps us choose the right starting point for your personalized
              <br className="hidden sm:inline" /> learning path.
            </p>
          </div>

          {/* 3 Experience Level Cards Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3.5 sm:gap-4 mb-7">
            {experienceOptions.map((option) => (
              <ExperienceLevelCard
                key={option.id}
                option={option}
                isSelected={selectedLevel === option.id}
                onSelect={setSelectedLevel}
              />
            ))}
          </div>

          {/* Your Current Learning Profile Section */}
          <div className="text-center mb-6 pt-2">
            <p className="text-[10px] sm:text-[11px] font-bold text-gray-400 uppercase tracking-widest mb-3">
              YOUR CURRENT LEARNING PROFILE
            </p>
            <div className="inline-flex flex-wrap items-center justify-center gap-2">
              {DEFAULT_PROFILE_SKILLS.map((skill) => (
                <span
                  key={skill}
                  className="px-3 py-1 rounded-full bg-white/80 border border-gray-200/80 text-xs font-medium text-[#1A1F36] shadow-2xs"
                >
                  {skill}
                </span>
              ))}
              <span className="text-gray-300 font-light mx-0.5">|</span>
              <motion.span
                key={selectedTitle}
                initial={{ scale: 0.9, opacity: 0 }}
                animate={{ scale: 1, opacity: 1 }}
                className="px-3.5 py-1 rounded-full bg-[#FAF4F0] border border-[#8B4D2B]/40 text-xs font-semibold text-[#8B4D2B] shadow-2xs"
              >
                {selectedTitle}
              </motion.span>
            </div>
          </div>

          {/* Personalization Explanation Panel */}
          <div className="w-full bg-[#F2EFFE]/90 border border-[#E6E1FF] rounded-2xl sm:rounded-3xl p-4 sm:p-5 flex items-start sm:items-center gap-3.5 shadow-xs mb-4">
            <div className="w-8 h-8 rounded-xl bg-white/80 border border-[#DDD7FF] flex items-center justify-center shrink-0 text-[#CC7D52] shadow-xs">
              <Sparkles className="w-4 h-4 text-[#CC7D52]" />
            </div>
            <p className="text-xs sm:text-sm text-gray-700 leading-relaxed font-normal">
              <strong className="font-semibold text-[#1A1F36]">Why we ask:</strong> Your experience level helps LearnAI decide whether to teach a concept from the fundamentals or move directly into practical application.
            </p>
          </div>

          {/* Individual Skill Level Link */}
          <div className="text-center mb-8">
            <p className="text-xs text-gray-500 font-normal">
              Want more control?{' '}
              <button
                type="button"
                onClick={() => showToast('Granular skill-level customization will be available soon.')}
                className="text-[#8B4D2B] font-semibold hover:underline cursor-pointer transition-colors"
              >
                Set different levels for individual skills.
              </button>
            </p>
          </div>

          {/* Bottom Action Controls */}
          <div className="flex items-center justify-between pt-2">
            {/* Back Button */}
            <button
              type="button"
              onClick={handleBack}
              className="inline-flex items-center gap-1.5 text-xs sm:text-sm font-semibold text-[#1A1F36] hover:text-[#8B4D2B] px-3 py-2 transition-colors cursor-pointer"
            >
              <ArrowLeft className="w-4 h-4" />
              <span>Back</span>
            </button>

            {/* Continue Button */}
            <motion.button
              whileHover={{ scale: 1.015 }}
              whileTap={{ scale: 0.985 }}
              type="button"
              onClick={handleContinue}
              className="w-[140px] sm:w-[165px] h-12 rounded-xl sm:rounded-2xl bg-[#CC7D52] hover:bg-[#B86D44] active:bg-[#A05D36] text-white font-semibold text-xs sm:text-sm flex items-center justify-center gap-2 shadow-sm shadow-[#CC7D52]/20 transition-all cursor-pointer"
            >
              <span>Continue</span>
              <ArrowRight className="w-4 h-4 text-white" />
            </motion.button>
          </div>
        </motion.div>
      </main>

      {/* Toast Notification */}
      <AnimatePresence>
        {toast && (
          <motion.div
            initial={{ opacity: 0, y: 18 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 18 }}
            className="fixed bottom-6 left-1/2 -translate-x-1/2 bg-[#1A1F36] text-white text-xs font-medium px-4 py-2.5 rounded-full shadow-lg z-50 flex items-center gap-2 whitespace-nowrap"
          >
            <Sparkles className="w-3.5 h-3.5 text-[#FFB091]" />
            <span>{toast}</span>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};
