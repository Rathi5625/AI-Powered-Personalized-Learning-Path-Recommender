import React, { useState, useEffect } from 'react';
import { Plus, X } from 'lucide-react';

const INITIAL_SKILLS = [
  'Java',
  'DSA',
  'React',
  'SQL',
  'Spring Boot',
  'Git',
  'REST APIs',
  'MySQL',
];

interface SkillsCardProps {
  skills?: string[];
  onSkillRemoved?: (skill: string) => void;
  onSkillAdded?: (skill: string) => void;
}

export const SkillsCard: React.FC<SkillsCardProps> = ({
  skills: controlledSkills,
  onSkillRemoved,
  onSkillAdded,
}) => {
  const [skills, setSkills] = useState<string[]>(controlledSkills || INITIAL_SKILLS);
  const [isAdding, setIsAdding] = useState(false);
  const [newSkillText, setNewSkillText] = useState('');

  useEffect(() => {
    if (controlledSkills && controlledSkills.length > 0) {
      setSkills(controlledSkills);
    }
  }, [controlledSkills]);

  const handleRemove = (skillToRemove: string) => {
    setSkills(skills.filter((s) => s !== skillToRemove));
    onSkillRemoved?.(skillToRemove);
  };

  const handleAdd = (e: React.FormEvent) => {
    e.preventDefault();
    const trimmed = newSkillText.trim();
    if (!trimmed) return;

    if (skills.some((s) => s.toLowerCase() === trimmed.toLowerCase())) {
      setIsAdding(false);
      setNewSkillText('');
      return;
    }

    setSkills([...skills, trimmed]);
    onSkillAdded?.(trimmed);
    setIsAdding(false);
    setNewSkillText('');
  };

  return (
    <section
      aria-label="Your Skills"
      className="rounded-3xl bg-white/75 backdrop-blur-2xl border border-white/90 p-6 sm:p-7 shadow-[0_8px_32px_rgba(23,35,58,0.04)] text-left space-y-5 select-none"
    >
      <h3 className="text-base sm:text-lg font-extrabold text-[#0f1b32] tracking-tight">
        Your Skills
      </h3>

      {/* Skill Chips Flex Container */}
      <div className="flex items-center gap-2.5 flex-wrap">
        {skills.map((skill) => (
          <div
            key={skill}
            className="group inline-flex items-center gap-1.5 px-4 py-2 rounded-full bg-[#e9edff]/80 border border-[#d8e2ff] text-xs font-bold text-[#0f1b32] shadow-2xs hover:bg-[#e1d8fe] transition-colors"
          >
            <span>{skill}</span>
            <button
              type="button"
              aria-label={`Remove ${skill}`}
              onClick={() => handleRemove(skill)}
              className="opacity-60 hover:opacity-100 hover:text-red-600 transition-opacity p-0.5"
            >
              <X className="w-3 h-3" />
            </button>
          </div>
        ))}

        {/* Add Skill Button or Inline Form */}
        {isAdding ? (
          <form onSubmit={handleAdd} className="inline-flex items-center gap-1.5">
            <input
              type="text"
              autoFocus
              value={newSkillText}
              onChange={(e) => setNewSkillText(e.target.value)}
              placeholder="e.g. Docker"
              className="px-3 py-1.5 rounded-full bg-white border border-[#8e4d2b] text-xs text-[#0f1b32] focus:outline-none shadow-2xs w-28"
            />
            <button
              type="submit"
              className="px-3 py-1.5 rounded-full bg-[#8e4d2b] text-white text-xs font-bold shadow-2xs cursor-pointer"
            >
              Add
            </button>
            <button
              type="button"
              onClick={() => setIsAdding(false)}
              className="p-1 text-gray-400 hover:text-gray-600"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          </form>
        ) : (
          <button
            type="button"
            onClick={() => setIsAdding(true)}
            className="inline-flex items-center gap-1 px-4 py-2 rounded-full bg-white border border-dashed border-gray-300 hover:border-[#8e4d2b] text-xs font-bold text-[#53433c] hover:text-[#8e4d2b] transition-colors cursor-pointer shadow-2xs active:scale-95"
          >
            <Plus className="w-3.5 h-3.5 text-[#8e4d2b]" />
            <span>Add Skill</span>
          </button>
        )}
      </div>
    </section>
  );
};
