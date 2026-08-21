import React, { useState } from 'react';

interface PersonalObjectiveCardProps {
  onSave?: (objective: string) => void;
}

export const PersonalObjectiveCard: React.FC<PersonalObjectiveCardProps> = ({ onSave }) => {
  const [objective, setObjective] = useState(
    'I want to solidify my backend skills and transition into a full-stack developer role within the next 6 months, specifically focusing on mastering React alongside my existing Java/Spring knowledge.'
  );

  return (
    <section
      aria-label="Personal Objective"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-3"
    >
      <h3 className="text-base sm:text-lg font-extrabold text-[#0f1b32] tracking-tight">
        Personal Objective
      </h3>

      <p className="text-xs text-[#53433c] font-medium leading-relaxed">
        Help the AI mentor understand your specific short-term goals.
      </p>

      {/* Editable Box */}
      <div className="p-4 rounded-2xl bg-white/90 border border-gray-100/90 shadow-2xs">
        <textarea
          rows={3}
          value={objective}
          onChange={(e) => {
            setObjective(e.target.value);
            onSave?.(e.target.value);
          }}
          className="w-full text-xs sm:text-sm text-[#0f1b32] leading-relaxed bg-transparent resize-none focus:outline-none placeholder-gray-400 font-normal"
        />
      </div>
    </section>
  );
};
