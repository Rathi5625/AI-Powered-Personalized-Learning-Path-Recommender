import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  GraduationCap,
  Briefcase,
  RefreshCw,
  Laptop,
  Sparkles,
  ArrowRight,
} from 'lucide-react';
import { AmbientBackground } from '../components/landing/AmbientBackground';
import { OnboardingProgress } from '../components/onboarding/OnboardingProgress';
import { ProfileOptionCard, type ProfileOption } from '../components/onboarding/ProfileOptionCard';
import { PersonalizationInsight } from '../components/onboarding/PersonalizationInsight';

const profileOptions: ProfileOption[] = [
  {
    id: 'student',
    title: 'Student',
    description: 'Looking to complement studies with practical AI skills.',
    icon: GraduationCap,
    insight:
      "✨ As a Student, we'll focus on foundational concepts and portfolio-building projects to get you career-ready.",
  },
  {
    id: 'working_professional',
    title: 'Working Professional',
    description: 'Upskilling for current role or preparing for promotion.',
    icon: Briefcase,
    insight:
      "✨ As a Working Professional, we'll focus on practical skills that fit your current role and career growth.",
  },
  {
    id: 'career_switcher',
    title: 'Career Switcher',
    description: 'Transitioning into a tech or AI-focused career.',
    icon: RefreshCw,
    insight:
      "✨ As a Career Switcher, we'll focus on structured foundations and job-ready skills to help you transition confidently.",
  },
  {
    id: 'freelancer',
    title: 'Freelancer',
    description: 'Building a competitive edge for independent work.',
    icon: Laptop,
    insight:
      "✨ As a Freelancer, we'll focus on practical, marketable skills that help you build a stronger independent career.",
  },
];

export const OnboardingStep1Page: React.FC = () => {
  const navigate = useNavigate();
  const [selectedId, setSelectedId] = useState<string>('student');

  const selectedOption =
    profileOptions.find((opt) => opt.id === selectedId) || profileOptions[0];

  const handleContinue = () => {
    // Preserve local selection in localStorage if desired
    try {
      localStorage.setItem('learnai_onboarding_profile', selectedId);
    } catch {
      // Ignore storage exceptions
    }
    navigate('/onboarding/step-2');
  };

  return (
    <div className="relative min-h-screen flex flex-col justify-between overflow-x-hidden selection:bg-[#FFB091]/30 selection:text-[#9C5B33]">
      {/* Ambient background glows */}
      <AmbientBackground />

      {/* Top Header outside card */}
      <header className="w-full max-w-5xl mx-auto flex items-center justify-between px-6 sm:px-8 pt-6 sm:pt-8 z-10">
        {/* Left: Brand Mark */}
        <Link
          to="/"
          className="flex items-center gap-2.5 group select-none cursor-pointer"
        >
          <div className="w-8 h-8 rounded-xl bg-[#8B4D2B] text-white flex items-center justify-center shadow-xs group-hover:scale-105 transition-transform">
            <GraduationCap className="w-4.5 h-4.5 text-white" />
          </div>
          <span className="font-extrabold text-base sm:text-lg tracking-tight text-[#1A1F36]">
            LearnAI
          </span>
        </Link>

        {/* Right: Skip for now */}
        <Link
          to="/"
          className="text-xs sm:text-sm font-medium text-gray-500 hover:text-[#1A1F36] transition-colors"
        >
          Skip for now
        </Link>
      </header>

      {/* Main Onboarding Card */}
      <main className="flex-1 flex flex-col items-center justify-center px-4 sm:px-6 py-6 sm:py-10 z-10">
        <motion.div
          initial={{ opacity: 0, scale: 0.97, y: 16 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          transition={{ duration: 0.45, ease: [0.16, 1, 0.3, 1] }}
          className="w-full max-w-[880px] bg-white/85 backdrop-blur-2xl rounded-[28px] sm:rounded-[36px] p-6 sm:p-10 border border-white/90 shadow-[0_24px_64px_rgba(26,31,54,0.06)]"
        >
          {/* Progress Header */}
          <OnboardingProgress
            currentStep={1}
            totalSteps={7}
            stepTitle="Who are you?"
          />

          {/* Personalization Badge */}
          <div className="mt-6 mb-3">
            <span className="inline-flex items-center gap-1.5 px-3 py-1 bg-white border border-gray-100/90 rounded-full text-[11px] font-semibold text-[#8B4D2B] shadow-xs">
              <Sparkles className="w-3 h-3 text-[#CC7D52]" />
              <span>LearnAI Personalization</span>
            </span>
          </div>

          {/* Introduction Heading */}
          <div className="mb-6">
            <h1 className="text-2xl sm:text-3xl font-extrabold text-[#1A1F36] tracking-tight">
              Let&apos;s get to know you.
            </h1>
            <p className="text-xs sm:text-sm text-gray-500 mt-1.5 font-normal">
              Tell us a bit about your current situation so we can tailor your learning path.
            </p>
          </div>

          {/* 2 × 2 Profile Options Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5 sm:gap-4 mb-5">
            {profileOptions.map((option) => (
              <ProfileOptionCard
                key={option.id}
                option={option}
                isSelected={selectedId === option.id}
                onSelect={setSelectedId}
              />
            ))}
          </div>

          {/* Dynamic Insight Panel */}
          <PersonalizationInsight insightText={selectedOption.insight} />

          {/* Bottom Action Row */}
          <div className="flex items-center justify-end mt-6 sm:mt-7">
            <motion.button
              whileHover={{ scale: 1.015 }}
              whileTap={{ scale: 0.985 }}
              type="button"
              onClick={handleContinue}
              className="w-full sm:w-[165px] h-12 rounded-xl sm:rounded-2xl bg-[#8B4D2B] hover:bg-[#753F22] active:bg-[#5C321B] text-white font-semibold text-xs sm:text-sm flex items-center justify-center gap-2 shadow-sm shadow-[#8B4D2B]/20 transition-all cursor-pointer"
            >
              <span>Continue</span>
              <ArrowRight className="w-4 h-4 text-white" />
            </motion.button>
          </div>
        </motion.div>
      </main>

      {/* Footer */}
      <footer className="w-full text-center py-5 text-xs text-gray-400 z-10 select-none">
        © 2024 LearnAI Platform. Tailored learning experiences.
      </footer>
    </div>
  );
};
