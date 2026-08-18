import { useEffect, useMemo, useState } from 'react';
import { api } from '../api/client';
import type { LearningProgress, ProgressStatus } from '../api/types';
import { ProgressOrbit } from '../components/three/ProgressOrbit';
import { ErrorMessage } from '../components/ui/ErrorMessage';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { PageHeader } from '../components/ui/PageHeader';
import { useAuth } from '../context/AuthContext';

const statuses: ProgressStatus[] = ['NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'PAUSED'];

export function ProgressPage() {
  const { session } = useAuth();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState<string | null>(null);
  const [error, setError] = useState('');
  const [progress, setProgress] = useState<LearningProgress[]>([]);

  useEffect(() => {
    if (!session?.user.id) return;
    api
      .getUserProgress(session.user.id)
      .then(setProgress)
      .catch((err) => setError(err instanceof Error ? err.message : 'Failed to load progress'))
      .finally(() => setLoading(false));
  }, [session?.user.id]);

  const averageCompletion = useMemo(() => {
    if (!progress.length) return 0;
    return Math.round(
      progress.reduce((sum, item) => sum + Number(item.completionPercentage), 0) / progress.length,
    );
  }, [progress]);

  async function updateProgress(item: LearningProgress, status: ProgressStatus, completion: number) {
    if (!session?.user.id) return;
    setSaving(item.courseId);
    try {
      const updated = await api.upsertProgress(session.user.id, item.courseId, {
        status,
        completionPercentage: completion,
      });
      setProgress((current) =>
        current.map((entry) => (entry.courseId === updated.courseId ? updated : entry)),
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to update progress');
    } finally {
      setSaving(null);
    }
  }

  if (loading) return <LoadingSpinner label="Loading progress..." />;

  return (
    <div>
      <PageHeader
        eyebrow="Progress Tracking"
        title="Track your course progress"
        description="Update completion status and watch your orbit fill as you learn."
      />
      {error && <div className="mb-4"><ErrorMessage message={error} /></div>}
      <div className="mb-8 overflow-hidden rounded-3xl border border-white/10 bg-[#111111] lg:max-w-md">
        <ProgressOrbit completion={averageCompletion} />
        <p className="pb-4 text-center text-sm text-[#A1A1AA]/70">Average completion: {averageCompletion}%</p>
      </div>
      {!progress.length ? (
        <p className="text-[#A1A1AA]/70">No progress records yet. Generate a learning path and start a course.</p>
      ) : (
        <div className="space-y-4">
          {progress.map((item) => (
            <div key={item.progressId} className="rounded-2xl border border-white/10 bg-[#111111] p-5">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <h3 className="font-semibold text-[#FFFFFF]">{item.courseTitle}</h3>
                  <p className="text-sm text-[#A1A1AA]/70">{item.status.replace('_', ' ')}</p>
                </div>
                <p className="text-2xl font-bold text-[#FFFFFF]">{Number(item.completionPercentage)}%</p>
              </div>
              <input
                type="range"
                min={0}
                max={100}
                value={Number(item.completionPercentage)}
                onChange={(e) =>
                  updateProgress(item, item.status, Number(e.target.value))
                }
                disabled={saving === item.courseId}
                className="mt-4 w-full accent-[#38BDF8]"
              />
              <div className="mt-3 flex flex-wrap gap-2">
                {statuses.map((status) => (
                  <button
                    key={status}
                    type="button"
                    disabled={saving === item.courseId}
                    onClick={() => updateProgress(item, status, Number(item.completionPercentage))}
                    className={`rounded-full border px-3 py-1 text-xs transition ${
                      item.status === status
                        ? 'border-[#38BDF8] bg-[#38BDF8]/10 text-[#38BDF8]'
                        : 'border-white/10 text-[#A1A1AA]/70 hover:bg-white/5'
                    }`}
                  >
                    {status.replace('_', ' ')}
                  </button>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
