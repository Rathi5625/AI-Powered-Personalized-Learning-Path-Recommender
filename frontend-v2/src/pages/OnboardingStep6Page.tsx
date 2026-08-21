import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowLeft, ArrowRight, Sparkles } from 'lucide-react';
import { AmbientBackground } from '../components/landing/AmbientBackground';
import { TimeAvailabilityCard, type TimeOption } from '../components/onboarding/TimeAvailabilityCard';
import { SchedulePreview } from '../components/onboarding/SchedulePreview';

const timeOptions: TimeOption[] = [
  { id: '5', hours: '5 hours', description: 'A light and flexible schedule' },
  { id: '10', hours: '10 hours', description: 'A balanced learning routine' },
  { id: '15', hours: '15 hours', description: 'A focused learning schedule' },
  { id: '20', hours: '20+ hours', description: 'An intensive learning routine' },
];

const PACE_OPTIONS = [
  { id: 'relaxed', label: 'RELAXED' },
  { id: 'balanced', label: 'BALANCED' },
  { id: 'intensive', label: 'INTENSIVE' },
];

const TIME_OF_DAY_OPTIONS = [
  { id: 'morning', label: 'Morning' },
  { id: 'afternoon', label: 'Afternoon' },
  { id: 'evening', label: 'Evening' },
  { id: 'night', label: 'Night' },
];

const DAYS_OF_WEEK = [
  { id: 'mon', label: 'M', name: 'Monday' },
  { id: 'tue', label: 'T', name: 'Tuesday' },
  { id: 'wed', label: 'W', name: 'Wednesday' },
  { id: 'thu', label: 'T', name: 'Thursday' },
  { id: 'fri', label: 'F', name: 'Friday' },
  { id: 'sat', label: 'S', name: 'Saturday' },
  { id: 'sun', label: 'S', name: 'Sunday' },
];

