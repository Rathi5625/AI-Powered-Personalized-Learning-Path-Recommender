import { useEffect, useState } from 'react';
import type { LearningPathPhase } from '../../api/types';

export interface LearningPathSceneProps {
  phases: LearningPathPhase[];
  className?: string;
}

export function LearningPathScene({ phases, className }: LearningPathSceneProps) {
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  if (!phases || phases.length === 0) {
    return (
      <div className={`p-8 text-center text-[#A1A1AA] ${className || ''}`}>
        No learning phases available.
      </div>
    );
  }

  return (
    <div className={`relative py-4 ${className || ''}`}>
      <div 
        className="absolute left-5 top-0 bottom-0 w-0.5 bg-[#A1A1AA]/20 origin-top transition-transform duration-1000 ease-out"
        style={{ transform: mounted ? 'scaleY(1)' : 'scaleY(0)' }}
      />
      
      <div className="flex flex-col gap-8">
        {phases.map((phase, index) => (
          <div 
            key={phase.phaseNumber} 
            className="relative flex items-center pl-16 group transition-all duration-500 ease-out"
            style={{ 
              opacity: mounted ? 1 : 0, 
              transform: mounted ? 'translateX(0)' : 'translateX(-20px)',
              transitionDelay: `${index * 150}ms`
            }}
          >
            <div className="absolute left-0 w-10 h-10 rounded-full border-2 border-[#38BDF8] bg-[#111111] flex items-center justify-center text-[#38BDF8] font-bold group-hover:bg-[#38BDF8] group-hover:text-[#000000] transition-colors duration-300">
              {index + 1}
            </div>
            
            <div>
              <h3 className="font-semibold text-[#FFFFFF] text-lg transition-colors group-hover:text-[#38BDF8]">
                {phase.phaseTitle || `Phase ${index + 1}`}
              </h3>
              <p className="text-[#A1A1AA] text-sm mt-1">
                {phase.estimatedDuration || 'Estimated duration not set'}
              </p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
