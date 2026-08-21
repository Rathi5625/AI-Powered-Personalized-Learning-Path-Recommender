import React from 'react';
import { Sparkles, Bookmark, BookmarkCheck, MessageSquare } from 'lucide-react';
import { ArchitectureDiagram } from './ArchitectureDiagram';

interface ProjectHeroProps {
  isSaved: boolean;
  isStarted: boolean;
  onToggleSave: () => void;
  onStartOrContinue: () => void;
  onOpenAIMentor: () => void;
}

export const ProjectHero: React.FC<ProjectHeroProps> = ({
  isSaved,
  isStarted,
  onToggleSave,
  onStartOrContinue,
  onOpenAIMentor,
}) => {
  return (
    <section
      aria-label="Project Overview Hero"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-8 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left"
    >
      <div className="grid grid-cols-1 md:grid-cols-12 gap-6 sm:gap-8 items-center">
        {/* Left Column: Architecture Visualization */}
        <div className="md:col-span-5 w-full">
          <ArchitectureDiagram />
        </div>

        {/* Right Column: Metadata, Title, Description, CTA Actions */}
        <div className="md:col-span-7 space-y-4">
          {/* Metadata Badges */}
          <div className="flex items-center gap-2 flex-wrap">
            <span className="px-3 py-1 rounded-full bg-[#e1d8fe]/60 border border-[#c7b8fe] text-[#615a7a] text-[10px] sm:text-[11px] font-bold uppercase tracking-wider shadow-2xs">
              INTERMEDIATE
            </span>
            <span className="px-3 py-1 rounded-full bg-[#f1f3ff] border border-gray-200/80 text-gray-700 text-[10px] sm:text-[11px] font-bold uppercase tracking-wider shadow-2xs">
              7 DAYS
            </span>
            <span className="px-3 py-1 rounded-full bg-[#f1f3ff] border border-gray-200/80 text-gray-700 text-[10px] sm:text-[11px] font-bold uppercase tracking-wider shadow-2xs">
              10 HRS/WK
            </span>
            <span className="inline-flex items-center gap-1 px-3 py-1 rounded-full bg-[#ffdbcb]/60 border border-[#d98b63]/30 text-[#8e4d2b] text-[10px] sm:text-[11px] font-bold uppercase tracking-wider shadow-2xs">
              <Sparkles className="w-3 h-3 text-[#8e4d2b]" />
              <span>94% AI MATCH</span>
            </span>
          </div>

          {/* Project Title with Clipped Gradient */}
          <h1 className="text-2xl sm:text-3xl lg:text-[34px] font-extrabold text-[#0f1b32] tracking-tight leading-tight">
            Build a Spring Boot{' '}
            <span className="bg-gradient-to-r from-[#8e4d2b] to-[#d98b63] bg-clip-text text-transparent block sm:inline">
              E-Commerce API
            </span>
          </h1>

          {/* Description */}
          <p className="text-xs sm:text-sm text-[#53433c] leading-relaxed font-normal">
            Build a production-style backend application with authentication, product management,
            orders and robust database integration using JPA and MySQL.
          </p>

          {/* Actions Row */}
          <div className="flex items-center gap-3 pt-2 flex-wrap">
            {/* Primary Start / Continue Button */}
            <button
              type="button"
              onClick={onStartOrContinue}
              className="px-6 py-3 rounded-2xl bg-[#8e4d2b] hover:bg-[#783e20] text-white font-bold text-xs sm:text-sm shadow-sm hover:shadow-md transition-all cursor-pointer active:scale-[0.98]"
            >
              {isStarted ? 'Continue Project' : 'Start Project'}
            </button>

            {/* Save Button */}
            <button
              type="button"
              onClick={onToggleSave}
              className="px-4 py-3 rounded-2xl bg-white/80 hover:bg-white border border-gray-200/80 text-[#0f1b32] font-bold text-xs flex items-center gap-1.5 shadow-2xs transition-all cursor-pointer"
            >
              {isSaved ? (
                <>
                  <BookmarkCheck className="w-4 h-4 text-[#8e4d2b]" />
                  <span>Saved</span>
                </>
              ) : (
                <>
                  <Bookmark className="w-4 h-4 text-gray-500" />
                  <span>Save</span>
                </>
              )}
            </button>

            {/* Ask AI Button */}
            <button
              type="button"
              onClick={onOpenAIMentor}
              aria-label="Ask AI Mentor"
              className="p-3 rounded-2xl bg-white/80 hover:bg-white border border-gray-200/80 text-gray-700 hover:text-[#8e4d2b] shadow-2xs transition-all cursor-pointer"
            >
              <MessageSquare className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>
    </section>
  );
};
