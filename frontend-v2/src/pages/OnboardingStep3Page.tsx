import React, { useState, useMemo } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Search,
  CheckCircle2,
  X,
  Sparkles,
  ArrowRight,
} from 'lucide-react';
import { AmbientBackground } from '../components/landing/AmbientBackground';
import { SegmentedProgress } from '../components/onboarding/SegmentedProgress';

interface SkillItem {
  id: string;
  name: string;
  categories: string[];
}

const CATEGORIES = [
  'ALL',
  'PROGRAMMING',
  'WEB DEVELOPMENT',
  'BACKEND',
  'DATABASE',
  'AI / ML',
  'FUNDAMENTALS',
] as const;

const SKILLS_DATA: SkillItem[] = [
  { id: 'java', name: 'Java', categories: ['PROGRAMMING', 'BACKEND'] },
  { id: 'python', name: 'Python', categories: ['PROGRAMMING', 'AI / ML', 'BACKEND'] },
  { id: 'cpp', name: 'C++', categories: ['PROGRAMMING', 'FUNDAMENTALS'] },
  { id: 'javascript', name: 'JavaScript', categories: ['PROGRAMMING', 'WEB DEVELOPMENT'] },
  { id: 'typescript', name: 'TypeScript', categories: ['PROGRAMMING', 'WEB DEVELOPMENT'] },
  { id: 'react', name: 'React', categories: ['WEB DEVELOPMENT'] },
  { id: 'html_css', name: 'HTML/CSS', categories: ['WEB DEVELOPMENT'] },
  { id: 'nextjs', name: 'Next.js', categories: ['WEB DEVELOPMENT', 'BACKEND'] },
  { id: 'dsa', name: 'DSA', categories: ['FUNDAMENTALS'] },
  { id: 'oop', name: 'OOP', categories: ['FUNDAMENTALS', 'PROGRAMMING'] },
  { id: 'git', name: 'Git', categories: ['FUNDAMENTALS'] },
  { id: 'sql', name: 'SQL', categories: ['DATABASE', 'BACKEND'] },
  { id: 'postgresql', name: 'PostgreSQL', categories: ['DATABASE', 'BACKEND'] },
  { id: 'machine_learning', name: 'Machine Learning', categories: ['AI / ML'] },
  { id: 'deep_learning', name: 'Deep Learning', categories: ['AI / ML'] },
  { id: 'nodejs', name: 'Node.js', categories: ['BACKEND', 'WEB DEVELOPMENT'] },
  { id: 'spring_boot', name: 'Spring Boot', categories: ['BACKEND'] },
];

