import React from 'react';
import { RoadmapPhaseCard, type RoadmapPhase } from './RoadmapPhaseCard';
import { type JourneyFilter } from './JourneyHeader';

interface RoadmapTimelineProps {
  phases: RoadmapPhase[];
  filter: JourneyFilter;
  onContinuePhase?: (phase: RoadmapPhase) => void;
}

export const RoadmapTimeline: React.FC<RoadmapTimelineProps> = ({
  phases,
  filter,
  onContinuePhase,
}) => {
  const filteredPhases = phases.filter((phase) => {
    if (filter === 'all') return true;
    if (filter === 'in_progress') return phase.status === 'in_progress';
    if (filter === 'completed') return phase.status === 'completed';
    return true;
  });

  if (phases.length === 0) {
    return (
      <div className="bg-white/80 backdrop-blur-xl rounded-3xl p-10 text-center space-y-4 border border-white/90 shadow-sm">
        <div className="w-12 h-12 rounded-2xl bg-[#FAF4F0] border border-[#F2DACB] flex items-center justify-center mx-auto text-[#8e4d2b]">
          <span className="font-extrabold text-lg">1</span>
        </div>
        <div>
          <h3 className="text-base font-bold text-[#0f1b32]">Diagnostic Assessment Ready</h3>
          <p className="text-xs text-gray-500 mt-1 max-w-sm mx-auto">
            Take your diagnostic assessment to calibrate your BKT knowledge graph and generate your personalized curriculum.
          </p>
        </div>
        <a
          href="/assessments"
          className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-[#8e4d2b] text-white text-xs font-bold hover:bg-[#783e20] shadow-sm transition-colors"
        >
          Start Diagnostic Assessment
        </a>
      </div>
    );
  }

  if (filteredPhases.length === 0) {
    return (
      <div className="bg-white/60 backdrop-blur-md rounded-2xl p-8 text-center text-gray-500 border border-gray-100">
        <p className="text-sm font-medium">No phases match the selected filter.</p>
      </div>
    );
  }

  return (
    <div className="relative space-y-6 sm:space-y-8">
      {/* Vertical Continuous Connecting Line */}
      <div className="absolute top-4 bottom-4 left-4 w-0.5 bg-gradient-to-b from-[#8e4d2b] via-[#d98b63]/60 to-gray-200 pointer-events-none" />

      {/* Rendered Phase Cards */}
      {filteredPhases.map((phase) => (
        <RoadmapPhaseCard
          key={phase.id}
          phase={phase}
          onContinue={() => onContinuePhase?.(phase)}
        />
      ))}
    </div>
  );
};
