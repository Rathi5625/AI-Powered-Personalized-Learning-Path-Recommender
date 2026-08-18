import { useState } from 'react';
import { api } from '../api/client';
import type { PersonalizedLearningPath } from '../api/types';
import { LearningPathScene } from '../components/three/LearningPathScene';
import { ErrorMessage } from '../components/ui/ErrorMessage';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { PageHeader } from '../components/ui/PageHeader';
import { useAuth } from '../context/AuthContext';

export function LearningPathPage() {
  const { session } = useAuth();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [path, setPath] = useState<PersonalizedLearningPath | null>(null);

  async function generatePath() {
    if (!session?.user.id || !session.careerId) return;
    setLoading(true);
    setError('');
    try {
      const response = await api.generateLearningPath(session.user.id, session.careerId);
      if (!response.success) {
        throw new Error(response.error ?? 'Failed to generate learning path');
      }
      setPath(response);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Generation failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <PageHeader
        eyebrow="Personalized Learning Path"
        title="AI-generated learning roadmap"
        description="Generate a phased plan tailored to your skill gaps and career goal."
      />
      <button
        type="button"
        onClick={generatePath}
        disabled={loading}
        className="rounded-xl bg-[#38BDF8] px-6 py-3 font-semibold text-[#000000] disabled:opacity-60 transition hover:bg-[#7DD3FC]"
      >
        {loading ? 'Generating with Gemini...' : 'Generate Learning Path'}
      </button>
      {loading && <LoadingSpinner label="Building your personalized path..." />}
      {error && <div className="mt-4"><ErrorMessage message={error} /></div>}
      {path && (
        <div className="mt-8 space-y-6">
          <div className="overflow-hidden rounded-3xl border border-white/10">
            <LearningPathScene phases={path.phases} />
          </div>
          <div className="rounded-2xl border border-white/10 bg-[#111111] p-5">
            <p className="text-sm text-[#FFFFFF]">{path.targetCareer}</p>
            <p className="mt-2 text-[#A1A1AA]">{path.summary}</p>
            <p className="mt-2 text-xs text-[#A1A1AA]/60">
              Powered by {path.provider} · {path.model}
            </p>
          </div>
          {path.phases.map((phase) => (
            <section key={phase.phaseNumber} className="rounded-2xl border border-white/10 p-5">
              <h3 className="text-xl font-semibold text-[#FFFFFF]">
                Phase {phase.phaseNumber}: {phase.phaseTitle}
              </h3>
              <p className="mt-2 text-sm text-[#A1A1AA]/70">{phase.estimatedDuration}</p>
              <p className="mt-3 text-[#A1A1AA]">{phase.explanation}</p>
              <div className="mt-4 flex flex-wrap gap-2">
                {phase.targetSkills.map((skill) => (
                  <span key={skill} className="rounded-full bg-white/10 px-3 py-1 text-xs text-[#A1A1AA]">
                    {skill}
                  </span>
                ))}
              </div>
              <div className="mt-4 space-y-2">
                {phase.courses.map((course) => (
                  <div key={course.courseId} className="rounded-xl bg-[#000000] px-4 py-3">
                    <p className="font-medium text-[#FFFFFF]">{course.courseTitle}</p>
                    <p className="text-sm text-[#A1A1AA]/70">
                      {course.provider} · {course.difficulty} · score {course.score?.toFixed(2)}
                    </p>
                  </div>
                ))}
              </div>
            </section>
          ))}
        </div>
      )}
    </div>
  );
}