export const OnboardingStep3Page: React.FC = () => {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('ALL');
  const [selectedSkillIds, setSelectedSkillIds] = useState<string[]>(['java', 'react']);

  // Filter skills by category and search query
  const filteredSkills = useMemo(() => {
    return SKILLS_DATA.filter((skill) => {
      const matchesCategory =
        selectedCategory === 'ALL' || skill.categories.includes(selectedCategory);
      const matchesSearch =
        skill.name.toLowerCase().includes(searchQuery.trim().toLowerCase());
      return matchesCategory && matchesSearch;
    });
  }, [selectedCategory, searchQuery]);

  const toggleSkill = (id: string) => {
    setSelectedSkillIds((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    );
  };

  const removeSkill = (id: string) => {
    setSelectedSkillIds((prev) => prev.filter((item) => item !== id));
  };

  const handleContinue = () => {
    try {
      localStorage.setItem('learnai_onboarding_skills', JSON.stringify(selectedSkillIds));
    } catch {
      // Ignore storage errors
    }
    navigate('/onboarding/step-4');
  };

  const handleBack = () => {
    navigate('/onboarding/step-2');
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
          {/* Top Brand & Skip Navigation Inside Card */}
          <div className="flex items-center justify-between mb-6">
            <div className="w-20"></div> {/* Balance spacer */}
            <Link
              to="/"
              className="font-extrabold text-base sm:text-lg tracking-tight text-[#1A1F36] hover:text-[#8B4D2B] transition-colors"
            >
              LearnAI
            </Link>
            <Link
              to="/"
              className="text-[11px] sm:text-xs font-bold uppercase tracking-wider text-gray-500 hover:text-[#1A1F36] transition-colors text-right"
            >
              SKIP FOR NOW
            </Link>
          </div>

          {/* Segmented 7-bar Progress */}
          <SegmentedProgress currentStep={3} totalSteps={7} />

          {/* Heading and Subtitle */}
          <div className="mt-7 mb-6">
            <h1 className="text-2xl sm:text-3xl font-extrabold text-[#1A1F36] tracking-tight">
              What do you already know?
            </h1>
            <p className="text-xs sm:text-sm text-gray-500 mt-1.5 leading-relaxed font-normal">
              Tell us about the skills you&apos;ve already worked with. Don&apos;t worry about being an expert — we&apos;ll use this to understand your starting point.
            </p>
          </div>

          {/* Search Bar */}
          <div className="relative flex items-center mb-4">
            <Search className="w-4 h-4 text-gray-400 absolute left-4 pointer-events-none" />
            <input
              type="text"
              placeholder="Search for a skill..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full h-11 sm:h-12 pl-11 pr-10 rounded-2xl bg-white/70 border border-gray-200/90 text-[#1A1F36] placeholder:text-gray-400 text-xs sm:text-sm focus:outline-none focus:border-[#CC7D52] focus:ring-3 focus:ring-[#CC7D52]/10 transition-all shadow-xs"
            />
            {searchQuery && (
              <button
                type="button"
                aria-label="Clear search"
                onClick={() => setSearchQuery('')}
                className="absolute right-3.5 text-gray-400 hover:text-gray-600 transition-colors"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            )}
          </div>

          {/* Category Filter Pills */}
          <div className="flex flex-wrap items-center gap-1.5 sm:gap-2 mb-6">
            {CATEGORIES.map((cat) => {
              const isActive = selectedCategory === cat;
              return (
                <button
                  key={cat}
                  type="button"
                  onClick={() => setSelectedCategory(cat)}
                  className={`
                    px-3 sm:px-3.5 py-1.5 rounded-full text-[10px] sm:text-[11px] font-bold uppercase tracking-wider transition-all duration-200 cursor-pointer select-none
                    ${
                      isActive
                        ? 'bg-[#CC7D52] text-[#1A1F36] shadow-xs'
                        : 'bg-white/60 hover:bg-white/90 text-gray-600 border border-transparent hover:border-gray-200/80'
                    }
                  `}
                >
                  {cat}
                </button>
              );
            })}
          </div>

          {/* Skills Grid */}
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-2.5 sm:gap-3 mb-6 min-h-[140px]">
            <AnimatePresence>
              {filteredSkills.map((skill) => {
                const isSelected = selectedSkillIds.includes(skill.id);
                return (
                  <motion.button
                    layout
                    initial={{ opacity: 0, scale: 0.95 }}
                    animate={{ opacity: 1, scale: 1 }}
                    exit={{ opacity: 0, scale: 0.95 }}
                    key={skill.id}
                    type="button"
                    onClick={() => toggleSkill(skill.id)}
                    className={`
                      h-11 sm:h-12 px-3.5 sm:px-4 rounded-xl sm:rounded-2xl text-xs sm:text-sm font-medium transition-all duration-150 flex items-center justify-between cursor-pointer border select-none text-left
                      ${
                        isSelected
                          ? 'bg-[#FAF4F0] border-[#8B4D2B]/80 text-[#1A1F36] shadow-xs font-semibold'
                          : 'bg-white/60 hover:bg-white/90 border-transparent hover:border-gray-200/80 text-[#1A1F36]'
                      }
                    `}
                  >
                    <span className="truncate">{skill.name}</span>
                    {isSelected && (
                      <CheckCircle2 className="w-4 h-4 text-[#8B4D2B] shrink-0 ml-1.5" />
                    )}
                  </motion.button>
                );
              })}
            </AnimatePresence>

            {filteredSkills.length === 0 && (
              <div className="col-span-full py-8 text-center text-xs sm:text-sm text-gray-400">
                No skills found matching &ldquo;{searchQuery}&rdquo;.
              </div>
            )}
          </div>

          {/* Selected Skills Section */}
          <div className="mb-6 pt-2 border-t border-gray-100">
            <h3 className="text-xs sm:text-sm font-bold text-[#1A1F36] mb-2.5">
              Your selected skills
            </h3>
            <div className="flex flex-wrap items-center gap-2 min-h-[32px]">
              <AnimatePresence>
                {selectedSkillIds.map((id) => {
                  const skill = SKILLS_DATA.find((s) => s.id === id);
                  if (!skill) return null;
                  return (
                    <motion.span
                      layout
                      initial={{ opacity: 0, scale: 0.8 }}
                      animate={{ opacity: 1, scale: 1 }}
                      exit={{ opacity: 0, scale: 0.8 }}
                      transition={{ duration: 0.2 }}
                      key={id}
                      className="inline-flex items-center gap-1.5 px-3 py-1 bg-[#EAE8FF] text-[#1A1F36] rounded-full text-xs font-semibold shadow-xs"
                    >
                      <span>{skill.name}</span>
                      <button
                        type="button"
                        aria-label={`Remove ${skill.name}`}
                        onClick={() => removeSkill(id)}
                        className="text-gray-500 hover:text-red-500 transition-colors cursor-pointer"
                      >
                        <X className="w-3 h-3" />
                      </button>
                    </motion.span>
                  );
                })}
              </AnimatePresence>

              {selectedSkillIds.length === 0 && (
                <span className="text-xs text-gray-400 font-normal">
                  No skills selected yet.
                </span>
              )}
            </div>
          </div>

          {/* Personalization Insight Panel */}
          <div className="w-full bg-[#F2EFFE]/90 border border-[#E6E1FF] rounded-2xl sm:rounded-3xl p-4 sm:p-5 flex items-center gap-3.5 shadow-xs mb-6 sm:mb-8">
            <div className="w-8 h-8 rounded-xl bg-white/80 border border-[#DDD7FF] flex items-center justify-center shrink-0 text-[#CC7D52] shadow-xs">
              <Sparkles className="w-4 h-4 text-[#CC7D52]" />
            </div>
            <p className="text-xs sm:text-sm text-gray-700 leading-relaxed font-normal">
              We&apos;ll use these skills to estimate your starting point and identify what you need to learn next.
            </p>
          </div>

          {/* Bottom Action Controls */}
          <div className="flex items-center justify-between pt-2">
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
