import { useState } from 'react';
import { api } from '../api/client';
import type { AdaptLearningPathResponse } from '../api/types';
import { LearningPathScene } from '../components/three/LearningPathScene';
import { ErrorMessage } from '../components/ui/ErrorMessage';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { PageHeader } from '../components/ui/PageHeader';
import { StatCard } from '../components/ui/StatCard';
import { useAuth } from '../context/AuthContext';

export function AdaptiveLearningPage() {
  const { session } = useAuth();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [result, setResult] = useState<AdaptLearningPathResponse | null>(null);

  async function adaptPath() {
    if (!session?.user.id || !session.careerId) return;
    setLoading(true);
    setError('');
    try {
      const response = await api.adaptLearningPath(session.user.id, session.careerId);
      setResult(response);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Adaptation failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <PageHeader
        eyebrow="Adaptive Learning"
        title="Evolve your path as you grow"
        description="Detect changes in your progress and regenerate your learning path when needed."
      />
      <button
        type="button"
        onClick={adaptPath}
        disabled={loading}
        className="rounded-xl bg-[#38BDF8] px-6 py-3 font-semibold text-[#000000] disabled:opacity-60 transition hover:bg-[#7DD3FC]"
      >
        {loading ? 'Analyzing learner state...' : 'Run Adaptive Update'}
      </button>
      {loading && <LoadingSpinner label="Checking for path changes..." />}
      {error && <div className="mt-4"><ErrorMessage message={error} /></div>}
      {result && (
        <div className="mt-8 space-y-6">
          <div className="grid gap-4 sm:grid-cols-3">
            <StatCard
              label="Path adapted"
              value={result.adapted ? 'Yes' : 'No'}
              accent={result.adapted ? 'emerald' : 'amber'}
            />
            <StatCard label="Completed skills" value={result.completedSkills.length} accent="cyan" />
            <StatCard label="Remaining skills" value={result.remainingSkills.length} accent="rose" />
          </div>
          <div className="rounded-2xl border border-white/10 bg-[#111111] p-5">
            <p className="text-sm text-[#FFFFFF]">Change reason</p>
            <p className="mt-2 text-[#A1A1AA]">{result.changeReason}</p>
          </div>
          <div className="overflow-hidden rounded-3xl border border-white/10">
            <LearningPathScene phases={result.path.phases} />
          </div>
          {result.path.summary && (
            <p className="text-[#A1A1AA]">{result.path.summary}</p>
          )}
        </div>
      )}
    </div>
  );
}
