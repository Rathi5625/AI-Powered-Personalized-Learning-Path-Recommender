import React from 'react';
import { Search } from 'lucide-react';

interface HelpHeroProps {
  searchQuery: string;
  onSearchChange: (query: string) => void;
  onSelectPopular: (topic: string) => void;
}

const POPULAR_TOPICS = [
  'reset progress',
  'change AI Mentor tone',
  'billing issue',
];

export const HelpHero: React.FC<HelpHeroProps> = ({
  searchQuery,
  onSearchChange,
  onSelectPopular,
}) => {
  return (
    <section
      aria-label="Help and Support Search Hero"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-8 sm:p-12 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-center space-y-6 select-none"
    >
      {/* Title */}
      <h2 className="text-3xl sm:text-4xl font-extrabold text-[#0f1b32] tracking-tight">
        How can we help?
      </h2>

      {/* Large Search Input */}
      <div className="relative max-w-2xl mx-auto">
        <Search className="w-5 h-5 text-gray-400 absolute left-4 top-1/2 -translate-y-1/2 pointer-events-none" />
        <input
          type="text"
          value={searchQuery}
          onChange={(e) => onSearchChange(e.target.value)}
          placeholder="Search for help..."
          className="w-full pl-12 pr-4 py-3.5 rounded-2xl bg-white/90 border border-gray-200/80 text-xs sm:text-sm text-[#0f1b32] placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-[#8e4d2b]/20 focus:border-[#8e4d2b] transition-all shadow-2xs"
        />
      </div>

      {/* Popular Topics */}
      <div className="flex items-center justify-center gap-1.5 flex-wrap text-xs text-gray-500">
        <span className="font-medium">Popular:</span>
        {POPULAR_TOPICS.map((topic, idx) => (
          <button
            key={topic}
            type="button"
            onClick={() => onSelectPopular(topic)}
            className="text-gray-500 hover:text-[#8e4d2b] hover:underline transition-colors cursor-pointer"
          >
            {topic}
            {idx < POPULAR_TOPICS.length - 1 ? ',' : ''}
          </button>
        ))}
      </div>
    </section>
  );
};