export const OnboardingStep6Page: React.FC = () => {
  const navigate = useNavigate();
  const [selectedHours, setSelectedHours] = useState<string>('10');
  const [selectedPace, setSelectedPace] = useState<string>('balanced');
  const [selectedTimes, setSelectedTimes] = useState<string[]>(['evening', 'night']);
  const [selectedDays, setSelectedDays] = useState<string[]>([
    'mon',
    'tue',
    'wed',
    'thu',
    'sun',
  ]);

  const toggleTimeOfDay = (id: string) => {
    setSelectedTimes((prev) =>
      prev.includes(id) ? prev.filter((t) => t !== id) : [...prev, id]
    );
  };

  const toggleDay = (id: string) => {
    setSelectedDays((prev) =>
      prev.includes(id) ? prev.filter((d) => d !== id) : [...prev, id]
    );
  };

  const handleContinue = () => {
    try {
      localStorage.setItem(
        'learnai_onboarding_schedule',
        JSON.stringify({
          hours: selectedHours,
          pace: selectedPace,
          times: selectedTimes,
          days: selectedDays,
        })
      );
    } catch {
      // Ignore storage errors
    }
    navigate('/onboarding/step-7');
  };

  const handleBack = () => {
    navigate('/onboarding/step-5');
  };

  return (
    <div className="relative min-h-screen flex flex-col justify-between overflow-x-hidden selection:bg-[#FFB091]/30 selection:text-[#9C5B33]">
      {/* Ambient background glows */}
      <AmbientBackground />

      {/* Main Onboarding Card */}
      <main className="flex-1 flex flex-col items-center justify-center px-4 sm:px-6 py-8 sm:py-12 z-10">
        <motion.div
          initial={{ opacity: 0, scale: 0.97, y: 16 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          transition={{ duration: 0.45, ease: [0.16, 1, 0.3, 1] }}
          className="w-full max-w-[880px] bg-white/85 backdrop-blur-2xl rounded-[28px] sm:rounded-[36px] p-6 sm:p-10 border border-white/90 shadow-[0_24px_64px_rgba(26,31,54,0.06)]"
        >
          {/* Top Brand Header & Progress */}
          <div className="flex items-center justify-between mb-6">
            {/* Left: Brand Mark */}
            <Link
              to="/"
              className="flex items-center gap-2.5 group select-none cursor-pointer"
            >
              <div className="w-8 h-8 rounded-xl bg-[#CC7D52] text-white flex items-center justify-center font-bold text-sm shadow-xs group-hover:scale-105 transition-transform">
                <span>L</span>
              </div>
              <span className="font-extrabold text-base sm:text-lg tracking-tight text-[#1A1F36]">
                LearnAI
              </span>
            </Link>

            {/* Right: Progress Indicator */}
            <div className="flex flex-col items-end space-y-1.5">
              <span className="font-semibold text-gray-500 uppercase tracking-wider text-[11px] sm:text-xs">
                STEP 6 OF 7
              </span>
              <div className="w-24 sm:w-28 h-1.5 bg-[#EAE8FF] rounded-full overflow-hidden">
                <div
                  className="h-full bg-[#CC7D52] rounded-full"
                  style={{ width: '86%' }}
                />
              </div>
            </div>
          </div>

          {/* Heading and Subtitle */}
          <div className="mb-7 text-left">
            <h1 className="text-2xl sm:text-3xl font-extrabold text-[#1A1F36] tracking-tight leading-snug">
              How much time can you invest in learning?
            </h1>
            <p className="text-xs sm:text-sm text-gray-500 mt-1.5 leading-relaxed font-normal">
              We&apos;ll use your availability to create a realistic learning schedule you can actually maintain.
            </p>
          </div>

          {/* Main 2-Column Responsive Layout */}
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 lg:gap-8 items-start mb-8">
            {/* Left Column: Hours, Pace & Schedule (7 cols on lg) */}
            <div className="lg:col-span-7 space-y-6">
              {/* Section 1: Hours available per week */}
              <div>
                <h3 className="text-xs sm:text-sm font-semibold text-[#1A1F36] mb-3">
                  Hours available per week
                </h3>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  {timeOptions.map((option) => (
                    <TimeAvailabilityCard
                      key={option.id}
                      option={option}
                      isSelected={selectedHours === option.id}
                      onSelect={setSelectedHours}
                    />
                  ))}
                </div>
              </div>

              {/* Section 2: Learning Pace */}
              <div>
                <h3 className="text-xs sm:text-sm font-semibold text-[#1A1F36] mb-3">
                  What pace feels right for you?
                </h3>
                <div className="flex items-center gap-2">
                  {PACE_OPTIONS.map((pace) => {
                    const isSelected = selectedPace === pace.id;
                    return (
                      <button
                        key={pace.id}
                        type="button"
                        onClick={() => setSelectedPace(pace.id)}
                        className={`
                          flex-1 py-2.5 px-3 rounded-full text-xs font-bold uppercase tracking-wider transition-all duration-150 cursor-pointer select-none text-center border
                          ${
                            isSelected
                              ? 'bg-[#FAF4F0] border-[#CC7D52] text-[#CC7D52] shadow-xs'
                              : 'bg-white/60 hover:bg-white text-gray-500 border-transparent hover:border-gray-200'
                          }
                        `}
                      >
                        {pace.label}
                      </button>
                    );
                  })}
                </div>
              </div>

              {/* Section 3: Learning Schedule */}
              <div className="space-y-4 pt-1">
                <h3 className="text-xs sm:text-sm font-bold text-[#1A1F36]">
                  Learning Schedule
                </h3>

                {/* Preferred Times of Day */}
                <div>
                  <p className="text-xs text-gray-500 mb-2 font-normal">
                    When do you usually prefer to learn?
                  </p>
                  <div className="flex flex-wrap items-center gap-2">
                    {TIME_OF_DAY_OPTIONS.map((time) => {
                      const isSelected = selectedTimes.includes(time.id);
                      return (
                        <button
                          key={time.id}
                          type="button"
                          onClick={() => toggleTimeOfDay(time.id)}
                          className={`
                            px-3.5 py-1.5 rounded-xl text-xs font-medium transition-all duration-150 cursor-pointer select-none border
                            ${
                              isSelected
                                ? 'bg-[#FAF4F0] border-[#CC7D52] text-[#CC7D52] font-semibold shadow-xs'
                                : 'bg-white/80 hover:bg-white text-gray-700 border-gray-200/80'
                            }
                          `}
                        >
                          {time.label}
                        </button>
                      );
                    })}
                  </div>
                </div>

                {/* Available Days */}
                <div>
                  <p className="text-xs text-gray-500 mb-2 font-normal">
                    Which days are you usually available?
                  </p>
                  <div className="flex items-center gap-2">
                    {DAYS_OF_WEEK.map((day) => {
                      const isSelected = selectedDays.includes(day.id);
                      return (
                        <button
                          key={day.id}
                          type="button"
                          aria-label={day.name}
                          onClick={() => toggleDay(day.id)}
                          className={`
                            w-8 h-8 rounded-full text-xs font-semibold flex items-center justify-center transition-all duration-150 cursor-pointer select-none border
                            ${
                              isSelected
                                ? 'bg-[#FAF4F0] border-[#CC7D52] text-[#CC7D52] shadow-2xs font-bold'
                                : 'bg-white/80 hover:bg-white text-gray-400 border-gray-200/70'
                            }
                          `}
                        >
                          {day.label}
                        </button>
                      );
                    })}
                  </div>
                </div>
              </div>
            </div>

            {/* Right Column: Schedule Preview & Insight (5 cols on lg) */}
            <div className="lg:col-span-5 space-y-4">
              {/* Dynamic Sample Schedule */}
              <SchedulePreview hoursId={selectedHours} />

              {/* Personalization Insight Card */}
              <div className="w-full bg-[#F2EFFE]/90 border border-[#E6E1FF] rounded-2xl sm:rounded-3xl p-5 flex items-start gap-3.5 shadow-xs">
                <div className="w-8 h-8 rounded-xl bg-white/80 border border-[#DDD7FF] flex items-center justify-center shrink-0 text-[#6B65E0] shadow-xs">
                  <Sparkles className="w-4 h-4 text-[#CC7D52]" />
                </div>
                <p className="text-xs sm:text-[13px] text-gray-700 leading-relaxed font-normal">
                  <strong className="font-semibold text-[#1A1F36]">LearnAI will keep your path realistic.</strong> We&apos;ll prioritize consistent progress instead of overwhelming you.
                </p>
              </div>
            </div>
          </div>

          {/* Bottom Action Controls */}
          <div className="flex items-center justify-between pt-2">
            {/* Back Button */}
            <button
              type="button"
              onClick={handleBack}
              className="inline-flex items-center gap-1.5 text-xs sm:text-sm font-semibold text-[#1A1F36] hover:text-[#CC7D52] px-3 py-2 transition-colors cursor-pointer"
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
    </div>
  );
};
