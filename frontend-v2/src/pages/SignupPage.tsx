import React from 'react';
import { motion } from 'framer-motion';
import { AmbientBackground } from '../components/landing/AmbientBackground';
import { LoginHeader } from '../components/layout/LoginHeader';
import { SignupForm } from '../components/auth/SignupForm';
import { LearningJourney } from '../components/auth/LearningJourney';

export const SignupPage: React.FC = () => {
  return (
    <div className="relative min-h-screen flex flex-col overflow-x-hidden selection:bg-[#FFB091]/30 selection:text-[#9C5B33]">
      {/* Ambient background glows */}
      <AmbientBackground />

      {/* Top floating pill header */}
      <LoginHeader />

      {/* Main content */}
      <main className="flex-1 flex flex-col items-center justify-center px-4 sm:px-6 py-8 sm:py-12 z-10">
        <motion.div
          initial={{ opacity: 0, scale: 0.97, y: 14 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          transition={{ duration: 0.45, ease: [0.16, 1, 0.3, 1] }}
          className="w-full max-w-[940px] bg-white/80 backdrop-blur-2xl rounded-[32px] sm:rounded-[36px] border border-white/90 shadow-[0_20px_60px_rgba(26,31,54,0.06)] overflow-hidden"
        >
          <div className="grid grid-cols-1 lg:grid-cols-[1fr_auto]">
            {/* Left: Signup Form */}
            <div className="px-6 sm:px-10 py-8 sm:py-10">
              <SignupForm />
            </div>

            {/* Right: Learning Journey Panel */}
            <div className="hidden lg:flex flex-col justify-center px-8 py-10 bg-[#F7F4F1]/60 border-l border-[#F0EBE7] min-w-[260px] max-w-[300px] rounded-r-[36px]">
              <p className="text-[11px] font-bold text-gray-400 uppercase tracking-widest mb-5">
                Your Journey
              </p>
              <LearningJourney />
            </div>
          </div>

          {/* Mobile: Journey Panel stacked below form */}
          <div className="lg:hidden border-t border-[#F0EBE7] px-6 sm:px-10 py-6 bg-[#F7F4F1]/60">
            <p className="text-[11px] font-bold text-gray-400 uppercase tracking-widest mb-4">
              Your Journey
            </p>
            {/* Horizontal row on mobile */}
            <div className="flex flex-row gap-3 overflow-x-auto pb-1">
              {[
                { label: 'Goal Setting', sub: 'Define objectives' },
                { label: 'Skill Mapping', sub: 'Current proficiency' },
                { label: 'AI Analysis', sub: 'Processing path' },
                { label: 'Learning Path', sub: 'Ready to start', accent: true },
              ].map((s, i) => (
                <div
                  key={i}
                  className={`flex flex-col items-center text-center flex-shrink-0 px-3 py-2 rounded-2xl ${
                    s.accent ? 'bg-[#F4E4D8]/60' : 'bg-white/60'
                  } border border-white/80 min-w-[96px]`}
                >
                  <div
                    className={`w-7 h-7 rounded-full flex items-center justify-center mb-1.5 ${
                      s.accent ? 'bg-[#CC7D52]' : 'bg-[#EAE8FF]'
                    }`}
                  >
                    <div className={`w-2 h-2 rounded-full ${s.accent ? 'bg-white' : 'bg-[#8E86FF]'}`} />
                  </div>
                  <p className={`text-[10px] font-semibold ${s.accent ? 'text-[#1A1F36]' : 'text-gray-700'}`}>
                    {s.label}
                  </p>
                  <p className={`text-[9px] mt-0.5 ${s.accent ? 'text-[#CC7D52]' : 'text-gray-400'}`}>
                    {s.sub}
                  </p>
                </div>
              ))}
            </div>
          </div>
        </motion.div>
      </main>
    </div>
  );
};
