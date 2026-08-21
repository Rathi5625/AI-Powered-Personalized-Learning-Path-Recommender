import React from 'react';
import {
  Sparkles,
  Star,
  Clock,
  BarChart2,
  Plus,
  Check,
  Play,
  Heart,
  GraduationCap,
} from 'lucide-react';

interface CourseHeroCardProps {
  isAdded: boolean;
  isFavorite: boolean;
  onToggleAdd: () => void;
  onStartCourse: () => void;
  onToggleFavorite: () => void;
}

export const CourseHeroCard: React.FC<CourseHeroCardProps> = ({
  isAdded,
  isFavorite,
  onToggleAdd,
  onStartCourse,
  onToggleFavorite,
}) => {
  return (
    <section
      aria-label="Course Overview"
      className="relative overflow-hidden rounded-3xl bg-white/70 backdrop-blur-2xl border border-white/80 p-6 sm:p-8 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left"
    >
      {/* Subtle warm radial background aura */}
      <div className="pointer-events-none absolute -right-16 -top-16 w-72 h-72 rounded-full bg-radial from-[#ffdbcb]/30 to-transparent blur-2xl" />

      <div className="relative z-10 grid grid-cols-1 md:grid-cols-12 gap-6 lg:gap-8 items-center">
        {/* Left Column: Course Thumbnail (5 cols on md/lg) */}
        <div className="md:col-span-5 relative w-full aspect-[4/3] rounded-2xl overflow-hidden border border-gray-200/70 shadow-sm group">
          {/* Visual DSA Neural / Network Graph Background */}
          <div className="w-full h-full bg-gradient-to-br from-[#1b253d] via-[#243556] to-[#0f1b32] relative flex items-center justify-center p-4">
            {/* SVG Network Visual representation */}
            <svg
              className="w-full h-full opacity-90 transition-transform duration-700 group-hover:scale-105"
              viewBox="0 0 300 225"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <defs>
                <linearGradient id="lineGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stopColor="#d98b63" stopOpacity="0.8" />
                  <stop offset="100%" stopColor="#8e4d2b" stopOpacity="0.3" />
                </linearGradient>
                <linearGradient id="nodeGrad1" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stopColor="#ffdbcb" />
                  <stop offset="100%" stopColor="#d98b63" />
                </linearGradient>
                <linearGradient id="nodeGrad2" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stopColor="#e1d8fe" />
                  <stop offset="100%" stopColor="#818cf8" />
                </linearGradient>
              </defs>

              {/* Connecting Edges */}
              <path d="M70 120 L150 70" stroke="url(#lineGrad)" strokeWidth="2.5" strokeDasharray="3 3" />
              <path d="M150 70 L230 110" stroke="url(#lineGrad)" strokeWidth="2.5" />
              <path d="M70 120 L120 170" stroke="url(#lineGrad)" strokeWidth="2" />
              <path d="M120 170 L190 175" stroke="url(#lineGrad)" strokeWidth="2.5" />
              <path d="M190 175 L230 110" stroke="url(#lineGrad)" strokeWidth="2" />
              <path d="M150 70 L190 175" stroke="url(#lineGrad)" strokeWidth="1.5" strokeDasharray="4 4" />

              {/* Glowing Nodes */}
              <circle cx="70" cy="120" r="14" fill="url(#nodeGrad2)" filter="drop-shadow(0 0 8px rgba(129, 140, 248, 0.6))" />
              <text x="70" y="124" fontSize="10" fill="#0f1b32" fontWeight="bold" textAnchor="middle">O(1)</text>

              <circle cx="150" cy="70" r="20" fill="url(#nodeGrad1)" filter="drop-shadow(0 0 12px rgba(217, 139, 99, 0.8))" />
              <text x="150" y="75" fontSize="12" fill="#fff" fontWeight="bold" textAnchor="middle">Tree</text>

              <circle cx="230" cy="110" r="16" fill="url(#nodeGrad2)" filter="drop-shadow(0 0 10px rgba(129, 140, 248, 0.6))" />
              <text x="230" y="114" fontSize="11" fill="#0f1b32" fontWeight="bold" textAnchor="middle">Graph</text>

              <circle cx="120" cy="170" r="12" fill="url(#nodeGrad1)" filter="drop-shadow(0 0 6px rgba(217, 139, 99, 0.6))" />
              <circle cx="190" cy="175" r="15" fill="url(#nodeGrad2)" filter="drop-shadow(0 0 8px rgba(129, 140, 248, 0.6))" />
              <text x="190" y="179" fontSize="10" fill="#0f1b32" fontWeight="bold" textAnchor="middle">DP</text>
            </svg>

            {/* Glowing bottom gradient overlay */}
            <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent pointer-events-none" />
          </div>

          {/* Provider Badge: Coursera */}
          <div className="absolute bottom-3 left-3 bg-white/95 backdrop-blur-md px-3 py-1.5 rounded-full border border-blue-100 flex items-center gap-1.5 shadow-sm">
            <GraduationCap className="w-3.5 h-3.5 text-blue-600" />
            <span className="text-xs font-bold text-blue-700 tracking-tight">Coursera</span>
          </div>
        </div>

        {/* Right Column: Course Details & Actions (7 cols on md/lg) */}
        <div className="md:col-span-7 space-y-3.5">
          {/* AI Match Badge */}
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#e1d8fe]/60 border border-[#c7b8fe] text-[#615a7a] text-xs font-bold tracking-wide shadow-2xs">
            <Sparkles className="w-3.5 h-3.5 text-[#615a7a]" />
            <span>96% AI MATCH</span>
          </div>

          {/* Course Title */}
          <h1 className="text-2xl sm:text-3xl lg:text-[34px] font-extrabold text-[#0f1b32] tracking-tight leading-tight">
            Data Structures &amp; Algorithms Specialization
          </h1>

          {/* Subtitle */}
          <p className="text-xs sm:text-sm text-[#53433c] font-normal leading-relaxed">
            Highly relevant to your current learning path.
          </p>

          {/* Course Metadata Row */}
          <div className="space-y-1.5 pt-1 text-xs text-[#53433c]">
            <div className="flex items-center gap-4 flex-wrap">
              <div className="flex items-center gap-1 font-semibold text-[#0f1b32]">
                <Star className="w-4 h-4 text-amber-500 fill-amber-500" />
                <span>4.8</span>
                <span className="text-gray-400 font-normal">(12,450 ratings)</span>
              </div>
              <div className="flex items-center gap-1 text-[#53433c]">
                <Clock className="w-3.5 h-3.5 text-gray-400" />
                <span>32 hours</span>
              </div>
            </div>
            <div className="flex items-center gap-1.5 text-[#53433c]">
              <BarChart2 className="w-3.5 h-3.5 text-gray-400" />
              <span>Intermediate</span>
            </div>
          </div>

          {/* Primary & Secondary Actions */}
          <div className="flex items-center gap-2.5 sm:gap-3 pt-2 flex-wrap">
            {/* Add to Learning Path Button */}
            <button
              type="button"
              onClick={onToggleAdd}
              className={`
                flex items-center gap-2 px-5 py-2.5 sm:py-3 rounded-2xl font-bold text-xs sm:text-sm shadow-sm transition-all duration-200 cursor-pointer active:scale-[0.98]
                ${
                  isAdded
                    ? 'bg-emerald-600 hover:bg-emerald-700 text-white shadow-emerald-600/20'
                    : 'bg-[#d98b63] hover:bg-[#8e4d2b] text-white shadow-[#d98b63]/20'
                }
              `}
            >
              {isAdded ? (
                <>
                  <Check className="w-4 h-4" />
                  <span>Added to My Learning Path</span>
                </>
              ) : (
                <>
                  <Plus className="w-4 h-4" />
                  <span>Add to My Learning Path</span>
                </>
              )}
            </button>

            {/* Start Course Button */}
            <button
              type="button"
              onClick={onStartCourse}
              className="flex items-center gap-2 px-5 py-2.5 sm:py-3 rounded-2xl bg-white/90 hover:bg-white text-[#0f1b32] font-bold text-xs sm:text-sm border border-gray-200/80 shadow-2xs hover:shadow-sm transition-all duration-200 cursor-pointer active:scale-[0.98]"
            >
              <Play className="w-3.5 h-3.5 text-[#0f1b32] fill-[#0f1b32]" />
              <span>Start Course</span>
            </button>

            {/* Favorite Toggle Button */}
            <button
              type="button"
              onClick={onToggleFavorite}
              aria-label={isFavorite ? 'Remove from favorites' : 'Add to favorites'}
              className="p-2.5 sm:p-3 rounded-2xl bg-white/90 hover:bg-white border border-gray-200/80 text-gray-500 hover:text-red-500 transition-colors shadow-2xs cursor-pointer active:scale-[0.95]"
            >
              <Heart
                className={`w-4 h-4 transition-colors ${
                  isFavorite ? 'text-red-500 fill-red-500' : 'text-gray-400'
                }`}
              />
            </button>
          </div>
        </div>
      </div>
    </section>
  );
};
