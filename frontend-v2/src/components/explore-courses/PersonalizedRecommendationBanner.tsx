import React from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, Sparkles } from 'lucide-react';

interface PersonalizedRecommendationBannerProps {
  targetRole?: string;
  onViewPersonalized?: () => void;
}

export const PersonalizedRecommendationBanner: React.FC<PersonalizedRecommendationBannerProps> = ({
  targetRole = 'Software Engineer',
  onViewPersonalized,
}) => {
  return (
    <section
      aria-label="AI Recommendation"
      className="relative overflow-hidden rounded-3xl bg-white/70 backdrop-blur-2xl border border-white/80 p-6 sm:p-8 lg:p-10 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left"
    >
      {/* Decorative gradient overlay accents */}
      <div className="pointer-events-none absolute -right-20 -top-20 w-80 h-80 rounded-full bg-radial from-[#ffdbcb]/40 to-transparent blur-2xl" />
      <div className="pointer-events-none absolute -left-20 -bottom-20 w-80 h-80 rounded-full bg-radial from-[#e1d8fe]/30 to-transparent blur-2xl" />

      <div className="relative z-10 space-y-4 sm:space-y-5 max-w-4xl">
        {/* Badge: AI CURATED FOR YOU */}
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#FAF4F0] border border-[#F2DACB] text-[#8e4d2b] text-[11px] font-bold tracking-wider uppercase shadow-2xs">
          <Sparkles className="w-3.5 h-3.5 text-[#8e4d2b]" />
          <span>AI CURATED FOR YOU</span>
        </div>

        {/* Heading */}
        <h2 className="text-2xl sm:text-3xl lg:text-4xl font-extrabold text-[#0f1b32] tracking-tight">
          Recommended for your path
        </h2>

        {/* Description */}
        <p className="text-xs sm:text-sm md:text-base text-[#53433c] leading-relaxed max-w-2xl font-normal">
          These courses are selected to accelerate your goal of becoming a{' '}
          <strong className="font-bold text-[#0f1b32]">{targetRole}</strong>, bridging specific
          skill gaps in your current profile.
        </p>

        {/* CTA Button */}
        <div className="pt-2">
          {onViewPersonalized ? (
            <button
              type="button"
              onClick={onViewPersonalized}
              className="inline-flex items-center gap-2 px-6 py-3 rounded-2xl bg-[#8e4d2b] hover:bg-[#783e20] text-white font-bold text-xs sm:text-sm shadow-sm hover:shadow-md transition-all duration-200 cursor-pointer active:scale-[0.98]"
            >
              <span>View Personalized</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          ) : (
            <Link
              to="/learning-path"
              className="inline-flex items-center gap-2 px-6 py-3 rounded-2xl bg-[#8e4d2b] hover:bg-[#783e20] text-white font-bold text-xs sm:text-sm shadow-sm hover:shadow-md transition-all duration-200 cursor-pointer active:scale-[0.98]"
            >
              <span>View Personalized</span>
              <ArrowRight className="w-4 h-4" />
            </Link>
          )}
        </div>
      </div>
    </section>
  );
};
