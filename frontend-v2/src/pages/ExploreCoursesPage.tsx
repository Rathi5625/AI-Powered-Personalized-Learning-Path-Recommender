import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Sparkles, BookOpen, Clock, Star } from 'lucide-react';
import { AmbientBackground } from '../components/landing/AmbientBackground';
import { ExploreCoursesSidebar } from '../components/explore-courses/ExploreCoursesSidebar';
import { ExploreCoursesTopBar } from '../components/explore-courses/ExploreCoursesTopBar';
import { PersonalizedRecommendationBanner } from '../components/explore-courses/PersonalizedRecommendationBanner';
import api from '../api/client';
import { Course } from '../api/types';

export const ExploreCoursesPage: React.FC = () => {
  const navigate = useNavigate();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('All Courses');
  const [selectedDifficulty, setSelectedDifficulty] = useState<string>('ALL');
  const [showUpgradeModal, setShowUpgradeModal] = useState(false);
  const [toast, setToast] = useState<string | null>(null);

  const [courses, setCourses] = useState<Course[]>([]);
  const [categories, setCategories] = useState<string[]>([
    'All Courses',
    'Computer Science',
    'Web Development',
    'Data Science',
    'Artificial Intelligence',
    'Cloud & DevOps'
  ]);
  const [loading, setLoading] = useState(true);

  const fetchCourses = async () => {
    try {
      setLoading(true);
      if (searchQuery.trim()) {
        const results = await api.searchCourses(searchQuery.trim());
        setCourses(results);
      } else if (selectedDifficulty !== 'ALL') {
        const res = await api.filterCourses({
          difficulty: selectedDifficulty,
          page: 0,
          size: 24,
        });
        setCourses(res.content || []);
      } else {
        const res = await api.getCourses({ page: 0, size: 24 });
        setCourses(res.content || []);
      }
    } catch (err) {
      console.error('Failed to load courses:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCourses();
  }, [searchQuery, selectedDifficulty]);

  useEffect(() => {
    const loadCategories = async () => {
      try {
        const cats = await api.getCategories();
        if (cats && cats.length > 0) setCategories(cats);
      } catch (err) {
        console.error('Failed to load categories:', err);
      }
    };
    loadCategories();
  }, []);

  const showToastNotice = (msg: string) => {
    setToast(msg);
    setTimeout(() => setToast(null), 3000);
  };

  const handleViewPersonalized = () => {
    navigate('/learning-path');
  };

  const handleEnroll = async (course: Course) => {
    try {
      await api.enrollCourse(course.id);
      showToastNotice(`Enrolled in "${course.title}"! Added to your dashboard.`);
    } catch (err: any) {
      showToastNotice(err.message || `Failed to enroll in ${course.title}`);
    }
  };

  return (
    <div className="relative min-h-screen flex bg-[#f9f9ff] text-[#0f1b32] selection:bg-[#ffdbcb] selection:text-[#8e4d2b] overflow-x-hidden">
      {/* Ambient background lighting */}
      <AmbientBackground />

      {/* Desktop Fixed Left Sidebar */}
      <ExploreCoursesSidebar onUpgrade={() => setShowUpgradeModal(true)} />

      {/* Mobile Drawer */}
      <AnimatePresence>
        {mobileMenuOpen && (
          <div className="fixed inset-0 z-50 lg:hidden flex">
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setMobileMenuOpen(false)}
              className="fixed inset-0 bg-black/30 backdrop-blur-xs"
            />
            <motion.div
              initial={{ x: -260 }}
              animate={{ x: 0 }}
              exit={{ x: -260 }}
              transition={{ type: 'spring', damping: 25, stiffness: 280 }}
              className="relative w-[260px] bg-white h-full z-10 p-5 flex flex-col justify-between shadow-2xl"
            >
              <div className="flex items-center justify-between pb-4 border-b border-gray-100">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-xl bg-[#8e4d2b] text-white flex items-center justify-center font-bold text-sm">
                    L
                  </div>
                  <span className="font-extrabold text-base text-[#0f1b32]">LearnAI</span>
                </div>
                <button
                  type="button"
                  aria-label="Close menu"
                  onClick={() => setMobileMenuOpen(false)}
                  className="p-1 text-gray-400 hover:text-gray-600"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              <div className="space-y-1 py-4">
                <a
                  href="/dashboard"
                  className="flex items-center gap-3 px-3 py-2 rounded-xl text-xs font-medium text-gray-700 hover:bg-gray-50"
                >
                  Dashboard
                </a>
                <a
                  href="/learning-path"
                  className="flex items-center gap-3 px-3 py-2 rounded-xl text-xs font-medium text-gray-700 hover:bg-gray-50"
                >
                  My Learning Path
                </a>
                <a
                  href="/explore-courses"
                  className="flex items-center gap-3 px-3 py-2 rounded-xl text-xs font-bold bg-[#ffdbcb]/60 text-[#8e4d2b]"
                >
                  Explore Courses
                </a>
              </div>

              <div className="pt-4 border-t border-gray-100 text-xs text-gray-400">
                © 2026 LearnAI Platform
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0 z-10">
        {/* Top Navigation Bar */}
        <ExploreCoursesTopBar
          onToggleMobileMenu={() => setMobileMenuOpen(true)}
          searchQuery={searchQuery}
          onSearchChange={(q) => {
            setSearchQuery(q);
          }}
        />

        {/* Content Body */}
        <main className="flex-1 px-4 sm:px-8 py-6 sm:py-8 max-w-7xl w-full mx-auto space-y-6 sm:space-y-8">
          {/* Main AI Curated Recommendation Banner */}
          <PersonalizedRecommendationBanner
            targetRole="Software Engineer"
            onViewPersonalized={handleViewPersonalized}
          />

          {/* Category Filter Pills & Difficulty Selector */}
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
            <div className="flex items-center gap-2 overflow-x-auto pb-2 sm:pb-0 w-full sm:w-auto scrollbar-none">
              {categories.map((cat) => (
                <button
                  key={cat}
                  onClick={() => setSelectedCategory(cat)}
                  className={`px-4 py-2 rounded-full text-xs font-bold whitespace-nowrap transition-all cursor-pointer ${
                    selectedCategory === cat
                      ? 'bg-[#8e4d2b] text-white shadow-2xs'
                      : 'bg-white/80 text-gray-600 hover:bg-white border border-gray-200/80'
                  }`}
                >
                  {cat}
                </button>
              ))}
            </div>

            <div className="flex items-center gap-2 self-end sm:self-auto">
              <span className="text-xs font-bold text-gray-500">Difficulty:</span>
              <select
                value={selectedDifficulty}
                onChange={(e) => setSelectedDifficulty(e.target.value)}
                className="px-3 py-1.5 rounded-xl bg-white/90 border border-gray-200 text-xs font-bold text-[#0f1b32] focus:outline-none focus:border-[#8e4d2b]"
              >
                <option value="ALL">All Levels</option>
                <option value="BEGINNER">Beginner</option>
                <option value="INTERMEDIATE">Intermediate</option>
                <option value="ADVANCED">Advanced</option>
              </select>
            </div>
          </div>

          {/* Courses Catalog Grid */}
          <section aria-label="Course Catalog" className="space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-extrabold text-[#0f1b32]">
                {selectedCategory} <span className="text-xs text-gray-400 font-normal">({courses.length} courses)</span>
              </h3>
            </div>

            {loading ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {[1, 2, 3, 4, 5, 6].map((n) => (
                  <div key={n} className="h-64 rounded-3xl bg-white/50 animate-pulse border border-white/60" />
                ))}
              </div>
            ) : courses.length === 0 ? (
              <div className="text-center py-16 bg-white/60 rounded-3xl border border-dashed border-gray-300">
                <BookOpen className="w-8 h-8 text-gray-400 mx-auto mb-3" />
                <h4 className="text-sm font-bold text-[#0f1b32]">No courses found</h4>
                <p className="text-xs text-gray-500 mt-1">Try adjusting your search query or filters.</p>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {courses.map((course) => (
                  <div
                    key={course.id}
                    className="rounded-3xl bg-white/85 backdrop-blur-xl border border-white/90 p-5 shadow-[0_8px_32px_rgba(23,35,58,0.04)] flex flex-col justify-between hover:shadow-[0_12px_40px_rgba(23,35,58,0.08)] hover:-translate-y-0.5 transition-all text-left group"
                  >
                    <div className="space-y-3">
                      <div className="flex items-center justify-between">
                        <span className="px-2.5 py-1 rounded-lg bg-[#FAF4F0] border border-[#F2DACB] text-[#8e4d2b] text-[10px] font-extrabold uppercase">
                          {course.provider || 'LearnAI'}
                        </span>
                        <span className="text-[11px] font-bold text-gray-500">
                          {course.difficulty || 'Intermediate'}
                        </span>
                      </div>

                      <h4 className="text-base font-bold text-[#0f1b32] group-hover:text-[#8e4d2b] transition-colors line-clamp-2">
                        {course.title}
                      </h4>

                      <p className="text-xs text-[#53433c] line-clamp-2 leading-relaxed">
                        {course.description || 'Comprehensive, project-guided curriculum to master core domain concepts.'}
                      </p>
                    </div>

                    <div className="pt-4 border-t border-gray-100/80 mt-4 space-y-3">
                      <div className="flex items-center justify-between text-[11px] font-semibold text-gray-500">
                        <div className="flex items-center gap-1">
                          <Clock className="w-3.5 h-3.5 text-gray-400" />
                          <span>{course.durationMinutes ? `${Math.round(course.durationMinutes / 60)}h` : '8 hours'}</span>
                        </div>
                        <div className="flex items-center gap-1 text-amber-500">
                          <Star className="w-3.5 h-3.5 fill-current" />
                          <span>{course.rating ? course.rating.toFixed(1) : '4.8'}</span>
                        </div>
                      </div>

                      <div className="flex items-center gap-2">
                        <button
                          type="button"
                          onClick={() => handleEnroll(course)}
                          className="flex-1 py-2.5 rounded-xl bg-[#8e4d2b] hover:bg-[#783e20] text-white text-xs font-bold transition-colors shadow-2xs text-center cursor-pointer active:scale-98"
                        >
                          Enroll Now
                        </button>
                        <a
                          href={course.url || '#'}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="px-3.5 py-2.5 rounded-xl border border-gray-200 text-xs font-bold text-gray-700 hover:bg-gray-50 transition-colors text-center"
                        >
                          Preview
                        </a>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>
        </main>
      </div>

      {/* Modal: Upgrade to Pro */}
      <AnimatePresence>
        {showUpgradeModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setShowUpgradeModal(false)}
              className="fixed inset-0 bg-black/40 backdrop-blur-xs"
            />
            <motion.div
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              className="relative w-full max-w-md bg-white rounded-3xl p-6 sm:p-7 shadow-2xl border border-gray-100 z-10 text-left"
            >
              <div className="flex items-center justify-between pb-3 border-b border-gray-100 mb-4">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-xl bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center text-[#8e4d2b]">
                    <Sparkles className="w-4 h-4" />
                  </div>
                  <h3 className="text-base font-bold text-[#0f1b32]">Upgrade to LearnAI Pro</h3>
                </div>
                <button
                  type="button"
                  aria-label="Close modal"
                  onClick={() => setShowUpgradeModal(false)}
                  className="text-gray-400 hover:text-gray-600 p-1"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>

              <p className="text-xs sm:text-sm text-gray-600 leading-relaxed font-normal mb-4">
                Unlock unlimited AI path recommendations, 1-on-1 AI Mentor chats, personalized code reviews, and priority course generation.
              </p>

              <button
                type="button"
                onClick={() => {
                  setShowUpgradeModal(false);
                  showToastNotice('Upgrade feature will be connected with billing soon!');
                }}
                className="w-full py-3 rounded-xl bg-[#8e4d2b] hover:bg-[#783e20] text-white text-xs font-bold transition-colors cursor-pointer shadow-sm"
              >
                Get Started with Pro
              </button>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Live Toast Notification */}
      <AnimatePresence>
        {toast && (
          <motion.div
            initial={{ opacity: 0, y: 18 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 18 }}
            className="fixed bottom-6 left-1/2 -translate-x-1/2 bg-[#0f1b32] text-white text-xs font-medium px-4 py-2.5 rounded-full shadow-lg z-50 flex items-center gap-2 whitespace-nowrap"
          >
            <Sparkles className="w-3.5 h-3.5 text-[#ffdbcb]" />
            <span>{toast}</span>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};
