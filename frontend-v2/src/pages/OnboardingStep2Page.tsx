import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  Code,
  Brain,
  BarChart2,
  Layout,
  Server,
  Smartphone,
  Palette,
  Shield,
  Cloud,
  Compass,
  ArrowRight,
} from 'lucide-react';
import { AmbientBackground } from '../components/landing/AmbientBackground';
import { OnboardingProgress } from '../components/onboarding/OnboardingProgress';
import { CareerOptionCard, type CareerOption } from '../components/onboarding/CareerOptionCard';

const careerOptions: CareerOption[] = [
  {
    id: 'software_engineer',
    title: 'Software Engineer',
    description: 'Build software, applications and digital products.',
    icon: Code,
  },
  {
    id: 'ai_ml_engineer',
    title: 'AI / ML Engineer',
    description: 'Build intelligent systems and AI-powered applications.',
    icon: Brain,
  },
  {
    id: 'data_scientist',
    title: 'Data Scientist',
    description: 'Turn data into insights and predictive models.',
    icon: BarChart2,
  },
  {
    id: 'frontend_developer',
    title: 'Frontend Developer',
    description: 'Create modern web experiences and interfaces.',
    icon: Layout,
  },
  {
    id: 'backend_developer',
    title: 'Backend Developer',
    description: 'Build scalable APIs, services and systems.',
    icon: Server,
  },
  {
    id: 'mobile_developer',
    title: 'Mobile Developer',
    description: 'Create Android and iOS applications.',
    icon: Smartphone,
  },
  {
    id: 'ui_ux_designer',
    title: 'UI/UX Designer',
    description: 'Design intuitive digital products and experiences.',
    icon: Palette,
  },
  {
    id: 'cybersecurity_engineer',
    title: 'Cybersecurity Engineer',
    description: 'Protect systems, applications and data.',
    icon: Shield,
  },
  {
    id: 'cloud_engineer',
    title: 'Cloud Engineer',
    description: 'Build and manage modern cloud infrastructure.',
    icon: Cloud,
  },
  {
    id: 'other',
    title: 'Other',
    description: 'Define your own career direction.',
    icon: Compass,
  },
];

export const OnboardingStep2Page: React.FC = () => {
  const navigate = useNavigate();
  const [selectedCareerId, setSelectedCareerId] = useState<string>('software_engineer');

  const handleContinue = () => {
    // Preserve local selection in localStorage if desired
    try {
      localStorage.setItem('learnai_onboarding_career', selectedCareerId);
    } catch {
      // Ignore storage exceptions
    }
    navigate('/onboarding/step-3');
  };

  const handleBack = () => {
    navigate('/onboarding/step-1');
  };

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
          {/* Progress Header */}
          <OnboardingProgress
            currentStep={2}
            totalSteps={7}
            stepLabel="STEP 2 OF 7"
            rightLabel="28%"
            percentage={28}
          />

          {/* Heading and Subtitle */}
          <div className="mt-7 mb-6">
            <h1 className="text-2xl sm:text-3xl font-extrabold text-[#1A1F36] tracking-tight">
              Where do you want to go?
            </h1>
            <p className="text-xs sm:text-sm text-gray-500 mt-1.5 font-normal">
              Choose the career direction you&apos;re working toward.
            </p>
          </div>

          {/* 2-Column Responsive Career Options Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5 sm:gap-4">
            {careerOptions.map((option) => (
              <CareerOptionCard
                key={option.id}
                option={option}
                isSelected={selectedCareerId === option.id}
                onSelect={setSelectedCareerId}
              />
            ))}
          </div>

          {/* Bottom Action Controls */}
          <div className="flex items-center justify-between mt-8 pt-2">
            {/* Back Button */}
            <button
              type="button"
              onClick={handleBack}
              className="text-xs sm:text-sm font-semibold text-[#1A1F36] hover:text-[#8B4D2B] px-3 py-2 transition-colors cursor-pointer"
            >
              Back
            </button>

            {/* Continue Button */}
            <motion.button
              whileHover={{ scale: 1.015 }}
              whileTap={{ scale: 0.985 }}
              type="button"
              onClick={handleContinue}
              disabled={!selectedCareerId}
              className="w-[140px] sm:w-[165px] h-12 rounded-xl sm:rounded-2xl bg-[#CC7D52] hover:bg-[#B86D44] active:bg-[#A05D36] text-white font-semibold text-xs sm:text-sm flex items-center justify-center gap-2 shadow-sm shadow-[#CC7D52]/20 transition-all cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span>Continue</span>
              <ArrowRight className="w-4 h-4 text-white" />
            </motion.button>
          </div>
        </motion.div>
      </main>
    </div>
  );
};
