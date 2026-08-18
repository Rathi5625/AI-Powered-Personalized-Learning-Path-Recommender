import { type FormEvent, useState } from 'react';
import { api } from '../api/client';
import type { ExperienceLevel, LearningStyle, PreferredContentType } from '../api/types';
import { ErrorMessage } from '../components/ui/ErrorMessage';
import { PageHeader } from '../components/ui/PageHeader';
import { useAuth } from '../context/AuthContext';

const experienceLevels: ExperienceLevel[] = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'];
const learningStyles: LearningStyle[] = ['PRACTICAL', 'THEORETICAL', 'VISUAL', 'AUDITORY', 'READING_WRITING'];
const contentTypes: PreferredContentType[] = ['VIDEO', 'ARTICLE', 'INTERACTIVE_EXERCISE', 'BOOK', 'PROJECT'];

export function ProfilePage() {
  const { session, refreshUser } = useAuth();
  const [name, setName] = useState(session?.user.name ?? '');
  const [careerGoal, setCareerGoal] = useState(session?.user.careerGoal ?? session?.careerName ?? '');
  const [experienceLevel, setExperienceLevel] = useState<ExperienceLevel>(
    session?.user.experienceLevel ?? 'BEGINNER',
  );
  const [dailyLearningHours, setDailyLearningHours] = useState(session?.user.dailyLearningHours ?? 2);
  const [learningStyle, setLearningStyle] = useState<LearningStyle>(
    session?.user.learningStyle ?? 'PRACTICAL',
  );
  const [preferredContentType, setPreferredContentType] = useState<PreferredContentType>(
    session?.user.preferredContentType ?? 'VIDEO',
  );
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    if (!session?.user.id) return;
    setSaving(true);
    setError('');
    setMessage('');
    try {
      await api.updateUser(session.user.id, {
        name,
        careerGoal,
        experienceLevel,
        dailyLearningHours,
        learningStyle,
        preferredContentType,
      });
      await refreshUser();
      setMessage('Profile updated successfully.');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Update failed');
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="max-w-2xl">
      <PageHeader
        eyebrow="Profile & Settings"
        title="Your learning preferences"
        description="Update your profile to improve recommendations and path generation."
      />
      <form onSubmit={handleSubmit} className="space-y-5 rounded-3xl border border-white/10 bg-[#111111] p-6">
        <label className="block">
          <span className="mb-2 block text-sm text-[#A1A1AA]">Name</span>
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="w-full rounded-xl border border-white/10 bg-[#000000] px-4 py-3 text-[#FFFFFF]"
          />
        </label>
        <label className="block">
          <span className="mb-2 block text-sm text-[#A1A1AA]">Email</span>
          <input
            value={session?.user.email ?? ''}
            disabled
            className="w-full rounded-xl border border-white/10 bg-[#111111] px-4 py-3 text-[#A1A1AA]/60"
          />
        </label>
        <label className="block">
          <span className="mb-2 block text-sm text-[#A1A1AA]">Career goal</span>
          <input
            value={careerGoal}
            onChange={(e) => setCareerGoal(e.target.value)}
            className="w-full rounded-xl border border-white/10 bg-[#000000] px-4 py-3 text-[#FFFFFF]"
          />
        </label>
        <label className="block">
          <span className="mb-2 block text-sm text-[#A1A1AA]">Experience level</span>
          <select
            value={experienceLevel}
            onChange={(e) => setExperienceLevel(e.target.value as ExperienceLevel)}
            className="w-full rounded-xl border border-white/10 bg-[#000000] px-4 py-3 text-[#FFFFFF]"
          >
            {experienceLevels.map((level) => (
              <option key={level} value={level}>
                {level}
              </option>
            ))}
          </select>
        </label>
        <label className="block">
          <span className="mb-2 block text-sm text-[#A1A1AA]">Daily learning hours</span>
          <input
            type="number"
            min={1}
            max={24}
            value={dailyLearningHours}
            onChange={(e) => setDailyLearningHours(Number(e.target.value))}
            className="w-full rounded-xl border border-white/10 bg-[#000000] px-4 py-3 text-[#FFFFFF]"
          />
        </label>
        <label className="block">
          <span className="mb-2 block text-sm text-[#A1A1AA]">Learning style</span>
          <select
            value={learningStyle}
            onChange={(e) => setLearningStyle(e.target.value as LearningStyle)}
            className="w-full rounded-xl border border-white/10 bg-[#000000] px-4 py-3 text-[#FFFFFF]"
          >
            {learningStyles.map((style) => (
              <option key={style} value={style}>
                {style.replace('_', ' ')}
              </option>
            ))}
          </select>
        </label>
        <label className="block">
          <span className="mb-2 block text-sm text-[#A1A1AA]">Preferred content type</span>
          <select
            value={preferredContentType}
            onChange={(e) => setPreferredContentType(e.target.value as PreferredContentType)}
            className="w-full rounded-xl border border-white/10 bg-[#000000] px-4 py-3 text-[#FFFFFF]"
          >
            {contentTypes.map((type) => (
              <option key={type} value={type}>
                {type.replace('_', ' ')}
              </option>
            ))}
          </select>
        </label>
        {error && <ErrorMessage message={error} />}
        {message && (
          <p className="rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-sm text-[#A1A1AA]">
            {message}
          </p>
        )}
        <button
          type="submit"
          disabled={saving}
          className="rounded-xl bg-[#38BDF8] px-6 py-3 font-semibold text-[#000000] disabled:opacity-60 hover:bg-[#7DD3FC] transition"
        >
          {saving ? 'Saving...' : 'Save changes'}
        </button>
      </form>
    </div>
  );
}
