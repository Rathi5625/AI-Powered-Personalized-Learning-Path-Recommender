import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { GraduationCap, ArrowRight } from 'lucide-react';
import { AmbientBackground } from '../components/landing/AmbientBackground';
import { AnalysisProgressCard } from '../components/building-path/AnalysisProgressCard';
import { CentralProgressRing } from '../components/building-path/CentralProgressRing';
import { SkillGapCard } from '../components/building-path/SkillGapCard';

import { useAuth } from '../context/AuthContext';

export const BuildingPathPage: React.FC = () => {
  const navigate = useNavigate();
  const { refreshUser } = useAuth();

  React.useEffect(() => {
    refreshUser();
  }, [refreshUser]);

  return (
    <div className="relative min-h-screen flex flex-col justify-between overflow-x-hidden selection:bg-[#FFB091]/30 selection:text-[#9C5B33]">
      {/* Atmospheric Ambient Background */}
      <AmbientBackground />

      {/* Top Header */}
      <header className="w-full max-w-6xl mx-auto flex items-center justify-between px-6 sm:px-8 pt-6 sm:pt-8 z-10">
        {/* Left: Brand Mark */}
        <Link
          to="/"
          className="flex items-center gap-2.5 group select-none cursor-pointer"
        >
          <div className="w-8 h-8 rounded-xl bg-[#CC7D52] text-white flex items-center justify-center shadow-xs group-hover:scale-105 transition-transform">
            <GraduationCap className="w-4.5 h-4.5 text-white" />
          </div>
          <span className="font-extrabold text-base sm:text-lg tracking-tight text-[#1A1F36]">
            LearnAI
          </span>
        </Link>

        {/* Center / Right: Floating Status Pill */}
        <motion.div
          initial={{ opacity: 0, y: -10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4 }}
          className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-white/80 backdrop-blur-md border border-white/90 shadow-sm text-xs font-bold text-[#1A1F36] uppercase tracking-wider"
        >
          <span className="w-2 h-2 rounded-full bg-[#CC7D52] animate-ping inline-block" />
          <span>BUILDING YOUR PATH</span>
        </motion.div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 flex flex-col items-center justify-center px-4 sm:px-6 py-8 sm:py-12 z-10 max-w-6xl mx-auto w-full">
        {/* Heading and Subtitle */}
        <motion.div
          initial={{ opacity: 0, y: 14 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.45 }}
          className="text-center mb-10 sm:mb-12 max-w-2xl"
        >
          <h1 className="text-2xl sm:text-4xl font-extrabold text-[#1A1F36] tracking-tight leading-tight">
            Building your personalized learning
            <br className="hidden sm:inline" /> path
          </h1>
          <p className="text-xs sm:text-sm text-gray-500 mt-2.5 leading-relaxed font-normal">
            LearnAI is analyzing your goals, skills and learning preferences to create a roadmap designed specifically for you.
          </p>
        </motion.div>

        {/* 3-Part Visualization Composition */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 lg:gap-8 items-center w-full">
          {/* Left: Analysis Progress Card (3.5 cols on lg) */}
          <div className="lg:col-span-3 order-2 lg:order-1 flex justify-center">
            <AnalysisProgressCard />
          </div>

          {/* Center: Central Progress Ring & Floating Insights (5.5 cols on lg) */}
          <div className="lg:col-span-6 order-1 lg:order-2 flex justify-center">
            <CentralProgressRing
              targetRole="Software Engineer"
              experience="Intermediate"
              learningTime="10 hrs/week"
            />
          </div>

          {/* Right: Skill Gap Card (3 cols on lg) */}
          <div className="lg:col-span-3 order-3 lg:order-3 flex justify-center">
            <SkillGapCard />
          </div>
        </div>

        {/* Optional Dashboard Navigation Action */}
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 1, duration: 0.4 }}
          className="mt-12 text-center"
        >
          <button
            type="button"
            onClick={() => navigate('/dashboard')}
            className="inline-flex items-center gap-2 px-6 py-2.5 rounded-full bg-white/80 hover:bg-white text-xs sm:text-sm font-semibold text-[#1A1F36] border border-gray-200 shadow-xs hover:shadow-sm transition-all cursor-pointer"
          >
            <span>Preview Dashboard</span>
            <ArrowRight className="w-3.5 h-3.5 text-[#CC7D52]" />
          </button>
        </motion.div>
      </main>

      {/* Footer */}
      <footer className="w-full text-center py-5 text-xs text-gray-400 z-10 select-none">
        © 2024 LearnAI Platform. Tailored learning experiences.
      </footer>
    </div>
  );
};
