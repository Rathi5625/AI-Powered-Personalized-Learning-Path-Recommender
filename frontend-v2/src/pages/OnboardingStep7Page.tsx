import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Sparkles,
  ArrowLeft,
  ArrowRight,
} from 'lucide-react';
import { AmbientBackground } from '../components/landing/AmbientBackground';
import {
  TimelineSelector,
  type TimelineOption,
} from '../components/onboarding/TimelineSelector';
import { ProfileSummary } from '../components/onboarding/ProfileSummary';
import api from '../api/client';
import { ExperienceLevel, LearningStyle, PreferredContentType } from '../api/types';

const SUGGESTIONS = [
  'Get my first software engineering job',
  'Become a full-stack developer',
  'Prepare for technical interviews',
];

const TIMELINE_OPTIONS: TimelineOption[] = [
  { id: '3_months', duration: '3 months', description: 'Fast and focused' },
  { id: '6_months', duration: '6 months', description: 'Balanced and achievable' },
  { id: '12_months', duration: '12 months', description: 'Long-term growth' },
  { id: 'flexible', duration: "I'm not sure yet", description: 'Flexible timeline' },
];

export const OnboardingStep7Page: React.FC = () => {
  const navigate = useNavigate();
  const [primaryGoal, setPrimaryGoal] = useState('Software Engineer');
  const [selectedTimeline, setSelectedTimeline] = useState('6_months');
  const [personalObjective, setPersonalObjective] = useState('');
  const [loading, setLoading] = useState(false);

  const handleObjectiveChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const text = e.target.value;
    if (text.length <= 500) {
      setPersonalObjective(text);
    }
  };

  const handleGeneratePath = async () => {
    setLoading(true);
    try {
      let career = primaryGoal;
      let exp: ExperienceLevel = 'BEGINNER';
      let style: LearningStyle = 'VISUAL';
      let contentType: PreferredContentType = 'VIDEO';
      let weeklyHours = 10;
      let selectedSkills: string[] = ['Java', 'React', 'SQL'];

      try {
        const storedCareer = localStorage.getItem('learnai_onboarding_career');
        if (storedCareer) career = storedCareer;
        const storedExp = localStorage.getItem('learnai_onboarding_experience');
        if (storedExp) exp = storedExp.toUpperCase() as ExperienceLevel;
        const storedSkills = localStorage.getItem('learnai_onboarding_skills');
        if (storedSkills) selectedSkills = JSON.parse(storedSkills);
      } catch {
        // ignore localStorage parsing errors
      }

      await api.completeOnboarding({
        targetCareer: career,
        experienceLevel: exp,
        selectedSkills,
        learningStyle: style,
        preferredContentType: contentType,
        preferredLearningPace: selectedTimeline,
        weeklyCommitmentHours: weeklyHours,
        currentGoal: primaryGoal,
        personalObjective: personalObjective || undefined,
      });

      localStorage.setItem(
        'learnai_onboarding_final',
        JSON.stringify({
          primaryGoal,
          timeline: selectedTimeline,
          objective: personalObjective,
        })
      );
    } catch (err) {
      console.error('Failed to complete onboarding on backend:', err);
    } finally {
      setLoading(false);
      navigate('/building-path');
    }
  };

  const handleBack = () => {
    navigate('/onboarding/step-6');
  };

  return (
    <div className="relative min-h-screen flex flex-col justify-between overflow-x-hidden selection:bg-[#ffdbcb] selection:text-[#8e4d2b]">
      {/* Background Lighting */}
      <AmbientBackground />

      {/* Top Header & Back Navigation */}
      <header className="w-full max-w-5xl mx-auto flex items-center justify-between px-6 sm:px-8 pt-6 sm:pt-8 z-10">
        <Link
          to="/"
          className="flex items-center gap-2 font-extrabold text-base sm:text-lg text-[#0f1b32] hover:opacity-80 transition-opacity"
        >
          <div className="w-8 h-8 rounded-xl bg-[#8e4d2b] flex items-center justify-center text-white font-bold text-sm">
            L
          </div>
          <span>LearnAI</span>
        </Link>

        {/* Step Indicator */}
        <div className="flex items-center gap-2">
          <span className="text-xs font-bold text-[#8e4d2b] tracking-wider uppercase bg-[#FAF4F0] border border-[#F2DACB] px-3 py-1 rounded-full">
            Step 7 of 7
          </span>
        </div>
      </header>

      {/* Main Form Content */}
      <main className="flex-1 max-w-4xl w-full mx-auto px-4 sm:px-6 py-6 sm:py-8 z-10 text-left">
        <div className="space-y-6 sm:space-y-8">
          {/* Header Title */}
          <div className="text-center sm:text-left space-y-2">
            <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#FAF4F0] border border-[#F2DACB] text-[#8e4d2b] text-[11px] font-bold uppercase tracking-wider shadow-2xs">
              <Sparkles className="w-3.5 h-3.5" />
              <span>FINAL STEP</span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-[#0f1b32] tracking-tight">
              What is your primary goal and target timeline?
            </h1>
            <p className="text-xs sm:text-sm text-[#53433c] font-normal leading-relaxed">
              We&apos;ll fine-tune your personalized curriculum, pace, and milestones based on what you want to achieve.
            </p>
          </div>

          {/* Form Content Grid: 2 Columns */}
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 sm:gap-8 items-start">
            {/* Left 8 Cols: Goal, Timeline & Objective */}
            <div className="lg:col-span-8 space-y-6">
              {/* Primary Goal Section */}
              <div className="space-y-3">
                <label className="text-xs font-bold text-[#0f1b32] uppercase tracking-wider block">
                  PRIMARY GOAL
                </label>
                <input
                  type="text"
                  value={primaryGoal}
                  onChange={(e) => setPrimaryGoal(e.target.value)}
                  placeholder="e.g. Full Stack Software Engineer"
                  className="w-full px-4 py-3 rounded-2xl bg-white/80 border border-gray-200/80 text-xs sm:text-sm font-semibold text-[#0f1b32] focus:outline-none focus:ring-2 focus:ring-[#8e4d2b]/20 focus:border-[#8e4d2b] shadow-2xs transition-all"
                />

                {/* Goal Suggestion Chips */}
                <div className="flex flex-wrap items-center gap-2 pt-1">
                  {SUGGESTIONS.map((suggestion) => (
                    <button
                      key={suggestion}
                      type="button"
                      onClick={() => setPrimaryGoal(suggestion)}
                      className="px-3 py-1.5 rounded-xl bg-white/70 hover:bg-[#FAF4F0] border border-gray-200/70 hover:border-[#F2DACB] text-[11px] font-semibold text-[#53433c] hover:text-[#8e4d2b] transition-all cursor-pointer shadow-2xs"
                    >
                      {suggestion}
                    </button>
                  ))}
                </div>
              </div>

              {/* Timeline Selector */}
              <div className="space-y-3">
                <label className="text-xs font-bold text-[#0f1b32] uppercase tracking-wider block">
                  TARGET TIMELINE
                </label>
                <TimelineSelector
                  options={TIMELINE_OPTIONS}
                  selectedId={selectedTimeline}
                  onSelect={setSelectedTimeline}
                />
              </div>

              {/* Personal Objective Textarea */}
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <label className="text-xs font-bold text-[#0f1b32] uppercase tracking-wider block">
                    PERSONAL OBJECTIVE (OPTIONAL)
                  </label>
                  <span className="text-[11px] text-gray-400 font-medium">
                    {personalObjective.length}/500
                  </span>
                </div>
                <textarea
                  rows={3}
                  value={personalObjective}
                  onChange={handleObjectiveChange}
                  placeholder="Tell us about any specific projects you want to build, interviews you are preparing for, or personal milestones..."
                  className="w-full p-4 rounded-2xl bg-white/80 border border-gray-200/80 text-xs sm:text-sm font-medium text-[#0f1b32] placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-[#8e4d2b]/20 focus:border-[#8e4d2b] resize-none shadow-2xs transition-all"
                />
              </div>
            </div>

            {/* Right 4 Cols: Live Profile Summary Card */}
            <div className="lg:col-span-4">
              <ProfileSummary
                targetRole={primaryGoal}
                commitment="10 hrs/week"
              />
            </div>
          </div>
        </div>
      </main>

      {/* Sticky Bottom Navigation Bar */}
      <footer className="w-full border-t border-gray-200/60 bg-[#f9f9ff]/80 backdrop-blur-xl px-6 sm:px-8 py-4 sm:py-5 z-10">
        <div className="max-w-4xl mx-auto flex items-center justify-between gap-4">
          <button
            type="button"
            onClick={handleBack}
            className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl border border-gray-200/80 bg-white/80 hover:bg-white text-xs sm:text-sm font-bold text-[#0f1b32] transition-colors cursor-pointer shadow-2xs"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Back</span>
          </button>

          <button
            type="button"
            disabled={loading}
            onClick={handleGeneratePath}
            className="inline-flex items-center gap-2 px-6 sm:px-8 py-3 rounded-2xl bg-[#8e4d2b] hover:bg-[#783e20] text-white font-bold text-xs sm:text-sm shadow-sm hover:shadow-md transition-all duration-200 cursor-pointer active:scale-[0.98] disabled:opacity-50"
          >
            <span>{loading ? 'Saving Profile...' : 'Generate My Learning Path'}</span>
            <ArrowRight className="w-4 h-4" />
          </button>
        </div>
      </footer>
    </div>
  );
};
