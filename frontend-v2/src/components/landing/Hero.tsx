import React from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Play } from 'lucide-react';
import { Button } from '../ui/Button';
import { DashboardPreview } from './DashboardPreview';

import { useAuth } from '../../context/AuthContext';

export const Hero: React.FC = () => {
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();

  return (
    <section className="relative pt-32 sm:pt-36 lg:pt-40 pb-16 sm:pb-24 lg:pb-32 px-6 sm:px-8 max-w-7xl mx-auto">
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 lg:gap-16 items-center">
        {/* Left Column: Headline, Body, CTA */}
        <motion.div
          initial={{ opacity: 0, x: -24 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.7, ease: [0.16, 1, 0.3, 1] }}
          className="lg:col-span-6 space-y-6 sm:space-y-8 text-left"
        >
          {/* Main Headline */}
          <h1 className="text-4xl sm:text-5xl lg:text-[62px] font-black text-[#1A1F36] leading-[1.1] tracking-tight">
            Stop guessing <br />
            what to learn. Let <br />
            AI build{' '}
            <span className="text-[#E88B6E] inline-block">your</span>{' '}
            <span className="text-[#8E86FF] inline-block">path.</span>
          </h1>

          {/* Subtitle */}
          <p className="text-base sm:text-lg text-gray-600 max-w-lg leading-relaxed font-normal">
            A premium, hyper-personalized learning ecosystem that adapts to your career goals, existing skills, and optimal pace.
          </p>

          {/* CTA Action Row */}
          <div className="flex flex-wrap items-center gap-4 pt-2 sm:pt-4">
            <Button
              variant="primary"
              size="lg"
              onClick={() => {
                if (isAuthenticated) {
                  navigate(user?.onboardingCompleted ? '/dashboard' : '/onboarding');
                } else {
                  navigate('/login');
                }
              }}
              className="font-semibold shadow-md shadow-[#A06A42]/20"
            >
              Build My Learning Path
            </Button>

            <button
              type="button"
              className="inline-flex items-center gap-2.5 px-4 py-3 text-sm sm:text-base font-semibold text-[#1A1F36] hover:text-[#A06A42] transition-colors cursor-pointer select-none group"
            >
              <div className="w-7 h-7 rounded-full border border-black/15 flex items-center justify-center group-hover:border-[#A06A42] group-hover:bg-[#A06A42]/5 transition-all">
                <Play className="w-3.5 h-3.5 fill-current ml-0.5 text-[#1A1F36] group-hover:text-[#A06A42]" />
              </div>
              <span>Watch Demo</span>
            </button>
          </div>
        </motion.div>

        {/* Right Column: Floating Dashboard Preview */}
        <motion.div
          initial={{ opacity: 0, x: 24 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.8, delay: 0.15, ease: [0.16, 1, 0.3, 1] }}
          className="lg:col-span-6 w-full mt-4 lg:mt-0"
        >
          <DashboardPreview />
        </motion.div>
      </div>
    </section>
  );
};
