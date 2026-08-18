import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { SkillGlobe } from '../components/three/SkillGlobe';
import { ErrorMessage } from '../components/ui/ErrorMessage';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { PageHeader } from '../components/ui/PageHeader';
import { StatCard } from '../components/ui/StatCard';
import { useAuth } from '../context/AuthContext';

export function SkillGapPage() {
  const { session } = useAuth();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [analysis, setAnalysis] = useState<Awaited<ReturnType<typeof api.getSkillGaps>> | null>(null);

  useEffect(() => {
    if (!session?.user.id || !session.careerId) return;
    api
      .getSkillGaps(session.user.id, session.careerId)
      .then(setAnalysis)
      .catch((err) => setError(err instanceof Error ? err.message : 'Failed to analyze skill gaps'))
      .finally(() => setLoading(false));
  }, [session?.user.id, session?.careerId]);

  if (loading) return <LoadingSpinner label="Analyzing skill gaps..." />;
  if (error) return <ErrorMessage message={error} />;
  if (!analysis) return null;

  return (
    <div>
      <PageHeader
        eyebrow="Skill Gap"
        title="Your skill gap analysis"
        description={`Comparing your profile against ${analysis.careerName} requirements.`}
      />
      <div className="grid gap-6 lg:grid-cols-[1fr_1.2fr]">
        <div className="overflow-hidden rounded-3xl border border-white/10 bg-[#111111]">
          <SkillGlobe
            skills={analysis.gaps.map((gap) => ({
              name: gap.skillName,
              severity: gap.severity,
            }))}
          />
        </div>
        <div className="grid gap-4 sm:grid-cols-2">
          <StatCard label="Required skills" value={analysis.totalRequiredSkills} accent="indigo" />
          <StatCard label="No gap" value={analysis.skillsWithNoGap} accent="emerald" />
          <StatCard label="Partial gaps" value={analysis.partialGaps} accent="amber" />
          <StatCard label="Full gaps" value={analysis.fullGaps} accent="rose" />
        </div>
      </div>
      <div className="mt-8 space-y-3">
        {analysis.gaps.map((gap) => (
          <div
            key={gap.skillId}
            className="rounded-2xl border border-white/10 bg-[#111111] p-4"
          >
            <div className="flex flex-wrap items-center justify-between gap-2">
              <div>
                <p className="font-semibold text-[#FFFFFF]">{gap.skillName}</p>
                <p className="text-sm text-[#A1A1AA]/70">{gap.skillCategory}</p>
              </div>
              <div className="flex gap-2">
                <span className="rounded-full bg-white/5 px-3 py-1 text-xs uppercase">{gap.gapType}</span>
                <span className="rounded-full bg-rose-500/20 px-3 py-1 text-xs text-[#A1A1AA]">{gap.severity}</span>
              </div>
            </div>
            <p className="mt-3 text-sm text-[#A1A1AA]">{gap.explanation}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
